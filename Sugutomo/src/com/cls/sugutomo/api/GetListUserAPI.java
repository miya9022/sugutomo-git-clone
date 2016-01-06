package com.cls.sugutomo.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.APIClientBaseActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.dialog.CustomizeProgressDialog;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

public class GetListUserAPI extends APIClientBaseActivity {

	private static final String TAG = GetListUserAPI.class.getSimpleName();
	private RequestParams mParams;
	private APICallbackInterface mActivityCallback;
	private Activity mActivity;
	private CustomizeProgressDialog mProgress;
	private Vector<UserModel> mListUser;
	private String mRequest;
	private boolean useProgressbar;

	public GetListUserAPI(APICallbackInterface activity, Activity context,
			Vector<UserModel> list, String request) {
		this.mActivityCallback = activity;
		this.mActivity = context;
		this.mListUser = list;
		this.mRequest = request;
		this.mProgress = new CustomizeProgressDialog(mActivity);
		// this.mProgress.setTitle(mActivity.getString(R.string.loading));
		// this.mProgress.setCanceledOnTouchOutside(false);
		// this.mProgress.setCancelable(true);
		// mProgress.setIndeterminate(true);
		// mProgress.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_animation));

	}

	public String getRequest() {
		return mRequest;
	}

	public RequestParams getParams() {
		return mParams;
	}

	public void setParams(RequestParams params) {
		this.mParams = params;
	}

	public void setUseProgressbar(boolean isUse) {
		this.useProgressbar = isUse;
	}

	private boolean isUseProgressbar() {
		return this.useProgressbar;
	}

