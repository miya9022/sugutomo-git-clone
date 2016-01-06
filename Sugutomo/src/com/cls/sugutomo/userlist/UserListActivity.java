package com.cls.sugutomo.userlist;

import java.io.File;
import java.util.Map;
import java.util.Vector;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.BuyVipActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.adapter.CustomGridViewAdapter;
import com.cls.sugutomo.adapter.EndlessScrollListener;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.BlockedByMeAPI;
import com.cls.sugutomo.api.GetFootprintAPI;
import com.cls.sugutomo.api.GetListUserAPI;
import com.cls.sugutomo.api.LoadStampAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.dialog.DialogTwoButton;
import com.cls.sugutomo.dialog.DialogTwoButton.FilterVaule;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.loadimg.ImageLoader;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.RequestParams;

public class UserListActivity extends BaseTabActivity implements
		APICallbackInterface, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String TAG = UserListActivity.class.getSimpleName();
	private LinearLayout horizontalLayout;
	private TextView nameUserAppeal;
	private ImageView imgUserAppeal;
	private HorizontalScrollView hsScrollView;
	private DialogTwoButton dtb;
	private ObjectAnimator animator;
	Vector<UserModel> listTopAppeal;
	CustomGridViewAdapter customGridAdapter;
	private GridView gridview;
	private SessionManager mSession;
	private boolean isScrolling = false;
	private boolean countDownFinish = true;
	private boolean animationRuning = false;
	private FilterVaule filterVaule;
	// data
	private GetListUserAPI apiListFriend, apiListAppeal;
	private ImageLoader imgLoader;
	private String[] navMenuTitles;
	private int mPage = 1;
	private String title;
	private static int MAX_REFRESH_TIME = 60000;// 60S
	private ImageButton refreshApeal, refreshUser;
	private boolean LEFT_TO_RIGHT = true;
	private float currentScroll;
	private boolean cancelAnimation = false;
	private ImageFetcher imageFetcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// View decorView = getWindow().getDecorView();
		// if (Build.VERSION.SDK_INT >= 16) {
		// decorView.setSystemUiVisibility(
		// View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		// | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		// }
		// UI
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.user_list_activity,
				null, false);
		frameLayout.addView(activityView);
		filterVaule = null;
		// set title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		} else {
			navMenuTitles = getResources().getStringArray(
					R.array.nav_drawer_items);
			title = navMenuTitles[0];
			setTitle(title);
		}
		hsScrollView = (HorizontalScrollView) findViewById(R.id.hsScrollView);
		horizontalLayout = (LinearLayout) findViewById(R.id.horizontalLayout);
		gridview = (GridView) findViewById(R.id.gridview);
		refreshApeal = (ImageButton) findViewById(R.id.appeal_refresh);
		refreshUser = (ImageButton) findViewById(R.id.user_refresh);
		refreshApeal.setVisibility(View.GONE);
		refreshUser.setVisibility(View.GONE);
		imgLoader = new ImageLoader(this);

		mSession = SessionManager.getInstance(getApplicationContext());
		Global.listFriend = new Vector<UserModel>();
		Global.listFriendMap.clear();
		listTopAppeal = new Vector<UserModel>();
		// load list user
		customGridAdapter = new CustomGridViewAdapter(UserListActivity.this,
				R.layout.custom_img, Global.listFriend);
		gridview.setAdapter(customGridAdapter);
		gridview.setOnScrollListener(new EndlessScrollListener() {

			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				// Triggered only when new data needs to be appended to the list
				// Add whatever code is needed to append new items to your
				// AdapterView
				customLoadMoreDataFromApi(page);
				// or customLoadMoreDataFromApi(totalItemsCount);
			}
		});

		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(UserListActivity.this,
						ViewProfileActivity.class);
				intent.putExtra(Params.USER,
						Global.listFriend.elementAt(position));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		// force to getlist first time create activities
		Global.LAST_TIME_RELOAD = 0;


		// load total count foot print
		RequestParams userRequest = new RequestParams();
		userRequest.put(Params.PARAM_USER_ID_, mSession.getUserId());
		userRequest.put(Params.PARAM_TOKEN_, mSession.getToken());
		userRequest.put(Params.PAGE, 1);
		userRequest.put(Params.LIMIT, Global.LIMIT_USER_IN_PAGE);
		userRequest.put(Params.PARAM_ORDER, Params.PARAM_COUNT);

		// if(useProgressDialog) Global.newFootPrint=0;
		GetFootprintAPI getFootprintAPI = new GetFootprintAPI(this, this,
				Global.listFootprint);
		getFootprintAPI.setUseProgressbar(false);
		getFootprintAPI.setParams(userRequest);
		getFootprintAPI.onRunButtonPressed();

		/*if (Global.checkPlayServices(this)) {
			SharedPreferences prefs = getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			String regIdGCM = prefs.getString(Params.GOOGLE_REGISTER_GCM, "");
			Log.v("", "regIdGCM: "+regIdGCM);
			int registeredVersion = prefs.getInt(Params.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	        int currentVersion = Global.getAppVersion(this);
			if (registeredVersion == currentVersion && !TextUtils.isEmpty(regIdGCM)) {
				Global.storeRegIdinServer(regIdGCM, this);
			} else {
				Global.registerInBackground(this);
			}
		}*/
		Global.getUnReadMessageTotal(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
		// Toast.makeText(this, ""+mPage, Toast.LENGTH_LONG).show();
		if (Global.LAST_TIME_RELOAD == 0
				|| (System.currentTimeMillis() - Global.LAST_TIME_RELOAD) >= MAX_REFRESH_TIME
				|| Global.listFriend.size() == 0 || listTopAppeal.size() == 0) {
			Global.LAST_TIME_RELOAD = System.currentTimeMillis();
			mPage = 1;
			if (animator != null)
				animator.cancel();
			if (hsScrollView != null) {
				hsScrollView.setScrollX(0);
			}

			if (horizontalLayout != null)
				horizontalLayout.removeAllViews();
			// load user
			// load API list user
			Global.LOAD_MORE_USER = true;
			Global.listFriend.removeAllElements();
			Global.listFriendMap.clear();
			customGridAdapter.notifyDataSetChanged();
			listTopAppeal.removeAllElements();

			loadUser(true);
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_USER_ID_, mSession.getUserId());
			request.put(Params.PARAM_TOKEN_, mSession.getToken());
			// load appeal user
			apiListAppeal = new GetListUserAPI(UserListActivity.this,
					UserListActivity.this, listTopAppeal, Params.APPEAL);
			apiListAppeal.setParams(request);
			apiListAppeal.setUseProgressbar(false);
			apiListAppeal.onRunButtonPressed();
			// get list blocked by me
			getListBlockedUser(request);
			//
			getListBlockedOrBeBlocked(request);
			// get list stamp
			loadStampAPI(request);
		}
		// demoe force close to send google anatic
		// customGridAdapter=null;
		// customGridAdapter.notifyDataSetChanged();
	}

	// Append more data into the adapter
	public void customLoadMoreDataFromApi(int offset) {
		// This method probably sends out a network request and appends new data
		// items to your adapter.
		// Use the offset value and add it as a parameter to your API request to
		// retrieve paginated data.
		// Deserialize API response and then construct new objects to append to
		// the adapter
//		Log.i(TAG, "load more data page=" + offset);
		if (mPage < offset) {
			// Log.i(TAG, "load more data page=" + offset);
			mPage = offset;
			// if ((Global.listFriend.size() % Global.LIMIT_USER_IN_PAGE) == 0)
			// {
			if (Global.LOAD_MORE_USER) {
				Log.i(TAG, "load more data page=" + offset);
				loadUser(false);
				findViewById(R.id.loadmorePB).setVisibility(View.VISIBLE);
			} else {
				Log.i(TAG, "load het userlist roi" + offset);
			}
		}
		loadUser(false);
		findViewById(R.id.loadmorePB).setVisibility(View.VISIBLE);
	}

	
	private void loadStampAPI(RequestParams request) {
		if (Global.listStampModel.size() <= 0) {
			LoadStampAPI stamp = new LoadStampAPI(this, Global.listStampModel);
			stamp.setParams(request);
			stamp.onRunButtonPressed();
		}
	}

	private void getListBlockedUser(RequestParams request) {
		// khi load thanh cong se clear list va add lai
		BlockedByMeAPI blocked = new BlockedByMeAPI(this,
				Global.listUserBlocked, true);
		blocked.setParams(request);
		blocked.onRunButtonPressed();

	}

	private void getListBlockedOrBeBlocked(RequestParams request) {
		// khi load thanh cong se clear list va add lai
		BlockedByMeAPI blocked = new BlockedByMeAPI(this,
				Global.listBlockedOrBeBlocked, false);
		blocked.setParams(request);
		blocked.onRunButtonPressed();
	}

	private void loadAppeal() {
		// Log.i(TAG, "size of listTopAppeal=" + listTopAppeal.size());
		DisplayMetrics displayMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
		int width = displayMetric.widthPixels;
		final int widthChild = width / 3;
		int size = listTopAppeal.size();
		int sizeAppeal = 0;
		if (size < 3)
			sizeAppeal = 3;
		else
			sizeAppeal = size;
		horizontalLayout.removeAllViews();
		for (int i = 0; i < sizeAppeal; i++) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.list_appeal_item,
					null);
			nameUserAppeal = (TextView) layout
					.findViewById(R.id.tv_user_appeal);
			imgUserAppeal = (ImageView) layout
					.findViewById(R.id.img_user_appeal);

			LinearLayout.LayoutParams params = new LayoutParams(widthChild,
					LayoutParams.MATCH_PARENT);
			layout.setLayoutParams(params);
			horizontalLayout.addView(layout);
			if (i < size) {
				nameUserAppeal.setText(listTopAppeal.get(i).getName());
				imgUserAppeal.setScaleType(ScaleType.CENTER_CROP);
//				imgLoader.DisplayImage(listTopAppeal.get(i).getUserAvatar(),
//						R.drawable.loader, imgUserAppeal);
				// save image
				imageFetcher.loadImage(listTopAppeal.get(i)
						.getUserAvatar(), imgUserAppeal);
//				LoadSaveImage.getThumbImage(listTopAppeal.get(i)
//						.getUserAvatar(), Params.PARAM_USER_AVATAR
//						+ File.separator + listTopAppeal.get(i).getUserId(),
//						listTopAppeal.get(i).getUserAvatar().hashCode() + "");
				layout.setTag(listTopAppeal.elementAt(i));
				layout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						UserModel user = (UserModel) v.getTag();
						if (user.getUserId() != Global.userInfo.getUserId()) {
							Intent intent = new Intent(UserListActivity.this,
									ViewProfileActivity.class);
							intent.putExtra(Params.USER, user);
							startActivity(intent);
						} else {
							ShowMessage
									.showDialog(
											UserListActivity.this,
											getString(R.string.ERR_TITLE),
											getString(R.string.you_click_yourself_in_appeal_list));
						}
					}
				});
			} else {
				nameUserAppeal.setVisibility(View.GONE);
				imgUserAppeal.setImageResource(R.drawable.kara);
				layout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(UserListActivity.this,
								BuyVipActivity.class);
						intent.putExtra(Params.PARAM_TITLE,
								getString(R.string.appeal_title));
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
					}
				});
			}
		}
		LEFT_TO_RIGHT = true;
		restartScrollAppealList();
	}

	private void restartScrollAppealList() {
		if (listTopAppeal.size() > 0) {
			// auto scroll
			DisplayMetrics displayMetric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
			int width = displayMetric.widthPixels;
			final int widthChild = width / 3;
			int widthScreen = displayMetric.widthPixels;
			// Log.v("", "Width screen total = " + widthScreen);

			// Width of the container (LinearLayout)
			int widthContainer = widthChild * listTopAppeal.size();
			// Log.v("", "Width container total = " + widthContainer);

			// Width of one child (Button)
			// Log.v("", "Width child = " + widthChild);

			// Nb children in screen
			int nbChildInScreen = widthScreen / widthChild;
			// Log.v("", "Width screen total / Width child = " +
			// nbChildInScreen);

			// Width total of the space outside the screen / 2 (= left position)
			int positionLeftWidth = (widthContainer - (widthChild * nbChildInScreen));
			// Log.v("", "Position left to the middle = " + positionLeftWidth);
			// Auto scroll to the middle
			if (!LEFT_TO_RIGHT)
				positionLeftWidth = 1;
			hsScrollView.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						isScrolling = true;
						currentScroll = hsScrollView.getScrollX();
						if (animator != null)
							animator.cancel();

						break;
					case MotionEvent.ACTION_MOVE:
						isScrolling = true;
						currentScroll = hsScrollView.getScrollX();
						if (animator != null)
							animator.cancel();
						break;
					case MotionEvent.ACTION_UP:
						isScrolling = false;
						if (countDownFinish) {
							countDownFinish = false;
							animationRuning = true;
							new CountDownTimer(3000, 1) {
								public void onTick(long millisUntilFinished) {
									currentScroll = hsScrollView.getScrollX();
								}

								public void onFinish() {
									if (!isScrolling) {
										hsScrollView
												.setScrollX((int) currentScroll);
										restartScrollAppealList();

									}
									countDownFinish = true;
								}
							}.start();
						}
						// restartScrollAppealList();
						break;
					case MotionEvent.ACTION_CANCEL:
						// restartScrollAppealList();
						break;
					default:
						break;
					}
					return false;
				}
			});
			if (positionLeftWidth > 0) {
				final int scrollTo = positionLeftWidth;
				hsScrollView.postDelayed(new Runnable() {
					public void run() {
						animator = ObjectAnimator.ofInt(hsScrollView,
								"scrollX", scrollTo);
						animator.setDuration(2000
								* Math.abs(scrollTo - hsScrollView.getScrollX())
								/ (widthChild / 3));// moi user scroll trong 3 s
						// animator.setRepeatCount(ValueAnimator.INFINITE);
						// animator.setRepeatMode(ValueAnimator.REVERSE);
						animator.addListener(new Animator.AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onAnimationRepeat(Animator animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animator animation) {
								// TODO Auto-generated method stub
								LEFT_TO_RIGHT = !LEFT_TO_RIGHT;
								if (cancelAnimation) {
									cancelAnimation = false;
								} else if (!isScrolling) {
									restartScrollAppealList();
								}
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								// TODO Auto-generated method stub
								// restartScrollAppealList();
								cancelAnimation = true;
							}
						});
						if (!animator.isRunning())
							animator.start();
						// hsScrollView.smoothScrollTo(positionLeftWidth, 0);
					}
				}, 1000);
			}

		}
	}

	private void loadImageFriend() {
//		AsyncTask<Void, Void, Void> loadthumb = new AsyncTask<Void, Void, Void>() {
//
//			@Override
//			protected Void doInBackground(Void... params) {
//				for (int i = 0; i < Global.listFriend.size(); i++) {
//					String userAvatar = Global.listFriend.elementAt(i)
//							.getUserAvatar();
//					if (userAvatar != null && userAvatar.length() > 0)
//						LoadSaveImage.getThumbImage(userAvatar,
//								Params.PARAM_USER_AVATAR
//										+ File.separator
//										+ Global.listFriend.elementAt(i)
//												.getUserId(),
//								userAvatar.hashCode() + "");
//				}
//				return null;
//			}
//
//			@Override
//			protected void onPostExecute(Void result) {
//				super.onPostExecute(result);
//				if (customGridAdapter != null)
//					customGridAdapter.notifyDataSetChanged();
//			}
//		};
//		loadthumb.execute();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_refresh:
			refreshUser.setVisibility(View.GONE);
			Global.listFriend.removeAllElements();
			Global.listFriendMap.clear();
			customGridAdapter.notifyDataSetChanged();
			loadUser(true);
			break;
		case R.id.appeal_refresh:
			refreshApeal.setVisibility(View.GONE);
			if (horizontalLayout != null)
				horizontalLayout.removeAllViews();
			listTopAppeal.removeAllElements();
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_USER_ID_, mSession.getUserId());
			request.put(Params.PARAM_TOKEN_, mSession.getToken());
			// load appeal user
			apiListAppeal = new GetListUserAPI(UserListActivity.this,
					UserListActivity.this, listTopAppeal, Params.APPEAL);
			apiListAppeal.setParams(request);
			apiListAppeal.setUseProgressbar(false);
			apiListAppeal.onRunButtonPressed();
			break;
		case R.id.filterUser:
			// TODO filter user
			dtb = new DialogTwoButton(UserListActivity.this,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							dtb.dismiss();
							filterVaule = dtb.getFiler();
							mPage = 1;// resetpage
							Global.listFriend.removeAllElements();// RESET USER
							Global.listFriendMap.clear();
							customGridAdapter.notifyDataSetChanged();
							// customGridAdapter = null;
							Global.LOAD_MORE_USER = true;
							loadUser(true);
						}
					});
			dtb.show();
			break;

		default:
			break;
		}
	}

	private void loadUser(boolean useProgressDialog) {
		// TODO updata location
		mSession = SessionManager.getInstance(getApplicationContext());
		RequestParams userRequest = new RequestParams();
		userRequest.put(Params.PARAM_USER_ID_, mSession.getUserId());
		userRequest.put(Params.PARAM_TOKEN_, mSession.getToken());
		userRequest.put(Params.PAGE, mPage);//mPage
		userRequest.put(Params.LIMIT, 100);//Global.LIMIT_USER_IN_PAGE
		if (Global.mLocationClient != null
				&& Global.mLocationClient.isConnected()) {
			if (Global.mLocationClient.getLastLocation() != null) {
				Log.i(TAG, "update location to server");
				Location location = Global.mLocationClient.getLastLocation();
				int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
						Params.SETTING_DEFAULT_LOCATION);
				double[] gps = Global.getLocation(location.getLongitude(),
						location.getLatitude(), shiftMet);
				userRequest.put(Params.PARAM_GPS_LAT_, gps[1]);
				userRequest.put(Params.PARAM_GPS_LNG_, gps[0]);
			}
		} else {
			if (Global.mLocationClient == null)
				Global.mLocationClient = new LocationClient(
						getApplicationContext(), this, this);

			Global.mLocationClient.connect();
		}
		//
		if (filterVaule != null) {
			Log.v(TAG, "filter TOKEN  " + mSession.getToken());
			Log.v(TAG, "filterVaule " + filterVaule.distance + " "
					+ filterVaule.loginTime + " " + filterVaule.sexId);
			userRequest.put(Params.FILTERS + "[" + Params.PARAM_SEX + "]",
					filterVaule.sexId);
			if (filterVaule.distance > 0)
				userRequest.put(Params.FILTERS + "[" + Params.PARAM_DISTANCE
						+ "]", filterVaule.distance);
			if (filterVaule.loginTime > 0)
				userRequest.put(Params.FILTERS + "[" + Params.PARAM_LASTLOGIN
						+ "]", filterVaule.loginTime);
			if (filterVaule.isAdvaneFilter) {
				Log.v(TAG, "filter advance Key : age" + filterVaule.age_start
						+ ":" + filterVaule.age_end);
				userRequest.put(Params.FILTERS + "[" + Params.PARAM_AGE_MIN
						+ "]", filterVaule.age_start);
				userRequest.put(Params.FILTERS + "[" + Params.PARAM_AGE_MAX
						+ "]", filterVaule.age_end);
				for (Map.Entry<String, String> entry : filterVaule.advanceFilter
						.entrySet()) {
					userRequest.put(entry.getKey(), entry.getValue());
					Log.v(TAG, "filter advance Key : " + entry.getKey()
							+ " Value : " + entry.getValue());
				}
			}
		}

		apiListFriend = new GetListUserAPI(this, this, Global.listFriend,
				Params.USER);
		apiListFriend.setUseProgressbar(useProgressDialog);
		apiListFriend.setParams(userRequest);
		apiListFriend.setUseProgressbar(false);
		apiListFriend.onRunButtonPressed();
	}

	@Override
	public void handleReceiveData() {
		// Log.v(TAG, "filter : " + Global.listFriend.size());
		if (Global.listFriend != null) {
			if (Global.listFriend.size() == 0) {
				refreshUser.setVisibility(View.VISIBLE);
			} else {
				refreshUser.setVisibility(View.GONE);

			}
		}
		if (customGridAdapter != null) {
			customGridAdapter.notifyDataSetChanged();
			loadImageFriend();
		} else {
			customGridAdapter = new CustomGridViewAdapter(
					UserListActivity.this, R.layout.custom_img,
					Global.listFriend);
			gridview.setAdapter(customGridAdapter);
			loadImageFriend();
		}

		//
		// Log.v(TAG, "filter :update  ");
		findViewById(R.id.loadmorePB).setVisibility(View.GONE);
	}

	@Override
	public void handleGetList() {
		if (listTopAppeal != null) {
			loadAppeal();
			// if (listTopAppeal.size() == 0) {
			// refreshApeal.setVisibility(View.VISIBLE);
			// } else {
			// refreshApeal.setVisibility(View.GONE);
			// loadAppeal();
			// }
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(
				this,
				getString(R.string.mess_connection_location_failed)
						+ "\nResult = " + arg0, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, getString(R.string.mess_connected_location),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, getString(R.string.mess_disconnected_location),
				Toast.LENGTH_SHORT).show();
	}
}
