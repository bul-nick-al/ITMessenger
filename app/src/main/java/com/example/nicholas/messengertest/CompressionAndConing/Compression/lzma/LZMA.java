package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma;

import android.provider.MediaStore;

import com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma.LZMADecoder;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma.LZMAEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.*;

import java.io.*;
import java.io.ByteArrayOutputStream;

public class LZMA {
    private static boolean diff(File alpha, File beta) throws IOException {
        FileReader frAlpha = new FileReader(alpha);
        FileReader frBeta = new FileReader(beta);

        int dataAlpha, dataBeta;
        while (true) {
            dataAlpha = frAlpha.read();
            dataBeta = frBeta.read();
            if (dataAlpha == -1 && dataBeta == -1) {
                return false;
            } else if (dataAlpha == dataBeta) {
                continue;
            } else {
                System.err.println(dataAlpha + " " + dataBeta);
                return true;
            }
        }
    }



    public static byte[] compressFile(byte[] input) throws IOException {
        InputStream in = new ByteArrayInputStream(input);
        OutputStream out = new ByteArrayOutputStream();
        LZMAEncoder encoder = new LZMAEncoder(in, out);
        encoder.code();
        return ((ByteArrayOutputStream)out).toByteArray();
    }

    public static byte[] decompressFile(byte[] input) throws IOException {
        InputStream in = new ByteArrayInputStream(input);
        OutputStream out = new ByteArrayOutputStream();
        LZMADecoder decoder = new LZMADecoder(in, out);
        decoder.code();
        return ((ByteArrayOutputStream)out).toByteArray();
    }


}
