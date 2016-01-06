package com.cls.sugutomo.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.RankActivity;
import com.cls.sugutomo.SplashScreenActivity;
import com.cls.sugutomo.api.SettingAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.chat.ChatBackgroundService.ChatServiceBinder;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.userlist.UserListActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.MyApplication;
import com.cls.sugutomo.viewpagerindicator.NotifyDialogMessFragmentAdapter;
import com.cls.sugutomo.viewpagerindicator.ViewPagerScroller;
import com.loopj.android.http.RequestParams;
import com.rockerhieu.emojicon.EmojiconEditText;

public class MessageDialog extends FragmentActivity {
	private static final String TAG = MessageDialog.class.getSimpleName();
	public static final String UPDATE_MESSENGER = "UPDATE_MESSENGER";
	public static final String MESSENGER = "MESSENGER";
	private boolean mIsBound = false;
	private UserModel user;
	private ChatBackgroundService mService;
	private EmojiconEditText chatEditText;
	private Button loginToAnswear;
	private ChatType mChatType;
	private DatabaseHandler database;
	private long mConversationId;
	private int notifiID;
	private SharedPreferences pref;
	private Editor editor;
	private ImageFetcher imageFetcher;

	private BroadcastReceiver receiver;
	private ArrayList<MessageModel> messengerList = new ArrayList<MessageModel>();
	boolean appInBackGround;
	String type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		List<Fragment> al = getSupportFragmentManager().getFragments();
//		if (al == null) {
//			// code that handles no existing fragments
//		} else {
//
//			for (Fragment frag : al) {
//				getSupportFragmentManager().beginTransaction().remove(frag)
//						.commit();
//			}
//		}
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.msg_dialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification_dialog);
		pref = this.getSharedPreferences(Global.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		editor = pref.edit();
		mChatType = ChatType.getInstance(getApplicationContext());
		user = (UserModel) getIntent().getSerializableExtra(Params.USER);
		notifiID = getIntent().getIntExtra(Params.NOTIFY_ID, -1);
		appInBackGround = getIntent().getBooleanExtra(Params.NOTIFY_STATE_APP,
				false);
		database = DatabaseHandler.getInstance(this);
		if (Global.userInfo != null)
			mConversationId = database.getConversationByUserId(
					user.getUserId(), Global.userInfo.getUserId());

		String messenger = (String) getIntent().getStringExtra(
				Params.PARAM_MESSENGER);
		type = (String) getIntent().getStringExtra(Params.PARAM_NOTIFY_TYPE);
		if (type.equalsIgnoreCase(ChatType.FROM_NOTIFICATION)
				|| type.equalsIgnoreCase(ChatType.NEW_REGISTER)
				|| type.equalsIgnoreCase(ChatType.RANKING)
				|| type.equalsIgnoreCase(ChatType.FAVORITE)) {
//			findViewById(R.id.notification_edittext).setVisibility(View.GONE);
//			findViewById(R.id.notification_send).setVisibility(View.GONE);
			findViewById(R.id.notification_dialog_answear_layout)
					.setVisibility(View.GONE);
//			findViewById(R.id.notification_dialog_btnlayout).setVisibility(
//					View.GONE);
		} else {
			findViewById(R.id.notification_edittext)
					.setVisibility(View.VISIBLE);
			findViewById(R.id.notification_send).setVisibility(View.VISIBLE);
			findViewById(R.id.notification_dialog_answear_layout)
			.setVisibility(View.VISIBLE);
		}
		messengerList.add(new MessageModel(messenger, type));
		TextView txtFromUser = (TextView) findViewById(R.id.notification_from);
		txtFromUser.setText(user.getName());

		loginToAnswear = (Button) findViewById(R.id.notification_dialog_login_to_answear);
		if (Global.userInfo == null) {
			// try get from pref
			String myString = pref.getString(Params.PREF_USER_INFO, "");
			try {
				UserModel user = Global.setUserInfo(new JSONObject(myString),
						null);
				if (user != null) {
					Global.userInfo = user;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (Global.userInfo != null) {
//			Toast.makeText(this, "not null", Toast.LENGTH_SHORT).show();
//			loginToAnswear.setVisibility(View.GONE);
//			findViewById(R.id.notification_dialog_answear_layout)
//					.setVisibility(View.VISIBLE);
//			findViewById(R.id.notification_dialog_btnlayout).setVisibility(
//					View.VISIBLE);
		} else {
//			Toast.makeText(this, " null", Toast.LENGTH_SHORT).show();
			loginToAnswear.setVisibility(View.VISIBLE);
			findViewById(R.id.notification_dialog_answear_layout)
					.setVisibility(View.GONE);
			findViewById(R.id.notification_dialog_btnlayout).setVisibility(
					View.GONE);
		}
		//loginToAnswear.setVisibility(View.GONE);
		chatEditText = (EmojiconEditText) findViewById(R.id.notification_edittext);

		CircularImageView avatar = (CircularImageView) findViewById(R.id.notification_avatar);
		loadAvatar(avatar);
		bindService();

		initUpdateMessenger();
		NotifyDialogMessFragmentAdapter mAdapter = new NotifyDialogMessFragmentAdapter(
				getSupportFragmentManager());
		NotifyDialogMessFragmentAdapter.mCount = messengerList.size();
		NotifyDialogMessFragmentAdapter.messengerList = messengerList;
		final ViewPager mPager = (ViewPager) findViewById(R.id.notifi_dialog_viewpager);
		changePagerScroller(mPager);
		mPager.setAdapter(mAdapter);
		Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				mPager.setCurrentItem(messengerList.size() - 1, true);
			}
		};
		handler.postDelayed(r, 100);
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter(MessageDialog.UPDATE_MESSENGER));
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		Global.IS_OPEN_DIALOG_NOTIFICATION = false;

		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Global.IS_OPEN_DIALOG_NOTIFICATION = false;
	}

	@Override
	public void onBackPressed() {
		Global.IS_OPEN_DIALOG_NOTIFICATION = false;
		super.onBackPressed();
	}

	private void initUpdateMessenger() {
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String messenger = (String) intent
						.getStringExtra(Params.PARAM_MESSENGER);
				String type = (String) intent
						.getStringExtra(Params.PARAM_NOTIFY_TYPE);
				messengerList.add(new MessageModel(messenger, type));
				NotifyDialogMessFragmentAdapter mAdapter = new NotifyDialogMessFragmentAdapter(
						getSupportFragmentManager());
				NotifyDialogMessFragmentAdapter.mCount = messengerList.size();
				NotifyDialogMessFragmentAdapter.messengerList = messengerList;
				final ViewPager mPager = (ViewPager) findViewById(R.id.notifi_dialog_viewpager);
				changePagerScroller(mPager);
				// mPager.setAdapter(mAdapter);
				mPager.setAdapter(mAdapter);
				mPager.setCurrentItem(messengerList.size() - 2);

				Handler handler = new Handler();
				Runnable r = new Runnable() {
					public void run() {
						mPager.setCurrentItem(messengerList.size() - 1, true);
					}
				};
				handler.postDelayed(r, 100);
			}
		};
	}

	private void changePagerScroller(ViewPager viewPager) {
		try {
			Field mScroller = null;
			mScroller = ViewPager.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			ViewPagerScroller scroller = new ViewPagerScroller(
					viewPager.getContext());
			mScroller.set(viewPager, scroller);
		} catch (Exception e) {
			Log.e(TAG, "error of change scroller ", e);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.notifi_go_dismiss:
			Global.IS_OPEN_DIALOG_NOTIFICATION = false;
			finish();
			break;
		case R.id.notifi_go_chat:
			mService.dismissNotification(notifiID);
			Global.IS_OPEN_DIALOG_NOTIFICATION = false;
			gotoChat();
			finish();
			break;
		case R.id.notification_send:
			long timeNowEpoch = System.currentTimeMillis();
			String text = chatEditText.getText().toString();
			if (text == null || text.length() == 0)
				return;

			Global.IS_OPEN_DIALOG_NOTIFICATION = false;
			mService.dismissNotification(notifiID);

			text = mChatType.sendTextJSON(timeNowEpoch, text).toString();
			String to = user.getUserName() + "@" + Global.CHAT_SERVER;
			Log.i(TAG, "Sending text from dialog notification " + text + " to "
					+ to);
			Message msg = new Message(to, Message.Type.chat);
			msg.setBody(text);
			// hiddenKeyboard();
			if (mService.isAuthenticated()) {
				// setConversationId();
				mService.sendMessage(msg);
				MessageModel message = new MessageModel(mConversationId,
						timeNowEpoch, Global.userInfo, text, timeNowEpoch,
						user.getDistance(), false);
				database.addMessage(Global.userInfo.getUserName(),
						user.getUserName(), message, false);// mConversationId
			}

			finish();
			break;
		case R.id.notification_edittext:
			break;
		case R.id.notification_setting:
			showDialogSetting();
			break;
		case R.id.notification_dialog_login_to_answear:
			Global.IS_OPEN_DIALOG_NOTIFICATION = false;
			mService.dismissNotification(notifiID);
			Intent intent = new Intent(this, SplashScreenActivity.class);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
	}

	private void showDialogSetting() {
		boolean[] checkList = new boolean[5];
		checkList[0] = pref.getBoolean(
				Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW, true);
		checkList[1] = pref
				.getBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT, true);
		checkList[2] = pref.getBoolean(
				Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE, true);
		checkList[3] = pref
				.getBoolean(Params.SETTING_BIGVIEW_WHEN_IN_TOP, true);
		;// lop top
		checkList[4] = pref.getBoolean(Params.SETTING_BIGVIEW_NEW_USER, true);
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
			editor.putBoolean(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW,
					isCheck);
			break;
		case 1:// sms support
			editor.putBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT, isCheck);
			break;
		case 2: {// co dua favorite minh
			editor.putBoolean(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE,
					isCheck);
			RequestParams rp = new RequestParams();
			if (!isCheck
					&& !pref.getBoolean(
							Params.SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE,
							true))
				rp.put(Params.NOTIFYCATION_FAVORITE, 0);
			else
				rp.put(Params.NOTIFYCATION_FAVORITE, 1);

			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(MessageDialog.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(MessageDialog.this).getToken());
			SettingAPI api = new SettingAPI(MessageDialog.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		case 3: {// minh lot top user
			editor.putBoolean(Params.SETTING_BIGVIEW_WHEN_IN_TOP, isCheck);
			RequestParams rp = new RequestParams();
			if (!isCheck)
				rp.put(Params.NOTIFYCATION_RANK, 0);
			else
				rp.put(Params.NOTIFYCATION_RANK, 1);

			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(MessageDialog.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(MessageDialog.this).getToken());
			SettingAPI api = new SettingAPI(MessageDialog.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		case 4: {// thong bao khi co user moi{
			editor.putBoolean(Params.SETTING_BIGVIEW_NEW_USER, isCheck);
			RequestParams rp = new RequestParams();
			if (!isCheck
					&& !pref.getBoolean(Params.SETTING_NOTIFYCATION_NEW_USER,
							true))
				rp.put(Params.NOTIFYCATION_NEWUSER, 0);
			else
				rp.put(Params.NOTIFYCATION_NEWUSER, 1);

			rp.put(Params.PARAM_USER_ID_,
					SessionManager.getInstance(MessageDialog.this).getUserId());
			rp.put(Params.PARAM_TOKEN_,
					SessionManager.getInstance(MessageDialog.this).getToken());
			SettingAPI api = new SettingAPI(MessageDialog.this,
					Params.SAVE_SETTING);
			api.setParams(rp);
			api.onRunButtonPressed();
			break;
		}
		default:
			break;
		}
		editor.commit();
		// checkList[0] =
		// pref.getBoolean(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW,true);
		// checkList[1] =
		// pref.getBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT,true);
		// checkList[2] =
		// pref.getBoolean(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE,true);
		// checkList[3] =
		// pref.getBoolean(Params.SETTING_BIGVIEW_WHEN_IN_TOP,true);;//lop top
		// checkList[4] = pref.getBoolean(Params.SETTING_BIGVIEW_NEW_USER,true);
	}

	private void gotoChat() {
		// if(user==null) Log.v("", "messageDialog user  is null");
		// else Log.v("", "messageDialog user  is not null");
		// dang runing app

		if (Global.currentActivity != null && !appInBackGround) {
			Intent resultIntent = null;
			if (type.equalsIgnoreCase(ChatType.RANKING)) {
				resultIntent = new Intent(this, RankActivity.class);
				resultIntent.putExtra(Params.PARAM_TITLE,
						getString(R.string.rank_title));
			} else if (type.equalsIgnoreCase(ChatType.NEW_REGISTER)
					|| type.equalsIgnoreCase(ChatType.FAVORITE)) {
				resultIntent = new Intent(this, ViewProfileActivity.class);
				resultIntent.putExtra(Params.USER, user);
			} else {
				resultIntent = new Intent(this, ChatDetailActivity.class);
				resultIntent.putExtra(Params.USER, user);
			}
			// resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(resultIntent);
			// Toast.makeText(this, "start activites chat luon " ,
			// Toast.LENGTH_LONG).show();
			return;
		}
		// case app not start
		Intent activityA = new Intent(this, UserListActivity.class);
		Intent resultIntent;
		if (type.equalsIgnoreCase(ChatType.RANKING)) {
			resultIntent = new Intent(this, RankActivity.class);
			resultIntent.putExtra(Params.PARAM_TITLE,
					getString(R.string.rank_title));
		} else if (type.equalsIgnoreCase(ChatType.NEW_REGISTER)
				|| type.equalsIgnoreCase(ChatType.FAVORITE)) {
			resultIntent = new Intent(this, ViewProfileActivity.class);
			resultIntent.putExtra(Params.USER, (UserModel) user);
		} else {
			resultIntent = new Intent(this, ChatDetailActivity.class);
			UserModel m = new UserModel();
			Global.cloneObj(m, user);
			resultIntent.putExtra(Params.USER, m);
		}

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		if (Global.userInfo != null) {// app in background
			UserModel myInfo = new UserModel();
			Global.cloneObj(myInfo, Global.userInfo);
			// resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			// resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			resultIntent.putExtra(Params.PARAM_STRAT_FROM_NOTIFICATION, myInfo);
			stackBuilder.addParentStack(UserListActivity.class);

			stackBuilder.addNextIntent(activityA);
			stackBuilder.addNextIntent(resultIntent);
		} else {// app got kill by ....
			stackBuilder.addParentStack(SplashScreenActivity.class);
			activityA = new Intent(this, SplashScreenActivity.class);
			stackBuilder.addNextIntent(activityA);
			// Log.e("", "splash screen");
		}

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
				(int) System.currentTimeMillis(),
				PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			resultPendingIntent.send();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void bindService() {
		if (!mIsBound) {
			Intent intent = new Intent(this, ChatBackgroundService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			Log.d(TAG, "bind service");
		}
	}

	private void unBindService() {
		if (mConnection != null) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindService();
		database.closeDB();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "on service connected");
			mIsBound = true;
			mService = ((ChatServiceBinder) service).getService();
			// get off message and save to databse
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

	};

	private void loadAvatar(final CircularImageView avatar) {
		imageFetcher = MyApplication.getInstance().getImageFetcher();
		// imageFetcher.setImageFadeIn(true);
		imageFetcher.setLoadingImage(R.drawable.loader);
		imageFetcher.loadImage(user.getUserAvatar(), avatar);
		// AsyncTask<Void, Void, Void> loadthumb = new AsyncTask<Void, Void,
		// Void>() {
		// private Bitmap bm = null;
		//
		// @Override
		// protected Void doInBackground(Void... params) {
		// String userAvatar = user.getUserAvatar();
		// if (userAvatar != null && userAvatar.length() > 0)
		// bm = LoadSaveImage.getThumbImage(
		// userAvatar,
		// Params.PARAM_USER_AVATAR + File.separator
		// + user.getUserId(), user.getUserAvatar()
		// .hashCode() + "");
		// return null;
		// }
		//
		// @Override
		// protected void onPostExecute(Void result) {
		// super.onPostExecute(result);
		// if (bm != null)
		// avatar.setImageBitmap(bm);
		// else {
		// Log.e("", "null bitmap");
		// }
		// }
		// };
		// loadthumb.execute();
	}

}
