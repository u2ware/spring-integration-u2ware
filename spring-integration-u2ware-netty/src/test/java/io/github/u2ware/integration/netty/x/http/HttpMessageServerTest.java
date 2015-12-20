package io.github.u2ware.integration.netty.x.http;

import io.github.u2ware.integration.netty.x.SampleEchoServer;

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
public class HttpMessageServerTest {

	@BeforeClass
	public static void beforeClass() throws Exception{
		SampleEchoServer.startup(10901);
	}
	@AfterClass
	public static void afterClass() throws Exception{
		SampleEchoServer.shutdown();
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Test
	public void test() throws Exception{
				
		//httpResponse.send(MessageBuilder.withPayload("hello world").build());
		Thread.sleep(3000);
		
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.postForObject("http://localhost:9991", "hello world~", String.class);
		logger.debug(result);
		Assert.assertEquals("hello world~", result);
	}
}


