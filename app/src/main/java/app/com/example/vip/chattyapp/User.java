package app.com.example.vip.chattyapp;

public class User {
    private String userid, name, status, imageUrl, stateOnOff;

    public User(String userid, String name, String status, String imageUrl) {
        this.userid = userid;
        this.name = name;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public User(String userid, String name, String status, String imageUrl, String stateOnOff) {
        this.userid = userid;
        this.name = name;
        this.status = status;
        this.imageUrl = imageUrl;
        this.stateOnOff = stateOnOff;
    }

    public String getStateOnOff() {
        return stateOnOff;
    }

    public void setStateOnOff(String stateOnOff) {
        this.stateOnOff = stateOnOff;
    }

    public String getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
