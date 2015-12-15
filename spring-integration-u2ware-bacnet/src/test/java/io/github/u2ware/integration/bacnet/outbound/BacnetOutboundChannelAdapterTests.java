package io.github.u2ware.integration.bacnet.outbound;

import io.github.u2ware.integration.bacnet.core.BacnetRequest;
import io.github.u2ware.integration.bacnet.core.BacnetSlave;

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
public class BacnetOutboundChannelAdapterTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		BacnetSlave.startup(47806);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		BacnetSlave.shutdown();
	}

	protected Log logger = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("bacnetRequest")
	private MessageChannel bacnetRequest;

    @Test
	public void testRunning() throws Exception {

    	BacnetRequest payload = new BacnetRequest("127.0.0.1:47806", 47806);
		bacnetRequest.send(MessageBuilder.withPayload(payload).build());
		Thread.sleep(3000);
	}
}