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
    //members
    private String _lastItemKey = "";
    private int _newItemCntSinceStart = 0;
    private Boolean _lastMessageMatched = false;
    protected  FirebaseManager _fbMgr = null;
    private Bundle properties = new Bundle();

    //public accessors
    public FirebaseManager GetFirebaseManager(){
        return this._fbMgr;
    }

    public void SetFirebaseManager(FirebaseManager fbm){
        this._fbMgr = fbm;
    }
    public String GetItem(){
        return this._lastItemKey;
    }

    public void SetItem(String item){
        this._lastItemKey = item;
    }

    public void PutString(String key, String value){

        this.properties.putString(key, value);
    }

    public String GetString(String key){
        return this.properties.getString(key);
    }

    //constructors
    public NewItemListener(){}

    public NewItemListener(DataSnapshot dss, FirebaseManager fbm){
        _fbMgr = fbm;
        Init(dss);
    }

    //To be overloaded
    public String TransformSnapshotValue(DataSnapshot dss){
        return (String)dss.getValue();

    }

    public Boolean IsLastItemReached(DataSnapshot currItemSnapshot){
        String currItemValue = TransformSnapshotValue(currItemSnapshot);
        return _lastItemKey.equalsIgnoreCase(currItemValue);
    }

    public void Init(DataSnapshot dss){
        if(dss!= null && dss.exists()) {
            this._lastItemKey = TransformSnapshotValue(dss);
        }
    }

    public void OnNewItem(DataSnapshot dataSnapshot, String s, int newItemCount){

    }

    //ValueEventListener interfacing
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(!this._lastMessageMatched){
            this._lastMessageMatched = IsLastItemReached(dataSnapshot);
        }
        //s being null means no previous snapshot, ie, this is first entry
        //there being no lastItem shows that this first entry is newly entered
        if((this._lastMessageMatched && s != null) || (s == null && this._lastItemKey.isEmpty())){
            this._newItemCntSinceStart++;
            OnNewItem(dataSnapshot, s, this._newItemCntSinceStart);
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

    private Boolean _isNewItemAdded(){
        return (this._lastMessageMatched && ++this._newItemCntSinceStart > 0);
    }
}