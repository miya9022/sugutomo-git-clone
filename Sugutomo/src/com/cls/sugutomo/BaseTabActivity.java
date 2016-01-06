package com.cls.sugutomo;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.GoogleAnalytics.AnalyticsExceptionParser;
import com.cls.sugutomo.adapter.NavDrawerListAdapter;
import com.cls.sugutomo.api.LogoutAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.databases.DatabaseHandler;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.map.MapActivity;
import com.cls.sugutomo.model.ConversationModel;
import com.cls.sugutomo.model.NavDrawerItem;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.userlist.UserListActivity;
import com.cls.sugutomo.utils.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.loopj.android.http.RequestParams;

public class BaseTabActivity extends FragmentActivity {
	public SharedPreferences pref;

	// Editor for Shared preferences
	public Editor editor;
	protected static final String TAG = BaseTabActivity.class.getSimpleName();
	// slide menu items
	private String[] navMenuTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private ActionBarDrawerToggle mDrawerToggle;
	private TextView mCounter;

	// nav drawer title
	public CharSequence mDrawerTitle;

	// Session Manager Class
	private SessionManager mSession;

	// receive broadcast
	private BroadcastReceiver mReceiver, mReceiverFootprint, mReceiverConnection;
	private int tabMenuIndex = 0;
	// DB
	private DatabaseHandler mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_activity);
		pref = this.getSharedPreferences(Global.PREFERENCE_NAME, 0);
		editor = pref.edit();

		// Session Manager
		mSession = SessionManager.getInstance(getApplicationContext());
		mDatabase = DatabaseHandler.getInstance(getApplicationContext());

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		mDrawerList.getBackground().setAlpha(200);

		reduceErrorWhenActivitesKill();

		navDrawerItems = new ArrayList<NavDrawerItem>();
		// adding nav drawer items to array
		navDrawerItems.add(new NavDrawerItem(true, Global.userInfo));
		for (int i = 1; i < navMenuTitles.length + 1; i++) {
			if (i == 3)
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i - 1],
						true, getSumNewMessageServer()));
			else if (i == 6)
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i - 1],
						true, Global.getSumFootPrintMessage()));// sumNewFootPrintFromDB()
			else
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i - 1],
						false));
		}
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(this, R.layout.drawer_list_item,
				navDrawerItems, this);
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
				changeTab(tabMenuIndex);
			}

			public void onDrawerOpened(View drawerView) {
				// getActionBar().setTitle(mTitle);
				notifyNewMessagesServer();
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}

		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);
		Global.currentActivity = this;

		final int abTitleId = getResources().getIdentifier("action_bar_title",
				"id", "android");
		View title = findViewById(abTitleId);

		final Handler handler = new Handler();
		// if not work try solution 2 with custom view in
		// http://stackoverflow.com/questions/24838155/set-onclick-listener-on-action-bar-title-in-android
		if (title != null) {
			title.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Do something
					if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mDrawerLayout.closeDrawer(Gravity.LEFT);
							}
						});
					} else {
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mDrawerLayout.openDrawer(mDrawerList);
							}
						});
					}
				}
			});
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// GoogleAnalytics.getInstance(this).reportActivityStart(this);
		EasyTracker.getInstance(this).activityStart(this);
		Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread
				.getDefaultUncaughtExceptionHandler();
		if (uncaughtExceptionHandler instanceof ExceptionReporter) {
			ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
			exceptionReporter
					.setExceptionParser(new AnalyticsExceptionParser());
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// GoogleAnalytics.getInstance(this).reportActivityStop(this);
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		} else {
			super.onBackPressed();
		}
	}

	private void reduceErrorWhenActivitesKill() {
		if (Global.userInfo != null) {
			String saveStr = Global.getUserInfo(Global.userInfo).toString();
			editor.putString(Params.PREF_USER_INFO, saveStr);
			editor.commit();
		} else {
			String myString = pref.getString(Params.PREF_USER_INFO, "");
			try {
				UserModel user = Global.setUserInfo(new JSONObject(myString),
						null);
				// Toast.makeText(this, "onRestoreInstanceState",
				// Toast.LENGTH_LONG).show();
				if (user != null) {
					Global.userInfo = user;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (Global.listUserBlocked.size() == 0) {
			try {
				String blocks = pref.getString(Params.PREF_LIST_USER_BLOCK
						+ Global.userInfo.getUserId(), "NULL");
				if (blocks != null && !blocks.equalsIgnoreCase("NULL")) {
					String[] lst = blocks.split(",");
					for (int i = 0; i < lst.length; i++) {
						if (lst[i].length() > 0 && !lst[i].equalsIgnoreCase("")) {
							int id = Integer.valueOf(lst[i]);
							// Log.d("", "restrick:" + badWord);
							Global.listUserBlocked.add(id);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		if (Global.listBlockedOrBeBlocked.size() == 0) {
			try {
				String blocks = pref.getString(
						Params.PREF_LIST_USER_BLOCK_OR_BEBLOCK
								+ Global.userInfo.getUserId(), "NULL");
				if (blocks != null && !blocks.equalsIgnoreCase("NULL")) {
					String[] lst = blocks.split(",");
					for (int i = 0; i < lst.length; i++) {
						if (lst[i].length() > 0 && !lst[i].equalsIgnoreCase("")) {
							int id = Integer.valueOf(lst[i]);
							Global.listBlockedOrBeBlocked.add(id);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		// favorite savve id
		if (Global.listFavoriteId.size() == 0) {
			try {
				String blocks = pref.getString(Params.PREF_LIST_FAVORITE_ID
						+ Global.userInfo.getUserId(), "NULL");
				if (blocks != null && !blocks.equalsIgnoreCase("NULL")) {
					String[] lst = blocks.split(",");
					for (int i = 0; i < lst.length; i++) {
						if (lst[i].length() > 0 && !lst[i].equalsIgnoreCase("")) {
							int id = Integer.valueOf(lst[i]);
							Global.listFavoriteId.add(id);
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		LinearLayout layout = (LinearLayout) menu.findItem(
				R.id.action_notification).getActionView();
		mCounter = (TextView) layout.findViewById(R.id.count_message_text);

		mCounter.setText(getSumNewMessageServer());
		layout.findViewById(R.id.notify_message).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(BaseTabActivity.this,
								ChatListActivityServer.class);//ChatListActivity
						mDrawerTitle = navDrawerItems.get(3).getTitle();
						intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
						// finish();
					}
				});
		ImageView sendAll = (ImageView) layout
				.findViewById(R.id.notice_sendall);
		sendAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BaseTabActivity.this,
						SendAllMessageActivity.class);
				mDrawerTitle = navDrawerItems.get(4).getTitle();
				intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				// finish();
			}
		});

		return true;
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_notification).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {

		default:
			break;
		}
		return true;
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			displayView(position);
		}
	}

	private void displayView(int position) {
		// display view for selected navigation drawer item
		if (position > 0) {
			mDrawerTitle = navDrawerItems.get(position).getTitle();
			if (mDrawerTitle != null)
				setTitle(mDrawerTitle);
			tabMenuIndex = position;
		}
		closeMenuLeft(position);

	}

	public void changeTab(int position) {
		switch (position) {
		case 0:
			break;
		case 1: {
			Intent intent = new Intent(BaseTabActivity.this,
					UserListActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// reset if it aredy
															// runn
			startActivity(intent);
			break;
		}
		case 2: {
			Intent intent = new Intent(BaseTabActivity.this, MapActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 3: {
			Intent intent = new Intent(BaseTabActivity.this,
					ChatListActivityServer.class);//ChatListActivity
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			//intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			//intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		case 4: {
			Intent intent = new Intent(BaseTabActivity.this,
					SendAllMessageActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 5: {
			Intent intent = new Intent(BaseTabActivity.this,
					FavouriteListActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 6: {
			Intent intent = new Intent(BaseTabActivity.this,
					FootprintListActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		case 7: {

			Intent intent = new Intent(BaseTabActivity.this,
					BuyPointActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 8: {
			// ranking
			Intent intent = new Intent(BaseTabActivity.this, RankActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// reset if it aredy
															// runn
			startActivity(intent);
			break;
		}
		case 9: {
			Intent intent = new Intent(BaseTabActivity.this,
					BuyVipActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}

		case 10: {
			// QA
			Intent intent = new Intent(BaseTabActivity.this,
					QuestionAnswerActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 11: {
			// setting
			Intent intent = new Intent(BaseTabActivity.this,
					SettingActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		}
		case 12: {
			// term condition
			Intent intent = new Intent(BaseTabActivity.this,
					InformationActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
			intent.putExtra(Params.REQUEST, Params.TERM);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		case 13: {
			// private policy
			Intent intent = new Intent(BaseTabActivity.this,
					InformationActivity.class);
			intent.putExtra(Params.PARAM_TITLE, mDrawerTitle);
			intent.putExtra(Params.LAYOUT_ID, R.layout.information_activity);
			intent.putExtra(Params.REQUEST, Params.POLICY);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		case 14: {
			Uri uri = Uri.parse("market://details?id=" + getPackageName());
			// Toast.makeText(this, getPackageName(), Toast.LENGTH_LONG).show();
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id="
								+ getPackageName())));
			}
			break;
		}
		case 15: {
			// get preference login
			int user_id = mSession.getUserId();
			String token = mSession.getToken();

			LogoutAPI logout = new LogoutAPI(BaseTabActivity.this, mSession);
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_USER_ID_, user_id);
			request.put(Params.PARAM_TOKEN_, token);
			logout.setParams(request);
			logout.onRunButtonPressed();
			break;
		}
		case 16: {
			// send maill support
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			// i.setType("text/plain");
			// i.putExtra(Intent.EXTRA_ ,
			// getString(R.string.send_support_EXTRA_EMAIL));
			i.putExtra(
					Intent.EXTRA_EMAIL,
					new String[] { getString(R.string.send_support_EXTRA_EMAIL) });
			i.putExtra(Intent.EXTRA_SUBJECT,
					getString(R.string.send_support_EXTRA_SUBJECT));
			try {
				String osVersion = Build.VERSION.RELEASE;
				String id = Global.getDeviceUDID(this);
				String appVersion = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
				String deviceId = Build.MODEL;
				String txt = String.format(
						getString(R.string.send_support_EXTRA_TEXT), id,
						osVersion, appVersion, deviceId);
				i.putExtra(Intent.EXTRA_TEXT, txt);
				startActivity(Intent.createChooser(i,
						getString(R.string.chose_share)));
			} catch (Exception ex) {
				ex.printStackTrace();
				// Toast.makeText(this, "There are no email clients installed.",
				// Toast.LENGTH_SHORT).show();
			}
			break;
		}
		default:
			break;
		}
	}

	public void closeMenuLeft(final int position) {
		mDrawerList.setItemChecked(position, true);
		// mDrawerList.setSelection(position);

		// TODO Auto-generated method stub
		mDrawerLayout.closeDrawer(mDrawerList);

	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		onReceiveMessage();
		onReceiveFootprint();
		onmReceiverConnection();
		if (adapter != null)
			adapter.notifyDataSetChanged();
		notifyNewMessagesServer();
		Global.appInBackGround = false;
	}
private void onmReceiverConnection(){
	IntentFilter intentFilter = new IntentFilter(getPackageName()
			+ Params.PARAM_RECONNECT_INTERNET_CHECK);
	mReceiverConnection = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// refresh chatlist prevent lost message openfire
			runOnUiThread(new Runnable() {
				public void run() {
					Global.CHAT_NEED_REFRESH=true;
					Global.CHATLIST_NEED_REFRESH=true;
				}
			});
		}
	};
	// registering our receiver
	this.registerReceiver(mReceiverConnection, intentFilter);
}
	protected void onReceiveFootprint() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_FOOTPRINT);
		mReceiverFootprint = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// extract our message from intent
				Global.newFootPrint += 1;
				Log.i(TAG, "show footprint dialog");
				Global.playBeep(getApplicationContext());
				String username = intent.getStringExtra(Params.PARAM_USERNAME);
				ShowMessage.showMessage(BaseTabActivity.this,
						getString(R.string.format_receive_footprint, username));
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiverFootprint, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mReceiver != null)
			this.unregisterReceiver(mReceiver);
		if (mReceiverFootprint != null)
			this.unregisterReceiver(mReceiverFootprint);
		if (mReceiverConnection != null)
			this.unregisterReceiver(mReceiverConnection);
		Global.appInBackGround = true;
	}

	
	private void notifyNewMessagesServer() {
		AsyncTask<Void, Void, String> myTask = new AsyncTask<Void, Void, String>() {
			String count = "0";

			@Override
			protected String doInBackground(Void... params) {

				count = getSumNewMessageServer();
				return count;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				// refresh in drawer list
				navDrawerItems.get(3).setCount(count);
				navDrawerItems.get(3).setCounterVisibility(true);
				adapter.notifyDataSetChanged();
//				 Toast.makeText(BaseTabActivity.this, "new message basetab activity: "+count,
//				 Toast.LENGTH_SHORT).show();
				// refresh in actionbar
				if (mCounter != null)
					mCounter.setText(count);
			}
		};
		myTask.execute();

	}
	// protected int sumNewFootPrintFromDB() {
	// Global.newFootPrint = 0;
	// for (int i = 0; i < Global.listFootprint.size(); i++) {
	//
	// Global.newFootPrint += Global.listFootprint.get(i)
	// .getCountFootprint();
	// }
	// return Global.newFootPrint;
	// }

	protected int sumNewMessageFromDB() {
		Vector<ConversationModel> listConversation = new Vector<ConversationModel>();
		listConversation = mDatabase.getAllConversations(Global.userInfo
				.getUserId());
		Global.newMessage = 0;
		for (int i = 0; i < listConversation.size(); i++) {
//			Log.i(TAG,
//					"basetabactivity new message = "
//							+ listConversation.get(i).getNewMessages());
			Global.newMessage += listConversation.get(i).getNewMessages();
		}
		return Global.newMessage;
	}

	protected void onReceiveMessage() {
		// TODO
		IntentFilter intentFilter = new IntentFilter(getPackageName()
				+ Params.PARAM_BROADCAST);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// extract our message from intent
				// String msg_for_me = intent.getStringExtra(Params.MESSAGE);
//				notifyNewMessages();
				notifyNewMessagesServer();
			}
		};
		// registering our receiver
		this.registerReceiver(mReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.closeDB();
		Global.currentActivity = null;
	}

	
	private String getSumNewMessageServer() {
		int sum = Global.newMessage;
		String s = "";
		if (sum >= 100)
			s = "99+";
		else
			s = String.valueOf(sum);

		return s;
	}

}
