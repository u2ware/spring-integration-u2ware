package io.github.u2ware.integration.netty.support;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyRedirectingHandler extends ChannelInboundHandlerAdapter{

	private ChannelGroup destination;
	private ChannelGroup source;

	public NettyRedirectingHandler(ChannelGroup destination){
		this.destination = destination;
		this.source = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	}
	public NettyRedirectingHandler(ChannelGroup destination, ChannelGroup source){
		this.destination = destination;
		this.source = source;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		source.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		source.remove(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		destination.write(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		destination.flush();
	}
}
