Spring Integration MODBUS Adapter
=================================================

#Introduction 

[MODBUS](http://www.modbus.org/) 디바이스와 메세지를 수신하고 전송하는 Channel Adapter 입니다. 
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
		<artifactId>spring-integration-u2ware-modbus</artifactId>
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
	xmlns:int-u2ware-modbus="http://u2ware.github.io/schema/integration/modbus"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/integration 
		http://www.springframework.org/schema/integration/spring-integration.xsd
		http://u2ware.github.io/schema/integration/modbus 
		http://u2ware.github.io/schema/integration/modbus/spring-integration-modbus.xsd">
		
</bean>
```

##Inbound Channel Adapter

polling 방식으로 원격 MODBUS Device 로 부터 받은 응답을 '수신 Message Channel' 에 담습니다.

```xml
	<int-u2ware-modbus:inbound-channel-adapter 
				id="modbusInboundChannelAdapter"   --(1)
				componentName="modbus"             --(2)
				host="127.0.0.1"                   --(3)
				port="10503"                       --(4)
				unitId="0"                         --(5)
				functionCode="4"                   --(6)
				offset="0"                         --(7)
				count="6"                          --(8)
				channel="modbusResponse"/>	       --(9)
```
1. **id**:	Unique ID.  
2. **componentName**: 임의대로 지정하는 이름입니다. 
3. **host**: 원격 MODBUS Device 의 IP 주소 입니다.
4. **port**: 원격 MODBUS Device 의 포트 번호 입니다. 
5. **unitId**: MODBUS Request 패킷의 unitId 값입니다.
6. **functionCode**: MODBUS Request 패킷의 functionCode 값입니다. 
7. **offset**: MODBUS Request 패킷의 offset 값입니다. 
8. **count**: MODBUS Request 패킷의 count 값입니다. 
9. **channel**: 수신 Message Channel입니다. 메세지는 [io.github.u2ware.integration.modbus.core.ModbusResponse](src/main/java/io/github/u2ware/integration/modbus/core/ModbusResponse.java) 객체를 담고 있습니다. 

##Outbound Gateway

'전송 Message Channel'의 요청을 원격 BACNet Device 에 전송하고 그 응답을 '수신 Message Channel' 에 담습니다.

```xml
	<int-u2ware-modbus:outbound-gateway 
				id="modbusOutboundGateway" 
				componentName="modbus"
				host="127.0.0.1"
				port="10505"
				request-channel="modbusRequest"    --(1)
				reply-channel="modbusResponse"/>   --(2)
```
1. **request-channel**: '전송 Message Channel'입니다. 메세지는 [io.github.u2ware.integration.modbus.core.ModbusRequest](src/main/java/io/github/u2ware/integration/modbus/core/ModbusRequest.java) 객체를 담고 있습니다. 
2. **reply-channel**: '수신 Message Channel'입니다. 메세지는 [io.github.u2ware.integration.modbus.core.ModbusResponse](src/main/java/io/github/u2ware/integration/modbus/core/ModbusResponse.java) 객체를 담고 있습니다. 

##Sample Code

* [ModbusInboundChannelAdapterTests-context.xml](src/test/java/io/github/u2ware/integration/modbus/inbound/ModbusInboundChannelAdapterTests-context.xml)
* [ModbusInboundChannelAdapterTests.java](src/test/java/io/github/u2ware/integration/modbus/inbound/ModbusInboundChannelAdapterTests.java)


