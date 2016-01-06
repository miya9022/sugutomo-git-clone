package com.cls.sugutomo.model;

public class BroadcastModel {

	private int people;
	private int point;
	
	public BroadcastModel(int people, int point) {
		super();
		this.people = people;
		this.point = point;
	}
	
	public int getPeople() {
		return people;
	}
	public void setPeople(int people) {
		this.people = people;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	
	
}
