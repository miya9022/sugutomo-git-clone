package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class SetFootprintAPI extends APIClientBaseActivity {

	private static final String TAG = SetFootprintAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private int mRequestCode;
	public static int REMOVE_FOOTPRINT = 1;
	public static int SET_FOOTPRINT = 0;

	public SetFootprintAPI(Activity context, int mRequestCode) {
		this.mActivity = context;
		this.mRequestCode = mRequestCode;
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
						Log.i(TAG, "send set footprint complete");
					} else {
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								message);
					}
				} catch (Exception e) {
					ShowMessage.showDialog(mActivity,
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
					Log.v(TAG, "onFailure:");
					ShowMessage.showDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE),
							mActivity.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		if (mRequestCode == SET_FOOTPRINT)
			return BASE_URL + Params.USER + File.separator
					+ Params.PARAM_FOOTPRINT + File.separator;
		else
			return BASE_URL + Params.USER + File.separator
					+ Params.PARAM_REMOVE_FOOTPRINT + File.separator;
		// PARAM_REMOVE_FOOTPRINT
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		//Log.v(TAG, "url request = " + getDefaultURL());
		client.setTimeout(20000);// 10s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
