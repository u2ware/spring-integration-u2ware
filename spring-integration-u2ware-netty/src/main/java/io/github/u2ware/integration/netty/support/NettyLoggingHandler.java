package io.github.u2ware.integration.netty.support;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.commons.logging.Log;

public class NettyLoggingHandler extends ChannelDuplexHandler{

	private final Log nettyLogger;
	private final boolean printMessage;
	
	public NettyLoggingHandler(Log nettyLogger){
		this(nettyLogger, true);
	}
	public NettyLoggingHandler(Log nettyLogger, boolean printMessage){
		this.nettyLogger = nettyLogger;
		this.printMessage = printMessage;
	}
	
	
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    	nettyLogger.info(format(ctx, "REGISTERED"));
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    	nettyLogger.info(format(ctx, "UNREGISTERED"));
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
    	nettyLogger.info(format(ctx, "ACTIVE"));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	nettyLogger.info(format(ctx, "INACTIVE"));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	nettyLogger.info(format(ctx, "EXCEPTION: " + cause), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    	nettyLogger.info(format(ctx, "USER_EVENT: " + evt));
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    	nettyLogger.info(format(ctx, "BIND(" + localAddress + ')'));
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    	nettyLogger.info(format(ctx, "CONNECT(" + remoteAddress + ", " + localAddress + ')'));
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	nettyLogger.info(format(ctx, "DISCONNECT()"));
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	nettyLogger.info(format(ctx, "CLOSE()"));
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    	nettyLogger.info(format(ctx, "DEREGISTER()"));
        super.deregister(ctx, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(printMessage){
    		nettyLogger.info(format(ctx, formatMessage("RECEIVED", msg)));
        }else{
    		nettyLogger.info(format(ctx, formatLength("RECEIVED", msg)));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(printMessage){
    		nettyLogger.info(format(ctx, formatMessage("WRITE", msg)));
        }else{
    		nettyLogger.info(format(ctx, formatLength("WRITE", msg)));
        }
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
    	nettyLogger.info(format(ctx, "FLUSH"));
        ctx.flush();
    }
	
    protected String format(ChannelHandlerContext ctx, String message) {
        String chStr = ctx.channel().toString();
        return new StringBuilder(chStr.length() + message.length() + 1)
        .append(chStr)
        .append(' ')
        .append(message)
        .toString();
    }
    
    protected String formatLength(String eventName, Object msg) {
        if (msg instanceof ByteBuf) {
            int length = ((ByteBuf)msg).readableBytes();
            return formatNonByteBuf(eventName, ""+length+" bytes");

        } else if (msg instanceof ByteBufHolder) {
            ByteBuf content = ((ByteBufHolder)msg).content();
            int length = content.readableBytes();
            return formatNonByteBuf(eventName, ""+length+" bytes");
        
        } else {
            return formatNonByteBuf(eventName, msg);
        }
    }
    
    protected String formatMessage(String eventName, Object msg) {
        if (msg instanceof ByteBuf) {
            return formatByteBuf(eventName, (ByteBuf) msg);
        } else if (msg instanceof ByteBufHolder) {
            return formatByteBufHolder(eventName, (ByteBufHolder) msg);
        } else {
            return formatNonByteBuf(eventName, msg);
        }
    }

    /**
     * Returns a String which contains all details to log the {@link ByteBuf}
     */
    protected String formatByteBuf(String eventName, ByteBuf msg) {
        int length = msg.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(eventName.length() + 4);
            buf.append(eventName).append(": 0B");
            return buf.toString();
        } else {
            int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
            StringBuilder buf = new StringBuilder(eventName.length() + 2 + 10 + 1 + 2 + rows * 80);

            buf.append(eventName).append(": ").append(length).append('B').append(NEWLINE);
            appendPrettyHexDump(buf, msg);

            return buf.toString();
        }
    }

    /**
     * Returns a String which contains all details to log the {@link Object}
     */
    protected String formatNonByteBuf(String eventName, Object msg) {
        return eventName + ": " + msg;
    }

    /**
     * Returns a String which contains all details to log the {@link ByteBufHolder}.
     *
     * By default this method just delegates to {@link #formatByteBuf(String, ByteBuf)},
     * using the content of the {@link ByteBufHolder}. Sub-classes may override this.
     */
    protected String formatByteBufHolder(String eventName, ByteBufHolder msg) {
        String msgStr = msg.toString();
        ByteBuf content = msg.content();
        int length = content.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(eventName.length() + 2 + msgStr.length() + 4);
            buf.append(eventName).append(", ").append(msgStr).append(", 0B");
            return buf.toString();
        } else {
            int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
            StringBuilder buf = new StringBuilder(
                    eventName.length() + 2 + msgStr.length() + 2 + 10 + 1 + 2 + rows * 80);

            buf.append(eventName).append(": ")
               .append(msgStr).append(", ").append(length).append('B').append(NEWLINE);
            appendPrettyHexDump(buf, content);

            return buf.toString();
        }
    }
}
