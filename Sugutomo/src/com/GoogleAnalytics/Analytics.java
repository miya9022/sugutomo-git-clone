package com.GoogleAnalytics;
import java.util.HashMap;

import android.app.Application;

import com.cls.sugutomo.R;
import com.cls.sugutomo.loadimage.ImageCache;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

 
public class Analytics extends Application {
	private ImageFetcher imageFetcher;
    private static final String PROPERTY_ID = "UA-60322866-1";
 
    public static int GENERAL_TRACKER = 0;
 
    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        ECOMMERCE_TRACKER,
    }
 
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
 
    public Analytics() {
        super();
    }
 
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
        
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            analytics.enableAutoActivityReports(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(
                            R.xml.global_tracker)
                            : analytics.newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            t.enableAutoActivityTracking(true);
            t.enableExceptionReporting(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
    private static Analytics instance;	
	public static synchronized Analytics getInstance(){
		return instance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		instance=this;
    	// initialize Image cache and fetcher 
    			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, "img");
    			cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
    			imageFetcher = new ImageFetcher(this, 300);
    			imageFetcher.addImageCache(null, cacheParams);
    			imageFetcher.setImageFadeIn(false);
    }
	public ImageFetcher getImageFetcher() {
		return imageFetcher;
	}
}
