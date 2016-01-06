package com.cls.sugutomo;

import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cls.sugutomo.adapter.SettingAdapter;
import com.cls.sugutomo.api.SettingAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.SettingModelItem;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class SettingActivity extends BaseTabActivity {
	private static final int GPS_RESQUEST = 1;
	SharedPreferences prefs;
	Editor editor;
	Context context;
	SettingAdapter listAdapter;
	Vector<SettingModelItem> listSetting = new Vector<SettingModelItem>();
	String[] settingName;
	private SessionManager mSession;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.activity_setting,
				null, false);
		frameLayout.addView(activityView);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			 title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		context = this;
		mSession = SessionManager.getInstance(getApplicationContext());
		init();
		final ListView listView = (ListView) findViewById(R.id.setting_listview);
		listAdapter = new SettingAdapter(SettingActivity.this,
				R.layout.setting_adapter, listSetting);
		listView.setAdapter(listAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO view profile
				switch (position) {
				case SettingActivity.SHOW_MY_LOCATION:
					final String[] options = getResources().getStringArray(
							R.array.setting_show_my_location_txt);
					final String[] optionsVal = getResources().getStringArray(
							R.array.setting_show_my_location_value);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SettingActivity.this);
					builder.setTitle(settingName[position]);
					builder.setNegativeButton(
							context.getString(R.string.btn_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Canceled.
								}
							});
					builder.setSingleChoiceItems(options, -1,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO
									editor.putInt(Params.SETTING_MY_LOCATION,
											Integer.parseInt(optionsVal[which]));
									editor.commit();
									listAdapter.notifyDataSetChanged();// update
																		// list
									dialog.dismiss();
									Global.updateLocation( SettingActivity.this);
								}
							});
					builder.show();
					break;
				case SettingActivity.MAIL_CHANGE:
					if (Global.TYPE_EMAIL == prefs.getInt(
							Params.PARAM_TYPE_LOGIN, -1))
						showDialog(settingName[position], 0);
					else
						ShowMessage.showDialog(context, settingName[position],
								getString(R.string.fb_cannot_change_mail));
					break;
				case SettingActivity.PASSWORD_CHANGE:
					if (Global.TYPE_EMAIL == prefs.getInt(
							Params.PARAM_TYPE_LOGIN, -1))
						showDialog(settingName[position], 1);
					else
						ShowMessage.showDialog(context, settingName[position],
								getString(R.string.fb_cannot_change_pass));
					break;
				case SettingActivity.NOTIFY_SETTING:
					// start new ACTIVITI
					Intent intent = new Intent(SettingActivity.this,
							SettingNotifyActivity.class);
					// send object UserModel
					intent.putExtra(Params.PARAM_TITLE, getString(R.string.setting_more_title));
					startActivity(intent);
					overridePendingTransition(R.anim.diagslide_enter,
							R.anim.diagslide_leave);
					// ShowMessage.showDialog(context,
					// "show notify setting",
					// "show notify setting");
					break;
				case SettingActivity.ACCOUNT_DELETE:
					if (Global.TYPE_EMAIL == prefs.getInt(
							Params.PARAM_TYPE_LOGIN, -1))
						showDialog(settingName[position], 2);
					else if (Global.TYPE_ANONYMOUS == prefs.getInt(
							Params.PARAM_TYPE_LOGIN, -1))
						ShowMessage.showDialog(context, settingName[position],
								getString(R.string.anynomous_cannot_dell_acc));
					else
						ShowMessage.showDialog(context, settingName[position],
								getString(R.string.fb_cannot_dell_acc));
					break;
				default:
					break;
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}
	public void init() {
		prefs = this.getSharedPreferences(Global.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		editor = prefs.edit();

		settingName = getResources().getStringArray(R.array.setting_option);
		for (int i = 0; i < settingName.length; i++) {
			listSetting.add(new SettingModelItem(settingName[i], ""));
		}
	}

	public static final int GPS = 0;
	public static final int SHOW_MY_LOCATION = 1;
	public static final int SOUND = 2;
	public static final int VIBE = 3;
	public static final int MAIL_CHANGE = 4;
	public static final int PASSWORD_CHANGE = 5;
	public static final int AUTO_LOGIN = 6;
	public static final int NOTIFY_SETTING = 7;
	public static final int ACCOUNT_DELETE = 8;

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != -1) {
			LocationManager locationManager;
			switch (requestCode) {
			case GPS_RESQUEST:
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

				if (locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					Toast.makeText(this, getString(R.string.mess_connected_location),
							Toast.LENGTH_SHORT).show();
					editor.putBoolean(Params.SETTING_GPS, true);
				} else {
					Toast.makeText(context, getString(R.string.mess_disconnected_location),
							Toast.LENGTH_LONG).show();
					editor.putBoolean(Params.SETTING_GPS, false);
				}
				editor.commit();
				listAdapter.notifyDataSetChanged();// refresh button background
													// if not turnon/off when
													// shoe default setting
													// android

				break;
			}
		}
	}

	public void onSettingChange(View v) {
		int idx = ((Integer) v.getTag()).intValue();
		Button b = (Button) v;
		b.setBackgroundResource(R.drawable.button_press);
		b.setTextColor(Color.WHITE);
		Button btn2 = null;
		switch (v.getId()) {
		case R.id.setting_on:
			// saveValue(idx,true);
			doSetting(idx, true);
			btn2 = (Button) ((View) v.getParent())
					.findViewById(R.id.setting_off);
			btn2.setBackgroundResource(R.drawable.button_default_bg);
			btn2.setTextColor(Color.BLACK);
			break;
		case R.id.setting_off:
			// saveValue(idx,false);
			doSetting(idx, false);
			btn2 = (Button) ((View) v.getParent())
					.findViewById(R.id.setting_on);
			btn2.setBackgroundResource(R.drawable.button_default_bg);
			btn2.setTextColor(Color.BLACK);

			break;
		default:
			break;
		}

	}

	@SuppressWarnings("deprecation")
	private void doSetting(int index, boolean enable) {
		switch (index) {
		case SettingActivity.GPS:
			boolean gps = prefs.getBoolean(Params.SETTING_GPS, false);
			if (gps == enable)
				return;
			startActivityForResult(new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
					GPS_RESQUEST);
			break;
		case SettingActivity.SOUND:
			if (enable) {
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audioManager
						.setStreamVolume(
								AudioManager.STREAM_NOTIFICATION,
								audioManager
										.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
								0);
			} else {
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
						0, 0);

			}
			editor.putBoolean(Params.SETTING_SOUND, enable);
			// get value settingsound and put noti.defaults |=
			// Notification.DEFAULT_SOUND; if sound enable
			// startActivity(new
			// Intent(android.provider.Settings.ACTION_SOUND_SETTINGS));
			break;
		case SettingActivity.VIBE:
			editor.putBoolean(Params.SETTING_VIBE, enable);
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (enable)
				audioManager.setStreamVolume(AudioManager.STREAM_RING,
						audioManager
								.getStreamMaxVolume(AudioManager.STREAM_RING),
						0);
			else
				audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
			break;
		case SettingActivity.AUTO_LOGIN:
//			Toast.makeText(context, "demo pass requice login " + enable,
//					Toast.LENGTH_LONG).show();
			mSession.setAutoLogin(!enable);
			mSession.saveRequirePassword(enable);
			break;
		default:
			break;
		}
		editor.commit();
	}

	private void showDialog(String title, final int request) {
		final AlertDialog alert = new AlertDialog.Builder(context)
				.setPositiveButton(android.R.string.ok, null)
				// Set to null. We
				// override the
				// onclick
				.setTitle(title)
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							};
						}).create();
		alert.setView(getLayoutInflater().inflate(R.layout.setting_changepass,
				null));

		alert.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Do something
						// Do something with value!
						if (request == 0) {
							// change mail
							EditText newMail = (EditText) alert
									.findViewById(R.id.setting_current_pass);
							EditText pass = (EditText) alert
									.findViewById(R.id.setting_new_pass);
							if (validateEmailAndPassword(newMail, pass)) {
								RequestParams request = new RequestParams();
								request.put(Params.PARAM_USER_ID_,
										mSession.getUserId());
								request.put(Params.PARAM_TOKEN_,
										mSession.getToken());
								request.put(Params.PARAM_EMAIL, newMail
										.getText().toString());
								SettingAPI api = new SettingAPI(
										SettingActivity.this,
										Params.SAVE_SETTING);
								api.setParams(request);
								api.onRunButtonPressed();
								alert.dismiss();
							}

						} else if (request == 1) {// change pass
							EditText oldPass = (EditText) alert
									.findViewById(R.id.setting_current_pass);
							EditText newPass = (EditText) alert
									.findViewById(R.id.setting_new_pass);
							EditText repeatPass = (EditText) alert
									.findViewById(R.id.setting_repeat_new_pass);
							if (oldPass.length() > 0 && newPass.length() > 0
									&& repeatPass.length() > 0) {
								if (validatePassword(oldPass, newPass)) {
									if (!newPass
											.getText()
											.toString()
											.equals(repeatPass.getText()
													.toString())) {
										ShowMessage
												.showDialog(context, getString(R.string.ERR_TITLE),
														getString(R.string.password_not_match));
									} else {
										RequestParams request = new RequestParams();
										request.put(Params.PARAM_USER_ID_,
												mSession.getUserId());
										request.put(Params.PARAM_TOKEN_,
												mSession.getToken());
										request.put(
												Params.PARAM_CURRENT_PASSWORD,
												oldPass.getText().toString());
										request.put(Params.PARAM_NEW_PASSWORD,
												newPass.getText().toString());
										SettingAPI apiChangePass = new SettingAPI(
												SettingActivity.this,
												Params.CHANGE_PASS);
										apiChangePass.setParams(request);
										apiChangePass.onRunButtonPressed();
										alert.dismiss();
									}
								} else {
									ShowMessage
											.showDialog(context, getString(R.string.ERR_TITLE),
													getString(R.string.password_format_error));
								}
							} else {
								ShowMessage.showDialog(context, getString(R.string.ERR_TITLE),
										getString(R.string.password_input_all));
							}
						} else if (request == 2) {
							// delete acc
							EditText pass = (EditText) alert
									.findViewById(R.id.setting_current_pass);
							if (!Global.checkInput(pass.getText().toString(),
									Global.REGEX_PASSWORD)) {
								pass.setBackgroundResource(R.drawable.edittext_bg_error);
								pass.setText("");
							} else {
								RequestParams request = new RequestParams();
								request.put(Params.PARAM_USER_ID_,
										mSession.getUserId());
								request.put(Params.PARAM_TOKEN_,
										mSession.getToken());
								SettingAPI apiChangePass = new SettingAPI(
										SettingActivity.this, Params.DELETE_ACC);
								apiChangePass.setParams(request);
								apiChangePass.onRunButtonPressed();
								alert.dismiss();
							}
						}
					}
				});
			}
		});
		alert.show();
		if (request == 0) {// change mail
			EditText newMail = (EditText) alert
					.findViewById(R.id.setting_current_pass);
			newMail.setHint(context.getString(R.string.setting_new_mail));
			newMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			newMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			EditText pass = (EditText) alert
					.findViewById(R.id.setting_new_pass);
			pass.setHint(context.getString(R.string.setting_current_pass));
			alert.findViewById(R.id.setting_repeat_new_pass).setVisibility(
					View.GONE);
		} else if (request == 1) {// change pass

		} else if (request == 2) {
			// delete account
			alert.findViewById(R.id.setting_new_pass).setVisibility(View.GONE);
			alert.findViewById(R.id.setting_repeat_new_pass).setVisibility(
					View.GONE);
		}
	}

	private boolean validateEmailAndPassword(EditText mEmailEdiText,
			EditText mNewPassEdiText) {
		boolean check = false;
		if (Global.checkInput(mEmailEdiText.getText().toString(),
				Global.REGEX_EMAIL))
			check = true;
		else {
			check = false;
			mEmailEdiText.setBackgroundResource(R.drawable.edittext_bg_error);
			mEmailEdiText.setText("");
		}
		if (Global.checkInput(mNewPassEdiText.getText().toString(),
				Global.REGEX_PASSWORD))
			check = true;
		else {
			check = false;
			mNewPassEdiText.setBackgroundResource(R.drawable.edittext_bg_error);
			mNewPassEdiText.setText("");
		}
		return check;
	}

	private boolean validatePassword(EditText mCurrentPassEdiText,
			EditText mNewPassEdiText) {
		boolean check = false;
		if (Global.checkInput(mCurrentPassEdiText.getText().toString(),
				Global.REGEX_PASSWORD))
			check = true;
		else {
			check = false;
			mCurrentPassEdiText
					.setBackgroundResource(R.drawable.edittext_bg_error);
			mCurrentPassEdiText.setText("");
		}
		if (Global.checkInput(mNewPassEdiText.getText().toString(),
				Global.REGEX_PASSWORD))
			check = true;
		else {
			check = false;
			mNewPassEdiText.setBackgroundResource(R.drawable.edittext_bg_error);
			mNewPassEdiText.setText("");
		}
		return check;
	}

}
