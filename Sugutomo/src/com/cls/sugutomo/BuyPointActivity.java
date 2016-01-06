package com.cls.sugutomo;

import java.io.File;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.adapter.CustomBuyPointGridViewAdapter;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.MyPointAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.BuyPointModelItem;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.loopj.android.http.RequestParams;
import com.payment.ConfigIAP;
import com.payment.GooglePlayIAPManager;
import com.payment.PaymentListenerIAP;

public class BuyPointActivity extends BaseTabActivity implements
		PaymentListenerIAP, APICallbackInterface {
	public static String UPDATE_POINTS = "UPDATE_PTS_POS";
	private int currentPoint;
	private View currentPointClick;
	private CircularImageView avatar;
	private TextView txtCurrentPoint, txtBuyCost;
	CustomBuyPointGridViewAdapter customGridAdapter;
	Vector<BuyPointModelItem> listPoint;
	private ImageFetcher imageFetcher;
	private AlertDialog dialog;
	BuyPointModelItem pointBuy = new BuyPointModelItem(-1, 0, 0);
	private BroadcastReceiver receiver;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GooglePlayIAPManager.getIntance().onCreate(this, this,
				Global.userInfo.getUserId() + "", ConfigIAP.consumeItems, null);
		// add layout of activity to framelayout
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.buy_point_activity,
				null, false);
		frameLayout.addView(activityView);
		// set title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		init();
		final GridView gridview = (GridView) findViewById(R.id.buypoint_gridview);
		listPoint = new Vector<BuyPointModelItem>();
		for (int i = 0; i < ConfigIAP.consumeItems.size(); i++) {
			listPoint.add(new BuyPointModelItem(ConfigIAP.consumeItemsPrice
					.get(i).cost, ConfigIAP.consumeItemsPrice.get(i).value));
		}
		customGridAdapter = new CustomBuyPointGridViewAdapter(
				BuyPointActivity.this, R.layout.custom_img_buypoint, listPoint,
				"\npt");
		gridview.setAdapter(customGridAdapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				pointBuy.id = position;
				pointBuy.point = listPoint.elementAt(position).getPoint();
				pointBuy.cost = listPoint.elementAt(position).getCost();
				txtBuyCost.setText("" + pointBuy.cost);
				if (currentPointClick != null)
					currentPointClick.setVisibility(View.GONE);
				currentPointClick = view
						.findViewById(R.id.item_image_buypoint_red);
				currentPointClick.setVisibility(View.VISIBLE);
				customGridAdapter.setIdCheck(position);
			}
		});
		initUpdateBroadcastReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
		loadPoint();// update point neu back tu activites khac
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
				new IntentFilter(BuyPointActivity.UPDATE_POINTS));
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
	}

	public void init() {

		txtCurrentPoint = (TextView) findViewById(R.id.buypoint_yourpoint);
		updateCurrentPoint();
		txtBuyCost = (TextView) findViewById(R.id.buypoint_cost);

		// AVATAR
		avatar = (CircularImageView) findViewById(R.id.buypoint_avatar);
		imageFetcher = MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
		imageFetcher.loadImage(Global.userInfo.getUserAvatar(), avatar);
		// Bitmap bm = LoadSaveImage.getImageFromSDCard(Params.PARAM_USER_AVATAR
		// + File.separator + Global.userInfo.getUserId(), Global.userInfo
		// .getUserAvatar().hashCode() + "");
		//
		// if (bm == null) {
		// Toast.makeText(BuyPointActivity.this, "null avatar  ",
		// Toast.LENGTH_SHORT).show();
		// } else {
		// avatar.setImageBitmap(bm);
		// }
	}

	public void onBuyPoint(View v) {
		if (pointBuy.cost > 0) {
			dialog = Global.customDialog(this,
					getString(R.string.sure_buy_point),
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							GooglePlayIAPManager.getIntance().buyItemPlayStore(
									ConfigIAP.consumeItems.get(pointBuy.id),
									pointBuy.cost);
						}
					});

		} else {
			Toast.makeText(BuyPointActivity.this,
					this.getString(R.string.chose_point_package),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void loadPoint() {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, SessionManager.getInstance(this)
				.getUserId());
		request.put(Params.PARAM_TOKEN_, SessionManager.getInstance(this)
				.getToken());
		MyPointAPI rankAPI = new MyPointAPI(this, this);
		rankAPI.setUseProgresBar(false);
		rankAPI.setParams(request);
		rankAPI.onRunButtonPressed();
	}

	private void updateCurrentPoint() {
		currentPoint = Global.userInfo.getPossessPoint();
		txtCurrentPoint.setText(currentPoint + "pt");
	}

	private void initUpdateBroadcastReceiver() {
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				currentPoint = intent
						.getIntExtra(Params.PARAM_POSSESS_POINT, 0);
				Global.userInfo.setPossessPoint(currentPoint);
				currentPoint = Global.userInfo.getPossessPoint();
				txtCurrentPoint.setText(currentPoint + "pt");
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean handle = GooglePlayIAPManager.getIntance().onActivityResult(
				requestCode, resultCode, data);
		if (!handle)
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		GooglePlayIAPManager.getIntance().onDestroy();
	}

	@Override
	public void onSetupIAP(boolean isSucess, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPurchaseItem(boolean isSucess, String itemId) {
		// TODO Auto-generated method stub
		if (isSucess) {
			updateCurrentPoint();
		}
	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		updateCurrentPoint();
	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub

	}

}
