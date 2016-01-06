package com.cls.sugutomo.api;

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
import com.cls.sugutomo.map.MapActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class MapAPI extends APIClientBaseActivity {

	private static final String TAG = MapAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private boolean callBack = true;
	private boolean showProgress = true;

	public MapAPI(Activity activity) {
		mActivity = activity;
		mProgress = new CustomizeProgressDialog(mActivity);
//		mProgress.setTitle(mActivity.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
	}

	public MapAPI(Activity activity, boolean noCallBack) {
		mActivity = activity;
		mProgress = new CustomizeProgressDialog(mActivity);
//		mProgress.setTitle(mActivity.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
		callBack = noCallBack;
	}

	public void setShowProgress(boolean showProgressBar) {
		showProgress = showProgressBar;
	}

	public RequestParams getmParams() {
		return mParams;
	}

	public void setmParams(RequestParams mParams) {
		this.mParams = mParams;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				try {

					if (showProgress)
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {

					if (showProgress)
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (showProgress && mActivity != null)
						mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {

						JSONArray user_list = response.getJSONArray(DATA);
						Log.d("load map success", user_list.toString());
						if (callBack)
							((MapActivity) mActivity)
									.handleLoadMapSuccess(user_list);
					} else {
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								mActivity.getString(R.string.ERR_CONNECT_FAIL));
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

					if (showProgress)
						mProgress.dismiss();
					Log.v(TAG, "load map onFailure: update mapp");
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
		return BASE_URL + Params.MAP;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
