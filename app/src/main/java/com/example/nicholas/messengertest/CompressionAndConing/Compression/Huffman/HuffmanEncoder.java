package com.example.nicholas.messengertest.CompressionAndConing.Compression.Huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class HuffmanEncoder {
    //vector of nodes. used to create a tree of probabilities
    //static PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    static PriorityQueue<Node> nodes = new PriorityQueue<>(10, new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            if (o1.value < o2.value)
                return -1;
            return 1;
        }
    });

    //codes for bytes
    static TreeMap<Integer, String> codes;

    //bytes from stream
    static ArrayList<Integer> bytes = new ArrayList<>();

    //how many bytes for word used to transfer an alphabet
    static int bytesForWord = 0;

    //if sum of codes is not multiple of 8, add some bits to the end to transform it to bytes
    static int bitsInTheEnd = 0;

    //probabilities of bytes
    static int probabilities[] = new int[256];

    /** handles new file for encoding
     * @param stream where to read bytes from
     */
    private static void handleNewText(InputStream stream) throws IOException{
        bytes = getBytes(stream);
        probabilities = new int[256];

        calculateCharIntervals(nodes);
        buildTree(nodes);
        generateCodes(nodes.peek(), "");
    }

    /** encodes file itself
     * @return arraylist of encoded bytes
     */
    private static ArrayList<Byte> encodeText() {
        ArrayList<Byte> arr = new ArrayList<>();

        String s = "";
        for (int b: bytes) {
            s += codes.get(b);
            while (s.length() >= 8){
                String newByte = s.substring(0, 8);
                arr.add((byte)Integer.parseInt(newByte, 2));
                s = s.substring(8, s.length());
            }
        }

        if (!s.equals("")) {
            while (s.length() < 8) {
                s += "0";
                bitsInTheEnd++;
            }
            arr.add((byte)Integer.parseInt(s, 2));
        }

        return arr;
    }

    /** translate code for every byte into bytes
     * @return arraylist of that bytes
     */
    private static ArrayList<Byte> generateAlphabet() {
        //length of maximum code word
        int maxCodeLength = 0;
        for (String s :codes.values())
            if (s.length() > maxCodeLength)
                maxCodeLength = s.length();

        bytesForWord = maxCodeLength / 8 + ((maxCodeLength % 8 == 0) ? 0 : 1);

        ArrayList<Byte> arr = new ArrayList<>();

        for (int i = 0; i < 256; i++) {
            String s = codes.get(i);

            if (s != null) {
                String word = s.substring(0, s.length());
                String prefix = "";
                for (int j = bytesForWord * 8 - word.length(); j > 0; j--) {
                    prefix = "0" + prefix;
                }

                word = prefix + word;

                int index = 0;
                while (index < word.length()) {
                    arr.add((byte) Integer.parseInt(word.substring(index, index + 8), 2));
                    index += 8;
                }

                arr.add((byte) s.length());
            }
            else {
                for (int j = 0; j <= bytesForWord; j++)
                    arr.add((byte)0);
            }
        }
        return arr;
    }

    /** builds tree of nodes of codes
     * @param vector where to write nodes
     */
    private static void buildTree(PriorityQueue<Node> vector) {
        while (vector.size() > 1)
            vector.add(new Node(vector.poll(), vector.poll()));
    }

    /** calculates probabilities for bytes
     * @param vector where to write probabilities
     */
    private static void calculateCharIntervals(PriorityQueue<Node> vector) {

        for (int i = 0; i < bytes.size(); i++) {
            probabilities[bytes.get(i)]++;
        }

        for (int i = 0; i < probabilities.length; i++)
            if (probabilities[i] > 0) {
                vector.add(new Node(probabilities[i] / (bytes.size() * 1.0), i));
            }
    }

    /** creates codes for bytes. recursive function
     * @param node
     * @param s
     */
    private static void generateCodes(Node node, String s) {
        if (node != null) {
            if (node.right != null)
                generateCodes(node.right, s + "1");

            if (node.left != null)
                generateCodes(node.left, s + "0");

            if (node.left == null && node.right == null)
                codes.put(node.b, s);
        }
    }

    /** reads bytes from the stream
     * @param stream where to read from
     * @return arraylist of returned bytes
     */
    private static ArrayList<Integer> getBytes(InputStream stream) throws IOException {
        ArrayList<Integer> arr = new ArrayList<>();
        /*String s = "qwerty qwerty qwerty";
        byte a[] = s.getBytes();
        for (int i = 0; i < a.length; i++)
            arr.add((int)a[i]);*/
        try {
            int b = stream.read();
            while (b != -1) {
                arr.add(b);
                b = stream.read();
            }
        } catch (IOException e) {
            throw new IOException("Incorrect read from stream.");
        }
        return arr;
    }

    /** encodes stream of bytes
     * @param stream where to get bytes from
     * @return arraylist of encoded bytes
     */
    static public ArrayList<Byte> getEncodedFile(InputStream stream) throws IOException{
        codes = new TreeMap<>();
        handleNewText(stream);

        ArrayList<Byte> result = new ArrayList<>();
        ArrayList<Byte> alphabet = generateAlphabet();
        ArrayList<Byte> code = encodeText();

        result.add((byte) bytesForWord);
        result.add((byte) bitsInTheEnd);

        result.addAll(alphabet);
        result.addAll(code);

        return result;
    }
}
