package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpClient;
import io.github.u2ware.integration.netty.support.NettyLoggingHandler;
import io.github.u2ware.integration.netty.support.NettyMessagingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

public class SampleEchoClient2 extends AbstractTcpClient{
	
	private static Log logger = LogFactory.getLog(SampleEchoClient2.class);
	
	
	private MessageChannel sendChannel;
	private PollableChannel receiveChannel;

	public void setSendChannel(MessageChannel sendChannel) {
		this.sendChannel = sendChannel;
	}
	public void setReceiveChannel(PollableChannel receiveChannel) {
		this.receiveChannel = receiveChannel;
	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {		

		pipeline.addLast(new NettyLoggingHandler(getClass()));
		
		pipeline.addLast(new ByteToMessageCodec<Object>() {

			@Override
			protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
				logger.debug("encode "+msg);
				logger.debug("encode "+msg.getClass());
				out.writeBytes(msg.toString().getBytes());
			}

			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				logger.debug("decode "+in);
				logger.debug("decode "+in.getClass());
				
				out.add(in.toString(CharsetUtil.UTF_8));
			}
		});
		/*
		pipeline.addLast(new MessageToByteEncoder<Object>() {
			protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
				logger.debug("encode "+msg);
				logger.debug("encode "+msg.getClass());
				out.writeBytes(msg.toString().getBytes());
			}
		});
		pipeline.addLast(new ByteToMessageDecoder() {
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				logger.debug("decode "+in);
				logger.debug("decode "+in.getClass());
				
				out.add(in.toString(CharsetUtil.UTF_8));
			}
		});
		*/
		pipeline.addLast(new NettyMessagingHandler(sendChannel, receiveChannel));
	}
}
