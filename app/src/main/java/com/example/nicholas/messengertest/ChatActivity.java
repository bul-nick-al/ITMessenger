package com.example.nicholas.messengertest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<Message> x;
    EditText editText;
    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager)layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        x= new ArrayList<>();
        x.add(new Message(null, Message.Type.text, null, "Hey", null, true));
        x.add(new Message(null, Message.Type.text, null, "Hey", null, false));
        x.add(new Message(null, Message.Type.text, null, "Hey", null, false));
        x.add(new Message(null, Message.Type.text, null, "Hey", null, false));
        x.add(new Message(null, Message.Type.text, null, "Hey", null, true));
        x.add(new Message(null, Message.Type.text, null, "fuckf,gerykfgerkyfgekyrfgerkufygaerfygaer,fjygaeryfgae,ryfga,eyfgae,ryfg,aeyfgae,rfga,ergfaej,rfgaerfmgejhfegrf,erjgfaj,ehgrfjreh", null, true));
        adapter = new CardMessageAdapter(x, getApplicationContext());
        recyclerView.setAdapter(adapter);
        button = (ImageButton)findViewById(R.id.sendMessageButton);
        editText = (EditText)findViewById(R.id.messageEditText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x.add(new Message(null, Message.Type.text, new Date(), editText.getText().toString(),null,true));
                recyclerView.getAdapter().notifyDataSetChanged();
                recyclerView.scrollToPosition(x.size()-1);
                editText.setText("");
            }
        });
    }
}
