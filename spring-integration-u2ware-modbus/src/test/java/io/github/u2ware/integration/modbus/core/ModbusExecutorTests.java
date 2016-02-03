package io.github.u2ware.integration.modbus.core;

import java.util.List;

import net.wimpi.modbus.Modbus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModbusExecutorTests {

	@BeforeClass
	public static void beforeClass() throws Exception {
		ModbusSlave.startup(10502);
	}

	@AfterClass
	public static void afterClass() throws Exception{
		ModbusSlave.shutdown(10502);
	}

	protected Log logger = LogFactory.getLog(getClass());

	@Autowired 
	private ModbusExecutor modbusExecutor;
	
	
	@Test
	public void testReadCoils() throws Exception {

        ModbusRequest request = new ModbusRequest(0, Modbus.READ_COILS, 0, 24);
	    List<ModbusResponse> response = modbusExecutor.readValues(request);
        Assert.assertNotNull(response);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}

	@Test
	public void testReadInputDiscretes() throws Exception {

        ModbusRequest request = new ModbusRequest(0, Modbus.READ_INPUT_DISCRETES, 0, 16);
	    List<ModbusResponse> response = modbusExecutor.readValues(request);
        Assert.assertNotNull(response);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	@Test
	public void testReadMultipleRegisters() throws Exception {

        ModbusRequest request = new ModbusRequest(0, Modbus.READ_MULTIPLE_REGISTERS, 0, 12);
	    List<ModbusResponse> response = modbusExecutor.readValues(request);
        Assert.assertNotNull(response);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	@Test
	public void testReadInputRegisters() throws Exception {

        ModbusRequest request = new ModbusRequest(0, Modbus.READ_INPUT_REGISTERS, 0, 6);
	    List<ModbusResponse> response = modbusExecutor.readValues(request);
        Assert.assertNotNull(response);
	    for(ModbusResponse r : response){
	    	logger.debug(r);
	    }
	}
	
	
}
