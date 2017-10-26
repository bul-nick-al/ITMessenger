package com.example.nicholas.messengertest;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by nicholas on 18/10/2017.
 */

public class CardChatAdapter extends RecyclerView.Adapter<CardChatAdapter.ViewHolder> {

    private ArrayList<ChatPreview> chats;
    Context mContext;
    EditText text;
    ImageButton button;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public CardChatAdapter(ArrayList<ChatPreview> chats, Context context){
        this.chats = chats;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_card, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CardView cardView = holder.cardView;
        CircleImageView pic = (CircleImageView) cardView.findViewById(R.id.profilePic);
        pic.setImageResource(R.drawable.q6);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
////                ((CircleImageView) cardView.findViewById(R.id.profilePic)).setImageResource(R.drawable.q8);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
}
