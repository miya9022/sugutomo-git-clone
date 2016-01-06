package com.cls.sugutomo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cls.sugutomo.api.ResendEmailActiveAPI;
import com.cls.sugutomo.apiclient.Params;
import com.loopj.android.http.RequestParams;

public class WaitingAcceptAccountActivity extends Activity {

	TextView textEmail;
	private String email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waiting_accept_account);
		textEmail = (TextView) findViewById(R.id.text_send_mail);
		Bundle bundle = getIntent().getExtras();
		email = bundle.getString(Params.PARAM_EMAIL);
		textEmail.setText(email + getString(R.string.text_send_mail));
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btn_resend_mail) {
			ResendEmailActiveAPI resend = new ResendEmailActiveAPI(this, Params.RESEND_EMAIL_ACTIVE);
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_EMAIL, email);
			resend.setParams(request);
			resend.onRunButtonPressed();
		}
	}
}
