package com.cls.sugutomo.chat;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.cls.sugutomo.BuyPointActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.RankActivity;
import com.cls.sugutomo.SplashScreenActivity;
import com.cls.sugutomo.api.AddPushMessageToServerAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.dialog.MessageDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.userlist.UserListActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class ChatBackgroundService extends Service {

	private static final String TAG = ChatBackgroundService.class
			.getSimpleName();
	public static final String HOST = Global.CHAT_SERVER;
	public static final int PORT = Global.CHAT_SERVER_PORT;
	public static final String SERVICE = "Main";
	private final Binder mBinder = new ChatServiceBinder();
	private XMPPConnection mConnection = null;
	private boolean mIsNeedRun = false;
	private DatabaseHandler mDatabase;
	private ChatType mChatType;
	private NotificationManager mNotificationManager;
	private Map<Integer, Integer> countMess = new HashMap<Integer, Integer>();
	private LocalBroadcastManager broadcaster;
	private long mCheckAddConversationId;
	private UserModel chatUser;
	private long conversationId;
	public static UserModel userStartService;
	private LogInOutReceiver receiver;
	private Handler handler;
	private NotificationCompat.Builder mBuilder;

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		mDatabase = DatabaseHandler.getInstance(this);
		mChatType = ChatType.getInstance(this);
		// loginToChat();
		Log.d(TAG, "Service is created");
		broadcaster = LocalBroadcastManager.getInstance(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(
				this);
		if (Global.userInfo != null)
			userStartService = Global.userInfo;
		else
			Global.appInBackGround = true;

	}

	public class ChatServiceBinder extends Binder {
		public ChatBackgroundService getService() {
			return ChatBackgroundService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// return super.onStartCommand(intent, flags, startId);
		loginToChat();
		return START_STICKY;// auto recreate if kill
	}

	private void stopService() {
		if (mConnection != null && mConnection.isConnected()) {
			mConnection.disconnect();
		}
		stopSelf();
	}

	public void registerReceiver() {
		IntentFilter filter = new IntentFilter(getPackageName()
				+ Params.AUTHENTICATE);
		filter.addAction(getPackageName() + Params.STOP_SERVICE);

		receiver = new LogInOutReceiver();
		registerReceiver(receiver, filter);
	}

	public void unregisterReceiverManual() {
		unregisterReceiver(receiver);
	}

	/**
	 * Called by Settings dialog when a connection is establised with the XMPP
	 * server
	 * 
	 * @param connection
	 */
	public void setConnection(XMPPConnection connection, final String username) {
		mConnection = connection;
		if (mConnection != null) {

			/**
			 * RECEIVING MESSAGES
			 */
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						// saveMessageToDatabase(message, username);
						handleMessageFromServer(message, username);
					}
				}

			}, filter);
		}
	}

	public boolean isAuthenticated() {
		if (mConnection != null && mConnection.isAuthenticated())
			return true;
		else
			return false;
	}

	public void reConenect() {
		loginToChat();
	}

	public void sendMessage(Message msg) {
		mConnection.sendPacket(msg);
	}

	public void setUserInfo(UserModel m) {
		if (m != null)
			userStartService = m;
	}

	private void sendFootprintBroadcast(String username) {
		Intent i = new Intent(getPackageName() + Params.PARAM_FOOTPRINT);
		i.putExtra(Params.PARAM_USERNAME, username);
		this.sendBroadcast(i);
	}

	private void sendBroadCast(Message message) {
		String messageBody = message.getBody();
		String type = mChatType.receiveJSON(messageBody);
		if (type != null) {
			if (!type.equals(ChatType.ERROR_1000)
					&& !type.equals(ChatType.ERROR_1001)
					&& !type.equals(ChatType.SEND_SUCCESS)) {
				Intent i = new Intent(getPackageName() + Params.PARAM_BROADCAST);
				i.putExtra(Params.MESSAGE, mChatType.getData(messageBody)
						.toString());
				this.sendBroadcast(i);
			}
		} else
			return;
	}

	private boolean getValuePref(String key) {
		SharedPreferences pref;
		try {
			pref = this.getSharedPreferences(Global.PREFERENCE_NAME,
					Context.MODE_PRIVATE);
			return pref.getBoolean(key, true);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void checkFootPrint(String messageBody) {
		Log.i(TAG, "receive footprint");
		// get lai chat user vi` format response hoi khac
		chatUser = getUserinfoFootPrint(messageBody);
		// neu setting theo tung user thi them
		// Params.SETTING_NOTIFYCATION_FOOTPRINT + userid
		// nhung phai check xem luk nay global.userinfouserid khac null ko
		boolean value = getValuePref(Params.SETTING_NOTIFYCATION_FOOTPRINT);
		if (!Global.listBlockedOrBeBlocked.contains(chatUser.getUserId())
				&& value) {
			sendFootprintBroadcast(chatUser.getName());
		} else {
			Log.v("", "user disable footprint ");
		}
	}

	private void addBlockUser() {
		Log.i(TAG, "some one block me ");
		if (Global.listBlockedOrBeBlocked != null) {
			Global.listBlockedOrBeBlocked.add(chatUser.getUserId());
			SharedPreferences pref = this.getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			Global.savePref(pref.edit(),
					Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
					Global.listBlockedOrBeBlocked);
		}
	}

	private void unBlockUser() {
		Log.i(TAG, "some one unblock block me ");
		if (Global.listBlockedOrBeBlocked != null) {
			Global.listBlockedOrBeBlocked.remove(Integer.valueOf(chatUser
					.getUserId()));
			SharedPreferences pref = this.getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			Global.savePref(pref.edit(),
					Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
					Global.listBlockedOrBeBlocked);
		}
	}

	private void deleteConversation(String fromName) {
		Log.i(TAG, "conversation id = " + conversationId + "to delete");
		// DELETE FROM SERVER
		if (fromName.equals(String.valueOf(Global.ADMIN))) {
			// admin dele converison
			// - xoa convaresion in chatlist(if have)
			// - xoa all chat in chat(if have)
			// - udate unread
		} else {
			// DELETE FROM USER: chi delete messenger cua doi phuong, con
			// mess cua minh trong cuoc hoi thoai do van giu
			// - chat user delete toan bo conversation cua ho voi minh`
			// => reload lai chat or clear message tu chatuser in chat activity
			// (if have)
			// => update chat list hoac xoa lastmessage neu no tu chatuser(if
			// have)
			// => update unread (if need)
		}
		// Intent i = new Intent(getPackageName() +
		// Params.PARAM_UPDATE_CHAT_LIST);
		// i.putExtra(Params.PARAM_TYPE,
		// ChatListActivityServer.FROM_CHAT_SERVICE_DELETE_CONVERSATION);
		// this.sendBroadcast(i);
		// i = new Intent(getPackageName() + Params.PARAM_BROADCAST);
		// this.sendBroadcast(i);
	}

	private void pushBroastcastOrNotificationToServer(
			MessageModel messageModel, String type) {
		// add len server message neu id<= 0
		final MessageModel model = messageModel;
		final int from = ChatType.getInstance(getApplicationContext())
				.executeGetFrom(messageModel.getMessage());
		final String messageChat = ChatType
				.getInstance(getApplicationContext()).executeReceive(
						messageModel.getMessage());
		model.setMessage(messageChat);
		model.setType(type);
		// call api save message to server
		// vi day la push message frm admin or broatcast
		Runnable r = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AddPushMessageToServerAPI api = new AddPushMessageToServerAPI(
						ChatBackgroundService.this,
						Global.chatListActivityServer, model);
				RequestParams request = new RequestParams();
				request.put(Params.PARAM_USER_ID_,
						SessionManager.getInstance(getApplicationContext())
								.getUserId());
				request.put(Params.PARAM_TOKEN_,
						SessionManager.getInstance(getApplicationContext())
								.getToken());
				request.put(Params.PARAM_FROM, from);
				request.put(Params.PARAM_TO, Global.userInfo.getUserId());
				request.put(Params.MESSAGE, messageChat);
				api.setParams(request);
				api.setUseProgressbar(false);
				api.onRunButtonPressed();
			}
		};
		handler.post(r);
	}

	public void handleMessageFromServer(Message message, String username) {
		if (Global.userInfo == null) {
			SharedPreferences pref = this.getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			String myString = pref.getString(Params.PREF_USER_INFO, "");
			if(myString.equals("")) return;
			try {
				UserModel user = Global.setUserInfo(new JSONObject(myString),
						null);
				// Toast.makeText(this, "onRestoreInstanceState",
				// Toast.LENGTH_LONG).show();
				if (user != null) {
					Global.userInfo = user;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (Global.userInfo == null)  return;
		// GET USER SEND MESSAGE
		String fromName = StringUtils.parseBareAddress(message.getFrom());
		fromName = fromName.split("@")[0];
		String toName = StringUtils.parseBareAddress(message.getTo());
		toName = toName.split("@")[0];
		Log.i(TAG, "Received message from = " + fromName + " send to = "
				+ message.getTo());
		Log.i(TAG, "Text Recieved = " + message.getBody());
		// CONVERT BODY TO MESSAGE
		String messageBody = message.getBody();
		chatUser = Global.getFriendById(fromName);
		if (chatUser == null) {
			chatUser = getUserinfo(messageBody);
		}
		// CHECK TYPE OF MESSAGE
		String type = mChatType.receiveJSON(messageBody);
		boolean checkMessage = false;
		if (type != null) {
			checkMessage = mChatType.acceptDenyMessage(type);
		} else
			return;

		conversationId = mChatType.getConversationId(messageBody);

		//Log.i(TAG, "CONVERSATIONID = " + conversationId);
		if (type.equals(ChatType.READ_TEXT)) {
			// pending task read text
			return;
		}
		// RECEIVE MESSAGE FOOTPRINT
		if (type.equals(ChatType.FOOTPRINT)) {
			checkFootPrint(messageBody);
			return;// vi footprint ko duoc luu tren database tren server, no chi
					// show Toast thoi
		}
		if (type.equals(ChatType.BLOCK_USER)) {
			addBlockUser();
			return;
		}
		if (type.equals(ChatType.UNBLOCK_USER)) {
			unBlockUser();
			return;
		}
		if (type.equals(ChatType.FROM_BROADCAST)) {// someone send broast cast->
													// n user(me in list) in
													// sendAllMessageActivity
			if (Global.listBlockedOrBeBlocked != null) {
				if (Global.listBlockedOrBeBlocked.contains(Integer
						.valueOf(chatUser.getUserId()))) {
					// ko nhan broast cast tu nguoi bi minh block
					return;
				}
			}
		}
		if (type.equals(ChatType.NEW_REGISTER)
				|| type.equals(ChatType.FAVORITE) || type.equals(ChatType.RANKING)) {
			if (Global.appInBackGround) {
				buildNotification(message, true);
			} else {
				buildNotification(message, false);
			}
			return;
		}

		// DELETE MESSAGE
		if (type.equals(ChatType.DELETE_MESSAGE)) {
			long messageId = mChatType.getMessageId(messageBody);
			Log.i(TAG, "delete message id = " + messageId);
			if (Global.chatDetailActivity != null
					&& Global.chatDetailActivity.isPausing) {
				// chatListActivityServer not in top stack activity
				Global.chatDetailActivity.deleteMessageFromServer(messageId,
						fromName);
			} else {
				Global.chatDetailActivity.deleteMessageFromServer(messageId,
						fromName);
			}
			// peding task -need doing if api server update chatlist when send delete messagge
			// but now api not update chatlist so we do nothing
			// - update unread(if not read) - api not update unnead when delete
			// mesage so we will not do anything
			// - delete message in chatlist(if it newest) - api not update
			// newest when delete mesage so we will not do anything
			// - delete message va update in chat(if chat activity is create
			// before)
			return;
		} else if (type.equals(ChatType.DELETE_CONVERSATION)) {
			deleteConversation(fromName);
			return;
		}

		// send success
		long id = mChatType.getId(mChatType.getData(messageBody));
		MessageModel messageModel = new MessageModel(conversationId, id,
				Global.userInfo, messageBody, false);
		// get type of data
		String typeInData = mChatType.getTypeInData(messageBody);
		// mCheckAddConversationId from broadcast (send)
		if (typeInData != null) {
			if (typeInData.equals(ChatType.BROADCAST)) {
				if (Global.sendAllMessageActivity != null)
					Global.sendAllMessageActivity.receiveMessage(messageBody,
							checkMessage);
			}
			if (typeInData.equals(ChatType.POINT)) {
				// send point to another user then i get response from server
				// show result when send point to someone
				Intent i = new Intent(getPackageName() + Params.PARAM_GIF_POINT);
				i.putExtra(Params.MESSAGE, message.getBody());
				this.sendBroadcast(i);
			}
		}

		// MESSAGE: NO ERROR
		long messageId = mChatType.getMessageId(mChatType.getData(messageBody));
		// add message to list messages
		messageModel = new MessageModel(conversationId, messageId, chatUser,
				messageBody, false);
		messageModel.setDateMessage(mChatType.getTimeSent(messageBody) * 1000);
		if (Global.userInfo != null && chatUser != null)
			messageModel.setDistance(Global.distanceBetweenGPS(Global.userInfo,
					chatUser, this));
		if (conversationId == 0) {
			// =0 la message chua duoc luu tren server
			// minh se push len de luu tai server, coi no la 1 message chat binh
			// thuong
			// => tra ve conversationId va messageId moi
			// sau khi server tra ve minh se tu buildnotification
			pushBroastcastOrNotificationToServer(messageModel, type);
			return;
		}
		final String messageChat = ChatType
				.getInstance(getApplicationContext()).executeReceive(
						messageModel.getMessage());
		// Log.v(TAG, "messageChat:"+messageChat);
		messageModel.setType(type);
		if (type.equals(ChatType.SEND_SUCCESS)
				|| type.equals(ChatType.ERROR_1000)
				|| type.equals(ChatType.ERROR_1001)) {
			// minh send message thi se nhan duoc la send ok hay ko

			// messageModel.setType(type);
			// messageModel.setMessage(ChatDetailActivity.messageToSend);//succes
			// thi chi tra ve success chu ko tra ve mess
			updateChatAndChatList(messageModel);
		} else {
			// con day la user duoc send message => update notify and ....
			messageModel.setMessage(messageChat);
			updateChatAndChatList(messageModel);
			buildNotifycationDependAppState(message, fromName);
			handler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Global.getUnReadMessageTotal(ChatBackgroundService.this);
				}
			});
		}
	}

	private void buildNotifycationDependAppState(Message message,
			String fromName) {
		if (Global.appInBackGround) {
			buildNotification(message, true);
		} else {
			if (Global.chatDetailActivity != null) {
				if (Global.chatDetailActivity.isPausing) {
					buildNotification(message, false);
				} else {
					if (Global.chatDetailActivity.chatUser != null) {
						if (!fromName.equals(Global.chatDetailActivity.chatUser
								.getUserName())) {
							buildNotification(message, false);
						}
					}
				}
			} else {
				buildNotification(message, false);
			}
		}
	}

	public void updateChatAndChatList(MessageModel model) {

		// update chat activity
		boolean updateChat =false;
		if (Global.chatDetailActivity != null
				&& Global.chatDetailActivity.isPausing) {
			updateChat=Global.chatDetailActivity.updateMessage(model);
		} else if (Global.chatDetailActivity != null) {
			updateChat=Global.chatDetailActivity.updateMessage(model);
			// Intent intent = new Intent(this.getPackageName()
			// + Params.PARAM_UPDATE_CHAT_LIST);
			// intent.putExtra(Params.PARAM_TYPE,
			// ChatListActivityServer.FROM_CHAT_SERVICE_NEW_MESSAGE);
			// intent.putExtra(Params.PARAM_MESSAGE_MODEL, model);
			// this.sendBroadcast(intent);
			// Log.v("", "vao dayyyy service:" + this.getPackageName());
		}
		//Toast.makeText(getApplicationContext(), "udate chat " + updateChat, Toast.LENGTH_SHORT).show();
		Log.v("", "udate chat " + updateChat);
		if (model.getType().equals(ChatType.ERROR_1000)
				|| model.getType().equals(ChatType.ERROR_1001))
			return;

		if (Global.chatListActivityServer != null
				&& Global.chatListActivityServer.isPausing) {
			// chatListActivityServer not in top stack activity
			Global.chatListActivityServer
					.notifyAdapter(
							ChatListActivityServer.FROM_CHAT_SERVICE_NEW_MESSAGE,
							model);
		} else {
			Intent intent = new Intent(this.getPackageName()
					+ Params.PARAM_UPDATE_CHAT_LIST);
			intent.putExtra(Params.PARAM_TYPE,
					ChatListActivityServer.FROM_CHAT_SERVICE_NEW_MESSAGE);
			intent.putExtra(Params.PARAM_MESSAGE_MODEL, model);
			this.sendBroadcast(intent);
			// Log.v("", "vao dayyyy service:" + this.getPackageName());
		}
	}

	/*
	 * public void saveMessageToDatabase(Message message, String username) { //
	 * GET USER SEND MESSAGE String fromName =
	 * StringUtils.parseBareAddress(message.getFrom()); fromName =
	 * fromName.split("@")[0]; String toName =
	 * StringUtils.parseBareAddress(message.getTo()); toName =
	 * toName.split("@")[0]; Log.i(TAG, "Received message from = " + fromName +
	 * " send to = " + message.getTo()); Log.i(TAG, "Text Recieved = " +
	 * message.getBody()); // CONVERT BODY TO MESSAGE String messageBody =
	 * message.getBody(); chatUser = Global.getFriendById(fromName); if
	 * (chatUser == null) { chatUser = getUserinfo(messageBody); } // CHECK TYPE
	 * OF MESSAGE String type = mChatType.receiveJSON(messageBody); boolean
	 * checkMessage = false; if (type != null) { checkMessage =
	 * mChatType.acceptDenyMessage(type); } else return;
	 * 
	 * if (!checkMessage) { return; } conversationId =
	 * mChatType.getConversationId(messageBody);
	 * 
	 * Log.i(TAG, "CONVERSATIONID = " + conversationId); if
	 * (type.equals(ChatType.READ_TEXT)) { // peding task read text return; } //
	 * RECEIVE MESSAGE FOOTPRINT if (type.equals(ChatType.FOOTPRINT)) {
	 * Log.i(TAG, "receive footprint"); // get lai chat user vi` format response
	 * hoi khac chatUser = getUserinfoFootPrint(messageBody); // neu setting
	 * theo tung user thi them // Params.SETTING_NOTIFYCATION_FOOTPRINT + userid
	 * // nhung phai check xem luk nay global.userinfouserid khac null ko
	 * boolean value = getValuePref(Params.SETTING_NOTIFYCATION_FOOTPRINT); if
	 * (!Global.listBlockedOrBeBlocked.contains(chatUser.getUserId()) && value)
	 * { sendFootprintBroadcast(chatUser.getName()); } else { Log.v("",
	 * "user disable footprint "); } return; } if
	 * (type.equals(ChatType.BLOCK_USER)) { // Log.i(TAG, "some one block me ");
	 * if (Global.listBlockedOrBeBlocked != null) {
	 * Global.listBlockedOrBeBlocked.add(chatUser.getUserId());
	 * SharedPreferences pref = this.getSharedPreferences(
	 * Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
	 * Global.savePref(pref.edit(), Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
	 * Global.listBlockedOrBeBlocked); } } if
	 * (type.equals(ChatType.UNBLOCK_USER)) { // UserModel chatUser =
	 * Global.getFriendById(fromName); Log.i(TAG, "some one unblock block me ");
	 * if (Global.listBlockedOrBeBlocked != null) {
	 * Global.listBlockedOrBeBlocked.remove(Integer.valueOf(chatUser
	 * .getUserId())); SharedPreferences pref = this.getSharedPreferences(
	 * Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
	 * Global.savePref(pref.edit(), Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK,
	 * Global.listBlockedOrBeBlocked); } } if
	 * (type.equals(ChatType.FROM_BROADCAST)) {// user send broast cast if
	 * (Global.listBlockedOrBeBlocked != null) { if
	 * (Global.listBlockedOrBeBlocked.contains(Integer
	 * .valueOf(chatUser.getUserId()))) { // ko nhan broast cast tu nguoi bi
	 * minh block return; } } } if (type.equals(ChatType.NEW_REGISTER) ||
	 * type.equals(ChatType.FAVORITE)) { if (Global.appInBackGround) {
	 * buildNotification(message, true); } else { buildNotification(message,
	 * false); } return; }
	 * 
	 * // DELETE MESSAGE if (type.equals(ChatType.DELETE_MESSAGE)) { long
	 * messageId = mChatType.getMessageId(messageBody); Log.i(TAG,
	 * "delete message id = " + messageId); int delete =
	 * mDatabase.deleteMessage(messageId);
	 * mDatabase.reduceNewMessages(conversationId); if
	 * (Global.chatDetailActivity != null &&
	 * !Global.chatDetailActivity.isPausing) {
	 * //Global.chatDetailActivity.executeDeleteMessageLocal(delete); } Intent i
	 * = new Intent(getPackageName() + Params.PARAM_UPDATE_CHAT_LIST);
	 * i.putExtra(Params.PARAM_TYPE,
	 * ChatListActivityServer.FROM_CHAT_SERVICE_DELETE_MESSAGE);
	 * 
	 * this.sendBroadcast(i); i = new Intent(getPackageName() +
	 * Params.PARAM_BROADCAST); this.sendBroadcast(i); return; // DELETE
	 * CONVERSATION } else if (type.equals(ChatType.DELETE_CONVERSATION)) {
	 * Log.i(TAG, "conversation id = " + conversationId + "to delete"); //
	 * DELETE FROM SERVER if (fromName.equals(String.valueOf(Global.ADMIN))) {
	 * int delete = mDatabase.deleteAllMessages(conversationId);
	 * mDatabase.deleteConversationId(conversationId); Log.i(TAG,
	 * "delete conversation from " + fromName + " and " + delete); } else { //
	 * DELETE FROM USER: chi delete messenger cua doi phuong, con // mess cua
	 * minh trong cuoc hoi thoai do van giu int delete =
	 * mDatabase.deleteAllMessagesFromUser( conversationId, fromName, toName);
	 * Log.i(TAG, "delete conversation from " + fromName + " and " + delete);
	 * mDatabase.updateNewMessages(conversationId, 0); if
	 * (Global.chatDetailActivity != null &&
	 * !Global.chatDetailActivity.isPausing) {
	 * //Global.chatDetailActivity.executeDeleteMessageLocal(delete); } } Intent
	 * i = new Intent(getPackageName() + Params.PARAM_UPDATE_CHAT_LIST);
	 * i.putExtra( Params.PARAM_TYPE,
	 * ChatListActivityServer.FROM_CHAT_SERVICE_DELETE_CONVERSATION);
	 * this.sendBroadcast(i); i = new Intent(getPackageName() +
	 * Params.PARAM_BROADCAST); this.sendBroadcast(i); return; }
	 * 
	 * // ADD A CONVERSATION if (!fromName.equals(username) && conversationId >=
	 * 0) { // UPDATE CONVERSATION FROM -FROMNAME -> CONVERSATIONID long
	 * old_conversationId = mDatabase.getConversationByUserId(
	 * Integer.valueOf(fromName), Integer.valueOf(username)); if (conversationId
	 * == 0) conversationId = old_conversationId; if (old_conversationId <
	 * DatabaseHandler.COVNERTSATION_BY_ADMIN) { if
	 * ((DatabaseHandler.COVNERTSATION_BY_ADMIN * old_conversationId) == Integer
	 * .valueOf(fromName)) mDatabase.updateConversationId(conversationId,
	 * Integer.valueOf(fromName), Integer.valueOf(username)); }
	 * 
	 * // GET USERINFO FROM MESSAGE JSONObject userInfo =
	 * mChatType.getFromUser(messageBody); if (userInfo == null) userInfo =
	 * mChatType.getToUser(messageBody);
	 * 
	 * // NORMAL MESSAGE if (!type.equals(ChatType.FROM_BROADCAST)) {
	 * mCheckAddConversationId = mDatabase.addConversation( conversationId,
	 * Integer.valueOf(fromName), Integer.valueOf(username), 0,
	 * userInfo.toString()); Log.i(TAG, "add conversation = " +
	 * mCheckAddConversationId); // update all message broadcast with new
	 * conversation // TODO int update = mDatabase.updateMessage(
	 * -Integer.valueOf(fromName), conversationId); Log.i(TAG, "Update " +
	 * update + " messages broadcast with new conversationId = " +
	 * conversationId + " and old conversationId = " +
	 * (-Integer.valueOf(fromName))); } else { // BROADCAST MESSAGE if
	 * (old_conversationId == DatabaseHandler.COVNERTSATION_BY_ADMIN) {
	 * mCheckAddConversationId = mDatabase.addConversation(
	 * -Integer.valueOf(fromName), Integer.valueOf(fromName),
	 * Integer.valueOf(username), 0, userInfo.toString()); Log.i(TAG,
	 * "add conversation of from_broadcast = " + mCheckAddConversationId);
	 * conversationId = -Integer.valueOf(fromName); } else if
	 * (old_conversationId > 0) { Log.i(TAG, "replace old conversation");
	 * conversationId = old_conversationId; } }
	 * 
	 * // UPDATE USER IN USER FRIEND UserModel userEdit =
	 * Global.getFriendById(fromName); if (userEdit != null) { UserModel
	 * chatUser = new UserModel(); JSONObject dataUser =
	 * mChatType.getFromUser(messageBody); if (dataUser != null) { chatUser =
	 * Global.setUserInfo(dataUser, null); if (chatUser != null) {
	 * userEdit.setName(chatUser.getName());
	 * userEdit.setUserAvatar(chatUser.getUserAvatar()); } } } }
	 * 
	 * // send success long id =
	 * mChatType.getId(mChatType.getData(messageBody)); MessageModel
	 * messageModel = new MessageModel(conversationId, id, Global.userInfo,
	 * messageBody, false); // get type of data String typeInData =
	 * mChatType.getTypeInData(messageBody); // mCheckAddConversationId from
	 * broadcast (send) if (typeInData != null) { if
	 * (typeInData.equals(ChatType.BROADCAST)) { if
	 * (Global.sendAllMessageActivity != null)
	 * Global.sendAllMessageActivity.receiveMessage(messageBody, checkMessage);
	 * } if (typeInData.equals(ChatType.POINT)) { Intent i = new
	 * Intent(getPackageName() + Params.PARAM_GIF_POINT);
	 * i.putExtra(Params.MESSAGE, message.getBody()); this.sendBroadcast(i); } }
	 * 
	 * // MESSAGE: NO ERROR if (checkMessage) { long messageId =
	 * mChatType.getMessageId(mChatType .getData(messageBody)); if
	 * (type.equals(ChatType.FROM_NOTIFICATION) ||
	 * type.equals(ChatType.FROM_BROADCAST) || messageId == 0) { messageId =
	 * System.currentTimeMillis(); } // add message to list messages chatUser =
	 * Global.getFriendById(fromName); if (chatUser == null) { chatUser =
	 * getUserinfo(messageBody); } messageModel = new
	 * MessageModel(conversationId, messageId, chatUser, messageBody, false);
	 * messageModel .setDateMessage(mChatType.getTimeSent(messageBody) * 1000);
	 * if (Global.userInfo != null && chatUser != null)
	 * messageModel.setDistance(Global.distanceBetweenGPS( Global.userInfo,
	 * chatUser, this)); // add to database if
	 * (type.equals(ChatType.SEND_SUCCESS)) mDatabase.updateMessage(id,
	 * messageModel, false); else { // ADD MESSAGE TO DB // ADD NEW MESSAGE TO
	 * CONVERSATION Log.i(TAG, "add new message"); if
	 * (!mDatabase.checkMessageId(messageModel.getMessageId())) {
	 * mDatabase.addMessage(fromName, toName, messageModel, false); int
	 * newMessages = mDatabase.getNewMessages(conversationId); newMessages++;
	 * Log.i(TAG, "new message: " + newMessages);
	 * mDatabase.updateNewMessages(conversationId, newMessages);
	 * sendBroadCast(message); } else { // da add vao db truoc do roi: case khi
	 * vao chatDetailActivity,tu // dong load all messeage tinh tu mess cuoi
	 * cung ban than // gui thanh con // neu lay duoc bao nhieu mess thi save
	 * luon vao db, fix bug // lost message wheen lost connetion openfire
	 * return; }
	 * 
	 * } } else { Log.i(TAG, "update message error id=" + id + "messageId=" +
	 * messageModel.getMessageId()); mDatabase.updateMessage(id,
	 * messageModel.getMessageId(), true); }
	 * 
	 * if (Global.chatDetailActivity != null &&
	 * !Global.chatDetailActivity.isPausing) { //
	 * Global.chatDetailActivity.setListAdapter(fromName, messageModel, //
	 * messageBody, conversationId); } // add len server message neu id<= 0
	 * final MessageModel model = messageModel; final int from =
	 * ChatType.getInstance(getApplicationContext())
	 * .executeGetFrom(messageModel.getMessage()); final String messageChat =
	 * ChatType .getInstance(getApplicationContext()).executeReceive(
	 * messageModel.getMessage()); model.setMessage(messageChat);
	 * model.setType(type); long conv_id = messageModel.getConversationId();
	 * Log.i(TAG, "CONVERSATIONID test = " + conv_id); // Toast.makeText(this,
	 * "call update"+conv_id, // Toast.LENGTH_SHORT).show(); if (conv_id <= 0) {
	 * // call api save message to server // vi day la push message frm admin or
	 * broatcast Runnable r = new Runnable() {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub
	 * Log.i(TAG, "CONVERSATIONID test test 0 = "); AddPushMessageToServerAPI
	 * api = new AddPushMessageToServerAPI( ChatBackgroundService.this,
	 * Global.chatListActivityServer, model); Log.i(TAG,
	 * "CONVERSATIONID test test 1 = "); RequestParams request = new
	 * RequestParams(); request.put(Params.PARAM_USER_ID_, SessionManager
	 * .getInstance(getApplicationContext()).getUserId());
	 * request.put(Params.PARAM_TOKEN_, SessionManager
	 * .getInstance(getApplicationContext()).getToken()); Log.i(TAG,
	 * "CONVERSATIONID test test 2 = "); request.put(Params.PARAM_FROM, from);
	 * request.put(Params.PARAM_TO, Global.userInfo.getUserId());
	 * request.put(Params.MESSAGE, messageChat); api.setParams(request);
	 * api.setUseProgressbar(false); api.onRunButtonPressed(); Log.i(TAG,
	 * "CONVERSATIONID test test = "); } }; handler.post(r); return; } // Add
	 * the incoming message to the list view if (Global.appInBackGround) {
	 * Log.e(TAG, "inbackground"); buildNotification(message, true); } else { if
	 * (Global.chatDetailActivity != null) { if
	 * (Global.chatDetailActivity.isPausing) { buildNotification(message,
	 * false); } else { if (Global.chatDetailActivity.chatUser != null) { if
	 * (!fromName.equals(Global.chatDetailActivity.chatUser .getUserName())) {
	 * buildNotification(message, false); } } } Log.e(TAG, "chat activity"); }
	 * else { buildNotification(message, false); Log.e(TAG,
	 * "not chat activity"); } } }
	 */
	private UserModel getUserinfo(String messageBody) {
		UserModel chatUser = new UserModel();
		JSONObject dataUser = mChatType.getFromUser(messageBody);
		if (dataUser != null) {
			chatUser = Global.setUserInfo(dataUser, null);
			// if (chatUser != null) {
			// if (Global.listFriendMap.get(chatUser.getUserName()) == null &&
			// chatUser.getUserId()!=Global.ADMIN) {
			// Global.listFriend.add(chatUser);
			// Global.listFriendMap.put(chatUser.getUserName(), chatUser);
			// }
			// }
		}
		return chatUser;
	}

	private UserModel getUserinfoFootPrint(String messageBody) {
		UserModel chatUser = new UserModel();
		JSONObject dataUser = mChatType.getFromUserFootprint(messageBody);
		if (dataUser != null) {
			chatUser = Global.setUserInfo(dataUser, null);
			// if (chatUser != null) {
			// if (Global.listFriendMap.get(chatUser.getUserName()) == null &&
			// chatUser.getUserId()!=Global.ADMIN) {
			// Global.listFriend.add(chatUser);
			// Global.listFriendMap.put(chatUser.getUserName(), chatUser);
			// }
			// }
		}
		return chatUser;
	}

	protected void buildNotification(Message message, boolean showDialog) {
		try {
			JSONObject jo = new JSONObject(message.getBody());
			String type = mChatType.receiveJSON(message.getBody());
			Log.i(TAG, "type notification =" + type);
			if (ChatType.SEND_SUCCESS.equals(type)
					|| type.equals(ChatType.ERROR_1000)
					|| type.equals(ChatType.ERROR_1001)) {
				return;
			}
			SharedPreferences pref = this.getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			if (Global.userInfo == null) {
				// try get from pref
				try {

					String myString = pref.getString(Params.PREF_USER_INFO, "");
					UserModel user = Global.setUserInfo(
							new JSONObject(myString), null);
					if (user != null) {
						Global.userInfo = user;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String userOpt = "from_user";
			if (type.equals(ChatType.FAVORITE)
					|| type.equals(ChatType.NEW_REGISTER)) {
				userOpt = "user";
			}
			JSONObject data = jo.getJSONObject("data");
			JSONObject j_user = null;
			int user_id = -1;
			String name = "";
			String msg = "";
			if (!type.equals(ChatType.RANKING)) {
				j_user = data.getJSONObject(userOpt);
				user_id = j_user.optInt("id");
				name = j_user.optString("name");
				msg = data.optString("msg");
				// msg=StringEscapeUtils.unescapeJava(msg);
				msg = Global.filterBadWordJapan(msg, this);
				//
			} else {
				name = Global.userInfo.getName();
			}

			if (Global.userInfo != null
					&& type.equalsIgnoreCase(ChatType.POINT)) {
				//
				int currentPts = data.optInt("point");
				// Log.e("", "curr pts:" + currentPts);
				Intent intent = new Intent(BuyPointActivity.UPDATE_POINTS);
				intent.putExtra(Params.PARAM_POSSESS_POINT, currentPts);
				// SEND BROADCAST : POINT
				broadcaster.sendBroadcast(intent);
			}
			boolean isShowNotificationMenuBar = true;
			boolean isShowBigView = true;
			if (pref.getInt(Params.SETTING_NOTIFYCATION_MESSENGER,
					Params.SETTING_NOTIFYCATION_MESSENGER_ALL) != Params.SETTING_NOTIFYCATION_MESSENGER_ALL) {
				if (Global.listFavoriteId.size() == 0) {// case android os kill
														// static value
					String blocks = pref.getString(Params.PREF_LIST_FAVORITE_ID
							+ Global.userInfo.getUserId(), "NULL");
					if (blocks != null && !blocks.equalsIgnoreCase("NULL")) {
						String[] lst = blocks.split(",");
						for (int i = 0; i < lst.length; i++) {
							int id = Integer.valueOf(lst[i]);
							Global.listFavoriteId.add(id);
						}
					}

				}
				if (!Global.listFavoriteId.contains(Integer.valueOf(user_id))) {
					isShowNotificationMenuBar = false;// setting ko nhan
														// notification tu nguoi
														// ko nam trong favorite
														// list
					// nhung van nhan duoc mess nguoi do o trong chatlist
					isShowBigView = false;
				} else {
					isShowNotificationMenuBar = true;
					boolean value = getValuePref(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW);
					if (!value) {
						Log.v("", "user disable show mess bigview");
						isShowBigView = false;
					} else {
						isShowBigView = true;
					}
				}
			} else {
				isShowNotificationMenuBar = true;
				boolean value = getValuePref(Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW);
				if (!value) {
					Log.v("", "user disable show mess bigview");
					isShowBigView = false;
				} else {
					isShowBigView = true;
				}
			}
			if (type.equals(ChatType.FROM_NOTIFICATION)) {// admin send
				boolean value = getValuePref(Params.SETTING_NOTIFYCATION_SMS_SUPPORT);
				if (!value) {
					Log.v("", "user disable admin sms notificaion");
					isShowNotificationMenuBar = false;
				} else {

				}
				Log.v("", "user enable admin sms notice");
				value = getValuePref(Params.SETTING_BIGVIEW_SMS_SUPPORT);
				if (!value) {
					Log.v("", "user disable admin sms bigview");
					isShowBigView = false;
				} else {

				}
				Log.v("", "user enable admin sms bigview");
			}
			// newuser,lottop,favortie here
			if (type.equals(ChatType.FAVORITE)) {// send notify favorite
				msg = String.format(getString(R.string.favorite_add), name);
				boolean value = getValuePref(Params.SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE);
				if (!value) {
					Log.v("", "user disable favorite notice notificaion");
					isShowNotificationMenuBar = false;
				} else {
					Log.v("", "user enable favorite notice");
				}
				value = getValuePref(Params.SETTING_BIGVIEW_SUBMIT_TO_FAVORITE);
				if (!value) {
					Log.v("", "user disable favorite notice bigview");
					isShowBigView = false;
				} else {
					Log.v("", "user enable favorite notice bigview");
				}
			}
			if (type.equals(ChatType.RANKING)) {// send notify favorite
				msg = getString(R.string.rank_notify);
				isShowNotificationMenuBar = true;// ko co setting notificaion
													// rank len luon luon show
				boolean value = getValuePref(Params.SETTING_BIGVIEW_WHEN_IN_TOP);
				if (!value) {
					Log.v("", "user disable ranking notice bigview");
					isShowBigView = false;
				} else {
					Log.v("", "user enable ranking notice bigview"
							+ isShowBigView);
				}
			}
			if (type.equalsIgnoreCase(ChatType.POINT)) {
				int pts = data.optInt("point_receiver");
				msg = String.format(getString(R.string.chat_point_receive),
						name, pts);
			}
			if (type.equalsIgnoreCase(ChatType.BLOCK_USER)) {
				msg = String.format(getString(R.string.chat_block_by_user),
						name);
			}
			if (type.equalsIgnoreCase(ChatType.UNBLOCK_USER)) {
				msg = String.format(getString(R.string.chat_unblock_by_user),
						name);
			}

			if (type.equalsIgnoreCase(ChatType.STAMP)){
				msg = getString(R.string.chat_list_stamp);
			}
			if ( type.equalsIgnoreCase(ChatType.IMAGE)) {
//				msg = getString(R.string.unformat_receive_image);
				msg = getString(R.string.chat_list_image);
			}
			if (type.equalsIgnoreCase(ChatType.RANKING)) {
				msg = getString(R.string.rank_notify);
			}
			if (type.equalsIgnoreCase(ChatType.NEW_REGISTER)) {
				if(user_id==Global.userInfo.getUserId()) return;//
				msg = String.format(getString(R.string.new_user), name);
				boolean value = getValuePref(Params.SETTING_NOTIFYCATION_NEW_USER);
				if (!value) {
					Log.v("", "user disable NEW_REGISTER notice notificaion");
					isShowNotificationMenuBar = false;
				} else {
					Log.v("", "user enable NEW_REGISTER notice");
				}
				value = getValuePref(Params.SETTING_BIGVIEW_NEW_USER);
				if (!value) {
					Log.v("", "user disable NEW_REGISTER notice bigview");
					isShowBigView = false;
				} else {
					Log.v("", "user enable NEW_REGISTER notice bigview");
				}
			}

			// if(j_user!=null){
			Log.d(TAG, "msg notification:" + msg);
			UserModel userModel;
			if (j_user != null) {
				Log.d(TAG, "from_user:" + j_user.toString());
				userModel = Global.setUserInfo(j_user, null);
			} else {
				userModel = new UserModel();
				Global.cloneObj(userModel, Global.userInfo);
			}

			if (isShowNotificationMenuBar) {
//				 mBuilder = new NotificationCompat.Builder(
//						this);
				mBuilder.setSmallIcon(R.drawable.icon)
						.setContentTitle(name).setContentText(msg).setOnlyAlertOnce(true);

				Intent activityA = new Intent(this, UserListActivity.class);
				Intent resultIntent = null;
				if (type.equalsIgnoreCase(ChatType.RANKING)) {
					resultIntent = new Intent(this, RankActivity.class);
					resultIntent.putExtra(Params.PARAM_TITLE,
							getString(R.string.rank_title));
				} else if (type.equalsIgnoreCase(ChatType.NEW_REGISTER)
						|| type.equalsIgnoreCase(ChatType.FAVORITE)) {
					resultIntent = new Intent(this, ViewProfileActivity.class);
					resultIntent.putExtra(Params.USER, (UserModel) userModel);
				} else {
					resultIntent = new Intent(this, ChatDetailActivity.class);
					resultIntent.putExtra(Params.USER, (UserModel) userModel);
				}
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				UserModel myInfo = null;
				if (Global.userInfo == null) {
					// try get from pref
					try {

						String myString = pref.getString(Params.PREF_USER_INFO,
								"");
						UserModel user = Global.setUserInfo(new JSONObject(
								myString), null);
						if (user != null) {
							Global.userInfo = user;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (Global.userInfo != null) {// app in background
					myInfo = new UserModel();
					Global.cloneObj(myInfo, Global.userInfo);
					resultIntent.putExtra(Params.PARAM_STRAT_FROM_NOTIFICATION,
							myInfo);
					stackBuilder.addParentStack(UserListActivity.class);

					stackBuilder.addNextIntent(activityA);
					stackBuilder.addNextIntent(resultIntent);
					// Log.e("", "myInfo :"+myInfo.getName()+
					// myInfo.getPossessPoint());
				} else {// app got kill by ....
					stackBuilder.addParentStack(SplashScreenActivity.class);
					activityA = new Intent(this, SplashScreenActivity.class);
					stackBuilder.addNextIntent(activityA);
					// Log.e("", "splash screen");
				}
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent((int) System.currentTimeMillis(),
								PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
				Notification notfi = mBuilder.build();
				notfi.flags |= Notification.FLAG_AUTO_CANCEL;
				SharedPreferences prefs = this.getSharedPreferences(
						Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
				long time = System.currentTimeMillis();
				long diffTime = time - prefs.getLong(Params.NOTIFI_TIME, 0);
				if (prefs.getBoolean(Params.SETTING_VIBE, true)
						&& diffTime > 2000)
					notfi.defaults |= Notification.DEFAULT_VIBRATE;
				if (prefs.getBoolean(Params.SETTING_SOUND, true)
						&& diffTime > 2000) {
					notfi.defaults |= Notification.DEFAULT_SOUND;
				}
				notfi.defaults |= Notification.DEFAULT_LIGHTS;
				prefs.edit().putLong(Params.NOTIFI_TIME, time);
				prefs.edit().commit();
				Integer count = countMess.get(user_id);
				if (count == null) {
					countMess.put(user_id, Integer.valueOf(1));
					notfi.number = 1;
				} else {
					Integer num = Integer.valueOf(count + 1);
					notfi.number = num.intValue();
					countMess.put(user_id, num);
				}
				// mId allows you to update the notification later on.
				mNotificationManager.notify(user_id, notfi);
			}
			// show message dialog
			if (showDialog && isShowBigView)// && settingShowBigView
				showMessageDialogActivity(type, userModel, msg, user_id);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void showMessageDialogActivity(String type, UserModel userModel,
			String msg, int user_id) {
		showNotifyDialog(userModel, msg, user_id, type);
		// Start dialog intent
		// }
	}

	private void showNotifyDialog(UserModel userModel, String msg, int user_id,
			String type) {
		if (!Global.IS_OPEN_DIALOG_NOTIFICATION) {
			Intent dialogIntent = new Intent(this, MessageDialog.class);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			dialogIntent.putExtra(Params.USER, (UserModel) userModel);
			dialogIntent.putExtra(Params.PARAM_MESSENGER, msg);
			dialogIntent.putExtra(Params.PARAM_NOTIFY_TYPE, type);
			dialogIntent.putExtra(Params.NOTIFY_ID, user_id);
			dialogIntent.putExtra(Params.NOTIFY_STATE_APP,
					Global.appInBackGround);
			getApplication().startActivity(dialogIntent);
			Global.IS_OPEN_DIALOG_NOTIFICATION = true;
			Global.DIALOG_NOTIFICATION_ID = user_id;
			Log.v("", "IS_OPEN_DIALOG_NOTIFICATION: CREATE NEW"
					+ Global.IS_OPEN_DIALOG_NOTIFICATION);
		} else if (user_id == Global.DIALOG_NOTIFICATION_ID) {
			// update mess
			Log.v("", "IS_OPEN_DIALOG_NOTIFICATION:"
					+ Global.IS_OPEN_DIALOG_NOTIFICATION);
			Intent intent = new Intent(MessageDialog.UPDATE_MESSENGER);
			intent.putExtra(Params.PARAM_MESSENGER, msg);
			intent.putExtra(Params.PARAM_NOTIFY_TYPE, type);
			broadcaster.sendBroadcast(intent);
		}
	}

	class LogInOutReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "receiver with action:" + action);
			if ((getPackageName() + Params.AUTHENTICATE).equals(action)) {
				loginToChat();
			} else if ((getPackageName() + Params.STOP_SERVICE).equals(action)) {
				stopService();
			}
		}

	}

	// TODO handle network loss connection
	private void loginToChat() {
		Log.d(TAG, "login to chat + service crate");
		if (mConnection != null && mConnection.isAuthenticated()) {
			Log.d(TAG, "login to chat + service create flag1");
			return;
		}
		SessionManager session = SessionManager
				.getInstance(ChatBackgroundService.this);
		String username = session.getUsername();
		String password = session.getPassword();
		if (username == null || password == null || session.getToken() == null) {
			if (username == null) {
				Log.d(TAG, "username is null");
			}
			if (password == null) {
				Log.d(TAG, "password is null");
			}
			if (session.getToken() == null) {
				Log.d(TAG, "token is null");
			}
			Log.d(TAG, "login to chat + service create flag2");
			return;
		}
		Log.d(TAG, "receive data with user:" + username + ", pass: " + password);

		MyRunnable r = new MyRunnable();
		r.setData(username, password);

		if (!mIsNeedRun) {
			Log.d(TAG, "login to chat + service create flag3");
			Thread t = new Thread(r);
			mIsNeedRun = true;
			t.start();
		}

	}

	class MyRunnable implements Runnable {
		private String username, password;

		public void setData(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public void run() {
			/**
			 * CONNECT TO SERVER
			 */
			ConnectionConfiguration connConfig = new ConnectionConfiguration(
					HOST, PORT, SERVICE);
			mConnection = new XMPPConnection(connConfig);
			Log.v("", "service name " + mConnection.getServiceName());
			while (true && mIsNeedRun) {
				if (mConnection.isConnected() && mConnection.isAuthenticated()) {
					mIsNeedRun = false;
					Intent i = new Intent(
							ChatBackgroundService.this.getPackageName()
									+ Params.PARAM_RECONNECT_CHATSERVER);
					ChatBackgroundService.this.sendBroadcast(i);
					return;
				} else if (mConnection.isConnected()
						&& !mConnection.isAuthenticated()) {
					/**
					 * LOGIN TO A SERVER
					 */
					try {
						mConnection.login(username, password,
								username + System.currentTimeMillis());
						// mConnection.login(username, password);
						Log.i(TAG, "Logged in as " + mConnection.getUser());
						/**
						 * SETTING USER PRESENCE
						 */
						// Set the status to available
						Presence presence = new Presence(
								Presence.Type.available);
						presence.setMode(Presence.Mode.available);// kaka add
						mConnection.sendPacket(presence);
						setConnection(mConnection, username);
					} catch (Exception e) {
						Log.e(TAG, "Failed to log in as " + username);
						Log.e(TAG, e.toString());
						mIsNeedRun = false;
						setConnection(null, username);
						return;
					}

				} else if (!mConnection.isConnected()) {
					try {
						// Connect to the server
						mConnection.connect();
						Log.i(TAG, "Connected to " + mConnection.getHost());
					} catch (Exception ex) {
						Log.e(TAG,
								"Failed to connect to " + mConnection.getHost());
						Log.e(TAG, ex.toString());
						setConnection(null, username);
						mIsNeedRun = false;
						return;
					}
				}
				try {
					Thread.sleep(Params.SLEEP_TIME);

					Log.d(TAG,
							"thread for connect with status:"
									+ mConnection.isConnected() + "and:"
									+ mConnection.isAuthenticated());
				} catch (Exception e) {
					mIsNeedRun = false;
					e.printStackTrace();
				}
			}
		}

	}

	public void dismissNotification(int id) {
		mNotificationManager.cancel(id);
		if(countMess!=null)countMess.put(id, Integer.valueOf(0));
	}

}
