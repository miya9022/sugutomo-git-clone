package com.cls.sugutomo;

import java.util.Vector;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.cls.sugutomo.adapter.InformationAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.InformationAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.InformationModel;
import com.loopj.android.http.RequestParams;

public class InformationActivity extends BaseTabActivity implements
		APICallbackInterface {

	private View activityView;
	private int layoutId;
	private ListView mListView;

	private SessionManager mSession;
	private String mRequest;
	private Vector<InformationModel> mListInfor = new Vector<InformationModel>();
	private InformationAdapter mAdapter;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		layoutId = bundle.getInt(Params.LAYOUT_ID);
		mRequest = bundle.getString(Params.REQUEST);
		if (bundle.containsKey(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			getActionBar().setDisplayShowHomeEnabled(false);
			final int abTitleId = getResources().getIdentifier(
					"action_bar_title", "id", "android");
			View title = findViewById(abTitleId);
			// if not work try solution 2 with custom view in
			// http://stackoverflow.com/questions/24838155/set-onclick-listener-on-action-bar-title-in-android
			if (title != null) {
				title.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Do something
						finish();
					}
				});
			}
		}
		// add layout to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		activityView = layoutInflater.inflate(layoutId, null, false);
		frameLayout.addView(activityView);

		// set title
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		mListView = (ListView) findViewById(R.id.list_information);

		// get data
		mSession = SessionManager.getInstance(getApplicationContext());
		int userid = mSession.getUserId();
		String token = mSession.getToken();
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, userid);
		request.put(Params.PARAM_TOKEN_, token);
		InformationAPI inforAPI = new InformationAPI(this, this, mListInfor,
				mRequest);
		inforAPI.setParams(request);
		inforAPI.onRunButtonPressed();

		// set adapter
		mAdapter = new InformationAdapter(this, R.layout.information_list_item,
				mListInfor);
		mListView.setAdapter(mAdapter);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Bundle bundle = getIntent().getExtras();
		if (bundle.containsKey(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN)) {
			overridePendingTransition(R.anim.diagslide_enter_back,
					R.anim.diagslide_leave_back);
		} 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Bundle bundle = getIntent().getExtras();
		if (bundle.containsKey(Params.VIEW_TERM_OR_POLICY_FROM_LOGIN)) {
			return false;
		}
		return super.onCreateOptionsMenu(menu);
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
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub
	}
}
