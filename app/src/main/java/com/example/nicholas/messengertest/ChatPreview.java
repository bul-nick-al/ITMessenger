package com.example.nicholas.messengertest;

import android.media.Image;

/**
 * Created by nicholas on 20/10/2017.
 */

public class ChatPreview {
    String username;
    int id;
    Image pic;
    String lastMessage;

    public ChatPreview(String username, int id, String lastMessage) {
        this.username = username;
        this.id = id;
        this.lastMessage = lastMessage;
    }
}
