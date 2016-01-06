package com.cls.sugutomo.adapter;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.NavDrawerItem;
import com.cls.sugutomo.profile.EditProfileActivity;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;

public class NavDrawerListAdapter extends ArrayAdapter<NavDrawerItem> {

	private Context mContext;
	private List<NavDrawerItem> navDrawerItems;
	private int layoutResID;
	private BaseTabActivity callBack = null;
private ImageFetcher imageFetcher;
	public NavDrawerListAdapter(Context context, int resource,
			List<NavDrawerItem> navDrawerItems, BaseTabActivity callBack) {
		super(context, resource, navDrawerItems);
		this.mContext = context;
		this.layoutResID = resource;
		this.navDrawerItems = navDrawerItems;
		this.callBack = callBack;
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		//imageFetcher.setImageFadeIn(true);
		imageFetcher.setLoadingImage(R.drawable.loader);
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public NavDrawerItem getItem(int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItemHolder drawerHolder;
		View view = convertView;

		if (view == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			drawerHolder = new DrawerItemHolder();
			view = mInflater.inflate(layoutResID, parent, false);

			drawerHolder.txtTitle = (TextView) view.findViewById(R.id.tv_title);
			drawerHolder.txtCount = (TextView) view
					.findViewById(R.id.tv_counter);
			drawerHolder.itemLayout = (RelativeLayout) view
					.findViewById(R.id.itemLayout);

			drawerHolder.avatarLayout = (LinearLayout) view
					.findViewById(R.id.avatarLayout);
			drawerHolder.tv_nav_username = (TextView) view
					.findViewById(R.id.tv_nav_username);
			drawerHolder.img_nav_avatar = (ImageView) view
					.findViewById(R.id.img_nav_avatar);
			drawerHolder.tv_edit_profile = (TextView) view
					.findViewById(R.id.tv_edit_profile);

			view.setTag(drawerHolder);
		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();
		}

		NavDrawerItem dItem = (NavDrawerItem) this.navDrawerItems.get(position);
		if (dItem.isAvatar()) {
			drawerHolder.itemLayout.setVisibility(View.GONE);
			drawerHolder.avatarLayout.setVisibility(View.VISIBLE);
			imageFetcher.loadImage(Global.userInfo.getUserAvatar(), drawerHolder.img_nav_avatar);
//			Bitmap avatar = LoadSaveImage.getThumbImage(
//					Global.userInfo.getUserAvatar(), Params.PARAM_USER_AVATAR
//							+ File.separator + Global.userInfo.getUserId(),
//					Global.userInfo.getUserAvatar().hashCode() + "");
//
//			drawerHolder.img_nav_avatar.setImageBitmap(avatar);
//			if (Global.userInfo != null)
//				Global.userInfo.setAvatarBitmap(avatar);
			drawerHolder.tv_nav_username.setText(Global.userInfo.getName());// dItem.getTitle()

			drawerHolder.img_nav_avatar
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// Toast.makeText(mContext, "Avatar selected",
							// Toast.LENGTH_SHORT).show();

						}
					});
			drawerHolder.tv_edit_profile
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							// Toast.makeText(mContext, "Edit profile selected",
							// Toast.LENGTH_SHORT).show();
							Intent intent = new Intent(mContext,
									EditProfileActivity.class);
							// intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							SessionManager mSession = SessionManager
									.getInstance(mContext);
							intent.putExtra(Params.PARAM_USER_ID_,
									mSession.getUserId());
							intent.putExtra(Params.PARAM_TOKEN_,
									mSession.getToken());
							intent.putExtra(Params.PARAM_NAME,
									Global.userInfo.getName());
							intent.putExtra(Params.PARAM_TITLE,
									Global.userInfo.getName());
							((Activity) mContext).startActivity(intent);
							if (callBack != null)
								callBack.closeMenuLeft(0);
						}
					});
		} else {
			drawerHolder.itemLayout.setVisibility(View.VISIBLE);
			drawerHolder.avatarLayout.setVisibility(View.GONE);

			drawerHolder.txtTitle.setText(dItem.getTitle());

			// displaying count
			// check whether it set visible or not
			if (dItem.getCounterVisibility()) {
				if (position == 6) {// foot print
					drawerHolder.txtCount.setText(Global.getSumFootPrintMessage());
				} else {
					drawerHolder.txtCount.setText(dItem.getCount());
				}
				drawerHolder.txtCount.setVisibility(View.VISIBLE);
			} else {
				// hide the counter view
				drawerHolder.txtCount.setVisibility(View.GONE);
			}
		}

		return view;
	}

	private static class DrawerItemHolder {
		TextView txtTitle, txtCount, tv_nav_username, tv_edit_profile;
		ImageView img_nav_avatar;
		LinearLayout avatarLayout;
		RelativeLayout itemLayout;
	}
}
