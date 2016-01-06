package com.cls.sugutomo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.SendAllMessageActivity;
import com.cls.sugutomo.SplashScreen;
import com.cls.sugutomo.SplashScreenActivity;
import com.cls.sugutomo.api.MapAPI;
import com.cls.sugutomo.api.SaveGCMRegisterAPI;
import com.cls.sugutomo.api.UnReadAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.dialog.MessageDialog;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.map.MapActivity;
import com.cls.sugutomo.model.BuyPointModelItem;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.MessagesModel;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.model.StampModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.userlist.UserListActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.RequestParams;

public class Global {
	
	public static final String GOOGLE_PROJ_ID = "318300684767";
	public static long LAST_TIME_RELOAD = 0;
	public static UserModel userInfo = null;
	public static final int ADMIN = 1;
	public static boolean LOAD_MORE_USER = true;
	public static final int LIMIT_USER_IN_PAGE = 15;
	public static final int LIMIT_MESSAGE_IN_CHAT = 20;
	public static final int LIMIT_CONVERSATION_IN_PAGE = 12;
	public static final int PADDING = 20;
	public static boolean CHAT_NEED_REFRESH = false;
	public static boolean CHATLIST_NEED_REFRESH = false;
	public static HashMap<String, ArrayList<String>> listBadWord = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, UserModel> listFriendMap = new HashMap<String, UserModel>();
	public static Vector<UserModel> listFriend = new Vector<UserModel>();
	public static Vector<UserModel> listChatFriend = new Vector<UserModel>();
	public static Vector<UserModel> listFavorite = new Vector<UserModel>();
	public static Vector<UserModel> listFootprint = new Vector<UserModel>();
	public static Vector<BuyPointModelItem> listPoint = new Vector<BuyPointModelItem>();
	public static ArrayList<UserModel> listRankMan = new ArrayList<UserModel>();
	public static ArrayList<UserModel> listRankWomen = new ArrayList<UserModel>();
	public static ArrayList<Integer> listUserBlocked = new ArrayList<Integer>();
	public static ArrayList<Integer> listBlockedOrBeBlocked = new ArrayList<Integer>();
	public static ArrayList<Integer> listFavoriteId = new ArrayList<Integer>();
	public static ArrayList<StampModel> listStampModel = new ArrayList<StampModel>();
	// vao edit profile van phai load lai mListProfileModel
	// mListProfileModel chi la danh sach profile torng luk load userlist de
	// show luk filter thoi
	public static ArrayList<ProfileModel> mListProfileModel = new ArrayList<ProfileModel>();
	public static int DIALOG_NOTIFICATION_ID = -1;
	public static boolean IS_OPEN_DIALOG_NOTIFICATION = false;
	public static boolean IS_LOADED_FAVORITE_LIST = false;
	public static boolean isRequestingFavorite1User = false;
	public static final int NOTIFY_REFRESH_DONE = 1001;
	public static final String PREFERENCE_NAME = "Sugutomo";
	public static final String PREFERENCE_UUID = "PREFERENCE_UUID";
	public static final String RADIUS_KEY = "RADIUS_KEY";
	public static final String apiSecretKey = "K34eJjEt";
	public static String apiBaseUrl = "http://chatserver.codeloversvietnam.com:8888/apix/";
	public static LocationClient mLocationClient;
	public static TabHost mTab;
	public static boolean appInBackGround = false;
	public static boolean isUploadingImageInEditProfile = false;
	public static BaseTabActivity currentActivity;
	public static MapActivity mapActivity;
	public static ChatDetailActivity chatDetailActivity;
	public static ChatListActivityServer chatListActivityServer;
	public static SendAllMessageActivity sendAllMessageActivity;
	// public static ChatService chatService;
	public static Vector<MessagesModel> listMessage = new Vector<MessagesModel>();
	public static String CHAT_SERVER = "sugutomo.codelovers.vn";
	public static int CHAT_SERVER_PORT = 5222;
	public static String CHAT_SECRET = "qr3TARIL";
	public static int newFootPrint = 0;
	public static int newMessage = 0;
	// Register
	public static final int TYPE_ANONYMOUS_REGISTER = 3;
	public static final int TYPE_ANONYMOUS = 2;
	public static final int TYPE_FACEBOOK = 1;
	public static final int TYPE_EMAIL = 0;
	public static final String REGEX_USERNAME = "^[a-z0-9_-]{3,16}$";
	public static final String REGEX_EMAIL = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
	public static final String REGEX_PASSWORD = "^[a-zA-Z0-9_-]{6,18}$";

	// edit text
	public static final int MAX_LINE = 10;
	public static final int MAX_LENGTH = 50;

	// public static String KEY_XOR = "sugutomo_key_xor";
	public static boolean checkInput(String input, String regex) {
		return input.matches(regex);
	}

