package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.SettingActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.model.SettingModelItem;
import com.cls.sugutomo.utils.Global;

public class SettingAdapter extends ArrayAdapter<SettingModelItem> {
	Context context;
	int layoutResourceId;
	private SharedPreferences prefs;
	Vector<SettingModelItem> data = new Vector<SettingModelItem>();

	public SettingAdapter(Context context, int layoutResourceId,
			Vector<SettingModelItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		prefs = context.getSharedPreferences(Global.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		SettingHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new SettingHolder();
			holder.txtName = (TextView) row.findViewById(R.id.setting_name);
			holder.txtValue = (TextView) row.findViewById(R.id.setting_value);
			holder.button1 = (Button) row.findViewById(R.id.setting_on);
			holder.button2 = (Button) row.findViewById(R.id.setting_off);
			row.setTag(holder);
		} else {
			holder = (SettingHolder) row.getTag();
		}
		SettingModelItem item = data.get(position);
		holder.txtName.setText(item.getName());
		String str = setSettingText(position);
		if (!str.equals("NULL")) {
			holder.txtValue.setVisibility(View.VISIBLE);
			holder.txtValue.setText(str);
			holder.txtValue.setText(setSettingText(position));
			if(position==1 || position ==7){
				if (Build.VERSION.SDK_INT >= 16) {
					holder.txtValue.setBackground(context.getResources().getDrawable(R.drawable.dropdown_more));
				} else {
					holder.txtValue.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dropdown_more));
				}
//				holder.txtValue.setBackground(background)
			}
			holder.button1.setVisibility(View.GONE);
			holder.button2.setVisibility(View.GONE);
		} else {
			holder.txtValue.setVisibility(View.GONE);
			holder.button1.setVisibility(View.VISIBLE);
			holder.button2.setVisibility(View.VISIBLE);
			boolean value = setSetting(position);
//			if (position == SettingActivity.AUTO_LOGIN) {
//				Log.v("Setting adapter", "value = "+value);
//				changeSettingButton(!value, holder);
//			} else {
				changeSettingButton(value, holder);
//			}

			holder.button1.setTag(position);
			holder.button2.setTag(position);

		}
		return row;
	}

	private void changeSettingButton(boolean value, SettingHolder holder) {
		if (value) {
			holder.button1.setBackgroundResource(R.drawable.button_press);
			holder.button2.setBackgroundResource(R.drawable.button_default_bg);
			holder.button2.setTextColor(Color.BLACK);
			holder.button1.setTextColor(Color.WHITE);
		} else {
			holder.button2.setBackgroundResource(R.drawable.button_press);
			holder.button2.setTextColor(Color.WHITE);
			holder.button1.setBackgroundResource(R.drawable.button_default_bg);
			holder.button1.setTextColor(Color.BLACK);
		}
	}

	private String setSettingText(int position) {
		String str = "";
		switch (position) {
		case SettingActivity.SHOW_MY_LOCATION:
			int distance =prefs.getInt(Params.SETTING_MY_LOCATION,Params.SETTING_DEFAULT_LOCATION);
			if(distance >= 1000){
				distance=distance/1000;
				str = distance+ "km";
			}else{
				str = distance+ "m";
			}
			break;
		case SettingActivity.MAIL_CHANGE:
			str = Global.userInfo.getEmail();
			if (str == null || str.equalsIgnoreCase("null"))
				str = "";
			break;
		case SettingActivity.PASSWORD_CHANGE:
			prefs.getBoolean(Params.SETTING_VIBE, false);
			break;
		case SettingActivity.NOTIFY_SETTING:
			// android.provider.Settings.
			break;
		case SettingActivity.ACCOUNT_DELETE:

			break;
		default:
			str = "NULL";
			break;
		}
		return str;
	}

	private boolean setSetting(int position) {
		boolean value = false;
		switch (position) {
		case SettingActivity.GPS:
			value = prefs.getBoolean(Params.SETTING_GPS, false);
			break;
		case SettingActivity.SOUND:
			value = prefs.getBoolean(Params.SETTING_SOUND, false);
			break;
		case SettingActivity.VIBE:
			value = prefs.getBoolean(Params.SETTING_VIBE, false);
			break;
		case SettingActivity.AUTO_LOGIN:
			value = prefs.getBoolean(Params.PARAM_REQUIRE_PASSWORD, false);
			break;
		default:
			break;
		}
		return value;

	}

	static class SettingHolder {
		TextView txtName, txtValue;
		Button button1, button2;
		// int cost;
	}
}
