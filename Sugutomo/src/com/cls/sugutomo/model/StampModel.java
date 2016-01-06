package com.cls.sugutomo.model;

import android.graphics.Bitmap;

public class StampModel {

	private int stampId;
	private int stampSourceId;
	private Bitmap bitmap;
	private String name;
	private String url;
	
	public StampModel() {}
	
	public StampModel(int stampId, int stampSourceId, String name, String url) {
		super();
		this.stampId = stampId;
		this.stampSourceId = stampSourceId;
		this.name = name;
		this.url = url;
	}
	
	public StampModel(int stampId, Bitmap bitmap, String name, String url) {
		super();
		this.stampId = stampId;
		this.bitmap = bitmap;
		this.name = name;
		this.url = url;
	}
	
	public int getStampSourceId() {
		return stampSourceId;
	}

	public void setStampSourceId(int stampSourceId) {
		this.stampSourceId = stampSourceId;
	}

	public int getStampId() {
		return stampId;
	}

	public void setStampId(int stampId) {
		this.stampId = stampId;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
