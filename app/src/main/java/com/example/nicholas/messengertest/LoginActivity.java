package com.example.nicholas.messengertest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.mime.Header;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LoginActivity extends AppCompatActivity {
    private boolean done;
    private boolean success;
    private String token;
    private Button loginButton;
    private EditText mUsernameEditTextView;
    private EditText mPasswordEditTextView;
    private SharedPreferences settings;
    private  SharedPreferences.Editor editor;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = getSharedPreferences("MySettings", 0);
        editor = settings.edit();
        loginButton = (Button) findViewById(R.id.btn_login);
        mUsernameEditTextView = (EditText) findViewById(R.id.input_username);
        mPasswordEditTextView = (EditText) findViewById(R.id.input_password);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = mUsernameEditTextView.getText().toString();
                password = mPasswordEditTextView.getText().toString();

                RestClient.get("api/session/create?name="+username+"&password="+password, null, new JsonHttpResponseHandler()  {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        try {
                            if (!response.get("success").equals("false")){
                                token = (String) response.get("auth_token");
                                editor.putString("username", username);
                                editor.putString("token", token);
                                editor.commit();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                LoginActivity.this.finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast toast = null;
                        try {
                            toast = Toast.makeText(getApplicationContext(), (String)errorResponse.get("message"), Toast.LENGTH_SHORT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        toast.show();
                    }
                });




            }
        });
    }

//                AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
//                builder1.setMessage(usrName);
//                builder1.setCancelable(true);
//
//                builder1.setPositiveButton(
//                        "Yes",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//
//                builder1.setNegativeButton(
//                        "No",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog alert11 = builder1.create();
//                alert11.show();
}
