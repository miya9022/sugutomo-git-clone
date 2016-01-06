package com.cls.sugutomo.apiclient;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import android.app.Activity;
import android.content.Context;

public abstract class APIClientBaseActivity extends Activity implements
		APIClientInterface {

	private final List<RequestHandle> requestHandles = new LinkedList<RequestHandle>();
	public static final String BASE_URL = "http://sugutomo.codelovers.vn/api/";
	public static final String ERR_10048 = "10048";
	public static final String ERROR_10000= "10000";//token sai hoac user bi xoa
	public static final String ERROR_10023= "10023";//Cannot find the appeal package
	public static final String ERROR_10024= "10024";//Cannot have enough point to buy this package appeal
	public static final String ERROR_10027= "10027";// not found that user == get detail profile 1 user ma da bi delete khoi server db
	public static final String ERROR_10033= "10033";//Not found that conversation
	public static final String ERROR_10034= "10034";//Cannot delete this conversation
	public static final String ERROR_10035= "10035";//"Not found conversation"
	public static final String OK = "OK";
	public static final String CODE = "code";
	public static final String MESSAGE = "message";
	public static final String DATA = "data";
	public static final String USER = "user";
	public static final String USERS = "users";
	public static final String OPENFIRE = "openfire";
	public static final String RESTRICT_WORD = "restricted_words";
	public static final String PROFILES ="profiles";
	public static final String AVATARS ="avatars";

	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient() {

		@Override
		protected AsyncHttpRequest newAsyncHttpRequest(
				DefaultHttpClient client, HttpContext httpContext,
				HttpUriRequest uriRequest, String contentType,
				ResponseHandlerInterface responseHandler, Context context) {
			AsyncHttpRequest httpRequest = getHttpRequest(client, httpContext,
					uriRequest, contentType, responseHandler, context);
			return httpRequest == null ? super.newAsyncHttpRequest(client,
					httpContext, uriRequest, contentType, responseHandler,
					context) : httpRequest;
		}
	};

	public String getMessage(JSONObject json, String params) {
		String result = "";
		try {
			JSONArray array = json.getJSONArray(params);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					result += array.getString(i);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<RequestHandle> getRequestHandles() {
		return requestHandles;
	}

	@Override
	public void addRequestHandle(RequestHandle handle) {
		if (null != handle) {
			requestHandles.add(handle);
		}
	}

	public void onRunButtonPressed() {
		addRequestHandle(executeRequest(getAsyncHttpClient(), getDefaultURL(),
				getRequestHeaders(), getRequestEntity(), getResponseHandler()));
	}

	public void onCancelButtonPressed() {
		asyncHttpClient.cancelRequests(this, true);
	}

	@Override
	public Header[] getRequestHeaders() {
		return null;
	}

	@Override
	public HttpEntity getRequestEntity() {
		return null;
	}

	@Override
	public AsyncHttpClient getAsyncHttpClient() {
		return this.asyncHttpClient;
	}

	@Override
	public void setAsyncHttpClient(AsyncHttpClient client) {
		this.asyncHttpClient = client;
	}

	@Override
	public AsyncHttpRequest getHttpRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, ResponseHandlerInterface responseHandler,
			Context context) {
		return null;
	}

	@Override
	public String getDefaultHeaders() {
		return null;
	}

	@Override
	public boolean isRequestHeadersAllowed() {
		return false;
	}

	@Override
	public boolean isRequestBodyAllowed() {
		return false;
	}

	@Override
	public boolean isCancelButtonAllowed() {
		return false;
	}

}
