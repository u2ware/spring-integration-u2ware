package io.github.u2ware.integration.modbus.core;

import io.github.u2ware.integration.modbus.core.ModbusExecutor;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModbusExecutorTemplate {

    protected Log logger = LogFactory.getLog(getClass());
	
	private ModbusExecutor executor;
	
	public ModbusExecutorTemplate(ModbusExecutor executor){
		this.executor = executor;
	}
	
	public ReadCoilsResponse readCoils(int unitId, int ref, int count) throws Exception {
        ReadCoilsRequest req = new ReadCoilsRequest(ref, count);
        req.setUnitID(unitId);

        ReadCoilsResponse res = executor.execute(req);        

        logger.debug(res);
        logger.debug(res.getDataLength());
        logger.debug(res.getBitCount());
        logger.debug(res.getOutputLength());
        logger.debug(res.getCoils());
        logger.debug(res.getCoils().size());

        for(int i = 0 ; i < res.getCoils().size(); i++){        	
            logger.debug("XX::: "+ (ref+i+1)+"="+res.getCoils().getBit(i));
        }
        
        for(int i = 0 ; i < res.getBitCount(); i++){        	
            logger.debug("YY::: "+ (ref+i+1)+"="+res.getCoilStatus(i));
        }
        
        return res;
	}
	
	public ReadInputDiscretesResponse readInputDiscretes(int unitId, int ref, int count) throws Exception {
        ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(ref, count);
        req.setUnitID(unitId);

        ReadInputDiscretesResponse res = executor.execute(req);        

        logger.debug(res);
        logger.debug(res.getDataLength());
        logger.debug(res.getBitCount());
        logger.debug(res.getOutputLength());
        logger.debug(res.getDiscretes());
        logger.debug(res.getDiscretes().size());

        for(int i = 0 ; i < res.getDiscretes().size(); i++){        	
            logger.debug("XX"+ (ref+i+1)+"="+res.getDiscretes().getBit(i));
        }
        
        for(int i = 0 ; i < res.getBitCount(); i++){        	
            logger.debug("YY"+ (ref+i+1)+"="+res.getDiscreteStatus(i));
        }
        return res;
	}
	
	public ReadMultipleRegistersResponse readHoldingRegisters(int unitId, int ref, int count) throws Exception {
        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(ref, count);
        req.setUnitID(unitId);
        
        ReadMultipleRegistersResponse res = executor.execute(req);        
        
        for(int i = 0 ; i < res.getWordCount(); i++){        	
            logger.debug((ref+i+1)+"="+res.getRegisterValue(i));
        }
        logger.debug(res);
        logger.debug(res.getWordCount());
        
        return res;
	}

	
	public ReadInputRegistersResponse readInputRegisters(int unitId, int ref, int count) throws Exception {
        ReadInputRegistersRequest req = new ReadInputRegistersRequest(ref, count);
        req.setUnitID(unitId);
        
        ReadInputRegistersResponse res = executor.execute(req);        
        
        for(int i = 0 ; i < res.getWordCount(); i++){        	
            logger.debug((ref+i+1)+"="+res.getRegisterValue(i));
        }
        logger.debug(res);
        logger.debug(res.getWordCount());
        
        return res;
	}
	
	
}
