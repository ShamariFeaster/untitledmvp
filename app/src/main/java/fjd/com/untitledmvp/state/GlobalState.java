package fjd.com.untitledmvp.state;
import android.app.Application;
import android.content.Context;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.LruCache;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class GlobalState extends Application {

    public LruCache Cache;

    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        Cache = new LruCache(ctx);
        Picasso.setSingletonInstance(new Picasso.Builder(ctx).memoryCache(Cache).build());
        Picasso.with(ctx).setIndicatorsEnabled(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
