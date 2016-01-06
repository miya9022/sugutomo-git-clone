package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.content.Context;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class DeleteConversationAPI extends APIClientBaseActivity {

	private static final String TAG = DeleteConversationAPI.class
			.getSimpleName();
	private Context mContext;
	private RequestParams mParams;
	private CustomizeProgressDialog mProgress;
	private long conversationId;

	public DeleteConversationAPI(Context context,long conversationId) {
		this.mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setCancelable(true);
//		mProgress.setCanceledOnTouchOutside(false);
		this.conversationId= conversationId;
	
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams mParams) {
		this.mParams = mParams;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				try {
					mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					mProgress.dismiss();
					 String message = response.toString();
//					 Log.v("", "response "+message);
					if (OK.equals(response.getString(CODE))) {
						// ShowMessage.showMessage(mContext, message);
						JSONObject data = response.getJSONObject(DATA);
						long conversationId = data
								.optLong(ChatType.JSON_CONVERSATION_ID);
						((ChatListActivityServer) mContext)
								.deleteConversationSuccess(conversationId);
					}else if(ERROR_10033.equals(response.getString(CODE))){
						//ko tim thay tren server thi del tren local luon, khoi phai nghi
						((ChatListActivityServer) mContext)
								.deleteConversationSuccess(conversationId);
					} else {
						Global.customDialog(((ChatListActivityServer) mContext), mContext.getString(R.string.fail), null);
//						ShowMessage.showDialog(mContext,
//								mContext.getString(R.string.ERR_TITLE),
//								mContext.getString(R.string.ERR_TITLE));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {

					mProgress.dismiss();
					Global.customDialog(((ChatListActivityServer) mContext), mContext.getString(R.string.ERR_CONNECT_FAIL), null);
//					ShowMessage.showDialog(mContext,
//							mContext.getString(R.string.ERR_TITLE),
//							mContext.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}

		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PARAM_CHAT + File.separator
				+ Params.PARAM_DELETE_CONVERSATION + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
