package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.SettingNotifyActivity;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.model.SettingModelItem;
import com.cls.sugutomo.utils.Global;

public class SettingNotifyAdapter extends ArrayAdapter<SettingModelItem> {
	Context context;
	int layoutResourceId;
	private SharedPreferences prefs ;
	Vector<SettingModelItem> data = new Vector<SettingModelItem>();

	public SettingNotifyAdapter(Context context, int layoutResourceId,
			Vector<SettingModelItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		prefs = context.getSharedPreferences(
				Global.PREFERENCE_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		SettingNotifyHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new SettingNotifyHolder();
			holder.txtName = (TextView) row
					.findViewById(R.id.setting_name);
			holder.txtValue = (TextView) row
					.findViewById(R.id.setting_value);
			holder.chkBox1 = (CheckBox) row
					.findViewById(R.id.setting_chkbox_mid);
			holder.chkBox2 = (CheckBox) row
					.findViewById(R.id.setting_chkbox_right);
			row.setTag(holder);
		} else {
			holder = (SettingNotifyHolder) row.getTag();
		}
		SettingModelItem item = data.get(position);
		holder.txtName.setText(item.getName());
		if(position==0){
			holder.chkBox1.setVisibility(View.VISIBLE);
			holder.chkBox2.setVisibility(View.VISIBLE);
			holder.chkBox2.setText(context.getString(R.string.setting_push_notify_favorite));
			int val =getValueChkBox(position);
			if(val==Params.SETTING_NOTIFYCATION_MESSENGER_ALL){
				holder.chkBox1.setChecked(true);
				holder.chkBox1.setEnabled(false);
				holder.chkBox2.setChecked(false);
				holder.chkBox2.setEnabled(true);
			}else{
				holder.chkBox1.setChecked(false);
				holder.chkBox2.setEnabled(true);
				holder.chkBox2.setChecked(true);
				holder.chkBox2.setEnabled(false);
			}
		}else if(position==(data.size()-1)){
			holder.chkBox1.setVisibility(View.GONE);
			holder.chkBox2.setVisibility(View.GONE);
		}else{
			holder.chkBox1.setVisibility(View.GONE);
			holder.chkBox2.setVisibility(View.VISIBLE);
			holder.chkBox2.setText("");
			int val =getValueChkBox(position);
			if(val>0) holder.chkBox2.setChecked(true);
		}
		holder.chkBox1.setTag(position);
		holder.chkBox2.setTag(position);
		return row;
	}
	private int getValueChkBox(int position){
		int val =0;
		boolean b=false;
		switch (position) {
		case SettingNotifyActivity.MESSENGER_FROM:
			val =prefs.getInt(Params.SETTING_NOTIFYCATION_MESSENGER,Params.SETTING_NOTIFYCATION_MESSENGER_ALL);
			break;
		case SettingNotifyActivity.SETTING_NOTIFYCATION_WHEN_SOMEONE_VIEW_MYPROFILE:
			b =prefs.getBoolean(Params.SETTING_NOTIFYCATION_FOOTPRINT,true);
			break;
		case SettingNotifyActivity.RECEIVER_MESSAGE_SUPPORT_ADMIN:
			b =prefs.getBoolean(Params.SETTING_NOTIFYCATION_SMS_SUPPORT,true);
			break;
		case SettingNotifyActivity.NOTIFY_WHEN_NEW_USER:
			b =prefs.getBoolean(Params.SETTING_NOTIFYCATION_NEW_USER,true);
			break;
		case SettingNotifyActivity.NOTIFY_WHEN__SOMEONE_FAVORITE_ME:
			b =prefs.getBoolean(Params.SETTING_NOTIFYCATION_SUBMIT_TO_FAVORITE,true);
			break;
		default:
			break;
		}
		if(b) val =1;
		return val;
	}
	
 		
	static class SettingNotifyHolder {
		TextView txtName, txtValue;
		CheckBox chkBox1, chkBox2;
		// int cost;
	}
}
