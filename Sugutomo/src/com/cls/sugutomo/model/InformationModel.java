package com.cls.sugutomo.model;

public class InformationModel {

	private int id;
	private int type;
	private String title;
	private String content;
	private int order;
	private int createdDateTime;
	private int updatedDateTime;
	
	public InformationModel(){}
	
	public InformationModel(int id, int type, String title, String content,
			int order, int createdDateTime, int updatedDateTime) {
		super();
		this.id = id;
		this.type = type;
		this.title = title;
		this.content = content;
		this.order = order;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(int createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public int getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(int updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	
	
	
}
