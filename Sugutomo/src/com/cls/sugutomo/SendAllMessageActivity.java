package com.cls.sugutomo;

import java.util.Vector;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cls.sugutomo.adapter.BroadcastAPI;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.chat.ChatBackgroundService.ChatServiceBinder;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.BroadcastModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class SendAllMessageActivity extends BaseTabActivity implements
		APICallbackInterface {
	private static final int SEX_MEN = 1;
	private static final int SEX_WOMEN = 2;
	private static final int SEX_BOTH = 3;
	private static final String TAG = SendAllMessageActivity.class
			.getSimpleName();
	// private AnimationDrawable broastAnimation;
	private Button[] btnSexList = new Button[3];

	private TextView mPointCost, mTextPeople, mTextMessage;
	private DataHolder data = null;
	private ChatType mChatType;
	private boolean mIsBound = false;
	private ChatBackgroundService mService;
	private String title;
	private SessionManager mSession;
	private boolean sendError=false;
	private AlertDialog dialog;
	// data
	private Vector<BroadcastModel> mListBroadcast = new Vector<BroadcastModel>();
	private Vector<String> mMessages = new Vector<String>();
	private long timeNowEpoch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// add layout of activity to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.send_all_message_activity, null, false);
		frameLayout.addView(activityView);
		mChatType = ChatType.getInstance(getApplicationContext());
		// set title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		bindService();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
		mSession = SessionManager.getInstance(this);
		init();
		loadData();

		Global.sendAllMessageActivity = this;
	}

	private void loadData() {
		mListBroadcast = new Vector<BroadcastModel>();
		mMessages = new Vector<String>();
		BroadcastAPI broadcast = new BroadcastAPI(this, this, mListBroadcast,
				mMessages);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		broadcast.setParams(request);
		broadcast.onRunButtonPressed();
	}

	public void init() {

		mPointCost = (TextView) findViewById(R.id.text_point);
		mTextPeople = (TextView) findViewById(R.id.text_people);
		mTextMessage = (TextView) findViewById(R.id.text_message);

		btnSexList[0] = (Button) findViewById(R.id.btn_broastcast_man);
		btnSexList[1] = (Button) findViewById(R.id.btn_broastcast_woman);
		btnSexList[2] = (Button) findViewById(R.id.btn_broastcast_bolth);

		// data = new DataHolder(SendAllMessageActivity.SEX_MEN, mListBroadcast
		// .get(0).getPeople(), mMessages.get(0), mListBroadcast.get(0)
		// .getPoint());
		// mPointCost.setText(data.point + "pt");
		// mTextPeople.setText(data.totalPeople +
		// getString(R.string.broastcast_people));
		// mTextMessage.setText(data.messenger);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text_message:
			showListMessenger();
			break;

		case R.id.text_people:
			showListTotalPeople();
			break;
		case R.id.custom_dialog_cancel:
			if (dialog != null){
				dialog.dismiss();
				sendError=false;
			}
			break;
		case R.id.custom_dialog_ok:
			dialog.dismiss();
			if (sendError) {
               // go to buy point
				Intent intent = new Intent(this, BuyPointActivity.class);
				intent.putExtra(Params.PARAM_TITLE, getString(R.string.buy_point_title));
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			} else {
				sendBroadcastToServer();
			}
			break;

		}

	}

	public void receiveMessage(String messageBody, boolean checkMessage) {
		JSONObject data = mChatType.getData(messageBody);
		if (data != null) {
			Log.i(TAG, "receive message: " + data.toString());
			long id = mChatType.getId(data);
			Log.i(TAG, "id = " + id + " timeNowSend = " + timeNowEpoch);
			if (id == timeNowEpoch) {
				String message = mChatType.executeError(messageBody);
				showAlert(message, checkMessage);
			}
		}
	}

	private void showAlert(final String message, final boolean checkMessage) {
		runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SendAllMessageActivity.this);
				View view ;
				if (!checkMessage) {
					sendError=true;
					view = LayoutInflater.from(SendAllMessageActivity.this)
							.inflate(R.layout.custom_dialog, null);
					((TextView) view.findViewById(R.id.custom_dialog_title))
							.setText(R.string.point_need_more_question_buypoint);
					builder.setView(view);
					dialog = builder.create();
					dialog.show();
					// ShowMessage.showDialog(SendAllMessageActivity.this,
					// getString(R.string.ERR_TITLE), message);
				} else {
					view = LayoutInflater.from(SendAllMessageActivity.this)
							.inflate(R.layout.custom_dialog_ok, null);
					((TextView) view.findViewById(R.id.custom_dialog_title))
							.setText(R.string.send_broastcast_success);
					builder.setView(view);
					dialog = builder.create();
					//dialog.setCanceledOnTouchOutside(false);
					dialog.show();
					// ShowMessage.showMessage(getApplicationContext(),
					// "Send success");
				}
			}
		});
	}

	public void onChangeSex(View v) {
		if (data == null) {
			ShowMessage.showDialog(this, this.getString(R.string.ERR_TITLE),
					this.getString(R.string.ERR_CONNECT_FAIL));
			return;
		}
		// data.sex = sexID;
		for (int i = 0; i < btnSexList.length; i++) {
			btnSexList[i].setBackgroundResource(R.drawable.button_press_change);
			btnSexList[i].setTextColor(Color.BLACK);
		}
		v.setBackgroundResource(R.drawable.button_press);
		((Button) v).setTextColor(Color.WHITE);
		switch (v.getId()) {
		case R.id.btn_broastcast_man:
			data.sex = SEX_MEN;
			break;
		case R.id.btn_broastcast_woman:
			data.sex = SEX_WOMEN;
			break;
		case R.id.btn_broastcast_bolth:
			data.sex = SEX_BOTH;
			break;
		default:
			break;
		}
	}

	public void onBroastCast(View v) {
		if (data == null) {
			loadData();
			return;
		}
		sendError=false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle(title);
		// String messs = String.format(getString(R.string.send_broastcast),
		// data.totalPeople,data.messenger,data.point);
		// builder.setMessage( messs );
		// builder.setNegativeButton(getString(R.string.btn_cancel),
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// // Canceled.
		// }
		// });
		// builder.setPositiveButton(getString(R.string.btn_ok),
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// sendBroadcastToServer();
		// }
		// });
		View view = LayoutInflater.from(this).inflate(R.layout.custom_dialog,
				null);
		builder.setView(view);
		// builder.show();
		dialog = builder.create();
		dialog.show();
	}

	private void sendBroadcastToServer() {
		timeNowEpoch = System.currentTimeMillis();
		String text = data.messenger;
		if (text == null || text.length() == 0)
			return;
		text = mChatType.sendBroadCast(timeNowEpoch, text, data.totalPeople,
				data.sex).toString();
		String to = Global.ADMIN + "@" + Global.CHAT_SERVER;
		Log.i(TAG, "Sending broadcast " + text + " to " + to);
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);

		if (mService.isAuthenticated()) {
			mService.sendMessage(msg);
		}
	}

	public void showListTotalPeople() {
		if (data == null) {
			loadData();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String[] array = new String[mListBroadcast.size()];
		for (int i = 0; i < mListBroadcast.size(); i++) {
			array[i] = mListBroadcast.get(i).getPeople()
					+ getString(R.string.broastcast_people);
		}
		builder.setTitle(R.string.broadcast_totalsend_dialog_title)
				.setItems(array, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mTextPeople.setText(array[which]);
						data.totalPeople = mListBroadcast.get(which)
								.getPeople();
						data.point = mListBroadcast.get(which).getPoint();
						mPointCost.setText(data.point + "pt");
					}
				}).create().show();
	}

	public void showListMessenger() {
		if (data == null) {
			loadData();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String[] array = new String[mMessages.size()];
		for (int i = 0; i < mMessages.size(); i++) {
			array[i] = mMessages.get(i);
		}
		builder.setTitle(R.string.broadcast_totalsend_dialog_title)
				.setItems(array, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mTextMessage.setText(array[which]);
						data.messenger = array[which];
					}
				}).create().show();
	}

	static class DataHolder {
		int sex;
		int totalPeople;
		String messenger;
		int point;

		public DataHolder(int sex, int total, String mess, int p) {
			this.sex = sex;
			this.totalPeople = total;
			this.messenger = mess;
			this.point = p;
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
	}

	@Override
	public void finish() {
		Global.sendAllMessageActivity = null;
		super.finish();
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

	@Override
	public void handleReceiveData() {
		init();
		data = new DataHolder(SendAllMessageActivity.SEX_MEN, mListBroadcast
				.get(0).getPeople(), mMessages.get(0), mListBroadcast.get(0)
				.getPoint());
		mPointCost.setText(data.point + "pt");
		mTextPeople.setText(data.totalPeople
				+ getString(R.string.broastcast_people));
		mTextMessage.setText(data.messenger);
		btnSexList[0].performClick();// default option sex men was selected
	}

	@Override
	public void handleGetList() {

	}
}
