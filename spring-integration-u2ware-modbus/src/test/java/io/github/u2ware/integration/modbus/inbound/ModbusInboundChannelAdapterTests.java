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

	protected Log logger = LogFactory.getLog(getClass());

	private static ModbusSlave modbusSlave;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		modbusSlave = new ModbusSlave();
		modbusSlave.setLocalPort(10503);
		modbusSlave.afterPropertiesSet();
	}
	@AfterClass
	public static void afterClass() throws Exception{
		modbusSlave.destroy();
	}
	
	@Autowired @Qualifier("modbusResponse")
	private PollableChannel modbusResponse;
	
	
	@Test
	public void testRunning() throws Exception {
		Object receive = modbusResponse.receive(10000);
		Assert.assertNotNull(receive);
	}
}
