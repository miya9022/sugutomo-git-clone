package com.cls.sugutomo.adapter;

import java.io.File;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.BuyPointActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.BuyPointModelItem;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class AppealAPI extends APIClientBaseActivity {

	private static final String TAG = AppealAPI.class.getSimpleName();
	public static int LOAD_PRICE = 1;
	public static int BUY_APPEAL = 2;
	private RequestParams mParams;
	private APICallbackInterface mActivity;
	private Context mContext;
	private CustomizeProgressDialog mProgress;
	private int mRequest;
	private Vector<BuyPointModelItem> listPrice;
private AlertDialog dialog;
	public AppealAPI(APICallbackInterface activity, Context context,
			Vector<BuyPointModelItem> list, int request) {

		mActivity = activity;
		mContext = context;
		mRequest = request;
		listPrice = list;
		mProgress = new CustomizeProgressDialog(mContext);
		if (mRequest == AppealAPI.LOAD_PRICE)
			mProgress.setTitle(mContext.getString(R.string.loading_price));
		else
			mProgress.setTitle(((BaseTabActivity) mActivity).getTitle()
					.toString());
		mProgress.setCanceledOnTouchOutside(false);
		mProgress.setCancelable(false);

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
						if (mRequest == LOAD_PRICE) {
							JSONArray jData = response.getJSONArray(DATA);
							for (int i = 0; i < jData.length(); i++) {
								JSONObject obj = jData.getJSONObject(i);
								int cost = obj.optInt("point");
								int duration = obj
										.optInt(Params.PARAM_DURATION);
								listPrice.add(new BuyPointModelItem(cost,
										duration));
							}
							mActivity.handleGetList();

						} else {
							JSONObject jData = response.getJSONObject(DATA);
							JSONObject jUser = jData.getJSONObject(USER);
							int possion_point = jUser.optInt("possess_point");
							Global.userInfo.setPossessPoint(possion_point);
							mActivity.handleReceiveData();
							String mess = String.format(
									mContext.getString(R.string.appeal_result),
									possion_point);
							ShowMessage.showDialog(mContext,
									((BaseTabActivity) mActivity).getTitle()
											.toString(), mess);
						}
					}else if(ERROR_10024.equals(response.getString(CODE))){
						//thieu point, bao user mua them
						
						dialog= Global.customDialog((BaseTabActivity) mActivity, mContext.getString(R.string.point_need_more_question_buypoint), new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								Intent intent = new Intent((BaseTabActivity) mActivity, BuyPointActivity.class);
								intent.putExtra(Params.PARAM_TITLE, mContext.getString(R.string.buy_point_title));
								intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								((BaseTabActivity) mActivity).startActivity(intent);
							}
						});
					} else {
						Global.customDialog((BaseTabActivity) mActivity, mContext.getString(R.string.fail), null);
//						ShowMessage.showDialog(mContext,
//								((BaseTabActivity) mActivity).getTitle()
//										.toString(), response
//										.getString(MESSAGE));
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
//					Log.v(TAG, "onFailure:");
//					ShowMessage.showDialog(mContext,
//							mContext.getString(R.string.ERR_TITLE),
//							mContext.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog((BaseTabActivity) mActivity, mContext.getString(R.string.ERR_CONNECT_FAIL), null);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		if (mRequest == AppealAPI.LOAD_PRICE)
			return BASE_URL + Params.APPEAL + File.separator + "setting"
					+ File.separator;
		else
			return BASE_URL + Params.APPEAL + File.separator + "buy"
					+ File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}
}