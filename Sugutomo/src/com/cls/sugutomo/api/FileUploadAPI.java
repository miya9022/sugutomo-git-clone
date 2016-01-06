package com.cls.sugutomo.api;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.profile.EditProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class FileUploadAPI extends APIClientBaseActivity {

	private static final String TAG = FileUploadAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private boolean isUploadImgInEditProfile=false;
    
	public FileUploadAPI(APICallbackInterface activity, Context context) {
		mActivity = activity;
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
		// mProgress.setTitle(mContext.getString(R.string.uploading));
		// mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// mProgress.setCanceledOnTouchOutside(false);
		// mProgress.setCancelable(false);
	}
	public FileUploadAPI(APICallbackInterface activity, Context context,boolean isUploadImgInEditProfile) {
		mActivity = activity;
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
//		mProgress.setTitle(mContext.getString(R.string.uploading));
//		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		mProgress.setCanceledOnTouchOutside(false);
//		mProgress.setCancelable(false);
		this.isUploadImgInEditProfile =isUploadImgInEditProfile;
	}

	public RequestParams getmParams() {
		return mParams;
	}

	public void setmParams(RequestParams mParams) {
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
						if(isUploadImgInEditProfile){
							JSONObject data =response.getJSONObject(DATA);
							ImageModel img =new ImageModel(data.getLong(Params.PARAM_IMAGE_ID), data.getString(Params.PARAM_FULL_PATH),Global.userInfo.getUserId());
						    ((EditProfileActivity) mActivity).addImageModelAfterUpload(img);
						}else{
							mActivity.handleReceiveData();
						}
						
					}else{
						ShowMessage.showDialog(mContext,
								mContext.getString(R.string.ERR_TITLE),
								mContext.getString(R.string.fail));
					}
				} catch (Exception e) {
					e.printStackTrace();
					if(isUploadImgInEditProfile) Global.isUploadingImageInEditProfile=false;
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					if(isUploadImgInEditProfile) Global.isUploadingImageInEditProfile=false;
				mProgress.dismiss();
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
		return BASE_URL + Params.REQUEST_METHOD_USER + Params.AVATAR_UPLOAD
				+ File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		client.setTimeout(60000);//60s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
