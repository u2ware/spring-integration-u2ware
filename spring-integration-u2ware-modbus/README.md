Spring Integration MODBUS Adapter
=================================================

#Introduction 


[MODBUS Slave](http://www.modbus.org/) 와 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 간 메세지를 처리하는 Channel Adapter 입니다. [jamod](http://jamod.sourceforge.net/) 라이브러리를 사용하고 있습니다.

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

Spring Context 설정에서 Namespace 선언이 필요합니다.

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

미리 정의된 요청 객체 ([ModbusRequest](src/main/java/io/github/u2ware/integration/modbus/core/ModbusRequest.java)) 를 이용하여 [MODBUS Slave](http://www.modbus.org/) 와 통신하고, 그 응답 객체 ([ModbusResponse](src/main/java/io/github/u2ware/integration/modbus/core/ModbusResponse.java)) 를 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 에 전송합니다. 통신을 위해  [MODBUS Master](http://www.modbus.org/) 가 생성됩니다.



```xml
	<int-u2ware-modbus:inbound-channel-adapter 
				id="modbusInboundChannelAdapter" --(1)
				host="127.0.0.1"                 --(2)
				port="10503"                     --(3)
				request-support="modbusRequest"  --(4)
				channel="modbusResponse">	     --(5)
			<int:poller fixed-rate="1000"/>         <!-- 설정에 따라 통신을 반복 합니다.(polling) -->
	</int-u2ware-modbus:inbound-channel-adapter >
	
	<bean id="snmpRequest" class="io.github.u2ware.integration.modbus.inbound.ModbusRequestSupport">
	 <constructor-arg>
	  <array>
	   <bean class="io.github.u2ware.integration.modbus.core.ModbusRequest">
		<property name="unitId" value="0"/>       <!--Modbus Protocal 의 unitId -->
		<property name="functionCode" value="4"/> <!--Modbus Protocal 의 functionCode -->
		<property name="offset" value="0"/>       <!--Modbus Protocal 의 offset -->
		<property name="count" value="6"/>        <!--Modbus Protocal 의 count -->
	   </bean>
	  </array>
	 </constructor-arg>
	</bean>
	
	<int:channel id="snmpResponse">   
		<int:queue/>
	</int:channel>
	              				
				
```

1. **id**:	Unique ID.  Optional.
2. **host**: 통신 대상이 되는 [MODBUS Slave](http://www.modbus.org/) 의 ip 입니다. Required.
3. **port**: 통신 대상이 되는 [MODBUS Slave](http://www.modbus.org/) 의 포트 번호 입니다. Required.
4. **request-support**:  [ModbusRequestSupport](src/main/java/io/github/u2ware/integration/modbus/inbound/ModbusRequestSupport.java)의 참조(reference)입니다. 한 개 혹은 다수의 [ModbusRequest](src/main/java/io/github/u2ware/integration/modbus/core/ModbusRequest.java) 를 설정 할 수 있습니다. Required.
5. **channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference)입니다.


##Outbound Gateway


RequestMessageChannel 로 부터 요청 객체 ([ModbusRequest](src/main/java/io/github/u2ware/integration/modbus/core/ModbusRequest.java)) 를 수신 하여, 이를 이용하여 [MODBUS Slave](http://www.modbus.org/) 와 통신하고, 그 응답 객체 ([ModbusResponse](src/main/java/io/github/u2ware/integration/modbus/core/ModbusResponse.java)) 를 ReplyMessageChannel 에 전송합니다. 통신을 위해  [MODBUS  Master](http://www.modbus.org/) 가 생성됩니다.

```xml
	<int-u2ware-modbus:outbound-gateway 
				id="modbusOutboundGateway"      --(1)
				host="127.0.0.1"                --(2)
				port="10505"                    --(3)
				request-channel="modbusRequest" --(4)
				reply-channel="modbusResponse"  --(5)
	/>
	
	<int:channel id="modbusRequest">
	</int:channel>

	<int:channel id="modbusResponse">
		<int:queue/>
	</int:channel>
				
				
```

1. **id**:	Unique ID.  Optional.
2. **host**: 통신 대상이 되는 [MODBUS Slave](http://www.modbus.org/) 의 ip 입니다. Required.
3. **port**: 통신 대상이 되는 [MODBUS Slave](http://www.modbus.org/) 의 포트 번호 입니다. Required.
4. **request-channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference)입니다. [ModbusRequest](src/main/java/io/github/u2ware/integration/modbus/core/ModbusRequest.java)를 처리합니다.
5. **reply-channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference) 입니다. [ModbusResponse](src/main/java/io/github/u2ware/integration/modbus/core/ModbusResponse.java)를 처리합니다. 



##Sample Code


* [core](src/test/java/io/github/u2ware/integration/modbus/core/)
* [inbound](src/test/java/io/github/u2ware/integration/modbus/inbound/)
* [outbound](src/test/java/io/github/u2ware/integration/modbus/outbound/)





