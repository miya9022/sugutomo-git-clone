package com.cls.sugutomo.adapter;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cls.sugutomo.R;
import com.cls.sugutomo.api.DeleteConversationAPI;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.chat.ChatType;
import com.cls.sugutomo.chatlist.ChatListActivityServer;
import com.cls.sugutomo.circlarIV.CircularImageView;
import com.cls.sugutomo.loadimage.ImageFetcher;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.model.MessageModel;
import com.cls.sugutomo.utils.Global;
import com.cls.sugutomo.utils.MyApplication;
import com.loopj.android.http.RequestParams;
import com.rockerhieu.emojicon.EmojiconTextView;

public class UserAdapter extends ArrayAdapter<MessageModel> {
	LayoutInflater mInflater;
	private Context mContext;
	private int layoutResID;
	private Vector<MessageModel> listChats;
	private ChatType mChatType;
	private SessionManager mSession;
	private ImageFetcher imageFetcher;
	public UserAdapter(Context c, int layoutResourceId,
			Vector<MessageModel> listUser) {
		super(c, layoutResourceId, listUser);
		mContext = c;
		layoutResID = layoutResourceId;
		mInflater = LayoutInflater.from(mContext);
		this.listChats = listUser;
		mChatType = ChatType.getInstance(mContext);
		mSession = SessionManager.getInstance(mContext);
		imageFetcher= MyApplication.getInstance().getImageFetcher();
		//imageFetcher.setImageFadeIn(true);
		imageFetcher.setLoadingImage(R.drawable.loader);

	}

	public int getCount() {
		return listChats.size();
	}

	public MessageModel getItem(int position) {
		return listChats.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ChatListHolder holder = null;
		View row = convertView;

		if (row == null) {

			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResID, parent, false);
			holder = new ChatListHolder();

			holder.avatar = (CircularImageView) row
					.findViewById(R.id.chat_list_avatar);
			holder.countMessage = (TextView) row
					.findViewById(R.id.chat_list_count_message);
			holder.name = (TextView) row.findViewById(R.id.chat_list_name);
			holder.distance = (TextView) row
					.findViewById(R.id.chat_list_distance);
			holder.lastTimeChat = (TextView) row
					.findViewById(R.id.chat_list_lastTimeChat);
			holder.message = (EmojiconTextView) row
					.findViewById(R.id.chat_list_message);
			holder.image = (ImageView) row.findViewById(R.id.chat_list_image);
			holder.remove = (Button) row.findViewById(R.id.btn_chat_remove);

			row.setTag(holder);
		} else {
			holder = (ChatListHolder) row.getTag();
		}

		MessageModel messageModel = listChats.elementAt(position);

		// set image
		imageFetcher.loadImage( messageModel.getUserInfo().getUserAvatar(), holder.avatar);
//		loadImageAdapter(holder.avatar, messageModel);

		// set name
		holder.name.setText(messageModel.getUserInfo().getName());

		// set distance
		if(Global.ADMIN!=messageModel.getUserInfo().getUserId())
		holder.distance.setText(messageModel.getDistance() == null ? ""
				: messageModel.getDistance());

		// set last chat time
		holder.lastTimeChat.setText(Global.calculateTime(mContext,
				messageModel.getDateMessage() / 1000)
				+ mContext.getString(R.string.before));
		// set new message
		if (messageModel.getNewMessages() > 0) {
			holder.countMessage.setVisibility(View.VISIBLE);
			holder.countMessage.setText(String.valueOf(messageModel
					.getNewMessages()));
		} else {
			holder.countMessage.setVisibility(View.GONE);
		}
		String messageChat = messageModel.getMessage();
		String type =messageModel.getType();
		if (type.equals(ChatType.TYPE_IMAGE)) {
			holder.message.setText(mContext.getString(R.string.chat_list_image));
		}else if (type.equals(ChatType.TYPE_STAMP)){
			holder.message.setText(mContext.getString(R.string.chat_list_stamp));
		}else if (type.equals(ChatType.TYPE_POINT)){
			holder.message.setText(mContext.getString(R.string.chat_list_point));
		}else {
			if (messageChat != null)
				holder.message.setText(Global.filterBadWordJapan(messageChat,mContext));
		}
			if (messageModel.getLastSender()
					.equals(Global.userInfo.getUserName())) {
				holder.message.setBackgroundResource(R.drawable.chat_item_right);
			} else {
				holder.message.setBackgroundResource(R.drawable.chat_item_left);
			}
		
		// last message
//		String type = mChatType.receiveJSON(messageModel.getMessage());
//		if (type.equals(ChatType.STAMP) || type.equals(ChatType.IMAGE)) {
//			holder.message.setText(mContext.getString(
//					R.string.format_receive_image, messageModel.getUserInfo()
//							.getName()));
//		} else {
//			String messageChat = mChatType.executeReceive(messageModel
//					.getMessage());
//			//messageChat=StringEscapeUtils.unescapeJava(messageChat);
//			if (messageChat != null)
//				holder.message.setText(Global.filterBadWordJapan(messageChat,mContext));
//			if (messageModel.getUserInfo().getUserName()
//					.equals(Global.userInfo.getUserName())) {
//				holder.message
//						.setBackgroundResource(R.drawable.chat_item_right);
//			} else {
//				holder.message.setBackgroundResource(R.drawable.chat_item_left);
//			}
//		}
		// remove a chat
		holder.remove.setTag(messageModel);
		holder.remove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MessageModel item = (MessageModel) v.getTag();
				deleteConversation(item);
			}
		});
		return row;
	}

	static class ChatListHolder {
		TextView name;
		EmojiconTextView message;
		TextView countMessage;
		TextView distance;
		TextView lastTimeChat;
		CircularImageView avatar;
		Button remove;
		ImageView image;
	}
/*
	private void loadImageAdapter(final CircularImageView img,
			final MessageModel messageModel) {
		//Log.i("UserAdapter", "link image="+messageModel.getUserInfo().getUserAvatar());
		final String key = messageModel.getUserInfo().getUserAvatar()
				.hashCode()
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
				bitmap = LoadSaveImage.getThumbImage(messageModel.getUserInfo()
						.getUserAvatar(), Params.PARAM_USER_AVATAR
						+ File.separator
						+ messageModel.getUserInfo().getUserId(), key);
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
	private void deleteConversation(final MessageModel item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(mContext.getString(R.string.title_delete_conversation));
		builder.setNegativeButton(mContext.getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.setPositiveButton(mContext.getString(R.string.btn_chat_remove),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (item.getConversationId() > 0)
							deleteConversationServer(item.getConversationId());
//						else {
//							((ChatListActivityServer) mContext)
//									.deleteConversationLocal(item
//											.getConversationId());
//						}
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public void deleteConversationServer(long conversationId) {
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_ID, conversationId);
		DeleteConversationAPI deleteAPI = new DeleteConversationAPI(mContext,conversationId);
		deleteAPI.setParams(request);
		deleteAPI.onRunButtonPressed();
	}
}
