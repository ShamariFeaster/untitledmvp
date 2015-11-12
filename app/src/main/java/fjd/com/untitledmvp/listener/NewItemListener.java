package fjd.com.untitledmvp.listener;

import android.os.Bundle;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import fjd.com.untitledmvp.helper.FirebaseManager;

/**
 * Created by WZHJTN on 11/11/2015.
 */
public class NewItemListener implements ChildEventListener {
    protected String _lastItemKey = "";
    protected int _newItemCntSinceStart = 0;
    protected Boolean _lastMessageMatched = false;
    protected  FirebaseManager _fbMgr = null;
    private Bundle properties;

    private Boolean _isNewItemAdded(){
        return (this._lastMessageMatched && this._newItemCntSinceStart++ > 0);
    }

    public NewItemListener(){}

    public NewItemListener(DataSnapshot dss, FirebaseManager fbm){
        _fbMgr = fbm;
        Init(dss);
    }



    public Boolean IsLastItemReached(DataSnapshot currItemSnapshot){
        String currItemValue = (String)currItemSnapshot.getValue();
        return _lastItemKey.equalsIgnoreCase(currItemValue);
    }

    public void Init(DataSnapshot dss){
        this._lastItemKey = (String) dss.getValue();
    }


    public Bundle GetBundle(){
        return properties;
    }

    public void SetBundle(Bundle properties){
        this.properties = properties;
    }

    public void OnNewItem(DataSnapshot dataSnapshot, String s){

    }

    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(IsLastItemReached(dataSnapshot) && _isNewItemAdded()){
            OnNewItem(dataSnapshot, s);
        }
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