Spring Integration Netty Adapter
=================================================

#Introduction 

[NettyChannel](http://netty.io/wiki/user-guide-for-4.x.html) 과 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 간 메세지를 처리하는 Channel Adapter 를 작성하기 위한 라이브러리입니다. [netty](https://netty.io) 라이브러리를 사용하고 있습니다.


```xml
<repositories>
    <repository>
        <id>u2ware-mvm-repo</id>
        <url>https://raw.github.com/u2ware/u2ware.github.com/mvn-repo/</url>
    </repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>io.github.u2ware</groupId>
		<artifactId>spring-integration-u2ware-netty</artifactId>
		<version>1.0.0</version>
	</dependency>
</dependencies>
```

## [NettyChannel](http://netty.io/wiki/user-guide-for-4.x.html)


EchoServer & EchoClient 예제와 같이 [Netty Channel](https://netty.io) 을 작성할 수 있습니다.
ChannelPipeline 설정에 관한 내용은 [netty.io](http://netty.io/wiki/user-guide-for-4.x.html)에서 도움을 받을수 있습니다.

```java
public class EchoServer extends AbstractTcpServer{   --(1)
	
	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception { 
		pipeline.addLast(new LineBasedFrameDecoder(256, false, false));
		pipeline.addLast(new ChannelInboundHandlerAdapter(){
    		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    			ctx.writeAndFlush(msg);
    			ctx.channel().read();
    		}
        });
	}
}

public class EchoClient extends AbstractTcpClient{   --(2)
	
	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception { 
		pipeline.addLast(new StringEncoder());
		pipeline.addLast(new LineBasedFrameDecoder(256));
		pipeline.addLast(new StringDecoder());
	}
}

```
1. [AbstractTcpServer](src/main/java/io/github/u2ware/integration/netty/core/AbstractTcpServer.java) 
2. [AbstractTcpClient](src/main/java/io/github/u2ware/integration/netty/core/AbstractTcpClient.java)  


##  [NettyMessagingHandler](src/main/java/io/github/u2ware/integration/netty/support/NettyMessagingHandler.java) 

[Netty Channel](https://netty.io) 과 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 간 메세지를 처리하기 위해 [NettyMessagingHandler](src/main/java/io/github/u2ware/integration/netty/support/NettyMessagingHandler.java) 를 사용 합니다.

```java
public class EchoClient extends AbstractTcpClient{  

	private PollableChannel receiveChannel;   //---(2)
	private MessageChannel sendChannel;       //---(3)	
	
	public void setSendChannel(MessageChannel sendChannel) {
		this.sendChannel = sendChannel;
	}
	public void setReceiveChannel(PollableChannel receiveChannel) {
		this.receiveChannel = receiveChannel;
	}

	@Override
	protected void initChannelPipeline(ChannelPipeline pipeline) throws Exception {		
		pipeline.addLast(new StringEncoder());
		pipeline.addLast(new LineBasedFrameDecoder(256));
		pipeline.addLast(new StringDecoder());
		pipeline.addLast(new NettyMessageHandler(getClass(), receiveChannel, sendChannel)); //---(1)
	}
}
```
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:int="http://www.springframework.org/schema/integration"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/integration 
		http://www.springframework.org/schema/integration/spring-integration.xsd">

	<int:channel id="echoRequest">
		<int:queue/>
	</int:channel>
	
	<bean id="echoClient1" class=".....EchoClient">
		<property name="host" value="127.0.0.1"/>
		<property name="port" value="9091"/>
		<property name="receiveChannel" ref="echoRequest"/>   <!-- (2) -->
		<property name="sendChannel" ref="echoResponse"/>     <!-- (3) -->
	</bean>
	
	<int:channel id="echoResponse">
		<int:queue/>
	</int:channel>
	
</beans>
	
```
1. [NettyMessagingHandler](src/main/java/io/github/u2ware/integration/netty/support/NettyMessagingHandler.java) 를 ChannelPipeline 에 추가하면,
2. receiveChannel 에 수신 가능한 메세지가 있을때, 이 메세지를 [NettyChannel](https://netty.io)에 write 합니다.
3. [NettyChannel](https://netty.io)에서 read 한 메세지가 있을 때, 이 메세지를 sendChannel 에 전송합니다.

##Sample Code

* [SampleEchoClient](src/test/java/io/github/u2ware/integration/netty/x/)


