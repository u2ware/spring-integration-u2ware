package io.github.u2ware.integration.bacnet.core;

public class BacnetRequest {

	public static final String READ_TYPE = "read";
	public static final String WRITE_TYPE = "write";
	
	private String type = READ_TYPE;
	private String remoteAddress;
	private int remoteInstanceNumber;

	public BacnetRequest(){
	}
	public BacnetRequest(String remoteAddress, int remoteInstanceNumber){
		this.remoteAddress = remoteAddress;
		this.remoteInstanceNumber = remoteInstanceNumber;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public int getRemoteInstanceNumber() {
		return remoteInstanceNumber;
	}
	public void setRemoteInstanceNumber(int remoteInstanceNumber) {
		this.remoteInstanceNumber = remoteInstanceNumber;
	}
}
