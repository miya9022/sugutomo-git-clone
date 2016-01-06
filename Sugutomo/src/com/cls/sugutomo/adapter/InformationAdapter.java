package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.model.InformationModel;

public class InformationAdapter extends ArrayAdapter<InformationModel> {

	private Context mContext;
	private int mResource;
	private Vector<InformationModel> mListInformation;
	
	public InformationAdapter(Context context, int resource,
			Vector<InformationModel> objects) {
		super(context, resource, objects);
		this.mContext = context;
		this.mResource = resource;
		this.mListInformation = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		InformationHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(mResource, parent, false);
			holder = new InformationHolder();
			holder.title = (TextView) row.findViewById(R.id.titleText);
			holder.content = (TextView) row.findViewById(R.id.contentText);
			row.setTag(holder);
		} else {
			holder = (InformationHolder) row.getTag();
		}
		InformationModel item = mListInformation.get(position);
		holder.title.setText(Html.fromHtml(item.getTitle()));
		holder.content.setText(Html.fromHtml(item.getContent()));
		
		return row;
	}
	
	static class InformationHolder{
		TextView title;
		TextView content;
	}
}
