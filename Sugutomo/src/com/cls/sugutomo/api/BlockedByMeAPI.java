package com.cls.sugutomo.api;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class BlockedByMeAPI extends APIClientBaseActivity {

	private static final String TAG = BlockedByMeAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private ArrayList<Integer> mListUserBlocked=null;
	private  ArrayList<Integer> listBlockedOrBeBlocked =null;

	public BlockedByMeAPI(Activity activity, ArrayList<Integer> list,boolean isListUserBlock) {
		mActivity = activity;
		if(isListUserBlock) mListUserBlocked = list;
		else listBlockedOrBeBlocked = list;
		mProgress = new CustomizeProgressDialog(mActivity);
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams mParams) {
		this.mParams = mParams;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if(mListUserBlocked!=null )mListUserBlocked.clear();
				if(listBlockedOrBeBlocked!=null )listBlockedOrBeBlocked.clear();
				
				//Log.i(TAG, "Load list user blocked complete");
//				String message = response.optString(MESSAGE);
				if (OK.equals(response.optString(CODE))) {
					JSONArray data = response.optJSONArray(DATA);
					if (data != null && data.length() > 0) {
						for (int i = 0; i < data.length(); i++) {
							JSONObject item = data.optJSONObject(i);
							UserModel userModel = new UserModel();
							userModel = Global.setUserInfo(item, null);
							if(mListUserBlocked!=null ){
								mListUserBlocked.add(userModel.getUserId());
								Log.v(TAG, " I am Blocked User "+userModel.getUserId());
							}
							if(listBlockedOrBeBlocked!=null ){
								listBlockedOrBeBlocked.add(userModel.getUserId());
								Log.v(TAG, "Blocked all User "+userModel.getUserId());
							}
						}
					}
				} else {
//					ShowMessage.showDialog(mActivity,
//							mActivity.getString(R.string.ERR_TITLE), message);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				mProgress.dismiss();
				Log.v(TAG, "onFailure:");
//				if(mActivity!=null) ShowMessage.showDialog(mActivity,
//						mActivity.getString(R.string.ERR_TITLE),
//						mActivity.getString(R.string.ERR_CONNECT_FAIL));
			}

		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PARAM_BLOCK + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
