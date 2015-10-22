package fjd.com.untitledmvp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.util.Constants;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileFragment extends Fragment {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath = "";
    private ImageView mImageView = null;
    private String mImagefileName = "";
    final private String TAG = "ProfileFragment";
    final private String MY_BUCKET = "untitled-mvp-images";
    private TransferUtility mTransferUtility = null;
    private ProgressBar mProgress;
    private Firebase mFBRef;
    private String uid = Constants.MOCK_UID;
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

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadToS3(){
        TransferObserver observer = mTransferUtility.upload(
                MY_BUCKET,     /* The bucket to upload to */
                mImagefileName,    /* The key for the uploaded object */
                new File(mCurrentPhotoPath)        /* The file where the data to upload exists */
        );

        observer.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something

                if(state == TransferState.IN_PROGRESS){
                    mProgress.setVisibility(View.VISIBLE);
                    mProgress.setProgress(0);
                }

                if(state == TransferState.COMPLETED){
                    Toast.makeText(ProfileFragment.this.getActivity(), "Upload Finished", Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                    Map<String, Object> imageMeta = new HashMap<String, Object>();
                    imageMeta.put("key", mImagefileName);
                    imageMeta.put("timestamp", new Date().getTime());
                    mFBRef.updateChildren(imageMeta);
                }


            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                mProgress.setProgress(percentage);

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d(TAG, "S3 ERROR: Upload failed");
            }

        });

    }

    private void toggleButtons(){
        if(mTakePicButton.getVisibility() == View.GONE){
            mTakePicButton.setVisibility(View.VISIBLE);
        }else{
            mTakePicButton.setVisibility(View.GONE);
        }

        if(mCropButton.getVisibility() == View.GONE){
            mCropButton.setVisibility(View.VISIBLE);
        }else{
            mCropButton.setVisibility(View.GONE);
        }
    }

    private void launchCropping() {

        final Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mCropImageView.setImageBitmap(bitmap);
        toggleButtons();

    }

    private void onCroppingFinished(){
        toggleButtons();
        mImageView.setImageBitmap(mCropImageView.getCroppedBitmap());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            launchCropping();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NOTE: setting context multiple places. Should remove upon creation of base class.
        Firebase.setAndroidContext(getContext());
        BasicAWSCredentials creds = new BasicAWSCredentials(Constants.AWS_KEY, Constants.AWS_SECRET);
        AmazonS3 s3 = new AmazonS3Client(creds);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        mTransferUtility = new TransferUtility(s3, getContext());
        mFBRef = new Firebase(Constants.FBURL).child("users").child(uid).child("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        mTakePicButton = (Button) v.findViewById(R.id.action_take_picture);
        mImageView = (ImageView) v.findViewById(R.id.image_preview);
        mProgress = (ProgressBar) v.findViewById(R.id.progressbar_s3);
        mCropImageView = (CropImageView) v.findViewById(R.id.cropImageView);
        mCropButton = (Button) v.findViewById(R.id.action_crop);

        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCroppingFinished();
            }
        });
        mProgress.setVisibility(View.GONE);

        mTakePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mFBRef.child("key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                String currentImageKey = (String) snapshot.getValue();
                if (!currentImageKey.equalsIgnoreCase(Constants.NO_IMAGE_YET)) {
                    //I should be checking for the file locally before fetching from Amazon
                    try {
                        File image = createTempImageFile(currentImageKey);
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
                                    mProgress.setVisibility(View.VISIBLE);
                                    mProgress.setProgress(0);
                                }

                                if (state == TransferState.COMPLETED) {
                                    Toast.makeText(ProfileFragment.this.getActivity(), "Download Finished", Toast.LENGTH_SHORT).show();
                                    mProgress.setVisibility(View.GONE);
                                    int targetW = mImageView.getWidth();
                                    int targetH = mImageView.getHeight();

                                    // Get the dimensions of the bitmap
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    bmOptions.inJustDecodeBounds = true;
                                    bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                                    int photoW = bmOptions.outWidth;
                                    int photoH = bmOptions.outHeight;

                                    // Determine how much to scale down the image
                                    int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                                    // Decode the image file into a Bitmap sized to fill the View
                                    bmOptions.inJustDecodeBounds = false;
                                    bmOptions.inSampleSize = scaleFactor;
                                    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                                    mImageView.setImageBitmap(bitmap);
                                }


                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                                mProgress.setProgress(percentage);

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

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


        return v;
    }
}
