package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.core.AbstractTcpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class SampleEchoServer extends AbstractTcpServer{

	public static void main(String[] args) throws Exception{
		int port = 10900;
		try{
			port = Integer.parseInt(args[0]);
		}catch(Exception e){
		}
		SampleEchoServer.startup(port);
	}
	
	private static SampleEchoServer echoServer;
	
	public static void startup(int port) throws Exception{
		echoServer = new SampleEchoServer();
		echoServer.setPort(port);
		echoServer.afterPropertiesSet();
	}
	public static void shutdown() throws Exception{
		echoServer.destroy();
	}
	
	
	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {
		//pipeline.addLast(new LoggingHandler(getClass()));
		pipeline.addLast(new LineBasedFrameDecoder(256, false, false));
		pipeline.addLast(new ChannelInboundHandlerAdapter(){
    		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    			ctx.writeAndFlush(msg);
    		}
        });
	}
}