package com.example.nicholas.messengertest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Toolbar mActionBarToolbar;
    private TextView mToolbarTitle;
    private ArrayList<ChatPreview> chatPreviews;
    private int otherUserID;
    private String otherUsername;
    private FloatingActionButton addNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File x= new File("temp");
        //first set up setting and check if logged in
        initializeSettings();
        checkLoggedIn();
        //then regular routine
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();
        customizeToolbar();
        setOnClickListeners();
        setTitle(settings.getString("token",null));
        loadMessages();
    }

    public void initializeSettings(){
        settings = getSharedPreferences("MySettings", 0);
        editor = settings.edit();
    }

    /**
     * checks whether the user is logged in, if not, launches Login intent
     */
    public void checkLoggedIn(){
        String token = settings.getString("token",null);
        if (token == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            this.finish();
        }
    }

    /**
     * fields initialization
     */
    public void initializeFields(){
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        chatPreviews = new ArrayList<>();
        adapter = new CardChatAdapter(chatPreviews, getApplicationContext());
        recyclerView.setAdapter(adapter);
        addNewChat = (FloatingActionButton) findViewById(R.id.fab);
    }

    /**
     * sets on click listeners
     */
    public void setOnClickListeners(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMessages();
            }
        });
        addNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewChatDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_log_out:
                editor.clear();
                editor.commit();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            case R.id.action_settings:
                showSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void customizeToolbar() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mActionBarToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mActionBarToolbar);
        mToolbarTitle.setText(settings.getString("username","no:(((("));
//        mToolbarTitle.setText("het");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void loadMessages(){
        chatPreviews.clear();
        RestClient.get("/api/message/index?token="+settings.getString("token", null),null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    JSONArray jsonMessages = ((JSONArray) response.get("messages"));
                    for (int i = 0; i < jsonMessages.length(); i++) {
                        JSONObject jsonMessage = (JSONObject) jsonMessages.get(i);
                        getOtherID(jsonMessage);
                        JSONObject message = jsonMessage.getJSONObject("message");
                        chatPreviews.add(new ChatPreview(otherUserID,message.getString("url"),jsonMessage.getLong("timestamp"),MainActivity.this, jsonMessage.getString("format")));
                    }
                    refreshRecyclerView();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * retrieves the other user's id from jsonMessage
     * @param jsonMessage
     */
    public void getOtherID(JSONObject jsonMessage){
        int id = settings.getInt("myID",0);
        try {
            if (jsonMessage.getInt("receiver_id") == id)
                otherUserID = jsonMessage.getInt("sender_id");
            else
                otherUserID = jsonMessage.getInt("receiver_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public boolean isMine(int id){
        return id == settings.getInt("myID", 0);
    }

    protected void showSettingsDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_settings, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);
        final RadioGroup radioGroupCompression = (RadioGroup)promptView.findViewById(R.id.radio_group_compression);
        final RadioGroup radioGroupCoding = (RadioGroup)promptView.findViewById(R.id.radio_group_coding);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //saving which coding algorithm to use
                        switch (radioGroupCoding.getCheckedRadioButtonId()){
                            case R.id.repetition:
                                editor.putString("coding", "repetition");
                                break;
                            case R.id.hamming:
                                editor.putString("coding", "hamming");
                                break;
                            case R.id.parity:
                                editor.putString("coding", "parity");
                                break;
                        }
                        //saving which compression algorithm to use
                        switch (radioGroupCompression.getCheckedRadioButtonId()){
                            case R.id.shannon:
                                editor.putString("compression", "shannon");
                                break;
                            case R.id.lzm:
                                editor.putString("compression", "lzm");
                                break;
                            case R.id.huffman:
                                editor.putString("compression", "huffman");
                                break;
                        }
                        editor.commit();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * launches dialog for starting new chat
     */
    protected void showNewChatDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_new_chat, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.username_for_new_chat);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Start chat", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getID(editText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * launches new chat activity
     */
    private void startChat(){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("id", otherUserID);
        intent.putExtra("username", otherUsername);
        startActivity(intent);
    }

    /**
     * retrieves user id from server given username
     * @param username
     */
    private void getID(String username){
        RestClient.get("/api/user/index?username="+username, null, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    otherUserID = response.getJSONArray("userlist").getJSONObject(0).getInt("id");
                    otherUsername = response.getJSONArray("userlist").getJSONObject(0).getString("username");
                    startChat();
                } catch (JSONException e) {
                    showToast("invalid username");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * shows a toast with the message
     * @param message
     */
    private void showToast(String message){
        Toast toast =  Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void refreshRecyclerView(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }
}