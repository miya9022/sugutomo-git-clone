package com.cls.sugutomo.chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.camera.MediaStoreUtils;
import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.BuyPointActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.ViewImageChatActivity;
import com.cls.sugutomo.adapter.MessageAdapter;
import com.cls.sugutomo.adapter.StampAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.DeleteConversationAPI;
import com.cls.sugutomo.api.DeleteMessageAPI;
import com.cls.sugutomo.api.FavoriteAPI;
import com.cls.sugutomo.api.GetMessageConversationAPI;
import com.cls.sugutomo.api.LoadStampAPI;
import com.cls.sugutomo.api.ReportUserAPI;
import com.cls.sugutomo.api.UploadImageChatAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatBackgroundService.ChatServiceBinder;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.MessagesModel;
import com.cls.sugutomo.model.StampModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.EditProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.loopj.android.http.RequestParams;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

/*know bug:
 *  - delete lastmessage then comeback to chatlist. Refresh chatlist 
 *  but lastmessage and unread in chatlist not update because Server
 *  not update this => it not client bug => server doesn't want to fix :(
 * 
 * 
 * 
 */
public class ChatDetailActivity extends BaseTabActivity implements
		APICallbackInterface, OnItemClickListener,
		EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener,
		OnItemLongClickListener, OnTouchListener {
	private static final String TAG = ChatDetailActivity.class.getSimpleName();
	private Context mContext;
	private FavoriteAPI apiFavorite;
	private BroadcastReceiver mReceiver, mReceiverReconnect,
			mReceiverChatServer;
	// Camera activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_SELECT_IMAGE_REQUEST_CODE = 200;
	private AlertDialog dialogBlock, dialogFavarite, dialogSendPoint;
	// Session Manager Class
	private SessionManager mSession;
	private ChatType mChatType;

	// handler run to load avatar
	public Handler mHandler = new Handler();

	// Database

	// Model
	public UserModel chatUser;
	public MessagesModel msgsDatabases;
	// private ImageModel mImageModel = null;

	// Adapter
	public MessageAdapter messageAdapter;
	private StampAdapter stampAdapter;

	// View
	private ViewGroup emoGroup;
	private ViewGroup optionGroup;
	private GridView listStamp;
	private EmojiconEditText textMessage;
	public ListView listview;
	public Button btnDownloadStamp;
	private SwipeRefreshLayout swipeContainer;

	// Array/Vector data
	public Vector<MessageModel> all_message = new Vector<MessageModel>();
	private long mConversationId = 0;
	private int mPage;

	// Progress bar
	private ProgressDialog mProgressDialog;
	private boolean mIsBound = false;
	private ChatBackgroundService mService;

	private boolean isFavorite = false;
	private boolean isBlocked = false;
	private String[] options;
	public boolean isPausing = false;
	private long lastMessageId = 0;
	public int totalMessLoad = 0;
	public static String messageToSend = "";
	private boolean isReconnectInternet = false;
	private HashMap<String, String> mListMessageKey = new HashMap<String, String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// start from notification bar
		Intent callerIntent = getIntent();
		UserModel us = (UserModel) callerIntent
				.getSerializableExtra(Params.PARAM_STRAT_FROM_NOTIFICATION);

		if (us != null) {
			Global.userInfo = us;
		}
		// end notification bar
		super.onCreate(savedInstanceState);
		mContext = this;
		// Session Manager
		mSession = SessionManager.getInstance(getApplicationContext());
		mChatType = ChatType.getInstance(getApplicationContext());

		// add layout of activity to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.activity_chat,
				null, false);
		frameLayout.addView(activityView);

		// init view
		textMessage = (EmojiconEditText) this.findViewById(R.id.chatET);
		listview = (ListView) this.findViewById(R.id.listMessages);
		listview.setOnItemLongClickListener(this);
		listview.setOnItemClickListener(this);
		listview.setOnTouchListener(this);
		messageAdapter = new MessageAdapter(ChatDetailActivity.this,
				R.layout.chat_item_right, all_message, chatUser);
		listview.setAdapter(messageAdapter);

		listStamp = (GridView) findViewById(R.id.list_stamp);
		emoGroup = (ViewGroup) findViewById(R.id.emoGroup);
		optionGroup = (ViewGroup) findViewById(R.id.chatOptionGroup);
		btnDownloadStamp = (Button) findViewById(R.id.btn_download_stamp);
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
		// Setup refresh listener which triggers new data loading
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(Global.checkInternet(mContext))
				loadMessageServer();
				else{
					swipeContainer.setRefreshing(false);
					ShowMessage.showDialog(mContext, getString(R.string.ERR_TITLE),
							getString(R.string.ERR_CONNECT_FAIL));
				}
				// new PullToRefreshDataTask().execute();
			}
		});
		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		// load stamp and emoji
		listStamp.setOnItemClickListener(this);
		// load stamp API
		if (Global.listStampModel.size() < 0) {
			// loadStampAPI();
			try {
				JSONArray data = new JSONArray(pref.getString(
						Params.PREF_STAMP, "NULL"));
				if (data != null && !data.equals("NULL") && data.length() > 0) {
					for (int i = 0; i < data.length(); i++) {
						JSONObject item = data.getJSONObject(i);
						StampModel stamp = new StampModel();
						stamp.setStampId(item.optInt(Params.PARAM_STAMP_ID));
						stamp.setName(item.optString(Params.PARAM_IMAGE));
						stamp.setUrl(item
								.optString(Params.PARAM_IMAGE_FULL_URL));
						Global.listStampModel.add(stamp);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		loadStamp();
		// set up emoji layout
		setEmojiconFragment();

		// initAll();
		// bind service
		bindService();
		Log.v("", "chatacti oncreate");
	}

	private void resetChatParam() {
		mPage = 1;
		lastMessageId = 0;
		all_message.clear();
		if(messageAdapter!=null)messageAdapter.notifyDataSetChanged();
		mListMessageKey.clear();
		totalMessLoad = 0;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		// reset all_message and messageInPages
		resetChatParam();
		Log.v(TAG, "onnew intent call");
		Log.v("", "chatacti onnew intent");
		// initAll();

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		chatUser = (UserModel) savedInstanceState.get(Params.USER);
		Log.v(TAG, "onRestoreInstanceState");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		savedInstanceState.putSerializable(Params.USER, chatUser);
		// Always call the superclass so it can save the view hierarchy state
		try {
			// deo hieu sao thinh thoang lai loi khi onSaveInstanceState lien
			// quan den emoji
			// fix tam thoi vay. update android support v4 da fix duoc loi nay.
			super.onSaveInstanceState(savedInstanceState);
		} catch (Exception e) {
			// TODO: handle exception
		}
		Log.v(TAG, "onSaveInstanceState");
	}

	public void initAll() {
		UserModel	user = (UserModel) getIntent().getSerializableExtra(Params.USER);
		boolean changeChatUser=true;
		if(chatUser!=null && chatUser.getUserId()==user.getUserId()) changeChatUser=false;
		
		chatUser=user;
		if (chatUser.getUserId() == Global.ADMIN)
			((ViewGroup) findViewById(R.id.linearLayout2))
					.setVisibility(View.GONE);
		else
			((ViewGroup) findViewById(R.id.linearLayout2))
					.setVisibility(View.VISIBLE);

		// get conversationId from database
		getActionBar().setTitle(chatUser.getName());
		Global.chatDetailActivity = this;
		if (all_message.size() > 0 && !Global.checkInternet(this)){
			//back to chat but lost conenction => no reload
			return;
		}else {
			if (Global.CHAT_NEED_REFRESH || changeChatUser) {
				// = true when recevier broadcast reconnect to intenet in
				// bastTabactivity
				// set = false in onCreat because oncreat we alway recreate new
				// activity
				// but when comback to top activity stack we need check update
				Global.CHAT_NEED_REFRESH = false;
				resetChatParam();
				messageAdapter.notifyDataSetChanged();
				loadMessageServer();
				//Log.v(TAG, "update because PARAM_RECONNECT_INTERNET_CHECK");
			}else{
				sendReadText();
			}
		}

	}

	private void loadMessageServer() {
		GetMessageConversationAPI messApi = new GetMessageConversationAPI(this,
				this, all_message, chatUser, mListMessageKey);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_USER_ID, chatUser.getUserId());
		if (lastMessageId > 0) {
			messApi.setUseProgressbar(false);
			request.put("last_message_id", lastMessageId);
		} else {
			messApi.setUseProgressbar(true);
		}
		// request.put("getnew", 1);
		messApi.setParams(request);
		messApi.onRunButtonPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPausing = true;
		if (mReceiver != null)
			this.unregisterReceiver(mReceiver);
		if (mReceiverReconnect != null)
			this.unregisterReceiver(mReceiverReconnect);
		if (mReceiverChatServer != null)
			this.unregisterReceiver(mReceiverChatServer);
		try {
			if(mService!=null )mService.dismissNotification(chatUser.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v("", "chatacti onresume");
		// if (!onActivityResult) {
		initAll();
		// } else {
		// onActivityResult = false;
		// }
		isPausing = false;
		getActionBar().setTitle(chatUser.getName());
		onReceiveReconnectInternet();
		onChatServiceReconnected();
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_GIF_POINT);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// ko can nua
				// extract our message from intent
				// String messenger = (String) intent
				// .getStringExtra(Params.MESSAGE);
				// Log.v("", "msg : "+messenger);
				// receiveSendPoint(messenger);
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
		if (mService != null && !mService.isAuthenticated()) {
			mService.reConenect();
		} else {
			if (mService == null)
				bindService();

		}
		try {
			if(mService!=null )mService.dismissNotification(chatUser.getUserId());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void onReceiveReconnectInternet() {
		// TODO
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_RECONNECT_INTERNET);
		mReceiverReconnect = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// refresh chat prevent lost message openfire
				Global.CHAT_NEED_REFRESH = false;
				isReconnectInternet = true;
				resetChatParam();
				messageAdapter.notifyDataSetChanged();
				loadMessageServer();
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiverReconnect, intentFilter);
	}

	private void onChatServiceReconnected() {
		// TODO
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_RECONNECT_CHATSERVER);
		mReceiverChatServer = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// refresh chat prevent lost message openfire
				if (isReconnectInternet) {
					sendReadText();
					isReconnectInternet = false;
					// Toast.makeText(getApplicationContext(),
					// "chat server is ready", Toast.LENGTH_SHORT).show();
				}
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiverChatServer, intentFilter);
	}

	private void bindService() {
		if (!mIsBound) {
			Intent intent = new Intent(this, ChatBackgroundService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

	};

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		MessageModel messageModel = (MessageModel) parent
				.getItemAtPosition(position);
		if (messageModel.getUserInfo().getUserId() == Global.userInfo
				.getUserId()) {
			showDialogOptionMessage(messageModel, position);
			// Toast.makeText(this, "id: " + messageModel.getMessageId(),
			// Toast.LENGTH_LONG).show();
		} else {
			// Toast.makeText(this, "avatar: " +
			// messageModel.getUserInfo().getUserAvatar(),
			// Toast.LENGTH_LONG).show();
			// Log.v("", "avatar model --> "+messageModel.toString());
			// Log.v("",
			// "avatar userinfo--> "+messageModel.getUserInfo().toString());
		}
		return false;
	}

	/**
	 * METHOD SEND API: DELETE MESSAGE
	 * 
	 * @param messageId
	 */
	private void deleteMessageServer(MessageModel model) {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_ID, model.getMessageId());

		DeleteMessageAPI deleteAPI = new DeleteMessageAPI(this, model);
		deleteAPI.setParams(request);
		deleteAPI.onRunButtonPressed();
	}

	public int performDeleteMessageLocal(long messageId) {
		return -1;
		// return database.deleteMessage(messageId);
	}

	/**
	 * METHOD DELETE MESSAGE IN LOCAL
	 * 
	 * @param messageId
	 */
	public void updateAfterdeleteMessage(MessageModel model) {
		all_message.removeElement(model);
		mListMessageKey.remove(model.getMessageId());
		totalMessLoad--;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				final int savePosition = listview.getFirstVisiblePosition();
				messageAdapter.notifyDataSetChanged();
				listview.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						listview.setSelection(savePosition);
					}
				});
				listview.getViewTreeObserver().addOnPreDrawListener(
						new OnPreDrawListener() {

							@Override
							public boolean onPreDraw() {
								// return true: draw content in listview to user
								// return false: not draw
								// when add or del message. notifydataset make
								// listview sometiem flash move from x->y
								// so use onPreDraw to only show content after
								// falsh move finish
								int firstVisible = listview
										.getFirstVisiblePosition();
								int lastVisible = listview
										.getLastVisiblePosition();
								boolean valueRetrun = false;
								if (firstVisible == 0 && savePosition == 0) {
									// del message at position 0
									valueRetrun = true;
								}
								if (firstVisible == 0
										&& lastVisible == all_message.size() - 1) {
									// chat too short, show all in listview
									// =>first time start chat activity and
									// number chat message small
									valueRetrun = true;
								}
								if (firstVisible != 0
										&& firstVisible <= savePosition) {
									// scroll up and load moreold message
									valueRetrun = true;
								}

								if (valueRetrun) {
									// //moi vao` chat. Chua load chat data
									listview.getViewTreeObserver()
											.removeOnPreDrawListener(this);
									return true;
								} else
									return false;
							}
						});
			}
		});

	}

	public void deleteMessageFromServer(final long messageId, String fromName) {
		if(!fromName.equals(chatUser.getUserName())) return;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int size = all_message.size();
				for (int i = size - 1; i >= 0; i--) {
					if(all_message.get(i).getMessageId() == messageId){
						all_message.get(i).setMessage(mContext.getString(R.string.message_removed));
						break;
					}
				}
				if(messageAdapter!=null)messageAdapter.notifyDataSetChanged();
			}
		});
		
	}

	/**
	 * POPUP SHOW DIALOG
	 * 
	 * @param messageModel
	 */
	private void showDialogOptionMessage(final MessageModel messageModel,
			final int position) {
		String[] listChoose;
		if (!messageModel.isError()) {
			listChoose = getResources().getStringArray(
					R.array.list_item_message_option_normal);
		} else {
			listChoose = getResources().getStringArray(
					R.array.list_item_message_option_full);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_option_message));
		builder.setItems(listChoose, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					// delete all my chat
					// - xoa trang khung chat - chu ko del mesage tren server
					resetChatParam();
					messageAdapter.notifyDataSetChanged();
					break;

				case 1:
					// delete message
					if (messageModel.getUserInfo().getUserId() == Global.userInfo
							.getUserId())
						if (!messageModel.isError())
							deleteMessageServer(messageModel);
						else
							updateAfterdeleteMessage(messageModel);

					break;
				case 2:
					// resend messages
					resendMessage(messageModel);
					break;
				}
			}

		}).create().show();
	}



	/**
	 * Add new message into database and listview
	 * 
	 * @param fromName
	 * @param messageModel
	 */
	public boolean updateMessage(final MessageModel messageModel) {
		// because function may call from service=> need update from
		// ui
		// thread
		 FutureTask<Boolean> task=new FutureTask<Boolean>(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				final String type = Global.convertType(messageModel.getType());
				// String typeInData =
				// mChatType.getTypeInData(messageModel.getMessage());
				String fromName = messageModel.getUserInfo().getUserName();
				MessageModel model = new MessageModel(
						messageModel.getConversationId(),
						messageModel.getMessageId(),
						messageModel.getUserInfo(), messageModel.getMessage(),
						false);
				model.setType(type);
				model.setDateMessage(messageModel.getDateMessage());
				model.setDistance(messageModel.getDistance());
//				Log.v(TAG, "vao dayyyy typee" + type + ":" + type + " : "
//						+ model.getMessage());
				if (type.equals(ChatType.SEND_SUCCESS)) {
					String typeInData = Global.convertType(mChatType
							.getTypeInData(messageModel.getMessage()));
					model.setType(typeInData);
					model.setMessage(messageToSend);
					model.setUserInfo(Global.userInfo);// sender is me
					fromName = Global.userInfo.getUserName();
					Log.v("", "" + typeInData + " : " + messageToSend);
				}
				if (!type.equals(ChatType.ERROR_1000)
						&& fromName.equals(chatUser.getUserName())) {
					// "data":{"id":1432189960290,"user":"1465555"},"type":"1000"}
					sendReadText();
				}
				if (!mChatType.acceptDenyMessage(type)) {
					// case nay chi sender la co the nhan duoc neu mess
					// error
					showAlertError(messageModel.getMessage());
				} else {
					if(!fromName.equals(chatUser.getUserName()) && !fromName.equals(Global.userInfo.getUserName())){
						return false;
					}
					// Global.playBeep(getApplicationContext());
					// case fail
					String messId = String.valueOf(model.getMessageId());
					if (mListMessageKey.containsKey(messId))
						return false;

					totalMessLoad++;
					all_message.add(model);
					mListMessageKey.put(messId, messId);
					int lastVisible = listview.getLastVisiblePosition();
					boolean scrollList = true;
					if (lastVisible < listview.getCount() - 1) {
						scrollList = false;
						if(!isPausing)ShowMessage.showMessage(mContext,
								"scroll down to view new message");
					}
					final int distance = listview.getFirstVisiblePosition();
					messageAdapter.notifyDataSetChanged();
                    handleScrollList(scrollList, distance);


				}
				if(!isPausing) {
					//post delay vi update chatactivity truoc roi moi update chat list
					mHandler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							sendReadText();
						}
					}, 500);
				}
				return true;
			}
		});
		runOnUiThread(task);
		try {
			return task.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
private void handleScrollList(boolean scrollList, final int distance){
	if (scrollList) {
		listview.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				listview.setSelection(listview.getCount() - 1);
			}
		});
	} else {
		listview.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				listview.setSelection(distance);

			}
		});
		listview.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						// return true: draw
						// content
						// in listview to
						// user
						// return false: not
						// draw
						// when add or del
						// message.
						// notifydataset
						// make
						// listview sometiem
						// flash
						// move from x->y
						// so use onPreDraw
						// to only
						// show content
						// after falsh
						// move finish
						int firstVisible = listview
								.getFirstVisiblePosition();
						int lastVisible = listview
								.getLastVisiblePosition();
						boolean valueRetrun = false;
						if (firstVisible == 0
								&& lastVisible == all_message
										.size() - 1) {
							// chat too
							// short, show
							// all in
							// listview
							// =>first time
							// start
							// chat activity
							// and
							// number
							// chat message
							// small
							valueRetrun = true;
						}
						if (firstVisible != 0
								&& firstVisible <= distance) {
							// scroll up and
							// load
							// moreold
							// message
							valueRetrun = true;
						}
						if (distance == 0 && firstVisible == 0) {
							valueRetrun = true;
						}
						if (valueRetrun) {
							listview.getViewTreeObserver()
									.removeOnPreDrawListener(
											this);
							return true;
						} else
							return false;
					}
				});

	}
}
	private void showAlertError(String json) {
		String type = mChatType.receiveJSON(json);
		String message = null;
		if (type != null) {
			// JSONObject data = mChatType.getData(json);
			if (type.equals(ChatType.ERROR_1000)) {
				message = getString(R.string.chat_error_user_not_found,
						chatUser.getName());// mChatType.executeError1000(data);
				final String mess = message;
				runOnUiThread(new Runnable() {
					public void run() {
						Global.customDialog(ChatDetailActivity.this, mess, null);
					}
				});
			} else if (type.equals(ChatType.ERROR_1001)) {
				message = getString(R.string.point_need_more_question_buypoint);
				final String mess = message;
				runOnUiThread(new Runnable() {
					public void run() {
						dialogSendPoint = Global.customDialog(
								ChatDetailActivity.this, mess,
								new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										dialogSendPoint.dismiss();
										Intent intent = new Intent(
												ChatDetailActivity.this,
												BuyPointActivity.class);
										intent.putExtra(
												Params.PARAM_TITLE,
												mContext.getString(R.string.buy_point_title));
										intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
										startActivity(intent);
									}
								});
					}
				});
			}
		}

	}

	private void setEmojiconFragment() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.list_emo, EmojiconsFragment.newInstance(false))
				.commit();
	}

	/**
	 * SEND MESSAGE
	 * 
	 * @param messageBody
	 * @param timeNowEpoch
	 */
	public void sendMessage(String messageBody, long timeNowEpoch,
			boolean isCheckBlock, String messageBodyFilter) {
		if (isCheckBlock
				&& Global.listBlockedOrBeBlocked.contains(chatUser.getUserId())) {
			ShowMessage.showDialog(this, this.getString(R.string.ERR_TITLE),
					this.getString(R.string.chat_refuse_cause_block));
			return;
		}

		String to = chatUser.getUserName() + "@" + Global.CHAT_SERVER;
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(messageBody);
		if (mService != null && mService.isAuthenticated()) {
			// setConversationId();
			mService.sendMessage(msg);

		} else {
			// ShowMessage.showDialog(this, this.getString(R.string.ERR_TITLE),
			// this.getString(R.string.ERR_CONNECT_FAIL));
			if (mService == null)
				bindService();
			else
				mService.reConenect();
			// Global.customDialog(this, "test case",
			// null);
			Global.customDialog(this,
					getString(R.string.chat_server_not_connect), null);
		}
	}

	public void resendMessage(MessageModel message) {
		String to = chatUser.getUserName() + "@" + Global.CHAT_SERVER;
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(message.getMessage());
		if (mService.isAuthenticated()) {
			mService.sendMessage(msg);
		}
	}

	public void sendStamp(int stampId, String image_url) {
		messageToSend = String.valueOf(stampId);
		long timeNowEpoch = System.currentTimeMillis();
		String messageBody = mChatType.sendStampJSON(timeNowEpoch, stampId,
				image_url).toString();
		sendMessage(messageBody, timeNowEpoch, true, messageBody);
	}

	public void sendText() {
		long timeNowEpoch = System.currentTimeMillis();
		String messageBody = textMessage.getText().toString();
		// messageBody = StringEscapeUtils.escapeJava(messageBody);
		// Log.v("","StringEscapeUtils"+StringEscapeUtils.escapeJava(messageBody));
		if (messageBody == null || messageBody.length() == 0)
			return;

		if (!Global.checkInternet(this)) {
			ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
					getString(R.string.ERR_CONNECT_FAIL));
			return;
		}
		messageToSend = messageBody;
		messageBody = mChatType.sendTextJSON(timeNowEpoch, messageBody)
				.toString();
		Log.i(TAG, "Sending text = " + messageBody);
		textMessage.setText("");
		sendMessage(messageBody, timeNowEpoch, true, messageBody);
	}

	public void sendImage(ImageModel imageModel) {
		String text = mChatType.sendImageJSON(imageModel.getTimeCreated(),
				(int) imageModel.getImageId(), imageModel.getFullPath())
				.toString();
		messageToSend = imageModel.getFullPath();
		Log.i(TAG, "Sending image " + text);
		Log.i(TAG, "Sending image " + messageToSend);
		sendMessage(text, imageModel.getTimeCreated(), true, text);
	}

	private void sendReadText() {
		if (all_message == null || all_message.size() == 0)
			return;
		MessageModel lastMessage = all_message.get(all_message.size() - 1);
		if (lastMessage.getUserInfo().getUserId() == Global.userInfo
				.getUserId())
			return;
		if (mConversationId <= 0)
			return;
		String text = mChatType.sendReadTextJSON(mConversationId).toString();
		Log.i(TAG, "Sending readtext " + text);
		if (mService != null && mService.isAuthenticated()) {
			sendMessage(text, System.currentTimeMillis(), false, text);
		}
		//
		if (Global.chatListActivityServer != null)
			Global.chatListActivityServer.updateFromChat(String
					.valueOf(mConversationId));
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Global.getUnReadMessageTotal(mContext);
			}
		}, 500);
	}

	private void sendPoints(int point) {
		messageToSend = String.format(
				this.getString(R.string.chat_point_receive),
				Global.userInfo.getName(), point);
		long timeNowEpoch = System.currentTimeMillis();
		String text = mChatType.sendPointJSON(timeNowEpoch, point).toString();
		Log.i(TAG, "Sending point json = " + text);
		sendMessage(text, timeNowEpoch, true, text);
	}

	private void blockOrUnblockUser() {
		long timeNowEpoch = System.currentTimeMillis();
		String text = "";
		if (!isBlocked) {
			text = mChatType.sendBlockJSON(timeNowEpoch).toString();
			Global.listUserBlocked.add(chatUser.getUserId());
			Global.listBlockedOrBeBlocked.add(chatUser.getUserId());
			Global.customDialog(this,
					getString(R.string.chat_block_user, chatUser.getName()),
					null);
			isBlocked = !isBlocked;
		} else {
			text = mChatType.sendUnblockJSON(timeNowEpoch).toString();
			Global.listUserBlocked
					.remove(Integer.valueOf(chatUser.getUserId()));
			Global.listBlockedOrBeBlocked.remove(Integer.valueOf(chatUser
					.getUserId()));
			Global.customDialog(this,
					getString(R.string.chat_unblock_user, chatUser.getName()),
					null);
			isBlocked = !isBlocked;
		}
		Global.savePref(editor, Params.PREF_LIST_USER_BLOCK,
				Global.listUserBlocked);
		Global.savePref(editor, Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
				Global.listBlockedOrBeBlocked);
		Log.i(TAG, "Sending block/unblock json = " + text);
		sendMessage(text, timeNowEpoch, false, text);
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position,
			long id) {
		hiddenKeyboard();
		if (listView == listStamp) {
			int stampId = Global.listStampModel.get(position).getStampId();
			String image_url = Global.listStampModel.get(position).getUrl();
			sendStamp(stampId, image_url);
			emoGroup.setVisibility(View.GONE);
			findViewById(R.id.btn_chatOption).setBackgroundResource(
					R.drawable.option_chat_icon);
			findViewById(R.id.showEmo).setBackgroundResource(
					R.drawable.emotion_icon);
		} else if (listView == this.listview) {
			MessageModel messageModel = (MessageModel) listView
					.getItemAtPosition(position);
			String type = messageModel.getType();
			if (type.equals(ChatType.TYPE_IMAGE)) {
				String path = messageModel.getMessage();
				Intent intent = new Intent(mContext,
						ViewImageChatActivity.class);
				intent.putExtra(Params.PARAM_IMAGE_FULL_URL, path);
				startActivity(intent);
				overridePendingTransition(R.anim.diagslide_enter,
						R.anim.diagslide_leave);
			} else if (type.equals(ChatType.STAMP)) {
				StampModel stampModel = mChatType.excuteStamp(messageModel
						.getMessage());
				File file = new File(
						android.os.Environment.getExternalStorageDirectory(),
						File.separator + Global.PREFERENCE_NAME
								+ File.separator + Params.PARAM_STAMP);
				if (!file.exists())
					file.mkdirs();
				file = new File(file.getPath(), stampModel.getName() + ".png");
				if (!file.exists()) {
					downloadStamps();// sau khi download tu dong notifi mess
										// adapter
				}
			}
		}
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(textMessage);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(textMessage, emojicon);
		// if (textMessage == null || emojicon == null) {
		// return;
		// }
		//
		// int start = textMessage.getSelectionStart();
		// int end = textMessage.getSelectionEnd();
		// int size =emojicon.getEmoji().length();
		// String str="";
		// // for (int i = 0; i < size; i++) {
		// // //Log.v("","emojicon"+ emojicon.getEmoji()+" + "+
		// //Integer.toHexString(Integer.valueOf(emojicon.getEmoji()));
		// char c =
		// (char)Integer.parseInt(emojicon.getEmoji().codePointAt(0)+"", 10 );
		// Log.v("","emojicon char "+emojicon.getIcon() + " : "+
		// Integer.toHexString(c |
		// 0x10000)
		// +"    --- "+Character.toChars(emojicon.getEmoji().codePointAt(0)).toString());
		// // c=
		// Character.toChars(emojicon.getEmoji().codePointAt(0)).toString();
		// // str+=c;
		// // }
		// // char c = (char)
		// // Integer.parseInt(emojicon.getEmoji().codePointAt(0)+"", 10 );
		// // Log.v("","emojicon char "+c + " : "+ Integer.toHexString(c |
		// // 0x10000));
		// str+=c;
		// if (start < 0) {
		// textMessage.append(str);
		// } else {
		// textMessage.getText().replace(Math.min(start, end), Math.max(start,
		// end), str, 0, str.length());
		// }
		//
		// Log.v("","emojicon textMessage "+ textMessage.getText().toString());
	}

	/**
	 * LOAD STAMP
	 */
	public void loadStamp() {
		if (checkStampExist(Params.PARAM_STAMP).size() <= 0) {
			btnDownloadStamp.setVisibility(View.VISIBLE);
		} else {
			getBitmapStamp();
		}
		// for (int i = 0; i < Global.listStampModel.size(); i++) {
		// Global.listStampModel.get(i).setStampSourceId(
		// getResources()
		// .getIdentifier(
		// Params.PARAM_STAMP
		// + Global.listStampModel.get(i)
		// .getStampId(), "drawable",
		// getPackageName()));
		// }
		//
		// stampAdapter = new StampAdapter(getApplicationContext(),
		// Global.listStampModel);
		// listStamp.setAdapter(stampAdapter);
	}

	public Vector<String> checkStampExist(String directoryName) {
		Vector<String> listName = new Vector<String>();
		File directory = new File(
				android.os.Environment.getExternalStorageDirectory(),
				File.separator + Global.PREFERENCE_NAME + File.separator
						+ directoryName);
		if (!directory.exists())
			return listName;
		// get all the files from a directory
		File[] fList = directory.listFiles();
		if (fList.length <= 0)
			return listName;
		for (File file : fList) {
			if (file.isFile() && file.exists()) {
				listName.add(file.getName());
			}
		}
		return listName;
	}

	/**
	 * Load list stamp from server
	 */
	private void loadStampAPI() {
		Global.listStampModel = new ArrayList<StampModel>();
		LoadStampAPI stamp = new LoadStampAPI(this, Global.listStampModel);
		RequestParams request = new RequestParams();
		int user_id = mSession.getUserId();
		String token = mSession.getToken();
		request.put(Params.PARAM_USER_ID_, user_id);
		request.put(Params.PARAM_TOKEN_, token);
		stamp.setParams(request);
		stamp.onRunButtonPressed();
	}

	private void getBitmapStamp() {
		for (int i = 0; i < Global.listStampModel.size(); i++) {
			Bitmap bitmap = LoadSaveImage.getImageFromSDCard(
					Params.PARAM_STAMP,
					String.valueOf(Global.listStampModel.get(i).getStampId()));
			if (bitmap == null) {
				ShowMessage.showMessage(getApplicationContext(),
						ShowMessage.ERR_LOAD_STAMP);
				return;
			} else {
				Global.listStampModel.get(i).setBitmap(bitmap);
			}
		}
		btnDownloadStamp.setVisibility(View.GONE);
		stampAdapter = new StampAdapter(getApplicationContext(),
				Global.listStampModel);
		listStamp.setAdapter(stampAdapter);
	}

	public void downloadStamps() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(this.getString(R.string.loading));
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCanceledOnTouchOutside(false);

		AsyncTask<Void, String, String> downloadTask = new AsyncTask<Void, String, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mProgressDialog.show();
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				mProgressDialog.setProgress(Integer.parseInt(values[0]));
			}

			@Override
			protected String doInBackground(Void... params) {
				int count = 0;
				for (int i = 0; i < Global.listStampModel.size(); i++) {
					Bitmap bitmap = null;
					String image_name = String.valueOf(Global.listStampModel
							.get(i).getStampId());
					bitmap = LoadSaveImage.getImageFromSDCard(
							Params.PARAM_STAMP, image_name);
					if (bitmap == null) {
						bitmap = LoadSaveImage.getThumbImage(
								Global.listStampModel.get(i).getUrl(),
								Params.PARAM_STAMP, image_name);
						Global.listStampModel.get(i).setBitmap(bitmap);
					}
					count++;
					publishProgress(""
							+ (int) ((count * 100) / Global.listStampModel
									.size()));
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				mProgressDialog.dismiss();
				btnDownloadStamp.setVisibility(View.GONE);
				stampAdapter = new StampAdapter(getApplicationContext(),
						Global.listStampModel);
				listStamp.setAdapter(stampAdapter);
				messageAdapter.notifyDataSetChanged();
			}
		};
		downloadTask.execute();
	}

	public void hiddenKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textMessage.getWindowToken(), 0);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View view = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (view instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
							.getBottom())) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}
		return ret;
	}

	@Override
	public void onBackPressed() {
		findViewById(R.id.btn_chatOption).setBackgroundResource(
				R.drawable.option_chat_icon);
		findViewById(R.id.showEmo).setBackgroundResource(
				R.drawable.emotion_icon);
		if (emoGroup != null && emoGroup.isShown())
			emoGroup.setVisibility(View.GONE);
		else if (optionGroup != null && optionGroup.isShown())
			optionGroup.setVisibility(View.GONE);
		else {
			overridePendingTransition(R.anim.diagslide_enter_back,
					R.anim.diagslide_leave_back);
			super.onBackPressed();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendBtn:
			sendText();
			break;

		case R.id.btn_chatOption:
			findViewById(R.id.btn_chatOption).setBackgroundResource(
					R.drawable.option_chat_icon_selected);
			findViewById(R.id.showEmo).setBackgroundResource(
					R.drawable.emotion_icon);
			emoGroup.setVisibility(View.GONE);
			optionGroup.setVisibility(View.VISIBLE);
			hiddenKeyboard();
			break;

		case R.id.chatOptionCamera:
			optionCamera();
			break;

		case R.id.chatOptionPresent:
			optionPresent();
			break;

		case R.id.chatOptionOther:
			optionOther();
			break;

		case R.id.showEmo:
			findViewById(R.id.showEmo).setBackgroundResource(
					R.drawable.emotion_icon_selected);
			findViewById(R.id.btn_chatOption).setBackgroundResource(
					R.drawable.option_chat_icon);
			optionGroup.setVisibility(View.GONE);
			emoGroup.setVisibility(View.VISIBLE);
			hiddenKeyboard();
			break;

		case R.id.chatET:
			if (emoGroup != null)
				emoGroup.setVisibility(View.GONE);
			if (optionGroup != null)
				optionGroup.setVisibility(View.GONE);
			findViewById(R.id.btn_chatOption).setBackgroundResource(
					R.drawable.option_chat_icon);
			findViewById(R.id.showEmo).setBackgroundResource(
					R.drawable.emotion_icon);
			break;
		case R.id.btn_download_stamp:
			btnDownloadStamp.setVisibility(View.GONE);
			downloadStamps();
			break;
		default:
			break;
		}
	}

	private void captureImage() {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File f = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"temp.jpg");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support capturing images!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void selectImage() {
		try {
			Intent intent2 = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent2, CAMERA_SELECT_IMAGE_REQUEST_CODE);
			
			
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support capturing images!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public void optionCamera() {
		String[] options = getResources().getStringArray(R.array.camera_option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.btn_chatOptionCamera));
		builder.setNegativeButton(mContext.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							captureImage();
							break;
						case 1:
							selectImage();
							break;
						}
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// onActivityResult = true;
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == Activity.RESULT_OK) {
				loadTempImage();
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled Image capture
				ShowMessage.showMessage(mContext,
						getString(R.string.CAPTURE_USER_CANCEL));
			} else {
				// failed to capture image
				ShowMessage.showDialog(mContext, getString(R.string.ERR_TITLE),
						getString(R.string.CAPTURE_ERROR));
			}
			break;

		case CAMERA_SELECT_IMAGE_REQUEST_CODE:
			if (resultCode == Activity.RESULT_OK) {
				long timeNowEpoch = System.currentTimeMillis();
				Uri selectedImage = data.getData();
				String[] filePath = { MediaStore.Images.Media.DATA };
				Cursor c = getContentResolver().query(selectedImage, filePath,
						null, null, null);
				c.moveToFirst();
				int columnIndex = c.getColumnIndex(filePath[0]);
				String picturePath = c.getString(columnIndex);
				c.close();
				File f = new File(picturePath);
				Bitmap bitmap = LoadSaveImage.decodeFile(f, 250, 250);
				String image_url = LoadSaveImage.saveThumbImage(
						bitmap,
						Params.CHAT_IMAGE + File.separator
								+ Global.userInfo.getUserName(), ""
								+ timeNowEpoch);
				uploadImageChat(image_url, timeNowEpoch);

			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled choose image
				ShowMessage.showMessage(mContext,
						getString(R.string.CHOOSE_USER_CANCEL));
			} else {
				// failed to capture image
				ShowMessage.showDialog(mContext, getString(R.string.ERR_TITLE),
						getString(R.string.CHOOSE_ERROR));
			}
			break;
		}
	}

	private void loadTempImage() {
		long timeNowEpoch = System.currentTimeMillis();
		File f = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		for (File temp : f.listFiles()) {
			if (temp.getName().equals("temp.jpg")) {
				f = temp;
				break;
			}
		}
		// save image
		Bitmap bitmap = LoadSaveImage.getBitmap(f.getAbsolutePath());
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(f.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_UNDEFINED);

		Matrix matrix = new Matrix();
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.postRotate(90);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.postRotate(180);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.postRotate(270);
			break;
		default:
			break;
		}
		f.delete();
		Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		String image_url = LoadSaveImage.saveThumbImage(
				oriented,
				Params.CHAT_IMAGE + File.separator
						+ Global.userInfo.getUserName(), "" + timeNowEpoch);
		uploadImageChat(image_url, timeNowEpoch);
	}

	private void uploadImageChat(String filePath, long timeCreated) {
		if (filePath == null || filePath == "") {
			ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
					getString(R.string.ERR_FILE_NOT_FOUND));
			return;
		} else {
			File image = new File(filePath);
			if (!image.exists()) {
				ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
						getString(R.string.ERR_FILE_NOT_FOUND));
				return;
			}

			RequestParams params = new RequestParams();
			try {
				params.put(Params.PARAM_IMAGE, image);
				params.put(Params.PARAM_USER_ID_, mSession.getUserId());
				params.put(Params.PARAM_TOKEN_, mSession.getToken());
				// mImageModel = null;
				// mImageModel.setTimeCreated(timeCreated);
				UploadImageChatAPI upload_file = new UploadImageChatAPI(
						ChatDetailActivity.this, this, null, timeCreated);
				upload_file.setmParams(params);
				upload_file.onRunButtonPressed();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void optionPresent() {
		final int[] options = getResources().getIntArray(R.array.point_option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_present));
		builder.setNegativeButton(mContext.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
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
						Log.v(TAG, "point=" + options[which]);
						// sendPoints(options[which]);
						dialog.dismiss();
						final int point = options[which];
						dialogSendPoint = Global.customDialog(
								ChatDetailActivity.this,
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
	}

	private void optionOther() {
		// de xac dinh da folow hay chua
		if (Global.listFavorite.size() > 0) {
			isFavorite = false;
			for (UserModel user : Global.listFavorite) {
				if (user.getUserId() == chatUser.getUserId()) {
					isFavorite = true;
					break;
				}
			}
		}
		if (Global.listUserBlocked.size() >= 0) {
			isBlocked = false;
			for (Integer entry : Global.listUserBlocked) {
				// Log.v(TAG, "user blocked " + entry.getKey());
				if (entry == chatUser.getUserId()) {
					isBlocked = true;
					break;
				}
			}
		}

		options = getResources().getStringArray(R.array.other_option);
		if (isFavorite) {
			options = getResources().getStringArray(
					R.array.other_option_unfollow);
		}
		if (isBlocked) {
			options[1] = getString(R.string.unblocked_user);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.btn_chatOptionOther));
		builder.setNegativeButton(mContext.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:// add favorite
							favoriteUser();
							break;
						case 1:
							// block user
							// blockOrUnblockUser();
							if (!isBlocked) {
								dialogBlock = Global.customDialog(
										ChatDetailActivity.this,
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
										ChatDetailActivity.this,
										getString(R.string.sure_unblock),
										new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												// TODO Auto-generated method
												dialogBlock.dismiss();
												blockOrUnblockUser();
											}
										});
							}

							break;
						case 2:
							reportUser(options[which]);
							break;
						}
						dialog.dismiss();
					}

				});
		builder.show();
	}

	private void favoriteUser() {
		// RequestParams request = new RequestParams();
		// request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		// request.put(Params.PARAM_TOKEN_, mSession.getToken());
		// request.put(Params.PARAM_USER_ID, chatUser.getUserId());
		// FavoriteAPI apiFavorite;
		// if (!isFavorite)
		// apiFavorite = new FavoriteAPI(ChatActivity.this, ChatActivity.this,
		// FavoriteAPI.ADD, chatUser);
		// else
		// apiFavorite = new FavoriteAPI(ChatActivity.this, ChatActivity.this,
		// FavoriteAPI.REMOVE, chatUser);
		// apiFavorite.setParams(request);
		// apiFavorite.onRunButtonPressed();

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
									chatUser.getUserId());
							apiFavorite = new FavoriteAPI(
									ChatDetailActivity.this,
									ChatDetailActivity.this, FavoriteAPI.ADD,
									chatUser);
							apiFavorite.setParams(request);
							apiFavorite.onRunButtonPressed();
							// Global.isRequestingFavorite1User = true;
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
									chatUser.getUserId());
							apiFavorite = new FavoriteAPI(
									ChatDetailActivity.this,
									ChatDetailActivity.this,
									FavoriteAPI.REMOVE, chatUser);
							apiFavorite.setParams(request);
							apiFavorite.onRunButtonPressed();
							// Global.isRequestingFavorite1User = true;
						}
					});
		}

	}

	private void executeReportUser(String content) {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_USER_ID, chatUser.getUserId());
		request.put(Params.PARAM_CONTENT, content);
		ReportUserAPI report = new ReportUserAPI(this);
		report.setParams(request);
		report.onRunButtonPressed();
	}

	private void reportUser(String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title
				+ getString(R.string.text_define_max_characters,
						Global.MAX_LENGTH));
		final EditText report = new EditText(mContext);
		report.setMaxLines(Global.MAX_LINE);
		report.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				Global.MAX_LENGTH) });
		report.setFocusable(true);
		report.setFocusableInTouchMode(true);
		report.requestFocus();
		builder.setView(report);
		builder.setNegativeButton(mContext.getString(R.string.btn_cancel),
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindService();
	}

	@Override
	public void finish() {
		// Global.updateLocation();
		Global.chatDetailActivity = null;
		super.finish();
	}

	public void onItemClick(View v) {
		switch (v.getId()) {
		case R.id.emotationTab:
			btnDownloadStamp.setVisibility(View.GONE);
			((Button) findViewById(R.id.emotationTab))
					.setBackgroundColor(getResources().getColor(
							R.color.text_edit));
			((Button) findViewById(R.id.emotationTab))
					.setTextColor(getResources().getColor(R.color.white));
			((Button) findViewById(R.id.stampTab))
					.setBackgroundColor(getResources().getColor(
							R.color.list_background));
			((Button) findViewById(R.id.stampTab)).setTextColor(getResources()
					.getColor(R.color.text_edit));
			listStamp.setVisibility(View.GONE);
			findViewById(R.id.list_emo).setVisibility(View.VISIBLE);

			break;
		case R.id.stampTab:
			((Button) findViewById(R.id.stampTab))
					.setBackgroundColor(getResources().getColor(
							R.color.text_edit));
			((Button) findViewById(R.id.stampTab)).setTextColor(getResources()
					.getColor(R.color.white));
			((Button) findViewById(R.id.emotationTab))
					.setBackgroundColor(getResources().getColor(
							R.color.list_background));
			((Button) findViewById(R.id.emotationTab))
					.setTextColor(getResources().getColor(R.color.text_edit));
			listStamp.setVisibility(View.VISIBLE);
			findViewById(R.id.list_emo).setVisibility(View.GONE);
			if (checkStampExist(Params.PARAM_STAMP).size() <= 0) {
				btnDownloadStamp.setVisibility(View.VISIBLE);
			} else {
				btnDownloadStamp.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void handleReceiveData() {
		isFavorite = !isFavorite;
		// send a message to user favorite if he/she enable notice when someone
		// favorite
		if (isFavorite) {
			Global.listFavoriteId.add(chatUser.getUserId());
		} else {
			Global.listFavoriteId.remove(Integer.valueOf(chatUser.getUserId()));
		}
		Global.savePref(editor, Params.PREF_LIST_FAVORITE_ID,
				Global.listFavoriteId);
	}

	@Override
	public void handleGetList() {
		// if (mImageModel != null) {
		// sendImage(mImageModel);
		// mImageModel = null;
		// } else {
		swipeContainer.setRefreshing(false);
		// load message success
		if (lastMessageId > 0)
			mPage++;
		if (all_message != null && all_message.size() > 0) {
			lastMessageId = all_message.get(0).getMessageId();
			mConversationId = all_message.get(0).getConversationId();
		}
		if (mPage == 1 && !isReconnectInternet) {
			// mPage = 1 => first time load chat => read all in this
			// conversation
			// isReconnectInternet =true=> sendReadText ko the send len vi
			// mservice chua authenticate
			// => sendReadText call trong receiver onchatservercreconnect
			sendReadText();

		}
		// final int distance = all_message.size() -
		// Global.LIMIT_MESSAGE_IN_CHAT
		// * (mPage - 1);
		final int distance = all_message.size() - totalMessLoad;
		totalMessLoad = all_message.size();
		// final int savePosition = listview.getFirstVisiblePosition();
		messageAdapter.notifyDataSetChanged();
		listview.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				listview.setSelection(distance);

			}
		});
		listview.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						// return true: draw content in listview to user
						// return false: not draw
						// when add or del message. notifydataset make
						// listview sometiem flash move from x->y
						// so use onPreDraw to only show content after falsh
						// move finish
						int firstVisible = listview.getFirstVisiblePosition();
						int lastVisible = listview.getLastVisiblePosition();
						boolean valueRetrun = false;
						if (firstVisible == 0
								&& lastVisible == all_message.size() - 1) {
							// chat too short, show all in listview
							// =>first time start chat activity and number
							// chat message small
							valueRetrun = true;
						}
						if (firstVisible != 0 && firstVisible <= distance) {
							// scroll up and load moreold message
							valueRetrun = true;
						}

						if (valueRetrun) {
							listview.getViewTreeObserver()
									.removeOnPreDrawListener(this);
							return true;
						} else
							return false;
					}
				});

		// }
	}

	public void loadMessageFail() {
		swipeContainer.setRefreshing(false);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (emoGroup != null && emoGroup.isShown())
			emoGroup.setVisibility(View.GONE);
		else if (optionGroup != null && optionGroup.isShown())
			optionGroup.setVisibility(View.GONE);
		findViewById(R.id.btn_chatOption).setBackgroundResource(
				R.drawable.option_chat_icon);
		findViewById(R.id.showEmo).setBackgroundResource(
				R.drawable.emotion_icon);
		return false;
	}

}
