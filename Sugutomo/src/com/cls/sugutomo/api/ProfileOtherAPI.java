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
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.ProfileModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class ProfileOtherAPI extends APIClientBaseActivity {

	private static final String TAG = ProfileOtherAPI.class.getSimpleName();
	private Activity mActivity;
	private RequestParams mParams;
	ArrayList<ProfileModel> listProfile;
	private APICallbackInterface mActivityCallback;
	private CustomizeProgressDialog mProgress;
	private ArrayList<ImageModel> userAvatarList;

	public ProfileOtherAPI(APICallbackInterface activity, Activity context,
			ArrayList<ProfileModel> list) {
		this.mActivity = context;
		listProfile = list;
		mActivityCallback = activity;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
	}

	public ProfileOtherAPI(APICallbackInterface activity, Activity context,
			ArrayList<ProfileModel> list, ArrayList<ImageModel> userAvatarList) {
		this.mActivity = context;
		listProfile = list;
		mActivityCallback = activity;
		this.mProgress = new CustomizeProgressDialog(mActivity);
		this.userAvatarList = userAvatarList;
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(true);
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
					String message = response.getString(MESSAGE);
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
							// int is_input =
							// item.getInt(Params.PARAM_IS_INPUT);
							String profile_value = item
									.getString(Params.PARAM_USER_DATA_TEXT);
							ProfileModel profile = new ProfileModel(profile_id,
									profile_name, profile_display_order,
									profile_value);
							listProfile.add(profile);
							// Log.e(TAG, "profile  = " + profile_name);
						}

						// user id
						JSONObject js = response.getJSONObject(DATA)
								.getJSONObject(USER);
						int userid = js.getInt(Params.PARAM_ID);
						// avatar
						JSONArray urls = response.getJSONObject(DATA)
								.getJSONArray(AVATARS);
						for (int i = 0; i < urls.length(); i++) {
							JSONObject item = urls.getJSONObject(i);
							int id = item.getInt(Params.PARAM_AVATAR_ID);
							String url = item
									.getString(Params.PARAM_AVATAR_FULL_URL);
							userAvatarList.add(new ImageModel(id, url, userid));
						}

						mActivityCallback.handleGetList();
					}else if(ERROR_10027.equals(response.getString(CODE))){
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.not_found_info_user),
								message);
					} else {
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {

					ShowMessage.showDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE),
							mActivity.getString(R.string.ERR_CONNECT_FAIL));
					mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.USER + File.separator + Params.GET_PROFILE
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
