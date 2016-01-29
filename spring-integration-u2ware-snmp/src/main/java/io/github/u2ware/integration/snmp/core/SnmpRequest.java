package io.github.u2ware.integration.snmp.core;

import org.springframework.util.StringUtils;



public class SnmpRequest {

	private String address;
	private String host;
	private int port;
	private String rootOid = "1.3.6";
	
	private SnmpRequest[] request;
	private int requestIndex;

	public SnmpRequest(){
	
	}
	public SnmpRequest(String address, String rootOid){
		setAddress(address);
		setRootOid(rootOid);
	}
	
	public SnmpRequest(SnmpRequest... request){
		this.request = request;
	}
	
	public SnmpRequest next(){
		if(request == null) return this;
		
		SnmpRequest result = request[requestIndex];
		requestIndex++;
		if(request.length == requestIndex){
			requestIndex = 0;
		}
		return result;
	}
	public String host() {
		return host;
	}
	public Integer port() {
		return port;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		String[] addr = StringUtils.delimitedListToStringArray(address, ":");
		this.host = addr[0];
		this.port = Integer.parseInt(addr[1]);
		this.address = address;
	}
	public String getRootOid() {
		return rootOid;
	}
	public void setRootOid(String rootOid) {
		this.rootOid = rootOid;
	}
	@Override
	public String toString() {
		return "SnmpRequest [address=" + address + ", rootOid=" + rootOid + "]";
	}
}
