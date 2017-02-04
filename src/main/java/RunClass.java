import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.websocket.WebSocketClientEndPoint;
import entity.Comment;
import entity.LiveVideo;
import entity.SocketAndPort;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by trantuanan on 1/22/17.
 */
public class RunClass {
    // Configuration params
    public static String tabType;
    public static Double userCountRate;
    public static Integer loopSetting;
    public static String tempLocation;
    public static String getDataUrl;
    public static String videoRotation;
    public static String sToday;
    public static GetLiveVideos getLiveVideos = new GetLiveVideos();

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, UnirestException {
        sToday = DateFormatUtils.format(new Date(), "dd-MM-yyyy");
        getParams();
        int gotVideos = 0;
        while (true) {
            System.out.println("================== START TURN ==============");
            ThreadGetLiveVideo threadGetLiveVideo = new ThreadGetLiveVideo(tabType, userCountRate, loopSetting);
            threadGetLiveVideo.run();
            gotVideos++;
            System.out.println("==================END TURN==============. TOTAL = " + gotVideos);
        }
    }

    public static void getParams() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Tab type: VN/10(LOL) ");
        tabType = sc.nextLine();
        System.out.print("User count rate: (1.5) ");
        userCountRate = sc.nextDouble();
        sc.nextLine();
        System.out.print("Loop setting: (3) ");
        loopSetting = sc.nextInt();
        sc.nextLine();
        System.out.print("Temp location: (bigoLiveTmp/bigoLolTmp) ");
        tempLocation = sc.nextLine();
        System.out.print("Get data url: (LIVE/GAME) ");
        String tempUrl = sc.nextLine();
        if (tempUrl.equals("GAME")) {
            getDataUrl = "https://www.bigo.tv/openOfficialWeb/vedioList/11";
        } else {
            getDataUrl = "https://www.bigo.tv/openOfficialWeb/vedioList/5";
        }
        System.out.print("Rotation: (0:Default|1:90Degree|2:180Degree): ");
        videoRotation = sc.nextLine();

