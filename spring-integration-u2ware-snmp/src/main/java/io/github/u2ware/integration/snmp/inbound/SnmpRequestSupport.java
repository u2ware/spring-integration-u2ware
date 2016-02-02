package io.github.u2ware.integration.snmp.inbound;

import io.github.u2ware.integration.snmp.core.SnmpRequest;

import org.springframework.util.StringUtils;

public class SnmpRequestSupport {
	
	private SnmpRequest[] request;
	private int requestIndex;

	public SnmpRequestSupport(SnmpRequest... req){
		this.request = req;
	}

	public SnmpRequestSupport(String... text){
		request = new SnmpRequest[text.length];
		for(int i=0 ; i < text.length; i++){
			String[] items = StringUtils.delimitedListToStringArray(text[i], ":");
			
			String host = items[0];
			int port = Integer.parseInt(items[1]);
			String rootOid = items[2];
			request[i] = new SnmpRequest(host, port, rootOid);
		}
	}
	
	public SnmpRequest next(){
		if(request == null) return null;
		
		SnmpRequest result = request[requestIndex];
		requestIndex++;
		if(request.length == requestIndex){
			requestIndex = 0;
		}
		return result;
	}
}
