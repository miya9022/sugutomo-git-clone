package com.cls.sugutomo.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.model.CreateProfileItem;

public class CreateProfileAdapter extends ArrayAdapter<CreateProfileItem> {

	private Context mContext;
	private List<CreateProfileItem> createProfileItem;
	private int layoutResID;

	public CreateProfileAdapter(Context context, int resource,
			List<CreateProfileItem> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.layoutResID = resource;
		this.createProfileItem = objects;
	}

	@Override
	public int getCount() {
		return createProfileItem.size();
	}

	@Override
	public CreateProfileItem getItem(int position) {
		return createProfileItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setEditInput(String name,int position){
		createProfileItem.get(position).setEditInput(name);
		notifyDataSetChanged();
	}
	
	public void setSelectInput(String name,int position, int posId){
		createProfileItem.get(position).setEditInput(name);
		createProfileItem.get(position).setPosSelect(posId);
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CreateProfileItemHolder holder;
		View view = convertView;
		if (view == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			holder = new CreateProfileItemHolder();
			view = mInflater.inflate(layoutResID, parent, false);

			holder.title = (TextView) view.findViewById(R.id.tv_title);
			holder.isNeed = (TextView) view.findViewById(R.id.tv_isNeed);
			holder.editInput = (TextView) view.findViewById(R.id.editValue);
			holder.layout = (RelativeLayout) view.findViewById(R.id.layout_only_profile_item);
			
			view.setTag(holder);
		} else {
			holder = (CreateProfileItemHolder) view.getTag();
		}
		CreateProfileItem item = this.createProfileItem.get(position);
		if (item.isNeed()) {
			holder.isNeed.setVisibility(View.VISIBLE);
		} else {
			holder.isNeed.setVisibility(View.GONE);
		}
		holder.title.setText(item.getTitle());
		holder.editInput.setText(item.getEditInput());
		if(item.isHeader()){
			holder.title.setTextSize(14);
			holder.title.setTextColor(mContext.getResources().getColor(R.color.text_edit));
			holder.layout.setBackgroundColor(mContext.getResources().getColor(R.color.counter_text_color));
		}
		return view;
	}

	private static class CreateProfileItemHolder {
		TextView title;
		TextView isNeed;
		TextView editInput;
		RelativeLayout layout;
	}
}
