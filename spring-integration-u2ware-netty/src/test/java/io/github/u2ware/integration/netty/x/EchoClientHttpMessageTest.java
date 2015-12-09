package io.github.u2ware.integration.netty.x;

import io.github.u2ware.integration.netty.x.EchoServer;

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
public class EchoClientHttpMessageTest {

    protected Log logger = LogFactory.getLog(getClass());

	protected static EchoServer echoServer;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		echoServer = new EchoServer();
		echoServer.setPort(10603);
		echoServer.afterPropertiesSet();
	}
	@AfterClass
	public static void afterClass() throws Exception{
		echoServer.destroy();
	}


	@Test
	public void test() throws Exception{
				
		//httpResponse.send(MessageBuilder.withPayload("hello world").build());
		
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject("http://localhost:10604", "hello world~", String.class);
		logger.debug(result);
		Assert.assertEquals("hello world~", result);
	}
}


