package io.github.u2ware.integration.netty.test.echo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EchoClientTest {

	protected static EchoServer echoServer;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		echoServer = new EchoServer();
		echoServer.setPort(10602);
		echoServer.afterPropertiesSet();
	}
	@AfterClass
	public static void afterClass() throws Exception{
		echoServer.destroy();
	}
	
	
    protected Log logger = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("echoRequest")
	private QueueChannel echoRequest;

	@Autowired @Qualifier("echoResponse")
	private QueueChannel echoResponse;
	

	@Test
	public void testRunning() throws Exception {
		
		

		Thread.sleep(2000);
		echoRequest.send(MessageBuilder.withPayload("MESSAGE\n").build());

		Thread.sleep(2000);
		Assert.assertEquals(1, echoResponse.getQueueSize());

		Thread.sleep(2000);
		echoRequest.send(MessageBuilder.withPayload("MESSAGE\n").build());

		Thread.sleep(2000);
		Assert.assertEquals(2, echoResponse.getQueueSize());
	}
}
