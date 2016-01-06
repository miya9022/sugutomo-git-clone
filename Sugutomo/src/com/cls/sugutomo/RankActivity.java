package com.cls.sugutomo;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.RankAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.slidingTab.SlidingTabsBasicFragment;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class RankActivity extends BaseTabActivity implements
		APICallbackInterface {
	Context context;
	private SessionManager mSession;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.activity_rank,
				null, false);
		frameLayout.addView(activityView);
		mSession = SessionManager.getInstance(getApplicationContext());

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		
		Global.listRankMan = new ArrayList<UserModel>();
		Global.listRankWomen = new ArrayList<UserModel>();
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		RankAPI rankAPI = new RankAPI(this, this, RankAPI.RANK_NEW);
		rankAPI.setParams(request);
		rankAPI.onRunButtonPressed();

	}

	public void init() {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
		transaction.replace(R.id.frame_container, fragment);
		transaction.commit();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}
	
	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		init();
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}

}
