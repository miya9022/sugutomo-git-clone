package com.cls.sugutomo.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.android.camera.MediaStoreUtils;
import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.AvatarAPI;
import com.cls.sugutomo.api.FileUploadAPI;
import com.cls.sugutomo.api.ProfileMasterDataAPI;
import com.cls.sugutomo.api.RegisterProfileAPI;
import com.cls.sugutomo.api.WallStatusAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.model.CreateProfileItem;
import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.cls.sugutomo.viewpagerindicator.CirclePageIndicator;
import com.cls.sugutomo.viewpagerindicator.CustomFragmentAdapter;
import com.loopj.android.http.RequestParams;

public class EditProfileActivity extends BaseTabActivity implements
		APICallbackInterface {

	private static final String TAG = EditProfileActivity.class.getSimpleName();
	// Camera activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_SELECT_IMAGE_REQUEST_CODE = 200;
	private static final int PIC_CROP_REQUEST_CODE = 300;

	private static int REQUEST_PICTURE = 1;
	private static int REQUEST_CROP_PICTURE = 2;
	// data
	private String mToken;
	private int mUserId;
	private ArrayList<ProfileModel> mListProfileModel = new ArrayList<ProfileModel>();
	private ArrayList<ProfileModel> mListProfileModelClone = new ArrayList<ProfileModel>();
	private String[] mArrayProfileItem;

	private LinearLayout detailProfileContainer;
	private AlertDialog.Builder mAlertDialog;

	private CustomFragmentAdapter mAdapter;
	private TextView txtWallStatus;
	private ProfileMasterDataAPI mProfileData;
	private float scaleDensity;
	private float deviceHeight;

	private RequestParams params;
	private FileUploadAPI mFileUpload;
	private ArrayList<ImageModel> urlsAvatar = new ArrayList<ImageModel>();
	private ViewPager mPager;
	private int currentPostImg = 0;
	private File croppedImageFile;
	private File captureImageFile;
	private File doneImageFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_edit_profile);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View activityView = layoutInflater.inflate(
				R.layout.activity_edit_profile, null, false);
		frameLayout.addView(activityView);
		mArrayProfileItem = getResources().getStringArray(
				R.array.edit_profile_items);
		mFileUpload = new FileUploadAPI(this, this, true);
		captureImageFile = new File(Environment.getExternalStorageDirectory(),
				File.separator + getString(R.string.app_name) + File.separator
						+ "capture.jpg");
		croppedImageFile = new File(Environment.getExternalStorageDirectory(),
				File.separator + getString(R.string.app_name) + File.separator
						+ "crop_img.jpg");
		// new File(
		// Environment.getExternalStorageDirectory(),
		// MediaStoreUtils.EXTRA_OUTPUT_URI_NAME);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String title = bundle.getString(Params.PARAM_TITLE);
			if (!TextUtils.isEmpty(title)) {
				// setTitle(title);
				setTitle(getString(R.string.editprofile_title));
			}
		}
		mUserId = bundle.getInt(Params.PARAM_USER_ID_, -1);
		mToken = bundle.getString(Params.PARAM_TOKEN_);
		// Log.v(TAG, "User info " + mUserId + ":" + mToken);
		params = new RequestParams();
		params.put(Params.PARAM_USER_ID_, mUserId);
		params.put(Params.PARAM_TOKEN_, mToken);

		txtWallStatus = (TextView) findViewById(R.id.edit_profile_wall_status);
		txtWallStatus.setText(Global.userInfo.getWallStatus());
		detailProfileContainer = (LinearLayout) findViewById(R.id.edit_profile_detail_layout);
		// initListView();
		scaleDensity = getResources().getDisplayMetrics().density;
		assignHeighLayout();

		// get profile master data
		// if (mListProfileModel.size() > 0) {// da load truoc do roi
		// handleReceiveData();
		// } else {
		mListProfileModel = new ArrayList<ProfileModel>();
		RequestParams params = new RequestParams();
		params.put(Params.PARAM_USER_ID_, mUserId);
		params.put(Params.PARAM_TOKEN_, mToken);
		mProfileData = new ProfileMasterDataAPI(this, this, mListProfileModel,
				urlsAvatar);
		mProfileData.setParams(params);
		mProfileData.onRunButtonPressed();
		// }

	}

	private int getProfileItemId(ArrayList<ProfileItem> list, int displayOrder) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getDisplayOrder() == displayOrder) {
				return list.get(i).getItemId();
			}
		}
		return -1;
	}

	private boolean checkInput() {
		// if (params.has(Params.USER + "[" + Params.PARAM_NAME + "]")
		// && params.has(Params.USER + "[" + Params.PARAM_SEX + "]")
		// && params.has(Params.USER + "[" + Params.PARAM_BIRTHDAY + "]")) {
		// return true;
		// }
		return true;
	}

	public void onClick(View v) {
		// TODO
		switch (v.getId()) {
		case R.id.edit_profile_add_img:
			selectImageOption();
			break;
		case R.id.btn_create_profile:
			if (mUserId < 0 || mToken == null || mToken == "")
				return;
			else {
				if (checkInput()) {
					RegisterProfileAPI register = new RegisterProfileAPI(this,
							this, true);

					register.setParams(params);
					register.onRunButtonPressed();
				} else {
					ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
							getString(R.string.ERR_INPUT_EMPTY));
				}
			}
			break;
		case R.id.edit_profile_remove_img: {
			if (urlsAvatar.size() <= 0)
				return;
			if (urlsAvatar.get(currentPostImg).isAvatar()) {
				ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
						getString(R.string.error_delete_avatar));
			} else {
				RequestParams param = new RequestParams();
				param.put(Params.PARAM_USER_ID_, mUserId);
				param.put(Params.PARAM_TOKEN_, mToken);
				param.put(Params.PARAM_IMAGE_ID, urlsAvatar.get(currentPostImg)
						.getImageId());
				AvatarAPI api = new AvatarAPI(this, this, AvatarAPI.REMOVE,
						true);
				api.setParams(param);
				api.onRunButtonPressed();
			}
			break;
		}
		case R.id.edit_profile_setAvatar:
			setAvatar();
			break;
		default:
			break;
		}

	}

	private void createDialogEditInput(final int position, final TextView tv,
			String title) {
		mAlertDialog = new AlertDialog.Builder(this);
		mAlertDialog.setTitle(title);
		// user input
		final EditText input = new EditText(this);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		input.setSingleLine();
		mAlertDialog.setView(input);
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		mAlertDialog.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = input.getText().toString().trim();
						if (text.length() > 0) {
							tv.setText(text);
							if (position == 0)
								params.put(Params.USER + "["
										+ Params.PARAM_NAME + "]", text);
							else {
								// save clone
								mListProfileModelClone.get(
										position - mArrayProfileItem.length)
										.setValue(text);
								//
								CreateProfileItem item = new CreateProfileItem(
										mListProfileModel
												.get(position
														- mArrayProfileItem.length)
												.getName(), false, "edit",
										mListProfileModel.get(position
												- mArrayProfileItem.length));
								if (item.getEditInput() != null
										&& item.getEditInput() != "") {
									if (item.getProfileModel().getProfileItem()
											.size() <= 0) {
										params.put(Params.PROFILES
												+ "["
												+ item.getProfileModel()
														.getId() + "]", text);
										Log.v(TAG, Params.PROFILES
												+ "["
												+ item.getProfileModel()
														.getId() + "]=" + text);
									} else {
										int itemId = getProfileItemId(item
												.getProfileModel()
												.getProfileItem(), which);
										params.put(Params.PROFILES
												+ "["
												+ item.getProfileModel()
														.getId() + "]", itemId);
									}
								}
							}
						}
					}
				});
		mAlertDialog.show();
	}

	private void createDialogPickerInput(final int position, int minValue,
			int maxValue, int defValue, final String postfix,
			final TextView tv, String title, final String param) {
		mAlertDialog = new AlertDialog.Builder(this);
		mAlertDialog.setTitle(title);
		// user input
		final NumberPicker numberpicker = new NumberPicker(this);
		numberpicker.setMinValue(minValue);
		numberpicker.setMaxValue(maxValue);
		numberpicker.setWrapSelectorWheel(true);
		numberpicker.setValue(defValue);
		mAlertDialog.setView(numberpicker);

		mAlertDialog.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = numberpicker.getValue() + postfix;
						tv.setText(text);
						if (param.equals(Params.PARAM_HEIGHT)) {
							params.put(Params.USER + "[" + Params.PARAM_HEIGHT
									+ "]", numberpicker.getValue());
						} else if (param.equals(Params.PARAM_BIRTHDAY)) {
							Calendar mCalendar = Calendar.getInstance();
							mYear = mCalendar.get(Calendar.YEAR);
							int birthYear = mYear - numberpicker.getValue();
							params.put(Params.USER + "["
									+ Params.PARAM_BIRTHDAY + "]", birthYear
									+ "-01-01");
						}
					}
				});
		mAlertDialog.show();
	}

	private void createDialogSelectInput(final int position,
			final String[] array, final TextView tv, String title) {
		mAlertDialog = new AlertDialog.Builder(this);
		mAlertDialog.setTitle(title);
		mAlertDialog.setSingleChoiceItems(array, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						tv.setText(array[which]);
						switch (position) {
						case 1:
							params.put(Params.USER + "[" + Params.PARAM_SEX
									+ "]", which);
							break;
						case 3:
							params.put(Params.USER + "["
									+ Params.PARAM_INTERESTED_PARTNER + "]",
									which);
							break;
						case 5: // body figure
							params.put(Params.USER + "["
									+ Params.PARAM_BODY_FIGURE + "]",
									array[which]);
							break;
						default:
							mListProfileModelClone.get(
									position - mArrayProfileItem.length)
									.setValue(array[which]);
							CreateProfileItem item = new CreateProfileItem(
									mListProfileModel
											.get(position
													- mArrayProfileItem.length)
											.getName(), false, "edit",
									mListProfileModel.get(position
											- mArrayProfileItem.length));
							if (item.getEditInput() != null
									&& item.getEditInput() != "") {
								if (item.getProfileModel().getProfileItem()
										.size() <= 0) {
									params.put(Params.PROFILES + "["
											+ item.getProfileModel().getId()
											+ "]", array[which]);
									Log.v(TAG, Params.PROFILES + "["
											+ item.getProfileModel().getId()
											+ "] =" + item.getEditInput());
								} else {
									int itemId = getProfileItemId(
											item.getProfileModel()
													.getProfileItem(), which);
									params.put(Params.PROFILES + "["
											+ item.getProfileModel().getId()
											+ "]", itemId);
									Log.v(TAG, Params.PROFILES + "["
											+ item.getProfileModel().getId()
											+ "] =" + itemId);
								}
							}

							break;
						}

					}
				});
		mAlertDialog.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.show();
	}

	private int mYear;
	private int mMonth;
	private int mDay;
	private Uri picUri;
	private String pathUpload;

	private void createDialogDateInput(final int position, final TextView tv) {
		Calendar mCalendar = Calendar.getInstance();
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH);
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

		final DatePickerDialog dpDialog = new DatePickerDialog(this, null,
				mYear, mMonth, mDay);
		dpDialog.setButton(DatePickerDialog.BUTTON_POSITIVE,
				getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mYear = dpDialog.getDatePicker().getYear();
						mMonth = dpDialog.getDatePicker().getMonth() + 1;
						mDay = dpDialog.getDatePicker().getDayOfMonth();
						String text = mYear + "-" + mMonth + "-" + mDay;
						tv.setText(text);
						params.put(Params.USER + "[" + Params.PARAM_BIRTHDAY
								+ "]", text);
					}
				});
		dpDialog.show();
	}

	private void setAvatar() {
		if (urlsAvatar.size() <= 0)
			return;
		if (urlsAvatar.get(currentPostImg).isAvatar()) {
			ShowMessage.showDialog(this, getString(R.string.avatar),
					getString(R.string.already_is_avatar));
		} else {
			RequestParams param = new RequestParams();
			param.put(Params.PARAM_USER_ID_, mUserId);
			param.put(Params.PARAM_TOKEN_, mToken);
			param.put(Params.PARAM_IMAGE_ID, urlsAvatar.get(currentPostImg)
					.getImageId());
			AvatarAPI api = new AvatarAPI(this, this, AvatarAPI.SET_DEFAULT,
					true);
			api.setParams(param);
			api.onRunButtonPressed();
		}
	}

	private void initViewPagerCircleIndictor() {
		mPager = (ViewPager) findViewById(R.id.profile_pager);
		CustomFragmentAdapter.userAvatarList = urlsAvatar;
		CustomFragmentAdapter.mCount = urlsAvatar.size();
		mAdapter = new CustomFragmentAdapter(getSupportFragmentManager(), false);
		mPager.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		for (int i = 0; i < urlsAvatar.size(); i++) {
			if (urlsAvatar.get(i).isAvatar()) {
				currentPostImg = i;
			}
		}
		mPager.setCurrentItem(currentPostImg);
		CirclePageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setFillColor(Color.RED);
		mIndicator
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						currentPostImg = position;
					}

					@Override
					public void onPageScrolled(int position,
							float positionOffset, int positionOffsetPixels) {
					}

					@Override
					public void onPageScrollStateChanged(int state) {
					}
				});
	}

	public void onChangeWallStatus(View v) {

		createDialogChangeStatus((TextView) v,
				getString(R.string.set_wallstatus));

	}

	private void createDialogChangeStatus(final TextView tv, String title) {
		mAlertDialog = new AlertDialog.Builder(this);
		mAlertDialog.setTitle(title);
		// user input
		final EditText input = new EditText(this);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		input.setSingleLine();
		mAlertDialog.setView(input);
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		mAlertDialog.setNegativeButton(this.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = input.getText().toString().trim();
						tv.setText(text);
						params.put(Params.USER + "[" + Params.PARAM_WALL_STATUS
								+ "]", text);

						WallStatusAPI api = new WallStatusAPI(
								EditProfileActivity.this,
								EditProfileActivity.this, text);
						RequestParams rp = new RequestParams();
						rp.put("status", text);
						rp.put(Params.PARAM_USER_ID_, mUserId);
						rp.put(Params.PARAM_TOKEN_, mToken);
						api.setParams(rp);
						api.onRunButtonPressed();
					}
				});
		mAlertDialog.show();
	}

	public void updateStatusFail() {
		txtWallStatus.setText(Global.userInfo.getWallStatus());
	}

	public void assignHeighLayout() {
		scaleDensity = getResources().getDisplayMetrics().density;
		// deviceWidthInDp =(int)(deviceWidth / scaleDensity);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		deviceHeight = dm.heightPixels - 40;// 40 ~ statusbar+ sugutomo bar tren
											// top
		RelativeLayout avatarContainer = (RelativeLayout) findViewById(R.id.profile_layout_avatar_container);
		// LinearLayout.LayoutParams vi parent cua no la linerlayout
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.3f));
		avatarContainer.setLayoutParams(parms);

		LinearLayout.LayoutParams lo = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.1f));
		lo.leftMargin = (int) (scaleDensity * 10);
		lo.rightMargin = (int) (scaleDensity * 10);
		txtWallStatus.setLayoutParams(lo);

		TextView txt = (TextView) findViewById(R.id.edit_profile_title);
		lo = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.07f));
		lo.leftMargin = (int) (scaleDensity * 10);
		lo.rightMargin = (int) (scaleDensity * 10);
		txt.setLayoutParams(lo);

		RelativeLayout tabContainer = (RelativeLayout) findViewById(R.id.profile_tab_bot);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.1f));
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		tabContainer.setLayoutParams(lp);

	}

	public void addImageModelAfterUpload(ImageModel m) {
		// delete img in capture folder
		File capture = new File(Environment.getExternalStorageDirectory(),
				File.separator + getString(R.string.app_name) + File.separator
						+ "capture");
		if (capture.exists()) {
			for (File file : capture.listFiles())
				file.delete();
		}
		// /////
		// urlsAvatar.add(m);
		urlsAvatar.add(0, m);
		CustomFragmentAdapter.userAvatarList = urlsAvatar;
		CustomFragmentAdapter.mCount = urlsAvatar.size();
		if (mPager != null) {
			mAdapter = (CustomFragmentAdapter) mPager.getAdapter();
			mAdapter.notifyDataSetChanged();
		}
		initViewPagerCircleIndictor();

		// mPager.setCurrentItem(urlsAvatar.size() - 1);
		mPager.setCurrentItem(0);
		// Log.v(TAG, "" + urlsAvatar.size() + ":"
		// + urlsAvatar.get(0).getFullPath());
		if (Global.isUploadingImageInEditProfile) {
			Global.isUploadingImageInEditProfile = false;
			// ShowMessage.showDialog(this, getString(R.string.photo),
			// getString(R.string.upload_photo_complete));
			setAvatar();
		}
	}

	public void updateAvatar(int mRequest) {
		if (mRequest == AvatarAPI.REMOVE) {
			int postRemove = currentPostImg;
			int currentItem = 0;
			if (postRemove == urlsAvatar.size() - 1)
				currentItem = postRemove - 1;
			else
				currentItem = postRemove;
			urlsAvatar.remove(postRemove);
			CustomFragmentAdapter cma = (CustomFragmentAdapter) mPager
					.getAdapter();
			if (cma != null)
				cma.clearAllPage();

			initViewPagerCircleIndictor();
			mPager.setCurrentItem(currentItem);
		} else if (mRequest == AvatarAPI.SET_DEFAULT) {
			for (ImageModel img : urlsAvatar) {
				img.setAvatar(false);
			}
			ImageModel avatar = urlsAvatar.get(currentPostImg);
			avatar.setAvatar(true);
			// set lai avatar va cho no ve page dau` tien
			// ImageModel im = new ImageModel(avatar.getImageId(),
			// avatar.getFullPath(), avatar.getUserId());
			// im.setAvatar(true);
			// urlsAvatar.remove(currentPostImg);
			// urlsAvatar.add(0, im);
			// currentPostImg = 0;
			// reset viewpager
			initViewPagerCircleIndictor();
			// broast cast change avatar

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub
		initViewPagerCircleIndictor();
		detailProfileContainer.removeAllViews();// remove all child da add
		if (mListProfileModel.size() > 0) {
			Collections.sort(mListProfileModel, new Comparator<ProfileModel>() {
				public int compare(ProfileModel s1, ProfileModel s2) {
					return (s1.displayOrder - s2.displayOrder);
				}
			});

			mListProfileModelClone = (ArrayList<ProfileModel>) mListProfileModel
					.clone();

			int sex = Global.userInfo.getSex();
			String gt = sex == 1 ? getString(R.string.broastcast_text_men)
					: getString(R.string.broastcast_text_women);
			String qt = Global.userInfo.getInterestedPartner() == 2 ? getString(R.string.broastcast_text_men)
					: getString(R.string.broastcast_text_women);
			// neu chua set body figure thi set mac dinh = 0
			if (Global.userInfo.getBodyFigure() < 0)
				Global.userInfo.setBodyFigure(0);
			String bodyFigure = getResources().getStringArray(
					R.array.body_figure_items)[Global.userInfo.getBodyFigure()];
			// ten,sex,tuoi,sex quan tam, chieu cao
			// for (int i = 0; i < mArrayProfileItem.length; i++) {
			addRow(mArrayProfileItem[0], Global.userInfo.getName(), true, null,
					0);
			params.put(Params.USER + "[" + Params.PARAM_NAME + "]",
					Global.userInfo.getName());
			Log.v(TAG, Params.USER + "[" + Params.PARAM_NAME + "]="
					+ Global.userInfo.getName());

			addRow(mArrayProfileItem[1], gt, true, null, 1);
			params.put(Params.USER + "[" + Params.PARAM_SEX + "]",
					Global.userInfo.getSex());
			Log.v(TAG, Params.USER + "[" + Params.PARAM_SEX + "]="
					+ Global.userInfo.getSex());

			addRow(mArrayProfileItem[2],
					String.valueOf(Global.userInfo.getAge())
							+ getString(R.string.profile_default_age), true,
					null, 2);
			params.put(Params.USER + "[" + Params.PARAM_BIRTHDAY + "]",
					Global.userInfo.getBirthday());
			Log.v(TAG, Params.USER + "[" + Params.PARAM_BIRTHDAY + "]="
					+ Global.userInfo.getBirthday());
			// addRow(mArrayProfileItem[3], qt, false, null, 3);
			// params.put(Params.USER + "[" + Params.PARAM_INTERESTED_PARTNER
			// + "]", Global.userInfo.getInterestedPartner());
			// Log.v(TAG, Params.USER + "[" + Params.PARAM_INTERESTED_PARTNER
			// + "]=" + Global.userInfo.getInterestedPartner());
			// addRow(mArrayProfileItem[4], Global.userInfo.getHeight() + "cm",
			// false, null, 4);
			// params.put(Params.USER + "[" + Params.PARAM_HEIGHT + "]",
			// Global.userInfo.getHeight());
			// Log.v(TAG, Params.USER + "[" + Params.PARAM_HEIGHT + "]="
			// + Global.userInfo.getHeight());
			// addRow(mArrayProfileItem[5], bodyFigure, false, null, 5);
			params.put(Params.USER + "[" + Params.PARAM_BODY_FIGURE + "]",
					Global.userInfo.getBodyFigure());
			Log.v(TAG, Params.USER + "[" + Params.PARAM_BODY_FIGURE + "]="
					+ Global.userInfo.getBodyFigure());
			// }
			params.put(Params.PARAM_WALL_STATUS, txtWallStatus.getText());
			params.put(Params.PARAM_IMAGE_ID, 1);// main avatar

			detailProfileContainer.setVisibility(View.VISIBLE);
			for (int i = 0; i < mListProfileModel.size(); i++) {
				addRow(mListProfileModel.get(i).getName(), mListProfileModel
						.get(i).getValue(), false, mListProfileModel.get(i),
						6 + i);
				if (mListProfileModel.get(i).valueID != null
						&& mListProfileModel.get(i).valueID.length() > 0) {
					params.put(Params.PROFILES + "["
							+ mListProfileModel.get(i).getId() + "]",
							mListProfileModel.get(i).valueID);
					Log.v(TAG, Params.PROFILES + "["
							+ mListProfileModel.get(i).getId() + "]="
							+ mListProfileModel.get(i).valueID);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleGetList() {
		// TODO update profile success
		// setTitle(Global.userInfo.getName());
		mListProfileModel = (ArrayList<ProfileModel>) mListProfileModelClone
				.clone();
	}

	private void addRow(final String name, String value, boolean isNeed,
			final ProfileModel profileModel, final int position) {
		RelativeLayout childLayOut = new RelativeLayout(this);
		childLayOut.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				(int) (deviceHeight * 0.1f)));
		detailProfileContainer.addView(childLayOut);

		TextView profileTitle = new TextView(this);
		RelativeLayout.LayoutParams lo = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lo.addRule(RelativeLayout.CENTER_VERTICAL);
		lo.leftMargin = (int) (10 * scaleDensity);
		profileTitle.setLayoutParams(lo);
		profileTitle.setTextColor(Color.BLACK);
		profileTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		profileTitle.setText(name);
		profileTitle.setId(1);
		childLayOut.addView(profileTitle);
		if (isNeed) {
			TextView isNeedTv = new TextView(this);
			lo = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lo.addRule(RelativeLayout.CENTER_VERTICAL);
			lo.leftMargin = (int) (10 * scaleDensity);
			lo.addRule(RelativeLayout.RIGHT_OF, profileTitle.getId());
			isNeedTv.setLayoutParams(lo);
			// isNeedTv.setPadding(left, top, right, bottom)
			isNeedTv.setBackgroundColor(getResources().getColor(
					R.color.text_edit));
			isNeedTv.setTextColor(Color.WHITE);
			isNeedTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			isNeedTv.setText(getString(R.string.text_is_need));
			childLayOut.addView(isNeedTv);
		}

		final TextView profileValue = new TextView(this);
		lo = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lo.addRule(RelativeLayout.CENTER_VERTICAL);
		lo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lo.rightMargin = (int) (10 * scaleDensity);
		profileValue.setLayoutParams(lo);
		profileValue.setTextColor(Color.BLACK);
		profileValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		if (value == null || value.length() == 0)
			value = getString(R.string.not_input);
		profileValue.setText(value);
		childLayOut.addView(profileValue);

		View v = new View(this);

		lo = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				(int) (1 * scaleDensity));
		lo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		v.setLayoutParams(lo);
		v.setBackgroundColor(Color.parseColor("#E8E8E8"));
		childLayOut.addView(v);
		if (!isNeed) {
			profileValue.setBackgroundResource(R.drawable.dropdown_more);
			childLayOut.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (position) {
					case 0: // edit name
						createDialogEditInput(position, profileValue, name);
						break;
					case 1:
						createDialogSelectInput(position, getResources()
								.getStringArray(R.array.sexual_items_notboth),
								profileValue, name);
						break;
					case 2:
						// createDialogDateInput(position, profileValue);
						createDialogPickerInput(position, 18, 150, 20, "歳",
								profileValue, name, Params.PARAM_BIRTHDAY);
						break;
					case 3:
						createDialogSelectInput(
								position,
								getResources().getStringArray(
										R.array.interested_partner_items),
								profileValue, name);
						break;

					case 4:// height
						createDialogPickerInput(position, 0, 300, 160, "cm",
								profileValue, name, Params.PARAM_HEIGHT);
						break;
					case 5:
						createDialogSelectInput(position, getResources()
								.getStringArray(R.array.body_figure_items),
								profileValue, name);
						break;
					default:
						ArrayList<ProfileItem> list = profileModel
								.getProfileItem();
						if (list.size() > 0) {
							String[] arrayInput = new String[list.size()];
							for (int i = 0; i < list.size(); i++) {
								arrayInput[i] = list.get(i).getItemName();
							}
							createDialogSelectInput(position, arrayInput,
									profileValue, name);
						} else {
							createDialogEditInput(position, profileValue, name);
						}
						break;
					}
				}
			});
		}
	}

	private void captureImage() {
		try {
			File mediaStorageDir = new File(Environment
					.getExternalStorageDirectory().getAbsolutePath(), ""
					+ getString(R.string.app_name));

			/** Create the storage directory if it does not exist */
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					// return null;
				}
			}
			String NOMEDIA = "capture";
			File nomediaFile = new File(mediaStorageDir.getAbsolutePath(),
					NOMEDIA);
			Log.v("", "save to : " + nomediaFile.getAbsolutePath().toString());
			if (!nomediaFile.exists()) {
				if (!nomediaFile.mkdirs()) {
					// return null;
				}
			}
			String imageFilePath = nomediaFile.getAbsolutePath()
					+ "/picture.jpg";
			File imageFile = new File(imageFilePath);
			Uri uri = Uri.fromFile(captureImageFile); // convert path to Uri
			MyApplication myApplication = (MyApplication) getApplication();
			myApplication.setPictUri(uri);
			// use standard intent to capture an image
			Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// we will handle the returned data in onActivityResult
			captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(captureIntent,
					CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
		} catch (Exception ex) {
			// display an error message
			ex.printStackTrace();
			String errorMessage = getString(R.string.CAPTURE_ERROR);
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void selectImage() {
		try {
			Intent intent2 = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent2, CAMERA_SELECT_IMAGE_REQUEST_CODE);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = getString(R.string.crop_not_support);
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public void selectImageOption() {

		String[] sOptionAddPhoto = getResources().getStringArray(
				R.array.camera_option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.photo));
		builder.setItems(sOptionAddPhoto,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							// captureImage();
							Uri uri = Uri.fromFile(captureImageFile);
							MyApplication myApplication = (MyApplication) getApplication();
							myApplication.setPictUri(uri);
							startActivityForResult(MediaStoreUtils
									.getCameraCaptureIntent(
											EditProfileActivity.this, uri),
									CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
							break;
						case 1:
							// selectImage();
							startActivityForResult(
									MediaStoreUtils
											.getPickImageIntent(EditProfileActivity.this),
									REQUEST_PICTURE);
							break;
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.show();
	}

	private void performCropImage() {
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 300);
			cropIntent.putExtra("outputY", 300);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP_REQUEST_CODE);
		} catch (Exception anfe) {
			// display an error message
			String errorMessage = getString(R.string.crop_not_support);
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {

			if ((requestCode == REQUEST_PICTURE || requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
					&& (resultCode == RESULT_OK)) {
				// When the user is done picking a picture, let's start the
				// CropImage Activity,
				// setting the output image file and size to 300x300 pixels
				// square.
				// Toast.makeText(getApplicationContext(), "begin crop",
				// Toast.LENGTH_SHORT).show();
				Uri croppedImage;
				if (requestCode == REQUEST_PICTURE) {
					croppedImage = Uri.fromFile(croppedImageFile);
					doneImageFile = new File(croppedImageFile.getAbsolutePath());
				} else {
					croppedImage = Uri.fromFile(captureImageFile);
					doneImageFile = new File(captureImageFile.getAbsolutePath());
				}
				CropImageIntentBuilder cropImage = new CropImageIntentBuilder(
						300, 300, croppedImage);
				cropImage.setOutlineColor(0xFF03A9F4);
				if (requestCode == REQUEST_PICTURE)
					cropImage.setSourceImage(data.getData());
				else
					cropImage.setSourceImage(croppedImage);

				startActivityForResult(cropImage.getIntent(this),
						REQUEST_CROP_PICTURE);
			} else if ((requestCode == REQUEST_CROP_PICTURE)
					&& (resultCode == RESULT_OK)) {
				// When we are done cropping, display it in the ImageView.
				// Toast.makeText(getApplicationContext(), "crop success",
				// Toast.LENGTH_SHORT).show();
				// imageView.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
				getCropImage(BitmapFactory.decodeFile(doneImageFile
						.getAbsolutePath()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getCropImage(Bitmap thePic) {
		long timeNowEpoch = System.currentTimeMillis();
		// get the returned data
		// get the cropped bitmap
		if (mUserId > -1)
			pathUpload = LoadSaveImage.saveThumbImage(thePic,
					Params.PARAM_AVATAR + File.separator + mUserId, ""
							+ timeNowEpoch);

		if (pathUpload == null || pathUpload == "") {
			ShowMessage.showMessage(getApplicationContext(),
					getString(R.string.fail));
		} else {
			File avatar = new File(pathUpload);
			if (mToken == null || mUserId < 0) {
				ShowMessage.showDialog(getApplicationContext(),
						getString(R.string.ERR_TITLE),
						getString(R.string.API_ERR_ACCOUNT_DOES_NOT_EXISTED));
				return;
			} else if (!avatar.exists()) {
				ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
						getString(R.string.ERR_FILE_NOT_FOUND));
				return;
			}
			RequestParams params = new RequestParams();
			try {
				Global.isUploadingImageInEditProfile = true;
				params.put(Params.PARAM_AVATAR, avatar);
				params.put(Params.PARAM_USER_ID_, mUserId);
				params.put(Params.PARAM_TOKEN_, mToken);
				mFileUpload.setmParams(params);
				mFileUpload.onRunButtonPressed();
			} catch (FileNotFoundException e) {
				Global.isUploadingImageInEditProfile = false;
				e.printStackTrace();
			}
		}
	}

	private void getCropImage(Intent data) {
		long timeNowEpoch = System.currentTimeMillis();
		// get the returned data
		Bundle extras = data.getExtras();
		if (extras != null) {
			// get the cropped bitmap
			Bitmap thePic = extras.getParcelable("data");
			if (mUserId > -1)
				pathUpload = LoadSaveImage.saveThumbImage(thePic,
						Params.PARAM_AVATAR + File.separator + mUserId, ""
								+ timeNowEpoch);

			if (pathUpload == null || pathUpload == "") {
				ShowMessage.showMessage(getApplicationContext(),
						getString(R.string.fail));
			} else {
				File avatar = new File(pathUpload);
				if (mToken == null || mUserId < 0) {
					ShowMessage
							.showDialog(
									getApplicationContext(),
									getString(R.string.ERR_TITLE),
									getString(R.string.API_ERR_ACCOUNT_DOES_NOT_EXISTED));
					return;
				} else if (!avatar.exists()) {
					ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
							getString(R.string.ERR_FILE_NOT_FOUND));
					return;
				}
				RequestParams params = new RequestParams();
				try {
					Global.isUploadingImageInEditProfile = true;
					params.put(Params.PARAM_AVATAR, avatar);
					params.put(Params.PARAM_USER_ID_, mUserId);
					params.put(Params.PARAM_TOKEN_, mToken);
					mFileUpload.setmParams(params);
					mFileUpload.onRunButtonPressed();
				} catch (FileNotFoundException e) {
					Global.isUploadingImageInEditProfile = false;
					e.printStackTrace();
				}
			}
		}
	}

	static class Data {
		public static int SEX_MEN = 1;
		public static int SEX_WOMAN = 2;
		public static int SEX_BOTH = 0;
		public int sexId = -1;
		public String name;
		public int loginTime = 0;
		// advan filter
		public boolean advaneFilter = false;
		public int age_start, age_end;
	}
}
