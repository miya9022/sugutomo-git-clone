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
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class FavoriteAPI extends APIClientBaseActivity {

	private static final String TAG = FavoriteAPI.class.getSimpleName();
	public static int ADD = 1;
	public static int REMOVE = 2;
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private int mRequest;
	private int indexInFavoriteList;
	private UserModel user;

	public FavoriteAPI(APICallbackInterface activity, Context context,
			int request, UserModel _user) {

		mActivity = activity;
		mContext = context;
		mRequest = request;
		mProgress = new CustomizeProgressDialog(mContext);
//		if (mRequest == FavoriteAPI.ADD)
//			mProgress.setTitle(mContext.getString(R.string.add));
//		else
//			mProgress.setTitle(mContext.getString(R.string.remove));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);

		user = _user;
		int size = Global.listFavorite.size();
		UserModel um = null;
		indexInFavoriteList = -1;
		for (int i = 0; i < size; i++) {
			um = Global.listFavorite.elementAt(i);
			if (_user.getUserId() == um.getUserId())
				indexInFavoriteList = i;
		}

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
					Log.v("", "favortie resp: " + response.toString());
					if (OK.equals(response.getString(CODE))) {
						// JSONObject jData = response.getJSONObject(DATA);
						mActivity.handleReceiveData();
						if (mRequest == FavoriteAPI.ADD) {
							Global.listFavorite.add(user);
						} else {
							Global.listFavorite
									.removeElementAt(indexInFavoriteList);
						}
//						ShowMessage.showDialog(mContext, "",
//								response.getString(MESSAGE));
						Global.customDialog((Activity)mActivity, mContext.getString(R.string.success), null);
					} else {
						Global.isRequestingFavorite1User = false;// ko favorite
																	// dc thi
																	// set lai =
																	// false
						if (ERR_10048.equals(response.getString(CODE))) {
//							ShowMessage.showDialog(mContext, mContext.getString(R.string.ERR_TITLE),
//									mContext.getString(R.string.favorite_in_hour));
							Global.customDialog((Activity)mActivity, mContext.getString(R.string.favorite_in_hour), null);
						} else {
							Global.customDialog((Activity)mActivity, mContext.getString(R.string.fail), null);
//							ShowMessage.showDialog(mContext, "",
//									mContext.getString(R.string.ERR_TITLE));
						}
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
					Log.v(TAG, "onFailure:");
//					ShowMessage.showDialog(mContext,
//							mContext.getString(R.string.ERR_TITLE),
//							mContext.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog((Activity)mActivity, mContext.getString(R.string.ERR_CONNECT_FAIL), null);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		if (mRequest == FavoriteAPI.ADD)
			return BASE_URL + Params.FAVORITE + File.separator
					+ Params.FAVORITE_ADD + File.separator;
		else
			return BASE_URL + Params.FAVORITE + File.separator
					+ Params.FAVORITE_REMOVE + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
