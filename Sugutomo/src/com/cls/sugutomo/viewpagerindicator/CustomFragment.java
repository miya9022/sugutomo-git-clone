package com.cls.sugutomo.viewpagerindicator;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.cls.sugutomo.utils.ZoomActivity;

public final class CustomFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";
	private static final String KEY_CONTENT_IMG_MODEL = "TestFragment:ContentIMG_MODEL";
	private static final String KEY_CONTENT_POS = "TestFragment:POST";

	public static CustomFragment newInstance(ImageModel _imgModel, int position) {
		CustomFragment fragment = new CustomFragment(_imgModel, position);
		return fragment;
	}

	public CustomFragment() {

	}

	public CustomFragment(ImageModel _imgModel, int position) {

		imgModel = _imgModel;
		pos = position;
	}

	private String mContent = "???";
	private Bitmap bm = null;
	private ImageModel imgModel;
	private int pos;
	private ImageView img;
	private ImageFetcher imageFetcher;
	private String imgFileName = "";// to check need redresh or not

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
			imgModel = (ImageModel) savedInstanceState
					.get(KEY_CONTENT_IMG_MODEL);
			pos = savedInstanceState.getInt(KEY_CONTENT_POS);
			
		}
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
	}
	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
	}

	void showImg() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		img = new ImageView(getActivity());
		RelativeLayout layout = new RelativeLayout(getActivity());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.addView(img);
		img.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT));
//		img.setImageBitmap(BitmapFactory.decodeResource(getActivity()
//				.getResources(), R.drawable.loader));

		if (imgModel != null && imgModel.isAvatar()) {
			TextView tv = new TextView(getActivity());
			RelativeLayout.LayoutParams lo = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			tv.setLayoutParams(lo);
			tv.setText(getActivity().getString(R.string.avatar));
			tv.setGravity(Gravity.CENTER);
			tv.setTextColor(Color.parseColor("#EEEEEE"));
			tv.setBackgroundColor(Color.parseColor("#55000000"));
			// tv.setBackground(background)
			layout.addView(tv);
		}
//		if (bm != null && imgFileName.equalsIgnoreCase(imgModel.getFileName())) {
//			img.setImageBitmap(bm);
//		} else {
//			MyTask loadthumb = new MyTask();
//			loadthumb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		}
		imageFetcher.loadImage(imgModel.getFullPath(),img);

		img.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//if (bm != null) {
					Intent intent = new Intent(getActivity(),
							ZoomActivity.class);
					intent.putExtra("pos", pos);
					startActivity(intent);
					getActivity().overridePendingTransition(R.anim.scale_enter,
							0);
				//}
			}
		});

		return layout;
	}

	public void updateFragment(ImageModel newModel) {
//		imgModel = newModel;
//		if (bm != null && imgFileName.equalsIgnoreCase(imgModel.getFileName())) {
//			img.setImageBitmap(bm);
//		} else {
//			MyTask loadthumb = new MyTask();
//			loadthumb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		}
	}

	class MyTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// for (int i = 0; i < Global.listFriend.size(); i++) {
			if (imgModel == null)
				return null;
			String url = imgModel.getFullPath();
			if (url != null && url.length() > 0)
				bm = LoadSaveImage.getThumbImage(url, Params.PARAM_USER_AVATAR
						+ File.separator + imgModel.getUserId(),
						"" + imgModel.getFileName());
			imgFileName = imgModel.getFileName();

			// }
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (bm == null) {
				// Toast.makeText(this, "null avatar  ",
				// Toast.LENGTH_SHORT).show();
			} else {
				img.setImageBitmap(bm);
			}

		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTENT, mContent);
		outState.putSerializable(KEY_CONTENT_IMG_MODEL, imgModel);
		outState.putInt(KEY_CONTENT_POS, pos);
	}
}
