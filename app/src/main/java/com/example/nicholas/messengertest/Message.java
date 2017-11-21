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
 * This class represents a message, which content may either be a text or a file
 */

public class Message {
    long timeDate;
    String message;
    String format;
    String path;
    Uri uri;
    String mime;
    boolean fileAttached;
    boolean isMine;
    Context context;
    ProgressButton button;

    public Message(long timeDate, String path, boolean isMine, String format, Context context) {
        this.timeDate = timeDate;
        this.path = path;
        this.isMine = isMine;
        this.format = format;
        this.context = context;
        processMessage();
    }

    /**
     * Finds out whether the message is a file or a text message and launches the corresponding
     * algorithm
     */
    private void processMessage() {
        if (format.contains("<M>")) {
            getMessage();
            fileAttached = false;
        } else {
            message = "File: " + path.substring(path.lastIndexOf('/') + 1);
            fileAttached = true;
        }
    }

    /**
     * downloads text message
     */
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

    /**
     * If this method is called, the input file contains a coded and compressed text message.
     * The text is retrieved from the file and returns as a string
     *
     * @param file
     * @return
     */
    public String retrieveText(File file){
        try {
            return new String(decode(getBytesFromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * downloads the content of the message from the server
     */
    public void getFile(){
        RestClient.get(path, null, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                uri = Uri.fromFile(file);
                mime = format.substring(format.lastIndexOf("type:")+5,format.lastIndexOf('>'));
            }
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                //displays the progress of file download
                button.setProgressAndMax((int)(((float)bytesWritten/totalSize)*10000), 10000);
                super.onProgress(bytesWritten, totalSize);
            }
        });
    }

    /**
     * Gets data from a file and converts it to a byte array
     *
     * @param file
     * @return
     * @throws IOException
     */
    private   byte[] getBytesFromFile(File file) throws IOException {
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

    /**
     * Gets the information about coding and compression algorithms used for the content of the
     * message from the <code>format</code> string received from the server with the message
     * and decodes and decompresses the content using the corresponding algorithms.
     *
     * @param input
     * @return
     * @throws Exception
     */
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
