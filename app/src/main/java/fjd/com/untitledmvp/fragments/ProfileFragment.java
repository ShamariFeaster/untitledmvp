package fjd.com.untitledmvp.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.auth.BasicAWSCredentials;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import fjd.com.untitledmvp.R;

import fjd.com.untitledmvp.state.GlobalState;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath = "";
    private String mNewPhotoPath = "";
    private ImageView mImageView = null;
    private String mImagefileName = "";
    final private String TAG = "ProfileFragment";
    final private String MY_BUCKET = "untitled-mvp-images";
    private TransferUtility mTransferUtility = null;
    private View mProgress;
    private Firebase mFBRef;
    private GlobalState mState;
    private String mUid;
    private CropImageView mCropImageView;
    private Button mCropButton;
    private Button mTakePicButton;
    public ProfileFragment() { }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createTempImageFile(null);
                mNewPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "ERROR creating image. " + ex.getMessage());
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createTempImageFile(String filename) throws IOException{
        if(filename == null){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            mImagefileName = imageFileName;
        }else{
            mImagefileName = filename;
        }

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"untitled");

        if (!storageDir.exists() && !storageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        File image = File.createTempFile(
                mImagefileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }


    private void uploadToS3(final Bitmap bm){
        File croppedImgFile = null;
        OutputStream outStream = null;

        try {
            croppedImgFile = createTempImageFile(null);
            outStream = new FileOutputStream(croppedImgFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TransferObserver observer = mTransferUtility.upload(
                MY_BUCKET,     /* The bucket to upload to */
                mImagefileName,    /* The key for the uploaded object */
                croppedImgFile        /* The file where the data to upload exists */
        );

        observer.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something

                if(state == TransferState.IN_PROGRESS){
                }

                if(state == TransferState.COMPLETED){
                    Toast.makeText(ProfileFragment.this.getActivity(), "Profile Pic saved", Toast.LENGTH_SHORT).show();
                    Map<String, Object> imageMeta = new HashMap<String, Object>();
                    imageMeta.put("key", mImagefileName);
                    imageMeta.put("timestamp", new Date().getTime());
                    mFBRef.updateChildren(imageMeta);
                    mState.Cache.set(mImagefileName, bm);
                }


            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d(TAG, "S3 ERROR: Upload failed");
            }

        });

    }

    private void toggleImageModes(){
        if(mTakePicButton.getVisibility() == View.GONE){
            mTakePicButton.setVisibility(View.VISIBLE);
        }else{
            mTakePicButton.setVisibility(View.GONE);
        }

        if(mImageView.getVisibility() == View.GONE){
            mImageView.setVisibility(View.VISIBLE);
        }else{
            mImageView.setVisibility(View.GONE);
        }

        if(mCropButton.getVisibility() == View.GONE){
            mCropButton.setVisibility(View.VISIBLE);
        }else{
            mCropButton.setVisibility(View.GONE);
        }

        if(mCropImageView.getVisibility() == View.GONE){
            mCropImageView.setVisibility(View.VISIBLE);
        }else{
            mCropImageView.setVisibility(View.GONE);
        }
    }
    /*
    * The image we receive from the camera intent is displayed, unscaled, in the cropper. After
     *  the user crops the image, scale it down to desired resolution (size) using
     *  Bitmap.createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter). The
     *  return value from that function is what we cache remotely (s3) and locally (Picasso).
     *
     *  The image returned from the Camera intent will be portrait and the cropped image will be
     *  square. USe ImageView.ScaleType.Center or android:scaleType="center" in xml
    *
    * */
    private void launchCropping() {
        //mCurrentPhotoPath is no good here b/c it gets updated async by s3 fetch - need alternative
        final Bitmap bitmap = BitmapFactory.decodeFile(mNewPhotoPath);
        mCropImageView.setImageBitmap(bitmap);
        toggleImageModes();

    }

    private void onCroppingFinished(){
        Bitmap croppedBm = mCropImageView.getCroppedBitmap();
        int before = BitmapCompat.getAllocationByteCount(croppedBm);
        Bitmap scaledDown = Bitmap.createScaledBitmap(croppedBm, 225, 225, true);
        int after = BitmapCompat.getAllocationByteCount(scaledDown);
        croppedBm.recycle();
        Log.d(TAG, "Before: " + before + " After: " + after);
        mImageView.setImageBitmap(scaledDown);
        toggleImageModes();
        uploadToS3(scaledDown);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            launchCropping();
        }
    }
    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getActivity().getApplicationContext();
        //NOTE: setting context multiple places. Should remove upon creation of base class.
        Firebase.setAndroidContext(ctx);
        BasicAWSCredentials creds = new BasicAWSCredentials(Constants.AWS_KEY, Constants.AWS_SECRET);
        AmazonS3 s3 = new AmazonS3Client(creds);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        mTransferUtility = new TransferUtility(s3, getContext());
        mState = (GlobalState) ctx;
        mUid = mState.getCurrUid();
        mFBRef = new Firebase(Constants.FBURL).child("users").child(mUid).child("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        mTakePicButton = (Button) v.findViewById(R.id.action_take_picture);
        mImageView = (ImageView) v.findViewById(R.id.image_preview);
        mProgress =  v.findViewById(R.id.progress_overlay);
        mCropImageView = (CropImageView) v.findViewById(R.id.cropImageView);
        mCropButton = (Button) v.findViewById(R.id.action_crop);

        //mCropImageView.setMinFrameSizeInDp(250);
        retrieveImgFromS3();
        mCropImageView.setGuideShowMode(CropImageView.ShowMode.NOT_SHOW);
        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCroppingFinished();
            }
        });

        mTakePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });




        return v;
    }

    private void retrieveImgFromS3(){
        mFBRef.child("key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                final String currentImageKey = (String) snapshot.getValue();
                if (currentImageKey!= null && !currentImageKey.equalsIgnoreCase(Constants.NO_IMAGE_YET)) {
                    //I should be checking for the file locally before fetching from Amazon
                    Bitmap profileBitmap = mState.Cache.get(currentImageKey);
                    if(profileBitmap != null){
                        mImageView.setImageBitmap(profileBitmap);
                        Log.d(TAG, "Getting profile image from MEMORY");
                    }else{
                        Log.d(TAG, "Getting profile image from REMOTE");
                        try {
                            File image = createTempImageFile(currentImageKey);
                            mCurrentPhotoPath = image.getAbsolutePath();
                            TransferObserver observer = mTransferUtility.download(
                                    MY_BUCKET,     /* The bucket to upload to */
                                    currentImageKey,    /* The key for the uploaded object */
                                    image        /* The file where the data to upload exists */
                            );

                            observer.setTransferListener(new TransferListener() {

                                @Override
                                public void onStateChanged(int id, TransferState state) {
                                    // do something

                                    if (state == TransferState.IN_PROGRESS) {
                                        Util.alphaAnimate(mProgress, View.VISIBLE, 0.4f, 200);
                                    }

                                    if (state == TransferState.COMPLETED) {
                                        //Toast.makeText(ProfileFragment.this.getActivity(), "Download Finished", Toast.LENGTH_SHORT).show();
                                        Util.alphaAnimate(mProgress, View.GONE, 0, 200);
                                        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                                        mImageView.setImageBitmap(bitmap);
                                        mState.Cache.set(mImagefileName, bitmap);
                                    }


                                }

                                @Override
                                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                    int percentage = (int) (bytesCurrent / bytesTotal * 100);

                                }

                                @Override
                                public void onError(int id, Exception ex) {
                                    Log.d(TAG, "S3 ERROR: Download failed");
                                }
                            });
                        } catch (IOException ex) {
                            Log.d(TAG, "Failed to create temp file for download.");
                        }
                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
