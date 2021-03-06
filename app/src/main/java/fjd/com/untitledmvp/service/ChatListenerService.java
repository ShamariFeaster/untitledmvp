package fjd.com.untitledmvp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import fjd.com.untitledmvp.listener.ValueEventListenerClosure;
import fjd.com.untitledmvp.listener.NewItemListener;
import fjd.com.untitledmvp.models.ChatMessage;

import fjd.com.untitledmvp.receiver.ServiceReceiver;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

public class ChatListenerService extends Service {

    private Boolean mIsStarted = false;
    private ArrayList<Pair<Firebase, ChildEventListener>> mListeners;
    private Context mCtx;
    private ServiceReceiver mServiceReceiver;
    public int mNotificationId = 0;

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCtx = this;

        if(!mIsStarted){
            Firebase.setAndroidContext(this);
            Firebase fbRef = new Firebase(Constants.FBURL);
            final FirebaseManager fbManager = new FirebaseManager(this);
            mListeners = new ArrayList<>();
            mServiceReceiver = new ServiceReceiver();
            mCtx.registerReceiver(mServiceReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
            //do work
            ArrayList<String> convos = intent.getStringArrayListExtra(Constants.SERVICE_CONVO_IDS);

            final HashMap<String,String> serializedCurrUserObj
                    = (HashMap)intent.getSerializableExtra(Constants.CURR_USER_KEY);

            final Pair<Firebase, ChildEventListener> referenceToListenerPair = new Pair<>();
            for (String compoundConvoInfo : convos) {

                Pair<String,String> ConvoParticipantUidToconvoIdPair
                        = Util.SplitConvoKey(compoundConvoInfo);

                referenceToListenerPair.key
                        = fbRef.child("messages/" + ConvoParticipantUidToconvoIdPair.value);

                ValueEventListenerClosure newMessagesListener
                        = generateListenerClosureThatBindsAndCaches(
                        referenceToListenerPair, serializedCurrUserObj, mListeners, fbManager
                );

                fbManager.getLastChatMessage(
                        ConvoParticipantUidToconvoIdPair.value, newMessagesListener);

            }



            if(serializedCurrUserObj.containsKey("uid")){
                String uid = serializedCurrUserObj.get("uid");
                String path = "users/"+uid+"/matches";
                final Firebase ref = fbRef.child(path);

                fbManager.SetNewItemListener(path, new NewItemListener() {

                    @Override
                    public void Init(DataSnapshot dss){
                        SetItem(GetFirebaseManager().GetHashTableItem(dss));
                    }

                    @Override
                    public String TransformSnapshotValue(DataSnapshot dss){
                        return dss.getKey();
                    }

                    @Override
                    public void OnNewItem(DataSnapshot dataSnapshot, String s, int newItemCnt) {
                        Pair<String, String> matcheeUidToConvoIdPair
                                = Util.SplitConvoKey(dataSnapshot.getKey());
                        fbManager.getFn( matcheeUidToConvoIdPair.key , new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String fn = (String)dataSnapshot.getValue();
                                    Bundle extras = new Bundle();
                                    extras.putString(Constants.BC_NEW_MATCH_EXTRAS_MATCH_FN, fn);
                                    extras.putInt(Constants.BC_NEW_MATCH_EXTRAS_ICON, R.drawable.ic_stat_name);
                                    Util.BroadcastEvent(mCtx, Constants.BROADCAST_NEW_MATCH, extras);
                                }

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                    }
                });

                mListeners.add(new Pair<>(ref,fbManager.GetNewItemListener(path)));


            }

            mIsStarted = true;
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsStarted = false;
        cleanupExistingListeners(mListeners);
        mCtx.unregisterReceiver(mServiceReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void cleanupExistingListeners(ArrayList<Pair<Firebase, ChildEventListener>> listeners){
        for(Pair<Firebase, ChildEventListener> refToListenerPair: mListeners){
            refToListenerPair.key.removeEventListener(refToListenerPair.value);
        }
        listeners.clear();
    }

    @SuppressWarnings("unchecked")
    private NewItemListener generateOnlyNewMessageListener(
            DataSnapshot lastSnapshotAsOfNow,
            final HashMap<String,String> serializedCurrUserObj,
            FirebaseManager fbManager){

        return new NewItemListener(lastSnapshotAsOfNow, fbManager) {

            @Override
            public void Init(DataSnapshot dss){
                if(dss != null){
                    SetItem(GetFirebaseManager().GetHashTableItem(dss));
                   PutString(Constants.BC_NEW_MSG_EXTRAS_CONVO_ID, dss.getKey());
                }
            }

            @Override
            public Boolean IsLastItemReached(DataSnapshot currItemSnapshot){
                return (GetItem().equalsIgnoreCase(currItemSnapshot.getKey()));
            }

            @Override
            public void OnNewItem(DataSnapshot dataSnapshot, String s, int newItemCnt) {
                final ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);

                Bundle extras = new Bundle();
                extras.putString(Constants.BC_NEW_MSG_EXTRAS_CONVO_ID, GetString(Constants.BC_NEW_MSG_EXTRAS_CONVO_ID));
                extras.putString(Constants.BC_NEW_MSG_EXTRAS_TEXT, msg.getText());
                extras.putSerializable(Constants.BC_NEW_MSG_EXTRAS_USER, serializedCurrUserObj);
                extras.putString(Constants.BC_NEW_MSG_EXTRAS_SENDER, msg.getSender());
                extras.putInt(Constants.BC_NEW_MSG_EXTRAS_ICON, R.drawable.ic_stat_name);
                Util.BroadcastEvent(mCtx, mServiceReceiver,Constants.BROADCAST_NEW_MESSAGE,extras);
            }

        };

    }

    private ValueEventListenerClosure generateListenerClosureThatBindsAndCaches(
            final Pair<Firebase, ChildEventListener> referenceListenerPair,
            final HashMap<String, String> serializedCurrUserObj,
            final ArrayList<Pair<Firebase, ChildEventListener>> referenceToListenerPairCache,
            final FirebaseManager fbManager){

        return new ValueEventListenerClosure<Firebase, ChildEventListener>
                (referenceListenerPair, serializedCurrUserObj) {

            final HashMap<String,String> serializedUserObj = this.__serializedCurrUserObj;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //pair.key is reference, pair.value is listener
                NewItemListener newMsgListener
                        = generateOnlyNewMessageListener(dataSnapshot, serializedUserObj, fbManager);

                if(this.__pair.key != null){
                    this.__pair.value = this.__pair.key.addChildEventListener(newMsgListener);
                    referenceToListenerPairCache.add(this.__pair);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        };

    }


}
