import com.websocket.WebSocketClientEndPoint;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by trantuanan on 2/4/17.
 */
public class TestWebsocket {
    public static void main(String[] args) throws URISyntaxException, IOException {
        File f = new File("bigoLiveTmp/1/sizeStorer.wt");
        String[] s = new String[]{f.getPath(),f.getPath() + "out"};
        CombineFile.combineFiles(s);



    }



}
class SocketReader extends Thread{
    @Override
    public void run() {
        try {
            // open websocket
            final WebSocketClientEndPoint clientEndPoint = new WebSocketClientEndPoint(new URI("ws://103.211.231.29:7778/wsconnect?2181151507"));

            // add listener
            clientEndPoint.addMessageHandler(new WebSocketClientEndPoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
            clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");

            // wait 5 seconds for messages from websocket

        }  catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}
