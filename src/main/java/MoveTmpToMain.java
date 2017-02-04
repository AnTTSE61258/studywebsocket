import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by trantuanan on 2/4/17.
 */
public class MoveTmpToMain {

    public static void main(String[] args) {
        final String sTempDirectory;
        final String sMainDirectory;
        System.out.println("================ Move done processes from temp to main=============");
        System.out.print("temp directory:   ");
        Scanner sc = new Scanner(System.in);
        sTempDirectory = sc.nextLine();
        System.out.print("main directory:    ");
        sMainDirectory = sc.nextLine();

        File mainDirectory = new File(sMainDirectory);
        File tempDirectory = new File(sTempDirectory);

        if (tempDirectory.exists()) {
            System.out.println("Found temp directory. Size = " + tempDirectory.listFiles().length + " elements");
        }else {
            System.out.println("Not found temp directory");
            return;
        }

        while (true){
            File[] subDirectories = tempDirectory.listFiles();
            for (File s:subDirectories ) {
                if (Utils.isDone(s)){
                    try {
                        FileUtils.moveToDirectory(s,mainDirectory,true);
                        System.out.println("Moved " + s.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                System.out.println("Sleep...10 * 60 * 10000");
                Thread.sleep(10 * 60 * 10000);
                System.out.println("Awake");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


}
