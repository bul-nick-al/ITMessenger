package com.example.nicholas.messengertest.CompressionAndConing.Coding.Hamming;

import java.util.BitSet;

import static java.lang.Math.pow;

/**
 * Created by AlinaCh on 07.11.2017.
 */
public class HammingDecode {

    final int R = 4, M = 8, leng = 12;
    public byte[] decoded;
    BitSet encoded;
    int length, errorBit;

    public HammingDecode(byte[] message) {
        length = message.length;
        decoded = new byte[length / 2];
        algorithm(message);
    }

    private void algorithm(byte[] message) {
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

    private void findError() {
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
            if (encoded.get(parity - 1) != isOdd(parityBit))
                errorBit += parity;
        }
    }

    private byte decode() {
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

    private void fromByteToBinary(byte[] message) {
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

    private byte fromBinaryToByte(BitSet input) {
        byte res = 0;
        for (int i = 0; i < M; i++) {
            if (input.get(i)) {
                res |= 1 << (7 - i % 8);
            }
        }
        return res;
    }

    private boolean isOdd(int parity) {
        return parity % 2 != 0;
    }


    public boolean isOne(char input) {
        return input == '1';
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < length / 2; i++)
            s += decoded[i] + " ";
        return s;
    }

    public void printBitSet(BitSet toPrint, int len) {
        String s = "";
        for (int i = 0; i < len; i++)
            s += (toPrint.get(i) ? 1 : 0);
        System.out.println(s);
    }
}
