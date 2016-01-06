package io.github.u2ware.integration.netty.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;
import io.netty.channel.rxtx.RxtxChannelOption;
import io.netty.channel.rxtx.RxtxDeviceAddress;

public abstract class AbstractSerialPort extends ChannelInitializer<Channel> {

	private String portName;
	private int baudrate = -1;
	private Databits databits;
	private Stopbits stopbits;
	private Paritybit paritybit;
	//private int flowControlMode = -1;
	//private int enableReceiveTimeout = -1;
	
	private EventLoopGroup group;
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public int getBaudrate() {
		return baudrate;
	}
	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}
	public Databits getDatabits() {
		return databits;
	}
	public void setDatabits(Databits databits) {
		this.databits = databits;
	}
	public Stopbits getStopbits() {
		return stopbits;
	}
	public void setStopbits(Stopbits stopbits) {
		this.stopbits = stopbits;
	}
	public Paritybit getParitybit() {
		return paritybit;
	}
	public void setParitybit(Paritybit paritybit) {
		this.paritybit = paritybit;
	}

	public void afterPropertiesSet() throws Exception {
	
        this.group = new OioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group)
         .channel(RxtxChannel.class)
         .handler(this);
        
        initBootstrap(b);
        
    	b.connect(new RxtxDeviceAddress(portName)).sync();
	}

	public void destroy() throws Exception {
    	if(group != null){
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
    	}
	}

	protected void initBootstrap(Bootstrap b) {
        if(baudrate != -1){
        	b.option(RxtxChannelOption.BAUD_RATE, baudrate);
        }
        if(databits != null){
        	b.option(RxtxChannelOption.DATA_BITS, databits);
        }
        if(stopbits != null){
        	b.option(RxtxChannelOption.STOP_BITS, stopbits);
        }
        if(paritybit != null){
        	b.option(RxtxChannelOption.PARITY_BIT, paritybit);
        }
	}
	@Override
	protected void initChannel(Channel ch) throws Exception{
		ChannelPipeline pipeline = ch.pipeline();
		initChannelPipeline(pipeline);
	}
	protected abstract void initChannelPipeline(ChannelPipeline pipeline)throws Exception;
}
