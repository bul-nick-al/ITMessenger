package com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming;

import java.util.BitSet;

import static java.lang.Math.*;

/**
 * Created by AlinaCh on 07.11.2017.
 */
public class HammingEncode {

    final static int R = 4, M = 8, leng = 12;
    static int length;
    static BitSet basic;
    public static byte[] encoded;

    public HammingEncode(byte[] message) {
        length = message.length;
        encoded = new byte[length * 2];
        algorithm(message);
    }

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

    private static boolean isOdd(int parity) {
        return parity % 2 != 0;
    }

    public static byte[] toByteSequence() {
        byte[] res = new byte[2];
        for (int i = 0; i < leng; i++) {
            if (basic.get(i)) {
                res[i / 8] |= 1 << (7 - i % 8);
            }
        }
        return res;
    }

    public static boolean isOne(char input) {
        return input == '1';
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < length; i++)
            s += (basic.get(i) ? 1 : 0);
        return s;
    }
}

