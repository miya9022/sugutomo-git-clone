package com.cls.sugutomo.map;

import java.io.File;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidmapsextensions.Circle;
import com.androidmapsextensions.CircleOptions;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMapClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.GoogleMap.OnMyLocationButtonClickListener;
import com.androidmapsextensions.GoogleMap.OnMyLocationChangeListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.api.MapAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatDetailActivity;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.profile.ViewProfileActivity;
import com.cls.sugutomo.router.GMapV2GetRouteDirection;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.loopj.android.http.RequestParams;

public class MapActivity extends BaseTabActivity implements
		OnMarkerClickListener, OnInfoWindowClickListener,
		OnMyLocationButtonClickListener, OnMyLocationChangeListener,
		OnMapClickListener {
	public final static int NOTIFY_AVATAR = 1;
	public final static int ACTIVE_USER = 2;
	private LinearLayout imgMyLocation;
	// Map
	public GoogleMap mGoogleMap;
	GMapV2GetRouteDirection gmap;
	private SupportMapFragment mapFragment;
	Document document;
	private String spinet;
	private String distance;
	private HashMap<String, UserModel> mListUsersLocation = new HashMap<String, UserModel>();
	private List<Marker> mark = new ArrayList<Marker>();
	private Circle mainCircle;
	private String title;
	private float zoom;

	// init UI
	private UserModel activeUser;
	private CameraPosition lastCameraPos = null;
	private static int MIN_LOAD_MAP = 10000;// 10 000 m
	private static int MAX_LOAD_MAP = 100000;// 100 000 m
	// UI Pin
	private TextView chatMapName;
	private TextView chatMapMessage;
	private TextView chatMapDistance;
	private TextView chatMapStatus;
	private CircularImageView chatMapAvatar;
	private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();

	// API request
	private MapAPI mMapAPI;

	private Handler handler = new Handler();
	private Runnable dataUpdater = new Runnable() {
		@Override
		public void run() {
			onDataUpdate();
			handler.postDelayed(this, 1000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(R.layout.activity_map, null,
				false);
		frameLayout.addView(activityView);
		// set title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
		}
		 imgMyLocation = (LinearLayout) findViewById(R.id.imgMyLocation);
		 imgMyLocation.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View v) {
	                getMyLocation();
	                onMyLocationButtonClick();
	            }
		 });
		gmap = new GMapV2GetRouteDirection();
		mMapAPI = new MapAPI(this);
		mMapAPI.setShowProgress(false);
		if (GooglePlayServicesErrorDialogFragment
				.showDialogIfNotAvailable(MapActivity.this)) {
			createMapFragmentIfNeeded();
		}

		// Init UI pin
		chatMapAvatar = (CircularImageView) findViewById(R.id.map_pin_avatar);
		chatMapAvatar.setBorderWidth(10);
		chatMapAvatar.addShadow();
		((ViewGroup) findViewById(R.id.map_pin_bg)).getBackground().setAlpha(
				230);
		((ViewGroup) findViewById(R.id.map_pin)).getBackground().setAlpha(150);
		chatMapDistance = (TextView) findViewById(R.id.map_pin_distance);
		chatMapName = (TextView) findViewById(R.id.map_pin_name);
		chatMapMessage = (TextView) findViewById(R.id.map_pin_wallpost);
		chatMapStatus = (TextView) findViewById(R.id.map_pin_status);

		Global.mapActivity = this;

	}
	private void getMyLocation() {
		try{
        LatLng latLng = new LatLng(Global.mLocationClient.getLastLocation().getLatitude(), Global.mLocationClient.getLastLocation().getLongitude());
       float zoom= mGoogleMap.getCameraPosition().zoom;
       CameraUpdate cameraUpdate ;
       if(zoom<18)
         cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
       else
    	   cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.animateCamera(cameraUpdate);
		}catch(Exception e){
			e.printStackTrace();
		}
    }
 
	private void createMapFragmentIfNeeded() {
		FragmentManager fm = getSupportFragmentManager();
		mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
		if (mapFragment == null) {
			mapFragment = createMapFragment();
			FragmentTransaction tx = fm.beginTransaction();
			tx.add(R.id.map, mapFragment);
			tx.commit();
		}
	}

	protected SupportMapFragment createMapFragment() {
		return SupportMapFragment.newInstance();
	}

	private void setUpMapIfNeeded() {
		if (mGoogleMap == null) {
			mGoogleMap = mapFragment.getExtendedMap();
			if (mGoogleMap != null) {
				setUpMap();
			}
		}
	}

	private void onDataUpdate() {
		if (mGoogleMap != null) {
			Marker m = mGoogleMap.getMarkerShowingInfoWindow();
			if (m != null && !m.isCluster()) {
				m.showInfoWindow();
			}
		}
	}

	private void displayAllMarkerWhenMaxZoom(final List<Marker> markers) {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(
				MapActivity.this);
		// builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle(this.getTitle());
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				MapActivity.this, android.R.layout.select_dialog_item);
		for (Marker m : markers) {
			arrayAdapter.add(m.getTitle());
		}
		builderSingle.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Marker m = markers.get(which);
						onMarkerClick(m);
					}
				});
		builderSingle.show();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		if (marker.isCluster()) {
			final float zoom = mGoogleMap.getCameraPosition().zoom;
			if (zoom == mGoogleMap.getMaxZoomLevel()) {
				List<Marker> markers = marker.getMarkers();
				displayAllMarkerWhenMaxZoom(markers);
			} else {
				List<Marker> markers = marker.getMarkers();
				Builder builder = LatLngBounds.builder();
				for (Marker m : markers) {
					builder.include(m.getPosition());
				}
				LatLngBounds bounds = builder.build();
				if (mGoogleMap != null) {
					mGoogleMap.animateCamera(CameraUpdateFactory
							.newLatLngBounds(bounds, getResources()
									.getDimensionPixelSize(R.dimen.padding)));
				}
			}
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		if (!Global.checkGPSStatus(getApplicationContext())) {
			// Ask the user to enable GPS
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("GPS");
			builder.setMessage(getString(R.string.enable_gps));
			builder.setPositiveButton(getString(R.string.btn_ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Launch settings, allowing user to make a change
							Intent i = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(i);
						}
					});
			builder.setNegativeButton(getString(R.string.btn_cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// No location service, no Activity
						}
					});
			builder.create().show();
		} else
			updateLocation();
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null && !marker.isCluster()
				&& !marker.getSnippet().equals(Global.userInfo.getUserName())) {
			spinet = marker.getSnippet();
			// Log.v("spinet", "spinet " + spinet +
			// "--- mListUsersLocation size"
			// + mListUsersLocation.size());
			UserModel user = mListUsersLocation.get(spinet);
			// name
			if (user == null)
				return false;
			if (user.getName() != null)
				chatMapName.setText(user.getName());
			// status
			if (user.getWallStatus() != null)
				chatMapMessage.setText(user.getWallStatus());
			// isOnline
			// int isLogin = user.getIsLogin();
			// if (isLogin == 0) {
			// chatMapStatus.setText("unknow");
			// if (user.getTimeLastLogin() > 0) {
			// String time = Global.calculateTime(getApplicationContext(),
			// user.getTimeLastLogin());
			// if (time != null)
			// chatMapStatus.setText(time
			// + this.getString(R.string.before));
			// }
			// } else if (isLogin == 1) {
			// chatMapStatus.setText("unknow");
			// chatMapStatus.setTextColor(getResources()
			// .getColor(R.color.blue));
			// }
			chatMapStatus.setText(Global.calculateTime(getApplicationContext(),
					user.getTimeLastLogin()) + this.getString(R.string.before));
			// distance
			DecimalFormat df = new DecimalFormat("0.00");
			// df.setRoundingMode(RoundingMode.UP);
			// chatMapDistance.setText(df.format(Global.distFrom(
			// Global.userInfo.location.getLatitude(),
			// Global.userInfo.location.getLongitude(),
			// marker.getPosition().latitude,
			// marker.getPosition().longitude))
			// + "km");
			if (Global.userInfo != null) {
				Location meLocation = Global.userInfo.getLocation();
				if (meLocation != null) {
					distance = Global.distanceBetweenGPS(Global.userInfo, user,this);
					chatMapDistance.setText(distance);
				}
			}
			user.setDistance(distance);
			// image
			Bitmap bitmap = LoadSaveImage.getImageFromSDCard(
					Params.PARAM_USER_AVATAR + File.separator
							+ user.getUserId(), user.getUserAvatar().hashCode()
							+ "");
			if (bitmap == null) {
				try {
					bitmap = LoadSaveImage.getThumbImage(
							user.getUserAvatar(),
							Params.PARAM_USER_AVATAR + File.separator
									+ user.getUserId(), user.getUserAvatar()
									.hashCode() + "");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (bitmap != null)
				chatMapAvatar.setImageBitmap(bitmap);
			((ViewGroup) findViewById(R.id.map_pin))
					.setVisibility(View.VISIBLE);
		}
		return false;
	}

	public OnCameraChangeListener getCameraChangeListener() {
		return new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {
				// addItemsToMap(mListUsersLocation);
				// Toast.makeText(MapActivity.this, "onCameraChange ",
				// Toast.LENGTH_LONG).show();
				// Log.v("","distances getCameraChangeListener");
				if (zoom > mGoogleMap.getCameraPosition().zoom) {// zoom chi
																	// tiet thi
																	// zoom cang
																	// lon
					zoom = mGoogleMap.getCameraPosition().zoom;
				} else if (zoom == mGoogleMap.getCameraPosition().zoom) {
					// move camera, lech >=1km moi load du lieu moi
					if (mGoogleMap != null) {
						if (lastCameraPos != null) {
							LatLng laiLng = position.target;
							// Location location = mGoogleMap.getMyLocation();
							float[] distances = new float[3];
							Location.distanceBetween(
									lastCameraPos.target.latitude,
									lastCameraPos.target.longitude,
									laiLng.latitude, laiLng.longitude,
									distances);
							if (distances[0] > 1000) {
								lastCameraPos = mGoogleMap.getCameraPosition();
								mMapAPI.setmParams(prepareParams(
										laiLng.latitude, laiLng.longitude));
								mMapAPI.onRunButtonPressed();
							}
						} else {
							lastCameraPos = mGoogleMap.getCameraPosition();
						}
					}
					return;
				} else {
					// zoom in not load
					zoom = mGoogleMap.getCameraPosition().zoom;
					return;
				}
				if (Global.mLocationClient != null) {
					if (Global.mLocationClient.isConnected()
							&& Global.mLocationClient.getLastLocation() != null) {
						// get data in arena
						LatLng laiLng = position.target;
						RequestParams params = prepareParams(laiLng.latitude,
								laiLng.longitude);
						params.remove(Params.PARAMS_UPDATE_GPS);
						mMapAPI.setmParams(params);
						mMapAPI.onRunButtonPressed();
					}
				}
			}
		};
	}

	// Note that the type "Items" will be whatever type of object you're adding
	// markers for so you'll
	// likely want to create a List of whatever type of items you're trying to
	// add to the map and edit this appropriately
	// Your "Item" class will need at least a unique id, latitude and longitude.
	private void addItemsToMap(HashMap<String, UserModel> items) {
		// Log.v("", "addItemsToMap " + items.size());
		ArrayList<UserModel> removeList = new ArrayList<UserModel>();
		HashMap<String, UserModel> addList = new HashMap<String, UserModel>();
		if (mGoogleMap != null) {
			MarkerOptions options = new MarkerOptions();
			// This is the current user-viewable region of the map
			LatLngBounds bounds = this.mGoogleMap.getProjection()
					.getVisibleRegion().latLngBounds;
			// Loop through all the items that are available to be placed on the
			// map
			for (UserModel m : items.values()) {
				// If the item is within the the bounds of the screen
				// if (bounds.contains(new LatLng(m.getGpsLat(),
				// m.getGpsLng()))) {
				// If the item isn't already being displayed

				// if(!visibleMarkers.containsKey(m.getUserId()))
				if (visibleMarkers.get(m.getUserId()) == null) {
					// Add the Marker to the Map and keep track of it with
					// the HashMap
					// getMarkerForItem just returns a MarkerOptions object
					int drawableId = m.getSex()==2?R.drawable.map_pin_girl:R.drawable.map_pin_boy;
					drawableId =m.getUserId()==Global.userInfo.getUserId()?R.drawable.mypin:drawableId;
					Marker mark = this.mGoogleMap.addMarker(options
							.position(new LatLng(m.getGpsLat(), m.getGpsLng()))
							.title(m.getName())
							.snippet(m.getUserName())
							.icon(BitmapDescriptorFactory
									.fromResource(drawableId)));
					if(m.getUserId()==Global.userInfo.getUserId()){
						mark.setClusterGroup(-1);//not cluster to anothor marker
//						CircleOptions circleOptions = new CircleOptions()
//						  .center(new LatLng(m.getGpsLat(), m.getGpsLng()))   //set center
//						  .radius(500)   //set radius in meters
//						  .fillColor(0x40ff0000)  //semi-transparent
//						  .strokeColor(Color.WHITE)
//						  .strokeWidth(5);
//						   mGoogleMap.addCircle(circleOptions);
					}else mark.setClusterGroup(1);
					this.visibleMarkers.put(m.getUserId(), mark);
					mListUsersLocation.put(m.getUserName(), m);
//					Log.v("",
//							"add" + m.getUserId() + "-name --"
//									+ m.getUserName());
					removeList.add(m);
				} else {
					visibleMarkers.get(m.getUserId()).setPosition(
							new LatLng(m.getGpsLat(), m.getGpsLng()));
				}

				// // If the marker is off screen
				// else {
				// // If the course was previously on screen
				// // if(visibleMarkers.containsKey(m.getUserId()))
				// if (visibleMarkers.get(m.getUserId()) != null) {
				// addList.put(m.getUserId() + "", m);
				// // 1. Remove the Marker from the GoogleMap
				// visibleMarkers.get(m.getUserId()).remove();
				//
				// // 2. Remove the reference to the Marker from the
				// // HashMap
				// visibleMarkers.remove(m.getUserId());
				// Log.v("", "remove" + m.getUserId());
				// }
				// }
			}
			// int total = removeList.size();
			// for (int i = 0; i < total; i++) {
			// mListUsersLocation.remove(removeList.get(i).getUserId() + "");
			// }
			// Log.v("", "size after remove " + mListUsersLocation.size());
			// mListUsersLocation.putAll(addList);
			// Log.v("", "size after add " + mListUsersLocation.size());
			// Log.v("", "size visible " + visibleMarkers.size());
		}
	}

	private void setUpMap() {
		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mGoogleMap.setOnInfoWindowClickListener(this);
//		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setOnMarkerClickListener(this);
		//mGoogleMap.setOnMyLocationButtonClickListener(this);
		mGoogleMap.setOnCameraChangeListener(getCameraChangeListener());
		mGoogleMap.setOnMyLocationChangeListener(this);
		mGoogleMap.setOnMapClickListener(this);
		mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			private TextView tv;
			{
				tv = new TextView(MapActivity.this);
				tv.setTextColor(Color.BLACK);
			}
			private Collator collator = Collator.getInstance();
			private Comparator<Marker> comparator = new Comparator<Marker>() {
				public int compare(Marker lhs, Marker rhs) {
					String leftTitle = lhs.getTitle();
					String rightTitle = rhs.getTitle();
					if (leftTitle == null && rightTitle == null) {
						return 0;
					}
					if (leftTitle == null) {
						return 1;
					}
					if (rightTitle == null) {
						return -1;
					}
					return collator.compare(leftTitle, rightTitle);
				}
			};

			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {
				if (marker == null || mGoogleMap == null || tv == null)
					return null;
				if (marker != null && marker.isCluster()) {

					//
					final float zoom = mGoogleMap.getCameraPosition().zoom;
					if (zoom == mGoogleMap.getMaxZoomLevel()) {
						displayAllMarkerWhenMaxZoom(marker.getMarkers());
						return null;
					}
					//
					List<Marker> markers = marker.getMarkers();
					mark.addAll(markers);
					int i = 0;
					String text = "";
					while (i < 3 && markers.size() > 0) {
						Marker m = Collections.min(markers, comparator);
						String title = m.getTitle();
						if (title == null) {
							break;
						}
						text += title + "\n";
						markers.remove(m);
						i++;
					}
					if (text.length() == 0) {
						text = "Markers with mutable data";
					} else if (markers.size() > 0) {
						Log.i("marker size", Integer.toString(markers.size()));
						text += "and " + markers.size() + " more...";
					} else {
						Log.i("Vitri", Integer.toString(i));
						text = text.substring(0, text.length() - 1);
					}
					tv.setText(text);
					return tv;
				} else {
					// tv.setText(marker.getTitle());
					// return tv;
				}
				return null;
			}
		});
		zoom = mGoogleMap.getMaxZoomLevel();
		updateClustering(0, true);
		drawRadius();
		if (Global.mLocationClient != null) {
			if (Global.mLocationClient.isConnected()
					&& Global.mLocationClient.getLastLocation() != null) {
				Location currentLocation = Global.mLocationClient
						.getLastLocation();
				mMapAPI.setmParams(prepareParams(currentLocation.getLatitude(),
						currentLocation.getLongitude()));

				mMapAPI.onRunButtonPressed();
			}
		}
	}

	private RequestParams prepareParams(double lat, double lng) {
		int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
				Params.SETTING_DEFAULT_LOCATION);
		// Log.v("", "old getLatitude "+lat + " old getLongitude"+lng
		// +" shiftMet "+shiftMet);
		double[] gps = Global.getLocation(lng, lat, shiftMet);
		lat = gps[1];
		lng = gps[0];
		RequestParams params = new RequestParams();

		SessionManager session = SessionManager.getInstance(this);
		params.put(Params.PARAM_USER_ID_, Global.userInfo.getUserId());
		params.put(Params.PARAM_TOKEN_, session.getToken());
		params.put(Params.PARAM_GPS_LAT, lat);
		params.put(Params.PARAM_GPS_LNG, lng);
		// params.put(Params.PARAM_DISTANCE_LIMIT, Params.LIMIT_DISTANCE);
		LatLngBounds latLngBounds = this.mGoogleMap.getProjection()
				.getVisibleRegion().latLngBounds;
		double lat1 = latLngBounds.northeast.latitude;
		double lng1 = latLngBounds.northeast.longitude;
		double lat2 = latLngBounds.southwest.latitude;
		double lng2 = latLngBounds.southwest.longitude;
		float[] distances = new float[3];
		Location.distanceBetween(lat1, lng1, lat2, lng2, distances);
		float m = distances[0];// add them 1 km nua
		if (m < MIN_LOAD_MAP)
			m = MIN_LOAD_MAP;// 10km
		if (m > MAX_LOAD_MAP)
			m = MAX_LOAD_MAP;
		// Log.v("", "distances " + m);
		params.put(Params.PARAM_DISTANCE_LIMIT, m / 2);
		params.put(Params.PARAM_GPS_IS_ON, 1);
		params.put(Params.PARAMS_UPDATE_GPS, 1);
		return params;
	}

	public void handleLoadMapSuccess(JSONArray user_list) throws JSONException {
		HashMap<String, UserModel> loadUser = new HashMap<String, UserModel>();
		final MarkerOptions options = new MarkerOptions();
		int size = user_list.length();
		Location curLocation = Global.userInfo.getLocation();
		// Log.d("addme", "name:" + Global.userInfo.getName() + ", username:"
		// + Global.userInfo.getUserName());
		// Removes all markers, overlays, and polylines from the map.
		// mGoogleMap.clear();
		mainCircle.remove();
		// if (size > 0) {
		// //=0 la update location thoi
		// mainCircle.remove();
		// mListUsersLocation.clear();
		// }
		// mGoogleMap.addMarker(options
		// .position(
		// new LatLng(curLocation.getLatitude(), curLocation
		// .getLongitude()))
		// .title(Global.userInfo.getName())
		// .snippet(Global.userInfo.getUserName())
		// .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_pin)));
		// Log.v("", "distances ADD" + size);
		UserModel user = new UserModel();
		Global.cloneObj(user, Global.userInfo);
		if (Global.mLocationClient != null
				&& Global.mLocationClient.isConnected()) {
			curLocation =Global.mLocationClient.getLastLocation();
		user.setLocation(curLocation);
		int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
				Params.SETTING_DEFAULT_LOCATION);
//		double[] gps = Global.getLocationNotShift(curLocation.getLongitude(), curLocation.getLatitude(), shiftMet);
		//curLocation.setLatitude(gps[1]);
		user.setGpsLat(curLocation.getLatitude());
		user.setGpsLng(curLocation.getLongitude());
		}else{
			int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
					Params.SETTING_DEFAULT_LOCATION);
			// Log.v("", "old getLatitude "+lat + " old getLongitude"+lng
			// +" shiftMet "+shiftMet);
			double[] gps = Global.getLocationNotShift(curLocation.getLongitude(), curLocation.getLatitude(), shiftMet);
			curLocation.setLatitude(gps[1]);
			user.setLocation(curLocation);
			user.setGpsLat(gps[1]);
			user.setGpsLng(gps[0]);
			if (Global.mLocationClient != null) Global.mLocationClient.connect();
		}
		 for (int i = 0; i < size; i++) {
		 JSONObject userInfo = user_list.getJSONObject(i);
		 UserModel us = Global.setUserInfo(userInfo, null);
		 // add to HashMap
		 loadUser.put(us.getUserName(), us);
		 }
		 loadUser.put(user.getUserName(), user);
		if (loadUser.size() > 0)
			addItemsToMap(loadUser);
	}

	private void drawRadius() {
		Log.d("checkmap", "draw radius");
		if (Global.mLocationClient != null
				&& Global.mLocationClient.isConnected()) {
			Location location = Global.mLocationClient.getLastLocation();
			if (location != null) {
				// int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
				// Params.SETTING_DEFAULT_LOCATION);
				// double[] gps = Global.getLocation(location.getLongitude(),
				// location.getLatitude(), shiftMet);
				// location.setLatitude(gps[1]);
				// location.setLongitude(gps[0]);
				mainCircle = mGoogleMap.addCircle(new CircleOptions()
						.radius(1000)
						.strokeColor(Color.RED)
						.center(new LatLng(location.getLatitude(), location
								.getLongitude())));
				CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(
						location.getLatitude(), location.getLongitude()));
				Log.d("checkmap", "location:" + location.getLatitude() + ","
						+ location.getLongitude());
				CameraUpdate zoom = CameraUpdateFactory
						.zoomTo(getZoomLevel(mainCircle));
				mGoogleMap.moveCamera(center);
				mGoogleMap.animateCamera(zoom);
				Global.userInfo.setLocation(location);
			}
		}
	}

	@SuppressWarnings("unused")
	private int calculateZoomLevel(int screenWidth) {
		double equatorLength = 40075004; // in meters
		double widthInPixels = screenWidth;
		double metersPerPixel = equatorLength / 256;
		int zoomLevel = 1;
		while ((metersPerPixel * widthInPixels) > 2000) {
			metersPerPixel /= 2;
			++zoomLevel;
		}
		Log.i("ADNAN", "zoom level = " + zoomLevel);
		return zoomLevel;
	}

	public int getZoomLevel(Circle circle) {
		int zoomLevel = 0;
		if (circle != null) {
			double radius = circle.getRadius();
			double scale = radius / 500;
			zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
		}
		return zoomLevel;
	}

	void updateClustering(int clusterSizeIndex, boolean enabled) {
		if (mGoogleMap == null) {
			return;
		}
		ClusteringSettings clusteringSettings = new ClusteringSettings();
		clusteringSettings.addMarkersDynamically(true);
		if (enabled) {
			clusteringSettings.clusterOptionsProvider(new ClusterOP(
					getResources()));
			double clusterSize = ClusteringSettings.DEFAULT_CLUSTER_SIZE;
			clusteringSettings.clusterSize(clusterSize);
		} else {
			clusteringSettings.enabled(false);
		}
		mGoogleMap.setClustering(clusteringSettings);
	}

	private void updateLocation() {
		// TODO
		if (Global.mLocationClient == null)
			return;
		if (Global.mLocationClient.isConnected()) {
			Location mCurrentLocation = Global.mLocationClient
					.getLastLocation();
			if (mCurrentLocation != null && Global.userInfo != null) {
				RequestParams params = prepareParams(
						mCurrentLocation.getLatitude(),
						mCurrentLocation.getLongitude());
				if (mListUsersLocation.size() > 0) {
					// update gps neu da load dc map location user
					// neu chua load duoc map location user thi lay list user
					// location luon
					params.remove(Params.PARAM_DISTANCE_LIMIT);// update
																// location chu
																// ko load list
																// user in map
				}
				mMapAPI.setmParams(params);
				mMapAPI.onRunButtonPressed();
				//
				// int shiftMet = pref.getInt(Params.SETTING_MY_LOCATION,
				// Params.SETTING_DEFAULT_LOCATION);
				// double[] gps = Global.getLocation(
				// mCurrentLocation.getLongitude(),
				// mCurrentLocation.getLatitude(), shiftMet);
				// mCurrentLocation.setLatitude(gps[1]);
				// mCurrentLocation.setLongitude(gps[0]);
				Global.userInfo.setLocation(mCurrentLocation);
			}
		} else {
			Global.mLocationClient.connect();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_pin_chat_btn: {
			UserModel chatUser = mListUsersLocation.get(spinet);
			Intent intent = new Intent(this, ChatDetailActivity.class);
			// send object UserModel
			intent.putExtra(Params.USER, chatUser);
			findViewById(R.id.map_pin).setVisibility(View.GONE);
			startActivity(intent);
			spinet = null;
		}
			break;
		case R.id.map_pin_profile_btn: {
			UserModel chatUser = mListUsersLocation.get(spinet);
			Intent intent = new Intent(this, ViewProfileActivity.class);
			intent.putExtra(Params.USER, chatUser);
			findViewById(R.id.map_pin).setVisibility(View.GONE);
			startActivity(intent);
			spinet = null;
		}
			break;
		case R.id.map_pin_close_btn:
			findViewById(R.id.map_pin).setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		createMapFragmentIfNeeded();// kaka add
		setUpMapIfNeeded();
		onMyLocationButtonClick();
		handler.post(dataUpdater);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(dataUpdater);
	}

	public void activeRoute(View v) {
		GetRouteTask getRouteTask = new GetRouteTask();
		getRouteTask.execute();
	}

	/**
	 * get route from marker to marker
	 * 
	 * @author reddevil_138
	 * 
	 */
	private class GetRouteTask extends AsyncTask<String, Void, String> {

		private ProgressDialog Dialog;
		private String response = "";

		LatLng me = new LatLng(Global.userInfo.getLocation().getLatitude(),
				Global.userInfo.getLocation().getLongitude());
		LatLng you = new LatLng(activeUser.getLocation().getLatitude(),
				activeUser.getLocation().getLongitude());

		@Override
		protected void onPreExecute() {
			Dialog = new ProgressDialog(MapActivity.this);
			Dialog.setMessage(MapActivity.this.getString(R.string.loading));
			Dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			// Get All Route values
			document = gmap.getDocument(me, you,
					GMapV2GetRouteDirection.MODE_DRIVING);
			response = "Success";
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			if (response.equalsIgnoreCase("Success")) {
				ArrayList<LatLng> directionPoint = gmap.getDirection(document);
				PolylineOptions rectLine = new PolylineOptions().width(5)
						.color(new Color().parseColor("#3BB9FF"));

				for (int i = 0; i < directionPoint.size(); i++) {
					rectLine.add(directionPoint.get(i));
				}
				// Adding route on the map
				mGoogleMap.addPolyline(rectLine);
			}
			Dialog.dismiss();
		}
	}

	@Override
	public void onMyLocationChange(Location location) {
		// TODO Auto-generated method stub
//		 Toast.makeText(this, "change location", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void onMapClick(LatLng position) {
		// TODO Auto-generated method stub
	}

}