	@Override
	public ResponseHandlerInterface getResponseHandler() {
		return new JsonHttpResponseHandler() {
			public void onStart() {
				super.onStart();
				try {
					if (isUseProgressbar())
						mProgress.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
					String message = response.getString(MESSAGE);
					if (OK.equals(response.getString(CODE))) {
						if (mRequest == Params.APPEAL
								|| mRequest == Params.FAVORITE) {
							JSONArray data = response.getJSONArray(DATA);
							if (data != null && data.length() > 0) {
								if (mRequest == Params.APPEAL)mListUser.clear();
								for (int i = 0; i < data.length(); i++) {
									UserModel user = Global.setUserInfo(
											data.getJSONObject(i), null);
									mListUser.add(user);
								}
							}
						} else {// userlist
							JSONArray data = response.getJSONObject(DATA)
									.getJSONArray("users");
							if (data != null && data.length() > 0) {
								if (Global.LIMIT_USER_IN_PAGE == data.length())
									Global.LOAD_MORE_USER = true;
								else
									Global.LOAD_MORE_USER = false;
								for (int i = 0; i < data.length(); i++) {
									UserModel user = Global.setUserInfo(
											data.getJSONObject(i), null);
									if (Global.listFriendMap.get(user
											.getUserName()) == null) {
										mListUser.add(user);
										Global.listFriendMap.put(
												user.getUserName(), user);
									}

								}
							} else
								Global.LOAD_MORE_USER = false;
							// profile mListProfileModel
							if (Global.mListProfileModel.size() == 0) {
								data = response.getJSONObject(DATA)
										.getJSONArray(PROFILES);
								for (int i = 0; i < data.length(); i++) {
									JSONObject item = data.getJSONObject(i);
									int profile_id = item
											.getInt(Params.PARAM_PROFILE_ID);
									String profile_name = item
											.getString(Params.PARAM_PROFILE_NAME);
									int profile_display_order = item
											.getInt(Params.PARAM_PROFILE_DISPLAY);
									int is_input = item
											.getInt(Params.PARAM_IS_INPUT);
									String userdata = "";// item.getString(Params.PARAM_USER_DATA);
									JSONArray profile_items = item
											.getJSONArray(Params.PARAM_PROFILE_ITEMS);
									String valueText = "";
									ArrayList<ProfileItem> listProfileItem = new ArrayList<ProfileItem>();
									if (profile_items.length() > 0) {
										for (int j = 0; j < profile_items
												.length(); j++) {
											JSONObject subItem = profile_items
													.getJSONObject(j);
											int profile_item_id = subItem
													.getInt(Params.PARAM_PROFILE_ITEM_ID);
											String profile_item_name = subItem
													.getString(Params.PARAM_PROFILE_ITEM_NAME);
											int profile_item_display_order = subItem
													.getInt(Params.PARAM_PROFILE_ITEM_DISPLAY);
											ProfileItem subProfileItem = new ProfileItem(
													profile_id,
													profile_item_id,
													profile_item_name,
													profile_item_display_order);
											listProfileItem.add(subProfileItem);
											if (userdata.equalsIgnoreCase(""
													+ profile_item_id))
												valueText = profile_item_name;
										}
									} else {
										valueText = userdata;
									}
									ProfileModel profileModel = new ProfileModel(
											profile_id, profile_name,
											profile_display_order, is_input,
											valueText, userdata,
											listProfileItem);
									profileModel.printProfile();
									Global.mListProfileModel.add(profileModel);
								}
							}

							//
						}

						// Log.i(TAG, "size list user = " + mListUser.size());
						if (mRequest == Params.USER
								|| mRequest == Params.FAVORITE)
							mActivityCallback.handleReceiveData();
						else if (mRequest == Params.APPEAL)
							mActivityCallback.handleGetList();
						if (mRequest == Params.FAVORITE)
							Global.IS_LOADED_FAVORITE_LIST = true;
					} else if (ERROR_10000.equals(response.getString(CODE))) {
						Global.backToLogin(mActivity);
					} else {
						if (mRequest == Params.USER)// || mRequest ==
													// Params.FAVORITE)
							mActivityCallback.handleReceiveData();
						else if (mRequest == Params.APPEAL) {
							mActivityCallback.handleGetList();
							return;
						}
						// ShowMessage.showDialog(mActivity,
						// mActivity.getString(R.string.ERR_TITLE),
						// message);
						Global.customDialog(mActivity,
								mActivity.getString(R.string.ERR_TITLE), null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (mRequest == Params.USER)// || mRequest ==
												// Params.FAVORITE)
						mActivityCallback.handleReceiveData();
					else if (mRequest == Params.APPEAL) {
						mActivityCallback.handleGetList();
						return;
					}
					// ShowMessage.showDialog(mActivity,
					// mActivity.getString(R.string.ERR_TITLE),
					// mActivity.getString(R.string.ERR_TITLE));
					Global.customDialog(mActivity,
							mActivity.getString(R.string.ERR_TITLE), null);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					if (isUseProgressbar())
						mProgress.dismiss();
					Log.v(TAG, "onFailure:");
					if (mRequest == Params.USER) {// || mRequest ==
													// Params.FAVORITE)
						// Global.LOAD_MORE_USER =true;
						mActivityCallback.handleReceiveData();
					} else if (mRequest == Params.APPEAL) {
						mActivityCallback.handleGetList();
						return;
					}
					// ShowMessage.showDialog(mActivity,
					// mActivity.getString(R.string.ERR_TITLE),
					// mActivity.getString(R.string.ERR_CONNECT_FAIL));
					Global.customDialog(mActivity,
							mActivity.getString(R.string.ERR_CONNECT_FAIL),
							null);
				} catch (Exception e) {
					// TODO: handle exception
					if (mRequest == Params.USER)// || mRequest ==
						// Params.FAVORITE)
						mActivityCallback.handleReceiveData();
					else if (mRequest == Params.APPEAL) {
						mActivityCallback.handleGetList();
						return;
					}
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public String getDefaultURL() {
		return BASE_URL + mRequest + File.separator;
	}

	@Override
	public RequestHandle executeRequest(AsyncHttpClient client, String URL,
			Header[] headers, HttpEntity entity,
			ResponseHandlerInterface responseHandler) {
		//Log.v(TAG, "url request = " + getDefaultURL());
		client.setTimeout(20000);// 10s
		return client.post(this, getDefaultURL(), mParams, responseHandler);
	}

}
