package com.cls.sugutomo;

import java.io.File;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.adapter.CustomGridViewAdapter;
import com.cls.sugutomo.adapter.EndlessScrollListener;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.GetFootprintAPI;
import com.cls.sugutomo.api.SetFootprintAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.loopj.android.http.RequestParams;

public class FootprintListActivity extends BaseTabActivity implements
		APICallbackInterface {

	private static final String TAG = FootprintListActivity.class
			.getSimpleName();
	private SessionManager mSession;

	private GridView gridview;
	CustomGridViewAdapter customGridAdapter;
	private SwipeRefreshLayout swipeContainer;
	private int mPage = 1;

	private GetFootprintAPI getFootprintAPI;
	private String title;
	private TextView txtNotice;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.follower_list_activity, null, false);
		frameLayout.addView(activityView);
		txtNotice=(TextView) findViewById(R.id.favorit_null_txt); 
		Global.listFootprint = new Vector<UserModel>();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshList);
		// Setup refresh listener which triggers new data loading
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//				}
				mPage=1;
				Global.listFootprint.removeAllElements();
				loadListUser(true);
				swipeContainer.setRefreshing(false);
			}
		});
		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		mSession = SessionManager.getInstance(getApplicationContext());
		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//remove fottprint
				UserModel user =Global.listFootprint.elementAt(position);
				RequestParams request = new RequestParams();
				request.put(Params.PARAM_USER_ID_, mSession.getUserId());
				request.put(Params.PARAM_TOKEN_, mSession.getToken());
				request.put(Params.PARAM_USER_ID, user.getUserId());

				SetFootprintAPI api = new SetFootprintAPI(FootprintListActivity.this,SetFootprintAPI.REMOVE_FOOTPRINT);
				api.setParams(request);
				api.onRunButtonPressed();
				
				Global.newFootPrint-=user.getCountFootprint();
				// TODO view profile
				Intent intent = new Intent(FootprintListActivity.this,
						ViewProfileActivity.class);
				intent.putExtra(Params.USER,user);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				Global.listFootprint.removeElementAt(position);
			}
		});
		gridview.setOnScrollListener(new EndlessScrollListener() {

			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				customLoadMoreDataFromApi(page);
			}
		});
		customGridAdapter = new CustomGridViewAdapter(this,
				R.layout.custom_img_count, Global.listFootprint);
		gridview.setAdapter(customGridAdapter);
	}

	// Append more data into the adapter
	public void customLoadMoreDataFromApi(int offset) {
		// This method probably sends out a network request and appends new data
		// items to your adapter.
		// Use the offset value and add it as a parameter to your API request to
		// retrieve paginated data.
		// Deserialize API response and then construct new objects to append to
		// the adapter
		Log.i(TAG, "load more data page=" + offset);
		mPage = offset;
		if ((Global.listFootprint.size() % Global.LIMIT_USER_IN_PAGE) == 0) {
			loadListUser(false);
			findViewById(R.id.loadmorePB).setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// remove 1 favorite in viewProfile and back to This need update
		//Global.listFootprint.removeAllElements();
		Global.listFootprint.removeAllElements();
		mPage=1;
		if (customGridAdapter != null) {
			customGridAdapter.notifyDataSetChanged();
		}
		loadListUser(true);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}

	private void loadListUser(boolean useProgressDialog) {
		// TODO Auto-generated method stub
		RequestParams userRequest = new RequestParams();
		userRequest.put(Params.PARAM_USER_ID_, mSession.getUserId());
		userRequest.put(Params.PARAM_TOKEN_, mSession.getToken());
		userRequest.put(Params.PAGE, mPage);
		userRequest.put(Params.LIMIT, Global.LIMIT_USER_IN_PAGE);
		//userRequest.put(Params.PARAM_ORDER, Params.PARAM_COUNT);// dung var nay se order by so luong lon -> nho?

		//if(useProgressDialog) Global.newFootPrint=0;
		getFootprintAPI = new GetFootprintAPI(this, this, Global.listFootprint);
		getFootprintAPI.setUseProgressbar(useProgressDialog);
		getFootprintAPI.setParams(userRequest);
		getFootprintAPI.onRunButtonPressed();
	}

	private void loadImageFriend() {
//		AsyncTask<Void, Void, Void> loadthumb = new AsyncTask<Void, Void, Void>() {
//
//			@Override
//			protected Void doInBackground(Void... params) {
//				for (int i = 0; i < Global.listFootprint.size(); i++) {
//					String userAvatar = Global.listFootprint.elementAt(i)
//							.getUserAvatar();
//					if (userAvatar != null && userAvatar.length() > 0)
//						LoadSaveImage.getThumbImage(userAvatar,
//								Params.PARAM_USER_AVATAR
//										+ File.separator
//										+ Global.listFootprint.elementAt(i)
//												.getUserId(),
//								userAvatar.hashCode() + "");
//				}
//				return null;
//			}
//
//			@Override
//			protected void onPostExecute(Void result) {
//				super.onPostExecute(result);
//				if (customGridAdapter != null){
//					customGridAdapter.notifyDataSetChanged();
////				    Log.v("", "update footprint");	
//				}
//			}
//		};
//		loadthumb.execute();
	}
	public void checkNotice(){
		if(Global.listFootprint==null || Global.listFootprint.size()==0){
			txtNotice.setText(getString(R.string.footprint_null));
			txtNotice.setVisibility(View.VISIBLE);
		}else{
			txtNotice.setVisibility(View.GONE);
		}
		
	}
	@Override
	public void handleReceiveData() {
		if (customGridAdapter != null) {
			customGridAdapter.notifyDataSetChanged();
			loadImageFriend();
		}
		findViewById(R.id.loadmorePB).setVisibility(View.GONE);
		checkNotice();
	}

	@Override
	public void handleGetList() {
	}
}
