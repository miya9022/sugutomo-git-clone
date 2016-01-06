package com.cls.sugutomo.api;

import java.io.File;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class GetFootprintAPI extends APIClientBaseActivity {

	private static final String TAG = GetFootprintAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivityCallback;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private Vector<UserModel> mListUser;
	private boolean useProgressbar;

	public GetFootprintAPI(APICallbackInterface activity, Activity context,
			Vector<UserModel> list) {
		this.mActivityCallback = activity;
		this.mActivity = context;
		this.mListUser = list;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(context.getString(R.string.uploading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(true);
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	public void setUseProgressbar(boolean isUse) {
		this.useProgressbar = isUse;
	}

	private boolean isUseProgressbar() {
		return this.useProgressbar;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {
			public void onStart() {
				super.onStart();
				try {
					if (isUseProgressbar())
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
					String message = response.getString(MESSAGE);
					if (OK.equals(response.getString(CODE))) {
						JSONArray data = response.getJSONObject(DATA)
								.getJSONArray(USERS);
						if (data != null && data.length() > 0) {
							for (int i = 0; i < data.length(); i++) {
								UserModel user = Global.setUserInfo(
										data.getJSONObject(i), null);
//								if (!user.getName().equalsIgnoreCase("null")) {
//									//null la case sau khi footprint no tu del hoac bi adlin del acc
//									mListUser.add(user);
//								}
								mListUser.add(user);
							}
						}
						Global.newFootPrint = Integer.valueOf(response
								.getJSONObject(DATA).getString("total"));
						mActivityCallback.handleReceiveData();
					} else {
						if (isUseProgressbar()) ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								message);
					}
				} catch (Exception e) {
					if (isUseProgressbar()) ShowMessage.showDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE),
							mActivity.getString(R.string.ERR_TITLE));
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					if (isUseProgressbar()) ShowMessage.showDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE),
							mActivity.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.USER + File.separator
				+ Params.PARAM_GET_FOOTPRINT + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		Log.v(TAG, "url request = " + getDefaultURL());
		client.setTimeout(20000);// 10s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
