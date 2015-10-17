package io.github.u2ware.integration.modbus.core;

public class ModbusRequest {

	private int unitId ;
	private int functionCode;
	private int offset ;
	private int count;
	
	public ModbusRequest(){
	}
	public ModbusRequest(int unitId, int functionCode, int offset, int count){
		this.unitId = unitId;
		this.functionCode = functionCode;
		this.offset = offset;
		this.count = count;
	}
	
	public int getUnitId() {
		return unitId;
	}
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	public int getFunctionCode() {
		return functionCode;
	}
	public void setFunctionCode(int functionCode) {
		this.functionCode = functionCode;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "ModbusReq [unitId=" + unitId + ", functionCode=" + functionCode
				+ ", offset=" + offset + ", count=" + count + "]";
	}
}
