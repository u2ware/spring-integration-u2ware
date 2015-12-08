package io.github.u2ware.integration.netty.test.echo;

import io.github.u2ware.integration.netty.support.NettyMessageHandler;
import io.github.u2ware.integration.netty.support.NettyTcpClient;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

public class EchoClient extends NettyTcpClient{
	
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
		pipeline.addLast(new LoggingHandler(getClass()));
		pipeline.addLast(new StringEncoder());
		pipeline.addLast(new LineBasedFrameDecoder(256));
		pipeline.addLast(new StringDecoder());
		pipeline.addLast(new NettyMessageHandler(sendChannel, receiveChannel, 100));
	}
}