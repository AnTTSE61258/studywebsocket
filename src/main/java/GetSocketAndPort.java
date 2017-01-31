import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import entity.SocketAndPort;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by trantuanan on 1/26/17.
 */
public class GetSocketAndPort {
    private static final String bigoUrl = "http://www.bigo.tv/%s";

    public static String getBigoUrl() {
        return bigoUrl;
    }

    public static SocketAndPort getSocketAndPort(String bigoId) throws UnirestException {
        HttpResponse<String> httpResponse = Unirest.get(String.format(bigoUrl,bigoId)).asString();
        String content = httpResponse.getBody().toString();
        Pattern MY_PATTERN = Pattern.compile(".*param name=\"FlashVars\".*/>");
        Matcher m = MY_PATTERN.matcher(content);
        List<String> matchResult = new ArrayList<String>();
        while (m.find()) {
            String s = m.group();
            if (!s.contains("<!--")){
               matchResult.add(s.trim());
            }

        }
        if (matchResult.size() == 0){
            return null;
        }
        String params = matchResult.get(0);
        String tmp = params.substring(params.indexOf("tmp")+4,params.indexOf("channel")-1);
        String channel = params.substring(params.indexOf("channel")+8,params.indexOf("srv")-1);
        String srv = params.substring(params.indexOf("srv")+4,params.indexOf("port")-1);
        String port = params.substring(params.indexOf("port")+5,params.length()-4);
        SocketAndPort socketAndPort = new SocketAndPort(srv,tmp,channel,Integer.parseInt(port));
        return socketAndPort;


    }
}
