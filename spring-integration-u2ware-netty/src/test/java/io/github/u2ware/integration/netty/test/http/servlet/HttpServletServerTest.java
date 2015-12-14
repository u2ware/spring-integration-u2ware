package io.github.u2ware.integration.netty.test.http.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HttpServletServerTest {

    protected Log logger = LogFactory.getLog(getClass());
   
	@Test
	public void test() throws Exception {

		Thread.sleep(3000);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result = restTemplate.getForEntity("http://localhost:9993/helloworld",  String.class);
		//logger.debug(result);
		Assert.assertEquals("hello world", result.getBody());
		
		String result2 = restTemplate.getForObject("http://localhost:9993/webjars/angularjs/1.4.8/angular.min.js",  String.class);
		//logger.debug(result2);
		Assert.assertNotNull(result2);
	}
}


