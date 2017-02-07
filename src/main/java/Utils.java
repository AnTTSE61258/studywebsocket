import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Comment;
import entity.LiveVideo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by trantuanan on 2/4/17.
 */
public class Utils {
    public static boolean isDone(File subDirectory) {
        // Check is there any done files
        File doneFile = getDoneFile(subDirectory);
        File[] flvFiles = getFlvFiles(subDirectory);
        File endWithFlvFiles = getEndWithFlvFiles(subDirectory);
        if (doneFile == null) {
            return false;
        } else if (endWithFlvFiles == null) {
            return false;
        } else if (flvFiles.length != 1) {
            return false;
        }
        return true;


    }

    public static File getDoneFile(File subDirectory) {
        File[] donefiles = subDirectory.listFiles(UploadYouTube.doneFileName);
        if (donefiles.length == 1) {
            return donefiles[0];
        } else {
            if (donefiles.length > 1) {
                System.out.println("Error: done file length" + donefiles.length + " " + subDirectory.getName());
            }
            return null;
        }
    }

    public static File[] getFlvFiles(File subDirectory) {
        File[] flvFiles = subDirectory.listFiles(UploadYouTube.flvFileName);
        return flvFiles;
    }

    public static File getEndWithFlvFiles(File subDirectory) {
        File[] endWithFlvFiles = subDirectory.listFiles(UploadYouTube.endWithFlvFileName);
        ;
        if (endWithFlvFiles.length == 1) {
            return endWithFlvFiles[0];
        } else {
            if (endWithFlvFiles.length > 1) {
                System.out.println("Error: end with flv length " + endWithFlvFiles + " " + subDirectory.getName());
            }
            return null;
        }
    }

    public static String convertToString(List<Comment> comments) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < comments.size() - 1; i++) {
            Comment c = comments.get(i);
            Date startDate = new Date(c.getTime());
            Date endDate;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String sContent = c.getContent();
            int j = i;
            while (comments.size() > j + 1 && comments.get(j + 1).getTime() - c.getTime() < 3000) {
                sContent += "\n" + comments.get(j + 1).getContent();
                i++;
                j++;
            }
            if (i == j) {
                endDate = new Date(c.getTime() + 3000);
            } else {
                endDate = new Date(comments.get(j + 1).getTime());
            }

            String sStartDate = timeFormat.format(startDate);
            String sEndDate = timeFormat.format(endDate);
            stringBuffer.append(i);
            stringBuffer.append("\n");
            stringBuffer.append(sStartDate + " --> " + sEndDate);
            stringBuffer.append("\n");
            stringBuffer.append(sContent);
            stringBuffer.append("\n");
            stringBuffer.append("\n");

            System.out.println(sStartDate + " --> " + sEndDate);

        }
        return stringBuffer.toString();

    }

    //        File f = new File("bigoLolTmp/H699669/♡》Bom《♡ - live stream 04-02-2017.flv");

    public static final void convertVideo(File f) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] convertSubtitle = new String[]{
                "ffmpeg",
                "-i",
                f.getParentFile().getCanonicalPath() + "\\" + "subtitle.srt",
                "-y",
                f.getParentFile().getCanonicalPath() + "\\" + "subtitle.ass"
        };
        System.out.println("[JNI] [START] convert subtitle");
        Process convert = rt.exec(convertSubtitle);
        try {
            convert.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File subtitle = new File(f.getParentFile().getCanonicalPath() + "/" + "subtitle.ass");
        System.out.println("[JNI] [END] convert subtitle. File status = " + subtitle.exists());
        if (!subtitle.exists() && subtitle.length() != 0) {
            System.out.println("[ERROR] Have content bute can't convert");
            return;
        } else {
            //System.out.println("[NA] No subtitles");
        }
        String ass = "";
        if (subtitle.exists()) {
            ass = "ass=" + f.getParentFile().getCanonicalPath() + "\\" + "subtitle.ass";
        }


        String transponse;
        if (RunClass.videoRotation == null || RunClass.videoRotation.equals("0")) {
            transponse = "";
        } else {
            transponse = "transpose=" + RunClass.videoRotation;
            if (!ass.equals("")) {
                transponse = transponse + ", ";
            }
        }


        String[] commandArray = new String[]{
                "ffmpeg",
                "-i",
                f.getCanonicalPath(),
                "-vf",
                transponse +
                        ass.replaceAll("\\\\","\\\\\\\\\\\\\\\\").replaceAll(":","\\\\\\\\:"),
                "-y",//overwrite
                "-q:v",// quaility
                "18",
                f.getParentFile().getCanonicalPath() + "\\" + f.getName().substring(0, f.getName().lastIndexOf('.')) + "_p.flv"
        };
        System.out.println("[JNI][START] add subtitle, rotate, and convert to mp4");


        try
        {
            Process proc = rt.exec(commandArray);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            System.out.println("<ERROR>");
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            System.out.println("</ERROR>");
            int exitVal = proc.waitFor();
            System.out.println("Process exitValue: " + exitVal);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println(Arrays.toString(commandArray));
        System.out.println("[JNI][END] add subtitle, rotate, and convert to mp4");
    }

    public static String execCmd(String[] cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static Comment getComment(String message, Date startVideo) {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (IOException e) {
            // e.printStackTrace();
            return null;
        }
        if (jsonNode == null) {
            System.out.println("Error: " + message);
            return null;
        }
        JsonNode data = jsonNode.findValue("data");
        if (data == null) {
            return null;
        }
        JsonNode cNode = jsonNode.findValue("c");
        if (cNode == null || !cNode.asText().equals("1")) {
            return null;
        }


        JsonNode nNode = data.findValue("n");
        if (nNode == null) {
            return null;
        }
        JsonNode mNode = data.findValue("m");
        if (mNode == null) {
            return null;
        }
        String name = nNode.textValue().substring(0, Math.min(nNode.textValue().length(), 10));
        String threeDot = nNode.textValue().length() > 10 ? "..." : "";
        String messageContent = name + threeDot + ":" + mNode.textValue();
        if (messageContent != null && !messageContent.equals("")) {
            Comment comment = new Comment(new Date().getTime() - startVideo.getTime(), messageContent);
            return comment;
        }
        return null;
    }

    //       Get File and Folder
    public static File getVideoFile(LiveVideo liveVideo) {
        return new File(RunClass.tempLocation + "/" + liveVideo.getBigoID() + "/" + liveVideo.getNick_name() + " - live stream " + RunClass.sToday + ".flv");
    }

    public static File getInfoFile(String bigoId) {
        return new File(RunClass.tempLocation + "/" + bigoId + "/" + bigoId + ".json");
    }

    public static File getDoneFile(String bigoId) {
        return new File(RunClass.tempLocation + "/" + bigoId + "/" + bigoId + ".done");
    }

    public static File getSubtitleFile(String bigoId) {
        return new File(RunClass.tempLocation + "/" + bigoId + "/" + "subtitle.srt");
    }

    public static File getMainDirectory() {
        return new File(RunClass.tempLocation);
    }
    //        Get file and Folder

}
