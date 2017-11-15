package com.example.nicholas.messengertest;

import android.content.Context;
import android.net.Uri;

import com.example.nicholas.messengertest.CompressionAndConing.CodingAndCompression;
import com.f2prateek.progressbutton.ProgressButton;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nicholas on 20/10/2017.
 */

public class Message {
    public enum Type{
        text, file;
    }
    String sender;
    Type type;
    long timeDate;
    String message;
    String format;
    String path;
    File file;
    Uri uri;
    String mime;
    boolean fileAttached;
    boolean isMine;
    Context context;
    ProgressButton button;

    public Message(String sender, Type type, long timeDate, String path, File file, boolean isMine, String format, Context context) {
        this.sender = sender;
        this.type = type;
        this.timeDate = timeDate;
        this.path = path;
        this.file = file;
        this.isMine = isMine;
        this.format = format;
        this.context = context;
        processMessage();
    }

    private void processMessage() {
        if (format.contains("<M>")) {
            getMessage();
            fileAttached = false;
        } else {
            message = "File: " + path.substring(path.lastIndexOf('/') + 1);
            fileAttached = true;
        }
    }

    public String retrieveText(File file){
        try {
            return new String(decode(getBytesFromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getMessage(){
        RestClient.get(path, null, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                message = retrieveText(file);
                ((ChatActivity)context).refreshRecyclerView(true);
            }

        });
    }

    public void getFile(){
        RestClient.get(path, null, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                uri = Uri.fromFile(file);
                mime = format.substring(format.lastIndexOf("type:")+5,format.lastIndexOf('>'));
                int x = 0;
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                button.setProgressAndMax((int)(((float)bytesWritten/totalSize)*10000), 10000);
                super.onProgress(bytesWritten, totalSize);
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
