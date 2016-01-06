package com.cls.sugutomo.api;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.cls.sugutomo.R;
import com.cls.sugutomo.WaitingAcceptAccountActivity;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.chat.ChatBackgroundService.ChatServiceBinder;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.profile.UploadPhotoActivity;
import com.cls.sugutomo.userlist.UserListActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class CommonLogin extends APIClientBaseActivity {

	private static final String TAG = CommonLogin.class.getSimpleName();
	private RequestParams mParams;
	private Activity mActivity;
	private String mToken;
	private int mLoginType = -1;
	private CustomizeProgressDialog mProgress = null;
	private boolean mIsBound = false;
	private APICallbackInterface mActivityCallback;

	public void setLoginType(int mLoginType) {
		this.mLoginType = mLoginType;
	}

	private SessionManager mSession;
	private ChatBackgroundService mService;

	private void bindService() {
		if (!mIsBound) {
			Intent intent = new Intent(mActivity, ChatBackgroundService.class);
			mActivity
					.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			Log.d("ChatCheck", "bind service");
		}
	}

	public void unBindService() {
		if (mIsBound) {
			mActivity.unbindService(mConnection);
			mIsBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("ChatCheck", "on service connected");
			mIsBound = true;
			mService = ((ChatServiceBinder) service).getService();
			mService.registerReceiver();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService.unregisterReceiverManual();
		}

	};

	public CommonLogin(Activity activity,
			APICallbackInterface activityCallback, SessionManager session) {
		mActivity = activity;
		mSession = session;
		mActivityCallback = activityCallback;
		mProgress = new CustomizeProgressDialog(mActivity);
		// mProgress.setTitle("Loading...");
		// mProgress.setCanceledOnTouchOutside(false);
		// mProgress.setCancelable(true);

		// bind service
		bindService();
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	public void prepareAutoLoginParam(int user_id, String token) {
		mParams = new RequestParams();
		mParams.put(Params.PARAM_USER_ID_, user_id);
		mParams.put(Params.PARAM_TOKEN_, token);
	}

	public void prepareEmailLoginParam(String email, String password) {
		mParams = new RequestParams();
		mParams.put(Params.PARAM_EMAIL, email);
		mParams.put(Params.PARAM_PASSWORD, password);
	}

	public String getLoginUrl() {
		if (mToken != null) {
			Log.d("login url:", Params.USER_GET_LOGIN);
			return BASE_URL + Params.USER_GET_LOGIN;
		} else if (mLoginType == Global.TYPE_EMAIL) {
			return BASE_URL + Params.USER_LOGIN;
		} else if (mLoginType == Global.TYPE_FACEBOOK) {
			return BASE_URL + Params.USER_FB_LOGIN;
		} else if (mLoginType == Global.TYPE_ANONYMOUS) {
			return BASE_URL + "user/login_anonymous";
		} else if (mLoginType == Global.TYPE_ANONYMOUS_REGISTER)
			return BASE_URL + "user/register_anonymous";
		else
			return null;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getLoginUrl(), mParams, responseHandler);

	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				try {

					if (mProgress != null)
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {

					if (mProgress != null)
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (mProgress != null)
						mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {

						JSONObject jData = response.getJSONObject(DATA);
						String token = jData.getString(Params.PARAM_TOKEN);
						JSONObject userInfo = jData.getJSONObject(USER);
						JSONObject openfireInfo = jData.getJSONObject(OPENFIRE);

						Global.listBadWord = new HashMap<String, ArrayList<String>>();
						JSONArray restrick_word = jData
								.getJSONArray(RESTRICT_WORD);
						for (int i = 0; i < restrick_word.length(); i++) {
							String badWord = restrick_word.getString(i);
							Log.d("", "restrick:" + badWord);
							String key = badWord.substring(0, 1);
							ArrayList<String> list = Global.listBadWord
									.get(key);
							if (list == null)
								list = new ArrayList<String>();
							list.add(badWord);
							Global.listBadWord.put(key, list);
						}
						SharedPreferences prefs = mActivity
								.getSharedPreferences(Global.PREFERENCE_NAME,
										Context.MODE_PRIVATE);
						Editor editor = prefs.edit();
						editor.putString(Params.PREF_BAD_WORD,
								restrick_word.toString());
						editor.commit();
						// //test too much badword check performance
						// for (int i = 0; i < 1000; i++) {
						// ArrayList<String> list = new ArrayList<String>();
						// list.add("test whga  asjgasjg dshgsd  djhgjdsgh djhjsd"+i);
						// Global.listBadWord.put("" + i, list);
						// }
						// end test too much badword
						// store token here
						boolean requirePassword = mSession.isLoggedIn();
						requirePassword = !mSession.getRequirePassword();
//						Log.d(TAG, "require password: " + requirePassword);
						if (mLoginType != -1) {
							mSession.createLoginSession(
									userInfo.getInt(Params.PARAM_ID), token,
									mLoginType, requirePassword);
						}
						handleLoginSuccessed(token, userInfo, openfireInfo);

					} else if (response.getInt(CODE) == Params.INVALID_TOKEN) {
						mSession.resetToken();
						unBindService();
						ShowMessage.showMessage(mActivity,
								mActivity.getString(R.string.ERR_TITLE));
						mActivityCallback.handleReceiveData();
					} else if (response.getInt(CODE) == Params.INVALID_ANONYMOUS_NOT_REGISTER) {

						mSession.resetToken();
						unBindService();
						mActivityCallback.handleReceiveData();
					} else {
						unBindService();
						Log.d("login: wrong email&pwd",
								response.getString(MESSAGE));
						// String message = response.getString(MESSAGE);
						// if (message != null && message != "")
						ShowMessage.showMessage(mActivity,
								mActivity.getString(R.string.ERR_TITLE));
						mActivityCallback.handleReceiveData();
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

					if (mProgress != null)
						mProgress.dismiss();
					ShowMessage.showMessage(mActivity,
							mActivity.getString(R.string.ERR_CONNECT_FAIL));
					// mActivityCallback.handleReceiveData();
				} catch (Exception e) {
					e.printStackTrace();
					//r TODO: handle exception
				}
			}
		};
	}

	private void handleLoginSuccessed(String token, JSONObject userInfo,
			JSONObject openfireInfo) throws JSONException {
		unBindService();
		if (userInfo.getInt(Params.GROUP_ID) == Params.MEMBER_NOT_ACTIVE) {
			Log.d(TAG, "member is not actived");
			Intent intent = new Intent(mActivity,
					WaitingAcceptAccountActivity.class);
			intent.putExtra(Params.PARAM_EMAIL,
					userInfo.getString(Params.PARAM_EMAIL));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mActivity.startActivity(intent);
			mActivity.overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			mActivity.finish();
		} else if (userInfo.getInt(Params.GROUP_ID) != Params.MEMBER_NOT_COMPLETE_INFO) {
			Log.d(TAG, "member is  completed info");
			// if (Global.userInfo == null)
			// Global.userInfo = new UserModel();
			Global.userInfo = Global.setUserInfo(userInfo, openfireInfo);
			mSession.storeOpenFireInfo(Global.userInfo.getUserName(),
					Global.userInfo.getPassword());

			notifyLoginOpenFire();
			mActivity.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			Intent intent = new Intent();
			intent.setClass(mActivity, UserListActivity.class);

			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mActivity.startActivity(intent);
			mActivity.overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			mActivity.finish();
		} else {
			Global.userInfo = Global.setUserInfo(userInfo, openfireInfo);
			Log.d(TAG, "member is not completed info with username:"
					+ Global.userInfo.getUserName());
			mSession.storeOpenFireInfo(Global.userInfo.getUserName(),
					Global.userInfo.getPassword());
			Intent intent = new Intent(mActivity, UploadPhotoActivity.class);
			intent.putExtra(Params.PARAM_TOKEN, token);
			intent.putExtra(Params.PARAM_USER_ID,
					userInfo.getInt(Params.PARAM_ID));
			intent.putExtra(Params.PARAM_NAME,
					userInfo.getString(Params.PARAM_NAME));
			intent.putExtra(Params.PARAM_USERNAME,
					userInfo.getString(Params.PARAM_USERNAME));
			if (mLoginType == Global.TYPE_FACEBOOK)
				intent.putExtra(Params.PARAM_FB,
						userInfo.getString(Params.PARAM_FB));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			unBindService();
			mActivity.startActivity(intent);
			mActivity.overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			mActivity.finish();
		}

	}

	private void notifyLoginOpenFire() {
		Intent intent = new Intent(mActivity.getPackageName()
				+ Params.AUTHENTICATE);
		Log.d(TAG, "send broadcast login");
		mActivity.sendBroadcast(intent);
		unBindService();
	}

	public void onDestroyManual() {
		mProgress = null;
		Log.d(TAG, "onCommonLogin is destroyed");
		//if(mService!=null) mService.unregisterReceiverManual();
		unBindService();
	}

	public void setToken(String token) {
		mToken = token;
	}

	@Override
	public String getDefaultURL() {
		return null;
	}
}
