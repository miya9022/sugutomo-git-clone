package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class RankAPI extends APIClientBaseActivity {

	private static final String TAG = RankAPI.class.getSimpleName();
	public static int RANK_NEW = 1;
	public static int RANK_UPDATE = 2;
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private int mRequest;

	public RankAPI(APICallbackInterface activity, Context context, int request) {
		mActivity = activity;
		mContext = context;
		mRequest = request;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setTitle(mContext.getString(R.string.loading));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
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
//					Log.e(TAG, "messs: "+response.toString());
					
					if (OK.equals(response.getString(CODE))) {
						if (mRequest == RANK_UPDATE) {

						} else {
							// rank new
							JSONObject jData = response.getJSONObject(DATA);
							JSONArray maleArray = jData.getJSONArray("male");
							JSONArray femaleArray = jData
									.getJSONArray("female");

							if (maleArray != null && maleArray.length() > 0) {
								for (int i = 0; i < maleArray.length(); i++) {
									UserModel user = Global.setUserInfo(
											maleArray.getJSONObject(i), null);
									Global.listRankMan.add(user);
								}
							}
							if (femaleArray != null && femaleArray.length() > 0) {
								for (int i = 0; i < femaleArray.length(); i++) {
									UserModel user = Global.setUserInfo(
											femaleArray.getJSONObject(i), null);
									Global.listRankWomen.add(user);
								}
							}
						}
						mActivity.handleReceiveData();
					} else {
						mActivity.handleReceiveData();
						ShowMessage.showDialog(mContext, mContext.getString(R.string.ERR_TITLE),
								mContext.getString(R.string.ERR_TITLE));
					}
					mProgress.dismiss();
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
					Log.v(TAG, "onFailure:");
					ShowMessage.showDialog(mContext,
							mContext.getString(R.string.ERR_TITLE),
							mContext.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {

		return BASE_URL + Params.RANK + File.separator;
		//return "http://bnacky.net/news-crawler/public/";

	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		///return client.post(this, getDefaultURL(), null, responseHandler);
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
