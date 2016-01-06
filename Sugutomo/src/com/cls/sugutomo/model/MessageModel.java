package com.cls.sugutomo.model;

import java.io.Serializable;

public class MessageModel implements Comparable<MessageModel>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long conversationId;
	private long messageId;
	private UserModel userInfo;
	private String message;
	private long dateMessage;
	private String distance;
	private boolean error = false;
	private int newMessages;
	private String type;
	private String lastSender;
	public MessageModel(String _message, String _type){
		super();
		this.message = _message;
		this.type = _type;
	}
	public MessageModel(long conversationId, long messageId,
			UserModel userInfo, String message, boolean error) {
		super();
		this.conversationId = conversationId;
		this.messageId = messageId;
		this.userInfo = userInfo;
		this.message = message;
		this.error = error;
	}

	public MessageModel(long conversationId, long messageId,
			UserModel userInfo, String message, long dateMessage,
			String distance, boolean error) {
		super();
		this.conversationId = conversationId;
		this.messageId = messageId;
		this.userInfo = userInfo;
		this.message = message;
		this.dateMessage = dateMessage;
		this.distance = distance;
		this.error = error;
	}

	public MessageModel() {
		super();
	}

	public long getConversationId() {
		return conversationId;
	}

	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public UserModel getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserModel userInfo) {
		this.userInfo = userInfo;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getDateMessage() {
		return dateMessage;
	}

	public void setDateMessage(long dateMessage) {
		this.dateMessage = dateMessage;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getNewMessages() {
		return newMessages;
	}

	public void setNewMessages(int newMessages) {
		this.newMessages = newMessages;
	}

	public String getLastSender() {
		return lastSender;
	}
	public void setLastSender(String lastSender) {
		this.lastSender = lastSender;
	}
	@Override
	public int compareTo(MessageModel another) {
		// TODO Auto-generated method stub
		long compareMessage = ((MessageModel) another).getDateMessage();
		return (int) (compareMessage - this.getDateMessage());
	}
}
