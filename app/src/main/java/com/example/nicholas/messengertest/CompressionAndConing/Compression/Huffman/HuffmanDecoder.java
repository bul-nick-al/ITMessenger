package com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman;
import java.util.ArrayList;
import java.util.TreeMap;

public class HuffmanDecoder {
    //bytes for every code
    static TreeMap<String, Integer> codes;

    //all bytes of stream
    static ArrayList<Byte> bytes;

    //how many bytes for word used to transfer an alphabet
    static int bytesForWord;

    //if sum of codes is not multiple of 8, add some bits to the end to transform it to bytes
    static int bitsInTheEnd;

    /**
     * decodes alphabet
     */
    private static void getAlphabet() {
        for (int i = 0; i < 256; i++) {
            String s = "";

            int byteNumber = i * (bytesForWord + 1) + 2;

            for (int j = 0; j < bytesForWord; j++) {
                Byte part = bytes.get(byteNumber + j);
                s += Integer.toBinaryString((part & 0xFF) + 0x100).substring(1);
            }

            int length = bytes.get(byteNumber + bytesForWord);

            if (length != 0) {
                String code = s.substring(s.length() - length, s.length());
                codes.put(code, i);
            }
        }
    }

    /** decode bytes itself
     * @return arraylist of that bytes
     */
    private static ArrayList<Byte> decodeText() {
        ArrayList<Byte> arr = new ArrayList<>();

        int i = 2 + (bytesForWord + 1) * 256;
        String buffer = "";

        while(i < bytes.size()) {
            buffer += Integer.toBinaryString((bytes.get(i++) & 0xFF) + 0x100).substring(1);

            if (i == bytes.size())
                buffer = buffer.substring(0, buffer.length() - bitsInTheEnd);

            for (int j = 0; j <= buffer.length(); j++) {
                if (codes.get(buffer.substring(0, j)) != null) {
                    arr.add((byte)(int)codes.get(buffer.substring(0, j)));
                    buffer = buffer.substring(j, buffer.length());
                    j = 0;
                }
            }
        }

        return arr;
    }

    /**decodes list of bytes
     * @param b arraylist of bytes
     * @return arraylist of decoded bytes
     */
    public static ArrayList<Byte> decode(ArrayList<Byte> b) {
        codes = new TreeMap<>();
        bytes = b;

        bytesForWord = bytes.get(0);
        bitsInTheEnd = bytes.get(1);

        getAlphabet();

        return decodeText();
    }
}

