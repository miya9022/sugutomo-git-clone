package com.cls.sugutomo;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.api.RegisterEmailAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.loopj.android.http.RequestParams;

public class RegisterActivity extends Activity implements TextWatcher, APICallbackInterface {

	private static final String TAG = RegisterActivity.class.getSimpleName();
	private EditText mUsername, mPassword, mEmail;
	private CheckBox mCheck18;

	private UiLifecycleHelper uiHelper;
	private SessionManager mSession;
	private CommonLogin mCommonLogin;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			if (session.isOpened()){
				Log.v("", " fb callback");
				onSessionStateChange(session, state, exception);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		mSession = SessionManager.getInstance(getApplicationContext());
		mCommonLogin = new CommonLogin(this,this, mSession);
        
		// UI
		mUsername = (EditText) findViewById(R.id.register_edittext_username);
		mPassword = (EditText) findViewById(R.id.register_edittext_password);
		mEmail = (EditText) findViewById(R.id.register_edittext_mail);
		mCheck18 = (CheckBox) findViewById(R.id.register_checkbox);

		// listener
		mUsername.addTextChangedListener(this);
		mPassword.addTextChangedListener(this);
		mEmail.addTextChangedListener(this);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	private void registerFacebook(String facebook_id, String name, String email) {
		RequestParams params = new RequestParams();
		params.put(Params.PARAM_FB, facebook_id);
		params.put(Params.PARAM_EMAIL, email);
		params.put(Params.PARAM_NAME, name);
		params.put(Params.PARAM_UDID, Global.getDeviceUDID(getApplicationContext()));
		params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
		params.put(Params.PARAM_PLATFORM_VERSION, Build.VERSION.RELEASE);
		params.put(Params.PARAM_PHONE_NAME, Build.MODEL);
		mCommonLogin.setParams(params);
		mCommonLogin.setLoginType(Global.TYPE_FACEBOOK);
		mCommonLogin.onRunButtonPressed();
		Log.d(TAG, "register facebook");
	}

	private void makeMeRequest(final Session session) {
		// Make an API call to get user data and define a
		// new callback to handle the response.
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(final GraphUser user,
							Response response) {
						// If the response is successful
						if (session == Session.getActiveSession()
								|| session.isOpened()) {
							if (user != null) {
								// TODO
								Log.v(TAG, "fbid=" + user.getId() + "\temail="
										+ (String) user.getProperty("email"));
								if (user.getId() == "" || user.getId() == null) {
									ShowMessage.showMessage(
											getApplicationContext(),
											ShowMessage.FB_API_ERR_LOGIN);
									Log.v(TAG, "error login email or id");
									return;
								} else {
									registerFacebook(user.getId(), user
											.getName(), (String) user
											.getProperty(Params.PARAM_EMAIL));
									// log out facebook
									if (session != null)
										session.closeAndClearTokenInformation();
								}
							}
						}
						if (response.getError() != null) {
							// Handle errors, will do so later.
							ShowMessage.showMessage(getApplicationContext(),
									ShowMessage.FB_API_ERR_LOGIN);
							Log.v(TAG, "error login");
							return;
						}
					}
				});
		request.executeAsync();
	}

	@SuppressWarnings("unused")
	private void addNewUser(String userid, String email, String name) {
		DatabaseHandler db = DatabaseHandler.getInstance(this);
		// create new user
		UserModel newUser = new UserModel();
		newUser.setUserName(userid);
		newUser.setName(name);
		newUser.setEmail(email);
		newUser.setActived(true);
		newUser.setWallStatus("online");
		// add to db
		db.createAccount(newUser);
		List<UserModel> userlist = db.getAllAccountUser();
		for (UserModel cn : userlist) {
			String log = "Id: " + cn.getUserId() + " ,FB_ID: " + cn.getUserName()
					+ " ,Name: " + cn.getName() + " ,Email: "
					+ cn.getEmail() + " ,Actived " + cn.isActived();
			// Writing Contacts to log
			Log.d("Name: ", log);
		}
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
	    Log.d(TAG, "onSessionState Changed");
		if (session != null && state.isOpened()) {
			Log.v("", " fb onSessionState");
			makeMeRequest(session);
		}
		if (state.isClosed()) {
			if (session != null) {
				session.closeAndClearTokenInformation();
				Session.setActiveSession(null);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			//Log.v("", " fb onResume");
			//onSessionStateChange(session, session.getState(), null);
		}
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		mCommonLogin.onDestroyManual();
	}

	private void backClick() {
//		if (findViewById(R.id.register_btn_layout).isShown()) {
			Intent intent = new Intent(this, TypeRegisterActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter_back,
					R.anim.diagslide_leave_back);
			finish();
//		} 
//		else if (findViewById(R.id.register_mail_layout).isShown()) {
//			findViewById(R.id.register_btn_layout).setVisibility(View.VISIBLE);
//			findViewById(R.id.register_mail_layout).setVisibility(View.GONE);
//		}
	}

	@Override
	public void onBackPressed() {
		backClick();
		// super.onBackPressed();
	}

	private boolean checkRequireRegister() {
		boolean check = false;
		// TODO Username -> name
//		if (Global.checkInput(mUsername.getText().toString(),
//				Global.REGEX_USERNAME))
//			check = true;
//		else {
//			check = false;
//			mUsername.setBackgroundResource(R.drawable.edittext_bg_error);
//			mUsername.setText("");
//		}
		if (Global.checkInput(mEmail.getText().toString(), Global.REGEX_EMAIL))
			check = true;
		else {
			check = false;
			mEmail.setBackgroundResource(R.drawable.edittext_bg_error);
			mEmail.setText("");
		}
		if (Global.checkInput(mPassword.getText().toString(),
				Global.REGEX_PASSWORD))
			check = true;
		else {
			check = false;
			mPassword.setBackgroundResource(R.drawable.edittext_bg_error);
			mPassword.setText("");
		}
		return check;
	}

	private void registerEmail() {
		if (mCheck18.isChecked()) {

			if (checkRequireRegister() == false) {
				ShowMessage.showMessage(getApplicationContext(),
						getString(R.string.ERR_INPUT_MISTAKE));
			} else {
				RegisterEmailAPI reg = new RegisterEmailAPI(this, mSession);
				RequestParams params = new RequestParams();
				params.put(Params.PARAM_EMAIL, mEmail.getText().toString());
				params.put(Params.PARAM_NAME, mUsername.getText()
						.toString());
				params.put(Params.PARAM_PASSWORD, mPassword.getText()
						.toString());
				params.put(Params.PARAM_UDID, Global.getDeviceUDID(getApplicationContext()));
				params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
				params.put(Params.PARAM_PLATFORM_VERSION, Build.VERSION.RELEASE);
				params.put(Params.PARAM_PHONE_NAME, Build.MODEL);
				params.put(Params.PARAM_PASSWORD, mPassword.getText()
						.toString());
				reg.setParams(params);
				reg.onRunButtonPressed();
			}
		} else {
			ShowMessage.showMessage(getApplicationContext(),
					getString(R.string.register_checkbox_error));
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			backClick();
			break;

		case R.id.btn_fb_register:
			// Check for an open session
			Session session = Session.getActiveSession();
			if (session != null && session.getState().isOpened()) {
				// Get the user's data
				Log.v("", " fb btn_fb_register");
				makeMeRequest(session);
			}
			break;

		case R.id.btn_mail_register:
			findViewById(R.id.register_btn_layout).setVisibility(View.GONE);
			findViewById(R.id.register_mail_layout).setVisibility(View.VISIBLE);
			break;

		case R.id.register_btn_signup:
			registerEmail();
			break;
		case R.id.txt_policy: {
			Intent intent = new Intent(this, InformationActivity.class);
			intent.putExtra(Params.PARAM_TITLE,
					getString(R.string.privace_policy));
			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
			intent.putExtra(Params.REQUEST, Params.POLICY);
			intent.putExtra(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
		}
			break;
		case R.id.txt_term_of_user: {
			Intent intent = new Intent(this, InformationActivity.class);
			intent.putExtra(Params.PARAM_TITLE,
					getString(R.string.term_of_user));
			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
			intent.putExtra(Params.REQUEST, Params.TERM);
			intent.putExtra(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
		}
			break;
		}
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (count > 0) {
			if (mUsername.getText().length() > 0)
				mUsername.setBackgroundResource(R.drawable.edittext_bg);
			if (mPassword.getText().length() > 0)
				mPassword.setBackgroundResource(R.drawable.edittext_bg);
			if (mEmail.getText().length() > 0)
				mEmail.setBackgroundResource(R.drawable.edittext_bg);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub
		
	}
}
