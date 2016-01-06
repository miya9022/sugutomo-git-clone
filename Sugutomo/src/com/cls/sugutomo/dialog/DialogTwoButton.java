package com.cls.sugutomo.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.ProfileMasterDataAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.CreateProfileItem;
import com.cls.sugutomo.model.ProfileItem;
import com.cls.sugutomo.model.ProfileModel;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;

public class DialogTwoButton extends BaseDialog implements APICallbackInterface {

	private LinearLayout userAge;
	private View userbg1, userbg2;
	private TextView tvButonClose, tvButonSubmit, txt1, txt2, txtDistance,
			txtLoginTime;
	private TextView txtAgeStart, txtAgeEnd;
	private LinearLayout filter_layout_scroll;

	private RadioButton sexMan, sexWoman, sexBoth;
	private FilterVaule filter;
	private SessionManager mSession;
	private Activity context;
	private LinearLayout detailProfileContainer;
	private String NOT_SELECT = "";
	private int[] ageStartValue;
	private boolean isLoadAdvanceOpt = false;

	public DialogTwoButton(final Activity context,
			View.OnClickListener callBackCLickSubmit) {
		super(context);
		setContentView(R.layout.dialog_fillter);
		this.context = context;
		NOT_SELECT = context.getString(R.string.not_input);
		detailProfileContainer = (LinearLayout) findViewById(R.id.filter_container);
		mSession = SessionManager.getInstance(context.getApplicationContext());
		filter = new FilterVaule();
		filter_layout_scroll = (LinearLayout) findViewById(R.id.filter_layout_scroll);
		RadioGroup rGroup = (RadioGroup) filter_layout_scroll
				.findViewById(R.id.radioGroup);
		rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				RadioButton checkedRadioButton = (RadioButton) group
						.findViewById(checkedId);
				// This puts the value (true/false) into the variable
				boolean isChecked = checkedRadioButton.isChecked();

				// Check which radio button was clicked
				switch (checkedRadioButton.getId()) {
				case R.id.man:
					if (isChecked)
						filter.sexId = FilterVaule.SEX_MEN;
					break;
				case R.id.woman:
					if (isChecked)
						filter.sexId = FilterVaule.SEX_WOMAN;
					break;
				case R.id.both:
					if (isChecked)
						filter.sexId = FilterVaule.SEX_BOTH;
					// Toast.makeText(context,
					// "both click " , Toast.LENGTH_SHORT)
					// .show();
					break;
				}
			}
		});
		sexMan = (RadioButton) findViewById(R.id.man);
		sexWoman = (RadioButton) findViewById(R.id.woman);
		sexBoth = (RadioButton) findViewById(R.id.both);
		sexBoth.setChecked(true);// default

		txt1 = (TextView) findViewById(R.id.id1);
		txt2 = (TextView) findViewById(R.id.id2);
		txtAgeStart = (TextView) findViewById(R.id.filter_age_start);
		txtAgeEnd = (TextView) findViewById(R.id.filter_age_end);
		userbg1 = findViewById(R.id.id3);
		userbg2 = findViewById(R.id.id4);
		userAge = (LinearLayout) findViewById(R.id.user_age);
		userAge.setVisibility(View.GONE);
		userbg1.setBackgroundColor(Color.RED);
		userbg2.setBackgroundColor(Color.WHITE);

		tvButonClose = (TextView) findViewById(R.id.tvButonClose);
		tvButonSubmit = (TextView) findViewById(R.id.tvButonSubmit);

		ageStartValue = context.getResources().getIntArray(
				R.array.filter_age_value);
		initAgeRange(context);
		final float scaleDensity = context.getResources().getDisplayMetrics().density;
		txt1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				userAge.setVisibility(View.GONE);

				userbg1.setBackgroundColor(Color.RED);
				userbg2.setBackgroundColor(Color.WHITE);
				filter_layout_scroll
						.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								(int) (180 * scaleDensity)));
				//
				int size = detailProfileContainer.getChildCount();
				for (int i = size - 1; i > 3; i--) {
					detailProfileContainer.removeViewAt(i);
				}
				filter.isAdvaneFilter = false;
				isLoadAdvanceOpt = false;

			}
		});
		txt2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				userbg2.setBackgroundColor(Color.RED);
				userbg1.setBackgroundColor(Color.WHITE);
				userAge.setVisibility(View.VISIBLE);
				filter.isAdvaneFilter = true;

				filter.age_start = ageStartValue[0];
				filter.age_end = ageStartValue[ageStartValue.length - 1];
				filter_layout_scroll
						.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								(int) (270 * scaleDensity)));
				if (Global.mListProfileModel.size() <= 0) {
					RequestParams params = new RequestParams();
					params.put(Params.PARAM_USER_ID_, mSession.getUserId());
					params.put(Params.PARAM_TOKEN_, mSession.getToken());
					ProfileMasterDataAPI mProfileData = new ProfileMasterDataAPI(
							DialogTwoButton.this, context,
							Global.mListProfileModel);
					mProfileData.setParams(params);
					mProfileData.onRunButtonPressed();
				} else if (!isLoadAdvanceOpt) {
					handleGetList();
				}

			}
		});
		// handle oncl
		tvButonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogTwoButton.this.dismiss();
			}
		});
		txtDistance = (TextView) findViewById(R.id.filter_distance);
		txtDistance.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDistaneChoice(context);
			}
		});
		filter.distance = 0;// default
		txtDistance.setText(NOT_SELECT);
		txtLoginTime = (TextView) findViewById(R.id.filter_loginbefore);
		txtLoginTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showTimeLoginChoice(context);
			}
		});
		filter.loginTime = 0;
		txtLoginTime.setText(NOT_SELECT);
		tvButonSubmit.setOnClickListener(callBackCLickSubmit);
	}

	private void showDistaneChoice(Context ctx) {

		final String[] ops = ctx.getResources().getStringArray(
				R.array.setting_show_my_location_txt);
		final String[] optionsVal = ctx.getResources().getStringArray(
				R.array.setting_show_my_location_value);

		final String[] options = new String[ops.length + 1];
		options[0] = NOT_SELECT;
		for (int i = 0; i < ops.length; i++) {
			options[i + 1] = ops[i];
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(ctx.getResources().getString(R.string.filter_distance));
		builder.setNegativeButton(ctx.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setPositiveButton(ctx.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int selectedPosition = ((AlertDialog) dialog)
								.getListView().getCheckedItemPosition();
						if (selectedPosition > 0)
							filter.distance = Integer
									.parseInt(optionsVal[selectedPosition - 1]);
						else
							filter.distance = 0;
						txtDistance.setText(options[selectedPosition]);
						dialog.dismiss();
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO
						// if (which > 0)
						// filter.distance = Integer
						// .parseInt(optionsVal[which - 1]);
						// else
						// filter.distance = 0;
						// txtDistance.setText(options[which]);
						// dialog.dismiss();
					}
				});
		builder.show();
	}

	private void showTimeLoginChoice(Context ctx) {

		final String[] ops = ctx.getResources().getStringArray(
				R.array.filter_timelogin_text);
		final String[] optionsVal = ctx.getResources().getStringArray(
				R.array.filter_timelogin_value);
		final String[] options = new String[ops.length + 1];
		options[0] = NOT_SELECT;
		for (int i = 0; i < ops.length; i++) {
			options[i + 1] = ops[i];
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(ctx.getResources().getString(R.string.filter_login));
		builder.setNegativeButton(ctx.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setPositiveButton(ctx.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int which = ((AlertDialog) dialog).getListView()
								.getCheckedItemPosition();
						if (which == 0)
							filter.loginTime = 0;
						else
							filter.loginTime = Integer
									.parseInt(optionsVal[which - 1]) * 60;// second
						txtLoginTime.setText(options[which]);
						dialog.dismiss();
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO
					}
				});
		builder.show();
	}

	private void initAgeRange(Context context) {
		String[] arr1 = context.getResources().getStringArray(
				R.array.filter_age_text);

		final int[] ageEndValue = ageStartValue;
		// Log.e("", "length filter age " + ageStartValue.length);

		filter.age_start = ageStartValue[0];
		filter.age_end = ageEndValue[ageEndValue.length - 1];
		txtAgeStart.setText(arr1[0]);
		txtAgeEnd.setText(arr1[arr1.length - 1]);
		txtAgeStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createDialogAge(true, txtAgeStart);
			}
		});
		txtAgeEnd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createDialogAge(false, txtAgeEnd);
			}
		});
	}

	public FilterVaule getFiler() {
		return filter;
	}

	public static class FilterVaule {
		public static int SEX_MEN = 1;
		public static int SEX_WOMAN = 2;
		public static int SEX_BOTH = 3;
		public int sexId = -1;
		public int distance = 100;
		public int loginTime = 0;// seconds
		// advan filter
		public boolean isAdvaneFilter = false;
		public int age_start, age_end;
		public Map<String, String> advanceFilter = new HashMap<String, String>();

	}

	@Override
	public void handleReceiveData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleGetList() {
		// TODO Auto-generated method stub
		isLoadAdvanceOpt = true;
		int size = Global.mListProfileModel.size();
		float scaleDensity = context.getResources().getDisplayMetrics().density;
		for (int i = 0; i < size; i++) {
			final ProfileModel pm = Global.mListProfileModel.get(i);
			// if (pm.getProfileItem().size() == 0) {
			// continue;
			// }
			RelativeLayout childLayOut = new RelativeLayout(context);
			childLayOut.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					(int) (60 * scaleDensity)));
			detailProfileContainer.addView(childLayOut);
			TextView profileTitle = new TextView(context);
			RelativeLayout.LayoutParams lo = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lo.addRule(RelativeLayout.CENTER_VERTICAL);
			lo.leftMargin = (int) (10 * scaleDensity);
			profileTitle.setLayoutParams(lo);
			profileTitle.setTextColor(Color.BLACK);
			profileTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			profileTitle.setText(pm.getName());
			childLayOut.addView(profileTitle);
			final TextView profileValue = new TextView(context);
			lo = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			lo.addRule(RelativeLayout.CENTER_VERTICAL);
			lo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lo.rightMargin = (int) (10 * scaleDensity);
			profileValue.setLayoutParams(lo);
			profileValue.setTextColor(Color.BLACK);
			profileValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			// Global.mListProfileModel.get(i).getProfileItem().get(0).getItemName()
			profileValue.setText(context.getString(R.string.not_input));// NOT_SELECT
			profileValue.setBackgroundResource(R.drawable.dropdown_more);
			childLayOut.addView(profileValue);
			// filter.advanceFilter.put(profileTitle.getText().toString(),
			// NOT_SELECT);
			childLayOut.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					createDialogWithItem(pm, profileValue);
				}
			});

			View v = new View(context);

			lo = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					(int) (1 * scaleDensity));
			lo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			v.setLayoutParams(lo);
			v.setBackgroundColor(Color.parseColor("#E8E8E8"));
			childLayOut.addView(v);
		}

	}

	private void createDialogWithItem(final ProfileModel profileModel,
			final TextView tv) {
		if (profileModel.getProfileItem().size() == 0) {
			createDialogEditInput(profileModel.getId(), tv, context
					.getResources().getString(R.string.filter_distance));
			return;
		}
		ArrayList<ProfileItem> item = profileModel.getProfileItem();
		final String[] options = new String[item.size() + 1];
		options[0] = NOT_SELECT;
		for (int i = 0; i < item.size(); i++) {
			options[i + 1] = item.get(i).getItemName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(
				R.string.filter_distance));
		builder.setNegativeButton(context.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int which = ((AlertDialog) dialog).getListView()
								.getCheckedItemPosition();
						tv.setText(options[which]);
						int itemId = getProfileItemId(
								profileModel.getProfileItem(), which);
						filter.advanceFilter.put(Params.PROFILES + "["
								+ profileModel.getId() + "]", "" + itemId);

						dialog.dismiss();
					}
				});
		builder.setSingleChoiceItems(options, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO
						// tv.setText(options[which]);
						// int itemId = getProfileItemId(
						// profileModel.getProfileItem(), which);
						// filter.advanceFilter.put(Params.PROFILES + "["
						// + profileModel.getId() + "]", "" + itemId);
						//
						// dialog.dismiss();
					}
				});
		builder.show();
	}

	private void createDialogAge(final boolean ageStart, final TextView tv) {
		final String[] arr1 = context.getResources().getStringArray(
				R.array.filter_age_text);
		String[] arr2 = arr1;

		final int[] ageEndValue = ageStartValue;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.filter_age));
		builder.setNegativeButton(context.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int which = ((AlertDialog) dialog).getListView()
								.getCheckedItemPosition();
						tv.setText(arr1[which]);
						if (ageStart)
							filter.age_start = ageEndValue[which];
						else
							filter.age_end = ageEndValue[which];
						dialog.dismiss();
					}
				});
		int check =ageStart==true?0:(arr1.length-1);
		builder.setSingleChoiceItems(arr1, check,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO
					}
				});
		builder.show();
	}

	private int getProfileItemId(ArrayList<ProfileItem> list, int displayOrder) {
		int disPlay = displayOrder - 1;// post 0 is item NOT_SELECT
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getDisplayOrder() == disPlay) {
				return list.get(i).getItemId();
			}
		}
		return -1;
	}

	private void createDialogEditInput(final int id, final TextView tv,
			String title) {
		AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(context);
		mAlertDialog.setTitle(title);
		// user input
		final EditText input = new EditText(context);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(40) });
		input.setSingleLine();
		mAlertDialog.setView(input);
		input.setFocusable(true);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		mAlertDialog.setNegativeButton(context.getString(R.string.btn_cancel),
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
							filter.advanceFilter.put(Params.PROFILES + "[" + id
									+ "]", "" + text);
							dialog.dismiss();
						}
					}
				});
		mAlertDialog.show();
	}
}