package com.cls.sugutomo.model;

public class BuyPointModelItem {

	public int cost;
	public int duration;
	public int point;
	public int id;

	public BuyPointModelItem(int cost, int value) {
		this.cost = cost;
		this.duration = value;
		this.point = value;
	}
	public BuyPointModelItem(int id,int cost, int value) {
		this.id=id;
		this.cost = cost;
		this.duration = value;
		this.point = value;
	}
	public int getCost() {
		return cost;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getduration() {
		return duration;
	}

	public void setduration(int duration) {
		this.duration = duration;
	}

}