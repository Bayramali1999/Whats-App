package uz.soft.whatsapp.model;

public class ChatModel {

    private String name, text, time, uid, type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatModel(String name, String text, String time, String uid, String type) {
        this.name = name;
        this.text = text;
        this.time = time;
        this.uid = uid;
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
