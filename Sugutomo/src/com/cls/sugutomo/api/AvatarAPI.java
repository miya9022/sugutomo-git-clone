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
import com.cls.sugutomo.profile.EditProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class AvatarAPI extends APIClientBaseActivity {

	private static final String TAG = AvatarAPI.class.getSimpleName();
	public static int SET_DEFAULT = 1;
	public static int REMOVE = 2;
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private int mRequest;
	private boolean isEditProfile;

	public AvatarAPI(APICallbackInterface activity, Context context,
			int request, boolean isEditProfile) {

		mActivity = activity;
		mContext = context;
		mRequest = request;
		mProgress = new CustomizeProgressDialog(mContext);
//		if (mRequest == AvatarAPI.SET_DEFAULT)
//			mProgress.setTitle(mContext.getString(R.string.set_avatar));
//		else
//			mProgress.setTitle(mContext.getString(R.string.set_avatar));
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
		this.isEditProfile = isEditProfile;
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
					if (OK.equals(response.getString(CODE))) {
						// JSONObject jData = response.getJSONObject(DATA);
						if (isEditProfile) {
							// cap nhat lai imageid,url avatar
							JSONObject jData = response.getJSONObject(DATA);
							if (mRequest == AvatarAPI.SET_DEFAULT) {
								int id = jData.getInt(Params.PARAM_AVATAR_ID);
								String url = jData
										.getString(Params.PARAM_AVATAR_FULL_URL);
								Global.userInfo.setImageId(id);
								Global.userInfo.setUserAvatar(url);
							} else {// delete
								// image_id
							}
							((EditProfileActivity) mActivity)
									.updateAvatar(mRequest);
						}
						if (mRequest == AvatarAPI.SET_DEFAULT)
							ShowMessage.showDialog(mContext,
									mContext.getString(R.string.avatar),
									mContext.getString(R.string.set_avatar_ok));
						else
							ShowMessage.showDialog(mContext,
									mContext.getString(R.string.avatar),
									mContext.getString(R.string.del_img_ok));
					} else {
						if (mRequest == AvatarAPI.SET_DEFAULT)
							ShowMessage.showDialog(mContext, mContext
									.getString(R.string.avatar), mContext
									.getString(R.string.set_avatar_fail));
						else
							ShowMessage.showDialog(mContext,
									mContext.getString(R.string.avatar),
									mContext.getString(R.string.del_img_fail));
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
					ShowMessage.showDialog(mContext,
							mContext.getString(R.string.ERR_TITLE),
							mContext.getString(R.string.ERR_CONNECT_FAIL));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		if (mRequest == AvatarAPI.REMOVE)
			return BASE_URL + Params.USER + File.separator
					+ Params.AVATAR_REMOVE + File.separator;
		else
			return BASE_URL + Params.USER + File.separator
					+ Params.AVATAR_SETDEFAULT + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
