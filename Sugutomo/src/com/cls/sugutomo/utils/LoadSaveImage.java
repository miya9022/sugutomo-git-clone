package com.cls.sugutomo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.model.MessageModel;

public class LoadSaveImage {

	private static final String TAG = LoadSaveImage.class.getSimpleName();
	
	public static void setAsWallpaper(Bitmap bitmap, Activity ac) {
		try {
			WallpaperManager wm = WallpaperManager.getInstance(ac);

			wm.setBitmap(bitmap);
			Toast.makeText(ac,
					ac.getString(R.string.toast_wallpaper_set),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(ac.getApplicationContext(),
					ac.getString(R.string.toast_wallpaper_set_failed),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void shareImage(String path, Activity ac) {
		File file = new File(path);
		Uri uri = Uri.fromFile(file);
		
		Intent uploadimage = new Intent(Intent.ACTION_SEND, uri);
		uploadimage.putExtra(Intent.EXTRA_STREAM, uri);
		uploadimage.setType("image/png");
		ac.startActivity(uploadimage);
	}
	
	// save thumb image and resize
	public static String saveThumbImage(Bitmap bitmap, String path,
			String filename) {
		try {
			// save
			File compressedPictureFile = new File(
					android.os.Environment.getExternalStorageDirectory(),
					File.separator + Global.PREFERENCE_NAME + File.separator
							+ path);
			if (!compressedPictureFile.exists())
				compressedPictureFile.mkdirs();
			compressedPictureFile = new File(compressedPictureFile.getPath(),
					filename + ".png");
			if (compressedPictureFile.exists())
				return compressedPictureFile.getAbsolutePath();
			// save
//			try {
				FileOutputStream fOut = new FileOutputStream(
						compressedPictureFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			return compressedPictureFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getThumbImage(String url_img, String path,
			String filename) {
		Bitmap bitmap = null;
		try {
			// get from sdcard
			bitmap = getImageFromSDCard(path, filename);
			// download
			if (bitmap == null) {
				if (url_img == null)
					return null;
				if (android.os.Build.VERSION.SDK_INT > 9) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
							.permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				URL url = new URL(url_img);
				HttpURLConnection http = (HttpURLConnection) url
						.openConnection();
				http.setConnectTimeout(20000);
				if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
					InputStream input = http.getInputStream();
					bitmap = BitmapFactory.decodeStream(input);
					if (bitmap != null)
						saveThumbImage(bitmap, path, filename);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static Bitmap getImageFromSDCard(String path, String filename) {
		Bitmap bitmap = null;
		try {
			File file = new File(
					android.os.Environment.getExternalStorageDirectory(),
					File.separator + Global.PREFERENCE_NAME + File.separator
							+ path);
			if (!file.exists())
				file.mkdirs();
			file = new File(file.getPath(), filename + ".png");
			if (file.exists()) {
				// return image from sdcard
				try {
//					bitmap = BitmapFactory.decodeStream(new FileInputStream(
//							file));
					bitmap = decodeFile(file, 250, 250);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// BEGIN_INCLUDE (calculate_sample_size)
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

		}
		return inSampleSize;
		// END_INCLUDE (calculate_sample_size)
	}

	public static Bitmap getBitmap(String path) {
		try {
			// Get the dimensions of the bitmap
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			// Decode the image file into a Bitmap sized to fill the View
			opts.inJustDecodeBounds = false;
			// opts.inSampleSize = scaleFactor * 1;
			opts.inSampleSize = calculateInSampleSize(opts, 500, 500);
			// opts.inPurgeable = true;
			return BitmapFactory.decodeFile(path, opts);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Bitmap decodeFile(File f, float maxWidth, float maxHeight) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < maxWidth
						|| height_tmp / 2 < maxHeight)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			//
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inDither = false; // Disable Dithering mode

			bitmapOptions.inPurgeable = true; // Tell to gc that whether it
												// needs free memory, the Bitmap
												// can be cleared

			bitmapOptions.inInputShareable = true; // Which kind of reference
													// will be used to recover
													// the Bitmap data after
													// being clear, when it will
													// be used in the future

			bitmapOptions.inTempStorage = new byte[32 * 1024];
			bitmapOptions.inSampleSize = scale;
			boolean imageSet = false;
			Bitmap bitmap = null;
			while (!imageSet) {
				try {
					bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
							null, bitmapOptions);
					imageSet = true;
				} catch (OutOfMemoryError e) {
					bitmapOptions.inSampleSize *= 2;
				}
			}
			return bitmap;

		} catch (FileNotFoundException e) {
		}
		return null;
	}


}
