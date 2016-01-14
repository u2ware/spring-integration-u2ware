package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpClient;
import io.github.u2ware.integration.netty.support.NettyLoggingHandler;
import io.github.u2ware.integration.netty.support.NettyMessagingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.CharsetUtil;

import java.nio.CharBuffer;
import java.util.List;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

public class SampleEchoClient2 extends AbstractTcpClient{
	
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
		
		
/*		
		pipeline.addLast(new MessageToByteEncoder<Object>() {
			protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
				logger.debug("encode "+msg.getClass());
				out.writeBytes(msg.toString().getBytes());
			}
		});
		pipeline.addLast(new ByteToMessageDecoder() {
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				logger.debug("decode "+in.getClass());
				out.add(in.toString(CharsetUtil.UTF_8));
				in.readBytes(in.readableBytes());
			}
		});
*/
		
/*
		pipeline.addLast(new ByteToMessageCodec<Object>() {
			@Override
			protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
				logger.debug("encode "+msg.getClass());
				out.writeBytes(msg.toString().getBytes());
			}

			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				logger.debug("decode "+in.getClass());
				out.add(in.toString(CharsetUtil.UTF_8));
				in.readBytes(in.readableBytes());
			}
		});
		
*/
		pipeline.addLast(new MessageToMessageCodec<ByteBuf, Object>() {

			@Override
			protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
				logger.debug("encode "+msg.getClass());
				ByteBuf b = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg.toString()), CharsetUtil.UTF_8);
				out.add(b);
			}

			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
				logger.debug("decode "+msg.getClass());
				out.add(msg.toString(CharsetUtil.UTF_8));
			}
		});
		
		pipeline.addLast(new NettyMessagingHandler(getClass(), receiveChannel, sendChannel));
	}
	
	public static class Response{};
}
