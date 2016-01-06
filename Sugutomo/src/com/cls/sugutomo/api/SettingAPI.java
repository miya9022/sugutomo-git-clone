package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class SettingAPI extends APIClientBaseActivity {

	private static final String TAG = SettingAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private String mMethod;

	public SettingAPI(Activity activity, String method) {
		mActivity = activity;
		mProgress = new CustomizeProgressDialog(mActivity);
//		mProgress.setTitle(mActivity.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
		mMethod = method;
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	private String getUrl() {
		return BASE_URL + mMethod + File.separator;
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
					if (OK.equals(response.get(CODE))) {
						// Log.v(TAG, "response=" + response.toString());
						String message = response.getString(MESSAGE);
						// Log.v(TAG, "OK" + " message=" + message);
						if (message != null && message != "")
							ShowMessage.showDialog(mActivity, mActivity
									.getTitle().toString(), mActivity
									.getString(R.string.success));
						if (mMethod.equalsIgnoreCase(Params.DELETE_ACC)) {
							ShowMessage.showMessage(mActivity, getString(R.string.delete_acc_success));
                             Global.backToLogin(mActivity);
						}
					} else {
						String title = mActivity.getString(R.string.ERR_TITLE);
						String message = response.getString(MESSAGE);
						// Log.v(TAG, "FAIL" + " message=" + message);
						if (title != null && title != "")
							ShowMessage.showDialog(mActivity, title,
									mActivity.getString(R.string.fail));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					mProgress.dismiss();
					// Log.v(TAG, "onFailure:");
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
		return null;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getUrl(), mParams, responseHandler);
	}

}
