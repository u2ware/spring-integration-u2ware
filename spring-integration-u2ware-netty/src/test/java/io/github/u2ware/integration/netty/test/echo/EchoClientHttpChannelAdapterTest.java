package io.github.u2ware.integration.netty.test.echo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EchoClientHttpChannelAdapterTest {

	protected static EchoServer echoServer;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		echoServer = new EchoServer();
		echoServer.setPort(9092);
		echoServer.afterPropertiesSet();
	}
	@AfterClass
	public static void afterClass() throws Exception{
		echoServer.destroy();
	}

	protected Log logger = LogFactory.getLog(getClass());
	
	@Test
	public void testRunning() throws Exception {
		
		Thread.sleep(3000);
		RestTemplate restTemplate = new RestTemplate();
		
		Object result = restTemplate.postForEntity("http://localhost:9093", "hello echo~\n", null);
		Assert.assertNotNull(result);
		logger.debug("result : "+result.getClass());
		logger.debug("result : "+result);
	}
	
}
