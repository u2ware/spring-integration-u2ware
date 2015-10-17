package io.github.u2ware.integration.bacnet.core;

public class BacnetRequest {

	public static final String READ_TYPE = "read";
	public static final String WRITE_TYPE = "write";
	
	private String type = READ_TYPE;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
