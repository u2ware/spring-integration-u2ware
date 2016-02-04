Spring Integration BACNet Adapter
=================================================

#Introduction 

[BACNet](http://www.bacnet.org/) 디바이스와 메세지를 수신하고 전송하는 Channel Adapter 입니다. 
다음과 같이 Maven Dependency 를 추가합니다.

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

[Spring Integration](http://projects.spring.io/spring-integration/) Context 설정에서 Namespace 선언이 필요합니다.

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

polling 방식으로 원격 BACNet Device 로 부터 받은 응답을 '수신 Message Channel' 에 담습니다.

```xml
	<int-u2ware-bacnet:inbound-channel-adapter 
				id="bacnetInboundChannelAdapter"        --(1)
				localPort="9995"                        --(2)
				request-support="127.0.0.1:47807:47807" --(3)
				channel="bacnetResponse"                --(4)
	/>              
```
1. **id**:	Unique ID.  
2. **localPort**: Local BACNet Device 의 로컬 포트 번호 입니다. Local BACNet Device 의 [instanceNumber](http://www.bacnet.org/) 는 로컬 포트 번호와 동일하게 설정됩니다.
3. **request-support**: 통신하고자 하는 Remote BACNet Device 정보입니다. , 를 사용하여 다수의 Remote BACNet Device 를 설정할 수 있습니다. ```<ip>:<port>:<instanceNumber>,...```
4. **channel**: 수신 Message Channel입니다. [io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetResponse.java)를 처리합니다.

##Outbound Gateway

'전송 Message Channel'의 요청을 원격 BACNet Device 에 전송하고 그 응답을 '수신 Message Channel' 에 담습니다.

```xml
	<int-u2ware-bacnet:outbound-gateway 
				id="bacnetOutboundGateway"         
				localPort="9997"                   
				request-channel="bacnetRequest"  --(1)
				reply-channel="bacnetResponse"   --(2)
	/>
```
1. **request-channel**: '전송 Message Channel'입니다. [io.github.u2ware.integration.bacnet.core.BacnetRequest](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetRequest.java)를 처리합니다. 
2. **reply-channel**: '수신 Message Channel'입니다. 메세지는 [io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetResponse.java)를 처리합니다. 

##Sample Code

* [BacnetInboundChannelAdapterTests-context.xml](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests-context.xml)
* [BacnetInboundChannelAdapterTests.java](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests.java)


