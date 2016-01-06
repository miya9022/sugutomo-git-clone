package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.WaitingAcceptAccountActivity;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class RegisterEmailAPI extends APIClientBaseActivity {

	private static final String TAG = RegisterEmailAPI.class.getSimpleName();
	private RequestParams mParams;
	private SessionManager mSession;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;

	public RegisterEmailAPI(Activity activity, SessionManager session) {
		mActivity = activity;
		mSession = session;
		mProgress = new CustomizeProgressDialog(mActivity);
//		mProgress.setTitle(mActivity.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	private String getUrl() {
		return BASE_URL + Params.REQUEST_METHOD_USER + Params.REGISTER
				+ File.separator;
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

						String message = response.getString(MESSAGE);
						JSONObject data = response.getJSONObject(DATA);
						Log.v(TAG, "OK" + " message=" + message);
						if (message != null && message != "")
							ShowMessage.showMessage(mActivity, message);

						// save session: userid and token
						String token = data.getString(Params.PARAM_TOKEN);
						int userid = data.getJSONObject(Params.USER).getInt(
								Params.PARAM_ID);
						String email = data.getJSONObject(Params.USER)
								.getString(Params.PARAM_EMAIL);
						Log.v(TAG, "save session userid=" + userid + ", token="
								+ token + ", email=" + email);
						mSession.createLoginSession(userid, token,
								Global.TYPE_EMAIL, true);

						// store username, password
						// JSONObject openfire_info = data
						// .getJSONObject("openfire");
						// String username = openfire_info
						// .getString(Params.PARAM_USERNAME);
						// String password = openfire_info
						// .getString(Params.PARAM_PASSWORD);
						// mSession.storeLoginInfo(username, password);

						// intent to waiting screen
						handlerRegisterSuccessed(email);
					} else {
						Log.v(TAG, "error code=" + response.getInt(CODE));
						String title = response.getString(MESSAGE);
						JSONObject data = response.getJSONObject(DATA);
						String message = getMessage(data, Params.PARAM_USERNAME)
								+ "\n" + getMessage(data, Params.PARAM_EMAIL);
						Log.v(TAG, "FAIL title=" + title + ", message="
								+ message);
						// show dialog error
						if (title != null && message != null)
							ShowMessage.showDialog(mActivity, title, message);
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

	private void handlerRegisterSuccessed(String email) {
		// change to Waiting accept account
		Intent intent = new Intent(mActivity,
				WaitingAcceptAccountActivity.class);
		intent.putExtra(Params.PARAM_EMAIL, email);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.diagslide_enter,
				R.anim.diagslide_leave);
		mActivity.finish();
	}

	@Override
	public String getDefaultURL() {
		return null;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		Log.v(TAG, "url=" + getUrl());
		return client.post(this, getUrl(), mParams, responseHandler);
	}

}
