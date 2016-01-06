package com.cls.sugutomo.model;


public class NavDrawerItem {

	private UserModel usermodel;
	private String title;
	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;
	private boolean isAvatar = false;
	
	public NavDrawerItem(){}

	public NavDrawerItem(boolean isAvatar, UserModel user){
		this.isAvatar = isAvatar;
		this.usermodel = user;
		this.title = user.getName();
	}
	
	public NavDrawerItem(String title){
		this.title = title;
	}
	
	public NavDrawerItem(String title, boolean isCounterVisible){
		this.title = title;
		this.isCounterVisible = isCounterVisible;
	}
	
	public NavDrawerItem(String title, boolean isCounterVisible, String count){
		this.title = title;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}
	
	public NavDrawerItem(String title, int icon){
		this.title = title;
		this.icon = icon;
	}
	
	public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}
	
	public UserModel getUsermodel() {
		return usermodel;
	}

	public void setUsermodel(UserModel usermodel) {
		this.usermodel = usermodel;
	}

	public boolean isAvatar() {
		return isAvatar;
	}

	public void setAvatar(boolean isAvatar) {
		this.isAvatar = isAvatar;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public int getIcon(){
		return this.icon;
	}
	
	public String getCount(){
		return this.count;
	}
	
	public boolean getCounterVisibility(){
		return this.isCounterVisible;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setIcon(int icon){
		this.icon = icon;
	}
	
	public void setCount(String count){
		this.count = count;
	}
	
	public void setCounterVisibility(boolean isCounterVisible){
		this.isCounterVisible = isCounterVisible;
	}
}
