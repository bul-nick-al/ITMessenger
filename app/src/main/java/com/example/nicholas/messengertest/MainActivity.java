package com.example.nicholas.messengertest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferences settings;
    private  SharedPreferences.Editor editor;
    private Toolbar mActionBarToolbar;
    private TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences("MySettings", 0);
        editor = settings.edit();
        editor.commit();
        String token = settings.getString("token","null");
        if (token.equals("null")){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            this.finish();
        }
        setTitle(token);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customizeToolbar();


        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<ChatPreview> x= new ArrayList<>();
        x.add(new ChatPreview());
        adapter = new CardChatAdapter(x, getApplicationContext());
        recyclerView.setAdapter(adapter);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_alarm) {
            editor.clear();
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    public void customizeToolbar() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) mActionBarToolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(mActionBarToolbar);
        mToolbarTitle.setText(settings.getString("token","no:(((("));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}


//    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//    final TextView textView = (TextView) findViewById(R.id.text);
//        fab.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View view) {
//        textView.setText("Hey fag");
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//        .setAction("Action", null).show();
//        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        }
//        });


//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("token","mamka twoya tut byla");
//        editor.commit();
//        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//                builder1.setMessage());
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
//        System.out.println("Hello");
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);