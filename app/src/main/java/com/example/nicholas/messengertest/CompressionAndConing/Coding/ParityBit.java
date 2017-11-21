package com.example.nicholas.messengertest.CompressionAndConing.Coding;

/**
 * This class implements parity bit compression algorithm.
 */
public class ParityBit {

    /**
     * Encodes a sequence of bits by adding a parity bit after each 8 bits. The
     * parity bit is equal to 1 if the number of 1bits is odd and 0 otherwise.
     *
     * @param input byte array to be decoded
     * @return encoded byte array
     */
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
     * decodes an encoded sequence of bits by removing each 9th but and checking
     * if the parity bit and 8 previous bits correlate. If the do not, throws an exception.
     *
     * @param input encoded byte array
     * @return decoded byte array
     * @throws Exception - if a bit flip has been detected
     */
    public static byte[] decode(byte[] input) throws Exception{
        String bitString = convertToBitString(input);
        StringBuilder sb = new StringBuilder();
        int count = 0; //counts the number of ones in every subsequence of 8 bits
        for (int i = 0; i < bitString.length(); i++) {
            //here each 9th bit is checked
            if (i%9 == 8){
                //if the number of 1bits in the subsequence mod 2 is equal to the parity bit
                //everything is fine. Otherwise the bit flip is detected and an exception is thrown
                if (count%2 != Integer.parseInt(bitString.charAt(i)+""))
                    throw new Exception("parity failed");
                count = 0;
                if (i+9 >= bitString.length())
                    break;
            }
            else {
                //every other bit is just passed to the decoded sequence.
                sb.append(bitString.charAt(i));
                count = bitString.charAt(i) == '1' ? count+1 : count;
            }
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
        byte[] output = new byte[(int)Math.ceil((float) input.length()/8)];
        System.out.println(output.length);
        for (int i = 0; i < input.length(); i += 8) {
            String subStr = input.substring(i, (i + 8 <= input.length()) ? i + 8 : i + input.length()%8);
            int anInt = Integer.parseInt(subStr, 2);
            output[i / 8] =  (i + 8 <= input.length()) ? (byte)anInt : (byte)(anInt<<(8 - input.length()%8));
        }
        return output;
    }

    /**
     * Converts a byte array into a bit string
     *
     * @return a String of bits
     */
    private static String convertToBitString(byte[] input) {
        StringBuilder sb = new StringBuilder(input.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * (input.length); i++)
            sb.append((input[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
};