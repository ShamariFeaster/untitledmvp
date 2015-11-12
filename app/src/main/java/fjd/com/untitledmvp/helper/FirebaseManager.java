package fjd.com.untitledmvp.helper;

import android.content.Context;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import fjd.com.untitledmvp.callback.ListCallback;
import fjd.com.untitledmvp.listener.NewItemListener;
import fjd.com.untitledmvp.util.Constants;

/**
 * Created by wzhjtn on 10/15/2015.
 */
public class FirebaseManager {
    private Firebase mFBRef;
    private FirebaseManager mThis;
    private HashMap<String,ChildEventListener> mItemListenersTable;
    public FirebaseManager(Context context){
        Firebase.setAndroidContext(context);
        mFBRef = new Firebase(Constants.FBURL);
        mThis = this;
        mItemListenersTable = new HashMap<>();
    }

    public Firebase fetchRef(String path){
        return mFBRef.child(path);
    }

    private void _get(String path, ValueEventListener listener){
        mFBRef.child(path).addListenerForSingleValueEvent(listener);
    }

    private void _getLastChild(String path, ValueEventListener listener){
        mFBRef.child(path).limitToLast(1).addListenerForSingleValueEvent(listener);
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
        if(convoId != null && !convoId.isEmpty()){
            mFBRef.child("messages/"+convoId).limitToLast(1).addListenerForSingleValueEvent(listener);
        }

    }

    public void SetNewItemListener(final String path, final NewItemListener listener){

        _getLastChild(path, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot lastItemSnapshot) {
                NewItemListener wrapperListener = new NewItemListener(lastItemSnapshot, mThis){
                    @Override
                    public void Init(DataSnapshot dss){
                        listener.Init(dss);
                    };
                    @Override
                    public Boolean IsLastItemReached(DataSnapshot currItemSnapshot){
                        return listener.IsLastItemReached(currItemSnapshot);
                    };
                    @Override
                    public void OnNewItem(DataSnapshot dataSnapshot, String s) {
                        listener.OnNewItem(dataSnapshot,s);
                    };
                };
                mItemListenersTable.put(path, listener);
                new Firebase(path).addChildEventListener(wrapperListener);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void RemoveNewItemListener(String path){
        if(mItemListenersTable.containsKey(path)){
            new Firebase(path).removeEventListener(mItemListenersTable.get(path));
        }
    }

    public ChildEventListener GetListener(String path){
        ChildEventListener ret = null;
        if(mItemListenersTable.containsKey(path)){
            ret = mItemListenersTable.get(path);
        }
        return  ret;
    }

    public void getLastMatch(String uid, ValueEventListener listener){
        if(uid != null && !uid.isEmpty()){
            mFBRef.child("users")
                    .child(uid)
                    .child("matches").limitToLast(1).addListenerForSingleValueEvent(listener);
        }

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


}
