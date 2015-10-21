package io.github.u2ware.integration.modbus.core;

import java.net.InetAddress;
import java.util.List;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;

public class ModbusExecutor implements InitializingBean, DisposableBean{

	private Log logger = LogFactory.getLog(getClass());

	private String host;
	private int port = Modbus.DEFAULT_PORT;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(host, "host must not be null.");
		Assert.notNull(port, "slaveAddress must not be null.");

		logger.info("MODBUS Master Started Port Number: "+port);
	}
	@Override
	public void destroy() throws Exception {
        logger.info("MODBUS Master Finished Port Number: "+port);
	}
	
	private InetAddress getInetAddress() throws Exception{
		if("localhost".equals(host) || "127.0.0.1".equals(host)){
			return InetAddress.getLocalHost();
		}else{
			return InetAddress.getByName(host); 
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized <Q extends net.wimpi.modbus.msg.ModbusRequest, A extends net.wimpi.modbus.msg.ModbusResponse> A execute(Q request) throws Exception{

		TCPMasterConnection con = new TCPMasterConnection(getInetAddress());
        con.setPort(port);
		con.setTimeout(100000);
        
		ModbusTransaction trans = new ModbusTCPTransaction(con);
		//trans.setRetries(100);
		
		logger.info("request["+host+":"+port+"] "+ClassUtils.getShortName(request.getClass())+" "+request.getHexMessage());
		trans.setRequest(request);
		trans.execute();
		net.wimpi.modbus.msg.ModbusResponse response = trans.getResponse();
		logger.info("response["+host+":"+port+"] "+ClassUtils.getShortName(response.getClass())+" "+response.getHexMessage());
		
        con.close();

		return (A)response;
	}

	public synchronized List<ModbusResponse> readValues(int unitId, int function, int offset, int count) throws Exception{
		net.wimpi.modbus.msg.ModbusRequest request = createRequest(unitId, function, offset, count);
		net.wimpi.modbus.msg.ModbusResponse response = execute(request);
		List<ModbusResponse> result = readValues(response);
		return result;
	}
	public synchronized List<ModbusResponse> readValues(ModbusRequest modbusReq) throws Exception{
		return readValues(modbusReq.getUnitId(), modbusReq.getFunctionCode(), modbusReq.getOffset(), modbusReq.getCount());
	}
	
	//0x01	//Read Coils              //read-write   //ReadCoils 
	//0x02	//Read Discrete Inputs    //read-only    //ReadInputDiscretes
	//0x03	//Read Holding Registers  //read-write   //ReadMultipleRegisters
	//0x04	//Read Input Registers    //read-only    //ReadInputRegister
	private net.wimpi.modbus.msg.ModbusRequest createRequest(int unitId, int function, int offset, int count){

		//System.err.println(function+" "+unitId+" "+ref+" "+count);

		if(Modbus.READ_COILS == function){ //0x01
			if(count > 2000) return null;
			ReadCoilsRequest request = new ReadCoilsRequest(offset, count);
			request.setUnitID(unitId);
			return request;
		}else if(Modbus.READ_INPUT_DISCRETES == function){ //0x02
			if(count > 2000) return null;
			ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(offset, count);
			request.setUnitID(unitId);
			return request;
		}else if(Modbus.READ_MULTIPLE_REGISTERS == function){ //0x03
			if(count > 125) return null;
			ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(offset, count);
			request.setUnitID(unitId);
			return request;
		}else if(Modbus.READ_INPUT_REGISTERS == function){
			if(count > 125) return null;
			ReadInputRegistersRequest request = new ReadInputRegistersRequest(offset, count);
			request.setUnitID(unitId);
			return request;
		}		
		return null;
	}
	
	private List<ModbusResponse> readValues(net.wimpi.modbus.msg.ModbusResponse response) {
		
		List<ModbusResponse> results = Lists.newArrayList();
		
		if(Modbus.READ_COILS == response.getFunctionCode()){ //0x01
			ReadCoilsResponse res = (ReadCoilsResponse)response;
			//byte[] bytes = res.getCoils().getBytes();
			for(int i=0 ; i < res.getCoils().size(); i++){
				results.add(new ModbusResponse(res.getUnitID()+"_"+Modbus.READ_COILS+"_"+i, res.getCoils().getBit(i)));
			}
		}else if(Modbus.READ_INPUT_DISCRETES == response.getFunctionCode()){ //0x02
			ReadInputDiscretesResponse res = (ReadInputDiscretesResponse)response;
			//byte[] bytes = res.getDiscretes().getBytes();
			for(int i=0 ; i < res.getDiscretes().size(); i++){
				results.add(new ModbusResponse(res.getUnitID()+"_"+Modbus.READ_INPUT_DISCRETES+"_"+i, res.getDiscretes().getBit(i)));
			}
		}else if(Modbus.READ_MULTIPLE_REGISTERS == response.getFunctionCode()){ //0x03
			ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse)response;
			
			for(int i=0 ; i < res.getWordCount(); i++){
				Register r = res.getRegister(i);
				results.add(new ModbusResponse(res.getUnitID()+"_"+Modbus.READ_MULTIPLE_REGISTERS+"_"+i, r.toUnsignedShort()));
			}
		}else if(Modbus.READ_INPUT_REGISTERS == response.getFunctionCode()){ //0x04
			ReadInputRegistersResponse res = (ReadInputRegistersResponse)response;
			for(int i=0 ; i < res.getWordCount(); i++){
				InputRegister r = res.getRegister(i);
				results.add(new ModbusResponse(res.getUnitID()+"_"+Modbus.READ_INPUT_REGISTERS+"_"+i, r.toUnsignedShort()));
			}
		}
		return results;
	}
	
	
	
}