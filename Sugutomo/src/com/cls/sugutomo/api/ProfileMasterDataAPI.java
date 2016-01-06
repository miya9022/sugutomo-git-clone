package com.cls.sugutomo.api;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.dialog.DialogTwoButton;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.profile.CreateProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class ProfileMasterDataAPI extends APIClientBaseActivity {

	private static final String TAG = ProfileMasterDataAPI.class
			.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private APICallbackInterface callback;
	private ArrayList<ProfileModel> listProfileModel;
	private CustomizeProgressDialog mProgress;
	private DialogTwoButton dtb = null;
	private ArrayList<ImageModel> avatarList;

	public ProfileMasterDataAPI(Activity activity,
			ArrayList<ProfileModel> listProfileModel) {
		this.mActivity = activity;
		this.listProfileModel = listProfileModel;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
	}

	public ProfileMasterDataAPI(APICallbackInterface callback,
			Activity activity, ArrayList<ProfileModel> listProfileModel,
			ArrayList<ImageModel> avatarList) {
		this.callback = callback;
		this.mActivity = activity;
		this.listProfileModel = listProfileModel;
		this.avatarList = avatarList;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
	}

	public ProfileMasterDataAPI(DialogTwoButton dialog, Activity activity,
			ArrayList<ProfileModel> listProfileModel) {
		this.dtb = dialog;
		this.mActivity = activity;
		this.listProfileModel = listProfileModel;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				try {
					mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				
				try {
					mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {
						JSONArray data = response.getJSONObject(DATA)
								.getJSONArray(PROFILES);
						for (int i = 0; i < data.length(); i++) {
							JSONObject item = data.getJSONObject(i);
							int profile_id = item
									.getInt(Params.PARAM_PROFILE_ID);
							String profile_name = item
									.getString(Params.PARAM_PROFILE_NAME);
							int profile_display_order = item
									.getInt(Params.PARAM_PROFILE_DISPLAY);
							int is_input = item.getInt(Params.PARAM_IS_INPUT);
							String userdata = item
									.getString(Params.PARAM_USER_DATA);
							JSONArray profile_items = item
									.getJSONArray(Params.PARAM_PROFILE_ITEMS);
							String valueText = "";
							ArrayList<ProfileItem> listProfileItem = new ArrayList<ProfileItem>();
							if (profile_items.length() > 0) {
								for (int j = 0; j < profile_items.length(); j++) {
									JSONObject subItem = profile_items
											.getJSONObject(j);
									int profile_item_id = subItem
											.getInt(Params.PARAM_PROFILE_ITEM_ID);
									String profile_item_name = subItem
											.getString(Params.PARAM_PROFILE_ITEM_NAME);
									int profile_item_display_order = subItem
											.getInt(Params.PARAM_PROFILE_ITEM_DISPLAY);
									ProfileItem subProfileItem = new ProfileItem(
											profile_id, profile_item_id,
											profile_item_name,
											profile_item_display_order);
									listProfileItem.add(subProfileItem);
									if (userdata.equalsIgnoreCase(""
											+ profile_item_id))
										valueText = profile_item_name;
								}
							} else {
								valueText = userdata;
							}
							ProfileModel profileModel = new ProfileModel(
									profile_id, profile_name,
									profile_display_order, is_input, valueText,
									userdata, listProfileItem);
							profileModel.printProfile();
							listProfileModel.add(profileModel);
						}

						// avatar
						if (avatarList != null) {
							JSONArray urls = response.getJSONObject(DATA)
									.getJSONArray(AVATARS);
							for (int i = 0; i < urls.length(); i++) {
//							for (int i = urls.length()-1; i > 0; i--) {
								JSONObject item = urls.getJSONObject(i);
								int id = item.getInt(Params.PARAM_AVATAR_ID);
								String url = item
										.getString(Params.PARAM_AVATAR_FULL_URL);
								ImageModel imgModel = new ImageModel(id, url,
										Global.userInfo.getUserId());
								if (id == Global.userInfo.getImageId()) 
									imgModel.setAvatar(true);
								avatarList.add(imgModel);
//								if (id == Global.userInfo.getImageId()) {
//									imgModel.setAvatar(true);
//									avatarList.add(0, imgModel);
//								} else {
//									avatarList.add(imgModel);
//								}
							}
						}
						//
					}
					if (dtb != null)
						dtb.handleGetList();
					else if (callback != null)
						callback.handleReceiveData();
					else
						((CreateProfileActivity) mActivity).initListView();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					
				mProgress.dismiss();
				Log.v(TAG, "onFailure:");
				ShowMessage.showDialog(mActivity,
						mActivity.getString(R.string.ERR_TITLE),
						mActivity.getString(R.string.ERR_CONNECT_FAIL));
				if (dtb == null && callback == null)
					((CreateProfileActivity) mActivity).initListView();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.REQUEST_METHOD_USER + Params.MY_PROFILE
				+ File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		Log.v(TAG, "url request = " + getDefaultURL());
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
