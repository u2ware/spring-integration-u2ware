package io.github.u2ware.integration.snmp.core;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SnmpExecutorTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		SnmpAgent.startup(10162);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		SnmpAgent.shutdown();
	}
	

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired @Qualifier("snmpManager")
	private SnmpManager snmpManager;
	
	@Test
	public void testFindDevice() throws Exception {
		
        logger.debug(snmpManager);

        SnmpRequest request = new SnmpRequest("127.0.0.1:10162:1.3.6");
        
        Collection<SnmpResponse> response = snmpManager.execute(request);
        
        logger.debug(response.size());
        
        for(SnmpResponse r : response){
            logger.debug(r);
        }
	}
}
