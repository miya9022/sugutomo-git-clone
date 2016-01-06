package com.cls.sugutomo.adapter;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.ImageCache;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;

public class CustomGridViewAdapter extends ArrayAdapter<UserModel> {

	Context context;
	int layoutResourceId;
	Vector<UserModel> data;
	private ImageFetcher imageFetcher;
	public CustomGridViewAdapter(Context context, int layoutResourceId,
			Vector<UserModel> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		//imageFetcher.setImageFadeIn(true);
		imageFetcher.setLoadingImage(R.drawable.loader);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new RecordHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
			holder.txtCount = (TextView) row.findViewById(R.id.item_count);
			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}
		if(position>=data.size()) return row;//fix bug 
		UserModel item = data.get(position);
		holder.txtTitle.setText(item.getName());
		holder.postion = position;
		if (holder.txtCount != null)
			holder.txtCount.setText(String.valueOf(item.getCountFootprint()));
//		holder.imageItem.setImageDrawable(context.getResources().getDrawable(R.drawable.loader));
		imageFetcher.loadImage(item.getUserAvatar(), holder.imageItem);
		//getImgFromSDCard(holder, item, position);

		return row;
	}
/*
	private void getImgFromSDCard(final RecordHolder viewHolder,
			final UserModel item, final int postion) {
		final ImageView img = viewHolder.imageItem;
		AsyncTask<Void, Void, Void> loadthumb = new AsyncTask<Void, Void, Void>() {
			Bitmap bitmap = null;

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
			}

			@Override
			protected Void doInBackground(Void... params) {
				bitmap = ImageCache.getInstance().getBitmap(
						item.getUserAvatar().hashCode() + "");
				if (bitmap == null)
					bitmap = LoadSaveImage.getImageFromSDCard(
							Params.PARAM_USER_AVATAR + File.separator
									+ item.getUserId(), item.getUserAvatar()
									.hashCode() + "");
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (bitmap == null) {
					img.setImageBitmap(BitmapFactory.decodeResource(
							context.getResources(), R.drawable.loader));
				} else {
					if (postion == viewHolder.postion) {
						img.setImageBitmap(bitmap);
						ImageCache.getInstance().putBitmap(
								item.getUserAvatar().hashCode() + "", bitmap);
					}
				}
			}
		};
		loadthumb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// loadthumb.execute();
	}
*/
	static class RecordHolder {
		int postion;
		TextView txtTitle;
		ImageView imageItem;
		TextView txtCount;
	}
}
