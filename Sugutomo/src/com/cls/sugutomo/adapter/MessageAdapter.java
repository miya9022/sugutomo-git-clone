package com.cls.sugutomo.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.model.UserModel;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.LoadSaveImage;
import com.cls.sugutomo.utils.MyApplication;
import com.rockerhieu.emojicon.EmojiconTextView;

public class MessageAdapter extends ArrayAdapter<MessageModel> {
	private static final String TAG = MessageAdapter.class.getSimpleName();

	LayoutInflater mInflater;
	private Context mContext;
	private ChatType mChatType;
	private Vector<MessageModel> mListMessage = new Vector<MessageModel>();
	// private CircularImageView avatar;
	private int layoutResID;

	private long timeDate2;
	private int beforePosition;

	private Bitmap imageBitmap;
	private UserModel chatUser;
	private ImageFetcher imageFetcher;
	private SharedPreferences pref;

	public MessageAdapter(Context c, int layoutResourceId,
			Vector<MessageModel> list, UserModel _chatUser) {
		super(c, layoutResourceId, list);
		imageFetcher = MyApplication.getInstance().getImageFetcher();
		imageFetcher.setLoadingImage(R.drawable.loader);
		pref = c.getSharedPreferences(Global.PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		chatUser = _chatUser;
		mContext = c;
		layoutResID = layoutResourceId;
		mInflater = LayoutInflater.from(mContext);
		this.mListMessage = list;
		mChatType = ChatType.getInstance(mContext);
		int size = mListMessage.size() - 1;
		String avatarUrl = null;
		while (size >= 0) {
			// Log.v("", "getUserName "+Global.userInfo.getUserName());
			UserModel user = mListMessage.get(size).getUserInfo();
			if (user == null || user.getUserName() == null) {
				user = chatUser;
			}
			// Log.v("", "mListMessage getUserName "+user.getUserName());
			if (!user.getUserName().equals(Global.userInfo.getUserName())) {
				if (avatarUrl == null)
					avatarUrl = user.getUserAvatar();
				if (!user.getUserAvatar().equalsIgnoreCase(avatarUrl)) {
					user.setUserAvatar(avatarUrl);
					// Log.e("","change avatar"+ position +
					// messageItem.getUserInfo().getUserAvatar() +" to "+
					// avatarUrl);
				}
			}
			// String mess =mListMessage.get(size).getMessage();
			// Log.v("", "test "+mListMessage.get(size).getType() +" - "+mess +
			// " ----- "+ Global.filterBadWordJapan(mess, c));
			// mess= Global.filterBadWordJapan(mess, c);
			// mListMessage.get(size).setMessage(mess);
			size--;
		}
	}

	public int getCount() {
		return mListMessage.size();
	}

	public MessageModel getItem(int position) {
		return mListMessage.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public void removeElementAtPosition(int position) {
		this.mListMessage.remove(position);
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		beforePosition = position;
		MessageModel messageItem = mListMessage.elementAt(position);
		if (Global.userInfo == null) {
			// try get from pref
			try {
				String myString = pref.getString(Params.PREF_USER_INFO, "");
				UserModel user = Global.setUserInfo(new JSONObject(myString),
						null);
				if (user != null) {
					Global.userInfo = user;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// set layout
		if (Global.userInfo == null)
			Log.e("", " messadapter userInfo  is null");
		if (Global.userInfo.getUserName() == null)
			Log.e("", " messadapter Global.userInfo.getUserName()  is null");
		if (messageItem.getUserInfo() == null)
			Log.e("", " messadapter messageItem.getUserInfo()  is null");
		if (messageItem.getUserInfo().getUserName() == null) {
			messageItem.getUserInfo().setUserName(
					"" + messageItem.getUserInfo().getUserId());
			Log.e("",
					" messadapter messageItem.getUserInfo().getUserName()  is null"
							+ messageItem.getUserInfo().getUserId()
							+ messageItem.getMessage());
		}
		// else
		if (messageItem.getUserInfo().getUserName()
				.equals(Global.userInfo.getUserName()))
			convertView = mInflater.inflate(R.layout.chat_item_right, null);
		else
			convertView = mInflater.inflate(R.layout.chat_item_left, null);

		// init UI
		CircularImageView avatar = (CircularImageView) convertView
				.findViewById(R.id.chat_avatar);
		TextView dateHeader = (TextView) convertView
				.findViewById(R.id.chat_dateHeader);
		EmojiconTextView message = (EmojiconTextView) convertView
				.findViewById(R.id.chat_message);
		ImageView stamp = (ImageView) convertView.findViewById(R.id.chat_stamp);
		ImageView image = (ImageView) convertView.findViewById(R.id.chat_image);
		TextView timeChat = (TextView) convertView.findViewById(R.id.chat_time);
		ImageView error = (ImageView) convertView
				.findViewById(R.id.chat_error_icon);
		ProgressBar progressbar = (ProgressBar) convertView
				.findViewById(R.id.chat_item_progressbar);

		if (messageItem.getUserInfo().getUserName()
				.equals(Global.userInfo.getUserName())) {
			message.setMaxWidth(Global.getWidthScreen(mContext)
					- (int) Global.convertDpToPixel(70, mContext));
		} else {
			message.setMaxWidth(Global.getWidthScreen(mContext)
					- (int) Global.convertDpToPixel(130, mContext));
		}
		// set time footer
		timeDate2 = messageItem.getDateMessage();
		timeChat.setVisibility(View.VISIBLE);
		timeChat.setText(Global.getTimeHour(timeDate2));

		// set time header
		if (position > 0) {
			long timeDate1 = mListMessage.elementAt(position - 1)
					.getDateMessage();
			if (Global.getTimeDate(timeDate1).equals(
					Global.getTimeDate(timeDate2))) {
				dateHeader.setVisibility(View.GONE);
			} else {
				dateHeader.setText(Global.getTimeDate(timeDate2));
				dateHeader.setVisibility(View.VISIBLE);
			}
		}
		if(avatar!=null) imageFetcher.loadImage(messageItem.getUserInfo().getUserAvatar(), avatar);
		//loadImageAdapter(avatar, messageItem);

		Paint p = new Paint();
		p.setTypeface(Typeface.DEFAULT); // if custom font use
		p.setTextSize(message.getTextSize());

		// get error
		if (error != null) {
			if (messageItem.isError())
				error.setVisibility(View.VISIBLE);
			else
				error.setVisibility(View.GONE);
		}

		// check type
//		String type = mChatType.receiveJSON(messageItem.getMessage());
		String type = messageItem.getType();
		// Log.v("","adapter mess: "+messageItem.getMessage());
//		if (type.equals(ChatType.STAMP)) {
		if (type.equals("3")) {
			// TODO
			Bitmap bm = LoadSaveImage.getImageFromSDCard(Params.PARAM_STAMP,messageItem.getMessage());
			if (bm != null) {
				stamp.setImageBitmap(bm);
			} else {
				stamp.setImageResource(R.drawable.ic_empty);
			}
			stamp.setVisibility(View.VISIBLE);
			message.setVisibility(View.GONE);
//		} else if (type.equals(ChatType.IMAGE)) {
		} else if (type.equals("2")) {
			imageFetcher.loadImage(messageItem.getMessage(), image);
//			downloadImage(progressbar, messageItem.getMessage(), image,
//					Params.CHAT_IMAGE + File.separator
//							+ messageItem.getUserInfo().getUserName(),messageItem.getMessage().hashCode()+"");
			// TODO Show Image
//			ImageModel imageModel = mChatType.excuteImage(messageItem
//					.getMessage());
//			imageBitmap = LoadSaveImage.getImageFromSDCard(Params.CHAT_IMAGE
//					+ File.separator + messageItem.getUserInfo().getUserName(),
//					String.valueOf(imageModel.getImageId()));
			// image of me
//			if (imageModel.getUserModel() == null) {
//				setThumbImage(imageBitmap, image);
//			} else {
//				// download image
//				if (imageBitmap == null)
//					downloadImage(progressbar, imageModel.getFullPath(), image,
//							Params.CHAT_IMAGE + File.separator
//									+ messageItem.getUserInfo().getUserName(),
//							String.valueOf(imageModel.getImageId()));
//				else
//					setThumbImage(imageBitmap, image);
//			}
			image.setVisibility(View.VISIBLE);
			message.setVisibility(View.GONE);
		} else {
			String messageChat = messageItem
					.getMessage();
			if (messageChat != null) {
				// String str =StringEscapeUtils.unescapeJava(messageChat);
				message.setText(Global
						.filterBadWordJapan(messageChat, mContext));

			}
			// message.setText(messageChat);
			// message.setTextFix(messageChat);
		}
		return convertView;
	}

	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		// reset avatar if user thay doi
		int size = mListMessage.size() - 1;
		String avatarUrl = null;
		while (size >= 0) {
			UserModel user = mListMessage.get(size).getUserInfo();
			if (user == null || user.getUserName() == null) {
				user = chatUser;
			}
			if (!user.getUserName().equals(Global.userInfo.getUserName())) {
				if (avatarUrl == null)
					avatarUrl = user.getUserAvatar();
				if (!user.getUserAvatar().equalsIgnoreCase(avatarUrl)) {
					user.setUserAvatar(avatarUrl);
					// Log.e("","change avatar"+ position +
					// messageItem.getUserInfo().getUserAvatar() +" to "+
					// avatarUrl);
				}
			}
			size--;
		}
		super.notifyDataSetChanged();
	}

	private void setThumbImage(Bitmap bitmap, ImageView image) {
		Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		if (thumbBitmap != null) {
			image.setImageBitmap(thumbBitmap);
		} else {
			image.setImageResource(R.drawable.ic_empty);
		}
	}

	private void downloadImage(final ProgressBar progressbar,
			final String imageUrl, final ImageView imageView,
			final String path, final String filename) {
		progressbar.setIndeterminate(false);
		progressbar.setMax(100);
		progressbar.setVisibility(View.VISIBLE);
		AsyncTask<Void, String, Bitmap> downloadTask = new AsyncTask<Void, String, Bitmap>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressbar.setProgress(0);
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				progressbar.setProgress(Integer.parseInt(values[0]));
			}

			@Override
			protected Bitmap doInBackground(Void... params) {
				int count;
				Bitmap bitmap = null;
				try {
					URL url = new URL(imageUrl);
					URLConnection conection = url.openConnection();
					conection.connect();
					// this will be useful so that you can show a tipical 0-100%
					// progress bar
					int lenghtOfFile = conection.getContentLength();

					// download the file
					InputStream input = new BufferedInputStream(
							url.openStream(), 8192);
					ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
					// Output stream
					OutputStream output = new BufferedOutputStream(dataStream);
					byte data[] = new byte[1024];
					long total = 0;
					while ((count = input.read(data)) != -1) {
						total += count;
						// publishing the progress....
						// After this onProgressUpdate will be called
						publishProgress(""
								+ (int) ((total * 100) / lenghtOfFile));
						// writing data to file
						output.write(data, 0, count);
					}
					// flushing output
					output.flush();
					BitmapFactory.Options bmOptions = new BitmapFactory.Options();
					bmOptions.inSampleSize = 1;

					byte[] bytes = dataStream.toByteArray();
					bitmap = BitmapFactory.decodeByteArray(bytes, 0,
							bytes.length, bmOptions);

					input.close();
					output.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				progressbar.setVisibility(View.GONE);
				LoadSaveImage.saveThumbImage(result, path, filename);
				setThumbImage(result, imageView);
				// Log.v(TAG, "save file complete with path = " + path + "/"
				// + filename);
			}
		};
		downloadTask.execute();
	}
	/*
	 public View getView(int position, View convertView, ViewGroup parent) {
		beforePosition = position;
		MessageModel messageItem = mListMessage.elementAt(position);
		if (Global.userInfo == null) {
			// try get from pref
			try {
				String myString = pref.getString(Params.PREF_USER_INFO, "");
				UserModel user = Global.setUserInfo(new JSONObject(myString),
						null);
				if (user != null) {
					Global.userInfo = user;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// set layout
		if (Global.userInfo == null)
			Log.e("", " messadapter userInfo  is null");
		if (Global.userInfo.getUserName() == null)
			Log.e("", " messadapter Global.userInfo.getUserName()  is null");
		if (messageItem.getUserInfo() == null)
			Log.e("", " messadapter messageItem.getUserInfo()  is null");
		if (messageItem.getUserInfo().getUserName() == null) {
			messageItem.getUserInfo().setUserName(
					"" + messageItem.getUserInfo().getUserId());
			Log.e("",
					" messadapter messageItem.getUserInfo().getUserName()  is null"
							+ messageItem.getUserInfo().getUserId()
							+ messageItem.getMessage());
		}
		// else
		if (messageItem.getUserInfo().getUserName()
				.equals(Global.userInfo.getUserName()))
			convertView = mInflater.inflate(R.layout.chat_item_right, null);
		else
			convertView = mInflater.inflate(R.layout.chat_item_left, null);

		// init UI
		CircularImageView avatar = (CircularImageView) convertView
				.findViewById(R.id.chat_avatar);
		TextView dateHeader = (TextView) convertView
				.findViewById(R.id.chat_dateHeader);
		EmojiconTextView message = (EmojiconTextView) convertView
				.findViewById(R.id.chat_message);
		ImageView stamp = (ImageView) convertView.findViewById(R.id.chat_stamp);
		ImageView image = (ImageView) convertView.findViewById(R.id.chat_image);
		TextView timeChat = (TextView) convertView.findViewById(R.id.chat_time);
		ImageView error = (ImageView) convertView
				.findViewById(R.id.chat_error_icon);
		ProgressBar progressbar = (ProgressBar) convertView
				.findViewById(R.id.chat_item_progressbar);

		if (messageItem.getUserInfo().getUserName()
				.equals(Global.userInfo.getUserName())) {
			message.setMaxWidth(Global.getWidthScreen(mContext)
					- (int) Global.convertDpToPixel(70, mContext));
		} else {
			message.setMaxWidth(Global.getWidthScreen(mContext)
					- (int) Global.convertDpToPixel(130, mContext));
		}
		// set time footer
		timeDate2 = messageItem.getDateMessage();
		timeChat.setVisibility(View.VISIBLE);
		timeChat.setText(Global.getTimeHour(timeDate2));

		// set time header
		if (position > 0) {
			long timeDate1 = mListMessage.elementAt(position - 1)
					.getDateMessage();
			if (Global.getTimeDate(timeDate1).equals(
					Global.getTimeDate(timeDate2))) {
				dateHeader.setVisibility(View.GONE);
			} else {
				dateHeader.setText(Global.getTimeDate(timeDate2));
				dateHeader.setVisibility(View.VISIBLE);
			}
		}
		if(avatar!=null) imageFetcher.loadImage(messageItem.getUserInfo().getUserAvatar(), avatar);
		//loadImageAdapter(avatar, messageItem);

		Paint p = new Paint();
		p.setTypeface(Typeface.DEFAULT); // if custom font use
		p.setTextSize(message.getTextSize());

		// get error
		if (error != null) {
			if (messageItem.isError())
				error.setVisibility(View.VISIBLE);
			else
				error.setVisibility(View.GONE);
		}

		// check type
		String type = mChatType.receiveJSON(messageItem.getMessage());
		if (type.equals(ChatType.STAMP)) {
			// TODO
			StampModel stampModel = mChatType.excuteStamp(messageItem
					.getMessage());
			Bitmap bm = LoadSaveImage.getImageFromSDCard(Params.PARAM_STAMP,
					stampModel.getName());
			if (bm != null) {
				stamp.setImageBitmap(bm);
			} else {
				stamp.setImageResource(R.drawable.ic_empty);
			}
			stamp.setVisibility(View.VISIBLE);
			message.setVisibility(View.GONE);
		} else if (type.equals(ChatType.IMAGE)) {
			// TODO Show Image
			ImageModel imageModel = mChatType.excuteImage(messageItem
					.getMessage());
			imageBitmap = LoadSaveImage.getImageFromSDCard(Params.CHAT_IMAGE
					+ File.separator + messageItem.getUserInfo().getUserName(),
					String.valueOf(imageModel.getImageId()));
			// image of me
			if (imageModel.getUserModel() == null) {
				setThumbImage(imageBitmap, image);
			} else {
				// download image
				if (imageBitmap == null)
					downloadImage(progressbar, imageModel.getFullPath(), image,
							Params.CHAT_IMAGE + File.separator
									+ messageItem.getUserInfo().getUserName(),
							String.valueOf(imageModel.getImageId()));
				else
					setThumbImage(imageBitmap, image);
			}
			image.setVisibility(View.VISIBLE);
			message.setVisibility(View.GONE);
		} else {
			String messageChat = mChatType.executeReceive(messageItem
					.getMessage());
			if (messageChat != null) {
				// String str =StringEscapeUtils.unescapeJava(messageChat);
				message.setText(Global
						.filterBadWordJapan(messageChat, mContext));

			}
			// message.setText(messageChat);
			// message.setTextFix(messageChat);
		}
		return convertView;
	}

	
	 */

	/*private void loadImageAdapter(final CircularImageView img,
			final MessageModel messageItem) {
		// Log.i(TAG, "" + messageItem.getUserInfo().getUserAvatar());
		final String key = messageItem.getUserInfo().getUserAvatar().hashCode()
				+ "";
		Bitmap bm = ImageCache.getInstance().getBitmap(key);
		if (bm != null && img != null) {
			img.setImageBitmap(bm);
			return;
		}
		class MyTask extends AsyncTask<Void, Void, Void> {
			private Bitmap bitmap = null;

			@Override
			protected Void doInBackground(Void... params) {
				bitmap = LoadSaveImage.getThumbImage(messageItem.getUserInfo()
						.getUserAvatar(), Params.PARAM_USER_AVATAR
						+ File.separator
						+ messageItem.getUserInfo().getUserId(), key);
				if (bitmap != null) {
					ImageCache.getInstance().putBitmap(key, bitmap);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (bitmap != null && img != null) {
					img.setImageBitmap(bitmap);
				}

			}
		}
		;
		new MyTask().execute();
	}
*/
}