package io.github.u2ware.integration.netty.support;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

public class HttpMessageServer extends NettyTcpServer{
	
	private MessageChannel sendChannel;
	private PollableChannel receiveChannel;
	private int maxContentLength = 1048576;
	private int messagingTimeout = 10000;

	public void setSendChannel(MessageChannel sendChannel) {
		this.sendChannel = sendChannel;
	}
	public void setReceiveChannel(PollableChannel receiveChannel) {
		this.receiveChannel = receiveChannel;
	}
	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}
	public void setMessagingTimeout(int messagingTimeout) {
		this.messagingTimeout = messagingTimeout;
	}
	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {
		pipeline.addLast(new HttpRequestDecoder());
	    pipeline.addLast(new HttpObjectAggregator(maxContentLength));
		pipeline.addLast(new HttpResponseEncoder());
	    pipeline.addLast(new HttpContentCompressor());
		pipeline.addLast(new HttpMessageHandler(sendChannel, receiveChannel, messagingTimeout));
	}
}
