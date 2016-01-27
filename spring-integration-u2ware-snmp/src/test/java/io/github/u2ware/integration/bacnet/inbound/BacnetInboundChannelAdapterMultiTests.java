package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.snmp.core.BacnetSlave;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
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
public class BacnetInboundChannelAdapterMultiTests {

	private static BacnetSlave bacnetSlave1;
	private static BacnetSlave bacnetSlave2;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		bacnetSlave1 = new BacnetSlave();
		bacnetSlave1.setLocalPort(37806);
		bacnetSlave1.setLocalInstanceNumber(37806);
		bacnetSlave1.afterPropertiesSet();

		bacnetSlave2 = new BacnetSlave();
		bacnetSlave2.setLocalPort(37805);
		bacnetSlave2.setLocalInstanceNumber(37805);
		bacnetSlave2.afterPropertiesSet();
	}

	@AfterClass
	public static void afterClass() throws Exception{
		bacnetSlave1.destroy();
		bacnetSlave2.destroy();
	}
	
	protected Log logger = LogFactory.getLog(getClass());

	@Autowired @Qualifier("bacnetResponse")
	private PollableChannel bacnetResponse;
	
	
	
	@Test
	public void testRunning() throws Exception {
		Thread.sleep(5000);
	}
}
