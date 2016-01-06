package com.cls.sugutomo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;

public class BaseDialog extends Dialog {

	public BaseDialog(Context context) {
		super(context);
		// config base dialog
		// config window
		getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Rect displayRectangle = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(
				displayRectangle);
		int mwith = (int) (displayRectangle.width() * 0.9);
		getWindow().setLayout(mwith, LayoutParams.WRAP_CONTENT);
	}
}
