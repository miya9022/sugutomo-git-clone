package com.cls.sugutomo.apiclient;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

public interface APIClientInterface {
    List<RequestHandle> getRequestHandles();

    void addRequestHandle(RequestHandle handle);

//    void onRunButtonPressed();
//
//    void onCancelButtonPressed();

    Header[] getRequestHeaders();

    HttpEntity getRequestEntity();

    AsyncHttpClient getAsyncHttpClient();

    void setAsyncHttpClient(AsyncHttpClient client);

    AsyncHttpRequest getHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context);

    ResponseHandlerInterface getResponseHandler();

    String getDefaultURL();

    String getDefaultHeaders();

    boolean isRequestHeadersAllowed();

    boolean isRequestBodyAllowed();

//    int getSampleTitle();

    boolean isCancelButtonAllowed();

    RequestHandle executeRequest(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler);
}
