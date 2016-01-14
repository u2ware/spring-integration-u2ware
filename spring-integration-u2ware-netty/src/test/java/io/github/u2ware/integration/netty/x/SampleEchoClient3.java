package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpClient;
import io.github.u2ware.integration.netty.support.NettyLoggingHandler;
import io.github.u2ware.integration.netty.support.NettyMessagingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.MessageChannel;

public class SampleEchoClient3 extends AbstractTcpClient{
	
	private static Log logger = LogFactory.getLog(SampleEchoClient3.class);
	
	
	private MessageChannel sendChannel;

	public void setSendChannel(MessageChannel sendChannel) {
		this.sendChannel = sendChannel;
	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {		

		pipeline.addLast(new NettyLoggingHandler(getClass()));
		
		pipeline.addLast("write_idle", new IdleStateHandler(3000, 0, 0, TimeUnit.MILLISECONDS));
		pipeline.addLast(new ByteToMessageDecoder() {

			@Override
			public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
				logger.debug("userEventTriggered");
				logger.debug("userEventTriggered");
				logger.debug("userEventTriggered");
				ctx.writeAndFlush(Unpooled.wrappedBuffer("Hello\n".getBytes()));
			}
			
			@SuppressWarnings("unused")
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
		pipeline.addLast(new NettyMessagingHandler(sendChannel));
	}
}
