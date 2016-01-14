package io.github.u2ware.integration.netty.x;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SampleEchoClient4Test {

	@BeforeClass
	public static void beforeClass() throws Exception{
		SampleEchoServer.startup(10904);
	}
	@AfterClass
	public static void afterClass() throws Exception{
		SampleEchoServer.shutdown();
	}
	
	
    protected Log logger = LogFactory.getLog(getClass());
	

	@Autowired @Qualifier("echoRequest")
	private QueueChannel echoRequest;

	@Autowired @Qualifier("echoResponse")
	private QueueChannel echoResponse;
	

	@Test
	public void testRunning() throws Exception {

		
		for(int i=0 ; i< 4; i++){
			Thread.sleep(1000);
			echoRequest.send(MessageBuilder.withPayload(new Integer(1)).build());

			Thread.sleep(1000);
			Message<?> receive = echoResponse.receive();
			logger.debug(receive.getPayload());
		}
	}
}
