package io.github.u2ware.integration.netty.test.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HttpMessageChannelAdapterTest {

    protected Log logger = LogFactory.getLog(getClass());


    @Autowired @Qualifier("httpResponse")
    private QueueChannel httpResponse;
    
	@Test
	public void test() throws Exception{
				
		httpResponse.send(MessageBuilder.withPayload("hello world").build());
		
		RestTemplate restTemplate = new RestTemplate();
		//String result = restTemplate.getForObject("http://localhost:9094", String.class);
		//Assert.assertEquals("hello world", result);

		String result2 = restTemplate.postForObject("http://localhost:9094", null, String.class);
		Assert.assertNotNull(result2);
	
	}
}


