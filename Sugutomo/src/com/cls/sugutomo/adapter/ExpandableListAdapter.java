package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.model.InformationModel;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private Vector<InformationModel> mListInformation;

	public ExpandableListAdapter(Context context, Vector<InformationModel> list) {
		this.mContext = context;
		this.mListInformation = list;
	}

	@Override
	public int getGroupCount() {
		return mListInformation.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mListInformation.get(groupPosition).getTitle();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mListInformation.get(groupPosition).getContent();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.question_layout, null);
		}
		TextView title = (TextView) convertView
				.findViewById(R.id.question_item);
		title.setTypeface(null, Typeface.BOLD);
		title.setText(Html.fromHtml(headerTitle));

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final String childText = (String) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.answer_layout, null);
		}

		TextView txtListChild = (TextView) convertView
				.findViewById(R.id.answer_item);

		txtListChild.setText(Html.fromHtml(childText));
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
