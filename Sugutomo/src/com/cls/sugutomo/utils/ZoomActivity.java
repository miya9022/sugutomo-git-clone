/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cls.sugutomo.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.cls.sugutomo.R;
import com.cls.sugutomo.viewpagerindicator.CustomFragmentAdapter;

/**
 * A sample showing how to zoom an image thumbnail to full-screen, by animating
 * the bounds of the zoomed image from the thumbnail bounds to the screen
 * bounds.
 * 
 * <p>
 * In this sample, the user can touch one of two images. Touching an image zooms
 * it in, covering the entire activity content area. Touching the zoomed-in
 * image hides it.
 * </p>
 */
public class ZoomActivity extends FragmentActivity {
	/**
	 * Hold a reference to the current animator, so that it can be canceled
	 * mid-way.
	 */

	/**
	 * The system "short" animation time duration, in milliseconds. This
	 * duration is ideal for subtle animations or animations that occur very
	 * frequently.
	 */

	private int currentPos;
	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_zoom);
		//findViewById(R.id.container).getBackground().setAlpha(180);
		currentPos = getIntent().getIntExtra("pos", 0);
		// zoomImageFromThumb();

		mPager = (ViewPager) findViewById(R.id.zoom_pager);
		// CustomFragmentAdapter.userAvatarList = urlsAvatar;
		// CustomFragmentAdapter.mCount = urlsAvatar.size();
		CustomFragmentAdapter mAdapter = new CustomFragmentAdapter(
				getSupportFragmentManager(), true);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(currentPos);

		// Retrieve and cache the system's default "short" animation time.
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(0, R.anim.scale_exit);  
	}
	

	

}
