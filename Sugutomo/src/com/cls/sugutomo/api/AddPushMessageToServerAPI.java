package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class AddPushMessageToServerAPI extends APIClientBaseActivity {

	private static final String TAG = AddPushMessageToServerAPI.class
			.getSimpleName();
	private RequestParams mParams;
	private CustomizeProgressDialog mProgress;
	private boolean useProgressbar;
	private MessageModel  model;
	private ChatBackgroundService service;
	public AddPushMessageToServerAPI(ChatBackgroundService _service,
			Activity context, MessageModel model) {
		this.service=_service;
		this.model = model;
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
					if (OK.equals(response.getString(CODE))) {
						Global.getUnReadMessageTotal(service);
						Log.v("", "msg onSuccess"+response.toString());
						JSONObject data =response.getJSONObject(DATA);
						model.setConversationId(data.optLong(Params.PARAM_CONVESATION_ID));
						model.setMessageId(data.optLong(Params.PARAM_MESSAGE_ID));
						service.updateChatAndChatList(model);
						if (Global.appInBackGround) {
							Log.e(TAG, "inbackground");
							Global.buildNotification(model, true, service);
						} else {
							if (Global.chatDetailActivity != null) {
								if (Global.chatDetailActivity.isPausing) {
									Global.buildNotification(model, false,service);
								} else {
									if (Global.chatDetailActivity.chatUser != null) {
										if (model.getUserInfo().getUserId()!=Global.chatDetailActivity.chatUser
												.getUserId()) {
											Global.buildNotification(model, false,service);
										}
									}
								}
								Log.e(TAG, "chat activity");
							} else {
								Global.buildNotification(model, false,service);
							}
						}
						
					} else if (ERROR_10000.equals(response.getString(CODE))) {
						//if(mActivity!=null)Global.backToLogin(mActivity);
					} else {
						//if(mActivityCallback!=null)mActivityCallback.handleGetList();
						// Global.customDialog(mActivity,
						// mActivity.getString(R.string.ERR_TITLE), null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					// ShowMessage.showDialog(mActivity,
					// mActivity.getString(R.string.ERR_TITLE),
					// mActivity.getString(R.string.ERR_TITLE));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + "chat" + File.separator + "addmessage";
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
