package fjd.com.untitledmvp.helper;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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
        _get("users/" + uid, listener);
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

    public String GetIndexString(DataSnapshot dss){
        String ret = "";
        if(dss != null){
            HashMap hm = (HashMap)dss.getValue();
            if (hm != null) {
                Set set = hm.keySet();
                if(!set.isEmpty()){
                    ret = (String) set.toArray()[0];
                }
            }
        }

        return ret;
    }

    public void GetConversationKeys(String uid, final ListCallback callback){
        getUser(uid, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapShots = dataSnapshot.child("matches").getChildren();
                Collection<DataSnapshot> convoIDs = new ArrayList<DataSnapshot>();
                for(DataSnapshot ss: snapShots){
                    convoIDs.add(ss);
                }

                ArrayList<String> listOfStrings = new ArrayList<>();
                for(DataSnapshot convoSS: convoIDs){
                    listOfStrings.add(convoSS.getKey());
                }
                callback.onListFetched(listOfStrings);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public  interface ListCallback{
        void onListFetched(ArrayList<String> list);
    }
}
