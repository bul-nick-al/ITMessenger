import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File("two.tif"));

            System.out.println(System.currentTimeMillis() / 1000);
            ArrayList<Byte> encoded = HuffmanEncoder.getEncodedFile(stream);
            System.out.println(System.currentTimeMillis() / 1000);
            ArrayList<Byte> bytes = HuffmanDecoder.decode(encoded);
            System.out.println(System.currentTimeMillis() / 1000);
            System.out.println(HuffmanEncoder.bytes.size());
            System.out.println(bytes.size());
        }
        catch (IOException e) {

        }
    }
}

