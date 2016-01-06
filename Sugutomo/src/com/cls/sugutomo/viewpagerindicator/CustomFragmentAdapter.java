package com.cls.sugutomo.viewpagerindicator;

import java.util.ArrayList;

import com.cls.sugutomo.model.ImageModel;
import com.cls.sugutomo.model.UserModel;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

public class CustomFragmentAdapter extends FragmentStatePagerAdapter implements
		IconPagerAdapter {
	protected static final String[] CONTENT = new String[] { "This", "Is", "A",
			"Test", };
	// public static UserModel mUserFriend;
	 public static ArrayList<ImageModel> userAvatarList;
	//private ArrayList<ImageModel> userAvatarList;
	private FragmentManager fragMan;
	private boolean isZoom;
	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

	public static int mCount = 1;

	public CustomFragmentAdapter(FragmentManager fm, boolean isZoom) {
		super(fm);
		fragMan = fm;
		this.isZoom = isZoom;
		//this.userAvatarList = userAvatarList;
	}

//	public void setAvatarList(ArrayList<ImageModel> userAvatarList) {
//		this.userAvatarList = userAvatarList;
//	}

	 @Override
	 public int getItemPosition(Object object) {
	 // TODO Auto-generated method stub
	 return POSITION_NONE;
	 }

	@Override
	public Fragment getItem(int position) {
		Fragment f;

		if (!isZoom)
			f = CustomFragment.newInstance(userAvatarList.get(position),
					position);// userAvatarList[position];
		else
			f = ZoomFragment.newInstance(userAvatarList.get(position));
		fragments.add(f);
		return f;
	}

	public Fragment getItemAt(int position) {
		return fragments.get(position);
	}

	public void clearAllPage() // Clear all page
	{
		for (int i = fragments.size()-1; i >0 ; i--)
			fragMan.beginTransaction().remove(fragments.get(i)).commit();
		fragments.clear();
	}

	@Override
	public int getCount() {
		if (mCount != userAvatarList.size()) {
			mCount = userAvatarList.size();
			notifyDataSetChanged();
		}
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return CustomFragmentAdapter.CONTENT[position % CONTENT.length];
	}

	@Override
	public int getIconResId(int index) {
		// return ICONS[index % ICONS.length];
		return 0;
	}

	public void setCount(int count) {
		// if (count > 0 && count <= 10) {
		mCount = count;
		notifyDataSetChanged();
		// }
	}
}