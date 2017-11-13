
//package demmprogrammi1.ShannonCD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.ByteBuffer;

class ShannonFano {
    private static HashMap<Byte, Integer> distribution; // initial distribution (Byte --> how many times it occurs)
    private static int totalCount; // total amount of accepted bytes
    private static int diffCount; // amount of different bytes
    private static ArrayList<Pair> dist; // sorted distribution
    private static HashMap<Byte, String> codes; // codes itself (Byte --> Code) 
    private static byte[] input; // input bytes (uncompressed)
    private static byte[] compressed; //output 
    /*
     * Return codes for accepted distribution by Shannon-Fano compression algorithm
     */

    public static void buildCodes(int len) {
        buildCode(0, len - 1, "");
    }

    /*
     * recursively iterate through dist and split it into 2 ~ same pieces ,assigning each piece prefix + 0 / 1
     * if lenght of piece is too small to split => len 1 or 2 ,we stop iterating and assign prefix from parent 
     */
    private static void buildCode(int low, int up, String prefix) {
        if (low == up) {
            codes.put(getB(low), prefix);
            return;
        }
        if (low + 1 == up) {
            codes.put(getB(low), prefix + "0");
            codes.put(getB(low + 1), prefix + "1");
            return;
        }
        int pivot = findPivot(low, up);
        buildCode(low, pivot, prefix + "0");
        buildCode(pivot + 1, up, prefix + "1");

    }

    /*
     * reconstruct binary string from byte[] into compressed code
     */
    private static StringBuilder reconstructInput() {
        //StringBuilder buffer = new StringBuilder();
        //StringBuilder code = new StringBuilder();
        List<Byte> bytes = new ArrayList<>();
        for (byte b : input)
            bytes.add(Byte.valueOf(b));
        return new StringBuilder(bytes.stream().map(el -> codes.get(el)).collect(Collectors.joining()));

        //buffer = buffer.append(codes.get(b));

    }

    private static byte[] getCompressedBytes(StringBuilder buff) {
        while (buff.length() % 8 != 0)
            buff.append('0');
        int len = buff.length() / 8;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte map[] = new byte[1];
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(codes);
            out.flush();
            map = bos.toByteArray();
            //size = ByteBuffer.allocate(4).putInt(map.length).array();
        } catch (Exception e) {
        }
        System.out.println("FROM COMPRESSED");
        System.out.println("Hashmap size: " + map.length);
        byte output[] = new byte[len + 4 + map.length];
        output[3] = (byte) (map.length & 0xFF);
        output[2] = (byte) ((map.length >> 8) & 0xFF);
        output[1] = (byte) ((map.length >> 16) & 0xFF);
        output[0] = (byte) ((map.length >> 24) & 0xFF);

        System.arraycopy(map, 0, output, 4, map.length);

