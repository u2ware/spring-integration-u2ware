package io.github.u2ware.integration.modbus.outbound;

import io.github.u2ware.integration.modbus.core.ModbusRequest;
import io.github.u2ware.integration.modbus.core.ModbusSlave;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusOutboundGatewayTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		ModbusSlave.startup(10505);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		ModbusSlave.shutdown();
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired @Qualifier("modbusRequest")
	private MessageChannel modbusRequest;

	@Autowired @Qualifier("modbusResponse")
	private PollableChannel modbusResponse;

    @Test
	public void testRunning() throws Exception {

    	modbusRequest.send(MessageBuilder.withPayload(new ModbusRequest(0,4,0,6)).build());

		Object receive = modbusResponse.receive(10000);
		Assert.assertNotNull(receive);
	}
}