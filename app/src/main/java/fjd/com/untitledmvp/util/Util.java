package fjd.com.untitledmvp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class Util {
    final private static String TAG = "OTUPUT";

    public static void LogExecTime(long start, String msg){
        msg = (msg.isEmpty()) ? msg : msg + " : ";
        Long execTime = new Long(System.currentTimeMillis() - start);
        Log.d(TAG, msg + execTime.toString() + "mills");
    }

    public static void alphaAnimate(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }
}
