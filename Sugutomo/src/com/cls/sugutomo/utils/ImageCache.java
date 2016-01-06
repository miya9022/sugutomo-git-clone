package com.cls.sugutomo.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
/*
 * For fast get bitmap in listView and gridView
 * save them to SoftReference. when app need memory. i can remove bitmap automatic.
 * Faster than get bitmap from sdcard. Increment smooth performance in UI when need load many image
*/
public class ImageCache {
	public static ImageCache instance;
	public HashMap<String, SoftReference<Drawable>> mCache;
	public HashMap<String, SoftReference<Bitmap>> mCacheBitmap;

	public ImageCache() {
	}

	public static ImageCache getInstance() {
		if (instance == null)
			instance = new ImageCache();
		return instance;
	}

	public void clearAll() {
		if (mCache != null) {
			mCache.clear();
			mCache = null;
		}
		if (mCacheBitmap != null) {
			mCacheBitmap.clear();
			mCacheBitmap = null;
		}
	}

	public Drawable get(String s) {
		Drawable drawable = null;
		Drawable drawable1;
		if (mCache == null) {
			drawable1 = null;
		} else {
			SoftReference<Drawable> softreference = (SoftReference<Drawable>) mCache
					.get(s);
			if (softreference != null)
				drawable = (Drawable) softreference.get();
			drawable1 = drawable;
		}
		return drawable1;
	}

	public Bitmap getBitmap(String s) {
		Bitmap bitmap1 = null;
		if (mCacheBitmap == null) {
			bitmap1 = null;
		} else {
			SoftReference<Bitmap> softreference = (SoftReference<Bitmap>) mCacheBitmap
					.get(s);
			if (softreference != null)
				bitmap1 = (Bitmap) softreference.get();
		}
		return bitmap1;
	}

	public void put(String s, Drawable drawable) {
		if (mCache == null)
			mCache = new HashMap<String, SoftReference<Drawable>>();
		mCache.put(s, new SoftReference<Drawable>(drawable));
	}

	public void putBitmap(String s, Bitmap bitmap) {
		if (mCacheBitmap == null)
			mCacheBitmap = new HashMap<String, SoftReference<Bitmap>>();
		mCacheBitmap.put(s, new SoftReference<Bitmap>(bitmap));
	}

}
