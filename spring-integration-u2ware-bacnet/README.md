Spring Integration BACNet Adapter
=================================================

#Introduction 

[Remote BACNet Device](http://www.bacnet.org/) 와 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 간 메세지를 처리하는 Channel Adapter 입니다. 

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
		<artifactId>spring-integration-u2ware-bacnet</artifactId>
		<version>1.0.0</version>
	</dependency>
</dependencies>
```

Spring Context 설정에서 Namespace 선언이 필요합니다.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:int="http://www.springframework.org/schema/integration"
	xmlns:int-u2ware-bacnet="http://u2ware.github.io/schema/integration/bacnet"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/integration 
		http://www.springframework.org/schema/integration/spring-integration.xsd
		http://u2ware.github.io/schema/integration/bacnet 
		http://u2ware.github.io/schema/integration/bacnet/spring-integration-bacnet.xsd">
		
</bean>
```

##Inbound Channel Adapter

미리 정의된 요청 객체 ([BacnetRequest](src/main/java/io/github/u2ware/integration/core/inbound/BacnetRequest.java)) 를 이용하여 [Remote BACNet Device](http://www.bacnet.org/) 와 통신하고, 그 응답 객체 ([BacnetResponse](src/main/java/io/github/u2ware/integration/core/inbound/BacnetResponse.java)) 를 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel)에 전송합니다. 

```xml
	<int-u2ware-bacnet:inbound-channel-adapter 
				id="bacnetInboundChannelAdapter" --(1)
				localPort="9995"                 --(2)
				request-support="bacnetRequest"  --(3)
				channel="bacnetResponse">        --(4)
			<int:poller fixed-rate="1000"/>             <!-- 설정에 따라 통신을 반복 합니다.(polling) -->
	</int-u2ware-bacnet:inbound-channel-adapter >
	
	<bean id="bacnetRequest" class="io.github.u2ware.integration.bacnet.inbound.BacnetRequestSupport">
	 <constructor-arg>
	  <array>
	   <bean class="io.github.u2ware.integration.bacnet.core.BacnetRequest">
	    <property name="host" value="127.0.0.1"/>       <!--Remote BACNet Device 의 ip -->
		<property name="port" value="37807"/>           <!--Remote BACNet Device 의 port -->
		<property name="instanceNumber" value="37807"/> <!--Remote BACNet Device 의 instance number-->
	   </bean>
	  </array>
	 </constructor-arg>
	</bean>
	
	<int:channel id="bacnetResponse">   
		<int:queue/>
	</int:channel>
	              
```
1. **id**:	Unique ID.  Optianal
2. **localPort**: 생성되는 [Local BACNet Device](http://www.bacnet.org/) 의 로컬 포트 번호 입니다.
3. **request-support**:  [BacnetRequestSupport](src/main/java/io/github/u2ware/integration/bacnet/inbound/BacnetRequestSupport.java)의 참조(reference)입니다. 한 개 혹은 다수의 [BacnetRequest](src/main/java/io/github/u2ware/integration/core/inbound/BacnetRequest.java) 를 설정 할 수 있습니다.
4. **channel**: Message Channel의 참조(reference)입니다. 


##Outbound Gateway

[Request-MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 으로 부터 요청 객체([io.github.u2ware.integration.bacnet.core.BacnetRequest](src/main/java/io/github/u2ware/integration/core/inbound/BacnetRequest.java)를 수신 하여, 이를 이용하여 Remote BACNet Device 와 통신하고, 그 응답 객체([io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/core/inbound/BacnetResponse.java))을 [Reply-MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel)에 전송합니다.

```xml
	<int-u2ware-bacnet:outbound-gateway 
				id="bacnetOutboundGateway"       --(1)      
				localPort="9997"                 --(2)           
				request-channel="bacnetRequest"  --(3)
				reply-channel="bacnetResponse"   --(4)
	/>

	<int:channel id="bacnetRequest"> --(3)
	</int:channel>

	<int:channel id="bacnetResponse"> --(4)
		<int:queue/>
	</int:channel>
	
```
1. **id**:	Unique ID.  Optianal
2. **localPort**: Local BACNet Device 의 로컬 포트 번호 입니다. Local BACNet Device 의 [instanceNumber](http://www.bacnet.org/) 는 로컬 포트 번호와 동일하게 설정됩니다.
3. **request-channel**: 요청 'Message Channel'의 참조(reference)입니다. [io.github.u2ware.integration.bacnet.core.BacnetRequest](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetRequest.java)를 처리합니다.
4. **reply-channel**: 응답 'Message Channel'의 참조(reference) 입니다. [io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetResponse.java)를 처리합니다. 

##Sample Code

* [BacnetInboundChannelAdapterTests-context.xml](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests-context.xml)
* [BacnetInboundChannelAdapterTests.java](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests.java)


