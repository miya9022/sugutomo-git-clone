package com.cls.sugutomo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.utils.Global;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;
	private final  String SHORTCUT_CREATED = "shortcut_created"; 

	// Constructor

	private static SessionManager instance = null;

	private SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(Global.PREFERENCE_NAME,
				PRIVATE_MODE);
		editor = pref.edit();
	}

	public static SessionManager getInstance(Context context) {
		if (instance == null) {
			instance = new SessionManager(context);
		}
		return instance;
	}
	
	public int getLoginType() {
	    return pref.getInt(Params.PARAM_TYPE_LOGIN, -1);
	}
	public int getDistanceInSetting() {
	    return pref.getInt(Params.SETTING_MY_LOCATION, Params.SETTING_DEFAULT_LOCATION);
	}
	
	public String getToken() {
		return pref.getString(Params.PARAM_TOKEN, null);
	}

	public int getUserId() {
		return pref.getInt(Params.PARAM_USER_ID, -1);
	}

	//
	public void resetToken() {
		editor.putString(Params.PARAM_TOKEN, null);
		editor.commit();
	}
	
	
	public void setShortcutCreated() {
	    editor.putBoolean(SHORTCUT_CREATED, true);
	    editor.commit();
	}
	
	public boolean isShortcutCreated() {
	    return pref.getBoolean(SHORTCUT_CREATED, false);
	}

	public void createLoginSession(int userid, String token, int loginType, boolean autoLogin) {
        editor.putBoolean(Params.PARAM_STATUS, autoLogin);
        editor.putInt(Params.PARAM_USER_ID, userid);
        editor.putInt(Params.PARAM_TYPE_LOGIN, loginType);
        editor.putString(Params.PARAM_TOKEN, token);
        Log.v("", "userid:"+userid+"token:"+token);
        editor.commit();
    }
	
	public void storeOpenFireInfo(String username, String password) {
	    editor.putString(Params.PARAM_USERNAME, username);
	    editor.putString(Params.PARAM_PASSWORD, password);
	    editor.commit();
	}
	
	public String getUsername() {
	    return pref.getString(Params.PARAM_USERNAME, null);
	}
	
	public String getPassword() {
	    return pref.getString(Params.PARAM_PASSWORD, null);
	}
	
	/**
	 * Clear session details
	 * */
	public void logoutUser() {
		// Clearing all data from Shared Preferences
		editor.putBoolean(Params.PARAM_STATUS, false);
		editor.putString(Params.PARAM_TOKEN, null);
		editor.putString(Params.PARAM_USERNAME, null);
		editor.putString(Params.PARAM_PASSWORD, null);
		editor.commit();
	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(Params.PARAM_STATUS, true);
	}
	
	public void setAutoLogin(boolean set){
		editor.putBoolean(Params.PARAM_STATUS, set);
		editor.commit();
	}
	
	public void saveRequirePassword(boolean set){
		editor.putBoolean(Params.PARAM_REQUIRE_PASSWORD, set);
		editor.commit();
	}
	
	public boolean getRequirePassword(){
		return pref.getBoolean(Params.PARAM_REQUIRE_PASSWORD, false);
	}
}
