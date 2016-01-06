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
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class ReportUserAPI extends APIClientBaseActivity {

	private static final String TAG = ReportUserAPI.class.getSimpleName();
	private RequestParams mParams;
	private Context mContext;
	private CustomizeProgressDialog mProgress;

	public ReportUserAPI(Context context) {
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setTitle(mContext.getString(R.string.sending));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(true);
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
					String message = response.getString(MESSAGE);
					if (OK.equals(response.getString(CODE))) {
						ShowMessage.showMessage(mContext, message);
					} else {
						ShowMessage
								.showDialog(mContext,
										mContext.getString(R.string.ERR_TITLE),
										message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				try {
					mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					ShowMessage.showDialog(mContext,
							mContext.getString(R.string.ERR_TITLE),
							mContext.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.USER + File.separator + Params.PARAM_REPORT
				+ File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
