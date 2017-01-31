package entity;

/**
 * Created by trantuanan on 1/26/17.
 */
public class SocketAndPort {
    private String socket;
    private String tmp;
    private String channel;
    private int port;

    public SocketAndPort(String socket, String tmp, String channel, int port) {
        this.socket = socket;
        this.tmp = tmp;
        this.channel = channel;
        this.port = port;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
