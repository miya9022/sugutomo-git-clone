package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

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
import com.payment.util.Purchase;

public class InAppPurchaseAPI extends APIClientBaseActivity {

	private static final String TAG = InAppPurchaseAPI.class.getSimpleName();

	private RequestParams mParams;
	private InAppPurchaseAPICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private boolean showProgress, isNonconsumeItem;
	private Purchase purchaseInfo;

	public InAppPurchaseAPI(InAppPurchaseAPICallbackInterface activity,
			Context context, Purchase _purchaseInfo, boolean _showProgress,
			boolean _isNonconsumeItem) {

		mActivity = activity;
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setTitle(mContext.getString(R.string.verify_purchase));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
		this.showProgress = _showProgress;
		purchaseInfo = _purchaseInfo;
		isNonconsumeItem = _isNonconsumeItem;
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

					if (showProgress)
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {

					if (showProgress)
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (showProgress)
						mProgress.dismiss();
					Log.e("", "" + response.getString(MESSAGE));
					if (OK.equals(response.getString(CODE))) {
						// cap nhat lai point
						JSONObject jData = response.getJSONObject(DATA);
						int pointPossesion = jData.getInt(Params.PARAM_POINT);
						if (Global.userInfo != null)
							Global.userInfo.setPossessPoint(pointPossesion);
						mActivity.handleReceiveData(true, isNonconsumeItem,
								response.getString(MESSAGE), purchaseInfo);
						if (Global.userInfo != null) {
							String str = String.format(mContext
									.getString(R.string.PURCHASE_SUCESS),
									Global.userInfo.getPossessPoint());
//							ShowMessage.showDialog(mContext,
//									mContext.getString(R.string.notice), str);
							Global.customDialog((Activity)mContext, str, null);
						}
					} else if ("10041".equals(response.getString(CODE))) {// 10041:
																			// duplicate
																			// purchase
						// send consumeAsync Item to google play
						mActivity.handleReceiveData(true, isNonconsumeItem,
								response.getString(MESSAGE), purchaseInfo);
					} else {

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
					if (showProgress)
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
//					ShowMessage.showDialog(mContext,
//							mContext.getString(R.string.ERR_TITLE),
//							mContext.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog((Activity)mContext, mContext.getString(R.string.ERR_CONNECT_FAIL), null);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.PURCHASE + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		client.setTimeout(15000);// 15s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
