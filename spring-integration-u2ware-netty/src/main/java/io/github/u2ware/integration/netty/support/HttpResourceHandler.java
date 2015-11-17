package io.github.u2ware.integration.netty.support;

import static io.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.CharsetUtil;
import io.netty.util.IllegalReferenceCountException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

@Sharable
public class HttpResourceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    protected Log logger = LogFactory.getLog(getClass());
    
	public ResourceLoader resourceLoader;
	public String resourceLocation;

	public HttpResourceHandler(ResourceLoader resourceLoader, String resourceLocation){
		this.resourceLoader = resourceLoader;
		this.resourceLocation = resourceLocation;
	}
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.getUri();
    	logger.info(ctx.channel() + " Request Uri: "+uri);
        
        
        final Resource resource = sanitizeResource(resourceLoader, resourceLocation, uri);
    	
    	
        if (resource == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        //Thread.sleep(10000);
//    	logger.info(ctx.channel() + " Request Resource: "+resource.exists());
//    	logger.info(ctx.channel() + " Request Resource: "+resource.getClass());
//    	logger.info(ctx.channel() + " Request Resource: "+resource.getFilename());
//    	logger.info(ctx.channel() + " Request Resource: "+resource.getURL());
//    	logger.info(ctx.channel() + " Request Resource: "+resource.contentLength());
//    	logger.info(ctx.channel() + " Request Resource: "+resource.lastModified());
        
    	long contentLength = resource.contentLength();
    	long lastModified = resource.lastModified();
    	
        // Cache Validation
        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = lastModified / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
            	logger.info(ctx.channel() + " Response complete.");
                return;
            }
        }
    	
    	
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpHeaders.setContentLength(response, contentLength);
        setContentTypeHeader(response, resource);
        setDateAndCacheHeaders(response, resource);
        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                    ctx.write(new ResourceFileRegion(resource, 0, contentLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture = null;
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedResource(resource, 0, contentLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                	logger.info(future.channel() + " Response progress: " + progress);
                } else {
                	logger.info(future.channel() + " Response progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
            	logger.info(future.channel() + " Response complete.");
            }
        });

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static Resource sanitizeResource(ResourceLoader resourceLoader, String resourcePrefix, String uri) {
    	
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
            uri.contains('.' + File.separator) ||
            uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
            INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        
        String path = resourcePrefix+uri;
        //return SystemPropertyUtil.get("user.dir") + File.separator + uri;
        Resource result = resourceLoader.getResource(path);
        
        if(result.exists()){
            return result;
        }
        return null;
    }
	
    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx
     *            Context
     */
    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response
     *            HTTP response
     */
    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     * @throws IOException 
     */
    private static void setDateAndCacheHeaders(HttpResponse response, Resource resource) throws IOException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                LAST_MODIFIED, dateFormatter.format(new Date(resource.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private static void setContentTypeHeader(HttpResponse response, Resource resource) {
    	ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(resource.getFilename()));
    }
    
    
    private static class ResourceFileRegion extends AbstractReferenceCounted implements FileRegion {

        private final InputStream in;
        private final long position;
        private final long count;
        private long transfered;
    	
        private ResourceFileRegion(Resource resource, long position, long count) throws IOException {
            if (resource == null) {
                throw new NullPointerException("file");
            }
            if (position < 0) {
                throw new IllegalArgumentException("position must be >= 0 but was " + position);
            }
            if (count < 0) {
                throw new IllegalArgumentException("count must be >= 0 but was " + count);
            }
            this.in = resource.getInputStream();
            this.position = position;
            this.count = count;
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        public long count() {
            return count;
        }

        @Override
        public long transfered() {
            return transfered;
        }

		@Override
		public long transferTo(WritableByteChannel target, long position) throws IOException {
	        long count = this.count - position;
	        if (count < 0 || position < 0) {
	            throw new IllegalArgumentException(
	                    "position out of range: " + position +
	                    " (expected: 0 - " + (this.count - 1) + ')');
	        }
	        if (count == 0) {
	            return 0L;
	        }
	        if (refCnt() == 0) {
	            throw new IllegalReferenceCountException(0);
	        }
	        
	        
	        ByteBuf src = Unpooled.buffer();
	        src.writeBytes(in, (int)count);
	        
	        long written = target.write(src.nioBuffer());

	        if (written > 0) {
	            transfered += written;
	        }
	        return written;
		}

		@Override
		protected void deallocate() {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    private static class ChunkedResource implements ChunkedInput<ByteBuf> {
    	
        private final InputStream in;
        private final long endOffset;
        private final int chunkSize;
        private long offset;

        /**
         * Creates a new instance that fetches data from the specified file.
         *
         * @param chunkSize the number of bytes to fetch on each
         *                  {@link #readChunk(ChannelHandlerContext)} call
         */
        private ChunkedResource(Resource resource, int chunkSize) throws IOException {
            this(resource, 0, resource.contentLength(), chunkSize);
        }

        private ChunkedResource(Resource resource, long offset, long length, int chunkSize) throws IOException {
            if (resource == null) {
                throw new NullPointerException("resource");
            }
            if (offset < 0) {
                throw new IllegalArgumentException(
                        "offset: " + offset + " (expected: 0 or greater)");
            }
            if (length < 0) {
                throw new IllegalArgumentException(
                        "length: " + length + " (expected: 0 or greater)");
            }
            if (chunkSize <= 0) {
                throw new IllegalArgumentException(
                        "chunkSize: " + chunkSize +
                        " (expected: a positive integer)");
            }

            this.in = resource.getInputStream();
            this.offset = offset;
            endOffset = offset + length;
            this.chunkSize = chunkSize;
        }

        @Override
        public boolean isEndOfInput() throws Exception {
            return !(offset < endOffset);
        }

        @Override
        public void close() throws Exception {
            in.close();
        }

        @Override
        public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
            long offset = this.offset;
            if (offset >= endOffset) {
                return null;
            }

            int chunkSize = (int) Math.min(this.chunkSize, endOffset - offset);
            // Check if the buffer is backed by an byte array. If so we can optimize it a bit an safe a copy

            ByteBuf buf = ctx.alloc().heapBuffer(chunkSize);
            boolean release = true;
            try {
                in.read(buf.array(), buf.arrayOffset(), chunkSize);
                
                buf.writerIndex(chunkSize);
                
                
                this.offset = offset + chunkSize;
                release = false;
                return buf;
            } finally {
                if (release) {
                    buf.release();
                }
            }
        }
        
    }
}
