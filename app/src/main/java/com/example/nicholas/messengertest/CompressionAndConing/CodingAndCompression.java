package com.example.nicholas.messengertest.CompressionAndConing;

import com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming.HammingDecode;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming.HammingEncode;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.ParityBit;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.Repetition;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman.HuffmanDecoder;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman.HuffmanEncoder;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.ShannonFano.ShannonFano;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.LZMA;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class provides an unified interface for encoding/decoding and compressing/decompressing data in
 * the byte array form.
 */

public class CodingAndCompression {
    //enum for coding choice
    public enum Coding {
        repetition, hamming, parity
    }
    //enum for compression choice
    public enum Compression {
        shannon, lzm, huffman
    }

    /**
     * Provides a unified interface for encoding and compression
     * @param input a byte array to be encoded and compressed
     * @param compression which compression algorithm to use
     * @param coding which encoding algorithm to use
     * @return encoded and compressed data
     */
    public static byte[] compressAndEncode(byte[] input, Compression compression, Coding coding){
        byte[] output = input;
        //compression with the chosen algorithm
        switch (compression){
            case shannon:
                output = ShannonFano.getCompressed(output);
                break;
            case lzm:
                try {
                    output = LZMA.compressFile(output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case huffman:
                try {
                    output = getBytes(HuffmanEncoder.getEncodedFile(new ByteArrayInputStream(output)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //encoding with the chosen algorithm
        switch (coding){
            case repetition:
                output = Repetition.encode(output);
                break;
            case hamming:
                output = new HammingEncode(output).encoded;
                break;
            case parity:
                output = ParityBit.encode(output);
                break;
        }
        return output;
    }

    /**
     * Provides a unified interface for decoding and decompression
     * @param input a byte array to be decoded and decompressed
     * @param compression which decompression algorithm to use
     * @param coding which decoding algorithm to use
     * @return decoded and decompressed data
     */
    public static byte[] decodeAndDecompress(byte[] input, Compression compression, Coding coding) throws Exception {
        byte[] output = input;
        //decoding with the chosen algorithm
        switch (coding){
            case repetition:
                output = Repetition.decode(output);
                break;
            case hamming:
                output = new HammingDecode(output).decoded;
                break;
            case parity:
                output = ParityBit.decode(output);
                break;
        }
        //decompression with the chosen algorithm
        switch (compression){
            case shannon:
                output = ShannonFano.getDecompressed(output);
                break;
            case lzm:
                output = LZMA.decompressFile(output);
                break;
            case huffman:
                output = getBytes(HuffmanDecoder.decode(getArrayListBytes(output)));
        }
        return output;
    }


    public static byte[] getBytes(ArrayList<Byte> list){
        byte[] result = new byte[list.size()];
        for(int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static ArrayList<Byte> getArrayListBytes(byte[] array){
        ArrayList<Byte> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            result.add(array[i]);
        }
        return result;
    }


}