	public static UserModel setUserInfo(JSONObject userInfo,
			JSONObject openfireInfo) {
		UserModel user = new UserModel();
		int user_id = userInfo.optInt(Params.PARAM_ID);
		user.setUserId(user_id);
		user.setName(userInfo.optString(Params.PARAM_NAME));
		// user.setUserName(userInfo.optString(Params.PARAM_USERNAME));
		if (user_id > 0) {
			user.setUserName(String.valueOf(user_id));
		} else {
			user.setUserName(userInfo.optString(Params.PARAM_USERNAME));
		}
		user.setEmail(userInfo.optString(Params.PARAM_EMAIL));
		user.setFacebookId(userInfo.optString(Params.PARAM_FB));
		if (openfireInfo != null)
			user.setPassword(openfireInfo.optString(Params.PARAM_PASSWORD));
		user.setSex(userInfo.optInt(Params.PARAM_SEX));
		user.setIsLogin(userInfo.optInt(Params.PARAM_IS_LOGIN));
		user.setWallStatus(userInfo.optString(Params.PARAM_WALL_STATUS));
		user.setImageId(userInfo.optInt(Params.PARAM_IMAGE_ID));
		user.setUserAvatar(userInfo.optString(Params.PARAM_IMAGE));
		user.setBirthday(userInfo.optString(Params.PARAM_BIRTHDAY));

		user.setInterestedPartner(userInfo
				.optInt(Params.PARAM_INTERESTED_PARTNER));
		user.setPossessPoint(userInfo.optInt(Params.PARAM_POSSESS_POINT));
		user.setHeight(userInfo.optInt(Params.PARAM_HEIGHT));
		user.setBodyFigure(userInfo.optInt(Params.PARAM_BODY_FIGURE));
		user.setGpsLat(userInfo.optDouble(Params.PARAM_GPS_LAT));
		user.setGpsLng(userInfo.optDouble(Params.PARAM_GPS_LNG));
		user.setTimeLastLogin(userInfo
				.optLong(Params.PARAM_LAST_LOGIN_DATETIME));
		user.setDistance(userInfo.optString(Params.PARAM_DISTANCE));
		user.setCountFootprint(userInfo.optInt(Params.PARAM_COUNT));

		// add age
		user.setAge(userInfo.optInt(Params.PARAM_AGE));
		return user;
	}

