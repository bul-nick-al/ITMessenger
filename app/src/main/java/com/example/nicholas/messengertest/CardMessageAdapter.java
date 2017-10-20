package com.example.nicholas.messengertest;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nicholas on 20/10/2017.
 */

public class CardMessageAdapter extends RecyclerView.Adapter<CardMessageAdapter.ViewHolder>  {
    private ArrayList<Message> messages;
        Context mContext;

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
        Message message = messages.get(position);
        ((TextView)cardView.findViewById(R.id.message_text)).setText(message.body);
        LinearLayout layout = (LinearLayout) cardView.findViewById(R.id.bubble_layout);
        LinearLayout parent_layout = (LinearLayout) cardView.findViewById(R.id.bubble_layout_parent);
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
}
