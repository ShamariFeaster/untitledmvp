package fjd.com.untitledmvp.listener;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import fjd.com.untitledmvp.helper.Pair;

/**
 * Created by WZHJTN on 11/11/2015.
 */
public class ValueEventListenerClosure<LeftT,RightT> implements ValueEventListener {

    protected Pair<LeftT,RightT> __pair = new Pair<>();
    protected HashMap<String, String> __serializedCurrUserObj = new HashMap<>();

    public ValueEventListenerClosure(Pair<LeftT, RightT> pair, HashMap<String, String> serializedUserObj){
        __pair.key = pair.key;
        __pair.value = pair.value;
        __serializedCurrUserObj = serializedUserObj;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}