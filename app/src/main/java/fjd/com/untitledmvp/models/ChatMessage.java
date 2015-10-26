package fjd.com.untitledmvp.models;

import java.util.Date;

/**
 * Created by WZHJTN on 10/7/2015.
 */
public class ChatMessage {
    private long timestamp = 0;
    private  String text = "Say Hello!";
    private String sender = "";

    public ChatMessage(){};

    public  ChatMessage(String sender, String text){
        this.timestamp = new Date().getTime();
        this.text = text;
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }
}
