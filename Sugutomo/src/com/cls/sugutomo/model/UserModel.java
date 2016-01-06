package com.cls.sugutomo.model;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.Marker;

public class UserModel implements Serializable, Comparable<UserModel>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2121512094369427173L;
	/**
	 * 
	 */
	private int id;
	private String username;
	private String name;
	private String password;
	private String wallStatus;
	private String facebookId;
	private String email;
	private String image;
	private int sex;
	private int image_id;
	private String birthday;
	private int interestedPartner;
	private int possessPoint;
	private int height;
	private int bodyFigure;
	private int age;
	
	private Location location;
	private Bitmap avatarBitmap;
	private Marker marker;
	private boolean actived;

	private int isFriend;
	private int isLogin;
	private long lastLoginDatetime;
	private double gpsLat;
	private double gpsLng;
	private String distance;
	private int countFootprint;
	
	public UserModel() {
	}

	public UserModel(String username, String name, String email,
			String image) {
		this.username = username;
		this.name = name;
		this.email = email;
		this.image = image;
	}

	public UserModel(int user_id, String username, String name, Double gpsLat,
			Double gpsLng, int sex, String status, int isLogin, String image, long timeLastLogin) {
	    this.id = user_id;
		this.username = username;
		this.name = name;
		this.gpsLat = gpsLat;
		this.gpsLng = gpsLng;
		this.wallStatus = status;
		this.sex = sex;
		this.isLogin = isLogin;
		this.image = image;
		this.lastLoginDatetime = timeLastLogin;
	}

	public int getUserId() {
		return id;
	}

	public void setUserId(int id) {
		this.id = id;
	}

	public String getUserName() {
		if(username==null) return getName();
		else return username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWallStatus() {
		return wallStatus;
	}

	public void setWallStatus(String status) {
		this.wallStatus = status;
	}
	
	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserAvatar() {
		return image;
	}

	public void setUserAvatar(String userAvatar) {
		this.image = userAvatar;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Bitmap getAvatarBitmap() {
		return avatarBitmap;
	}

	public void setAvatarBitmap(Bitmap avatar) {
		this.avatarBitmap = avatar;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public boolean isActived() {
		return actived;
	}

	public void setActived(boolean actived) {
		this.actived = actived;
	}

	public double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public double getGpsLng() {
		return gpsLng;
	}

	public void setGpsLng(double gpsLng) {
		this.gpsLng = gpsLng;
	}
	
	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public int getIsLogin() {
		return isLogin;
	}

	public void setIsLogin(int isLogin) {
		this.isLogin = isLogin;
	}

	public long getTimeLastLogin() {
		return lastLoginDatetime;
	}

	public void setTimeLastLogin(long timeLastLogin) {
		this.lastLoginDatetime = timeLastLogin;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getImageId() {
		return image_id;
	}

	public void setImageId(int image_id) {
		this.image_id = image_id;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getInterestedPartner() {
		return interestedPartner;
	}

	public void setInterestedPartner(int interestedPartner) {
		this.interestedPartner = interestedPartner;
	}

	public int getPossessPoint() {
		return possessPoint;
	}

	public void setPossessPoint(int possessPoint) {
		this.possessPoint = possessPoint;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getBodyFigure() {
		return bodyFigure;
	}

	public void setBodyFigure(int bodyFigure) {
		this.bodyFigure = bodyFigure;
	}
	
    public int getCountFootprint() {
		return countFootprint;
	}

	public void setCountFootprint(int countFootprint) {
		this.countFootprint = countFootprint;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public int compareTo(UserModel another) {
		int compareMessage = ((UserModel)another).getCountFootprint();
		return compareMessage - this.getCountFootprint();
	}
}
