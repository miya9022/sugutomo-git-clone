package com.cls.sugutomo.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.BuyPointActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.FavoriteAPI;
import com.cls.sugutomo.api.GetListUserAPI;
import com.cls.sugutomo.api.ProfileOtherAPI;
import com.cls.sugutomo.api.ReportUserAPI;
import com.cls.sugutomo.api.SetFootprintAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.chat.ChatBackgroundService.ChatServiceBinder;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.viewpagerindicator.CirclePageIndicator;
import com.cls.sugutomo.viewpagerindicator.CustomFragmentAdapter;
import com.loopj.android.http.RequestParams;

public class ViewProfileActivity extends BaseTabActivity implements
		APICallbackInterface {

	private static final String TAG = ViewProfileActivity.class.getSimpleName();
	private UserModel mUserFriend;
	private SessionManager mSession;
	private FavoriteAPI apiFavorite;
	private boolean isFavorite = false;
	private TextView txtFavorite;
	private BroadcastReceiver mReceiver;
	private TextView txtWallStatus, txtDistance, txtLastLogin;
	private TextView txtSex, txtAge, txtHight;// txtTargetFavorite;
	// private ImageView avatar;
	private AlertDialog dialogFavarite, dialogBlock, dialogSendPoint;
	// private boolean giftNeedPoint = false;
	private Context context;
	LinearLayout detailProfileContainer;
	LinearLayout loadProfileContainer;
	float deviceWidth, deviceHeight;
	float scaleDensity;
	public ArrayList<ProfileModel> listProfile = new ArrayList<ProfileModel>();
	// private int pointGift = 0;
	private AlertDialog dialogCustom;
	private boolean isBlocked;
	private String[] options;

	private boolean mIsBound = false;
	private ChatBackgroundService mService;
	private ChatType mChatType;
	private long timeNowEpoch;
	// Database
	private DatabaseHandler mDatabase;
	private ArrayList<ImageModel> userAvatarList = new ArrayList<ImageModel>();

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.activity_view_other_profile, null, false);
		frameLayout.addView(activityView);
		txtFavorite = (TextView) findViewById(R.id.txt_add_favorite);
		if (savedInstanceState != null) {
			try {
				mUserFriend = (UserModel) savedInstanceState.get(Params.USER);
				userAvatarList = (ArrayList<ImageModel>) savedInstanceState
						.get(Params.AVATAR_LIST);
				listProfile = (ArrayList<ProfileModel>) savedInstanceState
						.get(Params.LIST_PROFILE);
				if (userAvatarList.size() == 1) {
					// #1 thi call trong onrestore
					initViewPagerCircleIndictor();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			Intent intent = getIntent();
			if (intent != null) {
				mUserFriend = (UserModel) intent
						.getSerializableExtra(Params.USER);
				setTitle(mUserFriend.getName());
			}
			// set avatar o pager dau tien
			userAvatarList.add(new ImageModel(mUserFriend.getImageId(),
					mUserFriend.getUserAvatar(), mUserFriend.getUserId()));
			initViewPagerCircleIndictor();
		}

		mSession = SessionManager.getInstance(getApplicationContext());
		context = getApplicationContext();
//		Log.v("", "session: "+mSession.getToken()+":"+mSession.getUserId() +":"+mUserFriend.getUserId() );
		init();
		
		assignHeighLayout();

		// bind service
		mDatabase = DatabaseHandler.getInstance(this);
		bindService();
		mChatType = ChatType.getInstance(this);
		// reproduce foreclose to send to GA
		// ////////
		if (mUserFriend != null
				&& !Global.listBlockedOrBeBlocked.contains(mUserFriend
						.getUserId()))
			sendFootprintAPI();

		// Analytics.onCreate(this, TAG);version 4
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// ONCREAT-ONSTART-ONRESUME-onRestoreInstanceState(NEU CO)
		super.onRestoreInstanceState(savedInstanceState);
		try {
			mUserFriend = (UserModel) savedInstanceState.get(Params.USER);
			listProfile = (ArrayList<ProfileModel>) savedInstanceState
					.get(Params.LIST_PROFILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		handleGetList();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		savedInstanceState.putSerializable(Params.USER, mUserFriend);
		savedInstanceState.putSerializable(Params.AVATAR_LIST, userAvatarList);
		savedInstanceState.putSerializable(Params.LIST_PROFILE, listProfile);
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	private void sendFootprintAPI() {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_USER_ID, mUserFriend.getUserId());

		SetFootprintAPI api = new SetFootprintAPI(this,
				SetFootprintAPI.SET_FOOTPRINT);
		api.setParams(request);
		api.onRunButtonPressed();
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
	protected void onDestroy() {
		super.onDestroy();
		unBindService();
		mDatabase.closeDB();
	}

	private void initViewPagerCircleIndictor() {
		//Log.v("", "onview initViewPagerCircleIndictor ");
		// getSupportFragmentManager().beginTransaction().
		CustomFragmentAdapter mAdapter = new CustomFragmentAdapter(
				getSupportFragmentManager(), false);
		CustomFragmentAdapter.userAvatarList = userAvatarList;
		ViewPager mPager = (ViewPager) findViewById(R.id.profile_pager);
		mPager.setAdapter(mAdapter);
		CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setFillColor(Color.RED);
	}

	private void init() {
		// avatar = (ImageView) findViewById(R.id.profile_avatar);
		txtWallStatus = (TextView) findViewById(R.id.profile_wall_status);
		String statusStr = mUserFriend.getWallStatus();
		if (statusStr != null && statusStr.length() == 0) {
			txtWallStatus.setText(getString(R.string.no_status));
			txtWallStatus.setTextColor(Color.GRAY);
		} else{
			txtWallStatus.setText(mUserFriend.getWallStatus());
			txtWallStatus.setTextColor(Color.BLACK);
		}

		txtDistance = (TextView) findViewById(R.id.profile_txt_distance);
		txtDistance.setText(Global.distanceBetweenGPS(Global.userInfo,
				mUserFriend,this));
		mUserFriend.setDistance(Global.distanceBetweenGPS(Global.userInfo,
				mUserFriend,this));
		txtLastLogin = (TextView) findViewById(R.id.profile_txt_timelogin);
		txtLastLogin.setText(Global.calculateTime(getApplicationContext(),
				mUserFriend.getTimeLastLogin())
				+ this.getString(R.string.before));

		txtSex = (TextView) findViewById(R.id.profile_txt_sex);
		int sex = mUserFriend.getSex();
		String gt = sex == 1 ? getString(R.string.broastcast_text_men)
				: getString(R.string.broastcast_text_women);
		txtSex.setText(gt);
		txtAge = (TextView) findViewById(R.id.profile_txt_age);
		String age = Global.getAge(mUserFriend.getBirthday());
		if (!age.equalsIgnoreCase("unknow")) {
			age += getString(R.string.profile_default_age);
		}
		txtAge.setText(age);
		txtHight = (TextView) findViewById(R.id.profile_txt_hight);
		// txtHight.setText("" + mUserFriend.getHeight() + "cm");

		// String partner = mUserFriend.getInterestedPartner() == 2 ?
		// getString(R.string.broastcast_text_men)
		// : getString(R.string.broastcast_text_women);
		// txtTargetFavorite = (TextView)
		// findViewById(R.id.profile_target_favorite);
		// txtTargetFavorite.setText("" + partner);

	}

	public void onChangeWallStatus(View v) {

	}

	public void onLoadProfile(View v) {
		listProfile = new ArrayList<ProfileModel>();
		ProfileOtherAPI api = new ProfileOtherAPI(this, this, listProfile,
				userAvatarList);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_USER_ID, mUserFriend.getUserId());
		api.setParams(request);
		api.onRunButtonPressed();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mReceiver != null)
			this.unregisterReceiver(mReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkUserFollowed();
		setTitle(mUserFriend.getName());
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_GIF_POINT);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// extract our message from intent
				String messenger = (String) intent
						.getStringExtra(Params.MESSAGE);
				// Log.v("", "msg : "+messenger);
				receiveSendPoint(messenger);
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
		if (mService != null && !mService.isAuthenticated()) {
			mService.reConenect();
		}
	}

	private void checkUserFollowed() {
		// de xac dinh da folow hay chua
		if (!Global.IS_LOADED_FAVORITE_LIST) {
			Global.listFavorite = new Vector<UserModel>();
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_USER_ID_, mSession.getUserId());
			request.put(Params.PARAM_TOKEN_, mSession.getToken());
			GetListUserAPI apiListFavorite = new GetListUserAPI(this, this,
					Global.listFavorite, Params.FAVORITE);
			apiListFavorite.setParams(request);
			apiListFavorite.setUseProgressbar(true);
			apiListFavorite.onRunButtonPressed();
		} else {
			checkFollowed();
		}
	}

	private void checkFollowed() {
		if (Global.listFavorite.size() > 0) {
			isFavorite = false;
			for (UserModel user : Global.listFavorite) {
				if (user.getUserId() == mUserFriend.getUserId()) {
					isFavorite = true;
					break;
				}
			}
		}
		changeDrawableText();
	}

	private void changeDrawableText() {
		if (isFavorite) {
			// txtFavorite.setText("UnFollow");
			Drawable img = this.getResources().getDrawable(
					R.drawable.favorite_icon_red);
			txtFavorite.setCompoundDrawablesWithIntrinsicBounds(null, img,
					null, null);
		} else {
			// txtFavorite.setText("Follow");
			Drawable img = this.getResources().getDrawable(
					R.drawable.favorite_icon);
			txtFavorite.setCompoundDrawablesWithIntrinsicBounds(null, img,
					null, null);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.profile_setting_btn:
			optionOther();
			break;

		case R.id.profile_gift_btn:
			// TODO
			if (Global.userInfo.getUserId() == mUserFriend.getUserId()) {
				Toast.makeText(context, "unable send gift  to yourself ",
						Toast.LENGTH_LONG).show();
				break;
			}
			final int[] options = getResources().getIntArray(
					R.array.point_option);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.title_present));
			builder.setNegativeButton(getString(R.string.btn_cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});
			String[] optionTexts = new String[options.length];
			for (int i = 0; i < options.length; i++) {
				optionTexts[i] = options[i] + "pt";
			}
			builder.setSingleChoiceItems(optionTexts, 0,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Log.v(TAG, "point=" + options[which]);
							// sendPoints(options[which]);
							final int point = options[which];
							dialogSendPoint = Global.customDialog(
									ViewProfileActivity.this,
									getString(R.string.send_point_confirm,
											options[which]),
									new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											dialogSendPoint.dismiss();
											sendPoints(point);
										}
									});
						}
					});
			builder.show();
			break;
		case R.id.btn_chat:
			Intent intent = new Intent(this, ChatDetailActivity.class);
			// send object UserModel
			intent.putExtra(Params.USER, mUserFriend);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			overridePendingTransition(R.anim.diagslide_enter,
					R.anim.diagslide_leave);
			break;
		case R.id.txt_add_favorite:
			if (!isFavorite) {
				dialogFavarite = Global.customDialog(this,
						getString(R.string.sure_favorite),
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								dialogFavarite.dismiss();

								RequestParams request = new RequestParams();
								request.put(Params.PARAM_USER_ID_,
										mSession.getUserId());
								request.put(Params.PARAM_TOKEN_,
										mSession.getToken());
								request.put(Params.PARAM_USER_ID,
										mUserFriend.getUserId());
								apiFavorite = new FavoriteAPI(
										ViewProfileActivity.this,
										ViewProfileActivity.this,
										FavoriteAPI.ADD, mUserFriend);
								apiFavorite.setParams(request);
								apiFavorite.onRunButtonPressed();
								Global.isRequestingFavorite1User = true;
							}
						});

			} else {
				dialogFavarite = Global.customDialog(this,
						getString(R.string.sure_remove_favorite),
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								dialogFavarite.dismiss();

								RequestParams request = new RequestParams();
								request.put(Params.PARAM_USER_ID_,
										mSession.getUserId());
								request.put(Params.PARAM_TOKEN_,
										mSession.getToken());
								request.put(Params.PARAM_USER_ID,
										mUserFriend.getUserId());
								apiFavorite = new FavoriteAPI(
										ViewProfileActivity.this,
										ViewProfileActivity.this,
										FavoriteAPI.REMOVE, mUserFriend);
								apiFavorite.setParams(request);
								apiFavorite.onRunButtonPressed();
								Global.isRequestingFavorite1User = true;
							}
						});
			}

			break;

		default:
			break;
		}

	}

	private void sendPoints(final int point) {
		timeNowEpoch = System.currentTimeMillis();
		String text = mChatType.sendPointJSON(timeNowEpoch, point).toString();
		String to = mUserFriend.getUserName() + "@" + Global.CHAT_SERVER;
		Log.i(TAG, "Sending point json = " + text + " to " + to);
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);
		if (mService.isAuthenticated()) {
			mService.sendMessage(msg);
			MessageModel message = new MessageModel(timeNowEpoch, timeNowEpoch,
					Global.userInfo, text, timeNowEpoch,
					mUserFriend.getDistance(), false);
			mDatabase.addMessage(Global.userInfo.getUserName(),
					mUserFriend.getUserName(), message, false);
		} else {
			// ShowMessage.showDialog(this, this.getString(R.string.ERR_TITLE),
			// this.getString(R.string.ERR_CONNECT_FAIL));
			mService.reConenect();
			Global.customDialog(this, getString(R.string.ERR_CONNECT_FAIL),
					null);
		}
	}

	private void optionOther() {
		if (Global.listUserBlocked.size() >= 0) {
			isBlocked = false;
			for (Integer entry : Global.listUserBlocked) {
				// Log.v(TAG, "user blocked " + entry.getKey());
				if (entry == mUserFriend.getUserId()) {
					isBlocked = true;
					break;
				}
			}
		}

		options = getResources().getStringArray(R.array.other_option_2);
		if (isBlocked) {
			options = getResources().getStringArray(
					R.array.other_option_2_unblock);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.btn_chatOptionOther));
		builder.setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						dialog.dismiss();
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							// block user
							if (!isBlocked) {
								dialogBlock = Global.customDialog(
										ViewProfileActivity.this,
										getString(R.string.sure_block),
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												// stub
												dialogBlock.dismiss();
												blockOrUnblockUser();
											}
										});
							} else {
								dialogBlock = Global.customDialog(
										ViewProfileActivity.this,
										getString(R.string.sure_unblock),
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												// stub
												dialogBlock.dismiss();
												blockOrUnblockUser();
											}
										});
							}

							break;
						case 1:
							reportUser(options[which]);
							break;
						}
						dialog.dismiss();
					}

				});
		builder.show();
	}

	private void blockOrUnblockUser() {
		if (!mService.isAuthenticated()) {
			ShowMessage.showDialog(context, getString(R.string.ERR_TITLE),
					getString(R.string.ERR_CONNECT_FAIL));
			return;
		}
		long timeNowEpoch = System.currentTimeMillis();
		String text = "";
		if (!isBlocked) {
			text = mChatType.sendBlockJSON(timeNowEpoch).toString();
			Global.listUserBlocked.add(mUserFriend.getUserId());
			Global.listBlockedOrBeBlocked.add(Integer.valueOf(mUserFriend
					.getUserId()));
			// ShowMessage.showMessage(getApplicationContext(),
			// getString(R.string.chat_block_user));
			Global.customDialog(this,
					getString(R.string.chat_block_user, mUserFriend.getName()),
					null);
			isBlocked = !isBlocked;
		} else {
			text = mChatType.sendUnblockJSON(timeNowEpoch).toString();
			Global.listUserBlocked.remove(Integer.valueOf(mUserFriend
					.getUserId()));
			Global.listBlockedOrBeBlocked.remove(Integer.valueOf(mUserFriend
					.getUserId()));
			// ShowMessage
			// .showMessage(
			// getApplicationContext(),
			// getString(R.string.chat_unblock_user,
			// mUserFriend.getName()));
			Global.customDialog(
					this,
					getString(R.string.chat_unblock_user, mUserFriend.getName()),
					null);
			isBlocked = !isBlocked;
		}
		Global.savePref(editor, Params.PREF_LIST_USER_BLOCK,
				Global.listUserBlocked);
		Global.savePref(editor, Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
				Global.listBlockedOrBeBlocked);
		String to = mUserFriend.getUserName() + "@" + Global.CHAT_SERVER;
		// send mess len la` server tu goi api block or unblock
		Log.i(TAG, "Sending block/unblock json = " + text + " to " + to);
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);

		mService.sendMessage(msg);

		long mConversationId = mDatabase.getConversationByUserId(
				mUserFriend.getUserId(), Global.userInfo.getUserId());
		if (mConversationId < 0) {
			if (-mConversationId != mUserFriend.getUserId()) {
				// TODO update and add new conversation
				mConversationId = -mUserFriend.getUserId();
				mDatabase.addConversation(mConversationId,
						mUserFriend.getUserId(), Global.userInfo.getUserId(),
						0, Global.getUserInfo(mUserFriend).toString());
			}
		}

		MessageModel message = new MessageModel(mConversationId, timeNowEpoch,
				Global.userInfo, text, timeNowEpoch, mUserFriend.getDistance(),
				false);
		if (Global.userInfo != null && mUserFriend != null)
			message.setDistance(Global.distanceBetweenGPS(Global.userInfo,
					mUserFriend,this));
		mDatabase.addMessage(Global.userInfo.getUserName(),
				mUserFriend.getUserName(), message, false);
	}

	private void executeReportUser(String content) {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_USER_ID, mUserFriend.getUserId());
		request.put(Params.PARAM_CONTENT, content);
		ReportUserAPI report = new ReportUserAPI(this);
		report.setParams(request);
		report.onRunButtonPressed();
	}

	private void reportUser(String title) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title
				+ getString(R.string.text_define_max_characters,
						Global.MAX_LENGTH));
		final EditText report = new EditText(getApplicationContext());
		report.setMaxLines(Global.MAX_LINE);
		report.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				Global.MAX_LENGTH) });
		report.setFocusable(true);
		report.setFocusableInTouchMode(true);
		report.requestFocus();
		builder.setView(report);
		builder.setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setPositiveButton(getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Report user method
						executeReportUser(report.getText().toString());
						dialog.dismiss();
					}
				});
		builder.show();
	}

	public void receiveSendPoint(String messageBody) {
		JSONObject data = mChatType.getData(messageBody);
		if (data != null) {
			Log.i(TAG, "receive message test : " + data.toString());
			long id = mChatType.getId(data);
			// Log.i(TAG, "id = " + id + " timeNowSend = " + timeNowEpoch);
			if (id == timeNowEpoch) {
				String type = mChatType.receiveJSON(messageBody);
				String message = mChatType.executeError(messageBody);
				showAlert(type);
			}
		}
	}

	private void showAlert(final String type) {

		runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ViewProfileActivity.this);
				View view;
				String text = "";
				if (type.equals(ChatType.SEND_SUCCESS)) {
					view = LayoutInflater.from(ViewProfileActivity.this)
							.inflate(R.layout.custom_dialog_ok, null);
					text = getString(R.string.success);
					Global.customDialog(ViewProfileActivity.this, text, null);
				} else {// if (type.equals(ChatType.ERROR_1001)){
					view = LayoutInflater.from(ViewProfileActivity.this)
							.inflate(R.layout.custom_dialog, null);
					text = getString(R.string.point_need_more_question_buypoint);
					// giftNeedPoint = true;
					dialogFavarite = Global.customDialog(
							ViewProfileActivity.this, text,
							new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									Intent intents = new Intent(
											ViewProfileActivity.this,
											BuyPointActivity.class);
									intents.putExtra(Params.PARAM_TITLE,
											getString(R.string.buy_point_title));
									intents.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
									intents.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
									ViewProfileActivity.this
											.startActivity(intents);
									dialogFavarite.dismiss();
								}
							});
				}
				// ((TextView) view.findViewById(R.id.custom_dialog_title))
				// .setText(text);
				// builder.setView(view);
				// dialogCustom = builder.create();
				// dialogCustom.show();

			}
		});
	}

	@Override
	public void handleReceiveData() {
		if (Global.isRequestingFavorite1User) {// click folow or unfolow
			isFavorite = !isFavorite;
			Global.isRequestingFavorite1User = false;
			// send a message to user favorite if he/she enable notice when
			// someone favorite
			if (isFavorite) {
				Global.listFavoriteId.add(mUserFriend.getUserId());
			} else {
				Global.listFavoriteId.remove(Integer.valueOf(mUserFriend
						.getUserId()));
			}
			Global.savePref(editor, Params.PREF_LIST_FAVORITE_ID,
					Global.listFavoriteId);
			// Log.v("",
			// "send a message to user favorite if he/she enable notice when");
		} else {// load followlist lan dau
			checkFollowed();
		}
		changeDrawableText();
	}

	@Override
	public void handleGetList() {
		if (userAvatarList.size() > 1) {// =
			// co 1 avatar va nhieu image khac
			String url = userAvatarList.get(0).getFullPath();// id active
																// avatar, vi
																// avatar se la
																// img dau tien
																// duoc set in
																// oncreate
			int size = userAvatarList.size();
			for (int i = size - 1; i > 0; i--) {
				// ko dung imageID vi neu user change avatar thi nhung nguoi
				// khac chi
				// dc broastcast fullIMG khi co new messeage, khi load lai
				// userlist thi imageID moi refresh
				if (url.equalsIgnoreCase(userAvatarList.get(i).getFullPath())) {
					// loai bo image set = avatar trong list lay ve. vi` minh
					// set no luon trong oncreat roi
					userAvatarList.remove(i);
					break;
				}
			}

			initViewPagerCircleIndictor();
		}
		if (listProfile.size() > 0) {
			loadProfileContainer.setVisibility(View.GONE);
			detailProfileContainer = (LinearLayout) findViewById(R.id.profile_detail_layout);
			detailProfileContainer.removeAllViews();
			detailProfileContainer.setVisibility(View.VISIBLE);
			Collections.sort(listProfile, new Comparator<ProfileModel>() {
				public int compare(ProfileModel s1, ProfileModel s2) {
					return (s1.displayOrder - s2.displayOrder);
				}
			});
			for (int i = 0; i < listProfile.size(); i++) {
//				Log.e("sagasga", "profile  = " + listProfile.get(i).getName());
				RelativeLayout childLayOut = new RelativeLayout(context);
				childLayOut.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) (deviceHeight * 0.1f)));
				detailProfileContainer.addView(childLayOut);

				TextView profileTitle = new TextView(context);
				RelativeLayout.LayoutParams lo = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				lo.addRule(RelativeLayout.CENTER_VERTICAL);
				lo.leftMargin = (int) (10 * scaleDensity);
				profileTitle.setLayoutParams(lo);
				profileTitle.setTextColor(Color.BLACK);
				profileTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				profileTitle.setText(listProfile.get(i).getName());
				childLayOut.addView(profileTitle);

				TextView profileValue = new TextView(context);
				lo = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				lo.addRule(RelativeLayout.CENTER_VERTICAL);
				lo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				lo.rightMargin = (int) (10 * scaleDensity);
				profileValue.setLayoutParams(lo);
				profileValue.setTextColor(Color.BLACK);
				profileValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				String val=listProfile.get(i).getValue();
				if(val==null  || val.length()==0) val = getString(R.string.not_input);
				profileValue.setText(val);
				childLayOut.addView(profileValue);

				View v = new View(context);

				lo = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						(int) (1 * scaleDensity));
				lo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				v.setLayoutParams(lo);
				v.setBackgroundColor(Color.parseColor("#E8E8E8"));
				childLayOut.addView(v);
			}
		}

	}

	private void showDialog(String title, String mess, final int request) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(mess);
		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				// Do something with value!
				if (request == 1) {
					// demo
					Toast.makeText(context, "demo change status  ",
							Toast.LENGTH_LONG).show();
					txtWallStatus.setText(value);

				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	public void assignHeighLayout() {
		scaleDensity = getResources().getDisplayMetrics().density;
		// deviceWidthInDp =(int)(deviceWidth / scaleDensity);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		deviceWidth = dm.widthPixels;
		deviceHeight = dm.heightPixels - 40;// 40 ~ statusbar+ sugutomo bar tren
											// top
		RelativeLayout avatarContainer = (RelativeLayout) findViewById(R.id.profile_layout_avatar_container);
		// LinearLayout.LayoutParams vi parent cua no la linerlayout
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.3f));
		avatarContainer.setLayoutParams(parms);

		LinearLayout.LayoutParams lo = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.1f));
		lo.leftMargin = (int) (scaleDensity * 10);
		lo.rightMargin = (int) (scaleDensity * 10);
		txtWallStatus.setLayoutParams(lo);

		RelativeLayout basicInfoContainer = (RelativeLayout) findViewById(R.id.profile_layout_basicinfo_container);
		basicInfoContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.15f)));

		// RelativeLayout partnerContainer = (RelativeLayout)
		// findViewById(R.id.profile_layout_interst_container);
		// partnerContainer.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.MATCH_PARENT,
		// (int) (deviceHeight * 0.1f)));

		loadProfileContainer = (LinearLayout) findViewById(R.id.profile_layout_insertprofile_container);
		loadProfileContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.22f)));

		LinearLayout tabContainer = (LinearLayout) findViewById(R.id.profile_tab_bot);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.1f));
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		tabContainer.setLayoutParams(lp);

		detailProfileContainer = (LinearLayout) findViewById(R.id.profile_detail_layout);

	}
}