	public static JSONObject getUserInfo(UserModel user) {
		JSONObject userinfo = new JSONObject();
		try {
			userinfo.put(Params.PARAM_ID, user.getUserId());
			userinfo.put(Params.PARAM_NAME, user.getName());
			userinfo.put(Params.PARAM_USERNAME,
					String.valueOf(user.getUserId()));
			userinfo.put(Params.PARAM_EMAIL, user.getEmail());
			userinfo.put(Params.PARAM_FB, user.getFacebookId());
			userinfo.put(Params.PARAM_SEX, user.getSex());
			userinfo.put(Params.PARAM_IS_LOGIN, user.getIsLogin());
			userinfo.put(Params.PARAM_WALL_STATUS, user.getWallStatus());
			userinfo.put(Params.PARAM_IMAGE_ID, user.getImageId());
			userinfo.put(Params.PARAM_IMAGE, user.getUserAvatar());
			userinfo.put(Params.PARAM_BIRTHDAY, user.getBirthday());
			userinfo.put(Params.PARAM_AGE, user.getAge());

			userinfo.put(Params.PARAM_INTERESTED_PARTNER,
					user.getInterestedPartner());
			userinfo.put(Params.PARAM_POSSESS_POINT, user.getPossessPoint());
			userinfo.put(Params.PARAM_HEIGHT, user.getHeight());
			userinfo.put(Params.PARAM_BODY_FIGURE, user.getBodyFigure());
			userinfo.put(Params.PARAM_GPS_LAT, user.getGpsLat());
			userinfo.put(Params.PARAM_GPS_LNG, user.getGpsLng());
			userinfo.put(Params.PARAM_LAST_LOGIN_DATETIME,
					user.getTimeLastLogin());
			userinfo.put(Params.PARAM_DISTANCE, user.getDistance());
			userinfo.put(Params.PARAM_COUNT, user.getCountFootprint());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return userinfo;
	}

	/**
	 * Plays device's default notification sound
	 * */
	public static void playBeep(Context mContext) {
		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(mContext, notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return AndroidID
	 * 
	 * @return
	 */
	public static String getDeviceUDID(Context context) {
		String uuid;
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceID = tm.getDeviceId();
		// String simID = tm.getSimSerialNumber();
		// buggy: many divce can'r return android ID or default ID
		// 9774d56d682e549c

		if (deviceID == null) {
			deviceID = "";
		}

		// if (simID == null) {
		// simID = "";
		// }
		uuid = getUniquePsuedoID(deviceID, context);
		return uuid;

	}

	public static String getUniquePsuedoID(String id, Context context) {
		if (id.length() > 2) {
			return new UUID(id.hashCode(), new StringBuilder(id).reverse()
					.toString().hashCode()).toString();
			// return (String.valueOf(id.hashCode()));
		}
		String serial = null;
		String m_szDevIDShort = null;
		try {

			// devide id deu ko lay duoc thi`
			m_szDevIDShort = "" + (Build.BOARD.length() % 10)
					+ (Build.BRAND.length() % 10)
					+ (Build.CPU_ABI.length() % 10)
					+ (Build.DEVICE.length() % 10)
					+ (Build.MANUFACTURER.length() % 10)
					+ (Build.MODEL.length() % 10)
					+ (Build.PRODUCT.length() % 10);

			serial = android.os.Build.class.getField("SERIAL").get(null)
					.toString()
					+ id;
			// Go ahead and return the serial for api => 9
		} catch (Exception exception) {
			// String needs to be initialized
			serial = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		}

		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode())
				.toString();

	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device
	 * density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on
	 *         device density
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method converts device specific pixels to density independent
	 * pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	@SuppressWarnings("deprecation")
	public static int getWidthScreen(Context context) {
		int measuredWidth = 0;
		Point size = new Point();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			wm.getDefaultDisplay().getSize(size);
			measuredWidth = size.x;
		} else {
			Display d = wm.getDefaultDisplay();
			measuredWidth = d.getWidth();
		}
		return measuredWidth;
	}

	@SuppressWarnings("deprecation")
	public static int getHeightScreen(Context context) {
		int measuredHeight = 0;
		Point size = new Point();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			wm.getDefaultDisplay().getSize(size);
			measuredHeight = size.y;
		} else {
			Display d = wm.getDefaultDisplay();
			measuredHeight = d.getHeight();
		}
		return measuredHeight;
	}

	public static String encodeMD5(String source) throws Exception {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] data = source.getBytes();
			md.update(data);

			byte[] digest = md.digest();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < digest.length; i++) {
				int b = (0xFF & digest[i]);
				if (b < 16) {
					sb.append("0");
				}
				sb.append(Integer.toHexString(b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
	}

	public static String getTimeDate(long time) {
		return new java.text.SimpleDateFormat("MM/dd/yyyy")
				.format(new java.util.Date(time));
	}

	public static String getTimeHour(long time) {
		return new java.text.SimpleDateFormat("HH:mm:ss")
				.format(new java.util.Date(time));
	}

	public static String calculateTime(Context mContext, long timeLogin) {
		int diffYears = 0, diffMonths = 0, diffWeeks = 0, diffDays = 0, diffHours = 0, diffMinutes = 0, diffSeconds = 0;
		String calculateTime = "";
		long timeNowEpoch = System.currentTimeMillis();

		// Log.v("Global", "timelogin=" + timeLogin + " timeNow=" +
		// timeNowEpoch);
		// Log.v("Global", "timelogin=" + (timeLogin*1000 -timeNowEpoch));
		DateTime dt1 = new DateTime(timeLogin * 1000);
		DateTime dt2 = new DateTime(timeNowEpoch);
		try {
			diffYears = Years.yearsBetween(dt1, dt2).getYears();
			diffMonths = Months.monthsBetween(dt1, dt2).getMonths();
			diffWeeks = Weeks.weeksBetween(dt1, dt2).getWeeks();
			diffDays = Days.daysBetween(dt1, dt2).getDays();
			diffHours = Hours.hoursBetween(dt1, dt2).getHours() % 24;
			diffMinutes = Minutes.minutesBetween(dt1, dt2).getMinutes() % 60;
			diffSeconds = Seconds.secondsBetween(dt1, dt2).getSeconds() % 60;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (diffYears > 0)
			calculateTime = diffYears + mContext.getString(R.string.year);
		else if (diffMonths > 0)
			calculateTime = diffMonths + mContext.getString(R.string.month);
		else if (diffWeeks > 0)
			calculateTime = diffWeeks + mContext.getString(R.string.week);
		else if (diffDays > 0)
			calculateTime = diffDays + mContext.getString(R.string.day);
		else if (diffHours > 0)
			calculateTime = diffHours + mContext.getString(R.string.hour);
		else if (diffMinutes > 0)
			calculateTime = diffMinutes + mContext.getString(R.string.minute);
		else if (diffSeconds > 0)
			calculateTime = diffSeconds + mContext.getString(R.string.second);
		else
			calculateTime = "1" + mContext.getString(R.string.second);
		// Log.v("Global", diffYears + " years, " + diffMonths + " month, "
		// + diffWeeks + " weeks, " + diffDays + " days, " + diffHours
		// + " hours, " + diffMinutes + " minutes, " + diffSeconds
		// + " seconds.");
		// Log.v("Global", " diff " + calculateTime);
		return calculateTime;
	}

	public static boolean checkGPSStatus(Context mContext) {
		LocationManager service = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return enabled;
	}

	public static UserModel getFriendById(String fbId) {
		try {
			if (fbId == null || listFriend == null)
				return null;
			// if(listFriendMap.get(fbId)!=null) return listFriendMap.get(fbId);
			for (int i = 0; i < listFriend.size(); i++) {
				if (listFriend.get(i).getUserName().equals(fbId))
					return listFriend.get(i);
			}
			if (fbId.equals(Global.userInfo.getUserName()))
				return Global.userInfo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static double[] getLocationNotShift(double longitude,
			double latitude, float radius) {
		// Latitude: 1 deg = 110.54 km
		// Longitude: 1 deg = 111.320*cos(latitude) km
		// shift location di radius met;
		double[] gps = new double[2];
		gps[1] = latitude - (radius / 110540f);
		gps[0] = longitude;
		return gps;
	}

	public static double[] getLocation(double longitude, double latitude,
			float radius) {
		// Latitude: 1 deg = 110.54 km
		// Longitude: 1 deg = 111.320*cos(latitude) km
		// shift location di radius met;
		double[] gps = new double[2];
		gps[1] = latitude + (radius / 110540f);
		gps[0] = longitude;
		return gps;
		// Random random = new Random();
		// // Convert radius from meters to degrees
		// double radiusInDegrees = radius / 111320f/2;
		// double u = 1;//random.nextDouble();
		// double v = 1;//random.nextDouble();
		// double w = radiusInDegrees * Math.sqrt(u);
		// double t = 2 * Math.PI * v;
		// double x = w * Math.cos(t);
		// double y = w * Math.sin(t);
		//
		// // Adjust the x-coordinate for the shrinking of the east-west
		// distances
		// double new_x = x / Math.cos(latitude);
		//
		// double foundLongitude = new_x + longitude;
		// double foundLatitude = y + latitude;
		// gps[0] =foundLongitude;
		// gps[1]=foundLatitude;
		// return gps;
	}

	public static float distFrom(double d, double e, double f, double g) {
		double earthRadius = 6371; // kilometers
		double dLat = Math.toRadians(f - d);
		double dLng = Math.toRadians(g - e);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(f))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}

	public static String getAge(String birthday) {
		try {
			String[] parts = birthday.split("-");
			DateTime start = new DateTime(Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 0,
					0, 0, 0);
			DateTime end = new DateTime();
			Period period = new Period(start, end);
			return period.getYears() + "";
		} catch (Exception e) {
			return "unknow";
		}
	}

	public static float makeRounding(float distance) {
		float result;
		int x = (int) (distance / 50);
		if (x % 2 == 0) {
			result = x * 50;
		} else {
			result = (x + 1) * 50;
		}
		return result;
	}

	public static String distanceBetweenGPS(UserModel person1,
			UserModel person2, Context ctx) {
		String distance = "unknow";
		try {
			DecimalFormat df = new DecimalFormat("0");
			// df.setRoundingMode(RoundingMode.UP);
			float[] distances = new float[3];
			if (person1 != null && person2 != null) {
				Location.distanceBetween(person1.getGpsLat(),
						person1.getGpsLng(), person2.getGpsLat(),
						person2.getGpsLng(), distances);
				distances[0] = makeRounding(distances[0]);
				if (distances[0] > 1000) {
					float km = distances[0] / 1000;
					if (km > 1000)
						distance = ctx.getString(R.string.diff_distance)
								+ "1000km";
					else
						distance = ctx.getString(R.string.diff_distance)
								+ df.format(km) + "km";
				} else
					distance = ctx.getString(R.string.diff_distance)
							+ df.format(distances[0]) + "m";
				// Log.v("", "distance "+ distances[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return distance;
	}

	public static Drawable getAndroidDrawable(Context context,
			String pDrawableName) {
		int resourceId = context.getResources().getIdentifier(pDrawableName,
				"drawable", "android");
		if (resourceId == 0) {
			return null;
		} else {
			return context.getResources().getDrawable(resourceId);
		}
	}

	public static Drawable resizeImage(Context ctx, int resId, int w, int h) {
		float scaleDensity = ctx.getResources().getDisplayMetrics().density;
		Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
				resId);
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = (int) (w * scaleDensity);
		int newHeight = (int) (h * scaleDensity);
		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		return new BitmapDrawable(ctx.getResources(), resizedBitmap);
	}

	public static int distanceBetweenGPSToMet(UserModel person1,
			UserModel person2) {
		int distance = 0;
		// distance return km if isKmType is true,else return m
		float[] distances = new float[3];
		if (person2 == null) {
			distance = 0;
		}
		if (person1 == null) {
			distance = 0;
		}
		if (person1 != null && person2 != null) {
			Location.distanceBetween(person1.getGpsLat(), person1.getGpsLng(),
					person2.getGpsLat(), person2.getGpsLng(), distances);
			distance = (int) distances[0];
		}
		return distance;
	}

	public static void cloneObj(UserModel clone, UserModel user) {
		try {
			clone.setUserId(user.getUserId());
			clone.setUserName(user.getUserName());
			clone.setName(user.getName());
			clone.setPassword(user.getPassword());
			clone.setWallStatus(user.getWallStatus());
			clone.setFacebookId(user.getFacebookId());
			clone.setEmail(user.getEmail());
			clone.setUserAvatar(user.getUserAvatar());
			clone.setSex(user.getSex());
			clone.setImageId(user.getImageId());
			clone.setBirthday(user.getBirthday());
			clone.setInterestedPartner(user.getInterestedPartner());
			clone.setPossessPoint(user.getPossessPoint());
			clone.setHeight(user.getHeight());
			clone.setBodyFigure(user.getBodyFigure());

			// clone.setLocation(user.getLocation());//error because it not
			// seri.. obj
			// clone.setMarker(user.getMarker());
			clone.setActived(user.isActived());

			clone.setIsFriend(user.getIsFriend());
			clone.setIsLogin(user.getIsLogin());
			clone.setTimeLastLogin(user.getTimeLastLogin());
			clone.setGpsLat(user.getGpsLat());
			clone.setGpsLng(user.getGpsLng());
			clone.setDistance(user.getDistance());
		} catch (Exception e) {
			e.printStackTrace();
			// throw new Error("Something impossible just happened");
		}
	}

	private static String PREFIX_FILTER = "-123sugutomo321-";

	public static String filterBadWordJapan(String s, Context context) {
		if (Global.listBadWord.size() == 0) {
			try {
				SharedPreferences prefs = context.getSharedPreferences(
						Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
				String restrick = prefs.getString(Params.PREF_BAD_WORD, "");
				if (restrick != null && restrick.length() > 0) {
					JSONArray restrick_word = new JSONArray(restrick);
					for (int i = 0; i < restrick_word.length(); i++) {
						String badWord = restrick_word.getString(i);
						// Log.d("", "restrick:" + badWord);
						String key = badWord.substring(0, 1);
						ArrayList<String> list = Global.listBadWord.get(key);
						if (list == null)
							list = new ArrayList<String>();
						list.add(badWord);
						Global.listBadWord.put(key, list);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		s = PREFIX_FILTER + s + PREFIX_FILTER;// enforce always
												// s.split(badword).length>1
												// when badword in s.
		// example s ="fuck" will result split =1 but still have badword in s
		long t = System.currentTimeMillis();
		Set<String> list = Global.listBadWord.keySet();
		String regex_pattern = "";
		for (String key : list) {
			ArrayList<String> badList = Global.listBadWord.get(key);
			for (String badword : badList) {
				if (s.split("(?i)" + badword).length > 1) {// (?i)xxx se split
															// CASE_INSENSITIVE
															// : eg xXx cung
															// dung
															// Log.v("",
															// "length>1");
					regex_pattern += badword + "|";
				}
			}
		}

		if (regex_pattern.endsWith("|"))
			regex_pattern = regex_pattern.substring(0,
					regex_pattern.length() - 1);
		// Log.v("", "regex_pattern:" + regex_pattern);
		Pattern pat = Pattern.compile(regex_pattern, Pattern.CASE_INSENSITIVE);
		// s=s.replaceAll(regex_pattern, "*");
		Matcher mat = pat.matcher(s);
		StringBuffer buffer = new StringBuffer(s.length());
		while (mat.find()) {
			char[] haha = new char[mat.end() - mat.start()];
			for (int j = 0; j < mat.end() - mat.start(); j++) {
				haha[j] = '*';
			}
			String stars = new String(haha);
			mat.appendReplacement(buffer, stars);
		}
		s = mat.appendTail(buffer).toString();
		s = s.replaceAll(PREFIX_FILTER, " ").trim();
		// Log.v("", ""+s);
		// Log.v("", "total time filter " + (System.currentTimeMillis() - t));
		return s;
	}

	public static void savePref(Editor editor, String key,
			ArrayList<Integer> value) {
		String str = TextUtils.join(",", value);
		editor.putString(key + Global.userInfo.getUserId(), str);
		editor.commit();
		// Log.v(key, str);
	}

	public static String filterBadWord(String s) {
		String[] split = s.split("\\s+");
		int size = split.length;
		int[] isFilter = new int[size];

		long t = System.currentTimeMillis();
		ArrayList<String> listBadWord = Global.listBadWord.get(s
				.substring(0, 1));
		if (listBadWord == null)
			return s;
		for (String swearWord : listBadWord) {

			// Log.e("", "time Pattern: "+(System.currentTimeMillis() -t1 ));
			String replace = "";
			for (int i = 0; i < size; i++) {
				int length = split[i].length();
				if (isFilter[i] == 1 || length != swearWord.length())
					continue;

				replace = "";
				for (int j = 0; j < length; j++) {
					replace += "*";
				}
				String newStr = split[i].replaceAll("(?i)\\b[^\\w -]*"
						+ swearWord + "[^\\w -]*\\b", replace);
				if (!split[i].equalsIgnoreCase(newStr)) {
					isFilter[i] = 1;// 1 = filter roi
					split[i] = newStr;
				}
				// Pattern pat = Pattern.compile(swearWord,
				// Pattern.CASE_INSENSITIVE);
				// Matcher mat = pat.matcher(split[i]);
				// while (mat.find()) {
				// char[] haha = new char[mat.end() - mat.start()];
				// for (int j = 0; j < mat.end() - mat.start(); j++) {
				// haha[j] = '*';
				// }
				// stars = new String(haha);
				// split[i] = mat.replaceFirst(stars);
				// isFilter[i]=1;//1 = filter roi
				//
				// }
			}

		}
		String filter = "";
		for (int i = 0; i < split.length; i++) {
			filter += split[i] + " ";
		}

		Log.e("", "time: " + (System.currentTimeMillis() - t));

		return filter;
		// return s.replaceAll(censorWords(), "*");
		// long t = System.currentTimeMillis();
		//
		// String replace ="";
		//
		// for (String w : Global.listBadWord) {
		// replace="";
		// for (int j = 0; j < w.length(); j++) {
		// replace += "*";
		// }
		// s = s.replaceAll("(?i)\\b[^\\w -]*" + w + "[^\\w -]*\\b", replace);
		// }
		// Log.e("", "time: "+(System.currentTimeMillis() -t ));
		// return s;
	}

	public static void updateLocation(Activity ac) {
		// TODO
		if (Global.mLocationClient == null)
			return;
		if (Global.mLocationClient.isConnected()) {
			Location mCurrentLocation = Global.mLocationClient
					.getLastLocation();
			if (mCurrentLocation != null && Global.userInfo != null) {
				SharedPreferences pref = ac.getSharedPreferences(
						Global.PREFERENCE_NAME, 0);
				int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
						Params.SETTING_DEFAULT_LOCATION);
				double[] gps = Global.getLocation(
						mCurrentLocation.getLongitude(),
						mCurrentLocation.getLatitude(), shiftMet);

				RequestParams params = new RequestParams();

				SessionManager session = SessionManager.getInstance(ac);
				params.put(Params.PARAM_USER_ID_, Global.userInfo.getUserId());
				params.put(Params.PARAM_TOKEN_, session.getToken());
				params.put(Params.PARAM_GPS_LAT, gps[1]);
				params.put(Params.PARAM_GPS_LNG, gps[0]);
				// ko truyen distance de ko lay list user ve
				// params.put(Params.PARAM_DISTANCE_LIMIT,
				// Params.LIMIT_DISTANCE);
				params.put(Params.PARAM_GPS_IS_ON, 1);
				params.put(Params.PARAMS_UPDATE_GPS, 1);

				MapAPI mMapAPI = new MapAPI(ac);
				mMapAPI.setmParams(params);
				mMapAPI.onRunButtonPressed();
				Global.userInfo.setLocation(mCurrentLocation);
			}
		} else {
			Global.mLocationClient.connect();
		}
	}

	public static boolean checkInternet(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnected();
		return isConnected;
	}

	public static String getSumFootPrintMessage() {
		String s = "";
		if (Global.newFootPrint >= 100)
			s = "99+";
		else
			s = "" + Global.newFootPrint;

		return s;
	}

	public static AlertDialog customDialog(Activity ac, String title,
			View.OnClickListener onclickOk) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ac);
		View view = LayoutInflater.from(ac).inflate(
				R.layout.custom_dialog_pass_event, null);
		((TextView) view.findViewById(R.id.custom_dialog_title)).setText(title);
		Button btn = ((Button) view.findViewById(R.id.custom_dialog_ok));
		if (onclickOk == null) {
			btn.setVisibility(View.GONE);
		} else {
			btn.setOnClickListener(onclickOk);
		}
		builder.setView(view);
		final AlertDialog dialogCustom = builder.create();
		dialogCustom.show();

		Button btnCancel = ((Button) view
				.findViewById(R.id.custom_dialog_cancel));
		if (onclickOk == null) {
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.CENTER_HORIZONTAL);
			param.addRule(RelativeLayout.BELOW, R.id.custom_dialog_title);
			Resources resources = ac.getResources();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			int px = (int) (10 * (metrics.densityDpi / 160f));
			param.setMargins(px, 0, px, px);
			btnCancel.setLayoutParams(param);
			btnCancel.setText(ac.getString(R.string.btn_close));
			btnCancel.setTextColor(Color.WHITE);
			if (Build.VERSION.SDK_INT >= 16) {

				btnCancel.setBackground(ac.getResources().getDrawable(
						R.drawable.button_press));

			} else {
				btnCancel.setBackgroundDrawable(ac.getResources().getDrawable(
						R.drawable.button_press));
			}
			// btnCancel.setBackground(ac.getResources().getDrawable(R.drawable.button_press));
		}
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogCustom.dismiss();
			}
		});

		return dialogCustom;
	}

	public static void backToLogin(Activity mActivity) {
		// After logout redirect user to Logging Activity
		Intent i = new Intent(mActivity, SplashScreen.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		//
		// // Add new Flag to start new Activity
		// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// disconnect service
		Intent intent = new Intent(mActivity.getPackageName()
				+ Params.STOP_SERVICE);
		// Log.d(TAG, "send stop service broad cast");
		mActivity.sendBroadcast(intent);

		// logout session
		SessionManager.getInstance(mActivity).logoutUser();

		// Staring Login Activity
		mActivity.startActivity(i);
		mActivity.overridePendingTransition(R.anim.diagslide_enter,
				R.anim.diagslide_leave);
		mActivity.finish();

	}

	public static void registerInBackground(final Activity activity) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String regId = "";
				try {
					GoogleCloudMessaging gcmObj = GoogleCloudMessaging
							.getInstance(activity.getApplicationContext());

					regId = gcmObj.register(GOOGLE_PROJ_ID);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return regId;
			}

			@Override
			protected void onPostExecute(String regId) {
				if (!TextUtils.isEmpty(regId)) {
					SharedPreferences prefs = activity.getSharedPreferences(
							Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(Params.GOOGLE_REGISTER_GCM, regId);
					int appVersion = getAppVersion(activity);
					editor.putInt(Params.PROPERTY_APP_VERSION, appVersion);
					editor.commit();
					storeRegIdinServer(regId, activity);
					Toast.makeText(
							activity,
							"Registered with GCM Server successfully.\n\n"
									+ regId, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							activity,
							"Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time.",
							Toast.LENGTH_LONG).show();
				}
			}
		}.execute(null, null, null);
	}

	public static void storeRegIdinServer(String regId, Activity activity) {
		RequestParams userRequest = new RequestParams();
		userRequest.put(Params.PARAM_USER_ID_,
				SessionManager.getInstance(activity).getUserId());
		userRequest.put(Params.PARAM_TOKEN_,
				SessionManager.getInstance(activity).getToken());
		userRequest.put(Params.PARAM_PLATFORM, Params.PARAM_ANDROID);
		userRequest.put("devicetoken", regId);

		// if(useProgressDialog) Global.newFootPrint=0;
		SaveGCMRegisterAPI api = new SaveGCMRegisterAPI(activity);
		api.setUseProgresBar(false);
		api.setParams(userRequest);
		api.onRunButtonPressed();

	}

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean checkPlayServices(Activity activity) {
		int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(
						activity,
						"This device doesn't support Play services, App will not work normally",
						Toast.LENGTH_LONG).show();
			}
			return false;
		} else {
			Toast.makeText(
					activity,
					"This device supports Play services, App will work normally",
					Toast.LENGTH_LONG).show();
		}
		return true;
	}

	public static void getUnReadMessageTotal(Context context) {
		UnReadAPI api = new UnReadAPI(context);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, SessionManager.getInstance(context)
				.getUserId());
		request.put(Params.PARAM_TOKEN_, SessionManager.getInstance(context)
				.getToken());
		api.setParams(request);
		api.setUseProgresBar(false);
		api.onRunButtonPressed();
	}

