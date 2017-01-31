/**
 * Created by trantuanan on 1/22/17.
 */
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebsocketClientEndPoint {
    Session userSession = null;
    private MessageHandler messageHandler;
    public WebsocketClientEndPoint(URI endpointURI){
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            try {
                container.connectToServer(this,endpointURI);
            } catch (DeploymentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            }

    @OnOpen
    public void OnOpen(Session userSession){
        System.out.println("onOpen");
        this.userSession = userSession;
    }



    public static void main(String[] args){
        System.out.println("START");




        System.out.println("END");
    }
}
