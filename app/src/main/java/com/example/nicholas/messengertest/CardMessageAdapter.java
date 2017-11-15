package com.example.nicholas.messengertest;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.f2prateek.progressbutton.ProgressButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by nicholas on 18/10/2017.
 *
 * This class represent the adapter user by recycler view for messages in a chat
 */

public class CardMessageAdapter extends RecyclerView.Adapter<CardMessageAdapter.ViewHolder>  {
    private ArrayList<Message> messages;
    private Context mContext;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public CardMessageAdapter(ArrayList<Message> messages, Context context){
        this.messages = messages;
        mContext = context;
    }

    @Override
    public CardMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_card, parent, false);
        return new CardMessageAdapter.ViewHolder(cv);
    }


    @Override
    public void onBindViewHolder(CardMessageAdapter.ViewHolder holder, int position) {
        final CardView cardView = holder.cardView;
        final Message message = messages.get(position);

        ((TextView)cardView.findViewById(R.id.message_text)).setText(message.message);
        LinearLayout layout = (LinearLayout) cardView.findViewById(R.id.bubble_layout);
        LinearLayout parent_layout = (LinearLayout) cardView.findViewById(R.id.bubble_layout_parent);
        final ProgressButton progressButton = (ProgressButton) cardView.findViewById(R.id.progressButton);
        message.button =progressButton;
        if (message.fileAttached){
            progressButton.setVisibility(View.VISIBLE);
        }else {
            progressButton.setVisibility(View.GONE);
        }
        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.uri == null)
                    message.getFile();
                else{
                    open_file(message.uri, message.mime, message);
                }
            }
        });
        //change position of the cloud message depending upon whose message this is
        if (message.isMine) {
            layout.setBackgroundResource(R.drawable.bubble2);
            parent_layout.setGravity(Gravity.RIGHT);
        }
        // If not mine then align to left
        else {
            layout.setBackgroundResource(R.drawable.bubble1);
            parent_layout.setGravity(Gravity.LEFT);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void open_file(Uri uri, String mime, Message message) {
        File tempFile = new File(uri.getPath());

        try {
            byte[] temp = org.apache.commons.io.IOUtils.toByteArray(mContext.getContentResolver().openInputStream(uri));

            OutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(message.decode(temp));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Get URI and MIME type of file
        Uri uriNew = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", tempFile);


        // Open file with user selected app
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uriNew, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException ex){

        }

    }
}
