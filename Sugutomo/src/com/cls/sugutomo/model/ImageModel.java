package com.cls.sugutomo.model;

import java.io.Serializable;

public class ImageModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long imageId;
	private String fileName;
	private String fullPath;
	private long timeCreated;
	private UserModel userModel;
	private boolean isAvatar =false;
	private int userId;
	public ImageModel() {

	}
	
	public ImageModel(long imageId, String fullPath,int userId) {
		this.imageId = imageId;
		this.fullPath = fullPath;
		this.fileName =""+fullPath.hashCode();
		this.userId=userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isAvatar() {
		return isAvatar;
	}

	public void setAvatar(boolean isAvatar) {
		this.isAvatar = isAvatar;
	}

	public long getImageId() {
		return imageId;
	}

	public void setImageId(long imageId) {
		this.imageId = imageId;
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}
}
