Spring Integration SNMP Adapter
=================================================

#Introduction 

[SNMP Agent](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 와 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 간 메세지를 처리하는 Channel Adapter 입니다. 

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
		<artifactId>spring-integration-u2ware-snmp</artifactId>
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
	xmlns:int-u2ware-bacnet="http://u2ware.github.io/schema/integration/snmp"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/integration 
		http://www.springframework.org/schema/integration/spring-integration.xsd
		http://u2ware.github.io/schema/integration/snmp 
		http://u2ware.github.io/schema/integration/snmp/spring-integration-snmp.xsd">
		
</bean>
```

##Inbound Channel Adapter

미리 정의된 요청 객체 ([SnmpRequest](src/main/java/io/github/u2ware/integration/snmp/core/SnmpRequest.java)) 를 이용하여 [SNMP Agent](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 와 통신하고, 그 응답 객체 ([SnmpResponse](src/main/java/io/github/u2ware/integration/snmp/core/SnmpResponse.java)) 를 [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 에 전송합니다. 통신을 위해  [SNMP Manager](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 가 생성됩니다.

```xml
	<int-u2ware-snmp:inbound-channel-adapter 
				id="snmpInboundChannelAdapter" --(1)
				local-port="9995"              --(2)
				mib-file="/Users/my.mib"       --(3)
				request-support="snmpRequest"  --(3)
				channel="snmpResponse">        --(4)
			<int:poller fixed-rate="1000"/>             <!-- 설정에 따라 통신을 반복 합니다.(polling) -->
	</int-u2ware-bacnet:inbound-channel-adapter >
	
	<bean id="snmpRequest" class="io.github.u2ware.integration.snmp.inbound.SnmpRequestSupport">
	 <constructor-arg>
	  <array>
	   <bean class="io.github.u2ware.integration.snmp.core.SnmpRequest">
	    <property name="host" value="127.0.0.1"/>   <!--SNMP Agent 의 ip -->
		<property name="port" value="37807"/>       <!--SNMP Agent 의 port -->
		<property name="rootOid" value="1.3.6"/>    <!--SNMP Agent 의 rootOid-->
	   </bean>
	  </array>
	 </constructor-arg>
	</bean>
	
	<int:channel id="snmpResponse">   
		<int:queue/>
	</int:channel>
	              
```
1. **id**:	Unique ID.  Optianal
2. **local-port**: 생성되는 [SNMP Manager](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 의 로컬 포트 번호 입니다.
3. **mib-file**: [Management information base (MIB)](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol#Management_information_base_.28MIB.29) 파일 경로 입니다.
4. **request-support**:  [SnmpRequestSupport](src/main/java/io/github/u2ware/integration/snmp/inbound/SnRequempstSupport.java)의 참조(reference)입니다. 한 개 혹은 다수의 [SnmpRequest](src/main/java/io/github/u2ware/integration/snmp/core/BacnetRequest.java) 를 설정 할 수 있습니다.
5. **channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference)입니다. 


##Outbound Gateway

RequestMessageChannel 로 부터 요청 객체 ([SnmpRequest](src/main/java/io/github/u2ware/integration/snmp/core/BacnetRequest.java)) 를 수신 하여, 이를 이용하여 [SNMP Agent](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 와 통신하고, 그 응답 객체 ([SnmpResponse](src/main/java/io/github/u2ware/integration/snmp/core/SnmpResponse.java)) 를 ReplyMessageChannel 에 전송합니다. 통신을 위해  [SNMP Manager](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 가 생성됩니다.

```xml
	<int-u2ware-snmp:outbound-gateway 
				id="snmpOutboundGateway"       --(1)      
				local-port="9997"              --(2)           
				mib-file="9997"                --(3)           
				request-channel="snmpRequest"  --(4)
				reply-channel="snmpResponse"   --(5)
	/>

	<int:channel id="snmpRequest"> <!-- RequestMessageChannel -->
	</int:channel>

	<int:channel id="snmpResponse"> <!-- ReplyMessageChannel -->
		<int:queue/>
	</int:channel>
	
```
1. **id**:	Unique ID.  Optianal
2. **local-port**: 생성되는 [SNMP Manager](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) 의 로컬 포트 번호 입니다.
3. **mib-file**: [Management information base (MIB)](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol#Management_information_base_.28MIB.29) 파일 경로 입니다.4. **request-channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference)입니다. [SnmpRequest](src/main/java/io/github/u2ware/integration/snmp/core/SnmpRequest.java)를 처리합니다.
5. **reply-channel**: [MessageChannel](http://docs.spring.io/spring-integration/docs/4.2.4.RELEASE/reference/html/messaging-channels-section.html#channel) 의 참조(reference) 입니다. [SnmpResponse](src/main/java/io/github/u2ware/integration/snmp/core/SnmpResponse.java)를 처리합니다. 

##Sample Code

* [core](src/test/java/io/github/u2ware/integration/snmp/core/)
* [inbound](src/test/java/io/github/u2ware/integration/snmp/inbound/)
* [outbound](src/test/java/io/github/u2ware/integration/snmp/outbound/)


