package com.cls.sugutomo.viewpagerindicator;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public final class ZoomFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";

	public static ZoomFragment newInstance(ImageModel _imgModel) {
		ZoomFragment fragment = new ZoomFragment(_imgModel);
		return fragment;
	}

	public ZoomFragment() {

	}

	public ZoomFragment(ImageModel _imgModel) {

		imgModel = _imgModel;
	}

	private String mContent = "???";
	private Bitmap bm = null;
	private ImageModel imgModel;
	private ImageView img;
	private ImageFetcher imageFetcher;
	private String imgFileName = "";// to check need redresh or not

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
		}
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		container.setBackgroundColor(Color.TRANSPARENT);
		img = new ImageView(getActivity());
		RelativeLayout layout = new RelativeLayout(getActivity());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.setBackgroundColor(Color.TRANSPARENT);
		layout.addView(img);

		img.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT));

		imageFetcher.loadImage(imgModel.getFullPath(),img);
//		if (bm != null && imgFileName.equalsIgnoreCase(imgModel.getFileName())) {
//			img.setImageBitmap(bm);
//		} else {
//			MyTask loadthumb = new MyTask();
//			loadthumb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		}

		

		return layout;
	}

	public void updateFragment(ImageModel newModel) {
		imgModel = newModel;
		if (bm != null && imgFileName.equalsIgnoreCase(imgModel.getFileName())) {
			img.setImageBitmap(bm);
		} else {
			MyTask loadthumb = new MyTask();
			loadthumb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
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
	}
}
