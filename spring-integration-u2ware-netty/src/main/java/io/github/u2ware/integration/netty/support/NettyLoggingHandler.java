package io.github.u2ware.integration.netty.support;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyLoggingHandler extends LoggingHandler{

	private boolean printDump;

	public NettyLoggingHandler(Class<?> clazz) {
		super(clazz, LogLevel.INFO);
		this.printDump = true;
	}

	public NettyLoggingHandler(Class<?> clazz, boolean printDump) {
		super(clazz, LogLevel.INFO);
		this.printDump = printDump;
	}

	protected String formatMessage(String eventName, Object msg) {
		if(! printDump) return eventName;
		return super.formatMessage(eventName, msg);
	}
}
