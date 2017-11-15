package com.example.nicholas.messengertest;


import android.content.Context;

import com.example.nicholas.messengertest.CompressionAndConing.CodingAndCompression;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nicholas on 20/10/2017.
 * This class represents a single message preview
 */

public class ChatPreview {
    Context context;
    String username;
    String format;
    int id;
    long timeDate;
    String path;
    String message;

    public ChatPreview(int id, String path, long timeDate, Context context, String format) {
        this.id = id;
        this.path = path;
        this.timeDate = timeDate;
        this.context = context;
        this.format = format;
        setUsername();
        processMessage();
    }

    public String retrieveText(File file){
        try {
            return new String(decode(getBytesFromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processMessage(){
        if (format.contains("<M>")){
            getFile();
        }
        else {
            message = "File: "+path.substring(path.lastIndexOf('/')+1);
        }
    }

    private void setUsername(){
        RestClient.get("/api/user/index?id="+id, null, new JsonHttpResponseHandler()  {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                try {
                    username = response.getJSONArray("userlist").getJSONObject(0).getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ((MainActivity)context).refreshRecyclerView();
            }
        });
    }

    private void getFile(){
        RestClient.get(path, null, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                int x = 0;
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                message = retrieveText(file);
                ((MainActivity)context).refreshRecyclerView();
            }

        });
    }
    public  byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        return bytes;
    }

    public byte[] decode(byte[] input) throws Exception {
        CodingAndCompression.Compression compression = null;
        CodingAndCompression.Coding coding = null;
        if (format.contains("<huffman>"))
            compression = CodingAndCompression.Compression.huffman;
        if (format.contains("<lzm>"))
            compression = CodingAndCompression.Compression.lzm;
        if (format.contains("<shannon>"))
            compression = CodingAndCompression.Compression.shannon;
        if (format.contains("<repetition>"))
            coding = CodingAndCompression.Coding.repetition;
        if (format.contains("<parity>"))
            coding = CodingAndCompression.Coding.parity;
        if (format.contains("<hamming>"))
            coding = CodingAndCompression.Coding.hamming;
        return CodingAndCompression.decodeAndDecompress(input, compression, coding);
    }
}
