package fjd.com.untitledmvp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import fjd.com.untitledmvp.helper.FirebaseManager;
import fjd.com.untitledmvp.helper.Pair;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

public class ChatListenerService extends Service {
    private Boolean mIsStarted = false;
    private Firebase mFBRef = null;
    private ArrayList<Pair<Firebase, ChildEventListener>> mListeners;
    private FirebaseManager mFBManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Firebase.setAndroidContext(this);
        mFBRef = new Firebase(Constants.FBURL);
        mFBManager = new FirebaseManager(this);

        if(mIsStarted == false){
            //do work
            ArrayList<String> convos = intent.getStringArrayListExtra("convoIds");
            final Pair<Firebase, ChildEventListener> pair = new Pair<>();

            for (String convoId : convos) {
                Pair<String,String> convoObj = Util.SplitConvoKey(convoId);
                pair.key = mFBRef.child(convoObj.value);

                mFBManager.getLastChatMessage(convoObj.key, new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        MyChildEventListener listener =  new MyChildEventListener(dataSnapshot.getKey()) {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                //fires on startup, get's entire
                                if(_lastKey.equalsIgnoreCase(dataSnapshot.getKey())){
                                    _lastMessageMatched = true;
                                }

                                if(_lastMessageMatched == true){
                                    _newMsgCntSinceStart++;
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        };

                        pair.value = pair.key.addChildEventListener(listener);
                        mListeners.add(pair);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });



            }

            mIsStarted = true;
        }else{

        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsStarted = false;
        for(Pair<Firebase, ChildEventListener> pair: mListeners){
            pair.key.removeEventListener(pair.value);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyChildEventListener implements ChildEventListener{
        protected String _lastKey = "";
        protected int _newMsgCntSinceStart = 0;
        protected Boolean _lastMessageMatched = false;

        public MyChildEventListener(String lastKey){
            _lastKey = lastKey;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }

}
