package io.github.u2ware.integration.snmp.outbound;

import io.github.u2ware.integration.snmp.core.SnmpAgent;
import io.github.u2ware.integration.snmp.core.SnmpRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SnmpOutboundChannelAdapterTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		SnmpAgent.startup(10171);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		SnmpAgent.shutdown(10171);
	}

	protected Log logger = LogFactory.getLog(getClass());
	
	@Autowired @Qualifier("snmpRequest")
	private MessageChannel snmpRequest;

    @Test
	public void testRunning() throws Exception {

    	SnmpRequest payload = new SnmpRequest("127.0.0.1:10171", "1.3.6");
    	snmpRequest.send(MessageBuilder.withPayload(payload).build());
		Thread.sleep(3000);
	}
}