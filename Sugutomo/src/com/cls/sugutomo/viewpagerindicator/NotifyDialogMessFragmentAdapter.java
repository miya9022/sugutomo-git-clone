package com.cls.sugutomo.viewpagerindicator;

import java.util.ArrayList;

import com.cls.sugutomo.model.MessageModel;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class NotifyDialogMessFragmentAdapter extends FragmentStatePagerAdapter{
    protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };
//    protected static final int[] ICONS = new int[] {
//            R.drawable.perm_group_calendar,
//            R.drawable.perm_group_camera,
//            R.drawable.perm_group_device_alarms,
//            R.drawable.perm_group_location
//    };

    public static int mCount = 1;
  public static ArrayList<MessageModel> messengerList;
    public NotifyDialogMessFragmentAdapter(FragmentManager fm) {
        super(fm);
    }
	 @Override
	 public int getItemPosition(Object object) {
	 // TODO Auto-generated method stub
	 return POSITION_NONE;
	 }
    @Override
    public Fragment getItem(int position) {
        return MessengerFragment.newInstance(messengerList.get(position));
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return NotifyDialogMessFragmentAdapter.CONTENT[position % CONTENT.length];
    }


    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}