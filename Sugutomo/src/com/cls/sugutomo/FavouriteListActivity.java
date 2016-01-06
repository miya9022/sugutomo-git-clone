package com.cls.sugutomo;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.cls.sugutomo.adapter.CustomGridViewAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.GetListUserAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.loopj.android.http.RequestParams;

public class FavouriteListActivity extends BaseTabActivity implements
		APICallbackInterface {

	CustomGridViewAdapter customGridAdapter;
	private GridView gridview;
	private SessionManager mSession;

	// data
	private int mUserId;
	private String mToken;
	private GetListUserAPI apiListFavorite;
	private String title;
	private TextView txtNotice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// add layout of activity to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.follower_list_activity, null, false);
		frameLayout.addView(activityView);
		txtNotice=(TextView) findViewById(R.id.favorit_null_txt); 
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}

		mSession = SessionManager.getInstance(getApplicationContext());
		mUserId = mSession.getUserId();
		mToken = mSession.getToken();
		if (!Global.IS_LOADED_FAVORITE_LIST || Global.listFavorite.size() <= 0) {
			Global.listFavorite = new Vector<UserModel>();
			RequestParams request = new RequestParams();
			request.put(Params.PARAM_USER_ID_, mUserId);
			request.put(Params.PARAM_TOKEN_, mToken);
			apiListFavorite = new GetListUserAPI(this, this,
					Global.listFavorite, Params.FAVORITE);
			apiListFavorite.setParams(request);
			apiListFavorite.setUseProgressbar(true);
			apiListFavorite.onRunButtonPressed();
		}
		final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshList);
		// Setup refresh listener which triggers new data loading
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// }
				Global.listFavorite.removeAllElements();
				RequestParams request = new RequestParams();
				request.put(Params.PARAM_USER_ID_, mUserId);
				request.put(Params.PARAM_TOKEN_, mToken);
				apiListFavorite = new GetListUserAPI(
						FavouriteListActivity.this, FavouriteListActivity.this,
						Global.listFavorite, Params.FAVORITE);
				apiListFavorite.setParams(request);
				apiListFavorite.onRunButtonPressed();
				apiListFavorite.setUseProgressbar(true);
				swipeContainer.setRefreshing(false);
			}
		});
		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		//
		gridview = (GridView) findViewById(R.id.gridview);
		customGridAdapter = new CustomGridViewAdapter(this,
				R.layout.custom_img, Global.listFavorite);
		gridview.setAdapter(customGridAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO view profile
				Intent intent = new Intent(FavouriteListActivity.this,
						ViewProfileActivity.class);
				intent.putExtra(Params.USER,
						Global.listFavorite.elementAt(position));
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// remove 1 favorite in viewProfile and back to This need update
		if (customGridAdapter != null) {
			int index = gridview.getFirstVisiblePosition();
			gridview.setAdapter(null);
			customGridAdapter = new CustomGridViewAdapter(this,
					R.layout.custom_img, Global.listFavorite);
			gridview.setAdapter(customGridAdapter);
			customGridAdapter.notifyDataSetChanged();
			gridview.smoothScrollToPosition(index);
		}
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}

	private void loadImageFavorite() {
//		AsyncTask<Void, Void, Void> loadthumb = new AsyncTask<Void, Void, Void>() {
//
//			@Override
//			protected Void doInBackground(Void... params) {
//				for (int i = 0; i < Global.listFavorite.size(); i++) {
//					String userAvatar = Global.listFavorite.elementAt(i)
//							.getUserAvatar();
//					if (userAvatar != null && userAvatar.length() > 0)
//						LoadSaveImage.getThumbImage(userAvatar,
//								Params.PARAM_USER_AVATAR
//										+ File.separator
//										+ Global.listFavorite.elementAt(i)
//												.getUserId(),
//								Global.listFavorite.elementAt(i)
//										.getUserAvatar().hashCode()
//										+ "");
//				}
//				return null;
//			}
//
//			@Override
//			protected void onPostExecute(Void result) {
//				super.onPostExecute(result);
//				if (customGridAdapter != null)
//					customGridAdapter.notifyDataSetChanged();
//
//			}
//		};
//		loadthumb.execute();
	}
	public void checkNotice(){
		if(Global.listFavorite==null || Global.listFavorite.size()==0){
			txtNotice.setVisibility(View.VISIBLE);
		}else{
			txtNotice.setVisibility(View.GONE);
		}
		
	}
	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		if (customGridAdapter != null) {
			customGridAdapter.notifyDataSetChanged();
			loadImageFavorite();
		} else {
			customGridAdapter = new CustomGridViewAdapter(this,
					R.layout.custom_img, Global.listFavorite);
			gridview.setAdapter(customGridAdapter);
			customGridAdapter.notifyDataSetChanged();
		}
		if (Global.listFavorite != null && Global.listFavorite.size() > 0) {
			Global.listFavoriteId = new ArrayList<Integer>();
			for (int i = 0; i < Global.listFavorite.size(); i++) {
				Global.listFavoriteId.add(Global.listFavorite.get(i)
						.getUserId());
			}
		}
		checkNotice();
	}

	@Override
	public void handleGetList() {
	}
}