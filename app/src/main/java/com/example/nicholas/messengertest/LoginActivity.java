package com.example.nicholas.messengertest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LoginActivity extends AppCompatActivity {
    private boolean signUp;
    private String token;
    private Button loginButton;
    private EditText mUsernameEditTextView;
    private EditText mPasswordEditTextView;
    private TextView signUpTextView;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFields();
        setOnClickListeners();
    }

    /**
     * initializes all fields
     */
    private void initializeFields(){
        settings = getSharedPreferences("MySettings", 0);
        editor = settings.edit();
        loginButton = (Button) findViewById(R.id.btn_login);
        mUsernameEditTextView = (EditText) findViewById(R.id.input_username);
        mPasswordEditTextView = (EditText) findViewById(R.id.sign_up_password);
        signUpTextView = (TextView) findViewById(R.id.link_signup);
    }

    /**
     * sets onClick listeners to clickable objects
     */
    private void setOnClickListeners(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!signUp){
                    tryLogin();
                }
                else {
                    trySignUp();
                }

            }
        });
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp = true;
                loginButton.setText("Sign Up");
            }
        });
    }

    /**
     * Attempts to connect conduct authorization with the server
     */
    private void tryLogin(){
        username = mUsernameEditTextView.getText().toString();
        password = mPasswordEditTextView.getText().toString();
        RestClient.get("api/session/create?name="+username+"&password="+password, null, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    token = (String) response.get("auth_token");
                    editor.putString("username", username);
                    editor.putString("token", token);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    LoginActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    showToast((String)errorResponse.get("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Attempts to connect conduct authorization with the server
     */
    private void trySignUp(){
        username = mUsernameEditTextView.getText().toString();
        password = mPasswordEditTextView.getText().toString();
        RestClient.get("/api/registration/new?name="+username+"&password="+password, null, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                showToast("Success");
                signUp = false;
                loginButton.setText("Log in");
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    showToast((String) ((JSONArray) errorResponse.get("username")).get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
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

}
