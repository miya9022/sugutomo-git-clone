package com.cls.sugutomo.model;

public class CreateProfileItem {

	private String mTitle;
	private boolean mIsHeader;
	private boolean mIsNeed;
	private String mEditInput;
	private int mPosSelect;
	private ProfileModel mProfileModel;
	
	public CreateProfileItem(){}
	
	//only header
	public CreateProfileItem(String title, boolean isHeader){
		this.mIsHeader = isHeader;
		this.mTitle = title;
	}
	
	public CreateProfileItem(String title, boolean isneed, String editInput, int posSelect){
		this.mTitle = title;
		this.mIsNeed = isneed;
		this.mEditInput = editInput;
		this.mPosSelect = posSelect;
		this.mIsHeader = false;
	}
	
	//title, isNeed, editInput
	public CreateProfileItem(String title, boolean isneed, String editInput, ProfileModel profileModel){
		this.mTitle = title;
		this.mIsNeed = isneed;
		this.mEditInput = editInput;
		this.mProfileModel = profileModel;
		this.mIsHeader = false;
	}
	
	public boolean isHeader() {
		return mIsHeader;
	}

	public void setHeader(boolean isHeader) {
		this.mIsHeader = isHeader;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public boolean isNeed() {
		return mIsNeed;
	}

	public void setNeed(boolean isNeed) {
		this.mIsNeed = isNeed;
	}

	public String getEditInput() {
		return mEditInput;
	}

	public void setEditInput(String editInput) {
		this.mEditInput = editInput;
	}

	public ProfileModel getProfileModel() {
		return mProfileModel;
	}

	public void setProfileModel(ProfileModel profileModel) {
		this.mProfileModel = profileModel;
	}
	
	public int getPosSelect() {
		return mPosSelect;
	}

	public void setPosSelect(int mPosSelect) {
		this.mPosSelect = mPosSelect;
	}
}
