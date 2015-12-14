package io.github.u2ware.integration.netty.support;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.util.Assert;

@Sharable
public class NettyMessagingHandler extends ChannelDuplexHandler {

	private ScheduledFuture<?> scheduledFuture;

	private final MessagingTemplate template;
	private final MessageChannel sendChannel;
	private final PollableChannel receiveChannel;

	public NettyMessagingHandler(MessageChannel sendChannel, PollableChannel receiveChannel){
		this.template = new MessagingTemplate();
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
	}
	public NettyMessagingHandler(MessageChannel sendChannel, PollableChannel receiveChannel, long timeout){
		this.template = new MessagingTemplate();
		template.setReceiveTimeout(timeout);
		template.setSendTimeout(timeout);
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
	}
	public NettyMessagingHandler(MessageChannel sendChannel, PollableChannel receiveChannel, MessagingTemplate template){
		Assert.notNull(template, "template must not be null.");
		this.template = template;
		this.sendChannel = sendChannel;
		this.receiveChannel = receiveChannel;
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
    		Runnable worker = new SendChannelWorker(this, msg);
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

		private final MessagingTemplate template;
		private final PollableChannel receiveChannel;
		private final ChannelHandlerContext ctx;
		
		private ReceiveChannelWorker(NettyMessagingHandler handler, ChannelHandlerContext ctx){
			this.ctx = ctx;
			this.template = handler.template;
			this.receiveChannel = handler.receiveChannel;
		}
		
		@Override
		public void run() {
    		try{
	    		Message<?> message = template.receive(receiveChannel);
        		if(message != null){
            		ctx.writeAndFlush(message.getPayload());
        		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
		}
	}
	
	private static class SendChannelWorker implements Runnable{

		private final MessagingTemplate template;
		private final MessageChannel sendChannel;
		private final Object payload;
		
		private SendChannelWorker(NettyMessagingHandler handler, Object payload){
			this.template = handler.template;
			this.sendChannel = handler.sendChannel;
			this.payload = payload;
		}
		
		@Override
		public void run() {
    		try{
    			template.convertAndSend(sendChannel, payload);

    		}catch(Exception e){
    			e.printStackTrace();
    		}
		}
	}
}
