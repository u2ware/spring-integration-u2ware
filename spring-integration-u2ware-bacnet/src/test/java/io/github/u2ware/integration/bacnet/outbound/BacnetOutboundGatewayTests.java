package io.github.u2ware.integration.bacnet.outbound;

import io.github.u2ware.integration.bacnet.core.BacnetRequest;
import io.github.u2ware.integration.bacnet.core.BacnetSlave;

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
public class BacnetOutboundGatewayTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		BacnetSlave.startup(47805);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		BacnetSlave.shutdown();
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired @Qualifier("bacnetRequest")
	private MessageChannel bacnetRequest;

	@Autowired @Qualifier("bacnetResponse")
	private PollableChannel bacnetResponse;

    @Test
	public void testRunning() throws Exception {

    	BacnetRequest r = new BacnetRequest();
    	r.setRemoteAddress("127.0.0.1:47805");
    	r.setRemoteInstanceNumber(47805);
    	
		bacnetRequest.send(MessageBuilder.withPayload(r).build());

		Object receive = bacnetResponse.receive(10000);
		Assert.assertNotNull(receive);
	}
}