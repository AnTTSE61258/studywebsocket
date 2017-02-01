import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import entity.LiveVideo;

import java.io.IOException;
import java.util.ArrayList;
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
            for (LiveVideo item: liveVideos) {
                result.add(item);
                ignoreUids.add(item.getOwner());
            }
        }
        return result;
    }

    private LiveVideo[] getVideos(List<String> ignoreUids, String tabType) throws UnirestException, IOException {
        String editedIgnoreUids = prepareIgnoreUids(ignoreUids);
        System.out.println("Send request");
        HttpResponse<JsonNode> httpResponse = Unirest.post(RunClass.getDataUrl)
                .header("X-Requested-With", "XMLHttpRequest")
                .field("ignoreUids", editedIgnoreUids)
                .field("tabType", tabType).asJson();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        String content = httpResponse.getBody().toString();
        System.out.println("Receive response");
        LiveVideo[] liveVideos = objectMapper.readValue(content, LiveVideo[].class);
        System.out.println("Parse successfully");
        return liveVideos;
    }

    private String prepareIgnoreUids(List<String> raw){
        String result = "";
        for (String s: raw) {
            result += "." + s;
        }
        return  result;
    }
}
