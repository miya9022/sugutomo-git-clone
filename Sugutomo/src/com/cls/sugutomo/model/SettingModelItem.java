package com.cls.sugutomo.model;



public class SettingModelItem {

	private String name;
	private String value;
	
	public SettingModelItem(String name, String value) {
		this.value = value;
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
