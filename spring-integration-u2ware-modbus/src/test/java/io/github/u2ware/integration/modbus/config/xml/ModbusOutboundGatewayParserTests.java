package io.github.u2ware.integration.modbus.config.xml;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.outbound.ModbusMessageHandler;

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
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusOutboundGatewayParserTests {

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
	
	@Autowired @Qualifier("modbusOutboundGateway")
	private EventDrivenConsumer consumer;
	
	@Test
	public void testOutboundGatewayParser() throws Exception {
		
		Assert.assertNotNull(modbusRequest);
		Assert.assertEquals("modbusRequest", modbusRequest.getComponentName());
	
		Assert.assertNotNull(executor);
		Assert.assertEquals("127.0.0.1", executor.getHost());
		
		final ModbusMessageHandler outboundGateway = TestUtils.getPropertyValue(this.consumer, "handler", ModbusMessageHandler.class);
		Assert.assertNotNull(outboundGateway);
		long sendTimeout = TestUtils.getPropertyValue(outboundGateway, "messagingTemplate.sendTimeout", Long.class);

		Assert.assertEquals(100, sendTimeout);
	}
}
