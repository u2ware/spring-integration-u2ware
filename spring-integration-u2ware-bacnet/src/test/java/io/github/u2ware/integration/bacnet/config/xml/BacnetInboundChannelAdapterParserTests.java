package io.github.u2ware.integration.bacnet.config.xml;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BacnetInboundChannelAdapterParserTests {

    protected Log logger = LogFactory.getLog(getClass());

    @Autowired
    protected ApplicationContext applicationContext;

	@Before
	public void before() throws Exception {
		
        logger.debug("===================================================");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames, 0, beanNames.length);
        for(String name : beanNames){
            logger.debug(name+"="+applicationContext.getBean(name).getClass());
        }
        logger.debug("===================================================");
	}
	
	
	@Autowired @Qualifier("bacnetResponse")
	private AbstractMessageChannel bacnetResponse;

	@Autowired
	private BacnetExecutor bacnetExecutor;
	
	@Autowired
	private SourcePollingChannelAdapter consumer;
	
	@Test
	public void testInboundChannelAdapterParser() throws Exception {
		
		Assert.assertNotNull(bacnetResponse);
		Assert.assertEquals("bacnetResponse", bacnetResponse.getComponentName());
		
		Assert.assertNotNull(bacnetExecutor);

		Assert.assertNotNull(consumer);
		Assert.assertTrue(consumer.isAutoStartup());
		
		
	}
}
