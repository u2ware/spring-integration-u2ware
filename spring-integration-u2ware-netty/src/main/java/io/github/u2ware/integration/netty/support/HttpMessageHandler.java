package io.github.u2ware.integration.netty.support;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.PollableChannel;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
*
* @author u2waremanager@gamil.com
*/
@Sharable
public class HttpMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	
	protected Log logger = LogFactory.getLog(getClass());

	private MessagingTemplate template;
	private MessageChannel sendChannel;
	private PollableChannel receiveChannel;

	public HttpMessageHandler(MessageChannel sendChannel, PollableChannel receiveChannel){
		this(sendChannel, receiveChannel, 3000);
	}
	public HttpMessageHandler(MessageChannel sendChannel, PollableChannel receiveChannel, long timeout){
		this.template = new MessagingTemplate();
		template.setReceiveTimeout(timeout);
		template.setSendTimeout(timeout);
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
	}
	public HttpMessageHandler(MessageChannel sendChannel, PollableChannel receiveChannel, MessagingTemplate template){
		Assert.notNull(template, "template must not be null.");
		this.template = template;
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
	}
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		StringWriter errors = new StringWriter();
		cause.printStackTrace(new PrintWriter(errors));
		String payload = errors.toString();
		
		ByteBuf content = ByteBufUtil.encodeString(UnpooledByteBufAllocator.DEFAULT, CharBuffer.wrap(payload), Charset.defaultCharset());
		HttpResponse err = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
		logger.info("Send HTTP Error response:\n" + err, cause);
		ctx.writeAndFlush(err).addListener(ChannelFutureListener.CLOSE);
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

		logger.info("Received HTTP request:\n" + request);
		
		Message<?> sendMessage = toMessage(ctx, request);
		//logger.debug("Send Message Header: " + requestMessage.getHeaders());
		//logger.debug("Send Message Payload: " + requestMessage.getPayload());
		template.send(sendChannel, sendMessage);
		
		Message<?> responseMessage = template.receive(receiveChannel);
		if(responseMessage == null) throw new Exception("response channel message is not found");
		//logger.info("Receive Message waiting for "+template.getReceiveTimeout()+"ms.....");
		//logger.debug("Receive Message Header: " + responseMessage.getHeaders());
		//logger.debug("Receive Message Payload: " + responseMessage.getPayload().getClass());

		FullHttpResponse response = fromMessage(ctx, responseMessage);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		logger.info("Send HTTP response:\n" + response);
    }

	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

	public Message<?> toMessage(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception{
		
		/////////////////////
		//Message Headers
		//////////////////////
		Map<String, Object> messageHeaders = Maps.newHashMap();
		messageHeaders.put(NettyHeaders.REMOTE_ADDRESS, ctx.channel().remoteAddress().toString());

		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		Charset charsetToUse = null;
		boolean binary = false;
		for (Entry<String, String> entry : request.headers()) {
			if (entry.getKey().equalsIgnoreCase("Content-Type")) {
				MediaType contentType = MediaType.parseMediaType(entry.getValue());
				charsetToUse = contentType.getCharSet();
				messageHeaders.put(MessageHeaders.CONTENT_TYPE, entry.getValue());
				binary = MediaType.APPLICATION_OCTET_STREAM.equals(contentType);
			
			}else if (!entry.getKey().toUpperCase().startsWith("ACCEPT")
				&& !entry.getKey().toUpperCase().equals("CONNECTION")) {
				messageHeaders.put(entry.getKey(), entry.getValue());
			}
		}
		messageHeaders.put(NettyHeaders.HTTP_REQUEST_PATH, request.getUri());
		messageHeaders.put(NettyHeaders.HTTP_REQUEST_METHOD, request.getMethod().toString());
		if(keepAlive){
			messageHeaders.put(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		/////////////////////
		//Message Payload
		//////////////////////
		ByteBuf buf = request.content();
		MessageBuilder<?> builder;
		if (binary) {
			byte[] content = buf.array();
			//logger.debug("HTTP Request Content: "+content);
			builder = MessageBuilder.withPayload(content);
		
		}else {
			// ISO-8859-1 is the default http charset when not set
			charsetToUse = charsetToUse == null ? Charset.forName("ISO-8859-1") : charsetToUse;
			String content = buf.toString(charsetToUse);
			
			//logger.debug("HTTP Request Content: "+content);
			builder = MessageBuilder.withPayload(content);
		}
		builder.copyHeaders(messageHeaders);
		return builder.build();
	}
	
	public FullHttpResponse fromMessage(ChannelHandlerContext ctx, Message<?> message) {
		
		String payload = message.getPayload().toString();
		ByteBuf content = ByteBufUtil.encodeString(UnpooledByteBufAllocator.DEFAULT, CharBuffer.wrap(payload), Charset.defaultCharset());
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

		/////////////////////
		//Http Headers
		/////////////////////
		for(String key : message.getHeaders().keySet()){
			if(MessageHeaders.CONTENT_TYPE.equals(key)){
				response.headers().set(HttpHeaders.Names.CONTENT_TYPE, message.getHeaders().get(key));
				//logger.debug("HTTP Response Headers: "+HttpHeaders.Names.CONTENT_TYPE+"="+message.getHeaders().get(key));
			}else{
				response.headers().set(key, message.getHeaders().get(key));			
				//logger.debug("HTTP Response Headers: "+key+"="+message.getHeaders().get(key));
			}
        }
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
		response.headers().set("Access-Control-Allow-Origin", "*");
		response.headers().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.headers().set("Access-Control-Max-Age", "3600");
		response.headers().set("Access-Control-Allow-Headers", "x-requested-with");		

		return response;
	}
}