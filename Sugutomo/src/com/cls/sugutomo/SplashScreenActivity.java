package com.cls.sugutomo;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.CommonLogin;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chat.ChatBackgroundService;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.RequestParams;

public class SplashScreenActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, APICallbackInterface {

	private static final String TAG = SplashScreenActivity.class
			.getSimpleName();

	private static int SPLASH_TIME = 3000;
	private static int MSG_START_ANIM = 0;
	private static int MSG_STOP_ANIM = 1;

	// UI
	private ImageView image;
	private RelativeLayout mMainLayout;
	private int screenWidth, screenHeight;
	private Handler handler;
	private boolean blnIsStopAnim = false;

	private SessionManager mSession;
	// data for login
	private boolean mIsLogin;
	private int mTypeLogin, mUserId;
	private String mToken;
	private CommonLogin mCommonLogin;
	private boolean isStop=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSession = SessionManager.getInstance(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getWindow().clearFlags(
//				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.activity_splash_screen);
		//setTheme(android.R.style.Theme_Holo_Light_no);
		mMainLayout = (RelativeLayout) findViewById(R.id.animation_layout);
		screenWidth = Global.getWidthScreen(this);
		screenHeight = Global.getHeightScreen(this);

		// prepare data
		prepareData();

		blnIsStopAnim = false;
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					/* Refresh UI */
					runAnimation();
					break;
				case 1:
					stopAnimation();
				}
			}
		};
		// post msg for handle
		handler.sendEmptyMessage(MSG_START_ANIM);
		handler.sendEmptyMessageDelayed(MSG_STOP_ANIM, SPLASH_TIME);
	}

	private void prepareData() {
		// hide folder
		hideFolderImage();
		// GPS connect
		if (Global.mLocationClient == null) {
			Log.v(TAG, "LocationClient connect!");
			Global.mLocationClient = new LocationClient(
					getApplicationContext(), this, this);
			Global.mLocationClient.connect();
		} else if (!Global.mLocationClient.isConnected()) {
			Global.mLocationClient.connect();
		}

		// create shortcut if requested
		if (!mSession.isShortcutCreated()) {
			handleAutoCreateShortcut();
		}

		// get preference login
		mTypeLogin = mSession.getLoginType();
		mUserId = mSession.getUserId();
		mToken = mSession.getToken();
		mIsLogin = mSession.isLoggedIn();
		// log preference
		Log.v(TAG, "type login = " + mTypeLogin + " userid = " + mUserId
				+ " token=" + mToken + " isLogin=" + mIsLogin
				+ " requirePassword=" + mSession.getRequirePassword());

		// start service
		Intent serviceIntent = new Intent(this, ChatBackgroundService.class);
		startService(serviceIntent);
	}

	private void handleAutoLogin() {
		// auto login
		if (mToken != null && mUserId > -1 && mIsLogin
				&& !mSession.getRequirePassword()) {
			mCommonLogin = new CommonLogin(this, this, mSession);
			Log.d(TAG, "handle auto login splash");
			mCommonLogin.setToken(mToken);
			RequestParams params = new RequestParams();
			params.put(Params.PARAM_USER_ID_, mUserId);
			params.put(Params.PARAM_TOKEN_, mToken);
			if (mTypeLogin != -1)
				mCommonLogin.setLoginType(mTypeLogin);
			mCommonLogin.setParams(params);
			mCommonLogin.onRunButtonPressed();
		} else {
			Intent i = new Intent(SplashScreenActivity.this, SplashScreen.class);
			startActivity(i);
			finish();
		}
	}

	private void hideFolderImage() {
		String NOMEDIA = ".nomedia";

		File nomediaFile = new File(Environment.getExternalStorageDirectory()
				+ File.separator + Global.PREFERENCE_NAME + File.separator
				+ NOMEDIA);
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleAutoCreateShortcut() {
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(
				this, R.drawable.icon);

		Intent shortcutIntent = new Intent();
		Intent launchIntent = new Intent(this, SplashScreenActivity.class);
		// launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		shortcutIntent
				.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		sendBroadcast(shortcutIntent);
		mSession.setShortcutCreated();
	}

	private void runAnimation() {
		blnIsStopAnim = false;
		createSplashScreen();
	}

	private void stopAnimation() {
		if (blnIsStopAnim)
			return;
		blnIsStopAnim = true;
		if(!isStop ) handleAutoLogin();
	}
	
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isStop =true;
	}

	/**
	 * Stop animation on touch event
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			stopAnimation();
		return true;
	}

	private void createSplashScreen() {
		float[] size1 = getSizeBitmap(R.drawable.ss_01);
		float[] size3 = getSizeBitmap(R.drawable.ss_03);
		float[] size4 = getSizeBitmap(R.drawable.ss_04);
		float[] size5 = getSizeBitmap(R.drawable.ss_05);
		float[] appName = getSizeBitmap(R.drawable.ss_tit);
		float[] iconInside = getSizeBitmap(R.drawable.ss_icon);

		// calculate sum of width and height
		float totalW = size1[0] * 2 + size3[0];
		float totalH = size1[1] + size4[1] + appName[1] + Global.PADDING;

		// ss 01
		float tempX = (screenWidth - totalW) / 2;
		float tempY = (screenHeight - totalH) / 2;
		createImageView(R.drawable.ss_01, (int) size1[0], (int) size1[1],
				tempX, tempX, -size4[1], tempY, 200);

		// ss 02
		tempX = tempX + size1[0];
		createImageView(R.drawable.ss_02, (int) size1[0], (int) size1[1],
				tempX, tempX, -size4[1], tempY, 400);
		// ss 03
		tempX = tempX + size1[0];
		createImageView(R.drawable.ss_03, (int) size3[0], (int) size3[1],
				tempX, tempX, -size4[1], tempY, 600);

		// ss 04
		tempX = (screenWidth - totalW) / 2;
		tempY = (screenHeight - totalH) / 2 + size1[1];
		createImageView(R.drawable.ss_04, (int) size4[0], (int) size4[1],
				tempX, tempX, -size4[1], tempY, 800);

		// ss 05
		tempX += size4[0];
		createImageView(R.drawable.ss_05, (int) size5[0], (int) size5[1],
				tempX, tempX, -size4[1], tempY, 1000);

		// ss 06
		tempX += size5[0];
		createImageView(R.drawable.ss_06, (int) size5[0], (int) size5[1],
				tempX, tempX, -size4[1], tempY, 1200);

		// icon inside
		tempX = (screenWidth - iconInside[0]) / 2;
		float centerTempY = (screenHeight - totalH) / 2 + (size1[1] + size4[1])
				/ 2;
		tempY = centerTempY - iconInside[1] / 2;
		createImageView(R.drawable.ss_icon, (int) iconInside[0],
				(int) iconInside[1], tempX, tempX, -size4[1], tempY, 1400);

		// app name
		tempX = (screenWidth - appName[0]) / 2;
		tempY = (screenHeight - totalH) / 2;
		tempY = screenHeight - tempY - appName[1];
		createImageView(R.drawable.ss_tit, (int) appName[0], (int) appName[1],
				tempX, tempX, -size4[1], tempY, 1400);

	}

	private float[] getSizeBitmap(int id) {
		float[] size = new float[2];
		Bitmap bm = BitmapFactory.decodeResource(getResources(), id);
		size[0] = bm.getWidth();
		size[1] = bm.getHeight();
		return size;
	}

	private void animationImage(ImageView image, float toY, long timeOffset) {
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int originalPos[] = new int[2];
		image.getLocationOnScreen(originalPos);

		TranslateAnimation anim = new TranslateAnimation(originalPos[0],
				originalPos[0], originalPos[1], toY);
		anim.setDuration(300);
		anim.setStartOffset(timeOffset);
		anim.setFillAfter(true);
		anim.setInterpolator(new BounceInterpolator());
		image.startAnimation(anim);
	}

	private void createImageView(int resource, int width, int height,
			float fromX, float toX, float fromY, float toY, long timeOffset) {
		// set new params
		image = new ImageView(this);
		image.setImageResource(resource);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, height);
		params.leftMargin = (int) fromX;
		params.topMargin = (int) fromY;
		image.setLayoutParams(params);
		mMainLayout.addView(image);

		animationImage(image, toY + screenHeight / 4, timeOffset);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		ShowMessage.showMessage(this,
				getString(R.string.mess_connection_location_failed));
	}

	@Override
	public void onConnected(Bundle arg0) {
		ShowMessage.showMessage(this,
				getString(R.string.mess_connected_location));
		//update location
		Global.updateLocation(this);
	}

	@Override
	public void onDisconnected() {
		ShowMessage.showMessage(this,
				getString(R.string.mess_disconnected_location));
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		if(mCommonLogin!=null)
			mCommonLogin.unBindService();
		Intent i = new Intent(SplashScreenActivity.this, SplashScreen.class);
		startActivity(i);
		finish();
	}

	@Override
	public void handleGetList() {
	}
}
