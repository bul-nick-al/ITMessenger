package com.example.nicholas.messengertest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by nicholas on 18/10/2017.
 *
 * This class represent the adapter used by recycler view for preview of chats
 */

public class CardChatPreviewAdapter extends RecyclerView.Adapter<CardChatPreviewAdapter.ViewHolder> {

    private ArrayList<ChatPreview> chats;
    private Context mContext;
    private TextView username;
    private TextView body;
    private TextView date;
    private int[] picturesIDs;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public CardChatPreviewAdapter(ArrayList<ChatPreview> chats, Context context){
        this.chats = chats;
        mContext = context;
        initializePicIDArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Random random = new Random();
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_card, parent, false);
        //randomly sets the profile picture
        CircleImageView profilePic = (CircleImageView) cv.findViewById(R.id.profilePic);
        profilePic.setImageResource(picturesIDs[random.nextInt(6)]);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView cardView = holder.cardView;

        //setting the views according to the picked chat preview
        username = (TextView) cardView.findViewById(R.id.username);
        body = (TextView) cardView.findViewById(R.id.message);
        date = (TextView) cardView.findViewById(R.id.timeDate);
        username.setText(chats.get(position).username);
        body.setText(chats.get(position).message);
        date.setText(formatDate(chats.get(position).timeDate*1000));

        //setting onClick listener, so that the chat will open when a user clicks on a preview
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                //putting id and username, so ChatActivity will be able to know which conversation to load
                intent.putExtra("id", chats.get(position).id);
                intent.putExtra("username", chats.get(position).username);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * This one just saves ids of all profile pics we have
     */
    private void initializePicIDArray(){
        picturesIDs = new int[6];
        picturesIDs[0] = R.drawable.p1;
        picturesIDs[1] = R.drawable.p2;
        picturesIDs[2] = R.drawable.p3;
        picturesIDs[3] = R.drawable.p4;
        picturesIDs[4] = R.drawable.p5;
        picturesIDs[5] = R.drawable.p6;
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    /**
     * makes formatted timeDate and time form unix time
     *
     * @param unixTime
     * @return string with formatted timeDate/time
     */
    public String formatDate(long unixTime){
        Date date = new Date(unixTime);
        String formatedDate = new SimpleDateFormat("hh:mma MM/dd/yy").format(date);
        return formatedDate;
    }
}
