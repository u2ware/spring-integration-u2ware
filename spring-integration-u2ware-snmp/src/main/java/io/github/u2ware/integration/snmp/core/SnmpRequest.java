package io.github.u2ware.integration.snmp.core;





public class SnmpRequest {

	private String host;
	private int port;
	private String rootOid;
	
	public SnmpRequest(){
	}
	public SnmpRequest(String host, int port, String rootOid){
		this.host = host;
		this.port = port;
		this.rootOid = rootOid;
	}
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
