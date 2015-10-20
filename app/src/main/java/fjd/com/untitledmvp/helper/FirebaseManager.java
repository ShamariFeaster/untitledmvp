package fjd.com.untitledmvp.helper;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.util.Constants;

/**
 * Created by wzhjtn on 10/15/2015.
 */
public class FirebaseManager {
    private Firebase mFBRef;
    public FirebaseManager(Context context){
        Firebase.setAndroidContext(context);
        mFBRef = new Firebase(Constants.FBURL);
    }

    public Firebase fetchRef(String path){
        return mFBRef.child(path);
    }

    private void _get(String path, ValueEventListener listener){
        mFBRef.child(path).addListenerForSingleValueEvent(listener);
    }
    public void getUser(String uid, ValueEventListener listener){
        _get("users/" + uid, listener );
    }
    public void getUn(String uid, ValueEventListener listener){
        _get("users/" + uid + "/un", listener);
    }

    public void getFn(String uid, ValueEventListener listener){
        _get("users/"+uid+"/fn", listener);
    }

    public void getLastChatMessage(String convoId, ValueEventListener listener){
        mFBRef.child("messages/"+convoId).limitToLast(1).addListenerForSingleValueEvent(listener);
    }
}
