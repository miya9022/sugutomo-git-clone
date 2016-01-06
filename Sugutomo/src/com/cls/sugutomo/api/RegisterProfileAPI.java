package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.userlist.UserListActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class RegisterProfileAPI extends APIClientBaseActivity {

	private static final String TAG = RegisterProfileAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private boolean isEditProfile = false;
	private APICallbackInterface callback;

	public RegisterProfileAPI(Activity activity) {
		this.mActivity = activity;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
	}

	public RegisterProfileAPI(Activity activity,
			APICallbackInterface _callback, boolean isEditProfile) {
		this.mActivity = activity;
		this.callback = _callback;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(false);
		this.isEditProfile = isEditProfile;
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
						// ShowMessage.showMessage(mActivity, message);
						// save user info
						JSONObject jData = response.getJSONObject(DATA);
						JSONObject userInfo = jData.getJSONObject(USER);
						JSONObject openfireInfo = jData.getJSONObject(OPENFIRE);
						if (Global.userInfo == null)
							Global.userInfo = new UserModel();
						Global.userInfo = Global.setUserInfo(userInfo,
								openfireInfo);
						// go to main activity
						if (isEditProfile) {
							callback.handleGetList();
							ShowMessage
									.showDialog(
											mActivity,
											mActivity
													.getString(R.string.tv_edit_profile),
											mActivity
													.getString(R.string.profile_update_ok));
						} else
							handlerComplete();
					} else {
						ShowMessage.showDialog(mActivity, mActivity
								.getString(R.string.ERR_TITLE), mActivity
								.getString(R.string.profile_update_fail));
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

					mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					ShowMessage.showDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE),
							mActivity.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	private void handlerComplete() {
		Intent intent = new Intent(mActivity, UserListActivity.class);
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.diagslide_enter,
				R.anim.diagslide_leave);
		mActivity.finish();
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.REQUEST_METHOD_USER + Params.SAVE_PROFILE
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
