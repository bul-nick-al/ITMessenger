package com.example.nicholas.messengertest;
import android.os.Looper;

import com.loopj.android.http.*;

/**
 * Created by nicholas on 01/11/2017.
 */

public class RestClient {
    private static final String BASE_URL = "http://10.241.1.116:8080/";

    private static AsyncHttpClient client = new AsyncHttpClient();
    public static AsyncHttpClient syncHttpClient= new SyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);

    }

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
