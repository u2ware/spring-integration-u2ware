package io.github.u2ware.integration.netty.support;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

@Sharable
public class NettyMessagingHandler extends ChannelDuplexHandler {

	private InternalLogger logger;
	private ScheduledFuture<?> scheduledFuture;
	private Object sendMessage;

	private final MessagingTemplate template;
	private final PollableChannel receiveChannel;
	private final MessageChannel sendChannel;
	private final boolean useSendMessage;

	public NettyMessagingHandler(Class<?> clazz, MessageChannel sendChannel){
		this(clazz, null, sendChannel, false);
	}
	public NettyMessagingHandler(Class<?> clazz, PollableChannel receiveChannel){
		this(clazz, receiveChannel, null, false);
	}
	public NettyMessagingHandler(Class<?> clazz, PollableChannel receiveChannel, MessageChannel sendChannel){
		this(clazz, receiveChannel, sendChannel, false);
	}
	public NettyMessagingHandler(Class<?> clazz, PollableChannel receiveChannel, MessageChannel sendChannel, boolean useSendMessage){
		
		this.logger = InternalLoggerFactory.getInstance(clazz);
		this.template = new MessagingTemplate();
		template.setReceiveTimeout(1000);
		template.setSendTimeout(1000);
		this.receiveChannel = receiveChannel;
		this.sendChannel = sendChannel;
		this.useSendMessage = useSendMessage;
	}
	
	
	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
		if(receiveChannel != null){
			scheduledFuture = ctx.executor().scheduleAtFixedRate(new Runnable(){
				public void run() {
					Message<?> message = template.receive(receiveChannel);
	        		if(message != null){
	        			if( useSendMessage ){
	        				if(sendChannel != null && sendMessage != null){
		            			template.convertAndSend(sendChannel, sendMessage);
	        				}else{
		            			template.convertAndSend(sendChannel, "{}");
	        				}
	    	    			logger.info("MESSAGE RECEIVED AND SEND");
	        			}else{
		        			logger.info("MESSAGE RECEIVED ");
	            			ctx.writeAndFlush(message.getPayload());
	        			}
	        		}
				}
				
			}, 100, 100, TimeUnit.MICROSECONDS);
		}
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx)throws Exception {
		if(receiveChannel != null){
			scheduledFuture.cancel(true);
		}
	}

	@Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {

		if(sendChannel != null){
    		ctx.executor().submit(new Runnable() {
				public void run() {
	    			template.convertAndSend(sendChannel, msg);
	    			logger.info("MESSAGE SEND ");
				}
			});

    		if(useSendMessage){
				this.sendMessage = msg;
			}
    	}
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.info("MESSAGE EXCEPTION: "+cause.getMessage());
	}
}
