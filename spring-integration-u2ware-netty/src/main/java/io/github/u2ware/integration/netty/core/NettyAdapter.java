package io.github.u2ware.integration.netty.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public abstract class NettyAdapter extends ChannelInitializer<Channel> implements InitializingBean, DisposableBean{

	protected Log logger = LogFactory.getLog(getClass());

	private String host;
	private int port;
	private boolean ssl;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isSsl() {
		return ssl;
	}
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception{
		ChannelPipeline pipeline = ch.pipeline();
		if(isSsl()){
			SslHandler handler = createSslHandler(ch);
			if(handler != null){
				pipeline.addLast(handler);
			}
		}
		initChannelPipeline(pipeline);
	}
	protected abstract void initChannelPipeline(ChannelPipeline pipeline)throws Exception;

	protected abstract SslHandler createSslHandler(Channel channel)throws Exception;
}