package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.bacnet.core.BacnetDevice;

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
public class BacnetInboundChannelAdapter1Tests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		BacnetDevice.startup(37807);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		BacnetDevice.shutdown(37807);
	}
	
	protected Log logger = LogFactory.getLog(getClass());

	@Autowired @Qualifier("bacnetResponse")
	private PollableChannel bacnetResponse;
	
	
	
	@Test
	public void testRunning() throws Exception {
		Object receive = bacnetResponse.receive(10000);
		Assert.assertNotNull(receive);
	}
}
