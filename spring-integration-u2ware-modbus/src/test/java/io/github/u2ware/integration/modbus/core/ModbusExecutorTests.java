package io.github.u2ware.integration.modbus.core;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import io.github.u2ware.integration.modbus.core.ModbusResponse;

import java.util.Arrays;
import java.util.List;

import net.wimpi.modbus.Modbus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusExecutorTests {

	protected @Autowired ApplicationContext applicationContext;
	@Before
	public void before() throws Exception {
		logger.warn("===================================================");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames, 0, beanNames.length);
        for(String name : beanNames){
            logger.warn(name+"="+applicationContext.getBean(name).getClass());
        }
        logger.warn("===================================================");
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired 
	private ModbusExecutor modbusExecutor;
	
	
	@Test
	public void testReadCoils() throws Exception {

		int unitId = 0;
		int function = Modbus.READ_COILS;
		int ref = 0;
		int count = 24;
		
	    List<ModbusResponse> response = modbusExecutor.readValues(unitId, function, ref, count);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}

	@Test
	public void testReadInputDiscretes() throws Exception {

		int unitId = 0;
		int function = Modbus.READ_INPUT_DISCRETES;
		int ref = 0;
		int count = 16;
		
	    List<ModbusResponse> response = modbusExecutor.readValues(unitId, function, ref, count);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	@Test
	public void testReadMultipleRegisters() throws Exception {

		int unitId = 0;
		int function = Modbus.READ_MULTIPLE_REGISTERS;
		int ref = 0;
		int count = 12;
		
	    List<ModbusResponse> response = modbusExecutor.readValues(unitId, function, ref, count);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	@Test
	public void testReadInputRegisters() throws Exception {

		int unitId = 0;
		int function = Modbus.READ_INPUT_REGISTERS;
		int ref = 0;
		int count = 6;
		
	    List<ModbusResponse> response = modbusExecutor.readValues(unitId, function, ref, count);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	
}
