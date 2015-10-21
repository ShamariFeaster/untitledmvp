package fjd.com.untitledmvp.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.*;
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
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import com.amazonaws.HttpMethod;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.state.GlobalState;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

import com.squareup.picasso.Picasso;

/**
 * Created by wzhjtn on 10/12/2015.
 */

public class ImageManager {
    private TransferUtility mTransferUtility;
    private File mAppStorageDir;
    private  ArrayBlockingQueue<Pair<User,String>> mSharedImageKeyQueue;
    private  ArrayBlockingQueue<Pair<User, Bitmap>> mSharedBitmapQueue;
    private AmazonS3 mS3Client;
    private Context mContext;
    private GlobalState mState;
    private Thread mLooper;
    private ImageFetchLooper mFetchRunnable;
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
        mS3Client = new AmazonS3Client(creds);
        mS3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
        mTransferUtility = new TransferUtility(mS3Client, context);
        mAppStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"untitled");
        mSharedImageKeyQueue = inputQueue;
        mSharedBitmapQueue = outputQueue;
        mContext = context;
        mState = (GlobalState) mContext;
        mFetchRunnable = new ImageFetchLooper();
    }

    public ImageManager(android.content.Context context){
        this(context,new ArrayBlockingQueue<Pair<User,String>>(1),new ArrayBlockingQueue<Pair<User,Bitmap>>(1));
    }

    private String generateS3Url(String objectKey){
        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60; // 1 hour.
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(Constants.AWS_BUCKET, objectKey);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
        generatePresignedUrlRequest.setExpiration(expiration);

        URL s = mS3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return s.toString();
    }



    private String fetchS3WithPresigned(Pair<User, String> pair) throws InterruptedException{

        Bitmap result = null;
        String presignedUrl = "";
        long start = 0;
        if(pair != null){
            String s3ObjKey = pair.value;

            result = mState.Cache.get(s3ObjKey);

            if(result == null){
                presignedUrl = generateS3Url(s3ObjKey);

                try {
                    start = System.currentTimeMillis();
                    result = Picasso.with(mContext).load(presignedUrl).get();
                    Log.d(TAG, s3ObjKey + " loaded from remote");
                    //resize using picasso here and resized should be loaded in cache
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                start = System.currentTimeMillis();
                Log.d(TAG, s3ObjKey + " loaded from cache");
            }

            mSharedBitmapQueue.put(new Pair<>(pair.key, result));
            mState.Cache.set(s3ObjKey, result );
            pair.key.setImageKey(s3ObjKey);
        }

        Util.LogExecTime(start, "Image Load Time (Presigned)");
        return presignedUrl;
    }

    private class ImageFetchLooper implements  Runnable{
        private volatile boolean running = true;
        public void terminate(){
            running = false;
        }

        public void restart(){
            running = true;
        }

        @Override
        public void run() {
            Log.e(TAG, "Starting Loop");
            //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            while(running){

                try {
                    fetchS3ImageWithObserver(mSharedImageKeyQueue.take());
                }catch (InterruptedException ex){
                    Log.e(TAG, "UID retrieval failed: " + ex.getMessage());
                    break;
                }

            }
        }
    }

    public void fetchCacheAsync(){
        Log.e(TAG, "fetchCacheAsync");

        if(mLooper == null){
            mLooper = new Thread(mFetchRunnable);
        }

        synchronized (mLooper){
            Thread.State state =  mLooper.getState();
            if(mLooper != null && (state == Thread.State.TERMINATED || state == Thread.State.NEW)){
                mFetchRunnable.restart();
                mLooper.start();
            }
        }



    }

    public void StopLooper(){
        if(mLooper != null){
            synchronized (mLooper) {
                if ( mLooper.getState() == Thread.State.RUNNABLE) {
                    mFetchRunnable.terminate();
                    try {
                        mLooper.join();
                        mLooper = null;
                        Log.d(TAG, "Image Fetcher Stopped");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    //MOST LIKELY DEPRECATED

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

    private void fetchS3ImageWithObserver(Pair<User, String> pair) throws InterruptedException{
        final String imageKey = pair.value;
        final long start = System.currentTimeMillis();
        Bitmap result =  mState.Cache.get(imageKey);
        if(result == null){
            TransferObserver observer = getImage(imageKey);
            observer.setTransferListener(new MyTransferListener<User,String>(pair) {
                @Override
                public void onStateChanged(int i, TransferState transferState) {
                    if(transferState == TransferState.COMPLETED){
                        Log.e(TAG, "Offering from producer thread from remote");
                        //pair.value is the key of image in S3
                        Bitmap bm = BitmapFactory.decodeFile(getImagePath(this.pair.value));
                        Util.LogExecTime(start, "Image Load Time (TransferUtil)");
                        try {
                            mSharedBitmapQueue.put(new Pair<>(this.pair.key, bm));
                            mState.Cache.set(imageKey, bm);
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
            Log.e(TAG, "Offering from producer thread from local cache");
            mSharedBitmapQueue.put(new Pair<>(pair.key, result));
        }
    }
}
