package com.cls.sugutomo.apiclient;

import com.cls.sugutomo.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class ShowMessage {
	// new api error code
	public static final int INVALID_TOKEN = 10000; // 'User token invalid or
													// expired!'
	public static final int ERR_CREATE_TOKEN = 10001; // 'Error create token for
														// session';
	public static final int ERR_REGISTER_FB = 10002; // 'Error register with
														// facebook';
	public static final int ERR_UPLOAD_AVATAR = 10003; // 'Upload avatar error';
	public static final int ERR_SAVE_AVATAR = 10004; // 'Error save avatar to
														// dabatase';
	public static final int ERR_REGISTER_EMAIL = 10005; // 'Error register user
														// with email';
	public static final int ERR_SEND_EMAIL_ACTIVED = 10006; // 'Error send email
															// activate';
	public static final int ERR_ACCOUNT_HAS_ACTIVED = 1000; // 'Your account has
															// activate';
	public static final int ERR_SAVE_INFOUSER = 10008; // 'Error save info of
														// user';
	public static final int ERR_SAVE_PROFILE = 10009; // 'Error save profiles of
														// user';
	public static final int ERR_INVALID_PASSWORD = 10010; // 'Current password
															// is invalid';
	public static final int ERR_INVALID_EMAIL_OR_PASSWORD = 10011; // 'Email or
																	// password
																	// invalid!';
	public static final int ERR_SAVE_NEW_PASSWORD = 10012; // 'Error save new
															// password';
	public static final int ERR_INVALID_EMAIL = 10013; // 'Email invalid, cannot
														// reset password';
	public static final int ERR_SEND_EMAIL_RESETPASS = 10014; // 'Send mail
																// error to
																// reset
																// password';
	public static final int ERR_SET_PASSWORD = 10015; // 'Error set password';
	public static final int ERR_BLOCK_USER = 10016; // 'Error block user';
	public static final int ERR_UNBLOCK_USER = 10017; // 'Error unblock user';
	public static final int ERR_ADD_FAVORITE = 10018; // 'Error add user to
														// favorite',;
	public static final int ERR_REMOVE_FROM_FAVORITE = 10019; // 'Error remove
																// user from
																// favorite';
	public static final int ERR_SEND_BROADCAST = 10020; // 'Error send
														// broadcast';
	public static final int ERR_UPDATE_GPS_LOCATION = 10021; // 'Error update
																// gps
																// location';

	// success code
	public static final int API_SUCC_ACCOUNT_REGISTER = 0;
	public static final int API_SUCC_ACCOUNT_LOGIN = 1;
	public static final int FB_API_SUCC_LOGIN = 2;
	public static final int API_SUCC_SEND_EMAIL = 3;
	public static final int API_SUCC_GET_PROFILE_MASTERDATA = 4;
	public static final int API_SUCC_CREATE_PROFILE = 5;
	public static final int API_SUCC_ACCOUNT_FIRST_LOGIN = 6;
	public static final int API_SUCC_GET_TOP_APPEAL = 7;
	public static final int API_SUCC_LOAD_MAP = 8;
	public static final int API_SUCC_UPDATE_LOCATION = 9;

	// error code
	public static final int ERR_CONNECT_FAIL = 999;
	public static final int ERR_LOAD_STAMP = 9997;
	public static final int FB_API_ERR_LOGIN = 9998;
	public static final int API_ERR_ANOTHER = 9999;
	public static final int API_ERR_MISSING_PARAMS = 10000;
	public static final int API_ERR_ACCOUNT_EXISTED = 10001;
	public static final int API_ERR_ACCOUNT_WRONG_REGISTER_DATA_TYPE = 10002;
	public static final int API_ERR_ACCOUNT_LOGIN_FAIL = 10003;
	public static final int API_ERR_ACCOUNT_DOES_NOT_EXISTED = 10004;
	public static final int API_ERR_ACCOUNT_IS_INACTIVE_OR_BLOCKED = 10006;
	public static final int API_ERR_SEND_EMAIL_FAIL = 1007;
	public static final int API_ERR_ACCOUNT_IS_ACTIVATED = 1008;
	public static final int API_ERR_USER_USERNAME_NOT_EXIST = 10010;
	public static final int API_ERR_SAVE_LOCATION = 10011;
	public static final int API_ERR_GET_STAMP_FAIL = 30000;

	public static void showDialog(Context mContext, String title, String message) {
		try {

			if (mContext != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(title);
				builder.setMessage(message);
				builder.setNegativeButton(
						mContext.getString(R.string.btn_close),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});
				builder.show();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static void showDialogCustom(Context mContext, String title, String message) {
		try {

			if (mContext != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage(message);
				builder.setNegativeButton(
						mContext.getString(R.string.btn_close),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});
				builder.show();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void showMessage(Context mContext, String message) {
		if (mContext != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}
	}

	public static void showMessage(Context mContext, int error_code) {
		switch (error_code) {
		// success case
		case API_SUCC_ACCOUNT_REGISTER:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_SUCC_ACCOUNT_REGISTER),
					Toast.LENGTH_SHORT).show();
			break;
		case API_SUCC_ACCOUNT_LOGIN:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_SUCC_ACCOUNT_LOGIN),
					Toast.LENGTH_SHORT).show();
			break;
		case FB_API_SUCC_LOGIN:
			Toast.makeText(mContext,
					mContext.getString(R.string.FB_API_SUCC_LOGIN),
					Toast.LENGTH_SHORT).show();
			break;
		case API_SUCC_SEND_EMAIL:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_SUCC_SEND_EMAIL),
					Toast.LENGTH_SHORT).show();
			break;
		case API_SUCC_CREATE_PROFILE:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_SUCC_CREATE_PROFILE),
					Toast.LENGTH_SHORT).show();
			break;
		case API_SUCC_UPDATE_LOCATION:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_SUCC_UPDATE_LOCATION),
					Toast.LENGTH_SHORT).show();
			break;

		// error case
		case ERR_CONNECT_FAIL:
			Toast.makeText(mContext,
					mContext.getString(R.string.ERR_CONNECT_FAIL),
					Toast.LENGTH_SHORT).show();
			break;
		case ERR_LOAD_STAMP:
			Toast.makeText(mContext,
					mContext.getString(R.string.ERR_LOAD_STAMP),
					Toast.LENGTH_SHORT).show();
			break;
		case FB_API_ERR_LOGIN:
			Toast.makeText(mContext,
					mContext.getString(R.string.FB_API_ERR_LOGIN),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ANOTHER:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_ANOTHER),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_MISSING_PARAMS:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_MISSING_PARAMS),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_EXISTED:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_EXISTED),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_WRONG_REGISTER_DATA_TYPE:
			Toast.makeText(
					mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_WRONG_REGISTER_DATA_TYPE),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_LOGIN_FAIL:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_LOGIN_FAIL),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_DOES_NOT_EXISTED:
			Toast.makeText(
					mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_DOES_NOT_EXISTED),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_IS_INACTIVE_OR_BLOCKED:
			Toast.makeText(
					mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_IS_INACTIVE_OR_BLOCKED),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_SEND_EMAIL_FAIL:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_SEND_EMAIL_FAIL),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_ACCOUNT_IS_ACTIVATED:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_ACCOUNT_IS_ACTIVATED),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_USER_USERNAME_NOT_EXIST:
			Toast.makeText(
					mContext,
					mContext.getString(R.string.API_ERR_USER_USERNAME_NOT_EXIST),
					Toast.LENGTH_SHORT).show();
			break;
		case API_ERR_SAVE_LOCATION:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_SAVE_LOCATION),
					Toast.LENGTH_SHORT).show();
		case API_ERR_GET_STAMP_FAIL:
			Toast.makeText(mContext,
					mContext.getString(R.string.API_ERR_GET_STAMP_FAIL),
					Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
}
