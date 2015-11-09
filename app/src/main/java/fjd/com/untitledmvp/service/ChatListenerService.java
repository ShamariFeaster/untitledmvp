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
import java.util.HashMap;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.helper.FirebaseManager;
import fjd.com.untitledmvp.helper.Pair;
import fjd.com.untitledmvp.models.ChatMessage;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

public class ChatListenerService extends Service {
    private Boolean mIsStarted = false;
    private Firebase mFBRef = null;
    private ArrayList<Pair<Firebase, ChildEventListener>> mListeners;
    private FirebaseManager mFBManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mIsStarted == false){
            Firebase.setAndroidContext(this);
            mFBRef = new Firebase(Constants.FBURL);
            mFBManager = new FirebaseManager(this);
            mListeners = new ArrayList<>();
            //do work
            ArrayList<String> convos = intent.getStringArrayListExtra(Constants.SERVICE_CONVO_IDS);

            final Pair<Firebase, ChildEventListener> pair = new Pair<>();
            for (String convoId : convos) {
                Pair<String,String> convoObj = Util.SplitConvoKey(convoId);

                pair.key = mFBRef.child("messages/"+convoObj.value);

                ValueEventListener veListener = new FrozenValueEventListener<Firebase, ChildEventListener> (pair) {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        /*onChildAdded is called for once for every message*/
                        //this SS key is last message
                        OnlyNewMessagesListener newMsgListener =  new OnlyNewMessagesListener(dataSnapshot) {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                //fires on startup, get's entire
                                if(this._lastKey.equalsIgnoreCase(dataSnapshot.getKey())){
                                    this._lastMessageMatched = true;
                                }

                                //key() here is the parent key of the array (ie, convoID)
                                if(this._lastMessageMatched == true && this._newMsgCntSinceStart++ > 0){
                                    ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                                    Util.PostNotification(ChatListenerService.this, this._convoID, "New Message", msg.getText() , R.drawable.ic_stat_name );
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

                        this.__pair.value = this.__pair.key.addChildEventListener(newMsgListener);
                        mListeners.add(this.__pair);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                };

                mFBManager.getLastChatMessage(convoObj.value, veListener);



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

    private class OnlyNewMessagesListener implements ChildEventListener{
        protected String _lastKey = "";
        protected int _newMsgCntSinceStart = 0;
        protected Boolean _lastMessageMatched = false;
        protected String _convoID = "";

        public OnlyNewMessagesListener(DataSnapshot dss){
            _lastKey = (dss != null) ? mFBManager.GetIndexString(dss) : "";
            _convoID = dss.getKey();
        }

        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }


        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }


        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }


        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }


        public void onCancelled(FirebaseError firebaseError) {

        }
    }

    private class FrozenValueEventListener<LeftT,RightT> implements ValueEventListener{

        protected Pair<LeftT,RightT> __pair = new Pair<>();

        public FrozenValueEventListener(Pair<LeftT,RightT> pair){
            __pair.key = pair.key;
            __pair.value = pair.value;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
