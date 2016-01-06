package com.cls.sugutomo.viewpagerindicator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.model.MessageModel;
import com.rockerhieu.emojicon.EmojiconTextView;

public final class MessengerFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";
	private MessageModel messenger = null;

	public static MessengerFragment newInstance(MessageModel _messenger) {
		MessengerFragment fragment = new MessengerFragment(_messenger);
		return fragment;
	}

	public MessengerFragment() {

	}

	public MessengerFragment(MessageModel _messenger) {
		messenger = _messenger;
	}

	private String mContent = "???";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final EmojiconTextView txtMess = new EmojiconTextView(getActivity());
		// img.setAdjustViewBounds(true);
		ScrollView layout = new ScrollView(getActivity());
		layout.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(txtMess);
		FrameLayout.LayoutParams lo = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lo.gravity = Gravity.CENTER;
		txtMess.setTextColor(Color.BLACK);
		txtMess.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		float scaleDensity = getResources().getDisplayMetrics().density;
		txtMess.setEmojiconSize((int) (20 * scaleDensity));
		txtMess.setLayoutParams(lo);
		txtMess.setGravity(Gravity.CENTER_HORIZONTAL);
		if (messenger != null) {
			String type = messenger.getType();
			if (type.equalsIgnoreCase(ChatType.IMAGE)){
				txtMess.setText(getString(R.string.chat_list_image));
			}else if(type.equalsIgnoreCase(ChatType.STAMP))
				txtMess.setText(getString(R.string.chat_list_stamp));
			else
				txtMess.setText(messenger.getMessage());
		} else {
			//case dang show notification thi` dung taskmanger kill app
			//sau do khi nhan notification thi` goi MessengerFragment() chu ko goi MessengerFragment(messamodel)
			txtMess.setText("test");
		}
		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTENT, mContent);
	}
}
