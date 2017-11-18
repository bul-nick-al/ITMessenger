package com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming;

import java.util.BitSet;
import static java.lang.Math.pow;

/**
 * Created by AlinaCh on 07.11.2017.
 */
public class HammingDecode {

    final static int R = 4, M = 8, leng = 12;   //R - number of parity bits, M - number of bits, leng - length of the coded bits
    public static byte[] decoded;   //decoded message
    static BitSet encoded;  //helping variabled for decoding
    static int length, errorBit;    //length of encoded message; errorBit - index, where the error is
    public static boolean mistake;  //flag to check if the mistake was found

    /**
     * constructor of decoding message
     * calls the algorithm
     * @param message
     */
    public HammingDecode(byte[] message) {
        length = message.length;
        mistake = false;
        decoded = new byte[length / 2];
        algorithm(message);
    }

    /**
     * method to decode the message by byte
     * @param message
     */
    private static void algorithm(byte[] message) {
        for (int i = 0; i < length; i += 2) {
            byte[] temp = new byte[2];
            temp[0] = message[i];
            temp[1] = message[i + 1];
            encoded = new BitSet(leng);
            fromByteToBinary(temp);
            findError();
            decoded[i / 2] = decode();
        }
    }

    /**
     * method to decode
     * works as the encoder: calculates number of 1's,
     * then checks with the parity bit;
     * if the parity bit is not equal to the gotten result -> adds parity bit index to the errorBit, sets flag mistake to true
     * if not -> continues
     *
     * for parity bits:
     *  #1 - check 1, skip 1
     *  #2 - check 2, skip 2
     *  #4 - check 4, skip 4
     *  #8 - check 8, skip 8
     */
    private static void findError() {
        for (int i = 1; i <= R; i++) {
            int parityBit = 0;
            int parity = (int) pow(2, i - 1);
            for (int j = parity - 1; j < leng; j += (parity + parity)) {
                for (int k = 0; k < parity; k++) {
                    if (j + k < leng)
                        parityBit += (encoded.get(j + k) ? 1 : 0);
                }
            }
            parityBit -= (encoded.get(parity - 1) ? 1 : 0);
            if (encoded.get(parity - 1) != isOdd(parityBit)) {
                errorBit += parity;
                mistake = true;
            }
        }
    }

    /**
     * method to delete paratiy bits, and fix the mistake
     * if index of the bit equals to the error bit -> this bit was flipped
     * then calls method to change from bit to bytes
     * @return
     */
    private static byte decode() {
        BitSet dec = new BitSet(M);
        int j = 0;
        for (int i = 0; i < leng; i++) {
            if ((i & (i + 1)) != 0) {
                if (i != errorBit - 1)
                    dec.set(j, encoded.get(i));
                else
                    dec.set(j, !encoded.get(i));
                j++;
            }
        }
        return fromBinaryToByte(dec);
    }

    /**
     * convert bytes into binary
     * @param message
     */
    private static void fromByteToBinary(byte[] message) {
        int j = 0;
        for(int i = 0; i < message.length; i++) {
            char[] ch = String.format("%8s", Integer.toBinaryString(message[i] & 0xFF)).replace(' ', '0').toCharArray();
            int k = 0;
            while (k < ch.length) {
                if (j < leng) {
                    encoded.set(j, isOne(ch[k]));
                } else
                    break;
                k++;
                j++;
            }
        }
    }

    /**
     * convert from binary to bytes
     * @param input
     * @return
     */
    private static byte fromBinaryToByte(BitSet input) {
        byte res = 0;
        for (int i = 0; i < M; i++) {
            if (input.get(i)) {
                res |= 1 << (7 - i % 8);
            }
        }
        return res;
    }

    /**
     * @param parity
     * @return whether the counted number of 1's is odd or not
     */
    private static boolean isOdd(int parity) {
        return parity % 2 != 0;
    }

    /**
     * @param input
     * @return whether the bit is 1 or not
     */
    public static boolean isOne(char input) {
        return input == '1';
    }
}
