package com.cls.sugutomo.model;

public class ConversationModel {

	private long conversationId;
	private int fromUser;
	private int toUser;
	private int newMessages;
	private String userInfo;
	
	public ConversationModel(long conversationId, int from, int to, int newMessage, String userInfo){
		this.conversationId = conversationId;
		this.fromUser = from;
		this.toUser = to;
		this.newMessages = newMessage;
		this.userInfo = userInfo;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	
	public int getNewMessages() {
		return newMessages;
	}

	public void setNewMessages(int newMessages) {
		this.newMessages = newMessages;
	}

	public long getConversationId() {
		return conversationId;
	}

	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}

	public int getFromUser() {
		return fromUser;
	}

	public void setFromUser(int fromUser) {
		this.fromUser = fromUser;
	}

	public int getToUser() {
		return toUser;
	}

	public void setToUser(int toUser) {
		this.toUser = toUser;
	}
}
