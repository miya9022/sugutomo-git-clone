package com.cls.sugutomo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.cls.sugutomo.R;

public class CustomizeProgressDialog extends Dialog {

	private ImageView loadingIcon;
	private AnimationDrawable loadingViewAnim;
	private Context mContext;

	public CustomizeProgressDialog(Context context) {
		super(context, R.style.NewDialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams wlmp = getWindow().getAttributes();
		wlmp.gravity = Gravity.CENTER_HORIZONTAL;
		getWindow().setAttributes(wlmp);
		setTitle(null);
		setCancelable(true);
		setCanceledOnTouchOutside(false);
		setOnCancelListener(null);
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		loadingIcon = new ImageView(mContext);
		loadingIcon.setBackgroundResource(R.anim.progress_animation);
		loadingViewAnim = (AnimationDrawable) loadingIcon.getBackground();
		layout.addView(loadingIcon, params);
		addContentView(layout, params);
	}
	
	@Override
	public void show() {
		super.show();
		if (loadingViewAnim != null)
			loadingViewAnim.start();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if (loadingViewAnim != null)
			loadingViewAnim.stop();
	}
}
