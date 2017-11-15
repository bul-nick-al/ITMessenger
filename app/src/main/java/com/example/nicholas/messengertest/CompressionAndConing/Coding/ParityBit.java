package com.example.nicholas.messengertest.CompressionAndConing.Coding;

/**
 * Created by nicholas on 11/11/2017.
 */

public class ParityBit {
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
//        System.out.println(sb.toString());
        return convertToByteArray(sb.toString());
    }

    /**
     * Converts a string representing bits into a byte array
     *
     * @param input String to be converted into byte array
     * @return a byte array
     */
    private static byte[] convertToByteArray(String input) {
        byte[] output = new byte[(int)Math.ceil((float) input.length()/8)+2];
        System.out.println(output.length);
        for (int i = 0; i < input.length(); i += 8) {
            String subStr = input.substring(i, (i + 8 <= input.length()) ? i + 8 : i + input.length()%8);
            int anInt = Integer.parseInt(subStr, 2); //used for avoiding size mismatch
            output[i / 8] =  (i + 8 < input.length()) ? (byte)anInt : (byte)(anInt<<(8 - input.length()%8));
        }
        return output;
    }

    public static byte[] decode(byte[] input) throws Exception{
        String bitString = convertToBitString(input);
//        System.out.println(bitString);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < bitString.length(); i++) {
            if (i%9 == 8){
                if (count%2 != Integer.parseInt(bitString.charAt(i)+""))
                    throw new Exception("parity failed");
                count = 0;
                if (i+9 >= bitString.length())
                    break;
            }
            else {
                sb.append(bitString.charAt(i));
                count = bitString.charAt(i) == '1' ? count+1 : count;
            }
        }
//        System.out.println(sb.toString().length());
//        System.out.println(sb.toString());
        return convertToByteArray(sb.toString());
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