import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import entity.LiveVideo;
import entity.SocketAndPort;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.*;
import java.net.Socket;
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
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, UnirestException {
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
    public static void getParams(){
        Scanner sc = new Scanner(System.in);
        System.out.print("Tab type: (VN) ");
        tabType = sc.nextLine();
        System.out.print("User count rate: (1.5) ");
        userCountRate = sc.nextDouble();
        System.out.print("Loop setting: (3) ");
        loopSetting = sc.nextInt();
        System.out.println(String.format("Run get temp files with params: " +
                        "\ntabType = %s \nuserCountRate = %s \nloopSetting = %s"
                , tabType, userCountRate, loopSetting));
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
            File f = getVideoFile(l);
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

    public static File getVideoFile(LiveVideo liveVideo) {
        Date today = new Date();
        String sToday = DateFormatUtils.format(today, "dd-MM-yyyy");
        return new File("bigoLiveTmp/" + liveVideo.getBigoID() + "/" + liveVideo.getNick_name() + " - live stream " + sToday + ".flv");
    }

    public static File getInfoFile(String bigoId) {
        return new File("bigoLiveTmp/" + bigoId + "/" + bigoId + ".json");
    }

    public static File getDoneFile(String bigoId) {
        return new File("bigoLiveTmp/" + bigoId + "/" + bigoId + ".done");
    }

    public static File getMainDirectory() {
        return new File("bigoLiveTmp");
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

                GetLiveVideos getLiveVideos = new GetLiveVideos();
                List<LiveVideo> liveVideos = null;
                List<String> partFiles = new ArrayList<String>();
                try {
                    liveVideos = (getLiveVideos.getVideos(loopSetting, tabType));
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
                    ObjectMapper objectMapper = new ObjectMapper();
                    String info = objectMapper.writeValueAsString(target);
                    FileUtils.writeStringToFile(getInfoFile(target.getBigoID()), info);


                    while (true) {
                        if (isNewRecord) {
                            FileUtils.writeByteArrayToFile(getVideoFile(target), mainsource);
                            System.out.println("Write zero mock file");
                            isNewRecord = false;
                        }
                        int availableBytes = dataInputStream.available();
                        byte[] temp = new byte[availableBytes];
                        dataInputStream.readFully(temp);
                        System.out.print(availableBytes + "..");
                        mainsource = org.apache.commons.lang.ArrayUtils.addAll(mainsource, temp);
                        if (temp.length > 0) {
                            lastTimeGetData = (new Date()).getTime();
                        } else {
                            if ((new Date()).getTime() - lastTimeGetData > 15 * 1000) { // 15 second
                                throw new Exception("timeout exception. Target = " + target.getBigoID());
                            }
                        }
                        // Write main source to to file and remove it.
                        if (mainsource.length > 3 * 1000 * 1000) {
                            String partFileName = getVideoFile(target) + "." + partFiles.size();
                            partFiles.add(partFileName);
                            FileUtils.writeByteArrayToFile(new File(partFileName), mainsource);
                            System.out.println("Write to file. Size  = " + mainsource.length / (1000000)
                                    + "MB. Target = " + target.getBigoID() + " PartID = " + partFiles.size());
                            mainsource = new byte[0];
                        }

                        Thread.sleep(1000);
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
                        String partFileName = getVideoFile(target) + "." + partFiles.size();
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

                FileUtils.writeByteArrayToFile(getVideoFile(target), combineFile);
                System.out.println("Combine file successfully. Size after combine = " + combineFile.length / (1000000) + "MB");


                FileUtils.writeStringToFile(getDoneFile(target.getBigoID()), "done");
                // Combine file
                System.out.println("STOPPED. " + target.getBigoID());

            } catch (Exception e) {
                System.out.println("Error exception + " + e.getMessage());
            } finally {
                numberActiveThread--;
            }
        }
    }
}
