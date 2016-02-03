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

		logger.info("Mobdus Client Initialized: "+host+":"+port);
	}
	@Override
	public void destroy() throws Exception {
		logger.info("Mobdus Client Terminated: "+host+":"+port);
	}
	
	private InetAddress getInetAddress() throws Exception{
		if("localhost".equals(host) || "127.0.0.1".equals(host)){
			return InetAddress.getLocalHost();
		}else{
			return InetAddress.getByName(host); 
		}
	}
	
	public Object execute(ModbusRequest request) throws Exception{
		return readValues(request);
	}
	
	public synchronized List<ModbusResponse> readValues(ModbusRequest request) throws Exception{
	    long startTime = System.currentTimeMillis();

	    net.wimpi.modbus.msg.ModbusRequest modbusRequest = convertRequest(request);
		net.wimpi.modbus.msg.ModbusResponse modbusResponse = execute(modbusRequest);
		List<ModbusResponse> response = convertResponse(modbusResponse);

	    logger.info(request+", ModbusResponse [size="+response.size()
				+", timeInMillis="+ (System.currentTimeMillis()-startTime)
				+"]");
		
		return response;
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized <Q extends net.wimpi.modbus.msg.ModbusRequest, A extends net.wimpi.modbus.msg.ModbusResponse> A execute(Q request) throws Exception{

		TCPMasterConnection con = new TCPMasterConnection(getInetAddress());
        con.setPort(port);
		con.setTimeout(100000);
        
		ModbusTransaction trans = new ModbusTCPTransaction(con);
		//trans.setRetries(100);
		
		//logger.debug("Mobdus Client Request: "+host+":"+port+"\n"+request.getClass().getName()+"\n"+request.getHexMessage());
		trans.setRequest(request);
		trans.execute();
		net.wimpi.modbus.msg.ModbusResponse response = trans.getResponse();
		//logger.debug("Mobdus Client Response: "+host+":"+port+"\n"+response.getClass().getName()+"\n"+response.getHexMessage());
		
        con.close();

		return (A)response;
	}

	
	//0x01	//Read Coils              //read-write   //ReadCoils 
	//0x02	//Read Discrete Inputs    //read-only    //ReadInputDiscretes
	//0x03	//Read Holding Registers  //read-write   //ReadMultipleRegisters
	//0x04	//Read Input Registers    //read-only    //ReadInputRegister
	private net.wimpi.modbus.msg.ModbusRequest convertRequest(ModbusRequest request){

		//System.err.println(function+" "+unitId+" "+ref+" "+count);

		if(Modbus.READ_COILS == request.getFunctionCode()){ //0x01
			if(request.getCount() > 2000) return null;
			ReadCoilsRequest modbusRequest = new ReadCoilsRequest(request.getOffset(), request.getCount());
			modbusRequest.setUnitID(request.getUnitId());
			return modbusRequest;
		}else if(Modbus.READ_INPUT_DISCRETES == request.getFunctionCode()){ //0x02
			if(request.getCount() > 2000) return null;
			ReadInputDiscretesRequest modbusRequest = new ReadInputDiscretesRequest(request.getOffset(), request.getCount());
			modbusRequest.setUnitID(request.getUnitId());
			return modbusRequest;
		}else if(Modbus.READ_MULTIPLE_REGISTERS == request.getFunctionCode()){ //0x03
			if(request.getCount() > 125) return null;
			ReadMultipleRegistersRequest modbusRequest = new ReadMultipleRegistersRequest(request.getOffset(), request.getCount());
			modbusRequest.setUnitID(request.getUnitId());
			return modbusRequest;
		}else if(Modbus.READ_INPUT_REGISTERS == request.getFunctionCode()){
			if(request.getCount() > 125) return null;
			ReadInputRegistersRequest modbusRequest = new ReadInputRegistersRequest(request.getOffset(), request.getCount());
			modbusRequest.setUnitID(request.getUnitId());
			return modbusRequest;
		}		
		return null;
	}
	
	private List<ModbusResponse> convertResponse(net.wimpi.modbus.msg.ModbusResponse response) {
		
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