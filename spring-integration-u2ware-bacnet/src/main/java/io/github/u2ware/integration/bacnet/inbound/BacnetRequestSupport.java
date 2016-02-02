package io.github.u2ware.integration.bacnet.inbound;

import io.github.u2ware.integration.bacnet.core.BacnetRequest;

import org.springframework.util.StringUtils;

public class BacnetRequestSupport {
	
	private BacnetRequest[] request;
	private int requestIndex;

	public BacnetRequestSupport(BacnetRequest... req){
		this.request = req;
	}

	public BacnetRequestSupport(String... text){
		request = new BacnetRequest[text.length];
		for(int i=0 ; i < text.length; i++){
			String[] items = StringUtils.delimitedListToStringArray(text[i], ":");
			
			String host = items[0];
			int port = Integer.parseInt(items[1]);
			int instanceNumber = Integer.parseInt(items[2]);
			request[i] = new BacnetRequest(host, port, instanceNumber);
		}
	}
	
	public BacnetRequest next(){
		if(request == null) return null;
		
		BacnetRequest result = request[requestIndex];
		requestIndex++;
		if(request.length == requestIndex){
			requestIndex = 0;
		}
		return result;
	}
}
