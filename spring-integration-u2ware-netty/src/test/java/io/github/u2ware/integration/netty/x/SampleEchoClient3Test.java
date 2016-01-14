package io.github.u2ware.integration.netty.x;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SampleEchoClient3Test {

	@BeforeClass
	public static void beforeClass() throws Exception{
		SampleEchoServer.startup(10903);
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
		
		Thread.sleep(5000);
		Assert.assertEquals(1, echoResponse.getQueueSize());

		Thread.sleep(5000);
		Assert.assertEquals(2, echoResponse.getQueueSize());
	}
}
