package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by trantuanan on 1/26/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveVideo {
    private String room_id;
    private String sid;
    private String owner;
    private String room_name;
    private String user_count;
    private long time_stamp;
    private int is_locked;
    private int public_id;
    private int room_flag;
    private String room_topic;
    private String cn;
    private String country;
    private String cover_m;
    private String data1;
    private Map<String, String> data2;
    private Map<String, String> data4;
    private String data5;
    private String loc;
    private String locswitch;
    private String nick_name;
    private String version;
    private String show_type;
    private String bigoID;

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getUser_count() {
        return user_count;
    }

    public void setUser_count(String user_count) {
        this.user_count = user_count;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public int getIs_locked() {
        return is_locked;
    }

    public void setIs_locked(int is_locked) {
        this.is_locked = is_locked;
    }

    public int getPublic_id() {
        return public_id;
    }

    public void setPublic_id(int public_id) {
        this.public_id = public_id;
    }

    public int getRoom_flag() {
        return room_flag;
    }

    public void setRoom_flag(int room_flag) {
        this.room_flag = room_flag;
    }

    public String getRoom_topic() {
        return room_topic;
    }

    public void setRoom_topic(String room_topic) {
        this.room_topic = room_topic;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCover_m() {
        return cover_m;
    }

    public void setCover_m(String cover_m) {
        this.cover_m = cover_m;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public Map<String, String> getData2() {
        return data2;
    }

    public void setData2(Map<String, String> data2) {
        this.data2 = data2;
    }

    public Map<String, String> getData4() {
        return data4;
    }

    public void setData4(Map<String, String> data4) {
        this.data4 = data4;
    }

    public String getData5() {
        return data5;
    }

    public void setData5(String data5) {
        this.data5 = data5;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLocswitch() {
        return locswitch;
    }

    public void setLocswitch(String locswitch) {
        this.locswitch = locswitch;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShow_type() {
        return show_type;
    }

    public void setShow_type(String show_type) {
        this.show_type = show_type;
    }

    public String getBigoID() {
        return bigoID;
    }

    public void setBigoID(String bigoID) {
        this.bigoID = bigoID;
    }
}
