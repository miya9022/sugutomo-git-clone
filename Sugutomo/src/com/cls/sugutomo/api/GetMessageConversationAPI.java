package com.cls.sugutomo.api;

import java.util.HashMap;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class GetMessageConversationAPI extends APIClientBaseActivity {

	private static final String TAG = GetMessageConversationAPI.class
			.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivityCallback;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
//	private long conversationIDcheck;
	private boolean useProgressbar;
	private UserModel chatUser;
	private Vector<MessageModel> listChat = new Vector<MessageModel>();
	private HashMap<String, String> mListMessageKey = new HashMap<String, String>();

	public GetMessageConversationAPI(APICallbackInterface activity,
			Activity context, Vector<MessageModel> listChat,
			UserModel chatUser, HashMap<String, String> mListMessageKey) {
		this.mActivityCallback = activity;
		this.mActivity = context;
		this.listChat = listChat;
		this.mProgress = new CustomizeProgressDialog(mActivity);
		this.chatUser = chatUser;
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
					if (isUseProgressbar())
						mProgress.dismiss();
//					String message = response.getString(DATA);
					// Log.v("","message converation "+message);
					if (OK.equals(response.getString(CODE))) {
						JSONArray data = response.getJSONObject(DATA)
								.getJSONArray("messages");
						if (data != null) {
							Log.v("", "data length : " + data.length());
							if (data.length() > 0) {
								// message luu tu new->old. nguoc lai de save
								// mesage theo thu tu old-> new in local
								Vector<MessageModel> list = new Vector<MessageModel>();
								int size = data.length();
								String type = "";
								for (int i = size - 1; i >= 0; i--) {
									// for (int i = 0; i < size; i++) {
									JSONObject obj = data.optJSONObject(i);
									type = obj.getString(Params.PARAM_TYPE);
									// ko show block va unblock mess vi`
									// chatlist Api ko tra ve message cua 2 type
									// nay
									// => ko show de do^ng` bo^. chat va
									// chatlist
									if (type.equals(ChatType.TYPE_BLOCK)
											|| type.equals(ChatType.TYPE_UNBLOCK))
										continue;
									UserModel um = Global.setUserInfo(
											obj.getJSONObject(Params.USER),
											null);
									MessageModel messageModel = new MessageModel(
											obj.getLong("conversation_id"),
											obj.getLong("id"), um, "", false);
									messageModel.setType(type);
									switch (type) {
									// 1: text
									// 2: image
									// 3: stamp
									// 4: point
									// 5: block
									// 6: unblock
									// 7: support
									// 8: broadcast
									case ChatType.TYPE_IMAGE:
										messageModel.setMessage(obj
												.getJSONObject(
														Params.PARAM_IMAGE)
												.getString("full_url"));
										break;
									case ChatType.TYPE_STAMP:
										messageModel
												.setMessage(obj
														.getString(Params.PARAM_STAMP_ID));
										break;
									case ChatType.TYPE_POINT:
										String mess = String
												.format(mActivity
														.getString(R.string.chat_point_receive),
														um.getName(),
														obj.getInt(Params.PARAM_PRESENT_POINT));
										messageModel.setMessage(mess);
										break;
									default:
										messageModel.setMessage(obj
												.getString(Params.MSG));
										break;
									}
									messageModel
											.setDateMessage(obj
													.getLong("updated_datetime") * 1000);// *
																							// 1000
									if (Global.userInfo != null
											&& chatUser != null)
										messageModel.setDistance(Global
												.distanceBetweenGPS(
														Global.userInfo,
														chatUser, mActivity));
									String messId = String.valueOf(messageModel
											.getMessageId());
									if (!mListMessageKey.containsKey(messId)) {
										list.add(messageModel);
										mListMessageKey.put(messId, messId);
									}

								}
								listChat.addAll(0, list);
								mActivityCallback.handleGetList();
							} else {
								((ChatDetailActivity) mActivityCallback)
										.loadMessageFail();
							}
						}
						// Global.newFootPrint = Integer.valueOf(response
						// .getJSONObject(DATA).getString("total"));
						// mActivityCallback.handleReceiveData();
					}else if(ERROR_10035.equals(response.getString(CODE))){
						//do nothing
						((ChatDetailActivity) mActivityCallback)
						.loadMessageFail();
					} else {
						if (isUseProgressbar())
							ShowMessage.showDialog(mActivity,
									mActivity.getString(R.string.ERR_TITLE),
									mActivity.getString(R.string.ERR_TITLE));
						((ChatDetailActivity) mActivityCallback)
								.loadMessageFail();
					}
				} catch (Exception e) {
					((ChatDetailActivity) mActivityCallback).loadMessageFail();
					if (isUseProgressbar())
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								mActivity.getString(R.string.ERR_TITLE));
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					((ChatDetailActivity) mActivityCallback).loadMessageFail();
					if (isUseProgressbar())
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					if (isUseProgressbar())
						ShowMessage.showDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE),
								mActivity.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + "chat/conversation";
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		//Log.v(TAG, "url request = " + getDefaultURL());
		client.setTimeout(20000);// 10s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
