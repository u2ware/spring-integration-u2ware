package io.github.u2ware.integration.snmp.core;


public class SnmpResponse {
	
	private String id;
	private Object value;
	private String name;
	
	public SnmpResponse(){
		
	}
	SnmpResponse(String id, Object value){
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/*
	@SuppressWarnings("unchecked")
	public void addValue(Object addValue) {
		if(getValue() == null){
			setValue(addValue);
		
		}else{
			if(getValue() instanceof Collection){
				Collection<Object> listValue = (Collection<Object>)getValue();
				listValue.add(addValue);
				
			}else{
				Collection<Object> listValue = new ArrayList<Object>();
				listValue.add(getValue());
				listValue.add(addValue);
				setValue(listValue);
			}
		}
	}
	*/
	@Override
	public String toString() {
		return "SnmpResponse [id=" + id + ", value=" + value + ", name=" + name
				+ "]";
	}

}
