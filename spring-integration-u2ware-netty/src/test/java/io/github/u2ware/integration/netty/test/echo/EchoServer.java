package io.github.u2ware.integration.netty.test.echo;

import io.github.u2ware.integration.netty.support.NettyTcpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class EchoServer extends NettyTcpServer{

	public static void main(String[] args) throws Exception{
		int port = 10601;
		try{
			port = Integer.parseInt(args[0]);
		}catch(Exception e){
		}

		EchoServer s = new EchoServer();
		s.setPort(port);
		s.afterPropertiesSet();
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