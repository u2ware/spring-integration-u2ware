package io.github.u2ware.integration.netty.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractTcpClient extends ChannelInitializer<Channel> implements InitializingBean, DisposableBean{
	
	protected InternalLogger logger = InternalLoggerFactory.getInstance(getClass());
	
	private String host;
	private int port;
	private boolean ssl;
	private boolean autoConnection;
	private EventLoopGroup group;

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
	public boolean isAutoConnection() {
		return autoConnection;
	}
	public void setAutoConnection(boolean autoConnection) {
		this.autoConnection = autoConnection;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        this.group = new NioEventLoopGroup();
        connect(new Bootstrap(), group);
	}
	@Override
	public void destroy() throws Exception {
		disconnect(group);
	}

	///////////////////
	//
	///////////////////
	private void connect(Bootstrap b, EventLoopGroup group)  {
        b.group(group)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .handler(this);
        
        initBootstrap(b);
        
        if(isAutoConnection()){
            b.connect(getHost(), getPort()).addListener(new ConnectionListener());
        }else{
        	b.connect(getHost(), getPort());
        }
	}

	private void disconnect(EventLoopGroup group) {
    	if(group != null){
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
    	}
	}
	
	protected void initBootstrap(Bootstrap b) {

	}
	@Override
	protected void initChannel(Channel ch) throws Exception{
		ChannelPipeline pipeline = ch.pipeline();
		
		if(isAutoConnection()){
			pipeline.addLast(new ConnectionHandler());
		}
		if(isSsl()){
			SslHandler handler = createSslHandler(ch);
			if(handler != null){
				pipeline.addLast(handler);
			}
		}
		initChannelPipeline(pipeline);
	}
	protected abstract void initChannelPipeline(ChannelPipeline pipeline)throws Exception;

	protected SslHandler createSslHandler(Channel channel) throws SSLException {
		return null;
		//SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		//return sslCtx.newHandler(channel.alloc(), getHost(), getPort());
	}
	
	
	@Sharable
	private class ConnectionHandler extends ChannelInboundHandlerAdapter{
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			final EventLoop eventLoop = ctx.channel().eventLoop();  
			eventLoop.schedule(new Runnable() {  
				public void run() {  
					connect(new Bootstrap(), eventLoop);  
				}  
			}, 3, TimeUnit.SECONDS);  
			super.channelInactive(ctx);
		}
	}
	
	private class ConnectionListener implements ChannelFutureListener{

		public void operationComplete(ChannelFuture future) throws Exception {
			if(! future.isSuccess()){
				final EventLoop loop = future.channel().eventLoop();  
				loop.schedule(new Runnable() {
					@Override
					public void run() {
						connect(new Bootstrap(), loop);
					}  
				}, 3, TimeUnit.SECONDS);
			}
		}
	}
}
