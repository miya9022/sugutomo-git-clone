package com.cls.sugutomo.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.StampModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;

public class ChatType {

	private static final String TAG = ChatType.class.getSimpleName();
	private static ChatType instance;
	public static final String TYPE_TEXT = "1";
	public static final String TYPE_IMAGE = "2";
	public static final String TYPE_STAMP = "3";
	public static final String TYPE_POINT ="4";
	public static final String TYPE_BLOCK ="5";
	public static final String TYPE_UNBLOCK ="6";
	public static final String TYPE_SUPPORT ="7";
	public static final String TYPE_BROADCAST ="8";
	// TYPES
	public static final String BLOCK_USER = "block";
	public static final String UNBLOCK_USER = "unblock";
	public static final String READ_TEXT = "read";
	public static final String TEXT = "text";
	public static final String FAVORITE = "favorite";
	public static final String NEW_REGISTER = "newregister";
	public static final String RANKING = "ranking";// MINH` LOT TOP
	public static final String STAMP = "stamp";
	public static final String IMAGE = "image";
	public static final String POINT = "point";
	public static final String FROM_BROADCAST = "from_broadcast";// user send
																	// broadcast
	public static final String FROM_NOTIFICATION = "from_notification";// admin
																		// send
																		// broadcast
																		// from
																		// web
	public static final String SUPPORT = "support";
	public static final String BROADCAST = "broadcast";// minh send broast cat to n user in sendallmessageActivity
	public static final String DELETE_MESSAGE = "delete_message";
	public static final String DELETE_CONVERSATION = "delete_conversation";
	public static final String FOOTPRINT = "footprint";

	public static final String ERROR_1000 = "1000";// user not found
	public static final String ERROR_1001 = "1001";// thieu point
	public static final String SEND_SUCCESS = "1002";

	// Json params
	private final String JSON_ID = "id";
	private final String JSON_TYPE = "type";
	private final String JSON_DATA = "data";
	private final String JSON_MESSAGE = "msg";
	private final String JSON_POINT_FEE = "point_fee";
	private final String JSON_TIME = "time";
	public static final String JSON_MESSAGE_ID = "message_id";
	private final String JSON_FROM_USER = "from_user";
	private final String JSON_TO_USER = "to_user";
	private final String JSON_POINT_RECEIVER = "point_receiver";
	private final String JSON_COUNT_USER = "count_user";
	public static final String JSON_CONVERSATION_ID = "conversation_id";
	private final String JSON_SEX = "sex";

	private Context mContext;

	public ChatType(Context context) {
		this.mContext = context;
	}

	public static ChatType getInstance(Context context) {
		if (instance == null)
			instance = new ChatType(context);
		return instance;
	}

	private Context getActivity() {
		return mContext;
	}

	/**************************************************/
	/****************** SEND JSON PARAM ***************/
	/**************************************************/

