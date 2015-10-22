package io.github.u2ware.integration.netty.test.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HttpStaticFileChannelAdapterTest {

    protected Log logger = LogFactory.getLog(getClass());


	@Test
	public void test() throws Exception{
				
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject("http://localhost:9095/a/b/c.html", String.class);
		logger.debug(result);
		Assert.assertEquals("<html><body><h1>Hello World!!</h1></body></html>", result);
	}
}


