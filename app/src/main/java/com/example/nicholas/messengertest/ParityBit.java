package com.example.nicholas.messengertest;

import java.util.BitSet;
import java.util.IllegalFormatConversionException;

/**
 * Created by nicholas on 11/11/2017.
 */

public class ParityBit {
    private static final int BYTE_SIZE = 8;

    public static byte[] encode(byte[] input){
        StringBuilder sb = new StringBuilder(input.length * 9);
        for (byte anInput : input) {
            int count = 0;
            for (int j = 0; j < 8; j++) {
                if ((anInput << j & 0x80) != 0)
                    count++;
                sb.append((anInput << j & 0x80) == 0 ? '0' : '1');
            }
            sb.append(count % 2 == 1 ? '1' : '0');
        }
        return convertToByteArray(sb.toString());
    }

    /**
     * Converts a string representing bits into a byte array
     *
     * @param input String to be converted into byte array
     * @return a byte array
     */
    private static byte[] convertToByteArray(String input) {
        byte[] output = new byte[input.length() / 8];
        for (int i = 0; i < input.length(); i += 8) {
            String subStr = input.substring(i, i + 8);
            int anInt = Integer.parseInt(subStr, 2); //used for avoiding size mismatch
            output[i / 8] = (byte) anInt;
        }
        return output;
    }

    public static byte[] decode(byte[] input) throws Exception{
        String bitString = convertToBitString(input);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < bitString.length(); i++) {
            if (i%9 == 8){
                if (count%2 == Integer.parseInt(bitString.charAt(i)+""))
                    throw new Exception("parity failed");
            }
            else {
                sb.append(bitString.charAt(i));
                count = bitString.charAt(i) == '1' ? count+1 : count;
            }
        }
        return null;
    }

    /**
     * Converts a byte array into a bit string
     *
     * @return a String of bits
     */
    private static String convertToBitString(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * input.length; i++)
            sb.append((input[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
};