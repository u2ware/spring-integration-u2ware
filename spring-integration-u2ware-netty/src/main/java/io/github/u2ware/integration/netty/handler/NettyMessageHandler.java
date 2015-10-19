package io.github.u2ware.integration.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

public class NettyMessageHandler extends ChannelDuplexHandler {

	protected static Log logger = LogFactory.getLog(NettyMessageHandler.class);

	private MessagingTemplate template = new MessagingTemplate();
	private MessageConverter converter;
	private MessageChannel sendChannel;
	private PollableChannel receiveChannel;

	private ScheduledFuture<?> scheduledFuture;

	public NettyMessageHandler(MessageChannel sendChannel, PollableChannel receiveChannel, long timeout){
		this(new NettyMessageConverter(), sendChannel, receiveChannel, timeout);
	}
	public NettyMessageHandler(MessageConverter converter, MessageChannel sendChannel, PollableChannel receiveChannel, long timeout){
		Assert.notNull(converter, "converter must not be null.");
		this.converter = converter;
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
		template.setReceiveTimeout(timeout);
		template.setSendTimeout(timeout);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		if(receiveChannel != null){
			//logger.debug("handlerAdded");
			Runnable worker = new ReceiveChannelWorker(this, ctx);
			scheduledFuture = ctx.executor().scheduleAtFixedRate(worker, 100, 100, TimeUnit.MICROSECONDS);
		}
		super.handlerAdded(ctx);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx)throws Exception {
		if(receiveChannel != null){
			//logger.debug("handlerRemoved");
			scheduledFuture.cancel(true);
		}
		super.handlerRemoved(ctx);
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(sendChannel != null){
    		//logger.debug("channelRead");
    		Runnable worker = new SendChannelWorker(this, ctx, msg);
    		ctx.executor().submit(worker);
    	}
    	super.channelRead(ctx, msg);
    }
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//logger.debug("exceptionCaught", cause);
		super.exceptionCaught(ctx, cause);
	}
	
	private static class ReceiveChannelWorker implements Runnable{

		private final NettyMessageHandler handler;
		private final ChannelHandlerContext ctx;
		
		private ReceiveChannelWorker(NettyMessageHandler handler, ChannelHandlerContext ctx){
			this.handler = handler;
			this.ctx = ctx;
		}
		
		@Override
		public void run() {
    		try{
	    		Message<?> message = handler.template.receive(handler.receiveChannel);
        		if(message != null){
    	    		Object msg = handler.converter.fromMessage(message, null);
            		ctx.writeAndFlush(msg);
            		logger.info("Write Netty Message: "+msg);
        		}
    		}catch(Exception e){
    			//e.printStackTrace();
    		}
		}
	}
	
	private static class SendChannelWorker implements Runnable{

		private final NettyMessageHandler handler;
		private final ChannelHandlerContext ctx;
		private final Object payload;
		
		private SendChannelWorker(NettyMessageHandler handler, ChannelHandlerContext ctx, Object payload){
			this.handler = handler;
			this.ctx = ctx;
			this.payload = payload;
		}
		
		@Override
		public void run() {
    		try{
    			Map<String, Object> headers = Maps.newHashMap();
    			headers.put(NettyHeaders.REMOTE_ADDRESS, ctx.channel().remoteAddress().toString());
    			
	    		Message<?> message = handler.converter.toMessage(payload, new MessageHeaders(headers));
        		if(message != null){
        			logger.info("Read Netty Message: "+payload);
        			handler.template.send(handler.sendChannel, message);
        		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
		}
	}
}
