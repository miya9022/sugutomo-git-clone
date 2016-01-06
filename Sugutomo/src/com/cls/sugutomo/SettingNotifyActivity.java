package com.cls.sugutomo;

import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.cls.sugutomo.adapter.SettingNotifyAdapter;
import com.cls.sugutomo.api.SettingAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.dialog.MessageDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.SettingModelItem;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class SettingNotifyActivity extends BaseTabActivity {
	public static final int MESSENGER_FROM = 0;
	public static final int SETTING_NOTIFYCATION_WHEN_SOMEONE_VIEW_MYPROFILE = 1;
	public static final int RECEIVER_MESSAGE_SUPPORT_ADMIN = 2;//nhan mess support cua admin ko
	public static final int NOTIFY_WHEN_NEW_USER = 3;//thong bao co user moi
	public static final int NOTIFY_WHEN__SOMEONE_FAVORITE_ME = 4;//thong bao khi co dua favorite minh
	SharedPreferences prefs;
	Editor editor;
	Context context;
	SettingNotifyAdapter listAdapter;
	Vector<SettingModelItem> listSetting = new Vector<SettingModelItem>();
	String[] settingName;
	//private Button notifyMailOn, notifyMailOff;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.activity_setting_notify, null, false);
		frameLayout.addView(activityView);
		context = this;
		init();
		final ListView listView = (ListView) findViewById(R.id.setting_listview);
		listAdapter = new SettingNotifyAdapter(SettingNotifyActivity.this,
				R.layout.setting_notify_adapter, listSetting);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position==listSetting.size()-1){
					//show setting bigView(notication)
					showDialogSetting();
				}
			}
		});
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			 title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
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
		settingName = getResources().getStringArray(
				R.array.setting_notify_option);
		for (int i = 0; i < settingName.length; i++) {
			listSetting.add(new SettingModelItem(settingName[i], ""));
		}
	}

	public void onCheckboxClicked(View v) {
		int idx = ((Integer) v.getTag()).intValue();
		CheckBox b = (CheckBox) v;
		if (v.getId() == R.id.setting_chkbox_mid) {
			if (prefs.getInt(Params.SETTING_NOTIFYCATION_MESSENGER,
					Params.SETTING_NOTIFYCATION_MESSENGER_ALL) != Params.SETTING_NOTIFYCATION_MESSENGER_ALL) {
				CheckBox chk2 = (CheckBox) ((View) v.getParent())
						.findViewById(R.id.setting_chkbox_right);
				chk2.setEnabled(true);
//				chk2.setSelected(false);
				chk2.setChecked(false);
				b.setEnabled(false);
				editor.putInt(Params.SETTING_NOTIFYCATION_MESSENGER,
						Params.SETTING_NOTIFYCATION_MESSENGER_ALL);
			}
		} else if (v.getId() == R.id.setting_chkbox_right) {
			if (idx == 0) {
				if (prefs.getInt(Params.SETTING_NOTIFYCATION_MESSENGER,
						Params.SETTING_NOTIFYCATION_MESSENGER_ALL) != Params.SETTING_NOTIFYCATION_MESSENGER_FAVORITE) {
					CheckBox chk1 = (CheckBox) ((View) v.getParent())
							.findViewById(R.id.setting_chkbox_mid);
					chk1.setEnabled(true);
					//chk1.setSelected(false);
					chk1.setChecked(false);
					b.setEnabled(false);
					editor.putInt(Params.SETTING_NOTIFYCATION_MESSENGER,
							Params.SETTING_NOTIFYCATION_MESSENGER_FAVORITE);
				}
			} else {
				 saveValueFromIndex(idx);
			}
		}

		editor.commit();
	}
	private String saveValueFromIndex(int position){
		String key ="";
		switch (position) {
		case SettingNotifyActivity.MESSENGER_FROM:{
			key =Params.SETTING_NOTIFYCATION_MESSENGER;
			boolean saveVal =!prefs.getBoolean(key, true);
			editor.putBoolean(key, saveVal);
			break;
		}
		case SettingNotifyActivity.SETTING_NOTIFYCATION_WHEN_SOMEONE_VIEW_MYPROFILE:{
			key =Params.SETTING_NOTIFYCATION_FOOTPRINT;
			boolean saveVal =!prefs.getBoolean(key, true);
			editor.putBoolean(key, saveVal);
			break;
		}
		case SettingNotifyActivity.RECEIVER_MESSAGE_SUPPORT_ADMIN:{
			key =Params.SETTING_NOTIFYCATION_SMS_SUPPORT;
			boolean saveVal =!prefs.getBoolean(key, true);
			editor.putBoolean(key, saveVal);
			break;
		}
		case SettingNotifyActivity.NOTIFY_WHEN_NEW_USER:{
			key =Params.SETTING_NOTIFYCATION_NEW_USER;
			boolean saveVal =!prefs.getBoolean(key, true);
			editor.putBoolean(key, saveVal);
			RequestParams rp = new RequestParams();
			if(!saveVal && !prefs.getBoolean(Params.SETTING_BIGVIEW_NEW_USER, true))
				rp.put(Params.NOTIFYCATION_NEWUSER, 0);
			else
				rp.put(Params.NOTIFYCATION_NEWUSER, 1);
			
			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(SettingNotifyActivity.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(SettingNotifyActivity.this).getToken());
			SettingAPI api = new SettingAPI(
					SettingNotifyActivity.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		case SettingNotifyActivity.NOTIFY_WHEN__SOMEONE_FAVORITE_ME:
			key =Params.SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE;
			boolean saveVal =!prefs.getBoolean(key, true);
			editor.putBoolean(key, saveVal);
			RequestParams rp = new RequestParams();
			if(!saveVal && !prefs.getBoolean(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE, true))
				rp.put(Params.NOTIFYCATION_FAVORITE, 0);
			else
				rp.put(Params.NOTIFYCATION_FAVORITE, 1);
			
			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(SettingNotifyActivity.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(SettingNotifyActivity.this).getToken());
			SettingAPI api = new SettingAPI(
					SettingNotifyActivity.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		default:
			break;
		}
		return key;
	}
	
	public void onSettingChange(View v) {
		Button b = (Button) v;
		b.setBackgroundResource(R.drawable.button_press);
		b.setTextColor(Color.WHITE);
//		switch (v.getId()) {
//		case R.id.setting_notify_on:
//			// doSetting(idx,true);
//			editor.putBoolean(Params.SETTING_NOTIFYCATION_MAIL, true);
//			notifyMailOff.setBackgroundResource(R.drawable.button_default_bg);
//			notifyMailOff.setTextColor(Color.BLACK);
//			break;
//		case R.id.setting_notify_off:
//			editor.putBoolean(Params.SETTING_NOTIFYCATION_MAIL, false);
//			notifyMailOn.setBackgroundResource(R.drawable.button_default_bg);
//			notifyMailOn.setTextColor(Color.BLACK);
//			break;
//		default:
//			break;
//		}
		editor.commit();

	}
	
	
	private void showDialogSetting() {
		boolean[] checkList = new boolean[5];
		checkList[0] = pref.getBoolean(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW,true);
		checkList[1] = pref.getBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT,true);
		checkList[2] = pref.getBoolean(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE,true);
		checkList[3] = pref.getBoolean(Params.SETTING_BIGVIEW_WHEN_IN_TOP,true);;//lop top
		checkList[4] = pref.getBoolean(Params.SETTING_BIGVIEW_NEW_USER,true);
		// mSelectedItems = new ArrayList<Integer>(); // Where we track the
		// selected items
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Set the dialog title
		builder.setTitle(R.string.notification_dialog_setting_title)
				// Specify the list array, the items to be selected by default
				// (null for none),
				// and the listener through which to receive callbacks when
				// items are selected
				.setMultiChoiceItems(R.array.dialog_notification_setting,
						checkList,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								if (isChecked) {
									// If the user checked the item, add it to
									// the selected items
									saveSetting(which, true);
								} else {
									// Else, if the item is already in the
									// array, remove it
									saveSetting(which, false);
								}
							}
						})

				.setNegativeButton(R.string.btn_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		builder.create();
		builder.show();
	}
	private void saveSetting(int idx, boolean isCheck) {
		switch (idx) {
		case 0:// new mess
			editor.putBoolean(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW, isCheck);
			break;
		case 1:// sms support
			editor.putBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT,isCheck);
			break;
		case 2:{// co dua favorite minh
			editor.putBoolean(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE,isCheck);
			RequestParams rp = new RequestParams();
			if(!isCheck && !pref.getBoolean(Params.SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE, true))
				rp.put(Params.NOTIFYCATION_FAVORITE, 0);
			else
				rp.put(Params.NOTIFYCATION_FAVORITE, 1);
			
			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(SettingNotifyActivity.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(SettingNotifyActivity.this).getToken());
			SettingAPI api = new SettingAPI(
					SettingNotifyActivity.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		case 3:{// minh lot top user
			editor.putBoolean(Params.SETTING_BIGVIEW_WHEN_IN_TOP,isCheck);
			RequestParams rp = new RequestParams();
			if(!isCheck)
				rp.put(Params.NOTIFYCATION_RANK, 0);
			else
				rp.put(Params.NOTIFYCATION_RANK, 1);
			
			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(SettingNotifyActivity.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(SettingNotifyActivity.this).getToken());
			SettingAPI api = new SettingAPI(
					SettingNotifyActivity.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		case 4:{// thong bao khi co user moi{
			editor.putBoolean(Params.SETTING_BIGVIEW_NEW_USER, isCheck);
			RequestParams rp = new RequestParams();
			if(!isCheck && !pref.getBoolean(Params.SETTING_NOTIFYCATION_NEW_USER, true))
				rp.put(Params.NOTIFYCATION_NEWUSER, 0);
			else
				rp.put(Params.NOTIFYCATION_NEWUSER, 1);
			
			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(SettingNotifyActivity.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(SettingNotifyActivity.this).getToken());
			SettingAPI api = new SettingAPI(
					SettingNotifyActivity.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		default:
			break;
		}
		editor.commit();
	}


}
