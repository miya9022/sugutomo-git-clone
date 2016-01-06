package com.cls.sugutomo.api;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.model.StampModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class LoadStampAPI extends APIClientBaseActivity {

	private static final String TAG = LoadStampAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private ArrayList<StampModel> mListStamp;

	public LoadStampAPI(Activity activity, ArrayList<StampModel> hashmapStamp) {
		mActivity = activity;
		mListStamp = hashmapStamp;
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
			public void onStart() {
				super.onStart();
			}

			@Override
			public void onCancel() {
				super.onCancel();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					String message = response.getString(MESSAGE);
					if (OK.equals(response.getString(CODE))) {
						JSONArray data = response.getJSONArray(DATA);
						SharedPreferences pref = mActivity.getSharedPreferences(Global.PREFERENCE_NAME, 0);
						Editor editor = pref.edit();
						editor.putString(Params.PREF_STAMP,data.toString());
						editor.commit();
						if (data != null && data.length() > 0) {
							for (int i = 0; i < data.length(); i++) {
								JSONObject item = data.getJSONObject(i);
								StampModel stamp = new StampModel();
								stamp.setStampId(item
										.optInt(Params.PARAM_STAMP_ID));
								stamp.setName(item
										.optString(Params.PARAM_IMAGE));
								stamp.setUrl(item
										.optString(Params.PARAM_IMAGE_FULL_URL));
								mListStamp.add(stamp);
							}
						}
					} else {
//						ShowMessage.showDialog(mActivity,
//								mActivity.getString(R.string.ERR_TITLE),
//								message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Log.v(TAG, "onFailure:");
				ShowMessage.showDialog(mActivity,
						mActivity.getString(R.string.ERR_TITLE),
						mActivity.getString(R.string.ERR_CONNECT_FAIL));
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PARAM_STAMP + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
