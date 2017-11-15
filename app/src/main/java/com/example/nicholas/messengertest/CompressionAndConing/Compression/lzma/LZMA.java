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

//    private static void roundTripTest() throws IOException {
//        File source = new File("Test.txt");
//        File compressed = new File("compressed");
//        File decompressed = new File("decompressed");
//
//        System.err.println("Source file length: " + source.length());
//
//        compressFile(source, compressed);
//        System.err.println("Compressed file length: " + compressed.length());
//
//        decompressFile(compressed, decompressed);
//        System.err.println("Decompressed file length: " + decompressed.length());
//
//        if (diff(source, decompressed)) {
//            System.err.println("Source and decompressed files are different");
//        } else {
//            System.err.println("Source and decompressed files are the same");
//        }
//    }

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

//    public static void main(String[] args) {
//        try {
//            roundTripTest();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Round trip test failed!");
//        }
//    }

//    public static byte[] compress(byte[] input) throws IOException {
//        File source = new File("temp");
//        FileUtils.writeByteArrayToFile(source, input);
//        File compressed = new File("compressed");
//        compressFile(source, compressed);
//        return getBytesFromFile(compressed);
//
//    }
//
//    public static byte[] decompress(byte[] input) throws IOException {
//        File source = new File("temp");
//        FileUtils.writeByteArrayToFile(source, input);
//        File decompressed = new File("decompressed");
//        decompressFile(source, decompressed);
//        return getBytesFromFile(decompressed);
//
//    }
//
//    public static byte[] getBytesFromFile(File file) throws IOException {
//        // Get the size of the file
//        long length = file.length();
//
//        // You cannot create an array using a long type.
//        // It needs to be an int type.
//        // Before converting to an int type, check
//        // to ensure that file is not larger than Integer.MAX_VALUE.
//        if (length > Integer.MAX_VALUE) {
//            // File is too large
//            throw new IOException("File is too large!");
//        }
//
//        // Create the byte array to hold the data
//        byte[] bytes = new byte[(int)length];
//
//        // Read in the bytes
//        int offset = 0;
//        int numRead = 0;
//
//        InputStream is = new FileInputStream(file);
//        try {
//            while (offset < bytes.length
//                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
//                offset += numRead;
//            }
//        } finally {
//            is.close();
//        }
//
//        // Ensure all the bytes have been read in
//        if (offset < bytes.length) {
//            throw new IOException("Could not completely read file "+file.getName());
//        }
//        return bytes;
//    }
}