        for (int i = 0; i < len; ++i)
            output[i + 4 + map.length] = convertByte(buff.substring(i * 8, (i + 1) * 8));
        System.out.println("codes length: " + len);
        return output;
    }

    public static byte[] getCompressed(byte[] in) {
        // input = null;
        input = in;
        totalCount = input.length; // total amount of bytes
        diffCount = 0; // amount of different bytes (between 0..255)

        // get distribution over given byte array => map each byte to its frequency
        distribution = getDistribution(input);
        // array of Pairs (Byte,Frequency) in order to sort it
        dist = new ArrayList<Pair>();

        // fullfilling array
        for (Entry<Byte, Integer> entry : distribution.entrySet()) {
            dist.add(new Pair(entry.getKey(), entry.getValue()));
            ++diffCount;
        }
        //sort by frequency
        Collections.sort(dist, Collections.reverseOrder());

        //building codes for bytes
        codes = new HashMap<Byte, String>();
        buildCodes(diffCount);
        // build binary string that represents codes for input bytes
        StringBuilder binaryString = reconstructInput();
        System.out.print("binary str length: " + binaryString.length());

        //outputCodes();
        return getCompressedBytes(binaryString);
    }

    public static byte convertByte(String s) {
        return (byte) Integer.parseInt(s, 2);
    }

    public static String convertString(Byte b) {
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }

    public static byte[] getDecompressed(byte[] in) {
        // fetching size of Serialized Hashmap
        int MapSize = ByteBuffer.wrap(Arrays.copyOfRange(in, 0, 4)).getInt();
        System.out.println("FROM DECOMPRESSED");
        System.out.println("Size of hashmap: " + MapSize);
        HashMap<Byte, String> map = new HashMap<>();
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(in, 4, MapSize);
            ObjectInputStream ois = new ObjectInputStream(bis);
            map = (HashMap<Byte, String>) ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
        }
        // compressed bytes
        byte[] codes = Arrays.copyOfRange(in, MapSize + 4, in.length);
        //reversed Hashmap to decompress 
        HashMap<String, Byte> reverse = GetReversedMap(map);
        StringBuilder buffer = new StringBuilder();
        ArrayList<Byte> decompressed = new ArrayList<>();
        System.out.println("Compressed bytes: " + (in.length - MapSize - 4));
        for (byte b : codes)
            buffer.append(convertString(b));

        /*
         * Parsing binary String into sequence of bytes according to reversed Hashmap
         */
        int pos = 0;
        boolean end = false;
        while (pos < buffer.length() && !end) {
            int off = 0;
            while (reverse.get(buffer.substring(pos, pos + off)) == null)
                if (pos + off < buffer.length())
                    ++off;
                else {
                    end = true;
                    break;
                }
            if (end)
                break;
            decompressed.add(reverse.get(buffer.substring(pos, pos + off)));
            pos += off;
        }
        System.out.println(decompressed.size());
        byte[] result = new byte[decompressed.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = decompressed.get(i).byteValue();
        return result;
    }

    public static HashMap<String, Byte> GetReversedMap(HashMap<Byte, String> map) {
        HashMap<String, Byte> res = new HashMap<>();
        for (Entry<Byte, String> e : map.entrySet())
            res.put(e.getValue(), e.getKey());
        return res;
    }

    // Shortcut func to get Frequency of Byte on given index in "dist" 
    public static int getF(int index) {
        return dist.get(index).getFreq();
    }

    //shortcut func to get Byte by index
    public static Byte getB(int index) {
        return dist.get(index).getByte();
    }

    /*
     * finding pivot index in given array in order to split it into 2 ~ same piececs
     */
    public static double CodeLengthEntropy() {
        double entropy = 0;
        for (Entry<Byte, Integer> e : distribution.entrySet())
            entropy += codes.get(e.getKey()).length() * (double) e.getValue() / totalCount;
        return entropy;
    }
    // 1 + 256 (1 + 2) ~ 1 kb

    public static double Entropy() {
        double entropy = 0;
        for (Entry<Byte, Integer> e : distribution.entrySet()) {
            double prob = (double) e.getValue() / totalCount;
            entropy -= prob * Math.log(prob) / Math.log(2);
        }
        return entropy;
    }

    private static int findPivot(int l, int r) {
        int totalFreq = 0;
        int cur = 0;
        for (int i = l; i <= r; ++i)
            totalFreq += getF(i);
        for (int i = l; i <= r; ++i) {
            cur += getF(i);
            if (cur >= totalFreq / 2) {
                /* a(k) , avg, a(k+1) => choose the one who closer to avg
                -+            if (cur - totalFreq / 2 >= totalFreq / 2 - cur + getF(i) && i != l)
                    return i - 1;
                else */
                return i;
            }

        }
        return 0;
    }

    public static void outputCodes() {
        //System.out.print(codes.isEmpty());
        System.out.println("Codes: ");
        for (Entry<Byte, String> entry : codes.entrySet())
            System.out.println(entry.toString());
    }

    // get distribtuion of bytes 
    public static HashMap<Byte, Integer> getDistribution(byte[] b) {
        int len = b.length;
        double e = 0;
        HashMap<Byte, Integer> result = new HashMap<Byte, Integer>();
        for (int i = 0; i < len; ++i) {
            byte c = b[i];
            Integer val = result.get(Byte.valueOf(c));
            if (val != null) {
                result.put(c, new Integer(val + 1));
            } else {
                result.put(c, 1);
            }

        }
        return result;
    }
}