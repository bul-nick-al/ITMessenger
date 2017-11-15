package com.example.nicholas.messengertest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.nicholas.messengertest.CompressionAndConing.CodingAndCompression;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

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
    private long size;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initializeSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        customizeToolbar();
        initializeFields();
        launchBackgroundMessageReload();
        setOnClickListeners();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
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
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.add(new Message(id+"", Message.Type.text, (long)(new Date()).getTime()/1000, editText.getText().toString(),null,true,"<M>", ChatActivity.this));
                messages.get(messages.size()-1).message = editText.getText().toString().trim();
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size()-1);
                sendMessage(true);
                editText.setText("");
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
                            messages.add(new Message(null, Message.Type.text,jsonMessages.getJSONObject(i).getLong("timestamp"),
                                    jsonMessages.getJSONObject(i).getJSONObject("message").getString("url"),null, isMine((int) jsonMessages.getJSONObject(i).get("sender_id")),
                                    jsonMessages.getJSONObject(i).getString("format"), ChatActivity.this));
                        }
                        refreshRecyclerView(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            RestClient.get("/api/message/get?token="+settings.getString("token", null)+"&user_id="+id+"&time="+messages.get(messages.size()-1).timeDate,null,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    boolean somethingArrived = false;
                    try {
                        JSONArray jsonMessages = (JSONArray) response.get("messages");
                        for (int i = 0; i < jsonMessages.length(); i++) {
                            somethingArrived = true;
                            if (!isMine((int) jsonMessages.getJSONObject(i).get("sender_id")))
                                messages.add(new Message(null, Message.Type.text,jsonMessages.getJSONObject(i).getLong("timestamp"),
                                        jsonMessages.getJSONObject(i).getJSONObject("message").getString("url"),null, isMine((int) jsonMessages.getJSONObject(i).get("sender_id")),
                                        jsonMessages.getJSONObject(i).getString("format"), ChatActivity.this));
                        }

                        if (somethingArrived)
                            refreshRecyclerView(true);
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
    public void refreshRecyclerView(final boolean toBottom){
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.getAdapter().notifyDataSetChanged();
                if (toBottom)
                    recyclerView.smoothScrollToPosition(messages.size()-1);
            }
        });

    }

    @Override
    protected void onDestroy() {
        keepLoading = false;
        super.onDestroy();
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
            uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                messages.add(new Message(id+"", Message.Type.file, (long)(new Date()).getTime()/1000, uri.getPath(),null,true,"<M>", ChatActivity.this));
                refreshRecyclerView(true);
                messages.get(messages.size()-1).message = uri.getLastPathSegment().substring(uri.getLastPathSegment().lastIndexOf('/')+1);
                sendMessage(false);
            }
        }
    }

    private void sendMessage(final boolean isText){
        RequestParams params = new RequestParams();
        params.put("token", settings.getString("token",null));
        params.put("user_id",id);
        File tempFile = null;
        FileOutputStream outputStream = null;
        try {
            if (isText){
                tempFile = new File(ChatActivity.this.getFilesDir(),"test");

                outputStream = new FileOutputStream(tempFile);
                //TODO: here you should call not getBytes but some encoding and compression methods
                outputStream.write(encode(editText.getText().toString().trim().getBytes()));
                outputStream.close();
                //TODO: also add compression and coding names to formats
                params.put("format","<M><"+settings.getString("compression",null)+"><"+settings.getString("coding",null)+">");
                params.put("file","message",tempFile);
            }
            else {
                byte[] temp = org.apache.commons.io.IOUtils.toByteArray(getContentResolver().openInputStream(uri));
                tempFile = new File(ChatActivity.this.getFilesDir(),uri.getPath().substring(uri.getPath().lastIndexOf('/')+1));
                outputStream = new FileOutputStream(tempFile);
                //TODO: here you should call not temp but some encoding and compression methods
                outputStream.write(encode(temp));
                outputStream.close();
                //TODO: also add compression and coding names to formats
                params.put("format","<F><"+settings.getString("compression",null)+"><"+settings.getString("coding",null)+"><type:"+this.getContentResolver().getType(uri)+">");
                params.put("file",uri.getPath().substring(uri.getPath().lastIndexOf('/')+1),tempFile);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RestClient.post("/api/message/put",params,new JsonHttpResponseHandler(){
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

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                if (!isText){
                    messages.get(messages.size()-1).button.setVisibility(View.VISIBLE);
                    messages.get(messages.size()-1).button.setProgressAndMax((int)(((float)bytesWritten/totalSize)*10000), 10000);
                }
                super.onProgress(bytesWritten, totalSize);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });

        refreshRecyclerView(true);
    }

    Uri uri;

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    private byte[] encode(byte[] input){
        String compressionType = settings.getString("compression", null);
        String codingType = settings.getString("coding", null);
        CodingAndCompression.Compression compression = null;
        CodingAndCompression.Coding coding = null;
        if (compressionType.equals("shannon"))
            compression = CodingAndCompression.Compression.shannon;
        if (compressionType.equals("lzm"))
            compression = CodingAndCompression.Compression.lzm;
        if (compressionType.equals("huffman"))
            compression = CodingAndCompression.Compression.huffman;
        if (codingType.equals("repetition"))
            coding = CodingAndCompression.Coding.repetition;
        if (codingType.equals("parity"))
            coding = CodingAndCompression.Coding.parity;
        if (codingType.equals("hamming"))
            coding = CodingAndCompression.Coding.hamming;
        return CodingAndCompression.compressAndEncode(input, compression, coding);
    }


    private boolean isMine(int id){
        return id == settings.getInt("myID",0);
    }
}
