package com.example.nicholas.messengertest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar mActionBarToolbar;
    private TextView mToolbarTitle;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    EditText username;
    ArrayList<Message> messages;
    EditText editText;
    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences("MySettings", 0);
        editor = settings.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        customizeToolbar();
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        username = (EditText)findViewById(R.id.userName);
        layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager)layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        messages = new ArrayList<>();
        adapter = new CardMessageAdapter(messages, getApplicationContext());
        recyclerView.setAdapter(adapter);
        button = (ImageButton)findViewById(R.id.sendMessageButton);
        editText = (EditText)findViewById(R.id.messageEditText);
        loadMessages();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(username.getText().toString(), Message.Type.text, new Date().toString(), editText.getText().toString(),null,true));
                RequestParams params = new RequestParams();
                params.put("message", editText.getText().toString().getBytes());
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size()-1);
                editText.setText("");
                RestClient.post("/api/message/put?token="+settings.getString("token",null)+"&user_id="+username.getText().toString(),params,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        super.onSuccess(statusCode, headers, responseString);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                });
            }
        });
    }

    public void customizeToolbar() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mActionBarToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mActionBarToolbar);
        mToolbarTitle.setText("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void loadMessages(){
        RestClient.get("/api/message/get?token="+settings.getString("token", null)+"&user_id=3",null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    JSONArray jsonMessages = (JSONArray) response.get("messages");
                    for (int i = 0; i < jsonMessages.length(); i++) {
                        messages.add(new Message(null, Message.Type.text, (String) jsonMessages.getJSONObject(i).get("timestamp"), (String) jsonMessages.getJSONObject(i).get("body"),null, isMine((int) jsonMessages.getJSONObject(i).get("sender_id"))));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isMine(int id){
        return id == 4;
    }
}