	public static void buildNotification(MessageModel message,
			boolean showDialog, Context context) {
		try {
			SharedPreferences pref = context.getSharedPreferences(
					Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
			if (Global.userInfo == null) {
				// try get from pref
				try {

					String myString = pref.getString(Params.PREF_USER_INFO, "");
					UserModel user = Global.setUserInfo(
							new JSONObject(myString), null);
					if (user != null) {
						Global.userInfo = user;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int user_id = message.getUserInfo().getUserId();
			String name = message.getUserInfo().getName();
			String msg = message.getMessage();
			msg = Global.filterBadWordJapan(msg, context);

			boolean isShowNotificationMenuBar = true;
			boolean isShowBigView = true;
			if (pref.getInt(Params.SETTING_NOTIFYCATION_MESSENGER,
					Params.SETTING_NOTIFYCATION_MESSENGER_ALL) != Params.SETTING_NOTIFYCATION_MESSENGER_ALL) {
				if (Global.listFavoriteId.size() == 0) {// case android os kill
														// static value
					String blocks = pref.getString(Params.PREF_LIST_FAVORITE_ID
							+ Global.userInfo.getUserId(), "NULL");
					if (blocks != null && !blocks.equalsIgnoreCase("NULL")) {
						String[] lst = blocks.split(",");
						for (int i = 0; i < lst.length; i++) {
							int id = Integer.valueOf(lst[i]);
							Global.listFavoriteId.add(id);
						}
					}

				}
				if (!Global.listFavoriteId.contains(Integer.valueOf(user_id))) {
					isShowNotificationMenuBar = false;// setting ko nhan
														// notification tu nguoi
														// ko nam trong favorite
														// list
					// nhung van nhan duoc mess nguoi do o trong chatlist
					isShowBigView = false;
				} else {
					isShowNotificationMenuBar = true;
					boolean value = pref
							.getBoolean(
									Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW,
									true);
					if (!value) {
						// Log.v("", "user disable show mess bigview");
						isShowBigView = false;
					} else {
						isShowBigView = true;
					}
				}
			} else {
				isShowNotificationMenuBar = true;
				boolean value = pref.getBoolean(
						Params.SETTING_BIGVIEW_MESSENGER_SHOW_BIGVIEW, true);
				if (!value) {
					// Log.v("", "user disable show mess bigview");
					isShowBigView = false;
				} else {
					isShowBigView = true;
				}
			}
			String type = message.getType();
			if (type.equals(ChatType.FROM_NOTIFICATION)) {// admin send
				boolean value = pref.getBoolean(
						Params.SETTING_NOTIFYCATION_SMS_SUPPORT, true);
				if (!value) {
					// Log.v("", "user disable admin sms notificaion");
					isShowNotificationMenuBar = false;
				} else {

				}
				// Log.v("", "user enable admin sms notice");
				value = pref.getBoolean(Params.SETTING_BIGVIEW_SMS_SUPPORT,
						true);
				if (!value) {
					// Log.v("", "user disable admin sms bigview");
					isShowBigView = false;
				} else {

				}
				// Log.v("", "user enable admin sms bigview");
			}

			// if(j_user!=null){

			UserModel userModel = message.getUserInfo();
			if (isShowNotificationMenuBar) {
				NotificationCompat.Builder mBuilder= MyApplication.getInstance().getNotificationCompatBuilder();
				mBuilder.setSmallIcon(R.drawable.icon)
						.setContentTitle(name).setContentText(msg).setOnlyAlertOnce(true);

				Intent activityA = new Intent(context, UserListActivity.class);
				Intent resultIntent = null;

				resultIntent = new Intent(context, ChatDetailActivity.class);
				resultIntent.putExtra(Params.USER, (UserModel) userModel);
				TaskStackBuilder stackBuilder = TaskStackBuilder
						.create(context);
				UserModel myInfo = null;
				if (Global.userInfo == null) {
					// try get from pref
					try {
						String myString = pref.getString(Params.PREF_USER_INFO,
								"");
						UserModel user = Global.setUserInfo(new JSONObject(
								myString), null);
						if (user != null) {
							Global.userInfo = user;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (Global.userInfo != null) {// app in background
					myInfo = new UserModel();
					Global.cloneObj(myInfo, Global.userInfo);
					resultIntent.putExtra(Params.PARAM_STRAT_FROM_NOTIFICATION,
							myInfo);
					stackBuilder.addParentStack(UserListActivity.class);
					stackBuilder.addNextIntent(activityA);
					stackBuilder.addNextIntent(resultIntent);
				} else {// app got kill by ....
					stackBuilder.addParentStack(SplashScreenActivity.class);
					activityA = new Intent(context, SplashScreenActivity.class);
					stackBuilder.addNextIntent(activityA);
				}
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent((int) System.currentTimeMillis(),
								PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
				Notification notfi = mBuilder.build();
				notfi.flags |= Notification.FLAG_AUTO_CANCEL;
				SharedPreferences prefs = context.getSharedPreferences(
						Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
				long time = System.currentTimeMillis();
				long diffTime = time - prefs.getLong(Params.NOTIFI_TIME, 0);
				if (prefs.getBoolean(Params.SETTING_VIBE, true)
						&& diffTime > 1000)
					notfi.defaults |= Notification.DEFAULT_VIBRATE;
				if (prefs.getBoolean(Params.SETTING_SOUND, true)
						&& diffTime > 1000) {
					notfi.defaults |= Notification.DEFAULT_SOUND;
				}
				prefs.edit().putLong(Params.NOTIFI_TIME, time);
				prefs.edit().commit();
				notfi.defaults |= Notification.DEFAULT_LIGHTS;

				// Integer count = countMess.get(user_id);
				// if (count == null) {
				// countMess.put(user_id, Integer.valueOf(1));
				// notfi.number = 1;
				// } else {
				// Integer num = Integer.valueOf(count + 1);
				// notfi.number = num.intValue();
				// countMess.put(user_id, num);
				// }
				// mId allows you to update the notification later on.
				NotificationManager mNotificationManager = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(user_id, notfi);
			}
			// show message dialog
			if (showDialog && isShowBigView)// && settingShowBigView
				showMessageDialogActivity(type, userModel, msg, user_id,
						context);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void showMessageDialogActivity(String type,
			UserModel userModel, String msg, int user_id, Context context) {
		showNotifyDialog(userModel, msg, user_id, type, context);
		// Start dialog intent
		// }
	}

	private static void showNotifyDialog(UserModel userModel, String msg,
			int user_id, String type, Context context) {
		if (!Global.IS_OPEN_DIALOG_NOTIFICATION) {
			Intent dialogIntent = new Intent(context, MessageDialog.class);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			dialogIntent.putExtra(Params.USER, (UserModel) userModel);
			dialogIntent.putExtra(Params.PARAM_MESSENGER, msg);
			dialogIntent.putExtra(Params.PARAM_NOTIFY_TYPE, type);
			dialogIntent.putExtra(Params.NOTIFY_ID, user_id);
			dialogIntent.putExtra(Params.NOTIFY_STATE_APP,
					Global.appInBackGround);
			context.startActivity(dialogIntent);
			Global.IS_OPEN_DIALOG_NOTIFICATION = true;
			Global.DIALOG_NOTIFICATION_ID = user_id;
			Log.v("", "IS_OPEN_DIALOG_NOTIFICATION: CREATE NEW"
					+ Global.IS_OPEN_DIALOG_NOTIFICATION);
		} else if (user_id == Global.DIALOG_NOTIFICATION_ID) {
			// update mess
			Log.v("", "IS_OPEN_DIALOG_NOTIFICATION:"
					+ Global.IS_OPEN_DIALOG_NOTIFICATION);
			Intent intent = new Intent(MessageDialog.UPDATE_MESSENGER);
			intent.putExtra(Params.PARAM_MESSENGER, msg);
			intent.putExtra(Params.PARAM_NOTIFY_TYPE, type);
			LocalBroadcastManager broadcaster = LocalBroadcastManager
					.getInstance(context);
			broadcaster.sendBroadcast(intent);
		}
	}
	public static String convertType(String type) {
		String val = "";
		switch (type) {
		case ChatType.TEXT:
			val = ChatType.TYPE_TEXT;
			break;
		case ChatType.IMAGE:
			val = ChatType.TYPE_IMAGE;
			break;
		case ChatType.STAMP:
			val = ChatType.TYPE_STAMP;
			break;
		case ChatType.POINT:
			val = ChatType.TYPE_POINT;
			break;
		case ChatType.BLOCK_USER:
			val = ChatType.TYPE_BLOCK;
			break;
		case ChatType.UNBLOCK_USER:
			val = ChatType.TYPE_UNBLOCK;
			break;
		case ChatType.SUPPORT:
			val = ChatType.TYPE_SUPPORT;
			break;
		case ChatType.BROADCAST:
			val = ChatType.TYPE_BROADCAST;
			break;
		default:
			val = type;
			break;
		}
		return val;
	}

}
