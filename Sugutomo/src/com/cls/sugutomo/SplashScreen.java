package com.cls.sugutomo;

import java.util.Arrays;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class SplashScreen extends Activity implements APICallbackInterface {
	private SessionManager mSession;
	private CommonLogin mCommonLogin;
	private UiLifecycleHelper mUiHelper;
	private LoginButton mLoginButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
//		mSession = SessionManager.getInstance(getApplicationContext());
//		mCommonLogin = new CommonLogin(this, this, mSession);

//		mUiHelper = new UiLifecycleHelper(this, callback);
//		mUiHelper.onCreate(savedInstanceState);
		
		// Init UI
//				mLoginButton = (LoginButton) findViewById(R.id.login_FB_button);
//				mLoginButton.setReadPermissions(Arrays.asList("public_profile",
//						"email", "user_friends"));
//				mLoginButton.setUserInfoChangedCallback(userInfoChangedCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		Session session = Session.getActiveSession();
//		if (session != null && (session.isOpened() || session.isClosed())) {
//			onSessionStateChange(session, session.getState(), null);
//		}
//		mUiHelper.onResume();
//		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		mUiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		mUiHelper.onActivityResult(requestCode, resultCode, data,
//				dialogCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
//		mUiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		mUiHelper.onDestroy();
//		mCommonLogin.onDestroyManual();
	}

	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.btn_register_mail:{
//			 Intent intent = new Intent(this, RegisterActivity.class);
//			 startActivity(intent);
//			 overridePendingTransition(R.anim.diagslide_enter,
//			 R.anim.diagslide_leave);
//			 finish();
//			break;
//		}
//		case R.id.login_anonymous:
//			login_anonymous();
//			break;
//		case R.id.txt_policy: {
//			Intent intent = new Intent(this, InformationActivity.class);
//			intent.putExtra(Params.PARAM_TITLE,
//					getString(R.string.privace_policy));
//			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
//			intent.putExtra(Params.REQUEST, Params.POLICY);
//			intent.putExtra(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN, true);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//			overridePendingTransition(R.anim.diagslide_enter,
//					R.anim.diagslide_leave);
//		}
//			break;
//		case R.id.txt_term_of_user: {
//			Intent intent = new Intent(this, InformationActivity.class);
//			intent.putExtra(Params.PARAM_TITLE,
//					getString(R.string.term_of_user));
//			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
//			intent.putExtra(Params.REQUEST, Params.TERM);
//			intent.putExtra(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN, true);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//			overridePendingTransition(R.anim.diagslide_enter,
//					R.anim.diagslide_leave);
//		}
//			break;
		case R.id.btn_new_register :{
			Intent intent = new Intent(this, TypeRegisterActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			finish();
		}
			break;
		case R.id.btn_login: {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			finish();
		}
			break;

		// case R.id.btn_new_register: {
		// Intent intent = new Intent(this, RegisterActivity.class);
		// startActivity(intent);
		// overridePendingTransition(R.anim.diagslide_enter,
		// R.anim.diagslide_leave);
		// finish();
		// }
		// break;
		}
	}

//	private Session.StatusCallback callback = new Session.StatusCallback() {
//		@Override
//		public void call(Session session, SessionState state,
//				Exception exception) {
//			onSessionStateChange(session, state, exception);
//		}
//	};
//	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
//		@Override
//		public void onError(FacebookDialog.PendingCall pendingCall,
//				Exception error, Bundle data) {
//			Log.d("", String.format("Error: %s", error.toString()));
//		}
//
//		@Override
//		public void onComplete(FacebookDialog.PendingCall pendingCall,
//				Bundle data) {
//			Log.d("", "Success!");
//		}
//	};
//	private UserInfoChangedCallback userInfoChangedCallback = new UserInfoChangedCallback() {
//		@Override
//		public void onUserInfoFetched(final GraphUser user) {
//			if (user == null) {
//				return;
//			}
//			handleFacebookLogin(user.getId(), user.getName(),
//					(String) user.getProperty(Params.PARAM_EMAIL));
//			Session sessionFB = Session.getActiveSession();
//			sessionFB.closeAndClearTokenInformation();
//		}
//	};

//	/**
//	 * LOGIN THROW FACEBOOK ID
//	 * 
//	 * @param facebook_id
//	 * @param name
//	 * @param email
//	 */
//	private void handleFacebookLogin(String facebook_id, String name,
//			String email) {
//		RequestParams params = new RequestParams();
//		params.put(Params.PARAM_FB, facebook_id);
//		params.put(Params.PARAM_EMAIL, email);
//		params.put(Params.PARAM_NAME, name);
//		params.put(Params.PARAM_UDID, Global.getDeviceUDID(this));
//		params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
//		mCommonLogin.setParams(params);
//		mCommonLogin.setLoginType(Global.TYPE_FACEBOOK);
//		mCommonLogin.onRunButtonPressed();
//		// Log.v(TAG, "login fb xxxx");
//
//	}
//
//	private void onSessionStateChange(Session session, SessionState state,
//			Exception exception) {
//		if (state.isOpened()) {
//			Log.i("", "Logged in...");
//		} else if (state.isClosed()) {
//			Log.i("", "Logged out...");
//		}
//	}

	private void createDialogEditInput() {
		AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);
		mAlertDialog.setTitle(getString(R.string.anonymous));
		// user input
		final EditText input = new EditText(this);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		input.setSingleLine();
		input.setHint(R.string.hint_name);
		mAlertDialog.setInverseBackgroundForced(true);
		mAlertDialog.setView(input);
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		mAlertDialog.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = input.getText().toString().trim();
						if (text.length() > 0) {
							RequestParams params = new RequestParams();
							params.put(Params.PARAM_NAME, text);
							params.put(Params.PARAM_UDID, Global
									.getDeviceUDID(getApplicationContext()));
							params.put(Params.PARAM_PLATFORM,
									Params.PARAM_ANDROID);
							params.put(Params.PARAM_PLATFORM_VERSION,
									Build.VERSION.RELEASE);
							params.put(Params.PARAM_PHONE_NAME, Build.MODEL);
							mCommonLogin.setParams(params);
							mCommonLogin
									.setLoginType(Global.TYPE_ANONYMOUS_REGISTER);
							mCommonLogin.onRunButtonPressed();
						} else {
							ShowMessage
									.showDialog(
											SplashScreen.this,
											SplashScreen.this
													.getString(R.string.ERR_TITLE),
											SplashScreen.this
													.getString(R.string.ERR_INPUT_EMPTY));
						}
					}
				});
		mAlertDialog.show();
	}

	private boolean isAnonymous = false;

	private void login_anonymous() {
		isAnonymous = true;
		RequestParams params = new RequestParams();
		params.put(Params.PARAM_UDID, Global.getDeviceUDID(this));
		params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
		mCommonLogin.setParams(params);
		mCommonLogin.setLoginType(Global.TYPE_ANONYMOUS);
		mCommonLogin.onRunButtonPressed();
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		if (isAnonymous) {
			isAnonymous = false;
			Intent intent = new Intent(this, RegisterAnonymousActivity.class);
			intent.putExtra(Params.START_FROM_LOGIN, false);
			 startActivity(intent);
			 overridePendingTransition(R.anim.diagslide_enter,
			 R.anim.diagslide_leave);
			 finish();
//			createDialogEditInput();
		}
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}
}
