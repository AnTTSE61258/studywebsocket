import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import entity.LiveVideo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by trantuanan on 1/26/17.
 */
public class GetLiveVideos {

    public List<LiveVideo> getVideos(int scrollCount, String tabType) throws IOException, UnirestException {
        List<LiveVideo> result = new ArrayList<LiveVideo>();
        List<String> ignoreUids = new ArrayList<String>();
        for (int i = 0; i < scrollCount; i++) {
            LiveVideo[] liveVideos = getVideos(ignoreUids,tabType);
            if (liveVideos==null){
                continue;
            }
            for (LiveVideo item: liveVideos) {
                result.add(item);
                ignoreUids.add(item.getOwner());
            }
        }
        System.out.println("Get list video. Result = " + result.size());
        return result;
    }

    private LiveVideo[] getVideos(List<String> ignoreUids, String tabType) throws UnirestException, IOException {
        String editedIgnoreUids = prepareIgnoreUids(ignoreUids);
        HttpResponse<String> httpResponse = Unirest.post(RunClass.getDataUrl)
                .header("X-Requested-With", "XMLHttpRequest")
                .field("ignoreUids", editedIgnoreUids)
                .field("tabType", tabType).asString();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        String content = httpResponse.getBody().toString();
        try {
            LiveVideo[] liveVideos = objectMapper.readValue(content, LiveVideo[].class);
            return liveVideos;
        }catch (Exception e){
            System.out.println("[Error] exception parse response. Content = " + content);
        }
        return new LiveVideo[0];
    }

    private String prepareIgnoreUids(List<String> raw){
        String result = "";
        for (String s: raw) {
            result += "." + s;
        }
        return  result;
    }
}
