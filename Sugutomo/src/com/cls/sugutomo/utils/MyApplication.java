package com.cls.sugutomo.utils;

import android.app.Application;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.cls.sugutomo.loadimage.ImageCache;
import com.cls.sugutomo.loadimage.ImageFetcher;

public class MyApplication extends Application {
	private Uri picUri;
    private static MyApplication instance;	
    private ImageFetcher imageFetcher;
    NotificationCompat.Builder mBuilder ;
	public static synchronized MyApplication getInstance(){
		return instance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		instance=this;
		mBuilder = new NotificationCompat.Builder(this);
    	// initialize Image cache and fetcher 
    			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, "img");
    			cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
    			imageFetcher = new ImageFetcher(this, 300);
    			imageFetcher.addImageCache(null, cacheParams);
    			imageFetcher.setImageFadeIn(false);
    }
	public NotificationCompat.Builder getNotificationCompatBuilder() {
		return mBuilder;
	}
	public ImageFetcher getImageFetcher() {
		return imageFetcher;
	}
	public Uri getPicUri() {
		return picUri;
	}

	public void setPictUri(Uri auri) {
		picUri = auri;
	}
}
