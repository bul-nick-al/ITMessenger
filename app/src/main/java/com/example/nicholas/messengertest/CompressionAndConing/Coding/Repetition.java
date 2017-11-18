package com.example.nicholas.messengertest.CompressionAndConing.Coding;

import java.util.Arrays;

/**
 * Created by Nastya on 10.11.2017.
 */
public class Repetition {
    final static int numOfRepetitions = 3;
    static boolean foundError; //used for error detecting

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

    /**
     * Splits a string onto substrings of a length specified in the interval
     *
     * @param string   arbitrary input string
     * @param interval integer that repreaents the size of each substring
     * @return array of strings
     */
    private static String[] splitString(String string, int interval) {
        int length = (int) Math.ceil(string.length() / (double) interval);//rounding to get the minimum possible legth of the array
        String[] output = new String[length];
        int i = 0;
        for (int j = 0; j < output.length - 1; j++) {
            output[j] = string.substring(i, i + interval);
            i += interval;
        }
        output[output.length - 1] = string.substring(i);
        return output;
    }

    /**
     * Returns the most frequent value in the array
     *
     * @param input char array
     * @return the most frequent char
     */
    private static char vote(char[] input) {
        Arrays.sort(input);
        if (input[0] != input[2]) {
            foundError = true;
        }
        return input[input.length / 2];
    }

    /**
     * Encodes the input byte array using repetition code
     *
     * @param input byte array
     * @return encoded byte array
     */
    public static byte[] encode(byte[] input) {
        byte[] encoded;
        String bitString = convertToBitString(input);
        System.out.println("Encoded bitstring's length is " + bitString.length());
        System.out.println("Encoded bitstring is " + bitString);
        StringBuilder sb = new StringBuilder(bitString.length() * numOfRepetitions);
        for (char character : bitString.toCharArray()) {
            char[] chars = new char[numOfRepetitions];
            //Assigning the char value of character from bitString to each element of the array of chars of size 3.
            Arrays.fill(chars, character);
            sb.append(chars);
        }
        System.out.println("Encoded outputs's length is " + sb.length());
        System.out.println("Encoded output string is " + sb.toString());
        encoded = convertToByteArray(sb.toString());
        return encoded;
    }

    /**
     * Decodes the encoded byte array by major voting the repeated sequence of bits
     *
     * @param input encoded byte array
     * @return decoded byte array
     */
    public static byte[] decode(byte[] input) {
        byte[] decoded;
        String bitString = convertToBitString(input);
        // System.out.println("Bitstring is " + bitString);
        String[] repStrings = splitString(bitString, numOfRepetitions);
        StringBuilder sb = new StringBuilder(repStrings.length);
        for (String repetition : repStrings) {
            sb.append(vote(repetition.toCharArray()));
        }
        decoded = convertToByteArray(sb.toString());
        return decoded;
    }

}
