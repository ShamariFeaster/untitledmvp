package fjd.com.untitledmvp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import fjd.com.untitledmvp.activities.ChatActivity;
import fjd.com.untitledmvp.helper.FirebaseManager;
import fjd.com.untitledmvp.helper.Pair;
import fjd.com.untitledmvp.state.GlobalState;

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

    public static void PostNotification(Context ctx, String convoID, String title, String text, int iconResource){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(iconResource)
                        .setContentTitle(title)
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(ctx, ChatActivity.class);
                resultIntent.putExtra(Constants.CONVO_KEY, convoID);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(ChatActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());
    }

    public static Intent GetServiceIntent(Context ctx){
        return new Intent(ctx, Constants.SERVICE_CLASS_TKN);
    }

    public static  void StartService(final Context ctx, final Intent i){
        final Bundle bundle = new Bundle();
        final GlobalState state = (GlobalState) ctx;
        //get my converations
        new FirebaseManager(ctx).GetConversationKeys(state.getCurrUid(), new FirebaseManager.ListCallback() {
            @Override
            public void onListFetched(ArrayList<String> list) {
                bundle.putStringArrayList(Constants.SERVICE_CONVO_IDS, list);
                i.putExtras(bundle);
                ctx.startService(i);
            }
        });
    }
}
