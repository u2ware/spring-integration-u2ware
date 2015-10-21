package io.github.u2ware.integration.modbus.core;

import io.github.u2ware.integration.common.BuildingAutomationSystemData;

public class ModbusResponse implements BuildingAutomationSystemData{
	
	private static final long serialVersionUID = 256607891287558382L;

	private String id;
	private Object value;
	
	public ModbusResponse(){
	}
	
	public ModbusResponse(String id, Object value){
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ModbusResponse [id=" + id + ", value=" + value + "]";
	}
}
