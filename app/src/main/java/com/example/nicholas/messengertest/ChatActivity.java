package com.example.nicholas.messengertest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar mActionBarToolbar;
    private TextView mToolbarTitle;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private int id;
    private boolean keepLoading = true;
    private ArrayList<Message> messages;
    private EditText editText;
    private ImageButton sendMessageButton;
    private ImageButton attachFileButton;
    private Thread thread;
    private byte[] file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initializeSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        customizeToolbar();
        initializeFields();
        launchBackgroundMessageReload();
        setOnClickListeners();
    }

    /**
     * initializes fields of activity
     */
    public void initializeFields(){
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager)layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        messages = new ArrayList<>();
        adapter = new CardMessageAdapter(messages, getApplicationContext());
        recyclerView.setAdapter(adapter);
        sendMessageButton = (ImageButton)findViewById(R.id.sendMessageButton);
        attachFileButton = (ImageButton)findViewById(R.id.attachFileButton);
        editText = (EditText)findViewById(R.id.messageEditText);
    }

    public void initializeSettings(){
        settings = getSharedPreferences("MySettings", 0);
        id = getIntent().getExtras().getInt("id");
        editor = settings.edit();
    }

    /**
     * customises toolbar
     */
    public void customizeToolbar() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mActionBarToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mActionBarToolbar);
        mToolbarTitle.setText(getIntent().getExtras().getString("username"));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * loads messages in background
     */
    public void launchBackgroundMessageReload(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepLoading){
                    loadMessages();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * set on click listeners
     */
    public void setOnClickListeners(){
        mActionBarToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File file = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "xxx.jpeg");
                    FileOutputStream fOut = new FileOutputStream(file);
                    fOut.write(ChatActivity.this.file);
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(id+"", Message.Type.text, (long)(new Date()).getTime()/1000, editText.getText().toString(),null,true));
                RequestParams params = new RequestParams();
                params.put("message", editText.getText().toString().trim());
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size()-1);
                editText.setText("");
                RestClient.post("/api/message/put?token="+settings.getString("token",null)+"&user_id="+id+"",params,new JsonHttpResponseHandler(){
                });
            }
        });
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 42);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * loads messages from server
     */
    public void loadMessages(){
        if (messages.size() == 0){
            RestClient.get("/api/message/get?token="+settings.getString("token", null)+"&user_id="+id,null,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    try {
                        JSONArray jsonMessages = (JSONArray) response.get("messages");
                        for (int i = 0; i < jsonMessages.length(); i++) {
                            messages.add(new Message(null, Message.Type.text,jsonMessages.getJSONObject(i).getLong("timestamp"), (String) jsonMessages.getJSONObject(i).get("body"),null, isMine((int) jsonMessages.getJSONObject(i).get("sender_id"))));
                        }
                        refreshRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            RestClient.get("/api/message/get?token="+settings.getString("token", null)+"&user_id="+id+"&time="+messages.get(messages.size()-1).date,null,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    boolean somethingArrived = false;
                    try {
                        JSONArray jsonMessages = (JSONArray) response.get("messages");
                        for (int i = 0; i < jsonMessages.length(); i++) {
                            somethingArrived = true;
                            if (!isMine((int) jsonMessages.getJSONObject(i).get("sender_id")))
                                messages.add(new Message(null, Message.Type.text,jsonMessages.getJSONObject(i).getLong("timestamp"), (String) jsonMessages.getJSONObject(i).get("body"),null, isMine((int) jsonMessages.getJSONObject(i).get("sender_id"))));
                        }
                        refreshRecyclerView();
                        if (somethingArrived)
                            recyclerView.scrollToPosition(messages.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * invoke recycler view refreshment
     */
    public void refreshRecyclerView(){
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }
    @Override
    protected void onStop() {
        keepLoading = false;
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            file = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                     file = org.apache.commons.io.IOUtils.toByteArray(getContentResolver().openInputStream(uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private boolean isMine(int id){
        return id == settings.getInt("myID",0);
    }
}
