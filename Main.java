import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created by AlinaCh on 07.11.2017.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path pic = Paths.get("test.mp3");
        Path res = Paths.get("result.mp3");
        byte[] array = Files.readAllBytes(pic);
        long s = System.nanoTime();
        Encode test = new Encode(array);
        //System.out.println(test.toString());
        Decode d = new Decode(test.encoded);
        System.out.println(s - System.nanoTime());
        Files.write(res, d.decoded);
        //System.out.println(d.toString());
    }
}