	/**
	 * CREATE JSON PARAM
	 * 
	 * @param type
	 * @param data
	 * @return
	 */
	public JSONObject createJSONParam(String type, JSONObject data) {
		JSONObject jsonParam = new JSONObject();

		try {
			jsonParam.put(JSON_TYPE, type);
			jsonParam.putOpt(JSON_DATA, data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonParam;
	}

	/**
	 * SEND BLOCK USER
	 * 
	 * @return
	 */
	public JSONObject sendBlockJSON(long id) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			jsonParam = createJSONParam(BLOCK_USER, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND UNBLOCK USER
	 * 
	 * @return
	 */
	public JSONObject sendUnblockJSON(long id) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			jsonParam = createJSONParam(UNBLOCK_USER, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND READ TEXT
	 * 
	 * @return
	 */
	public JSONObject sendReadTextJSON(long id) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			jsonParam = createJSONParam(READ_TEXT, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND TEXT MESSAGE
	 * 
	 * @param text
	 * @return
	 */
	public JSONObject sendTextJSON(long id, String text) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(JSON_MESSAGE, text);
			jsonParam = createJSONParam(TEXT, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	public JSONObject sendFavoriteJSON(long id, String text) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(JSON_MESSAGE, text);
			jsonParam = createJSONParam(FAVORITE, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND TO SUPPORT
	 * 
	 * @param id
	 * @param text
	 * @return
	 */
	public JSONObject sendSupportJSON(long id, String text) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(JSON_MESSAGE, text);
			jsonParam = createJSONParam(SUPPORT, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND PARAM_BROADCAST
	 * 
	 * @param id
	 * @param text
	 * @param count_user
	 * @return
	 */
	public JSONObject sendBroadCast(long id, String text, int count_user,
			int sex) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(JSON_COUNT_USER, count_user);
			data.put(JSON_MESSAGE, text);
			data.put(JSON_SEX, sex);
			jsonParam = createJSONParam(BROADCAST, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND IMAGE
	 * 
	 * @param text
	 * @return
	 */
	public JSONObject sendImageJSON(long id, int image_id, String image_url) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		Log.v("cHATTYPE", "ID image_url:"+image_id);
		Log.v("cHATTYPE", "image_url:"+image_url);
		try {
			data.put(JSON_ID, id);
			data.put(Params.PARAM_IMAGE_ID, image_id);
			data.put(Params.PARAM_IMAGE_FULL_URL, image_url);
			jsonParam = createJSONParam(IMAGE, data);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND STAMP
	 * 
	 * @param stamp_id
	 * @param image_url
	 * @return
	 */
	public JSONObject sendStampJSON(long id, int stamp_id, String image_url) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(Params.PARAM_STAMP_ID, stamp_id);
			data.put(Params.PARAM_IMAGE_FULL_URL, image_url);
			jsonParam = createJSONParam(STAMP, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**
	 * SEND POINT
	 * 
	 * @param point
	 * @return
	 */
	public JSONObject sendPointJSON(long id, int point) {
		JSONObject data = new JSONObject();
		JSONObject jsonParam = new JSONObject();
		try {
			data.put(JSON_ID, id);
			data.put(POINT, point);
			jsonParam = createJSONParam(POINT, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonParam;
	}

	/**************************************************/
	/************* RECEIVE JSON PARAM *****************/
	/**************************************************/

	/**
	 * RECEIVE JSON FROM OPENFIRE
	 * 
	 * @param receive
	 * @return
	 */
	public String receiveJSON(String receive) {
		try {
			JSONObject jsonObject = new JSONObject(receive);

			// parser result
			String type = jsonObject.optString(JSON_TYPE);
			JSONObject data = jsonObject.optJSONObject(JSON_DATA);
			if (type == null || type == "" || data == null
					|| data.length() <= 0)
				return null;
			else {
				return getType(type);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject getData(String receive) {
		try {
			JSONObject jsonObject = new JSONObject(receive);
			return jsonObject.optJSONObject(JSON_DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return null;
	}

	public String getTypeInData(String receive) {
		JSONObject data = getData(receive);
		if (data != null) {
			return data.optString(JSON_TYPE);
		} else
			return null;
	}

	public long getMessageId(JSONObject data) {
		return data.optLong(JSON_MESSAGE_ID);
	}

	public long getConversationId(String json) {
		long conversationId = -1;
		JSONObject data = getData(json);
		if (data != null) {
			try {
				conversationId = data.getLong(JSON_CONVERSATION_ID);
			} catch (JSONException e) {
				e.printStackTrace();
				conversationId = -1;
			}
		}
		return conversationId;
	}

	public long getMessageId(String json) {
		long messageId = 0;
		JSONObject data = getData(json);
		if (data != null) {
			messageId = data.optLong(JSON_MESSAGE_ID);
		}
		return messageId;
	}

	public JSONObject getFromUserFootprint(String json) {
		JSONObject data = getData(json);
		if (data == null)
			return null;
		else
			return data.optJSONObject("user");
	}

	public JSONObject getFromUser(String json) {
		JSONObject data = getData(json);
		if (data == null)
			return null;
		else
			return data.optJSONObject(JSON_FROM_USER);
	}

	public JSONObject getToUser(String json) {
		JSONObject data = getData(json);
		if (data == null)
			return null;
		else
			return data.optJSONObject(JSON_TO_USER);
	}

	public long getId(JSONObject data) {
		return data.optLong(JSON_ID);
	}

	public String getType(String type) {
		return type;
	}

	public long getTimeSent(String json) {
		JSONObject data = getData(json);
		long time = data.optLong(JSON_TIME);
		return time;
	}

	public boolean acceptDenyMessage(String type) {
		if (type.equals(ERROR_1000)) {
			return false;
		} else if (type.equals(ERROR_1001)) {
			return false;
		} else if (type.equals(SEND_SUCCESS)) {
			return true;
		} else
			return true;
	}

	public String executeError(String json) {
		String type = receiveJSON(json);
		if (type != null) {
			JSONObject data = getData(json);
			if (type.equals(ERROR_1000)) {
				return executeError1000(data);
			} else if (type.equals(ERROR_1001)) {
				return executeError1001(data);
			}
		}
		return null;
	}
	public int  executeGetFrom(String json) {
		JSONObject data = getData(json);
		Log.v("", "msg getfrom:"+data.toString());
		JSONObject from_user = data.optJSONObject(JSON_FROM_USER);
		return from_user.optInt(Params.PARAM_ID, -1);
	}
	public String executeGetMessageChat(String json) {
		String type = receiveJSON(json);
		JSONObject data = getData(json);
		// receive from server
		 if (type.equals(TEXT)) {
			return excuteText(data);
		} else if (type.equals(STAMP)) {
			return data.optString(Params.PARAM_STAMP_ID,"");
		} else if (type.equals(IMAGE)) {
			return data.optString(Params.PARAM_IMAGE_FULL_URL,"");
		} else if (type.equals(POINT)) {
			return excutePoint(data);
		} else if (type.equals(FROM_BROADCAST)) {
			// TODO only receive
			return executeBroadCast(data);
		} else if (type.equals(FROM_NOTIFICATION)) {
			// TODO only receive
			return executeNotification(data);
		} else if (type.equals(SUPPORT)) {
			return executeSupport(data);
		}
		return null;
	}
	public String executeReceive(String json) {
		String type = receiveJSON(json);
		JSONObject data = getData(json);
		// receive from server
		if (type.equals(BLOCK_USER)) {
			return excuteBlock_Unblock_ReadText(data, R.string.chat_block_user,
					R.string.chat_block_by_user);
		} else if (type.equals(UNBLOCK_USER)) {
			return excuteBlock_Unblock_ReadText(data,
					R.string.chat_unblock_user, R.string.chat_unblock_by_user);
		} else if (type.equals(READ_TEXT)) {
			return excuteBlock_Unblock_ReadText(data, R.string.chat_read,
					R.string.chat_read_by_user);
		} else if (type.equals(TEXT)) {
			return excuteText(data);
		} else if (type.equals(STAMP)) {
			return data.optString(Params.PARAM_STAMP_ID,"");
		} else if (type.equals(IMAGE)) {
			return data.optString(Params.PARAM_IMAGE_FULL_URL,"");
		} else if (type.equals(FAVORITE)) {
			// String msg =
			// String.format(mContext.getString(R.string.favorite_add), name);
			return excuteText(data);
		} else if (type.equals(POINT)) {
			return excutePoint(data);
		} else if (type.equals(FROM_BROADCAST)) {
			// TODO only receive
			return executeBroadCast(data);
		} else if (type.equals(FROM_NOTIFICATION)) {
			// TODO only receive
			return executeNotification(data);
		} else if (type.equals(SUPPORT)) {
			return executeSupport(data);
		}
		return null;
	}
	
	public String executeReceive(JSONObject data) {
		String type = data.optString(JSON_TYPE);
		// receive from server
		if (type.equals(BLOCK_USER)) {
			return excuteBlock_Unblock_ReadText(data, R.string.chat_block_user,
					R.string.chat_block_by_user);
		} else if (type.equals(UNBLOCK_USER)) {
			return excuteBlock_Unblock_ReadText(data,
					R.string.chat_unblock_user, R.string.chat_unblock_by_user);
		} else if (type.equals(READ_TEXT)) {
			return excuteBlock_Unblock_ReadText(data, R.string.chat_read,
					R.string.chat_read_by_user);
		} else if (type.equals(TEXT)) {
			return excuteText(data);
		} else if (type.equals(FAVORITE)) {
			// String msg =
			// String.format(mContext.getString(R.string.favorite_add), name);
			return excuteText(data);
		} else if (type.equals(POINT)) {
			return excutePoint(data);
		} else if (type.equals(FROM_BROADCAST)) {
			// TODO only receive
			return executeBroadCast(data);
		} else if (type.equals(FROM_NOTIFICATION)) {
			// TODO only receive
			return executeNotification(data);
		} else if (type.equals(SUPPORT)) {
			return executeSupport(data);
		}
		return null;
	}

	/**
	 * EXECUTE WHEN ERROR1000 OCCUR
	 * 
	 * @param data
	 * @return
	 */
	public String executeError1000(JSONObject data) {
		String user = data.optString(Params.USER);
		return getActivity()
				.getString(R.string.chat_error_user_not_found, user);
	}

	/**
	 * EXECUTE WHEN ERROR1001 OCCUR
	 * 
	 * @param data
	 * @return
	 */
	public String executeError1001(JSONObject data) {
		JSONObject to_user = data.optJSONObject(JSON_TO_USER);
		UserModel user = new UserModel();
		if (to_user != null) {
			user = Global.setUserInfo(to_user, null);
		}
		int point = data.optInt(POINT);
		int point_fee = data.optInt(JSON_POINT_FEE);

		return getActivity().getString(R.string.chat_error_not_enough_point,
				user.getName(), point, point_fee);
	}

	/**
	 * EXECUTE SEND SUCCESS
	 * 
	 * @param data
	 * @return
	 */
	public String executeSendSuccess(JSONObject data) {
//		long message_id = data.optLong(JSON_MESSAGE_ID);
		int point = data.optInt(POINT);
//		int point_fee = data.optInt(JSON_POINT_FEE);
//		String to_user = data.optString(JSON_TO_USER);
//		long time = data.optLong(JSON_TIME);

		if (point > 0)
			Global.userInfo.setPossessPoint(point);
		return getActivity().getString(R.string.chat_send_message_success);
	}

	/**
	 * EXECUTE BLOCK USER/ UNBLOCK USER/ READ TEXT
	 * 
	 * @param data
	 * @param resource
	 * @return
	 */
	public String excuteBlock_Unblock_ReadText(JSONObject data, int sent,
			int received) {
//		long message_id = data.optLong(JSON_MESSAGE_ID);
		JSONObject from_user = data.optJSONObject(JSON_FROM_USER);
		UserModel user = new UserModel();
		if (from_user != null)
			user = Global.setUserInfo(from_user, null);
//		long time = data.optLong(JSON_TIME);

		if (user.getName() == null)
			return getActivity().getString(sent);
		else
			return getActivity().getString(received, user.getName());
	}

	/**
	 * EXECUTE TEXT
	 * 
	 * @param data
	 * @return
	 */
	public String excuteText(JSONObject data) {
		String msg = "";
		msg = data.optString(JSON_MESSAGE);
		// Log.v("", "test favorite"+ msg);
		return msg;
	}

	/**
	 * EXECUTE SEND STAMP
	 * 
	 * @param data
	 * @return
	 */
	public StampModel excuteStamp(String json) {
		JSONObject data = getData(json);
//		long message_id = data.optLong(JSON_MESSAGE_ID);
		int stamp_id = data.optInt(Params.PARAM_STAMP_ID);
		String image_url = data.optString(Params.PARAM_IMAGE_FULL_URL);
//		long time = data.optLong(JSON_TIME);
		return new StampModel(stamp_id, null, String.valueOf(stamp_id),
				image_url);
	}

	/**
	 * EXECUTE SEND IMAGE
	 * 
	 * @param data
	 * @return
	 */
	public ImageModel excuteImage(String json) {
		// TODO
		Log.v("", "json: " + json);
		JSONObject data = getData(json);
		long id = data.optLong(JSON_ID);
//		long message_id = data.optLong(JSON_MESSAGE_ID);
		int image_id = data.optInt(Params.PARAM_IMAGE_ID);
		String image_url = data.optString(Params.PARAM_IMAGE_FULL_URL);
		JSONObject from_user = data.optJSONObject(JSON_FROM_USER);
		UserModel user = null;
		if (from_user != null)
			user = Global.setUserInfo(from_user, null);
//		long time = data.optLong(JSON_TIME);

		ImageModel imageModel = new ImageModel();
		imageModel.setImageId(id);
		imageModel.setFileName(String.valueOf(image_id));
		imageModel.setFullPath(image_url);
		imageModel.setUserModel(user);
		return imageModel;
	}

	/**
	 * EXECUTE SEND POINT
	 * 
	 * @param data
	 * @return
	 */
	public String excutePoint(JSONObject data) {
//		long message_id = data.optLong(JSON_MESSAGE_ID);
		int point = data.optInt(POINT);
		int point_receive = data.optInt(JSON_POINT_RECEIVER);
		int point_fee = data.optInt(POINT);
		JSONObject from_user = data.optJSONObject(JSON_FROM_USER);
		UserModel user = new UserModel();
		if (from_user != null)
			user = Global.setUserInfo(from_user, null);
//		long time = data.optLong(JSON_TIME);
		if (point > 0)
			Global.userInfo.setPossessPoint(point);
		String message = "";
		if (user.getName() == null)
			message = getActivity().getString(R.string.chat_point_send,
					point_fee);
		else
			message = getActivity().getString(R.string.chat_point_receive,
					user.getName(), point_receive);

		return message;
	}

	private String executeBroadCast(JSONObject data) {
		String msg = "";
		msg = data.optString(JSON_MESSAGE);
		return msg;
	}

	private String executeNotification(JSONObject data) {
		String msg = "";
		msg = data.optString(JSON_MESSAGE);
		return msg;
	}

	private String executeSupport(JSONObject data) {
		String msg = "";
		msg = data.optString(JSON_MESSAGE);
		return msg;
	}

	private void executeDeleteConversation() {
		// TODO Auto-generated method stub

	}

	private void executeDeleteMessage() {
		// TODO Auto-generated method stub

	}

	public JSONObject saveLostMessageJSON(JSONObject obj) {
		// 1: text
		// 2: image
		// 3: stamp
		// 4: point
		// 5: block
		// 6: unblock
		// 7: support
		// 8: broadcast
		JSONObject jsonParam = new JSONObject();
		try {
			int type = obj.getInt("type");
			long mesage_id = obj.getLong("id");
			String text = obj.getString("msg");
			String stampeid = obj.getString("stamp_id");
			long conversation_ID = obj.getLong("conversation_id");
			String time = obj.getLong("updated_datetime") * 1000 + "";

			JSONObject data = new JSONObject();
			data.put(JSON_TIME, time);
			data.put(JSON_CONVERSATION_ID, conversation_ID);
			data.put(JSON_FROM_USER, obj);
			data.put(JSON_MESSAGE_ID, mesage_id);
			//data.put(IMAGE, obj.getJSONObject(Params.USER).getString("image"));//IMG o day la avatar
			switch (type) {
			case 1:
				data.put(JSON_MESSAGE, text);
				jsonParam = createJSONParam(TEXT, data);
				break;
			case 2:
				data.put(Params.PARAM_IMAGE_FULL_URL, obj.getJSONObject(IMAGE).getString("full_url"));
				jsonParam = createJSONParam(IMAGE, data);
				break;
			case 3:
				data.put(Params.PARAM_STAMP_ID, stampeid);
				jsonParam = createJSONParam(STAMP, data);
				break;
			default:
				break;
			}
			Log.v("", "jsonPARAM TEXT rECIEVED " + jsonParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonParam;
	}
}
