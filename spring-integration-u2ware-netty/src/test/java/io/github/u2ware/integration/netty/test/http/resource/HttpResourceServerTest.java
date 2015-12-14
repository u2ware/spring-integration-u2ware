package io.github.u2ware.integration.netty.test.http.resource;

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
public class HttpResourceServerTest {

    protected Log logger = LogFactory.getLog(getClass());

	@Test
	public void test() throws Exception{
				
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject("http://localhost:9992/foo/bar/baz.html", String.class);
		logger.debug(result);
		Assert.assertEquals("<html><body><h1>Hello World!!</h1></body></html>", result);
	}
}


