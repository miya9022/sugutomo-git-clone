package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.model.MessageModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class DeleteMessageAPI extends APIClientBaseActivity {

	private static final String TAG = DeleteMessageAPI.class
			.getSimpleName();
	private Context mContext;
	private RequestParams mParams;
private MessageModel model;
	public DeleteMessageAPI(Context context, MessageModel model) {
		this.mContext = context;
		this.model = model;
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
			}

			@Override
			public void onCancel() {
				super.onCancel();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
//					String message = response.getString(MESSAGE);
					if (OK.equals(response.getString(CODE))) {
						ShowMessage.showMessage(mContext, mContext.getString(R.string.success));
						JSONObject data = response.getJSONObject(DATA);
						long conversationId = data
								.optLong(ChatType.JSON_CONVERSATION_ID);
						long messageId = data.optLong(ChatType.JSON_MESSAGE_ID);
						if (conversationId != 0 && messageId != 0)
							((ChatDetailActivity) mContext)
									.updateAfterdeleteMessage(model);
					} else {
						ShowMessage
								.showDialog(mContext,
										mContext.getString(R.string.ERR_TITLE),
										mContext.getString(R.string.fail));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Log.v(TAG, "onFailure:");
				ShowMessage.showDialog(mContext,
						mContext.getString(R.string.ERR_TITLE),
						mContext.getString(R.string.ERR_CONNECT_FAIL));
			}

		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PARAM_CHAT + File.separator
				+ Params.PARAM_DELETE_MESSAGE + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
