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
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.ImageModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class UploadImageChatAPI extends APIClientBaseActivity {

	private static final String TAG = FileUploadAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private ImageModel mImageModel;

	public UploadImageChatAPI(APICallbackInterface activity, Context context,
			ImageModel imageModel, long timeCreated) {
		mActivity = activity;
		mContext = context;
		mProgress = new CustomizeProgressDialog(mContext);
		mImageModel = imageModel;
		if (mImageModel == null) {
			mImageModel = new ImageModel();
			mImageModel.setTimeCreated(timeCreated);
		}
		// mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// mProgress.setCanceledOnTouchOutside(false);
		// mProgress.setCancelable(true);
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
					Log.v("sending image upload", "sendding image upload: "
							+ response.toString());
					if (OK.equals(response.getString(CODE))) {
						JSONObject data = response.getJSONObject(DATA);
						mImageModel.setImageId(data
								.getInt(Params.PARAM_IMAGE_ID));
						mImageModel.setFileName(data
								.optString(Params.PARAM_FILE_NAME));
						mImageModel.setFullPath(data
								.optString(Params.PARAM_FULL_PATH));

						((ChatDetailActivity)mActivity).sendImage(mImageModel);
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
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + Params.CHAT_IMAGE_UPLOAD + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		client.setConnectTimeout(30000);
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}
