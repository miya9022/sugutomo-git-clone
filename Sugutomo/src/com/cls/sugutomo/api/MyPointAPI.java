package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class MyPointAPI extends APIClientBaseActivity {

	private static final String TAG = MyPointAPI.class.getSimpleName();
	public static int RANK_NEW = 1;
	public static int RANK_UPDATE = 2;
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private boolean isUseProgressBar=true;

	public MyPointAPI(APICallbackInterface activity, Context context) {
		mActivity = activity;
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setTitle(mContext.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
	}
public void setUseProgresBar(boolean isUseProgressBar){
	this.isUseProgressBar=isUseProgressBar;
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
					if(isUseProgressBar) mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					if(isUseProgressBar)	mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if(isUseProgressBar) mProgress.dismiss();
					if (OK.equals(response.getString(CODE))) {
						
							// rank new
							JSONObject jData = response.getJSONObject(DATA);
							int point = jData.getInt(Params.PARAM_POINT);
							if(Global.userInfo!=null) Global.userInfo.setPossessPoint(point);
							mActivity.handleReceiveData();
					} else {
						Global.customDialog((Activity) mContext, mContext.getString(R.string.fail), null);
//						ShowMessage.showDialog(mContext, mContext.getString(R.string.ERR_TITLE),
//								response.getString(MESSAGE));
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
					if(isUseProgressBar)	mProgress.dismiss();
//					ShowMessage.showDialog(mContext,
//							mContext.getString(R.string.ERR_TITLE),
//							mContext.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog((Activity) mContext, mContext.getString(R.string.ERR_CONNECT_FAIL), null);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {

		return BASE_URL + Params.USER + File.separator+"my_point"+ File.separator;

	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
