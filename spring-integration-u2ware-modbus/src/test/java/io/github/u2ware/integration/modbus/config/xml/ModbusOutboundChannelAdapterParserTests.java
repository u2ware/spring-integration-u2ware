package io.github.u2ware.integration.modbus.config.xml;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;

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
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusOutboundChannelAdapterParserTests {

	
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
	
	@Autowired @Qualifier("modbusRequest")
	private AbstractMessageChannel modbusRequest;

	@Autowired
	private ModbusExecutor executor;

	@Autowired @Qualifier("modbusOutboundChannelAdapter")
	private EventDrivenConsumer consumer;


	@Test
	public void testMessageHandlerParser() throws Exception {

		Assert.assertNotNull(modbusRequest);
		Assert.assertEquals("modbusRequest", modbusRequest.getComponentName());
		
		Assert.assertNotNull(executor);

		Assert.assertNotNull(consumer);
		
	}

}
