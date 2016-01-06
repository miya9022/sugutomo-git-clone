package com.payment.util;

public class Item {
	public int cost;
	public int value;// eg: 1$ = 100point => itemValue = 100;
	
	public Item(int cost, int itemValue) {
		this.cost = cost;
		this.value = itemValue;
	}

}
