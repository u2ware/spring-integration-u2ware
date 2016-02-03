package io.github.u2ware.integration.modbus.inbound;

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
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusInboundChannelAdapterTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		ModbusSlave.startup(10503);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		ModbusSlave.shutdown(10503);
	}

	protected Log logger = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("modbusResponse")
	private PollableChannel modbusResponse;
	
	
	@Test
	public void testRunning() throws Exception {
		Object receive = modbusResponse.receive(10000);
		Assert.assertNotNull(receive);
	}
}
