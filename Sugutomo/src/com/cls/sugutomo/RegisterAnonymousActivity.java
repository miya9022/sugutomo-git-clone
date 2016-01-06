package com.cls.sugutomo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class RegisterAnonymousActivity extends Activity implements TextWatcher,
		APICallbackInterface {

	private static final String TAG = RegisterAnonymousActivity.class
			.getSimpleName();
	private EditText mUsername;
	private CheckBox mCheck18;

	private SessionManager mSession;
	private CommonLogin mCommonLogin;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_anonymous);
		mSession = SessionManager.getInstance(getApplicationContext());
		mCommonLogin = new CommonLogin(this, this, mSession);

		// UI
		mUsername = (EditText) findViewById(R.id.register_edittext_username);
		mCheck18 = (CheckBox) findViewById(R.id.register_checkbox);

		// listener
		mUsername.addTextChangedListener(this);

	}





	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCommonLogin.onDestroyManual();
	}

	private void backClick() {
		// if (findViewById(R.id.register_btn_layout).isShown()) {
		Intent intent;
		Bundle b = getIntent().getExtras();
		if(b.getBoolean(Params.START_FROM_LOGIN))
		 intent = new Intent(this, Login.class);
		else 
			intent = new Intent(this, TypeRegisterActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		overridePendingTransition(R.anim.diagslide_enter_back,
				R.anim.diagslide_leave_back);
		finish();
		// }
		// else if (findViewById(R.id.register_mail_layout).isShown()) {
		// findViewById(R.id.register_btn_layout).setVisibility(View.VISIBLE);
		// findViewById(R.id.register_mail_layout).setVisibility(View.GONE);
		// }
	}

	@Override
	public void onBackPressed() {
		backClick();
		// super.onBackPressed();
	}


	private void registerEmail() {
		if (mCheck18.isChecked()) {
				if (mUsername.getText().length() > 0) {
					RequestParams params = new RequestParams();
					params.put(Params.PARAM_NAME, mUsername.getText());
					params.put(Params.PARAM_UDID,
							Global.getDeviceUDID(getApplicationContext()));
					params.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
					params.put(Params.PARAM_PLATFORM_VERSION,
							Build.VERSION.RELEASE);
					params.put(Params.PARAM_PHONE_NAME, Build.MODEL);
					mCommonLogin.setParams(params);
					mCommonLogin.setLoginType(Global.TYPE_ANONYMOUS_REGISTER);
					mCommonLogin.onRunButtonPressed();
				} else {
					ShowMessage.showDialog(this,
							this.getString(R.string.ERR_TITLE),
							this.getString(R.string.ERR_INPUT_EMPTY));
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
