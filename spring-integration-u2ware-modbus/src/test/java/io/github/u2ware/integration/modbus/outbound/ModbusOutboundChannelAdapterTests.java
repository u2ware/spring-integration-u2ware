package io.github.u2ware.integration.modbus.outbound;

import io.github.u2ware.integration.modbus.core.ModbusRequest;
import io.github.u2ware.integration.modbus.core.ModbusSlave;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusOutboundChannelAdapterTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		ModbusSlave.startup(10504);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		ModbusSlave.shutdown();
	}

	protected Log logger = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("modbusRequest")
	private MessageChannel modbusRequest;

    @Test
	public void testRunning() throws Exception {

    	modbusRequest.send(MessageBuilder.withPayload(new ModbusRequest(0,4,0,6)).build());
		Thread.sleep(3000);
	}
}