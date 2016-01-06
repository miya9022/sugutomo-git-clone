package com.cls.sugutomo;

import java.util.Arrays;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.apiclient.Params;
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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TypeRegisterActivity extends Activity implements APICallbackInterface {
	private SessionManager mSession;
	private CommonLogin mCommonLogin;
	private UiLifecycleHelper mUiHelper;
	private LoginButton mLoginButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_type_register);
		mSession = SessionManager.getInstance(getApplicationContext());
		mCommonLogin = new CommonLogin(this, this, mSession);

		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);
		
		// Init UI
		mLoginButton = (LoginButton) findViewById(R.id.login_FB_button);
		mLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));
		mLoginButton.setUserInfoChangedCallback(userInfoChangedCallback);
	}
	
	private void backClick() {
		Intent intent = new Intent(this, SplashScreen.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.diagslide_enter_back,
				R.anim.diagslide_leave_back);
		finish();
	}

	@Override
	public void onBackPressed() {
		backClick();
		// super.onBackPressed();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		mUiHelper.onResume();
		AppEventsLogger.activateApp(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mUiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();
		mCommonLogin.onDestroyManual();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mUiHelper.onActivityResult(requestCode, resultCode, data,
				dialogCallback);
	}
	
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.btn_register_mail:{
				 Intent intent = new Intent(this, RegisterActivity.class);
				 startActivity(intent);
				 overridePendingTransition(R.anim.diagslide_enter,
				 R.anim.diagslide_leave);
				 finish();
				break;
			}
			case R.id.login_anonymous:
				login_anonymous();
				break;
			case R.id.btn_back:
				backClick();
				break;
		}
	}
	
	
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("facebook register", "Success!");
		}
	};
	private UserInfoChangedCallback userInfoChangedCallback = new UserInfoChangedCallback() {
		@Override
		public void onUserInfoFetched(final GraphUser user) {
			if (user == null) {
				return;
			}
			handleFacebookLogin(user.getId(), user.getName(), (String) user.getProperty(Params.PARAM_EMAIL));
			Session sessionFB = Session.getActiveSession();
			sessionFB.closeAndClearTokenInformation();
		}
	};

	/**
	 * LOGIN THROW FACEBOOK ID
	 * 
	 * @param facebook_id
	 * @param name
	 * @param email
	 */
	private void handleFacebookLogin(String facebook_id, String name,
			String email) {
		RequestParams params = new RequestParams();
		params.put(Params.PARAM_FB, facebook_id);
		params.put(Params.PARAM_EMAIL, email);
		params.put(Params.PARAM_NAME, name);
		params.put(Params.PARAM_UDID, Global.getDeviceUDID(this));
		params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
		mCommonLogin.setParams(params);
		mCommonLogin.setLoginType(Global.TYPE_FACEBOOK);
		mCommonLogin.onRunButtonPressed();
		// Log.v(TAG, "login fb xxxx");

	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i("", "Logged in...");
		} else if (state.isClosed()) {
			Log.i("", "Logged out...");
		}
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
			 overridePendingTransition(R.anim.diagslide_enter, R.anim.diagslide_leave);
			 finish();
//			createDialogEditInput();
		}
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}
}
