package com.cls.sugutomo.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class ChatListAPI extends APIClientBaseActivity {

	private static final String TAG = ChatListAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivityCallback;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private Vector<MessageModel> mListMessage;
	private boolean useProgressbar;
	private HashMap<String, String> mListMessageKey = new HashMap<String, String>();

	public ChatListAPI(APICallbackInterface activity, Activity context,
			Vector<MessageModel> list, HashMap<String, String> mListMessageKey) {
		this.mActivityCallback = activity;
		this.mActivity = context;
		this.mListMessage = list;
		this.mProgress = new CustomizeProgressDialog(mActivity);
		this.mListMessageKey = mListMessageKey;
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	public void setUseProgressbar(boolean isUse) {
		this.useProgressbar = isUse;
	}

	private boolean isUseProgressbar() {
		return this.useProgressbar;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {
			public void onStart() {
				super.onStart();
				try {
					if (isUseProgressbar())
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					ChatListActivityServer.LOAD_FINISH = true;
					if (isUseProgressbar())
						mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {
						JSONArray dataArr = response.getJSONArray(DATA);
						int size = dataArr.length();
						if (size < ChatListActivityServer.LIMIT_PAGE_CHATLIST) {
							ChatListActivityServer.canLoadMore = false;
						}
						int last_sender = -1;
						String distance = "";
						for (int i = 0; i < size; i++) {
							JSONObject obj = dataArr.getJSONObject(i);
							last_sender = obj.getInt("last_sender");
							long updateTime = obj.getLong("updated_datetime") * 1000;
							MessageModel message = new MessageModel();
							long conversaionID = obj.getLong(Params.PARAM_ID);
							String conID=String.valueOf(conversaionID);
							// da co roi` thi thoi bo qua, vi da duoc update qua push openfire
							if(mListMessageKey.containsKey(conID)) continue;
							
							mListMessageKey.put(conID, conID);
							message.setConversationId(conversaionID);
							// message.setMessageId(cursor.getLong(2));
							JSONObject user = obj.getJSONObject(Params.USER);
							UserModel chatUser = Global.setUserInfo(user, null);
							message.setMessage(obj.getString("last_message"));
							message.setType(ChatType.TYPE_TEXT);
							message.setDateMessage(updateTime);
							distance = Global.distanceBetweenGPS(chatUser,
									Global.userInfo, mActivity);
							message.setDistance(distance);
							message.setError(false);
							message.setUserInfo(chatUser);
							message.setLastSender(String.valueOf(last_sender));
							if (last_sender != Global.userInfo.getUserId()) {
								message.setNewMessages(obj.getInt("unread"));
							} else{
								message.setNewMessages(0);
							}
							mListMessage.add(message);
						}
						mActivityCallback.handleReceiveData();
					} else if (ERROR_10000.equals(response.getString(CODE))) {
						Global.backToLogin(mActivity);
					} else {
						Global.customDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE), null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					// ShowMessage.showDialog(mActivity,
					// mActivity.getString(R.string.ERR_TITLE),
					// mActivity.getString(R.string.ERR_TITLE));
					Global.customDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE), null);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					ChatListActivityServer.LOAD_FINISH = true;
					if (isUseProgressbar())
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					// ShowMessage.showDialog(mActivity,
					// mActivity.getString(R.string.ERR_TITLE),
					// mActivity.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog(mActivity,
							mActivity.getString(R.string.ERR_CONNECT_FAIL),
							null);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + "chat" + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		Log.v(TAG, "url request = " + getDefaultURL());
		client.setTimeout(20000);// 10s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
