package fjd.com.untitledmvp.models;

import android.graphics.Bitmap;

import java.util.Map;

/**
 * Created by wzhjtn on 10/14/2015.
 */
public class MatchListItem {
    private Bitmap avatar = null;
    private String lastMessage = "";
    private String convoId = "";
    private User user = new User();

    public  MatchListItem(){};
    public  MatchListItem(Bitmap avatar, String lastMessage, User user){
        this.avatar = avatar;
        this.lastMessage = lastMessage;
        this.user = user;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public User getUser() {
        return user;
    }


}
