package com.cls.sugutomo.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.cls.sugutomo.R;
import com.cls.sugutomo.adapter.CreateProfileAdapter;
import com.cls.sugutomo.api.ProfileMasterDataAPI;
import com.cls.sugutomo.api.RegisterProfileAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.model.CreateProfileItem;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.loopj.android.http.RequestParams;

public class CreateProfileActivity extends Activity implements
		OnItemClickListener {

	private static final String TAG = CreateProfileActivity.class
			.getSimpleName();
	// data
	private String mToken, mName;
	private int mUserId;
	private ArrayList<ProfileModel> mListProfileModel = new ArrayList<ProfileModel>();
	private String[] mArrayProfileItem;
	private ArrayList<CreateProfileItem> mListProfileAdapter;

	private ListView mListViewProfile;
	private CreateProfileAdapter mAdapter;
	private Context mContext;
	private AlertDialog.Builder mAlertDialog;

	private ProfileMasterDataAPI mProfileData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_create_profile);
		mArrayProfileItem = getResources().getStringArray(
				R.array.create_profile_items);

		Bundle bundle = getIntent().getExtras();
		mUserId = bundle.getInt(Params.PARAM_USER_ID_, -1);
		mToken = bundle.getString(Params.PARAM_TOKEN_);
		mName = bundle.getString(Params.PARAM_NAME);

		// get profile master data
		RequestParams params = new RequestParams();
		params.put(Params.PARAM_USER_ID_, mUserId);
		params.put(Params.PARAM_TOKEN_, mToken);
		mProfileData = new ProfileMasterDataAPI(this, mListProfileModel);
		mProfileData.setParams(params);
		mProfileData.onRunButtonPressed();

		mListViewProfile = (ListView) findViewById(R.id.list_profile_items);
		mListViewProfile.setOnItemClickListener(this);
		initListView();

	}

	public void initListView() {
		mListProfileAdapter = new ArrayList<CreateProfileItem>();
		for (int i = 0; i < mArrayProfileItem.length + mListProfileModel.size(); i++) {
			if (i < mArrayProfileItem.length) {
				if (i == 3)
					mListProfileAdapter.add(new CreateProfileItem(
							mArrayProfileItem[i], true));
				else if (i < 3) {
					String name = "";
					if (i == 0)
						name = mName;
					mListProfileAdapter.add(new CreateProfileItem(
							mArrayProfileItem[i], true, name, -1));
				} else
					mListProfileAdapter.add(new CreateProfileItem(
							mArrayProfileItem[i], false, "", -1));
			} else {
				mListProfileAdapter
						.add(new CreateProfileItem(mListProfileModel.get(
								i - mArrayProfileItem.length).getName(), false,
								"", mListProfileModel.get(i
										- mArrayProfileItem.length)));
			}
		}
		mAdapter = new CreateProfileAdapter(this, R.layout.create_profile_item,
				mListProfileAdapter);
		mListViewProfile.setAdapter(mAdapter);
	}

	private boolean checkInput() {
		boolean check = true;
		for (int i = 0; i < 3; i++) {//requice name, sex, age
				String input = mListProfileAdapter.get(i).getEditInput();
				if (input == null || input == "") {
					check = false;
					break;
				}
		}
//		for (int i = 0; i < mArrayProfileItem.length; i++) {
//			if (i != 3) {
//				String input = mListProfileAdapter.get(i).getEditInput();
//				Log.v(TAG, "edit input = " + input);
//				if (input == null || input == "") {
//					check = false;
//					break;
//				}
//			}
//		}
		return check;
	}

	private int getProfileItemId(ArrayList<ProfileItem> list, int displayOrder) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getDisplayOrder() == displayOrder) {
				return list.get(i).getItemId();
			}
		}
		return -1;
	}

	public void onClick(View v) {
		// TODO
		if (v.getId() == R.id.btn_create_profile) {
			if (mUserId < 0 || mToken == null || mToken == "")
				return;
			else {
				if (checkInput()) {
					RegisterProfileAPI register = new RegisterProfileAPI(this);
					RequestParams params = new RequestParams();
					params.put(Params.PARAM_USER_ID_, mUserId);
					params.put(Params.PARAM_TOKEN_, mToken);

					params.put(Params.USER + "[" + Params.PARAM_NAME + "]",
							mListProfileAdapter.get(0).getEditInput());
					params.put(Params.USER + "[" + Params.PARAM_SEX + "]",
							mListProfileAdapter.get(1).getPosSelect());

					Calendar mCalendar = Calendar.getInstance();
					mYear = mCalendar.get(Calendar.YEAR);
					int age = Integer.valueOf(mListProfileAdapter.get(2)
							.getEditInput());
					
					int birthYear = mYear- age;
					params.put(Params.USER + "[" + Params.PARAM_BIRTHDAY + "]",
							birthYear + "-01-01");
					params.put(Params.USER + "["
							+ Params.PARAM_INTERESTED_PARTNER + "]",
							mListProfileAdapter.get(4).getPosSelect());
					params.put(Params.USER + "[" + Params.PARAM_HEIGHT + "]",
							mListProfileAdapter.get(5).getEditInput());
					params.put(Params.USER + "[" + Params.PARAM_BODY_FIGURE
							+ "]", mListProfileAdapter.get(6).getPosSelect());

					for (int i = mArrayProfileItem.length; i < mListProfileAdapter
							.size(); i++) {
						CreateProfileItem item = mListProfileAdapter.get(i);
						if (item.getEditInput() != null
								&& item.getEditInput() != "") {
							if (item.getProfileModel().getProfileItem().size() <= 0)
								params.put(Params.PROFILES + "["
										+ item.getProfileModel().getId() + "]",
										item.getEditInput());
							else {
								int itemId = getProfileItemId(item
										.getProfileModel().getProfileItem(),
										item.getPosSelect());
								params.put(Params.PROFILES + "["
										+ item.getProfileModel().getId() + "]",
										itemId);
							}
						}
					}
					register.setParams(params);
					register.onRunButtonPressed();
				} else {
					ShowMessage.showDialog(mContext,
							getString(R.string.ERR_TITLE),
							getString(R.string.ERR_INPUT_EMPTY));
				}
			}
		}
	}

	private void createDialogEditInput(final int position) {
		mAlertDialog = new AlertDialog.Builder(mContext);
		mAlertDialog.setTitle(mListProfileAdapter.get(position).getTitle());
		// user input
		final EditText input = new EditText(mContext);
		if(position==0) input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });
		else input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		input.setSingleLine();
		mAlertDialog.setView(input);
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		mAlertDialog.setNegativeButton(mContext.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		mAlertDialog.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = input.getText().toString();
						mAdapter.setEditInput(text, position);
					}
				});
		mAlertDialog.show();
	}

	private void createDialogPickerInput(final int position, int minValue,
			int maxValue, int defValue, final String postfix) {
		mAlertDialog = new AlertDialog.Builder(mContext);
		mAlertDialog.setTitle(mListProfileAdapter.get(position).getTitle());
		// user input
		final NumberPicker numberpicker = new NumberPicker(mContext);
		numberpicker.setMinValue(minValue);
		numberpicker.setMaxValue(maxValue);
		numberpicker.setWrapSelectorWheel(true);
		numberpicker.setValue(defValue);
		mAlertDialog.setView(numberpicker);

		mAlertDialog.setNegativeButton(mContext.getString(R.string.btn_cancel),
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
						mAdapter.setEditInput(text, position);
					}
				});
		mAlertDialog.show();
	}

	private void createDialogSelectInput(final int position,
			final String[] array) {
		mAlertDialog = new AlertDialog.Builder(mContext);
		mAlertDialog.setTitle(mListProfileAdapter.get(position).getTitle());
		mAlertDialog.setSingleChoiceItems(array, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						int fixWhich = which;
						if (Arrays.equals(
								array,
								getResources().getStringArray(
										R.array.sexual_items))) {
							fixWhich++;
						}
						mAdapter.setSelectInput(array[which], position,
								fixWhich);
					}
				});
		mAlertDialog.setNegativeButton(mContext.getString(R.string.btn_cancel),
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

	private void createDialogDateInput(final int position) {
		Calendar mCalendar = Calendar.getInstance();
		mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH);
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

		final DatePickerDialog dpDialog = new DatePickerDialog(mContext, null,
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
						mAdapter.setEditInput(text, position);
					}
				});
		dpDialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0: // edit name
			createDialogEditInput(0);
			break;
		case 1:
			createDialogSelectInput(1,
					getResources().getStringArray(R.array.sexual_items));
			break;
		case 2:
			// createDialogDateInput(2);
			createDialogPickerInput(2, 18, 150, 20, "");
			break;
		case 3:
			break;
		case 4:
			createDialogSelectInput(
					4,
					getResources().getStringArray(
							R.array.interested_partner_items));
			break;
		case 5:
			createDialogPickerInput(5, 0, 300, 160, "");
			break;
		case 6:
			createDialogSelectInput(6,
					getResources().getStringArray(R.array.body_figure_items));
			break;
		default:
			ArrayList<ProfileItem> list = mListProfileAdapter.get(position)
					.getProfileModel().getProfileItem();
			if (list.size() > 0) {
				String[] arrayInput = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arrayInput[i] = list.get(i).getItemName();
				}
				createDialogSelectInput(position, arrayInput);
			} else {
				createDialogEditInput(position);
			}
			break;
		}
	}
}
