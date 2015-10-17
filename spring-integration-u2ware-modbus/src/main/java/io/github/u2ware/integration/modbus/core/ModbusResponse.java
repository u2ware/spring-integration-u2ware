package io.github.u2ware.integration.modbus.core;

import java.io.Serializable;

public class ModbusResponse implements Serializable, Comparable<ModbusResponse>{
	
	private static final long serialVersionUID = 256607891287558382L;

	private String componentName;
	private String id;
	private Object presentValue;
	
	public ModbusResponse(){
	}
	
	public ModbusResponse(String componentName, String id, Object presentValue){
		this.componentName = componentName;
		this.id = id;
		this.presentValue = presentValue;
	}

	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getPresentValue() {
		return presentValue;
	}
	public void setPresentValue(Object presentValue) {
		this.presentValue = presentValue;
	}

	@Override
	public int compareTo(ModbusResponse o) {
		return id.compareTo(o.id);
	}
	@Override
	public String toString() {
		return "ModbusRes [componentName=" + componentName + ", id=" + id
				+ ", presentValue=" + presentValue + "]";
	}

}
