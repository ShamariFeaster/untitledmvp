package fjd.com.untitledmvp.state;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.LruCache;

import fjd.com.untitledmvp.models.User;
import fjd.com.untitledmvp.util.Constants;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class GlobalState extends Application {

    public LruCache Cache;
    public User CurrUser = null;

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
        //registerActivityLifecycleCallbacks(new ALC());
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

        }
    }
}
