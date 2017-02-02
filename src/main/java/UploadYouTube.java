import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;
import entity.LiveVideo;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/**
 * Created by trantuanan on 1/27/17.
 */
public class UploadYouTube {
    private static YouTube youtube;
    private static final String VIDEO_FILE_FORMAT = "video/*";
    public static String license = "";
    public static FilenameFilter doneFileName = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            if (name.contains(".done")) {
                return true;
            }
            return false;
        }
    };
    public static FilenameFilter flvFileName = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.contains(".flv");
        }
    };
    public static FilenameFilter endWithFlvFileName = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith("flv");
        }
    };
    public static void main(String[] args) throws IOException {
        String sMainDirectory;
        Scanner sc = new Scanner(System.in);
        System.out.print("Upload Directory");
        sMainDirectory = sc.nextLine();
        System.out.print("License: ");
        license = sc.nextLine();

        while (true) {
            File mainDirectory = new File(sMainDirectory);
            if (mainDirectory.exists()){
                System.out.println("Directory " + mainDirectory.getName() + " is exist");
            }else {
                System.out.println("[ERROR]Directory " + mainDirectory.getName() + " is not exist");
                break;
            }
            if (mainDirectory.isDirectory()) {
                File[] files = mainDirectory.listFiles();
                System.out.println("Get list file from main directory. Size = " + files.length);
                for (File subFolder : files) {
                    if (subFolder.isDirectory()) {
                        System.out.println("Check folder " + subFolder.getName());
                        File[] doneFile = subFolder.listFiles(doneFileName);
                        // if have done file then find flv file
                        if (doneFile.length == 1) {
                            File[] flvFile = subFolder.listFiles(flvFileName);
                            if (flvFile.length == 1) {
                                if (flvFile[0].getName().endsWith(".flv")) {
                                    boolean uploadResult = uploadToYoutube(subFolder);
                                    if (uploadResult) {
                                        FileUtils.deleteDirectory(subFolder);
                                        System.out.println("Removed : " + subFolder.getName());
                                    } else {
                                        System.out.println("Upload failed: " + subFolder.getName());
                                    }
                                } else {
                                    System.out.println("One file but not main. Shit happend");
                                }
                            } else {
                                System.out.println("Error. Find flv file for ID = "
                                        + subFolder.getName() + ". Size = " + flvFile.length);
                            }
                        } else {
                            if (doneFile.length == 0) {
                                System.out.println("Video ID = " + subFolder + " still streaming");
                            } else {
                                System.out.println("There are many done file. Shit happend");
                            }
                        }
                    }
                }

            }
            try {
                System.out.println("Sleeping...");
                Thread.sleep(60 * 60 * 1000);
                System.out.println("Awake....");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean uploadToYoutube(File subFolder) {
        // This OAuth 2.0 access scope allows an application to upload files
        // to the authenticated user's YouTube channel, but doesn't allow
        // other types of access.

        FilenameFilter doneFileName = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.contains(".done")) {
                    return true;
                }
                return false;
            }
        };
        FilenameFilter flvFileName = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains(".flv");
            }
        };

        FilenameFilter jsonFileName = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains(".json");
            }
        };
        File[] flvFile = subFolder.listFiles(flvFileName);
        File[] jsonFile = subFolder.listFiles(jsonFileName);
        String extendDescription = "";
        String bigoId = "Not found";
        if (jsonFile.length == 1) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                LiveVideo liveVideo = objectMapper.readValue(jsonFile[0], LiveVideo.class);
                if (liveVideo != null && liveVideo.getData4() != null) {
                    String s = liveVideo.getData4().get("st");
                    if (s != null) {
                        extendDescription += s;
                    }
                }
                if (liveVideo != null) {
                    bigoId = liveVideo.getBigoID();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        File file = flvFile[0];
        if (file.length() < 10 * 1000 * 1000) {
            System.out.println("File too small. Just remove");
            return true;
        }
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "uploadvideo");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-uploadvideo-sample").build();

            System.out.println("Uploading: " + file.getName());
            // Add extra information to the video before uploading.
            Video videoObjectDefiningMetadata = new Video();

            // Set the video to be publicly visible. This is the default
            // setting. Other supporting settings are "unlisted" and "private."
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");
            videoObjectDefiningMetadata.setStatus(status);

            // Most of the video's metadata is set on the VideoSnippet object.
            VideoSnippet snippet = new VideoSnippet();

            // This code uses a Calendar instance to create a unique name and
            // description for test purposes so that you can easily upload
            // multiple files. You should remove this code from your project
            // and use your own standard names instead.
            Calendar cal = Calendar.getInstance();
            snippet.setTitle("Hot Bigo " + file.getName().substring(0, file.getName().length() - 4));

            snippet.setDescription(file.getName().substring(0, file.getName().length() - 4)
                    + ". Tổng hợp hot stream BIGO. Cập nhật liên tục. " + "\n -"
                    + extendDescription + "\n - Bigo ID: " + bigoId);
            if (file.getName().contains("ℕℂℂ")) {
                System.out.println("Shit NCC");
                return false;
            }
            // Set the keyword tags that you want to associate with the video.
            List<String> tags = new ArrayList<String>();
//            tags.add("bigo");
//            tags.add("hot");
//            tags.add("live");
//            tags.add("show");
            snippet.setTags(tags);

            // Add the completed snippet object to the video resource.
            videoObjectDefiningMetadata.setSnippet(snippet);

            final InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, new FileInputStream(file));

            // Insert the video. The command sends three arguments. The first
            // specifies which information the API request is setting and which
            // information the API response should return. The second argument
            // is the video resource that contains metadata about the new video.
            // The third argument is the actual video content.
            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

            // Set the upload type and add an event listener.
            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

            // Indicate whether direct media upload is enabled. A value of
            // "True" indicates that direct media upload is enabled and that
            // the entire media content will be uploaded in a single request.
            // A value of "False," which is the default, indicates that the
            // request will use the resumable media upload protocol, which
            // supports the ability to resume an upload operation after a
            // network interruption or other transmission failure, saving
            // time and bandwidth in the event of network failures.
            uploader.setDirectUploadEnabled(false);
            final long fileLength = mediaContent.getInputStream().available();
            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            System.out.println("Initiation Started");
                            break;
                        case INITIATION_COMPLETE:
                            System.out.println("Initiation Completed");
                            break;
                        case MEDIA_IN_PROGRESS:
                            System.out.println("Upload in process: "
                                    + ((double) uploader.getNumBytesUploaded() / (double) fileLength) * 100 + " %");
                            break;
                        case MEDIA_COMPLETE:
                            System.out.println("Upload Completed!");
                            break;
                        case NOT_STARTED:
                            System.out.println("Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Call the API and upload the video.
            Video returnedVideo = videoInsert.execute();

            // Print data about the newly inserted video from the API response.
            System.out.println("\n================== Returned Video ==================\n");
            System.out.println("  - Id: " + returnedVideo.getId());
            System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
            System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
            System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
            System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
            return true;
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }
}
