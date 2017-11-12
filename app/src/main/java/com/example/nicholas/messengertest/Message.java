package com.example.nicholas.messengertest;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by nicholas on 20/10/2017.
 */

public class Message {
    public enum Type{
        text, file;
    }
    String sender;
    Type type;
    long date;
    String body;
    File file;
    boolean isMine;

    public Message(String sender, Type type, long date, String body, File file, boolean isMine) {
        this.sender = sender;
        this.type = type;
        this.date = date;
        this.body = body;
        this.file = file;
        this.isMine = isMine;
    }
}
