import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Created by AnTT5 on 2/6/2017.
 */
public class CombineFile {
    public static void combineFiles(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Syntax: infiles... outfile");
        }
        Path outFile = Paths.get(args[args.length - 1]);
        System.out.println("TO " + outFile);
        FileChannel out = FileChannel.open(outFile, CREATE, WRITE);
        for (int ix = 0, n = args.length - 1; ix < n; ix++) {
            Path inFile = Paths.get(args[ix]);
            System.out.println(inFile + "...");
            FileChannel in = FileChannel.open(inFile, READ);
            for (long p = 0, l = in.size(); p < l; )
                p += in.transferTo(p, l - p, out);
            in.close();
        }
        out.close();

        System.out.println("DONE.");
    }
}
