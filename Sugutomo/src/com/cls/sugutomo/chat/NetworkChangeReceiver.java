package com.cls.sugutomo.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.utils.Global;

public class NetworkChangeReceiver extends BroadcastReceiver {
	private static boolean firstConnect = true;
	@Override
	public void onReceive(final Context context, final Intent intent) {

		if (checkInternet(context)) {
			SharedPreferences pref;
			boolean status = true;
			try {
				pref = context.getSharedPreferences(Global.PREFERENCE_NAME,
						Context.MODE_PRIVATE);
				status = pref.getBoolean(Params.PARAM_STATUS, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (status) {
				Intent pushIntent = new Intent(context,
						ChatBackgroundService.class);
				context.startService(pushIntent);
//				Toast.makeText(context, "service sugutomo  create",
//						 Toast.LENGTH_LONG).show();
			}else{
//			 Toast.makeText(context, "service sugutomo not create because logout",
//			 Toast.LENGTH_LONG).show();
			}
			Global.getUnReadMessageTotal(context);
			//intent PARAM_RECONNECT_INTERNET_CHECK nedd call before PARAM_RECONNECT_INTERNET
			//because when receiver PARAM_RECONNECT_INTERNET we update chatlist or chat activity
			//so that PARAM_RECONNECT_INTERNET_CHECK don't need and we set CHATLIST_NEED_REFRESH
			//or CHAT_NEED_REFRESH = false in  receiver PARAM_RECONNECT_INTERNET
			// but if ths activity is not in top stack activity so we only receiver PARAM_RECONNECT_INTERNET_CHECK
			//and when comeback to activty => use CHATLIST_NEED_REFRESH,CHAT_NEED_REFRESH
			//to update or not 
			Log.v("","update because PARAM_RECONNECT_INTERNET NetworkChangeReceiver");
			Intent i = new Intent(context.getPackageName() + Params.PARAM_RECONNECT_INTERNET_CHECK);
			context.sendBroadcast(i);
			i = new Intent(context.getPackageName() + Params.PARAM_RECONNECT_INTERNET);
			context.sendBroadcast(i);
		}

	}

	boolean checkInternet(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isConnected =false;
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
            if(firstConnect) { 
                // do subroutines here
                firstConnect = false;
        		 isConnected = activeNetwork != null
        				&& activeNetwork.isConnected();
            }
        }
        else {
            firstConnect= true;
        }
		return isConnected;
		
		
	}

}
