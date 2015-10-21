package io.github.u2ware.integration.netty.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.springframework.util.Assert;

public abstract class NettyTcpServer extends NettyAdapter{
	
	private EventLoopGroup bossGroup, workerGroup;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		Assert.notNull(getPort(), "NettyInitializer must not be null.");
		
        // Configure the server.
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .option(ChannelOption.SO_BACKLOG, 100)
         .childHandler(this);

        initBootstrap(b);
        
        // Start the server.
        b.bind(getPort()).sync();
        
		logger.info("[<local>:"+getPort()+"]  is opened.");
		System.out.println("[<local>:"+getPort()+"]  is opened. ");
	}
	
	@Override
	public void destroy() throws Exception {
        try {
            // Wait until the server socket is closed.
            //f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
        	if(bossGroup != null)
            bossGroup.shutdownGracefully();
        	if(workerGroup != null)
            workerGroup.shutdownGracefully();
    		logger.info("[<local>:"+getPort()+"]  is closed.");
    		System.out.println("[<local>:"+getPort()+"]  is closed. ");

    		bossGroup = null;
    		workerGroup = null;
        }
	}
	
	protected void initBootstrap(ServerBootstrap b) {
	}
	
	@Override
	protected SslHandler createSslHandler(Channel channel) throws SSLException, CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		return sslCtx.newHandler(channel.alloc());
	}
}
