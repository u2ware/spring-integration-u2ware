package io.github.u2ware.integration.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
*
* @author u2waremanager@gamil.com
*/
public class NettyHttpMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	

	protected Log logger = LogFactory.getLog(getClass());

	private MessagingTemplate template = new MessagingTemplate();
	private MessageChannel receiveChannel;
	private MessageChannel sendChannel;
	private MessageConverter converter;
	
	public NettyHttpMessageHandler(MessageChannel sendChannel, MessageChannel receiveChannel, long timeout){
		this(new NettyHttpMessageConverter(), sendChannel, receiveChannel, timeout);
	}	
	public NettyHttpMessageHandler(MessageConverter converter, MessageChannel sendChannel, MessageChannel receiveChannel, long timeout){
		Assert.notNull(converter, "converter must not be null.");
		Assert.notNull(sendChannel, "sendChannel must not be null.");
		Assert.notNull(receiveChannel, "receiveChannel must not be null.");
		this.converter = converter;
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
		template.setReceiveTimeout(timeout);
		template.setSendTimeout(timeout);
	}	
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

		Assert.isInstanceOf(FullHttpRequest.class, msg);

		FullHttpRequest request = (FullHttpRequest) msg;
		logger.info("Received HTTP request:\n" + request);
		
		
		Map<String, Object> headers = Maps.newHashMap();
		headers.put(NettyHeaders.REMOTE_ADDRESS, ctx.channel().remoteAddress().toString());

		Message<?> requestMessage = converter.toMessage(request, new MessageHeaders(headers));
		logger.debug("Send Request Message Header: " + requestMessage.getHeaders());
		logger.debug("Send Request Message Payload: " + requestMessage.getPayload());
		template.send(sendChannel, requestMessage);

		
		logger.debug("Receive Response Message Header waiting for "+template.getReceiveTimeout()+"ms.....");
		Message<?> responseMessage = template.receive(receiveChannel);
		if(responseMessage == null) throw new Exception("response channel message is not found");
		logger.debug("Receive Response Message Header: " + responseMessage.getHeaders());
		logger.debug("Receive Response Message Payload: " + responseMessage.getPayload().getClass());

		FullHttpResponse response = (FullHttpResponse)converter.fromMessage(responseMessage, HttpResponse.class);

		boolean keepAlive = HttpHeaders.isKeepAlive(request);
        if(keepAlive){
			response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			response.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
		
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		logger.info("Send HTTP response:\n" + response);
    }


	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.debug("", cause);

		StringWriter errors = new StringWriter();
		cause.printStackTrace(new PrintWriter(errors));
		String payload = errors.toString();
		
		ByteBuf content = ByteBufUtil.encodeString(UnpooledByteBufAllocator.DEFAULT, CharBuffer.wrap(payload), Charset.defaultCharset());
		HttpResponse err = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
		logger.info("Send HTTP Error response:\n" + err);
		ctx.writeAndFlush(err).addListener(ChannelFutureListener.CLOSE);
    }

}