package com.cls.sugutomo.databases;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.model.ConversationModel;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String TAG = DatabaseHandler.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;

	// database name
	private static final String DATABASE_NAME = "MessagesManager";

	// table name
	private static final String TABLE_CONVERSATION = "Conversations";
	private static final String TABLE_MESSAGES = "Messages";
	private static final String TABLE_ACCOUNT = "Accounts";

	// key primary
	private static final String KEY_ID = "id";

	// key of account table
	// userId = fbid || username(email)
	private static final String KEY_USERID = "userId";
	// fullname when create profile
	private static final String KEY_FULLNAME = "fullname";
	// email
	private static final String KEY_EMAIL = "email";
	// link avatar
	private static final String KEY_USERAVATAR = "useravatar";
	private static final String KEY_LOCATION = "location";
	private static final String KEY_ACTIVED = "actived";
	private static final String KEY_STATUS = "status";

	// key of conversation
	private static final String KEY_CONVERSATION_ID = "conversation_id";
	private static final String KEY_USER_INFO = "user_info";
	private static final String KEY_NEW_MESSAGES = "new_messages";

	// key of message
	private static final String KEY_MESSAGE_ID = "message_id";

	// key of chat table
	private static final String KEY_FROM = "me";
	private static final String KEY_TO = "you";
	private static final String KEY_BODY = "body";
	private static final String KEY_DATE = "time";
	private static final String KEY_DISTANCE = "distance";
	private static final String KEY_ERROR_MESSAGE = "error";

	private ChatType mChatType;
	private static DatabaseHandler mInstance = null;

	public static DatabaseHandler getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new DatabaseHandler(ctx.getApplicationContext());
		}
		return mInstance;
	}

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mChatType = ChatType.getInstance(context);
	}

	String CREATE_CONVERSATION_TABLE = "CREATE TABLE " + TABLE_CONVERSATION
			+ "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_CONVERSATION_ID + " LONG," + KEY_FROM + " INTEGER," + KEY_TO
			+ " INTEGER," + KEY_NEW_MESSAGES + " INTEGER," + KEY_USER_INFO
			+ " TEXT" + ")";

	String CREATE_CHATS_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY," + KEY_CONVERSATION_ID + " LONG,"
			+ KEY_MESSAGE_ID + " LONG," + KEY_FROM + " TEXT," + KEY_TO
			+ " TEXT," + KEY_BODY + " TEXT," + KEY_DATE + " LONG,"
			+ KEY_DISTANCE + " TEXT," + KEY_ERROR_MESSAGE + " INTEGER" + ")";

	String CREATE_ACCOUNT_TABLE = "CREATE TABLE " + TABLE_ACCOUNT + "("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_USERID
			+ " TEXT," + KEY_EMAIL + " TEXT," + KEY_FULLNAME + " TEXT,"
			+ KEY_STATUS + " TEXT," + KEY_USERAVATAR + " TEXT," + KEY_LOCATION
			+ " TEXT," + KEY_ACTIVED + " BOOLEAN" + ")";

	@Override
	public void onCreate(SQLiteDatabase db) {
		// table conversation
		db.execSQL(CREATE_CONVERSATION_TABLE);
		// table account
		db.execSQL(CREATE_ACCOUNT_TABLE);
		// table contact
		db.execSQL(CREATE_CHATS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_CONVERSATION_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
		// create new tables
		onCreate(db);
	}

	/**
	 * Create a account user
	 * 
	 * @param user
	 */
	public long createAccount(UserModel user) {
		if (getAccountUser(user.getUserName()) != null) {
			Log.v(TAG, "error when create new account: " + user.getUserName()
					+ " was created to DB");
			return -1;
		}
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USERID, user.getUserName());
		values.put(KEY_EMAIL, user.getEmail());
		values.put(KEY_FULLNAME, user.getName());
		values.put(KEY_USERAVATAR, user.getUserAvatar());
		values.put(KEY_ACTIVED, user.isActived());
		values.put(KEY_STATUS, user.getWallStatus());
		// values.put(KEY_LOCATION, user.getLocation().toString());

		db.insert(TABLE_ACCOUNT, null, values);
		// db.close();
		return 0;
	}

	/**
	 * GET ALL ACCOUNT IN DATABASE
	 * 
	 * @return
	 */
	public List<UserModel> getAllAccountUser() {
		List<UserModel> listUser = new ArrayList<UserModel>();
		String selectQuery = "SELECT * FROM " + TABLE_ACCOUNT;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		if (c.moveToFirst()) {
			do {
				UserModel user = new UserModel();
				user.setUserId(c.getInt(c.getColumnIndex(KEY_ID)));
				user.setUserName(c.getString(c.getColumnIndex(KEY_USERID)));
				user.setName(c.getString(c.getColumnIndex(KEY_FULLNAME)));
				user.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
				user.setUserAvatar(c.getString(c.getColumnIndex(KEY_USERAVATAR)));
				user.setActived(c.getExtras().getBoolean(KEY_ACTIVED));
				user.setWallStatus(c.getString(c.getColumnIndex(KEY_STATUS)));

				// add to list
				listUser.add(user);
			} while (c.moveToNext());
			c.close();
		}
		// db.close();
		return listUser;
	}

	// SELECT * FROM accounts WHERE email = search
	/**
	 * Get a account user
	 * 
	 * @param search
	 * @return a user
	 */
	public UserModel getAccountUser(String search) {
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		UserModel user = null;
		selectQuery = "SELECT * FROM " + TABLE_ACCOUNT + " WHERE " + KEY_USERID
				+ " = '" + search + "'";
		if (selectQuery == "")
			return null;
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null && c.moveToFirst()) {
			user = new UserModel();
			user.setUserId(c.getInt(c.getColumnIndex(KEY_ID)));
			user.setUserName(c.getString(c.getColumnIndex(KEY_USERID)));
			user.setName(c.getString(c.getColumnIndex(KEY_FULLNAME)));
			user.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
			user.setUserAvatar(c.getString(c.getColumnIndex(KEY_USERAVATAR)));
			user.setActived(c.getExtras().getBoolean(KEY_ACTIVED));
			user.setWallStatus(c.getString(c.getColumnIndex(KEY_STATUS)));
			c.close();
			// set location
		} else
			return null;
		// db.close();
		return user;
	}

	public UserModel getAccountUserByMail(String search) {
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		UserModel user = null;
		selectQuery = "SELECT * FROM " + TABLE_ACCOUNT + " WHERE " + KEY_EMAIL
				+ " = '" + search + "'";
		if (selectQuery == "")
			return null;
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null) {
			c.moveToFirst();
			user = new UserModel();
			user.setUserId(c.getInt(c.getColumnIndex(KEY_ID)));
			user.setUserName(c.getString(c.getColumnIndex(KEY_USERID)));
			user.setName(c.getString(c.getColumnIndex(KEY_FULLNAME)));
			user.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
			user.setUserAvatar(c.getString(c.getColumnIndex(KEY_USERAVATAR)));
			user.setActived(c.getExtras().getBoolean(KEY_ACTIVED));
			user.setWallStatus(c.getString(c.getColumnIndex(KEY_STATUS)));
			c.close();
			// set location
		}
		// db.close();
		return user;
	}

	/**
	 * Update a account info
	 * 
	 * @param user
	 * @return number row affected
	 */
	public int updateAccountInfo(UserModel user) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, user.getUserId());
		values.put(KEY_USERID, user.getUserName());
		values.put(KEY_EMAIL, user.getEmail());
		values.put(KEY_FULLNAME, user.getName());
		values.put(KEY_USERAVATAR, user.getUserAvatar());
		values.put(KEY_ACTIVED, user.isActived());
		values.put(KEY_STATUS, user.getWallStatus());
		// values.put(KEY_LOCATION, user.getLocation().toString());

		// updating row
		int id = db.update(TABLE_ACCOUNT, values, KEY_ID + " = ?",
				new String[] { String.valueOf(user.getUserId()) });
		// db.close();
		return id;
	}

	/**
	 * Delete a user
	 * 
	 * @param user
	 */
	public void deleteToDo(UserModel user) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ACCOUNT, KEY_ID + " = ?",
				new String[] { String.valueOf(user.getUserId()) });
		// db.close();
	}

	// add new a conversation to DB
	public long addConversation(long conversation_id, int from, int to,
			int newMessage, String userInfo) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (!checkConversationId(conversation_id, from, to)) {
			values.put(KEY_CONVERSATION_ID, conversation_id);
			values.put(KEY_FROM, from);
			values.put(KEY_TO, to);
			values.put(KEY_NEW_MESSAGES, newMessage);
			values.put(KEY_USER_INFO, userInfo);
			long id = db.insert(TABLE_CONVERSATION, null, values);
			return id;
		} else {
			updateUserinfoOfAConversation(conversation_id, userInfo);
			Log.i(TAG, "add conversation fail");
			// db.close();
			return -1;
		}
	}

	// update conversation
	public long updateUserinfoOfAConversation(long conversation_id,
			String userInfo) {
		Log.i(TAG, "update userinfo");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_INFO, userInfo);
		// updating row
		int id = db.update(TABLE_CONVERSATION, values, KEY_CONVERSATION_ID
				+ " = " + conversation_id, null);
		return id;
	}

	// update conversation
	public long updateConversationId(long conversation_id, int from, int to) {
		SQLiteDatabase db = this.getWritableDatabase();
		if (!checkConversationId(conversation_id, from, to)) {
			ContentValues values = new ContentValues();
			values.put(KEY_CONVERSATION_ID, conversation_id);
			// updating row
			int id = db.update(TABLE_CONVERSATION, values, KEY_FROM + " = "
					+ from + " AND " + KEY_TO + " = " + to, null);
			// db.close();
			return id;
		}else return -1;
	}

	// get new message
	public int getNewMessages(long conversationId) {
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		selectQuery = "SELECT * FROM " + TABLE_CONVERSATION + " WHERE "
				+ KEY_CONVERSATION_ID + " = '" + conversationId + "'";
		Cursor c = db.rawQuery(selectQuery, null);
		if (c == null) {
			// db.close();
			return -1;
		} else if (!c.moveToFirst() || c.getCount() <= 0) {
			// db.close();
			return -1;
		} else {
			Log.v(TAG,
					"get new message id="
							+ c.getLong(c.getColumnIndex(KEY_CONVERSATION_ID))
							+ ", from=" + c.getInt(c.getColumnIndex(KEY_FROM))
							+ ", to=" + c.getInt(c.getColumnIndex(KEY_TO))
							+ ", newMessage="
							+ c.getInt(c.getColumnIndex(KEY_NEW_MESSAGES)));
			int id = c.getInt(c.getColumnIndex(KEY_NEW_MESSAGES));
			c.close();
			// db.close();
			return id;
		}
	}

	// update new message
	public long updateNewMessages(long conversation_id, int newMessage) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NEW_MESSAGES, newMessage);
		// updating row
		int id = db.update(TABLE_CONVERSATION, values, KEY_CONVERSATION_ID
				+ " = " + conversation_id, null);
		// db.close();
		return id;
	}

	public long reduceNewMessages(long conversation_id) {
		int totalNew = getNewMessages(conversation_id);
		if (totalNew <= 0)
			return 0;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NEW_MESSAGES, totalNew - 1);
		// updating row
		int id = db.update(TABLE_CONVERSATION, values, KEY_CONVERSATION_ID
				+ " = " + conversation_id, null);
		// db.close();
		return id;
	}

	// get a conversation from userid
	public boolean checkConversationId(long conversation, int from, int to) {
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		if (!checkEmptyTable(TABLE_CONVERSATION)) {
			// db.close();
			return false;
		}
		selectQuery = "SELECT * FROM " + TABLE_CONVERSATION + " WHERE "
				+ KEY_CONVERSATION_ID + " = '" + conversation + "' AND "
				+ KEY_FROM + " = '" + from + "' AND " + KEY_TO + " = '" + to
				+ "'";
		Cursor c = db.rawQuery(selectQuery, null);
		if (c == null)
			return false;
		else if (!c.moveToFirst() || c.getCount() <= 0) {
			// db.close();
			return false;
		} else {
			Log.v(TAG,
					"get Conversation id="
							+ c.getLong(c.getColumnIndex(KEY_CONVERSATION_ID))
							+ ", from=" + c.getInt(c.getColumnIndex(KEY_FROM))
							+ ", to=" + c.getInt(c.getColumnIndex(KEY_TO))
							+ " userinfo"
							+ c.getString(c.getColumnIndex(KEY_USER_INFO)));
			// db.close();
			return true;
		}
	}

	public boolean checkEmptyTable(String table_name) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table_name, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int count = cursor.getInt(0);
			if (count > 0) {
				return true;
			}
			cursor.close();
		}
		// db.close();
		return false;
	}

	// get all conversations from DB
	public Vector<ConversationModel> getAllConversations(int toUser) {
		Vector<ConversationModel> contactList = new Vector<ConversationModel>();
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		selectQuery = "SELECT * FROM " + TABLE_CONVERSATION + " WHERE "
				+ KEY_TO + " = '" + toUser + "'";
		Cursor c = db.rawQuery(selectQuery, null);
		if (c.moveToFirst()) {
			do {
				contactList.add(new ConversationModel(c.getLong(1),
						c.getInt(2), c.getInt(3), c.getInt(4), c.getString(5)));
			} while (c.moveToNext());
			c.close();
		}
		// db.close();
		return contactList;
	}

	// get a conversation from userid
	public static int COVNERTSATION_BY_ADMIN=-1;
	public long getConversationByUserId(int from, int to) {
		String selectQuery = "";
		SQLiteDatabase db = this.getReadableDatabase();
		selectQuery = "SELECT * FROM " + TABLE_CONVERSATION + " WHERE "
				+ KEY_FROM + " = '" + from + "' AND " + KEY_TO + " = '" + to
				+ "'";
		Cursor c = db.rawQuery(selectQuery, null);
		Log.i(TAG, "selectQuery = " + selectQuery);
		if (c == null) {
//			Log.i(TAG, "cursor null");
			// db.close();
			return COVNERTSATION_BY_ADMIN;
		} else if (!c.moveToFirst() || c.getCount() <= 0) {
//			Log.i(TAG, "cursor <=0");
			c.close();
			// db.close();
			return COVNERTSATION_BY_ADMIN;
		} else {
			Log.v(TAG,
					"get Conversation id="
							+ c.getLong(c.getColumnIndex(KEY_CONVERSATION_ID))
							+ ", from=" + c.getInt(c.getColumnIndex(KEY_FROM))
							+ ", to=" + c.getInt(c.getColumnIndex(KEY_TO)));
			long id = c.getLong(c.getColumnIndex(KEY_CONVERSATION_ID));
			c.close();
			// db.close();
			return id;
		}
	}

	// add a new message to DB
	public void addMessage(String from, String to, MessageModel message,
			boolean flag) {
		ContentValues values = new ContentValues();
		if (!checkMessageId(message.getMessageId())) {
			values.put(KEY_MESSAGE_ID, message.getMessageId());
			values.put(KEY_CONVERSATION_ID, message.getConversationId());
			values.put(KEY_FROM, from);
			values.put(KEY_TO, to);
			values.put(KEY_BODY, message.getMessage());
			values.put(KEY_DATE, message.getDateMessage());
			values.put(KEY_DISTANCE, message.getDistance());
			values.put(KEY_ERROR_MESSAGE, (flag == true) ? 1 : 0);
			SQLiteDatabase db = this.getWritableDatabase();
			db.insert(TABLE_MESSAGES, null, values);
			// db.close();
		}
	}

	// get a conversation from userid
	public boolean checkMessageId(long messageId) {
		String selectQuery = "";
		if (!checkEmptyTable(TABLE_MESSAGES)) {
			return false;
		}
		SQLiteDatabase db = this.getReadableDatabase();
		selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE "
				+ KEY_MESSAGE_ID + " = '" + messageId + "'";
		Cursor c = db.rawQuery(selectQuery, null);
		if (c == null) {
			// db.close();
			return false;
		} else if (!c.moveToFirst() || c.getCount() <= 0) {
			c.close();
			// db.close();
			return false;
		} else {
			c.close();
			// db.close();
			return true;
		}
	}

	// update conversationId and messageId and error(if have)
	public int updateMessage(long old_messageId, MessageModel new_message,
			boolean flag) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_CONVERSATION_ID, new_message.getConversationId());
		values.put(KEY_MESSAGE_ID, new_message.getMessageId());
		values.put(KEY_ERROR_MESSAGE, (flag == true) ? 1 : 0);
		// updating row
		int id = db.update(TABLE_MESSAGES, values, KEY_MESSAGE_ID + " = ?",
				new String[] { String.valueOf(old_messageId) });
		// db.close();
		return id;
	}

	public int updateMessage(long old_conversationId, long new_conversationId) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_CONVERSATION_ID, new_conversationId);
		// updating row
		int id = db.update(TABLE_MESSAGES, values, KEY_CONVERSATION_ID
				+ " = " + old_conversationId, null);
		return id;
	}

	public int updateMessage(long old_messageId, long new_messageId,
			boolean flag) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_MESSAGE_ID, new_messageId);
		values.put(KEY_ERROR_MESSAGE, (flag == true) ? 1 : 0);
		// updating row
		int id = db.update(TABLE_MESSAGES, values, KEY_MESSAGE_ID + " = ?",
				new String[] { String.valueOf(old_messageId) });
		return id;
	}

	public int deleteConversationId(long conversationId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int id = db.delete(TABLE_CONVERSATION, KEY_CONVERSATION_ID + " = ?",
				new String[] { String.valueOf(conversationId) });
		// db.close();
		return id;
	}

	public int deleteMessage(long messageId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int id = db.delete(TABLE_MESSAGES, KEY_MESSAGE_ID + " = ?",
				new String[] { String.valueOf(messageId) });
		// db.close();
		return id;
	}

	public int deleteAllMessages(long conversationId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int id = db.delete(TABLE_MESSAGES, KEY_CONVERSATION_ID + " = ?",
				new String[] { String.valueOf(conversationId) });
		// db.close();
		return id;
	}

	public int deleteAllMessagesFromUser(long conversationId, String from,
			String to) {
		SQLiteDatabase db = this.getWritableDatabase();
		int id = db.delete(TABLE_MESSAGES, KEY_CONVERSATION_ID + " = "
				+ conversationId + " AND " + KEY_FROM + " = " + from + " AND "
				+ KEY_TO + " = " + to, null);
		// db.close();
		return id;
	}

	// get all messages of a conversation
	public MessageModel getLastMessageOfAConversation(long conversation_id) {
		// Log.v(TAG, "conversation id = " + conversation_id);
		MessageModel message = null;
		String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE "
				+ KEY_CONVERSATION_ID + " = '" + conversation_id + "'" + " ORDER BY "+ KEY_CONVERSATION_ID +" ASC";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToLast()) {
			message = new MessageModel();
			UserModel user = new UserModel();
			if (cursor.getString(3).equals(Global.userInfo.getUserName())){
				//user = Global.userInfo;
			Global.cloneObj(user, Global.userInfo);
			}else {
				if (mChatType.getFromUser(cursor.getString(5)) != null)
					user = Global.setUserInfo(
							mChatType.getFromUser(cursor.getString(5)), null);
				Global.listChatFriend.add(user);
			}
			message.setConversationId(cursor.getLong(1));
			message.setMessageId(cursor.getLong(2));
			message.setUserInfo(user);
			message.setMessage(cursor.getString(5));
			message.setDateMessage(cursor.getLong(6));
			message.setDistance(cursor.getString(7));
			message.setError((cursor.getInt(8) == 1) ? true : false);
		}
		cursor.close();
		return message;
	}

	// get all messages of a conversation
	public Vector<MessageModel> getAllMessagesOfAConversation(
			long conversation_id) {
		// Log.v(TAG, "conversation id = " + conversation_id);
		Vector<MessageModel> contactList = new Vector<MessageModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE "
				+ KEY_CONVERSATION_ID + " = '" + conversation_id + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
//				Log.v(TAG,
//						 " MessageId = " + cursor.getInt(2));
				// Log.v(TAG,
				// " Conversation = " + cursor.getInt(1) + " MessageId="
				// + cursor.getInt(2) + " From="
				// + cursor.getString(3) + " To="
				// + cursor.getString(4) + " Body="
				// + cursor.getString(5) + " date="
				// + cursor.getLong(6) + " distance="
				// + cursor.getString(7));
				MessageModel message = new MessageModel();
				UserModel user = new UserModel();
				// Log.e(TAG, "cursor.getString(3) "+cursor.getString(3) +": "+
				// Global.userInfo.getUserName()
				// +":"+Global.userInfo.getUserId());
				if (cursor.getString(3).equals(Global.userInfo.getUserName())){
					 Global.cloneObj(user, Global.userInfo) ;
				     //user =Global.userInfo;
				}else {
					//Log.v(TAG, "vao day1 cursor "+cursor.getString(5));
					if (mChatType.getFromUser(cursor.getString(5)) != null)
						user = Global.setUserInfo(
								mChatType.getFromUser(cursor.getString(5)),
								null);
					Global.listChatFriend.add(user);
				}
				message.setConversationId(cursor.getLong(1));
				message.setMessageId(cursor.getLong(2));
				message.setUserInfo(user);
				message.setMessage(cursor.getString(5));
				message.setDateMessage(cursor.getLong(6));
				message.setDistance(cursor.getString(7));
				message.setError((cursor.getInt(8) == 1) ? true : false);
				// add list message
				contactList.add(message);
			} while (cursor.moveToNext());
			cursor.close();
		}
		return contactList;
	}

	// closing database
	public void closeDB() {
		// SQLiteDatabase db = this.getReadableDatabase();
		// if (db != null && db.isOpen())
		// //db.close();
	}
}