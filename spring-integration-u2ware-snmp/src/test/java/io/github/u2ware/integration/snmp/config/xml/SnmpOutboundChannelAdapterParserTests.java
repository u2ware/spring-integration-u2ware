package io.github.u2ware.integration.snmp.config.xml;

import io.github.u2ware.integration.snmp.core.SnmpManager;

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
public class SnmpOutboundChannelAdapterParserTests {

	
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
	
	@Autowired @Qualifier("snmpRequest")
	private AbstractMessageChannel snmpRequest;

	@Autowired
	private SnmpManager snmpManager;

	@Autowired @Qualifier("snmpOutboundChannelAdapter")
	private EventDrivenConsumer consumer;


	@Test
	public void testMessageHandlerParser() throws Exception {

		Assert.assertNotNull(snmpRequest);
		Assert.assertEquals("snmpRequest", snmpRequest.getComponentName());
		
		Assert.assertNotNull(snmpManager);

		Assert.assertNotNull(consumer);
	
	}

}
