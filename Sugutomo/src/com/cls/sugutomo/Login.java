package com.cls.sugutomo;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.api.ResendEmailActiveAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.CreateProfileItem;
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

public class Login extends Activity implements TextWatcher,
		APICallbackInterface {
	private static final String TAG = Login.class.getSimpleName();

	private LoginButton mLoginButton;
	private UiLifecycleHelper mUiHelper;
	private Context mContext;

	private EditText mPasswordEditText, mForgotPassEditText, mEmailEditText;
	private SessionManager mSession;
	private CommonLogin mCommonLogin;

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
			Log.d(TAG, String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d(TAG, "Success!");
		}
	};
	private UserInfoChangedCallback userInfoChangedCallback = new UserInfoChangedCallback() {
		@Override
		public void onUserInfoFetched(final GraphUser user) {
			if (user == null) {
				return;
			}
			handleFacebookLogin(user.getId(), user.getName(),
					(String) user.getProperty(Params.PARAM_EMAIL));
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
		Log.v(TAG, "login fb xxxx");

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSession = SessionManager.getInstance(getApplicationContext());
		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_login);

		((ViewGroup) findViewById(R.id.background_login)).setAlpha(200);
		// Init UI
		mLoginButton = (LoginButton) findViewById(R.id.login_FB_button);
		mLoginButton.setReadPermissions(Arrays.asList("public_profile",
				"email", "user_friends"));
		mPasswordEditText = (EditText) findViewById(R.id.login_edittext_password);
		mEmailEditText = (EditText) findViewById(R.id.login_edittext_mail);
		mForgotPassEditText = (EditText) findViewById(R.id.forgot_edittext_mail);
		mPasswordEditText.addTextChangedListener(this);
		mEmailEditText.addTextChangedListener(this);
		mLoginButton.setUserInfoChangedCallback(userInfoChangedCallback);
		mCommonLogin = new CommonLogin(this, this, mSession);

	}

	// @SuppressWarnings("unused")
	// private void getFriendList() {
	// Log.i("Login", "start load friend");
	// Session session = Session.getActiveSession();
	// Request friendRequest = Request.newMyFriendsRequest(session,
	// new GraphUserListCallback() {
	// @Override
	// public void onCompleted(List<GraphUser> users,
	// Response response) {
	// Log.i("INFO", response.toString());
	// if (users != null) {
	// Global.listFriend = new Vector<UserModel>();
	// for (GraphUser item : users) {
	// UserModel user = new UserModel();
	// user.username = item.getId();
	// user.name = item.getName();
	// Global.listFriend.add(user);
	// }
	// }
	// }
	// });
	// Bundle params = new Bundle();
	// params.putString("fields", "id, name, picture");
	// friendRequest.setParameters(params);
	// friendRequest.executeAsync();
	// }

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

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
		}
	}

	private void backClick() {
		if (findViewById(R.id.login_layout).isShown()) {
			Intent intent = new Intent(this, SplashScreen.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter_back,
					R.anim.diagslide_leave_back);
			finish();
		} else if (findViewById(R.id.forgot_password_layout).isShown()) {
			findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.forgot_password_layout).setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		backClick();
		// super.onBackPressed();
	}

	String username;
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

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			backClick();
			break;
		case R.id.login_anonymous:
			login_anonymous();
			break;
		case R.id.login_fail_title:
			// TODO show login fail
			break;

		case R.id.login_email:
			handleEmailLogin();
			break;
		case R.id.forgot_password:
			findViewById(R.id.login_layout).setVisibility(View.GONE);
			findViewById(R.id.forgot_password_layout).setVisibility(
					View.VISIBLE);
			break;

		case R.id.btn_reset_password:
			String email = mForgotPassEditText.getText().toString();
			if (Global.checkInput(email, Global.REGEX_EMAIL)) {
				ResendEmailActiveAPI resend = new ResendEmailActiveAPI(this,
						Params.RESET_PASSWORD);
				RequestParams request = new RequestParams();
				request.put(Params.PARAM_EMAIL, email);
				resend.setParams(request);
				resend.onRunButtonPressed();
			} else {
				ShowMessage.showMessage(mContext,
						getString(R.string.ERR_INPUT_MISTAKE));
				mForgotPassEditText
						.setBackgroundResource(R.drawable.edittext_bg_error);
				mForgotPassEditText.setText("");
			}
			break;

		}
	}

	private void handleEmailLogin() {
		if (!validateEmailPassword()) {
			ShowMessage.showMessage(mContext,
					getString(R.string.ERR_INPUT_MISTAKE));
		} else {
			// trigger login
			RequestParams params = new RequestParams();
			params.put(Params.PARAM_EMAIL, mEmailEditText.getText().toString());
			params.put(Params.PARAM_PASSWORD, mPasswordEditText.getText()
					.toString());
			params.put(Params.PARAM_UDID, Global.getDeviceUDID(this));
			params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
			mCommonLogin.setParams(params);
			mCommonLogin.setLoginType(Global.TYPE_EMAIL);
			mCommonLogin.onRunButtonPressed();
		}
	}

	private boolean validateEmailPassword() {
		boolean check = false;
		if (Global.checkInput(mEmailEditText.getText().toString(),
				Global.REGEX_EMAIL))
			check = true;
		else {
			check = false;
			mEmailEditText.setBackgroundResource(R.drawable.edittext_bg_error);
			mEmailEditText.setText("");
		}
		if (Global.checkInput(mPasswordEditText.getText().toString(),
				Global.REGEX_PASSWORD))
			check = true;
		else {
			check = false;
			mPasswordEditText
					.setBackgroundResource(R.drawable.edittext_bg_error);
			mPasswordEditText.setText("");
		}
		return check;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (count > 0) {
			if (mPasswordEditText.getText().length() > 0)
				mPasswordEditText.setBackgroundResource(R.drawable.edittext_bg);
			if (mEmailEditText.getText().length() > 0)
				mEmailEditText.setBackgroundResource(R.drawable.edittext_bg);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
	private void createDialogEditInput(){
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
							params.put(Params.PARAM_UDID, Global.getDeviceUDID(getApplicationContext()));
							params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
							params.put(Params.PARAM_PLATFORM_VERSION, Build.VERSION.RELEASE);
							params.put(Params.PARAM_PHONE_NAME, Build.MODEL);
							mCommonLogin.setParams(params);
							mCommonLogin.setLoginType(Global.TYPE_ANONYMOUS_REGISTER);
							mCommonLogin.onRunButtonPressed();
							Log.d(TAG, "register anonymous");
							
						}else{
							ShowMessage.showDialog(Login.this, mContext.getString(R.string.ERR_TITLE), mContext.getString(R.string.ERR_INPUT_EMPTY));
						}
					}
				});
		mAlertDialog.show();
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		if (isAnonymous) {
			isAnonymous = false;
			Intent intent = new Intent(this, RegisterAnonymousActivity.class);
			intent.putExtra(Params.START_FROM_LOGIN, true);
			 startActivity(intent);
			 overridePendingTransition(R.anim.diagslide_enter,
			 R.anim.diagslide_leave);
			 finish();
			//createDialogEditInput();
		}
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}

}
