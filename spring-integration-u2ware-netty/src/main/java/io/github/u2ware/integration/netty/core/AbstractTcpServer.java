package io.github.u2ware.integration.netty.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class AbstractTcpServer extends ChannelInitializer<Channel> implements InitializingBean, DisposableBean{

	protected Log logger = LogFactory.getLog(getClass());

	private int port;
	private boolean ssl;
	private EventLoopGroup bossGroup, workerGroup;

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
	
	protected SslHandler createSslHandler(Channel channel) throws SSLException, CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		return sslCtx.newHandler(channel.alloc());
	}
}
