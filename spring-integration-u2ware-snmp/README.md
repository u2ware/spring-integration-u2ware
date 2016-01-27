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
				id="bacnetInboundChannelAdapter"   --(1)
				componentName="myBacnet"           --(2)
				localPort="9995"                   --(3)
				remoteAddress="127.0.0.1:47807"    --(4)
				remoteInstanceNumber="47807"       --(5)
				channel="bacnetResponse"/>         --(6)
```
1. **id**:	Unique ID.  
2. **componentName**: 임의대로 지정하는 이름입니다. 
3. **localPort**: 원격 BACNet Device 와 연결하는 로컬 포트 번호 입니다. 
4. **remoteAddress**: 원격 BACNet Device 의 Address 입니다. ```<ip>:<port>``` 
5. **remoteInstanceNumber**: 원격 BACNet Device 의 Instance Number 입니다. 
6. **channel**: 수신 Message Channel입니다. 메세지는 [io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetResponse.java) 객체를 담고 있습니다. 

##Outbound Gateway

'전송 Message Channel'의 요청을 원격 BACNet Device 에 전송하고 그 응답을 '수신 Message Channel' 에 담습니다.

```xml
	<int-u2ware-bacnet:outbound-gateway 
				id="bacnetOutboundGateway"         
				componentName="myBacnet"           
				localPort="9997"                   
				remoteAddress="127.0.0.1:47805"    
				remoteInstanceNumber="47805"       
				request-channel="bacnetRequest"    --(1)
				reply-channel="bacnetResponse"/>   --(2)
```
1. **request-channel**: '전송 Message Channel'입니다. 메세지는 [io.github.u2ware.integration.bacnet.core.BacnetRequest](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetRequest.java) 객체를 담고 있습니다. 
2. **reply-channel**: '수신 Message Channel'입니다. 메세지는 [io.github.u2ware.integration.bacnet.core.BacnetResponse](src/main/java/io/github/u2ware/integration/bacnet/core/BacnetResponse.java) 객체를 담고 있습니다. 

##Sample Code

* [BacnetInboundChannelAdapterTests-context.xml](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests-context.xml)
* [BacnetInboundChannelAdapterTests.java](src/test/java/io/github/u2ware/integration/bacnet/inbound/BacnetInboundChannelAdapterTests.java)


