package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class UnReadAPI extends APIClientBaseActivity {

	private static final String TAG = UnReadAPI.class.getSimpleName();
	private RequestParams mParams;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private boolean isUseProgressBar=true;

	public UnReadAPI( Context context) {
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
	}
public void setUseProgresBar(boolean isUseProgressBar){
	this.isUseProgressBar=isUseProgressBar;
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
				try {
					if(isUseProgressBar) mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					if(isUseProgressBar)	mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if(isUseProgressBar) mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {
						
							// 
							JSONObject jData = response.getJSONObject(DATA);
							int total = jData.getInt("total");
							 Global.newMessage=total;
//							 Log.v("", "Global.newMessage:"+Global.newMessage);
							 Intent i = new Intent(mContext.getPackageName() + Params.PARAM_BROADCAST);
							 mContext.sendBroadcast(i);
					} else {
//						Global.customDialog((Activity) mContext, mContext.getString(R.string.fail), null);
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
					if(isUseProgressBar)	mProgress.dismiss();
//					Global.customDialog((Activity) mContext, mContext.getString(R.string.ERR_CONNECT_FAIL), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {

		return BASE_URL + "chat" + File.separator+"unread_count"+ File.separator;

	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
