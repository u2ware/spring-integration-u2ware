Spring Integration Extentions
=================================================

메세지 기반 통신을 지원하는 [Spring Integration Adapter](http://docs.spring.io/spring-integration/docs/4.2.0.RELEASE/reference/html/endpoint-summary.html) 입니다.

|Module|Inbound Adapter|Outbound Gateway| Bean|
|------|---|---|---|---|
|BACNet|[spring-integration-u2ware-bacnet](spring-integration-u2ware-bacnet/)|[spring-integration-u2ware-bacnet](spring-integration-u2ware-bacnet/)|N|
|MODBUS|[spring-integration-u2ware-modbus](spring-integration-u2ware-modbus/)|[spring-integration-u2ware-modbus](spring-integration-u2ware-modbus/)|N|
|MongoDB|N|[spring-integration-u2ware-mongodb](spring-integration-u2ware-mongodb/)|N|
|TCP/IP|N|N|[spring-integration-u2ware-netty](spring-integration-u2ware-netty/)|

Maven Dependency 를 다음과 같이 설정 할 수 있습니다.

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
		<dependency>
			<groupId>io.github.u2ware</groupId>
			<artifactId>spring-integration-u2ware-modbus</artifactId>
			<version>1.0.0</version>
		</dependency>
		<dependency>
			<groupId>io.github.u2ware</groupId>
			<artifactId>spring-integration-u2ware-momgodb</artifactId>
			<version>1.0.0</version>
		</dependency>
		<dependency>
			<groupId>io.github.u2ware</groupId>
			<artifactId>spring-integration-u2ware-netty</artifactId>
			<version>1.0.0</version>
		</dependency>
	</dependencies>
```
