package com.cls.sugutomo.model;

import java.io.Serializable;

public class ProfileItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public int itemId;
	public String itemName;
	public int displayOrder;
	
	public ProfileItem(int id, int itemId, String itemName, int displayOrder){
		this.id = id;
		this.itemId = itemId;
		this.itemName = itemName;
		this.displayOrder = displayOrder;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
}
