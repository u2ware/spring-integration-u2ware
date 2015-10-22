package io.github.u2ware.integration.bacnet.core;

import java.io.Serializable;



public class BacnetResponse implements Serializable{
	
	private static final long serialVersionUID = 8696164872704881555L;
	
	private String id; //<remoteDevice instanceNumber>_<oid instanceNumber>_<oid type>
	private Object value;
	
	private String objectIdentifier;
	private String units;
	private String outputUnits;
	private String inactiveText;
	private String activeText;
	private String stateText;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getObjectIdentifier() {
		return objectIdentifier;
	}
	public void setObjectIdentifier(String objectIdentifier) {
		this.objectIdentifier = objectIdentifier;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getOutputUnits() {
		return outputUnits;
	}
	public void setOutputUnits(String outputUnits) {
		this.outputUnits = outputUnits;
	}
	public String getInactiveText() {
		return inactiveText;
	}
	public void setInactiveText(String inactiveText) {
		this.inactiveText = inactiveText;
	}
	public String getActiveText() {
		return activeText;
	}
	public void setActiveText(String activeText) {
		this.activeText = activeText;
	}
	public String getStateText() {
		return stateText;
	}
	public void setStateText(String stateText) {
		this.stateText = stateText;
	}
	@Override
	public String toString() {
		return "BacnetResponse [id=" + id + ", value=" + value
				+ ", objectIdentifier=" + objectIdentifier + ", units=" + units
				+ ", outputUnits=" + outputUnits + ", inactiveText="
				+ inactiveText + ", activeText=" + activeText + ", stateText="
				+ stateText + "]";
	}
}
