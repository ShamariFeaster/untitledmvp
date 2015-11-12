package fjd.com.untitledmvp.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.models.ChatMessage;
import fjd.com.untitledmvp.models.MatchListItem;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.state.GlobalState;
import fjd.com.untitledmvp.util.Constants;

/**
 * Created by wzhjtn on 10/14/2015.
 */
public class MatchListAdapter extends FirebaseListAdapter<String> {
    private Firebase mFBRef;
    private FirebaseManager mFBManager;
    private ImageManager mImageManager;
    public MatchListAdapter(Firebase FBRef, Class<String> mModelClass, int mLayout, Activity activity) {

        super(FBRef.child("users")
                .child(((GlobalState) activity.getApplicationContext()).getCurrUid())
                .child("matches"), mModelClass, mLayout, activity);
        mImageManager = new ImageManager(activity.getApplicationContext());
        mFBManager = new FirebaseManager(activity.getApplicationContext());
    }

    @Override
    protected void populateView(View v, String model) {

        //model is a composit key. pull uid from key(after the "|" char)
        //fetch avatar from cache (using uid)
        //fetch User object (using uid)
        //using chat roomm ID (b4 "|"), pull last message
        final TextView tvUsername = (TextView) v.findViewById(R.id.match_item_username);
        final TextView tvLastMessage = (TextView) v.findViewById(R.id.match_item_last_message);
        final ImageView ivAvatar = (ImageView) v.findViewById(R.id.match_item_avatar);

        String[] parts = model.split("\\|");
        if(parts.length == 2){

            mFBManager.getUser(parts[1], new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = (User) dataSnapshot.getValue(User.class);
                    tvUsername.setText(user.getFn());
                    String imageKey = (String) user.getImage().get("key");
                    Bitmap bm = mImageManager.retrieveThumbnail(imageKey);
                    ivAvatar.setImageBitmap(bm);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            /*
            mFBManager.getFn(parts[1], new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvUsername.setText((String)dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            */
            //set tag as convo ID
            v.setTag(parts[0]);

            mFBManager.getLastChatMessage(parts[0], new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> shell = (HashMap) dataSnapshot.getValue();
                    if (shell != null) {
                        String key = (String) ((HashMap) shell).keySet().toArray()[0];
                        Object a = shell.get(key);

                        if (shell != null) {
                            tvLastMessage.setText((String) ((HashMap) a).get("text"));
                        }
                    }

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }


    }


}
