package io.github.u2ware.integration.snmp.core;

import org.springframework.util.StringUtils;


public class SnmpRequest {

	private String host;
	private Integer port;
	private String rootOid = "1.3.6";

	public SnmpRequest(){
	
	}
	public SnmpRequest(String text) throws Exception{
		
		String[] items = StringUtils.delimitedListToStringArray(text, ":");
		
		if(items.length == 3){
			this.host = items[0];
			this.port = Integer.parseInt(items[1]);
			this.rootOid = items[2];

		}else{
			throw new Exception("items size ("+items.length+") is not good. ");
		}
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getRootOid() {
		return rootOid;
	}
	public void setRootOid(String rootOid) {
		this.rootOid = rootOid;
	}

	@Override
	public String toString() {
		return "SnmpRequest ["+host+":"+port+":"+rootOid+"]";
	}
}
