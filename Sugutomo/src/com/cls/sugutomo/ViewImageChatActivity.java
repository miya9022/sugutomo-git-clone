package com.cls.sugutomo;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.loadimage.ImageWorker.ImageWorkerEventListener;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;

public class ViewImageChatActivity extends BaseTabActivity {

	private static final String STATE_POSITION = "STATE_POSITION";

	private ViewPager mPager;
	private int pagerPosition;
	private ImageView mImageView;

	private String mImagePath;
	private String mImageName;

	private Bitmap imageBitmap;
	private ImageFetcher imgFetcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.view_image_chat_activity, null, false);
		frameLayout.addView(activityView);
		mImageView = (ImageView) findViewById(R.id.image_pager);
		// hide the action bar in fullscreen mode
		getActionBar().hide();
		imgFetcher=MyApplication.getInstance().getImageFetcher();
		Intent i = getIntent();
		if (i != null) {
			mImagePath = i.getStringExtra(Params.PARAM_IMAGE_FULL_URL);
			if ( mImagePath != null) {
				imgFetcher.loadImage(mImagePath, mImageView, new ImageWorkerEventListener() {
					
					@Override
					public void OnImageLoadingCompletion(ImageView parent, BitmapDrawable value) {
						// TODO Auto-generated method stub
						//Toast.makeText(getApplicationContext(), "load finish", Toast.LENGTH_SHORT).show();
						Log.v("", "img url:"+mImagePath);
						try {
							
						imageBitmap =((BitmapDrawable)mImageView.getDrawable()).getBitmap();
						} catch (Exception e) {
							// TODO: handle exception
						}
//						if(value!=null)
							//imageBitmap =value.getBitmap();
					}
				});
			}

		}
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.llSetWallpaper:
			if (imageBitmap != null)
				LoadSaveImage.setAsWallpaper(imageBitmap, this);
			break;

		case R.id.llShareImage:
			if (imageBitmap == null)
				return;
			String pathofBmp = Images.Media.insertImage(getContentResolver(),
					imageBitmap, "share", null);
			Uri bmpUri = Uri.parse(pathofBmp);
			final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
			intent.setType("image/png");
			startActivity(intent);
			break;
		}
	}
}
