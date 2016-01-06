package com.cls.sugutomo.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.android.camera.MediaStoreUtils;
import com.cls.sugutomo.R;
import com.cls.sugutomo.api.APICallbackInterface;
import com.cls.sugutomo.api.FileUploadAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.apiclient.ShowMessage;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.loopj.android.http.RequestParams;

public class UploadPhotoActivity extends Activity implements
		APICallbackInterface {

	private static final String TAG = UploadPhotoActivity.class.getSimpleName();
	// Camera activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_SELECT_IMAGE_REQUEST_CODE = 200;
	private static final int PIC_CROP_REQUEST_CODE = 300;
	private static int REQUEST_CROP_PICTURE = 2;
	private TextView title1, title2;
	private CircularImageView avatarIv;
	private Button confirmPhoto;
	private ProgressDialog progressdialogAvatar;

	private FileUploadAPI mFileUpload;

	private String pathUpload = null;
	private int mUserId;
	private String mName, mToken, mFB_id, mUsername;
	private int type;
	private Uri picUri;
	private File croppedImageFile;
	private File captureImageFile;
	private File doneImageFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_photo_activity);
		createSDcardFolder();
		croppedImageFile = new File(Environment.getExternalStorageDirectory(),
				File.separator + getString(R.string.app_name) + File.separator
						+ "crop_img.jpg");
		title1 = (TextView) findViewById(R.id.title_register_photo1);
		title2 = (TextView) findViewById(R.id.title_register_photo2);
		avatarIv = (CircularImageView) findViewById(R.id.avatarIv);
		confirmPhoto = (Button) findViewById(R.id.btn_confirm_photo);
		avatarIv.setBorderWidth(10);
		avatarIv.addShadow();

		progressdialogAvatar = new ProgressDialog(this);
		progressdialogAvatar.setMessage(getString(R.string.loading));
		mFileUpload = new FileUploadAPI(this, this);

		Intent intent = getIntent();
		if (intent != null) {
			// register by facebook
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				type = SessionManager.getInstance(this).getLoginType();
				mName = bundle.getString(Params.PARAM_NAME);
				mToken = bundle.getString(Params.PARAM_TOKEN);
				mUserId = bundle.getInt(Params.PARAM_USER_ID);
				mUsername = bundle.getString(Params.PARAM_USERNAME);
				if (type == Global.TYPE_FACEBOOK) {
					mFB_id = bundle.getString(Params.PARAM_FB);
					loadAvatarFB(mFB_id, mUsername);
					title1.setText(getString(R.string.title_register_photo));
					title2.setVisibility(View.GONE);
				} else {
					type = Global.TYPE_EMAIL;
					title1.setText(getString(R.string.title_register_nophoto1));
					title2.setVisibility(View.VISIBLE);
					findViewById(R.id.btn_input_fbphoto).setVisibility(
							View.GONE);
				}
			}

		}
	}

	public void loadAvatarFB(String fbid, final String userId) {
		progressdialogAvatar.show();
		AsyncTask<String, Void, Bitmap> task = new AsyncTask<String, Void, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				try {
					URL url = new URL("https://graph.facebook.com/" + params[0]
							+ "/picture?type=large");
					Log.i("Map", "get avatar:" + "https://graph.facebook.com/"
							+ params[0] + "/picture?type=large");
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					http.setConnectTimeout(20000);
					if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream input = http.getInputStream();
						Bitmap avatar = BitmapFactory.decodeStream(input);
						input.close();
						return avatar;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				progressdialogAvatar.dismiss();
				if (result != null) {
					confirmPhoto.setVisibility(View.VISIBLE);
					avatarIv.setImageBitmap(result);
					pathUpload = LoadSaveImage.saveThumbImage(result,
							Params.PARAM_USER_AVATAR, userId);
				}
			}
		};
		task.execute(fbid);
	}

	private void createSDcardFolder() {
		File mediaStorageDir = new File(Environment
				.getExternalStorageDirectory().getAbsolutePath(), ""
				+ getString(R.string.app_name));

		/** Create the storage directory if it does not exist */
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				// return null;
			}
		}
		String NOMEDIA = "capture";
		File nomediaFile = new File(mediaStorageDir.getAbsolutePath(), NOMEDIA);
		Log.v("", "save to : " + nomediaFile.getAbsolutePath().toString());
		if (!nomediaFile.exists()) {
			if (!nomediaFile.mkdirs()) {
				// return null;
			}
		}
	}

	private void captureImage() {
		try {
			// use standard intent to capture an image
			File mediaStorageDir = new File(Environment
					.getExternalStorageDirectory().getAbsolutePath(), ""
					+ getString(R.string.app_name));

			/** Create the storage directory if it does not exist */
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					// return null;
				}
			}
			String NOMEDIA = "capture";
			File nomediaFile = new File(mediaStorageDir.getAbsolutePath(),
					NOMEDIA);
			Log.v("", "save to : " + nomediaFile.getAbsolutePath().toString());
			if (!nomediaFile.exists()) {
				if (!nomediaFile.mkdirs()) {
					// return null;
				}
			}
			String imageFilePath = nomediaFile.getAbsolutePath()
					+ "/picture.jpg";
			captureImageFile = new File(imageFilePath);
			startActivityForResult(MediaStoreUtils.getCameraCaptureIntent(
					UploadPhotoActivity.this, Uri.fromFile(captureImageFile)),
					CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
		} catch (Exception ex) {
			ex.printStackTrace();
			// display an error message
			String errorMessage = getString(R.string.CAPTURE_ERROR);
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void selectImage() {
		try {
			// Intent intent2 = new Intent(
			// Intent.ACTION_PICK,
			// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// startActivityForResult(intent2,
			// CAMERA_SELECT_IMAGE_REQUEST_CODE);
			startActivityForResult(MediaStoreUtils.getPickImageIntent(this),
					CAMERA_SELECT_IMAGE_REQUEST_CODE);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support capturing images!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	/**
	 * SELECT OPTION:
	 * 
	 */
	public void selectOption() {
		String[] sOptionAddPhoto = getResources().getStringArray(
				R.array.camera_option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.photo));
		builder.setItems(sOptionAddPhoto,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							captureImage();
							break;
						case 1:
							selectImage();
							break;
						}
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {

			if ((requestCode == CAMERA_SELECT_IMAGE_REQUEST_CODE || requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
					&& (resultCode == RESULT_OK)) {
				// When the user is done picking a picture, let's start the
				// CropImage Activity,
				// setting the output image file and size to 300x300 pixels
				// square.
				// Toast.makeText(getApplicationContext(), "begin crop",
				// Toast.LENGTH_SHORT).show();
				Uri croppedImage;
				if (requestCode == CAMERA_SELECT_IMAGE_REQUEST_CODE) {
					croppedImage = Uri.fromFile(croppedImageFile);
					doneImageFile = new File(croppedImageFile.getAbsolutePath());
				} else {
					croppedImage = Uri.fromFile(captureImageFile);
					doneImageFile = new File(captureImageFile.getAbsolutePath());
				}
				CropImageIntentBuilder cropImage = new CropImageIntentBuilder(
						300, 300, croppedImage);
				cropImage.setOutlineColor(0xFF03A9F4);
				if (requestCode == CAMERA_SELECT_IMAGE_REQUEST_CODE)
					cropImage.setSourceImage(data.getData());
				else
					cropImage.setSourceImage(croppedImage);

				startActivityForResult(cropImage.getIntent(this),
						REQUEST_CROP_PICTURE);
			} else if ((requestCode == REQUEST_CROP_PICTURE)
					&& (resultCode == RESULT_OK)) {
				// When we are done cropping, display it in the ImageView.
				// Toast.makeText(getApplicationContext(), "crop success",
				// Toast.LENGTH_SHORT).show();
				// imageView.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
				// if (data != null)
				// getCropImage(data);
				// getCropImage(BitmapFactory.decodeFile(croppedImageFile
				// .getAbsolutePath()));
				Bitmap thePic = BitmapFactory.decodeFile(doneImageFile
						.getAbsolutePath());
				confirmPhoto.setVisibility(View.VISIBLE);
				avatarIv.setImageBitmap(thePic);
				long timeNowEpoch = System.currentTimeMillis();
				if (mUserId > -1)
					pathUpload = LoadSaveImage.saveThumbImage(thePic,
							Params.PARAM_AVATAR + File.separator + mUserId, ""
									+ timeNowEpoch);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		// switch (requestCode) {
		// case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
		// if (resultCode == Activity.RESULT_OK) {
		// // get the Uri for the captured image
		// MyApplication myApplication = (MyApplication) getApplication();
		// picUri = myApplication.getPicUri();// use the pic uri
		// // picUri = data.getData();
		// performCropImage();
		// } else if (resultCode == RESULT_CANCELED) {
		// // user cancelled Image capture
		// ShowMessage.showMessage(this,
		// getString(R.string.CAPTURE_USER_CANCEL));
		// } else {
		// // failed to capture image
		// ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
		// getString(R.string.CAPTURE_ERROR));
		// }
		// break;
		//
		// case CAMERA_SELECT_IMAGE_REQUEST_CODE:
		// if (resultCode == Activity.RESULT_OK) {
		// picUri = data.getData();
		// performCropImage();
		//
		// } else if (resultCode == RESULT_CANCELED) {
		// // user cancelled choose image
		// ShowMessage.showMessage(this,
		// getString(R.string.CHOOSE_USER_CANCEL));
		// } else {
		// // failed to capture image
		// ShowMessage.showDialog(this, getString(R.string.ERR_TITLE),
		// getString(R.string.CHOOSE_ERROR));
		// }
		// break;
		//
		// case PIC_CROP_REQUEST_CODE:
		// if (resultCode == Activity.RESULT_OK) {
		// if (data != null)
		// getCropImage(data);
		// }
		// break;
		// }
	}

	private void performCropImage() {
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 300);
			cropIntent.putExtra("outputY", 300);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP_REQUEST_CODE);
		} catch (Exception anfe) {
			// display an error message
			String errorMessage = getString(R.string.crop_not_support);
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
		// String errorMessage = getString(R.string.crop_not_support);
		// try {
		// // call the standard crop action intent (the user device may not
		// // support it)
		// Intent cropIntent = new Intent("com.android.camera.action.CROP");
		// cropIntent.setType("image/*");
		// // cropIntent.setClassName("com.android.gallery",
		// // "com.android.camera.CropImage");
		// // cropIntent.setClassName("com.google.android.gallery3d",
		// // "com.android.gallery3d.app.CropImage");
		// List<ResolveInfo> list = getPackageManager().queryIntentActivities(
		// cropIntent, 0);
		// int size = list.size();
		// if (size == 0) {
		// Log.i(TAG, "Can not find image crop app");
		// Toast toast = Toast
		// .makeText(this, errorMessage, Toast.LENGTH_SHORT);
		// toast.show();
		// return;
		// } else {
		// // indicate image type and Uri
		// cropIntent.setDataAndType(picUri, "image/*");
		// // set crop properties
		// cropIntent.putExtra("crop", "true");
		// // indicate aspect of desired crop
		// cropIntent.putExtra("aspectX", 1);
		// cropIntent.putExtra("aspectY", 1);
		// // indicate output X and Y
		// cropIntent.putExtra("outputX", 300);
		// cropIntent.putExtra("outputY", 300);
		// cropIntent.putExtra("scale", true);
		// // retrieve data on return
		// cropIntent.putExtra("return-data", true);
		// // start the activity - we handle returning in onActivityResult
		// startActivityForResult(cropIntent, PIC_CROP_REQUEST_CODE);
		// }
		// } catch (ActivityNotFoundException anfe) {
		// // display an error message
		//
		// Toast toast = Toast
		// .makeText(this, errorMessage, Toast.LENGTH_SHORT);
		// toast.show();
		// }
	}

	private void getCropImage(Intent data) {
		long timeNowEpoch = System.currentTimeMillis();
		// get the returned data
		Bundle extras = data.getExtras();
		if (extras != null) {
			// get the cropped bitmap
			Log.i(TAG, "get cropped bitmap!");
			Bitmap thePic = extras.getParcelable("data");
			confirmPhoto.setVisibility(View.VISIBLE);
			avatarIv.setImageBitmap(thePic);
			if (mUserId > -1)
				pathUpload = LoadSaveImage.saveThumbImage(thePic,
						Params.PARAM_AVATAR + File.separator + mUserId, ""
								+ timeNowEpoch);
		}

		// ////

		// ////

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm_photo:
			File avatar = new File(pathUpload);
			if (pathUpload == null || pathUpload == "") {
				handleReceiveData();
			} else {
				if (mToken == null || mUserId < 0) {
					ShowMessage
							.showDialog(
									getApplicationContext(),
									getString(R.string.ERR_TITLE),
									getString(R.string.API_ERR_ACCOUNT_DOES_NOT_EXISTED));
					break;
				} else if (!avatar.exists()) {
					ShowMessage.showDialog(getApplicationContext(),
							getString(R.string.ERR_TITLE),
							getString(R.string.ERR_FILE_NOT_FOUND));
					break;
				}
				RequestParams params = new RequestParams();
				try {
					params.put(Params.PARAM_AVATAR, avatar);
					params.put(Params.PARAM_USER_ID_, mUserId);
					params.put(Params.PARAM_TOKEN_, mToken);
					mFileUpload.setmParams(params);
					mFileUpload.onRunButtonPressed();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			break;
		case R.id.btn_input_fbphoto:
			loadAvatarFB(mFB_id, mUsername);
			break;
		case R.id.btn_input_uploadphoto:
			selectOption();
			break;
		default:
			break;
		}
	}

	@Override
	public void handleReceiveData() {
		ShowMessage.showMessage(getApplicationContext(),
				getString(R.string.upload_photo_complete));
		Intent intent = new Intent(this, CreateProfileActivity.class);
		intent.putExtra(Params.PARAM_NAME, mName);
		intent.putExtra(Params.PARAM_TOKEN_, mToken);
		intent.putExtra(Params.PARAM_USER_ID_, mUserId);
		startActivity(intent);
		overridePendingTransition(R.anim.diagslide_enter,
				R.anim.diagslide_leave);
		// delete img in capture folder
		File capture = new File(Environment.getExternalStorageDirectory(),
				File.separator + getString(R.string.app_name) + File.separator
						+ "capture");
		if (capture.exists()) {
			for (File file : capture.listFiles())
				file.delete();
		}
		// /////
		finish();
	}

	@Override
	public void handleGetList() {

	}
}