package io.github.u2ware.integration.netty.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

public abstract class NettyTcpClient extends NettyAdapter{
	
	private EventLoopGroup group;
	
	@Override
	public void afterPropertiesSet() throws Exception {
        this.group = new NioEventLoopGroup();
        connect(new Bootstrap(), group);
	}
	
	
	private void connect(Bootstrap b, EventLoopGroup group) {

        b.group(group)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .handler(this);
        initBootstrap(b);
        
        
        b.connect(getHost(), getPort()).addListener(new ChannelFutureListener(){

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {

				if(future.isSuccess()){
			        logger.info("["+getHost()+":"+getPort()+"] is opened. ");
			        System.out.println("["+getHost()+":"+getPort()+"] is opened. ");
				}else{
			        logger.info("["+getHost()+":"+getPort()+"] is failured. ");
			        System.out.println("["+getHost()+":"+getPort()+"] is failured. ");

					final EventLoop loop = future.channel().eventLoop();  
					loop.schedule(new Runnable() {
						@Override
						public void run() {
							connect(new Bootstrap(), loop);
						}  
						
					}, 3, TimeUnit.SECONDS);
				}
			}
        });
	}
	
	
	@Override
	public void destroy() throws Exception {
        try {
            // Wait until the connection is closed.
            //f.channel().closeFuture().sync();
        } finally {
        	if(group != null)
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
	        logger.info("["+getHost()+":"+getPort()+"] is closed. ");
	        System.out.println("["+getHost()+":"+getPort()+"] is closed. ");

            group = null;
        }
	}
	
	protected void initBootstrap(Bootstrap b) {

	}

	@Override
	protected SslHandler createSslHandler(Channel channel) throws SSLException {
		SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		return sslCtx.newHandler(channel.alloc(), getHost(), getPort());
	}
}
