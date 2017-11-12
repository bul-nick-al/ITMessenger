package com.example.nicholas.messengertest;


import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicholas on 20/10/2017.
 * This class represents a single message preview
 */

public class ChatPreview {
    Context context;
    String username;
    int id;
    long timeDate;
    String lastMessage;

    public ChatPreview(int id, String lastMessage, long timeDate, Context context) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.timeDate = timeDate;
        this.context = context;
        setUsername();
    }

    private void setUsername(){
        RestClient.get("/api/user/index?id="+id, null, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    username = response.getJSONArray("userlist").getJSONObject(0).getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ((MainActivity)context).refreshRecyclerView();
            }
        });
    }
}
