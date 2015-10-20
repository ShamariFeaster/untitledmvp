package fjd.com.untitledmvp.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.*;
import android.os.Process;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.util.Constants;

/**
 * Created by wzhjtn on 10/12/2015.
 */

public class ImageManager {
    private TransferUtility mTransferUtility;
    private File mAppStorageDir;
    private  ArrayBlockingQueue<Pair<User,String>> mSharedImageKeyQueue;
    private  ArrayBlockingQueue<Pair<User, Bitmap>> mSharedBitmapQueue;
    final private String TAG = "OUTPUT";

    //So Each listener can carry metadata with it
    abstract class MyTransferListener<L,R> implements TransferListener{
        public Pair<L,R> pair;
        public MyTransferListener(Pair<L,R> pair){
            this.pair = pair;
        }
    }

    public ImageManager(android.content.Context context, ArrayBlockingQueue<Pair<User,String>> inputQueue,
                        ArrayBlockingQueue<Pair<User, Bitmap>> outputQueue){
        BasicAWSCredentials creds = new BasicAWSCredentials(Constants.AWS_KEY, Constants.AWS_SECRET);
        AmazonS3 s3 = new AmazonS3Client(creds);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        mTransferUtility = new TransferUtility(s3, context);
        mAppStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"untitled");
        mSharedImageKeyQueue = inputQueue;
        mSharedBitmapQueue = outputQueue;
    }

    public ImageManager(android.content.Context context){
        BasicAWSCredentials creds = new BasicAWSCredentials(Constants.AWS_KEY, Constants.AWS_SECRET);
        AmazonS3 s3 = new AmazonS3Client(creds);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        mTransferUtility = new TransferUtility(s3, context);
        mAppStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"untitled");
        mSharedImageKeyQueue = new ArrayBlockingQueue<Pair<User,String>>(1);
        mSharedBitmapQueue = new ArrayBlockingQueue<Pair<User,Bitmap>>(1);
    }
    public void fetchCacheAsync(){
        Log.e(TAG, "fetchCacheAsync");
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.e(TAG, "Starting Loop");
                //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                while(true){

                    try {
                        Pair<User,String> pair =  mSharedImageKeyQueue.take();
                        String imageKey = pair.value;
                        pair.key.setImageKey(imageKey);
                        String path = getImagePath(imageKey);
                        File temp = new File(path);
                        if(!temp.exists()){
                            TransferObserver observer = getImage(imageKey);
                            observer.setTransferListener(new MyTransferListener<User,String>(pair) {
                                @Override
                                public void onStateChanged(int i, TransferState transferState) {
                                    if(transferState == TransferState.COMPLETED){
                                        Log.e(TAG, "Offering from producer thread from remote");
                                        //pair.value is the key of image in S3
                                        Bitmap bm = BitmapFactory.decodeFile(getImagePath(this.pair.value));
                                        try {
                                            mSharedBitmapQueue.put(new Pair<>(this.pair.key, bm));
                                        } catch (InterruptedException e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }

                                @Override
                                public void onProgressChanged(int i, long l, long l1) {

                                }

                                @Override
                                public void onError(int i, Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });

                        }else{
                            Bitmap bm = BitmapFactory.decodeFile(path);
                            Log.e(TAG, "Offering from producer thread from local");
                            mSharedBitmapQueue.put(new Pair<>(pair.key, bm));
                        }
                    }catch (InterruptedException ex){
                        Log.e(TAG, "UID retrieval failed: " + ex.getMessage());
                        break;
                    }

                }
            }
        }).start();
    }

    public TransferObserver putImage(String key, String localImagePath){
        return mTransferUtility.upload(
                Constants.AWS_BUCKET,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                new File(localImagePath)        /* The file where the data to upload exists */
        );
    }



    public TransferObserver getImage(String key){
        return mTransferUtility.download(
                Constants.AWS_BUCKET,     /* The bucket to upload to */
                key,    /* The key for the uploaded object */
                createTempImageFile(key)        /* The file where the data to upload exists */
        );
    }
    public String makeAndSaveThumbnail(Pair<User, Bitmap> pair){
        String filename = (String) pair.key.getImage().get("key");
        File image = createTempImageFile(filename + "-thumbnail");

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap thumb = ThumbnailUtils.extractThumbnail(pair.value,50,50);
        thumb.compress(Bitmap.CompressFormat.JPEG, 85, fOut);

        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       return image.getAbsolutePath();

    }

    public  Bitmap retrieveThumbnail(String imageKey){
        File thumbnail = new File(mAppStorageDir, imageKey + "-thumbnail.jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(thumbnail.getAbsolutePath(), options);
    }

    private File createTempImageFile(String filename) {
        String imageFilename;
        File image = null;
        if(filename == null){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFilename = "JPEG_" + timeStamp + "_";
            try {
                image = File.createTempFile(
                        imageFilename,  /* prefix */
                        ".jpg",         /* suffix */
                        mAppStorageDir      /* directory */
                );
            }catch(IOException ex){
                Log.e(TAG, ex.getMessage());
            }

        }else{
            image = new File(getImagePath(filename));
        }

        if (!mAppStorageDir.exists() && !mAppStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }


        return image;
    }

    private String getImagePath(String key){
        return mAppStorageDir.getAbsolutePath() + File.separator + key + ".jpg";
    }
}
