package com.cls.sugutomo;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.adapter.AppealAPI;
import com.cls.sugutomo.adapter.CustomBuyPointGridViewAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.BuyPointModelItem;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.loopj.android.http.RequestParams;

public class BuyVipActivity extends BaseTabActivity implements
		APICallbackInterface {
	private int currentPoint = 0;
	private View currentVipClick;
	private CircularImageView avatar;
	private TextView txtBuyCost;
	CustomBuyPointGridViewAdapter customGridAdapter;
	static boolean loadPrice = false;

	BuyPointModelItem vipBuy = new BuyPointModelItem(0, 0);
	private SessionManager mSession;
	private String title;
	private ImageFetcher imageFetcher;
private AlertDialog dialogBuy;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// add layout of activity to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.buy_vip_activity,
				null, false);
		frameLayout.addView(activityView);
		mSession = SessionManager.getInstance(getApplicationContext());
		// set title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}

		final GridView gridview = (GridView) findViewById(R.id.buyvip_gridview);

		customGridAdapter = new CustomBuyPointGridViewAdapter(
				BuyVipActivity.this, R.layout.custom_img_buypoint,
				Global.listPoint, "\nhour");
		gridview.setAdapter(customGridAdapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				vipBuy.duration = Global.listPoint.elementAt(position)
						.getduration();
				vipBuy.cost = Global.listPoint.elementAt(position).getCost();
				txtBuyCost.setText(vipBuy.cost + "pt");
				if (currentVipClick != null)
					currentVipClick.setVisibility(View.GONE);
				currentVipClick = view
						.findViewById(R.id.item_image_buypoint_red);
				currentVipClick.setVisibility(View.VISIBLE);
				customGridAdapter.setIdCheck(position);
			}
		});
		init();
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		updateCurrentPoint();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}



	public void init() {

		// txtCurrentPoint = (TextView) findViewById(R.id.buypoint_yourpoint);
		txtBuyCost = (TextView) findViewById(R.id.buyvip_cost);
		updateCurrentPoint();

		// AVATAR
		avatar = (CircularImageView) findViewById(R.id.buy_vip_avatar);
		imageFetcher = MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
		imageFetcher.loadImage(Global.userInfo.getUserAvatar(), avatar);
//		Bitmap bm = LoadSaveImage.getImageFromSDCard(Params.PARAM_USER_AVATAR+File.separator+Global.userInfo.getUserId(),
//				Global.userInfo.getUserAvatar().hashCode()+"");
//		if (bm == null) {
//			Toast.makeText(BuyVipActivity.this, "null avatar  ",
//					Toast.LENGTH_SHORT).show();
//		} else {
//			avatar.setImageBitmap(bm);
//		}

		if (!loadPrice) {

			RequestParams request = new RequestParams();

			request.put(Params.PARAM_USER_ID_, mSession.getUserId());
			request.put(Params.PARAM_TOKEN_, mSession.getToken());
			AppealAPI api = new AppealAPI(this, this, Global.listPoint,
					AppealAPI.LOAD_PRICE);
			api.setParams(request);
			api.onRunButtonPressed();
		} else {
			handleGetList();
		}
	}

	public void onBuyVip(View v) {
		if (vipBuy.cost > 0) {
		  dialogBuy =	Global.customDialog(this, getString(R.string.sure_buy_appeal), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					RequestParams request = new RequestParams();
					request.put(Params.PARAM_USER_ID_, mSession.getUserId());
					request.put(Params.PARAM_TOKEN_, mSession.getToken());
					request.put(Params.PARAM_DURATION, vipBuy.duration);
					AppealAPI api = new AppealAPI(BuyVipActivity.this, BuyVipActivity.this, null,
							AppealAPI.BUY_APPEAL);
					api.setParams(request);
					api.onRunButtonPressed();
					dialogBuy.dismiss();
				}
			});
			

		} else {
//			ShowMessage.showDialog(this, this.getTitle().toString(),
//					getString(R.string.chose_appeal_package));
			Global.customDialog(this, getString(R.string.chose_appeal_package), null);
		}
	}

	private void updateCurrentPoint() {
		currentPoint = Global.userInfo.getPossessPoint();
		// txtCurrentPoint.setText(currentPoint+"pt");
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		// update text show posion point everwhere show it
		updateCurrentPoint();
		//FORCE userlist refresh if back click to it
		Global.LAST_TIME_RELOAD  =0;
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub
		if (Global.listPoint.size() > 0) {
			loadPrice = true;
			customGridAdapter.notifyDataSetChanged();
		}
		
		


	}

}