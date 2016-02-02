package io.github.u2ware.integration.bacnet.core;

public class BacnetRequest {

	private String host;
	private int port;
	private int instanceNumber;

	public BacnetRequest(){
		
	}
	public BacnetRequest(String host, int port, int instanceNumber){
		this.host = host;
		this.port = port;
		this.instanceNumber = instanceNumber;
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
	public int getInstanceNumber() {
		return instanceNumber;
	}
	public void setInstanceNumber(int instanceNumber) {
		this.instanceNumber = instanceNumber;
	}
	@Override
	public String toString() {
		return "BacnetRequest [host=" + host + ", port=" + port
				+ ", instanceNumber=" + instanceNumber + "]";
	}


}
