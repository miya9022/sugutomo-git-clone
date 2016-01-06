package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.SplashScreen;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class LogoutAPI extends APIClientBaseActivity {

	private static final String TAG = LogoutAPI.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private SessionManager mSession;
	private CustomizeProgressDialog mProgress;

	public LogoutAPI(Activity activity, SessionManager session) {
		this.mActivity = activity;
		this.mSession = session;
		this.mProgress = new CustomizeProgressDialog(mActivity);
//		this.mProgress.setTitle(mActivity.getString(R.string.log_out));
//		this.mProgress.setCanceledOnTouchOutside(true);
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
						handlerComplete();
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
				Log.v(TAG, "onFailure:");
				try {

					mProgress.dismiss();
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
		// After logout redirect user to Logging Activity
		Intent i = new Intent(mActivity, SplashScreen.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


		// logout session
		mSession.logoutUser();

		// Staring Login Activity
		mActivity.startActivity(i);
		mActivity.overridePendingTransition(R.anim.diagslide_enter,
				R.anim.diagslide_leave);
		mActivity.finish();
		// disconnect service
		sendStopServiceSignal();

	}

	private void sendStopServiceSignal() {
		Intent intent = new Intent(mActivity.getPackageName()
				+ Params.STOP_SERVICE);
		Log.d(TAG, "send stop service broad cast");
		mActivity.sendBroadcast(intent);
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.REQUEST_METHOD_USER + Params.LOGOUT
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
