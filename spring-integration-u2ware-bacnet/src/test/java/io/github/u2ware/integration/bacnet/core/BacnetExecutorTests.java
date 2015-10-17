package io.github.u2ware.integration.bacnet.core;

import io.github.u2ware.integration.bacnet.core.BacnetExecutor;
import io.github.u2ware.integration.bacnet.core.BacnetResponse;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.serotonin.bacnet4j.RemoteDevice;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BacnetExecutorTests {

	protected @Autowired ApplicationContext applicationContext;

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

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired 
	private BacnetExecutor bacnetExecutor;
	
	@Test
	public void testFindDevice() throws Exception {
		
        logger.debug("1------------------------------------------------");
        logger.debug(bacnetExecutor);

        Thread.sleep(3000);
        bacnetExecutor.sendGlobalBroadcast();
        Thread.sleep(3000);
        for(RemoteDevice d : bacnetExecutor.getRemoteDevices()){
        	logger.debug(d.getInstanceNumber()+" "+d.getAddress().getDescription());
        }
        
        
        List<BacnetResponse> result = bacnetExecutor.readValues("127.0.0.1:47808", 47808);
        for(BacnetResponse r : result){
        	logger.debug(r);
        }
	}
}
