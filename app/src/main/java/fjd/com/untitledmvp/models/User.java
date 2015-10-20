package fjd.com.untitledmvp.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WZHJTN on 10/5/2015.
 */
public class User {
    private String un;
    private String fn;
    private String ln;
    private String email;
    private Map<String, Object> likes = new HashMap<String, Object>();
    private Map<String, Object> dislikes = new HashMap<String, Object>();
    private Map<String, Object> matches = new HashMap<String, Object>();
    private Map<String, Object> prefs = new HashMap<String, Object>();
    public String uid;


    public void setImageKey(String key) {
        this.image.put("key", key);
    }

    private Map<String, Object> image = new HashMap<String, Object>();
    public User(){}

    public User(String un,String fn,String ln,String email){
        this.un = un;
        this.fn = fn;
        this.ln = ln;
        this.email = email;
        this.likes.put("a", "b");
        this.dislikes.put("a", "b");
        this.matches.put("a", "b");
        this.prefs.put("radius", -1);
        this.prefs.put("age_lower",18 );
        this.prefs.put("age_upper",25 );
        this.image.put("key", "-1");
        this.image.put("timestamp", "-1");
    }
    public String  getUn(){ return this.un; }

    public String getFn(){ return this.fn;}

    public String getLn(){ return this.ln;}

    public String getEmail(){ return this.email;}
    public  Map<String, Object> getMatches(){ return this.matches;}
    public  Map<String, Object> getPrefs(){return this.prefs;}
    public Map<String, Object> getImage() {return this.image;}
    public Map<String, Object> getLikes() {
        return likes;
    }

    public Map<String, Object> getDislikes() {
        return dislikes;
    }

}
