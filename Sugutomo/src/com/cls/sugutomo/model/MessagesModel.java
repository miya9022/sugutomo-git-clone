package com.cls.sugutomo.model;

import java.util.Vector;

public class MessagesModel {
	private UserModel userModel;
	private Vector<MessageModel> messages;

	public MessagesModel(UserModel usermodel, Vector<MessageModel> messages) {
		super();
		this.userModel = usermodel;
		this.messages = messages;
	}

	public MessagesModel() {
		super();
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public Vector<MessageModel> getMessages() {
		return messages;
	}

	public void setMessages(Vector<MessageModel> messages) {
		this.messages = messages;
	}
	
}
