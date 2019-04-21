package app.com.example.vip.chattyapp;

public class MessageObject {

    String  fromId ,message, type, date, time;
    public MessageObject() {
    }

    public MessageObject(String fromId, String message, String type, String date, String time) {
        this.fromId = fromId;
        this.message = message;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
