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
import com.cls.sugutomo.model.InformationModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class InformationAPI extends APIClientBaseActivity {

	private static final String TAG = InformationAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivityCallback;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private Vector<InformationModel> mListInfor;
	private String mRequest;

	public InformationAPI(APICallbackInterface activity, Activity context,
			Vector<InformationModel> list, String request) {
		this.mActivityCallback = activity;
		this.mActivity = context;
		this.mListInfor = list;
		this.mRequest = request;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.loading));
//		this.mProgress.setCanceledOnTouchOutside(false);
//		this.mProgress.setCancelable(true);
	}

	public String getRequest() {
		return mRequest;
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
						JSONArray data = response.getJSONArray(DATA);
						if (data != null && data.length() > 0) {
							for (int i = 0; i < data.length(); i++) {
								JSONObject item = data.getJSONObject(i);
								InformationModel infor = new InformationModel();
								infor.setId(item.optInt(Params.PARAM_ID));
								infor.setType(item.optInt(Params.PARAM_TYPE));
								infor.setTitle(item
										.optString(Params.PARAM_TITLE));
								infor.setContent(item
										.optString(Params.PARAM_CONTENT));
								infor.setOrder(item.optInt(Params.PARAM_ORDER));
								infor.setCreatedDateTime(item
										.optInt(Params.PARAM_CREATED_DATETIME));
								infor.setUpdatedDateTime(item
										.getInt(Params.PARAM_UPDATED_DATETIME));

								mListInfor.add(infor);
								// Log.v("",
								// item.optString(Params.PARAM_CONTENT));
							}
							mActivityCallback.handleReceiveData();
						}
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

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PAGE + File.separator + getRequest()
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
