package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.model.BuyPointModelItem;

public class CustomBuyPointGridViewAdapter extends ArrayAdapter<BuyPointModelItem> {
	Context context;
	int layoutResourceId;
	int idCheck;
	Vector<BuyPointModelItem> data = new Vector<BuyPointModelItem>();
	private String tail;

	public CustomBuyPointGridViewAdapter(Context context, int layoutResourceId,
			Vector<BuyPointModelItem> data, String _tail) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		idCheck=-1;
		tail =_tail;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		BuyPointHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new BuyPointHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.item_text_buypoint);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image_buypoint);
			holder.imageItem_red = (ImageView) row.findViewById(R.id.item_image_buypoint_red);
			row.setTag(holder);
		} else {
			holder = (BuyPointHolder) row.getTag();
		}
		BuyPointModelItem item = data.get(position);
		holder.txtTitle.setText(item.getduration() + tail);
		if(idCheck>=0 && position ==idCheck){
			holder.imageItem_red.setVisibility(View.VISIBLE);
		}else{
			holder.imageItem_red.setVisibility(View.GONE);
		}
//		Bitmap bitmap = LoadSaveImage.getImageFromSDCard(
//				Params.PARAM_USER_AVATAR, item.getUserName());
//		if (bitmap == null)
//			holder.imageItem.setBackgroundResource(R.drawable.loader);
//		else
//			holder.imageItem.setImageBitmap(bitmap);
		return row;
	}

	public int getIdCheck() {
		return idCheck;
	}

	public void setIdCheck(int idCheck) {
		this.idCheck = idCheck;
	}

	static class BuyPointHolder {
		TextView txtTitle;
		ImageView imageItem;
		ImageView imageItem_red;
		//int cost;
	}
}
