package com.cls.sugutomo.chatlist;

import java.util.HashMap;
import java.util.Vector;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.adapter.EndlessScrollListener;
import com.cls.sugutomo.adapter.UserAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.ChatListAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.utils.Global;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.loopj.android.http.RequestParams;

public class ChatListActivityServer extends BaseTabActivity implements
		APICallbackInterface {
	public static boolean LOAD_FINISH = true;
	public static int LIMIT_PAGE_CHATLIST = 20;
	public static boolean canLoadMore = true;
	private static final String TAG = ChatListActivityServer.class
			.getSimpleName();
	public UserAdapter adapter;
	private Handler mHandler = new Handler();
	private ProgressDialog progress;
	public static SwipeListView swipelistview;
	private Vector<MessageModel> mListMessage = new Vector<MessageModel>();
	private HashMap<String, String> mListMessageKey = new HashMap<String, String>();

	private SwipeRefreshLayout swipeContainer;
	private int mPage = 1;
	private String title;
	private BroadcastReceiver mReceiver, mReceiverReconnect;
	private RelativeLayout txtNotice;
	public boolean isPausing = false;
	private ChatType mChatType;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mChatType = ChatType.getInstance(this);
		canLoadMore = true;
		LOAD_FINISH = true;
		mContext = this;
		// add layout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.chat_list_activity,
				null, false);
		frameLayout.addView(activityView);
		txtNotice = (RelativeLayout) findViewById(R.id.chatlist_null_lo);
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshChatList);
		// Setup refresh listener which triggers new data loading
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (Global.checkInternet(mContext)) {
					Global.getUnReadMessageTotal(ChatListActivityServer.this);
					new PullToRefreshDataTask().execute();
				} else {
					swipeContainer.setRefreshing(false);
					ShowMessage.showDialog(mContext,
							getString(R.string.ERR_TITLE),
							getString(R.string.ERR_CONNECT_FAIL));
				}
			}
		});
		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		// set title
		Intent intent = getIntent();
		if (intent != null) {
			title = intent.getExtras().getString(Params.PARAM_TITLE);
			setTitle(title);
		}

		// swipe listview and adapter
		swipelistview = (SwipeListView) findViewById(R.id.chat_swipe_list);
		adapter = new UserAdapter(this, R.layout.chat_list_adapter,
				mListMessage);
		swipelistview.setAdapter(adapter);
		swipelistview.setOnScrollListener(new EndlessScrollListener() {

			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				// Triggered only when new data needs to be appended to the list
				// Add whatever code is needed to append new items to your
				// AdapterView
				Log.v("", "page" + page + ": " + totalItemsCount);
				if (!canLoadMore || !LOAD_FINISH)
					return;
				customLoadMoreDataFromApi(page);
				// or customLoadMoreDataFromApi(totalItemsCount);
			}
		});
		swipelistview.setSwipeListViewListener(new BaseSwipeListViewListener() {
			@Override
			public void onOpened(int position, boolean toRight) {
			}

			@Override
			public void onClosed(int position, boolean fromRight) {
			}

			@Override
			public void onListChanged() {
				swipelistview.closeOpenedItems();
				swipelistview.invalidateViews();
			}

			@Override
			public void onMove(int position, float x) {

			}

			@Override
			public void onStartOpen(int position, int action, boolean right) {
			}

			@Override
			public void onStartClose(int position, boolean right) {

			}

			@Override
			public void onClickFrontView(int position) {
				MessageModel user = mListMessage.elementAt(position);
				Intent intent = new Intent(ChatListActivityServer.this,
						ChatDetailActivity.class);
				intent.putExtra(Params.USER, user.getUserInfo());
				// TODO
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(R.anim.diagslide_enter,
						R.anim.diagslide_leave);
			}

			@Override
			public void onClickBackView(int position) {
				swipelistview.closeAnimate(position);
				swipelistview.closeOpenedItems();
			}

			@Override
			public void onDismiss(int[] reverseSortedPositions) {

			}
		});
		swipelistview.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
		swipelistview.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
		swipelistview.setSwipeCloseAllItemsWhenMoveList(true);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		swipelistview.setOffsetLeft(width - convertDpToPixel(80)); // left side
																	// offset
		swipelistview.setAnimationTime(500);

		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.loading));
		progress.setCanceledOnTouchOutside(false);
		Global.chatListActivityServer = this;
		Global.CHATLIST_NEED_REFRESH = false;
		getListChat(true, mPage);
	}

	public void checkNotice() {
		if (mListMessage == null || mListMessage.size() == 0) {
			txtNotice.setVisibility(View.VISIBLE);
		} else {
			txtNotice.setVisibility(View.GONE);
		}

	}

	private class PullToRefreshDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onCancelled() {
			swipeContainer.setRefreshing(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			canLoadMore = true;
			mPage = 1;
			mListMessage.clear();
			mListMessageKey.clear();
			getListChat(true, mPage);
			adapter.notifyDataSetChanged();
			swipeContainer.setRefreshing(false);
		}
	}

	public int convertDpToPixel(float dp) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return (int) px;
	}

	public static final int FROM_CHAT_ACTIVITY = 1;
	public static final int FROM_CHAT_SERVICE_DELETE_MESSAGE = 2;
	public static final int FROM_CHAT_SERVICE_DELETE_CONVERSATION = 3;
	public static final int FROM_CHAT_SERVICE_NEW_MESSAGE = 4;

	public void notifyAdapter(final int type, final MessageModel message) {
		mHandler.post(new Runnable() {
			public void run() {
				Intent intent = new Intent(getPackageName()
						+ Params.PARAM_BROADCAST);
				ChatListActivityServer.this.sendBroadcast(intent);
				switch (type) {
				case FROM_CHAT_SERVICE_DELETE_MESSAGE:
				case FROM_CHAT_SERVICE_DELETE_CONVERSATION: {
					// update lai chatlist. ko tu sua duoc vi chatlistAPI ko tra
					// ve messageID nao duoc xoa,...
					// tam thoi ko lam gi. Vi reload lai listChat cung ko hay
					// del convesaion: user A del convesation x thi x xoa khoi
					// chatlist cua A luon => A ko can refresh listChat
					// user B neu co refresh listChat thi conversation x luk nay
					// con toan message cua b=> KO Can refresh
					// ttuong tu voi delete message
					// o day neu can chinh xac thi phai reload de cap nhat lai
					// new message
					break;
				}
				case FROM_CHAT_SERVICE_NEW_MESSAGE: {
					// update va dua len top neu da co trong mListmessage
					// neu chua thi add them tren top
					// Log.v("", "msg:" + message.getMessage());
					MessageModel model = message;
					String typeModel = model.getType();
					if (typeModel.equals(ChatType.SEND_SUCCESS)) {
						String typeInData = Global.convertType(mChatType
								.getTypeInData(model.getMessage()));
						model.setType(typeInData);
						model.setMessage(ChatDetailActivity.messageToSend);
						// sender is me
						model.setLastSender(Global.userInfo.getUserName());
					} else {
						model.setType(Global.convertType(model.getType()));
						model.setMessage(message.getMessage());
						model.setLastSender(model.getUserInfo().getUserName());
					}
					long id = message.getConversationId();
					if (id > 0) {
						String convesationID = String.valueOf(message
								.getConversationId());
						addAndRefresh(convesationID, model);
					}
					break;
				}
				case FROM_CHAT_ACTIVITY: {// MINH SEND MESSAGE TO SOMEONE
					break;
				}
				default:
					break;
				}
			}
		});
	}

	public void updateFromChat(final String convesationID) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mListMessageKey.containsKey(convesationID)) {
					for (int i = mListMessage.size() - 1; i >= 0; i--) {
						String id = String.valueOf(mListMessage.get(i)
								.getConversationId());
						if (id.equals(convesationID)) {
							// set read =0
							mListMessage.get(i).setNewMessages(0);
							adapter.notifyDataSetChanged();
							break;
						}
					}
				}
			}
		});
	}

	/**
	 * some time total unread va total unread tung cai trong chatlist ko chuan
	 * la vi cai nay, moi lan minh chi add unread them 1, truong hop lost push
	 * message oepnfrie thi se sai vi APIgetUnread tra ve dung, nhung push ve ko
	 * du? de add them +1 => fix: moi khi co ket noi internet chatlist nhan duoc
	 * broastcat reconnect internet => khi do minh reload chatlist
	 * 
	 * @param convesationID
	 * @param model
	 */
	private void addAndRefresh(String convesationID, MessageModel model) {
		if (mListMessageKey.containsKey(convesationID)) {
			for (int i = mListMessage.size() - 1; i >= 0; i--) {
				String id = String.valueOf(mListMessage.get(i)
						.getConversationId());
				long oldTime = mListMessage.get(i).getDateMessage();
				if (id.equals(convesationID)
						&& oldTime < model.getDateMessage()) {
					// Log.v(TAG, convesationID+" :timeupdate:"+oldTime
					// +"<"+model.getDateMessage());
					// remove it , readd to top
					//if (!model.getType().equals(ChatType.SEND_SUCCESS))
					if (!model.getLastSender().equals(Global.userInfo.getUserName()))
						model.setNewMessages(mListMessage.get(i)
								.getNewMessages() + 1);
					else
						model.setNewMessages(0);// da send mess thi mac dinh la
												// minh da doc het mess doi
												// phuong gui truoc do

					model.setConversationId(Long.parseLong(convesationID));
					mListMessage.remove(i);
					mListMessage.add(0, model);
					adapter.notifyDataSetChanged();
					// Toast.makeText(this, "update"+i,
					// Toast.LENGTH_LONG).show();
					break;
				}
			}
		} else {
			model.setNewMessages(1);
			mListMessage.add(0, model);
			mListMessageKey.put(convesationID, convesationID);
			adapter.notifyDataSetChanged();
		}
	}

	public void deleteConversationSuccess(long conversationId) {
		for (int i = mListMessage.size() - 1; i >= 0; i--) {
			if (mListMessage.get(i).getConversationId() == conversationId) {
				mListMessageKey.remove(String.valueOf(mListMessage.get(i)
						.getConversationId()));
				mListMessage.remove(i);
				// Toast.makeText(this, ""+i, Toast.LENGTH_LONG).show();
				break;
			}
		}
		adapter.notifyDataSetChanged();
		// adapter = new UserAdapter(this, R.layout.chat_list_adapter,
		// mListMessage);
		// swipelistview.setAdapter(adapter);

		swipeContainer.setRefreshing(false);
		checkNotice();
		// update new unread message
		Global.getUnReadMessageTotal(this);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isPausing = true;
		if (mReceiver != null)
			this.unregisterReceiver(mReceiver);
		if (mReceiverReconnect != null)
			this.unregisterReceiver(mReceiverReconnect);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPausing = false;
		Global.getUnReadMessageTotal(this);
		if (Global.CHATLIST_NEED_REFRESH) {
			// = true when recevier broadcast reconnect to intenet in
			// bastTabactivity
			// set = false in onCreat because oncreat we alway recreate new
			// activity
			// but when comback to top activity stack we need check update
			resetChatListParam();
			Global.CHATLIST_NEED_REFRESH = false;
			getListChat(true, mPage);
			Log.v(TAG, "update because PARAM_RECONNECT_INTERNET_CHECK");
		}
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}

		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_UPDATE_CHAT_LIST);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// extract our message from intent
				int type = intent.getExtras().getInt(Params.PARAM_TYPE);
				MessageModel model = (MessageModel) intent.getExtras()
						.getSerializable(Params.PARAM_MESSAGE_MODEL);
				notifyAdapter(type, model);
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
		onReceiveReconnectInternet();
	}

	private void onReceiveReconnectInternet() {
		// TODO
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_RECONNECT_INTERNET);
		mReceiverReconnect = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// refresh chatlist prevent lost message openfire
				runOnUiThread(new Runnable() {
					public void run() {
						resetChatListParam();
						getListChat(true, mPage);
						Global.CHATLIST_NEED_REFRESH=false;
						Log.v(TAG, "update because PARAM_RECONNECT_INTERNET");
					}
				});
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiverReconnect, intentFilter);
	}

	private void resetChatListParam() {
		mPage = 1;
		mListMessage.clear();
		mListMessageKey.clear();
		canLoadMore = true;
		if (adapter != null)
			adapter.notifyDataSetChanged();
		swipeContainer.setRefreshing(false);
	}

	@Override
	public void finish() {
		Global.chatListActivityServer = null;
		super.finish();
	}

	public void customLoadMoreDataFromApi(int offset) {
		// This method probably sends out a network request and appends new data
		// items to your adapter.
		// Use the offset value and add it as a parameter to your API request to
		// retrieve paginated data.
		// Deserialize API response and then construct new objects to append to
		// the adapter
		// Toast.makeText(this, "" + offset, Toast.LENGTH_SHORT).show();
		Log.i(TAG, "load more data page=" + offset);
		if (mPage < offset) {
			// Log.i(TAG, "load more data page=" + offset);
			mPage = offset;
			// if ((Global.listFriend.size() % Global.LIMIT_USER_IN_PAGE) == 0)
			// {
			// if (Global.LOAD_MORE_USER) {
			Log.i(TAG, "load more data page=" + offset);
			getListChat(false, 1);
			findViewById(R.id.loadmoreChatList).setVisibility(View.VISIBLE);
			// } else {
			// Log.i(TAG, "load het userlist roi" + offset);
			// }
		}
	}

	private void getListChat(boolean useProgressbar, int multiPage) {
		// runOnUiThread for not forclose in pull to refresh...
		// TODO Auto-generated method stub
		ChatListActivityServer.LOAD_FINISH = false;
		ChatListAPI api = new ChatListAPI(ChatListActivityServer.this,
				ChatListActivityServer.this, mListMessage, mListMessageKey);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_,
				SessionManager.getInstance(ChatListActivityServer.this)
						.getUserId());
		request.put(Params.PARAM_TOKEN_,
				SessionManager.getInstance(ChatListActivityServer.this)
						.getToken());
		if (multiPage > 1) {
			// reload lai tong so page da load truoc do
			request.put(Params.PAGE, 1);
			mListMessage.clear();
			mListMessageKey.clear();
			canLoadMore = true;
			adapter.notifyDataSetChanged();
			swipeContainer.setRefreshing(false);
		} else {
			request.put(Params.PAGE, mPage);
		}
		request.put(Params.LIMIT, multiPage * LIMIT_PAGE_CHATLIST);
		api.setParams(request);
		api.setUseProgressbar(useProgressbar);
		api.onRunButtonPressed();
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		// Log.v("", "size key" + mListMessageKey.size());
		adapter.notifyDataSetChanged();
		if (mListMessage == null || mListMessage.size() == 0) {
			txtNotice.setVisibility(View.VISIBLE);
		} else {
			txtNotice.setVisibility(View.GONE);
		}
		findViewById(R.id.loadmoreChatList).setVisibility(View.GONE);
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}
}
