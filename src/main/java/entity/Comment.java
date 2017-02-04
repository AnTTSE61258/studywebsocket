package entity;

/**
 * Created by trantuanan on 2/4/17.
 */
public class Comment {
    long time;
    String content;

    public Comment(){

    }
    public Comment(long time, String content) {
        this.time = time;
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
