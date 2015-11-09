package fjd.com.untitledmvp.state;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.LruCache;

import java.util.ArrayList;

import fjd.com.untitledmvp.helper.FirebaseManager;
import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class GlobalState extends Application {

    public LruCache Cache;
    public User CurrUser = null;
    private FirebaseManager mFBManager;
    public String getCurrUid(){
        return (CurrUser == null) ? Constants.MOCK_UID : CurrUser.uid;
    }

    public String getCurrFn(){
        return (CurrUser == null) ? Constants.MOCK_FN : CurrUser.getFn();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        Cache = new LruCache(ctx);
        Picasso.setSingletonInstance(new Picasso.Builder(ctx).memoryCache(Cache).build());
        Picasso.with(ctx).setIndicatorsEnabled(true);
        mFBManager = new FirebaseManager(ctx);
        registerActivityLifecycleCallbacks(new ALC());
        //check for polling timer, stop if started
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //check if service running, kill it if it is
        //could set polling timer to wake up for push notifications
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    private final class ALC implements ActivityLifecycleCallbacks{

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {
            final Context ctx = getApplicationContext();
            final Intent svcIntent = Util.GetServiceIntent(ctx);

            if("ChatActivity".equalsIgnoreCase(activity.getClass().getSimpleName())){
                Util.StartService(ctx, svcIntent);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

            final Context ctx = getApplicationContext();
            final Intent svcIntent = Util.GetServiceIntent(ctx);

            if(!"ChatActivity".equalsIgnoreCase(activity.getClass().getSimpleName())){
                Util.StartService(ctx, svcIntent);
            }else{
               stopService(svcIntent);
            }
        }
    }
}
