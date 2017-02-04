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
    public static int numberActiveThread = 0;
    // Configuration params
    public static String tabType;
    public static Double userCountRate;
    public static Integer loopSetting;
    public static String tempLocation;
    public static String getDataUrl;
    public static String sToday;
    public static GetLiveVideos getLiveVideos = new GetLiveVideos();

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, UnirestException {
        sToday = DateFormatUtils.format(new Date(), "dd-MM-yyyy");
        getParams();
        numberActiveThread = 0;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>());
        while (true) {
            if (numberActiveThread > 0) {
                Thread.sleep(10000);
            } else {
                ThreadGetLiveVideo threadGetLiveVideo = new ThreadGetLiveVideo(tabType, userCountRate, loopSetting);
                threadPoolExecutor.execute(threadGetLiveVideo);
                numberActiveThread++;
                Thread.sleep(5000);
            }
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

        System.out.println("===========================================================================================");
        System.out.println(String.format("Run get temp files with params: " +
                        "\ntabType = %s \nuserCountRate = %s \nloopSetting = %s \ntempLocation: %s \ngetDataUrl: %s "
                , tabType, userCountRate, loopSetting, tempLocation, getDataUrl));
        System.out.println("=============================Press enter to start==========================================");
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



    public static File getMainDirectory() {
        return new File(tempLocation);
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
                    e.printStackTrace();
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                if (liveVideos == null) {
                    System.out.println("video list is null. This case is rarely");
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
                    System.out.println("target is null. STOP");
                    return;
                } else {
                    System.out.println("target is " + target.getBigoID() + " with user count =  " + target.getUser_count());
                }
                SocketAndPort socketAndPort = null;
                try {
                    socketAndPort = GetSocketAndPort.getSocketAndPort(target.getBigoID());
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                if (socketAndPort == null) {
                    System.out.println("Socket and port is null. Shit happended");
                    return;
                }
                // Step 1. Connect to server
                String serverIP = socketAndPort.getSocket();
                int serverPort = socketAndPort.getPort();
                int channel = (int) Long.parseLong(socketAndPort.getChannel());
                long tmp = Long.parseLong(socketAndPort.getTmp());
                DataInputStream dataInputStream = null;
                Socket socket = null;
                byte[] mainsource = new byte[0];
                try {
                    socket = new Socket(serverIP, serverPort);
                    System.out.println("Connected to " + serverIP + ":" + serverPort);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    byteBuffer.putInt(channel);
                    byteBuffer.putInt((int) tmp);
                    dataOutputStream.write(byteBuffer.array());
                    dataOutputStream.flush();
                    System.out.println("Write out channel and tmp to server");
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    boolean isNewRecord = true;
                    long lastTimeGetData = (new Date()).getTime();
                    // Write into
                    final ObjectMapper objectMapper = new ObjectMapper();
                    String info = objectMapper.writeValueAsString(target);
                    FileUtils.writeStringToFile(Utils.getInfoFile(target.getBigoID()), info);
                    // Read chat
                    if (socketAndPort.getWebsocket() != null) {
                        final WebSocketClientEndPoint clientEndPoint = new WebSocketClientEndPoint(new URI(socketAndPort.getWebsocket()));
                        clientEndPoint.addMessageHandler(new WebSocketClientEndPoint.MessageHandler() {
                            private Date startVideo = new Date();

                            public void handleMessage(String message) {
                                try {
                                    JsonNode jsonNode = objectMapper.readTree(message);
                                    if (jsonNode == null) {
                                        System.out.println("Error: " + message);
                                        return;
                                    }
                                    JsonNode data = jsonNode.findValue("data");
                                    if (data == null) {
                                        return;
                                    }
                                    JsonNode cNode = jsonNode.findValue("c");
                                    if (cNode==null||!cNode.asText().equals("1")){
                                        return;
                                    }


                                    JsonNode nNode = data.findValue("n");
                                    if (nNode == null) {
                                        return;
                                    }
                                    JsonNode mNode = data.findValue("m");
                                    if (mNode == null) {
                                        return;
                                    }

                                    String messageContent = mNode.textValue();
                                    if (messageContent != null && !messageContent.equals("")) {
                                        Comment comment = new Comment(new Date().getTime() - startVideo.getTime(), messageContent);
                                        comments.add(comment);
                                    }

                                } catch (IOException e) {
                                    //e.printStackTrace();
                                } catch (Exception e) {

                                }
                            }
                        });

                    }


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
                                System.out.println();
                                System.out.println();
                            }
                            System.out.print("." + availableBytes + ".");
                        } else {
                            System.out.println("Disconnected..");
                        }
                        mainsource = org.apache.commons.lang.ArrayUtils.addAll(mainsource, temp);
                        if (temp.length > 0) {
                            lastTimeGetData = (new Date()).getTime();
                        } else {
                            if ((new Date()).getTime() - lastTimeGetData > 15 * 1000) { // 15 second
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
                    e.printStackTrace();
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
                        System.out.println("Write to file. Size  = " + mainsource.length / (1000000) + "MB. Target = " + target.getBigoID());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Combine to 1 file
                System.out.println("Combine to one file start");
                byte[] combineFile = new byte[0];
                for (String s : partFiles) {
                    File f = new File(s);
                    byte[] partBytes = FileUtils.readFileToByteArray(f);
                    combineFile = org.apache.commons.lang.ArrayUtils.addAll(combineFile, partBytes);
                    f.delete();
                }

                FileUtils.writeByteArrayToFile(Utils.getVideoFile(target), combineFile);
                // Combine file

                System.out.println("Combine file successfully. Size after combine = " + combineFile.length / (1000000) + "MB");
                // Save sub files
                System.out.println("Save subtitles");
                String subTitle = Utils.convertToString(comments);
                FileUtils.writeStringToFile(Utils.getSubtitleFile(target.getBigoID()),subTitle);
                FileUtils.writeStringToFile(Utils.getDoneFile(target.getBigoID()), "done");
                // Run with ffmpeg
                Utils.convertVideo(Utils.getVideoFile(target));


                System.out.println("STOPPED. " + target.getBigoID());

            } catch (Exception e) {
                System.out.println("Error exception + " + e.getMessage());
            } finally {
                numberActiveThread--;
            }
        }
    }
}
