package io.github.u2ware.integration.bacnet.core;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.serotonin.bacnet4j.RemoteDevice;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BacnetExecutorTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		BacnetSlave.startup(47808);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		BacnetSlave.shutdown();
	}
	

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired 
	private BacnetExecutor bacnetExecutor;
	
	@Test
	public void testFindDevice() throws Exception {
		
        logger.debug(bacnetExecutor);

        Thread.sleep(3000);
        for(RemoteDevice d : bacnetExecutor.getRemoteDevices()){
        	logger.debug(d.getInstanceNumber()+" "+d.getAddress().getDescription());
        }
        
        List<BacnetResponse> result = bacnetExecutor.readValues("127.0.0.1:47808", 47808);
        logger.debug(result.size());
	}
}
