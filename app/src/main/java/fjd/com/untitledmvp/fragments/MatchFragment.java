package fjd.com.untitledmvp.fragments;


import android.graphics.Bitmap;
import android.os.*;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.helper.GeoQueryWrapper;
import fjd.com.untitledmvp.helper.ImageManager;
import fjd.com.untitledmvp.helper.Pair;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.util.Constants;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchFragment extends Fragment {

    private TransferUtility mTransferUtility;
    private Firebase mFBRef;
    private ImageManager mImageManager;
    private GeoQueryWrapper mGeoWrapper;
    private Boolean IS_MOCK = true;
    private Queue<String> mMatches;
    private ArrayBlockingQueue<Pair<User,String>> mSharedImageKeyQueue;
    private ArrayBlockingQueue<Pair<User, Bitmap>> mSharedBitmapQueue;
    private ImageView mProspectImage;
    private Button mBtnLike;
    private Button mBtnDisLike;
    private Pair<User, Bitmap> mCurrentProspect;
    private String TAG = "OUTPUT";
    public MatchFragment() {
        // Required empty public constructor
    }

    private void setCurrProspect(ImageView iv, Pair<User, Bitmap> pair){

            if(pair != null){
                mCurrentProspect = pair;
                Bitmap firstBitmap = pair.value;
                iv.setImageBitmap(firstBitmap);
            }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_match, container, false);
        final Handler uiHandler = new Handler();
        mProspectImage = (ImageView) v.findViewById(R.id.imageProspect);
        Button mBtnLike = (Button) v.findViewById(R.id.action_like);
        Button mBtnDisLike = (Button) v.findViewById(R.id.action_dislike);

        mMatches = mGeoWrapper.Query(0,0,0);
        Log.e(TAG, "SPAWNING");
        mImageManager.fetchCacheAsync();
        for(final String prospectUid : mMatches){

            mFBRef.child("users").child(prospectUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    user.uid = prospectUid;
                    Pair<User,String> pair = new Pair<>(user, (String) dataSnapshot.child("image").child("key").getValue());
                    mSharedImageKeyQueue.offer(pair);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }

        mBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCurrentProspect != null){
                    final Map<String, Object> update = new HashMap<>();
                    final String uid = mCurrentProspect.key.uid;
                    update.put(uid, 1);
                    mImageManager.makeAndSaveThumbnail(mCurrentProspect);

                    //add prospect to my likes
                    mFBRef.child("users")
                            .child(Constants.MOCK_UID)
                            .child("likes").updateChildren(update, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            //check prospects "like" array for me
                            mFBRef.child("users")
                                    .child(uid)
                                    .child("likes")
                                    .child(Constants.MOCK_UID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.exists()) {
                                                update.clear();
                                                String chatID = Constants.MOCK_UID + "-" + uid;
                                                String myMatchCompositeKey = chatID + "|" + uid;
                                                String prospectMatchCompositeKey = chatID + "|" + Constants.MOCK_UID;
                                                update.put("users/" + Constants.MOCK_UID + "/matches/" + myMatchCompositeKey, Constants.USE_KEY);
                                                update.put("users/" + uid + "/matches/" + prospectMatchCompositeKey, Constants.USE_KEY);
                                                update.put("messages/" + chatID, Constants.USE_KEY);
                                                mFBRef.updateChildren(update, new Firebase.CompletionListener() {
                                                    @Override
                                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                        Log.d(TAG, "Matched user " + uid);
                                                        Toast.makeText(getActivity(), "You matched with user " + uid, Toast.LENGTH_SHORT).show();
                                                    }

                                                });

                                            } else {
                                                Log.d(TAG, "Did not match user " + uid);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });

                            //if there, enter "myUid-prospectUid|prospectUid" into mine and
                            //"myUid-prospectUid|myUid" prospect's matches, with other UID always going last
                            //enter "myUid-prospectUid" into "/messages"
                            //for convo listview we split on "|" use part[1] as display name
                            //part[0] is passed to chatActivity keys message queue to listen to
                        }
                    });
                    mCurrentProspect = mSharedBitmapQueue.poll();
                    setCurrProspect(mProspectImage, mCurrentProspect);
                }

            }
        });

        mBtnDisLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentProspect != null){
                    final Map<String, Object> update = new HashMap<>();
                    update.put(Constants.MOCK_UID,1);
                    mFBRef.child("users").child(Constants.MOCK_UID).child("dislikes").updateChildren(update);
                    mCurrentProspect = mSharedBitmapQueue.poll();
                    setCurrProspect(mProspectImage, mCurrentProspect);
                }

            }
        });


        new Thread(new Runnable() {
            private Pair<User, Bitmap> _result;
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Polling from non-ui thread.");
                    mCurrentProspect = mSharedBitmapQueue.take();
                    Log.d(TAG, "DONE Polling from non-ui thread.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setCurrProspect(mProspectImage, mCurrentProspect);
                    }
                });
            }
        }).start();

        return v;
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NOTE: setting context multiple places. Should remove upon creation of base class.
        Firebase.setAndroidContext(getActivity().getApplicationContext());
        mSharedImageKeyQueue = new ArrayBlockingQueue<>(10);
        mSharedBitmapQueue = new ArrayBlockingQueue<>(10);
        mImageManager = new ImageManager(getActivity().getApplicationContext()
                                        , mSharedImageKeyQueue
                                        ,mSharedBitmapQueue);
        mGeoWrapper = new GeoQueryWrapper(IS_MOCK);
        mFBRef = new Firebase(Constants.FBURL);
    }
}
