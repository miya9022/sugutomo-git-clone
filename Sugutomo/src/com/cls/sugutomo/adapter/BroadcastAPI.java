package com.cls.sugutomo.adapter;

import java.io.File;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.BroadcastModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class BroadcastAPI extends APIClientBaseActivity {

	private static final String TAG = BroadcastAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mContext;
	private APICallbackInterface mActivity;
	private CustomizeProgressDialog mProgress;
	private Vector<BroadcastModel> mListBroadcast;
	private Vector<String> mMessages;

	public BroadcastAPI(APICallbackInterface activity, Activity context,
			Vector<BroadcastModel> list, Vector<String> array) {
		this.mActivity = activity;
		this.mContext = context;
		this.mListBroadcast = list;
		this.mMessages = array;
		this.mProgress = new CustomizeProgressDialog(mContext);
//		this.mProgress.setTitle(mContext.getString(R.string.loading));
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
					mProgress.cancel();
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
						JSONObject data = response.getJSONObject(DATA);
						JSONArray setting = data
								.getJSONArray(Params.PARAM_SETTINGS);
						JSONArray messages = data
								.getJSONArray(Params.PARAM_MESSAGES);

						if (setting != null && setting.length() > 0) {
							for (int i = 0; i < setting.length(); i++) {
								JSONObject item = setting.optJSONObject(i);
								if (item != null) {
									mListBroadcast.add(new BroadcastModel(item
											.optInt(Params.PARAM_PEOPLE), item
											.optInt(Params.PARAM_POINT)));
								}
							}
						}

						if (messages != null && messages.length() > 0) {
							for (int i = 0; i < messages.length(); i++) {
								mMessages.add(messages.optString(i));
							}
						}
						mActivity.handleReceiveData();
					} else {
						ShowMessage
								.showDialog(mContext,
										mContext.getString(R.string.ERR_TITLE),
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

					mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					ShowMessage.showDialog(mContext,
							mContext.getString(R.string.ERR_TITLE),
							mContext.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PARAM_BROADCAST + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		Log.v(TAG, "url request = " + getDefaultURL());
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
