package com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming;

import java.util.BitSet;

import static java.lang.Math.*;

/**
 * Created by AlinaCh on 07.11.2017.
 */
public class HammingEncode {

    final static int R = 4, M = 8, leng = 12;   //R - number of parity bits, M - number of bits, leng - length of the coded bits
    static int length;  //length of encoded message
    static BitSet basic;    //helping variabled for encoding
    public static byte[] encoded;   //encoded message

    /**
     * constructor of encoding message
     * calls the algorithm
     * @param message
     */
    public HammingEncode(byte[] message) {
        length = message.length;
        encoded = new byte[length * 2];
        algorithm(message);
    }

    /**
     * method to encode the message by byte
     * @param message
     */
    private static void algorithm(byte[] message) {
        for (int i = 0; i < message.length; i++) {
            basic = new BitSet(leng);
            fromByteToBinary(message[i]);
            encode();
            byte[] result = toByteSequence();
            encoded[2 * i] = result[0];
            encoded[2 * i + 1] = result[1];
        }
    }

    /**
     * method to encode message
     * counts the parity bits for the byte
     * counts the number of 1's, if the number is odd, than parity bit is 1
     * else parity bit is zero
     *
     * for parity bits:
     *  #1 - check 1, skip 1
     *  #2 - check 2, skip 2
     *  #4 - check 4, skip 4
     *  #8 - check 8, skip 8
     */
    public static void encode() {
        for (int i = 1; i <= R; i++) {
            int parityBit = 0;
            int parity = (int) pow(2, i - 1);
            for (int j = parity - 1; j < leng; j += (parity + parity)) {
                for (int k = 0; k < parity; k++) {
                    if (j + k < leng)
                        parityBit = parityBit + (basic.get(j + k) ? 1 : 0);
                }
            }
            basic.set(parity - 1, isOdd(parityBit));
        }
    }

    /**
     * convert from bytes to binary
     * @param input
     */
    private static void fromByteToBinary(byte input) {
        int j = 1;
        char[] ch = String.format("%8s", Integer.toBinaryString(input & 0xFF)).replace(' ', '0').toCharArray();
        int k = 0;
        while (k < ch.length) {
            if ((j & (j - 1)) != 0) {
                basic.set(j - 1, isOne(ch[k]));
                k++;
            } else {
                basic.set(j - 1, false);
            }
            j++;
        }
    }

    /**
     * @param parity
     * @return whether the number of 1's is odd or not
     */
    private static boolean isOdd(int parity) {
        return parity % 2 != 0;
    }

    /**
     * converts to byte from the binary
     * @return
     */
    public static byte[] toByteSequence() {
        byte[] res = new byte[2];
        for (int i = 0; i < leng; i++) {
            if (basic.get(i)) {
                res[i / 8] |= 1 << (7 - i % 8);
            }
        }
        return res;
    }

    /**
     * @param input
     * @return true if the bit is 1
     */
    public static boolean isOne(char input) {
        return input == '1';
    }
}

