package com.cls.sugutomo;

import java.util.Vector;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.cls.sugutomo.adapter.ExpandableListAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.InformationAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.InformationModel;
import com.loopj.android.http.RequestParams;

public class QuestionAnswerActivity extends BaseTabActivity implements
		APICallbackInterface {

	private SessionManager mSession;
	private Vector<InformationModel> mListInfor = new Vector<InformationModel>();
	ExpandableListAdapter listAdapter;
	ExpandableListView listview;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// add layout to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.question_answer_layout, null, false);
		frameLayout.addView(activityView);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		
		// UI
		listview = (ExpandableListView) findViewById(R.id.qa_expandableList);
		listAdapter = new ExpandableListAdapter(this, mListInfor);
		listview.setAdapter(listAdapter);

		mSession = SessionManager.getInstance(getApplicationContext());
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		InformationAPI inforAPI = new InformationAPI(this, this, mListInfor,
				Params.QA);
		inforAPI.setParams(request);
		inforAPI.onRunButtonPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}
	
	public int getPixelFromDips(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			listview.setIndicatorBounds(width - getPixelFromDips(35), width
					- getPixelFromDips(5));

		} else {
			listview.setIndicatorBoundsRelative(width - getPixelFromDips(35),
					width - getPixelFromDips(5));
		}
	}

	@Override
	public void handleReceiveData() {
		if (listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}
}
