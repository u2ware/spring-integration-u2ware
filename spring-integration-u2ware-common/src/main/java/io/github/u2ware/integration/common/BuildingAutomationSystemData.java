package io.github.u2ware.integration.common;

import java.io.Serializable;

public interface BuildingAutomationSystemData extends Serializable{

	public String getId();
	public Object getValue();

	public void setId(String id);
	public void setValue(Object value);
	
}
