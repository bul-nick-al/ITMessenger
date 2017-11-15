package com.example.nicholas.messengertest.CompressionAndConing;

import com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming.HammingDecode;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming.HammingEncode;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.ParityBit;
import com.example.nicholas.messengertest.CompressionAndConing.Coding.Repetition;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman.HuffmanDecoder;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman.HuffmanEncoder;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.ShannonFano.ShannonFano;
import com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.LZMA;
//import com.example.nicholas.messengertest.CompressionAndConing.Compression.ShannonFano.ShannonFano;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nicholas on 14/11/2017.
 */

public class CodingAndCompression {
    public enum Coding {
        repetition, hamming, parity
    }
    public enum Compression {
        shannon, lzm, huffman
    }

    public static byte[] compressAndEncode(byte[] input, Compression compression, Coding coding){
        byte[] output = input;
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

    public static byte[] decodeAndDecompress(byte[] input, Compression compression, Coding coding) throws Exception {
        byte[] output = input;
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
