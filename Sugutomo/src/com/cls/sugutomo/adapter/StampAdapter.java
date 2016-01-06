package com.cls.sugutomo.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.model.StampModel;

public class StampAdapter extends BaseAdapter {
	ArrayList<StampModel> listStamp;
	LayoutInflater mInflater;
	private Context mContext;

	public StampAdapter(Context c, ArrayList<StampModel> listStamp) {
		mContext = c;
		mInflater = LayoutInflater.from(mContext);
		this.listStamp = listStamp;
	}

	public int getCount() {
		return listStamp.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.emo_adapter, null);
		ImageView thumb = (ImageView) convertView.findViewById(R.id.thumb);
//		Bitmap bMap = BitmapFactory.decodeResource(mContext.getResources(), listStamp.get(position).getStampSourceId());
//		thumb.setImageBitmap(bMap);
		thumb.setImageBitmap(listStamp.get(position).getBitmap());
		return convertView;
	}

}