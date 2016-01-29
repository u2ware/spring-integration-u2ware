package io.github.u2ware.integration.snmp.core;



public class SnmpRequest {

	private String host;
	private Integer port;
	private String rootOid = "1.3.6";

	private SnmpRequest[] request;
	private int requestIndex;

	public SnmpRequest(){
	
	}
	public SnmpRequest(String host, int port, String rootOid){
		this.host = host;
		this.port = port;
		this.rootOid = rootOid;
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
		return "SnmpRequest [host=" + host + ", port=" + port + ", rootOid="
				+ rootOid + "]";
	}
}
