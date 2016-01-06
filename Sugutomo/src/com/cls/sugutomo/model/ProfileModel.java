package com.cls.sugutomo.model;

import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

public class ProfileModel implements Serializable{

	private static final String TAG = ProfileModel.class.getSimpleName();
	public int id;
	public String name;
	public int displayOrder;
	public int isInput;
	public ArrayList<ProfileItem> profileItem;
	public String value;
	public String valueID;

	public ProfileModel(int id, String name, int displayOrder, int isInput,
			String valueText, ArrayList<ProfileItem> profileItem) {
		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;
		this.isInput = isInput;
		this.value = valueText;
		this.profileItem = profileItem;
	}

	public ProfileModel(int id, String name, int displayOrder, int isInput,
			String valueText, String valueID, ArrayList<ProfileItem> profileItem) {
		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;
		this.isInput = isInput;
		if (valueText == null || valueText.equalsIgnoreCase("null"))
			this.value = "";
		else
			this.value = valueText;
		if (valueID == null || valueID.equalsIgnoreCase("null"))
			this.valueID = "";
		else
			this.valueID = valueID;
		this.profileItem = profileItem;
	}

	public ProfileModel(int id, String name, int displayOrder, String value) {
		this.id = id;
		this.name = name;
		this.displayOrder = displayOrder;
		this.value = value;
	}

	public void printProfile() {
		Log.i(TAG, "id = " + getId());
		Log.i(TAG, "name = " + getName());
		Log.i(TAG, "displayOrder = " + getDisplayOrder());
		Log.i(TAG, "isInput = " + getIsInput());
		if (getProfileItem().size() > 0) {
			for (int i = 0; i < getProfileItem().size(); i++) {
				Log.i(TAG, "item_id = " + getProfileItem().get(i).getItemId());
				Log.i(TAG, "profile_item_name = "
						+ getProfileItem().get(i).getItemName());
				Log.i(TAG, "profile_item_display_order = "
						+ getProfileItem().get(i).getDisplayOrder());
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int getIsInput() {
		return isInput;
	}

	public void setIsInput(int isInput) {
		this.isInput = isInput;
	}

	public ArrayList<ProfileItem> getProfileItem() {
		return profileItem;
	}

	public void setProfileItem(ArrayList<ProfileItem> profileItem) {
		this.profileItem = profileItem;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
