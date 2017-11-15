package com.example.nicholas.messengertest;
import android.os.Looper;

import com.loopj.android.http.*;

/**
 * Created by nicholas on 01/11/2017.
 *
 * This is a static class for communication with the server
 */

public class RestClient {
    private static final String BASE_URL = "http://10.241.1.116:8080/";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static AsyncHttpClient syncHttpClient= new SyncHttpClient();

    /**
     * This method conducts the get request. ResponseHandler methods have to be overriden.
     *
     * @param url
     * @param params any additional parameters to the request. The  RequestParams class should be used
     * @param responseHandler handler to the response from the server.
     */
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);

    }

    /**
     * This method conducts the post request. ResponseHandler methods have to be overriden.
     *
     * @param url
     * @param params any additional parameters to the request. The  RequestParams class should be used
     * @param responseHandler handler to the response from the server.
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return client;
    }
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


}
