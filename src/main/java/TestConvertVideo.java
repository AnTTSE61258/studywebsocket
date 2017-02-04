import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by trantuanan on 2/4/17.
 */
public class TestConvertVideo {
    public static void main(String[] args) throws IOException {
        File f = new File("bigoLolTmp/H699669/♡》Bom《♡ - live stream 04-02-2017.flv");

        Runtime rt = Runtime.getRuntime();
        rt.exec("cd " + f.getParentFile().getCanonicalPath());
        String[] convertSubtitle = new String[]{
            "ffmpeg",
                "-i",
                f.getParentFile().getCanonicalPath()+"/"+"subtitle.srt",
                f.getParentFile().getCanonicalPath()+"/"+"subtitle.ass"
        };

        rt.exec(convertSubtitle);
        File subtitle = new File(f.getParentFile().getCanonicalPath()+"/"+"subtitle.ass");

        System.out.println("Convert done. File exist = " +subtitle.exists());
        if (!subtitle.exists()){
            System.out.println("Can't convert subtitle. Exist");
            return;
        }

        String[] commandArray = new String[]{
                "ffmpeg",
                "-i",
                f.getCanonicalPath(),
                "-vf",
                "transpose=2" + ", ass=" + f.getParentFile().getCanonicalPath() + "/" + "subtitle.ass",
                "-y",//overwrite
                "-q:v",// quaility
                "10",
//                "-vf",
//                "ass="+f.getParentFile().getCanonicalPath()+"/"+"subtitles.ass",

                f.getParentFile().getCanonicalPath() + "/" + f.getName().substring(0, f.getName().lastIndexOf('.')) + ".mp4"
        };
        Process proc = rt.exec(commandArray);


        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

    }


}
