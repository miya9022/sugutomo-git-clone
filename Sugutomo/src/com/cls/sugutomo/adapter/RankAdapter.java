package com.cls.sugutomo.adapter;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.model.MessagesModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;

public class RankAdapter extends ArrayAdapter<UserModel> {
	LayoutInflater mInflater;
	private Context mContext;
	private int layoutResID;
	private ArrayList<UserModel> listChats;
	private ImageFetcher imageFetcher;
	public RankAdapter(Context c, int layoutResourceId,
			ArrayList<UserModel> listUser) {
		super(c, layoutResourceId, listUser);
		mContext = c;
		layoutResID = layoutResourceId;
		mInflater = LayoutInflater.from(mContext);
		this.listChats = listUser;
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		//imageFetcher.setImageFadeIn(true);
		imageFetcher.setLoadingImage(R.drawable.loader);
	}

	public int getCount() {
		return listChats.size();
	}

	public UserModel getItem(int position) {
		return listChats.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		RankListHolder holder = null;
		View row = convertView;

		if (row == null) {

			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResID, parent, false);
			holder = new RankListHolder();

			holder.avatar = (CircularImageView) row
					.findViewById(R.id.chat_list_avatar);
			holder.rankIndex = (TextView) row
					.findViewById(R.id.rank_index_txt);
			holder.rankImg = (ImageView) row
					.findViewById(R.id.rank_index_img);
			holder.name = (TextView) row.findViewById(R.id.chat_list_name);
			holder.distance = (TextView) row
					.findViewById(R.id.rank_list_distance);
			holder.lastTimeLogin = (TextView) row
					.findViewById(R.id.rank_lastTimeLogin);
			holder.message = (TextView) row
					.findViewById(R.id.rank_list_message);

			row.setTag(holder);
		} else {
			holder = (RankListHolder) row.getTag();
		}

		UserModel user = listChats.get(position);

		// set avatar
//		Bitmap bitmap = LoadSaveImage.getThumbImage(user
//				.getUserAvatar(), Params.PARAM_USER_AVATAR+File.separator+user.getUserId(), user
//				.getUserAvatar().hashCode()+"");
//		if (bitmap != null)
//			holder.avatar.setImageBitmap(bitmap);
		imageFetcher.loadImage(user.getUserAvatar(), holder.avatar);

		if(position>2){
			holder.rankIndex.setVisibility(View.VISIBLE);
			holder.rankIndex.setText(""+(position+1)+mContext.getString(R.string.rank_number));
			holder.rankImg.setVisibility(View.GONE);
		}else{
			holder.rankIndex.setVisibility(View.GONE);
			holder.rankImg.setVisibility(View.VISIBLE);
			holder.rankImg.setImageResource(mContext.getResources().
			         getIdentifier("drawable/rank"+(position+1), null, mContext.getPackageName()));
		}
		// set name
		holder.name.setText(user.getName());

		// set distance
		holder.distance.setText(Global.distanceBetweenGPS(
				Global.userInfo, user,mContext));

		// set last chat time
		holder.lastTimeLogin.setText(Global.calculateTime(mContext,
				user.getTimeLastLogin())
				+ mContext.getString(R.string.before));

		// last status
		
			holder.message.setText(user.getWallStatus());
		


		return row;
	}

	static class RankListHolder {
		TextView name;
		TextView message;
		TextView rankIndex;
		TextView distance;
		TextView lastTimeLogin;
		CircularImageView avatar;
		ImageView rankImg;
	}
}