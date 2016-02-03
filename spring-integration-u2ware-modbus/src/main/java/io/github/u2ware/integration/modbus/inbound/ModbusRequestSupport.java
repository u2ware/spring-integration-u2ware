package io.github.u2ware.integration.modbus.inbound;

import io.github.u2ware.integration.modbus.core.ModbusRequest;

import org.springframework.util.StringUtils;

public class ModbusRequestSupport {
	
	private ModbusRequest[] request;
	private int requestIndex;

	public ModbusRequestSupport(ModbusRequest... req){
		this.request = req;
	}

	public ModbusRequestSupport(String... text){
		request = new ModbusRequest[text.length];
		for(int i=0 ; i < text.length; i++){
			String[] items = StringUtils.delimitedListToStringArray(text[i], ":");
			
			int unitId = Integer.parseInt(items[0]);
			int functionCode = Integer.parseInt(items[1]);
			int offset = Integer.parseInt(items[2]);
			int count = Integer.parseInt(items[3]);

			request[i] = new ModbusRequest(unitId, functionCode, offset, count);
		}
	}
	
	public ModbusRequest next(){
		if(request == null) return null;
		
		ModbusRequest result = request[requestIndex];
		requestIndex++;
		if(request.length == requestIndex){
			requestIndex = 0;
		}
		return result;
	}
}
