package fjd.com.untitledmvp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

import fjd.com.untitledmvp.helper.Pair;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class Util {
    final private static String TAG = "OTUPUT";


    public static Pair<String, String> SplitConvoKey(String key){
        String[] parts = key.split("\\|");
        Pair<String, String> pair = new Pair<>("target", parts[1],"convoId", parts[0]);
        return pair;
    }

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