        System.out.println("\n\n\n\n\n\n\n\n======================================PARAM========================================");
        System.out.println(String.format("\ntabType = %s \nuserCountRate = %s \nloopSetting = %s \ntempLocation: %s \ngetDataUrl: %s "
                , tabType, userCountRate, loopSetting, tempLocation, getDataUrl));
        System.out.println("=============================Press enter to start= ==================================");
        sc.nextLine();
    }

    private static LiveVideo getTarget(List<LiveVideo> liveVideos, Double countRate) {
        int userCountLimit;
        int tempSum = 0;
        for (LiveVideo l : liveVideos) {
            tempSum += Integer.parseInt(l.getUser_count());
        }
        // Get limit as average
        userCountLimit = (int) (tempSum / liveVideos.size() * countRate);
        System.out.println("User count limit: " + tempSum / liveVideos.size() + " * " + countRate + " = " + userCountLimit);

        for (LiveVideo l : liveVideos) {
            File f = Utils.getVideoFile(l);
            if (!f.exists()) {
                if (Integer.parseInt(l.getUser_count()) >= userCountLimit) {
                    if ((new Date()).getTime() / 1000 - l.getTime_stamp() < 10 * 60) {
                        return l;
                    } else {
                        if (Integer.parseInt(l.getUser_count()) / userCountLimit > 1.5) {
                            System.out.println("X - High user count - This show is " + ((new Date()).getTime() / 1000 - l.getTime_stamp()) + " seconds before");
                            return l;
                        }
                        System.out.println("This show is " + ((new Date()).getTime() / 1000 - l.getTime_stamp()) + " seconds before");
                    }
                } else {
                    System.out.println("user count is smaller than " + userCountLimit);
                }
            } else {
                System.out.println(l.getBigoID() + " is exist");
            }
        }
        return null;
    }


    private static class ThreadGetLiveVideo extends Thread {
        String tabType;
        Double userCountRate;
        int loopSetting;

        public ThreadGetLiveVideo(String tabType, Double userCountRate, int loopSetting) {
            this.tabType = tabType;
            this.userCountRate = userCountRate;
            this.loopSetting = loopSetting;
        }


        @Override
        public void run() {
            try {
                final List<Comment> comments = new ArrayList<Comment>();
                List<LiveVideo> liveVideos = null;
                List<String> partFiles = new ArrayList<String>();
                try {
                    liveVideos = (getLiveVideos.getVideos(loopSetting, tabType));
                    sToday = DateFormatUtils.format(new Date(), "dd-MM-yyyy");
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("[ERROR] Check connection");
                    Thread.sleep(30*1000);
                } catch (UnirestException e) {
                    //e.printStackTrace();
                    System.out.println("[ERROR] Check connection");
                    Thread.sleep(30*1000);
                }
                if (liveVideos == null) {
                    System.out.println("[Error]. video list is empty. This case is rarely. Check connection");
                    return;
                }
                System.out.println("get list videos: " + liveVideos.size() + " Params: loopSetting = " + loopSetting);
                Collections.sort(liveVideos, new Comparator<Object>() {
                    public int compare(Object o1, Object o2) {
                        LiveVideo lv1 = (LiveVideo) o1;
                        LiveVideo lv2 = (LiveVideo) o2;
                        return (int) (lv2.getTime_stamp() - lv1.getTime_stamp());
                    }
                });

                // logic to get first
                LiveVideo target = getTarget(liveVideos, userCountRate);
                if (target == null) {
                    System.out.println("Not found target. Waiting....30s");
                    Thread.sleep(30 * 1000);
                    return;
                } else {
                    System.out.println("Target found: " + target.getBigoID() + " with user count =  " + target.getUser_count());
                    System.out.println("\n\n\n\n\n\n\n\n\n");
                }
                SocketAndPort socketAndPort = null;
                try {
                    socketAndPort = GetSocketAndPort.getSocketAndPort(target.getBigoID());
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                if (socketAndPort == null) {
                    System.out.println("[Error] Socket and port null. Something went wrong");
                    return;
                }
                int channel = (int) Long.parseLong(socketAndPort.getChannel());
                long tmp = Long.parseLong(socketAndPort.getTmp());
                DataInputStream dataInputStream = null;
                Socket socket = null;
                byte[] mainsource = new byte[0];
                try {
                    socket = new Socket(socketAndPort.getSocket(), socketAndPort.getPort());
                    System.out.println("[OK] Connect to " + socketAndPort.getSocket() + ":" + socketAndPort.getPort());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    byteBuffer.putInt(channel);
                    byteBuffer.putInt((int) tmp);
                    dataOutputStream.write(byteBuffer.array());
                    dataOutputStream.flush();
                    System.out.println("[OK] Write out channel and tmp to server");
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    boolean isNewRecord = true;
                    long lastTimeGetData = (new Date()).getTime();
                    final ObjectMapper objectMapper = new ObjectMapper();
                    String info = objectMapper.writeValueAsString(target);
                    FileUtils.writeStringToFile(Utils.getInfoFile(target.getBigoID()), info);
                    System.out.println("[OK] Write info to file");

                    // [START] Read comments
                    if (socketAndPort.getWebsocket() != null) {
                        final WebSocketClientEndPoint clientEndPoint = new WebSocketClientEndPoint(new URI(socketAndPort.getWebsocket()));
                        clientEndPoint.addMessageHandler(new WebSocketClientEndPoint.MessageHandler() {
                            private Date startVideo = new Date();

                            public void handleMessage(String message) {
                                Comment comment = Utils.getComment(message, startVideo);
                                if (comment != null) {
                                    comments.add(comment);
                                }
                            }
                        });
                    }
                    // [END] Read comments

                    while (true) {
                        if (isNewRecord) {
                            FileUtils.writeByteArrayToFile(Utils.getVideoFile(target), mainsource);
                            System.out.println("Write zero mock file. Set start time");
                            isNewRecord = false;
                        }
                        int availableBytes = dataInputStream.available();
                        byte[] temp = new byte[availableBytes];
                        dataInputStream.readFully(temp);
                        if (availableBytes != 0) {
                            if (mainsource.length == 0) {
                                System.out.println("\n");
                            }
                            System.out.print(".[" + availableBytes + "].");
                        } else {
                            System.out.print(".[D].");
                        }
                        mainsource = org.apache.commons.lang.ArrayUtils.addAll(mainsource, temp);
                        long now = new Date().getTime();
                        if (temp.length > 0) {
                            lastTimeGetData = now;
                        } else {
                            if (now - lastTimeGetData > 15 * 1000) { // 15 second
                                throw new Exception("timeout exception. Target = " + target.getBigoID());
                            }
                        }
                        // Write main source to to file and remove it.
                        if (mainsource.length > 1 * 1000 * 1000) {
                            String partFileName = Utils.getVideoFile(target) + "." + partFiles.size();
                            partFiles.add(partFileName);
                            FileUtils.writeByteArrayToFile(new File(partFileName), mainsource);
                            System.out.println("\n\n\nWrite to file. Size  = " + mainsource.length / (1000000)
                                    + "MB. Target = " + target.getBigoID() + " PartID = " + partFiles.size() + "\n");
                            mainsource = new byte[0];
                        }
                        Thread.sleep(700);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("[NA] End videos with exception: " + e.getMessage());
                    //e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Disconected");
                        e.printStackTrace();
                    }
                    try {
                        String partFileName = Utils.getVideoFile(target) + "." + partFiles.size();
                        partFiles.add(partFileName);
                        FileUtils.writeByteArrayToFile(new File(partFileName), mainsource);
                        System.out.println("Write last part to file. Size  = " + mainsource.length / (1000000) + "MB. Target = " + target.getBigoID());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // [START] Combine file

                byte[] combineFile = new byte[0];
                for (String s : partFiles) {
                    File f = new File(s);
                    byte[] partBytes = FileUtils.readFileToByteArray(f);
                    combineFile = org.apache.commons.lang.ArrayUtils.addAll(combineFile, partBytes);
                    f.delete();
                }

                FileUtils.writeByteArrayToFile(Utils.getVideoFile(target), combineFile);
                // [END] Combine file

                System.out.println("[OK] Combine file successfully. Size after combine = " + combineFile.length / (1000000) + "MB");
                // Save sub files
                String subTitle = Utils.convertToString(comments);
                FileUtils.writeStringToFile(Utils.getSubtitleFile(target.getBigoID()), subTitle);
                System.out.println("[OK] Write subtitle,srt successfullty" );
                FileUtils.writeStringToFile(Utils.getDoneFile(target.getBigoID()), "done");
                System.out.println("[OK] Write done file");
                // Run with ffmpeg
                System.out.println("[START] Re-touch video with JNI");
                Utils.convertVideo(Utils.getVideoFile(target));
                System.out.println("[END] Re-touch video with JNI");
            } catch (Exception e) {
                System.out.println("Error exception + " + e.getMessage());
            }
        }
    }
}
