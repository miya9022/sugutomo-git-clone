package com.payment.util;

public class ItemInfo {
	public String itemName;
	public int itemValue;// eg: 1$ = 100point => itemValue = 100;
	public ItemInfo(String itemName) {
		this.itemName = itemName;
	}
	public ItemInfo(String itemName, int itemValue) {
		this.itemName = itemName;
		this.itemValue = itemValue;
	}

}
