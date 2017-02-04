import com.websocket.WebSocketClientEndPoint;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by trantuanan on 2/4/17.
 */
public class TestWebsocket {
    public static void main(String[] args) throws URISyntaxException {
        SocketReader socketReader = new SocketReader();
        socketReader.setDaemon(true);
        socketReader.start();
        try {
            if (1==2){
                socketReader.interrupt();
            }
        }catch (Exception e){
            System.out.println(e);
        }
        while (1==1){
        }

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
