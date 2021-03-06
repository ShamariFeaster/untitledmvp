package fjd.com.untitledmvp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import fjd.com.untitledmvp.activities.ChatActivity;
import fjd.com.untitledmvp.callback.ListCallback;
import fjd.com.untitledmvp.helper.FirebaseManager;
import fjd.com.untitledmvp.helper.Pair;
import fjd.com.untitledmvp.receiver.EmptyReceiver;
import fjd.com.untitledmvp.service.ChatListenerService;
import fjd.com.untitledmvp.state.GlobalState;

/**
 * Created by wzhjtn on 10/21/2015.
 */
public class Util {
    final private static String TAG = "OTUPUT";


    public static Pair<String, String> SplitConvoKey(String key){
        String[] parts = key.split("\\|");
        Pair<String, String> pair;
        pair = new Pair<>(null,null);
        if(parts.length > 0){
            pair = new Pair<>("target", parts[1],"convoId", parts[0]);
        }

        return pair;
    }

    public static int GetBroadCastTypeCode(String type){
        int typeInt = -1;
        if(type == null) return typeInt;
        if(type.equalsIgnoreCase(Constants.BROADCAST_NEW_MATCH)){
            typeInt = Constants.BROADCAST_CODE_NEW_MATCH;
        }else if(type.equalsIgnoreCase(Constants.BROADCAST_NEW_MESSAGE)){
            typeInt = Constants.BROADCAST_CODE_NEW_MESSAGE;
        }
        return typeInt;
    }

    public static void BroadcastEvent(Context ctx, BroadcastReceiver receiver,  String eventType, Bundle extras){
        ctx.sendOrderedBroadcast(
                Util.GetBroadcastIntent(),
                null,
                receiver,
                null,
                Constants.BROADCAST_UNHANDLED,
                eventType,
                extras
        );

    }

    public static void BroadcastEvent(Context ctx,  String eventType, Bundle extras){
        ctx.sendOrderedBroadcast(
                Util.GetBroadcastIntent(),
                null,
                new EmptyReceiver(),
                null,
                Constants.BROADCAST_UNHANDLED,
                eventType,
                extras
        );

    }

    public static void ShowToast(Context ctx, String msg){
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static String GetClassName(Class<?> classtoken){
        return classtoken.getSimpleName();
    }

    public static String GetClassName(Context ctx){
        return ctx.getClass().getSimpleName();
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

    public static void PostNotification(Context ctx, String text, int iconResource, Class<?> activityToOpen){
        PostNotification(ctx, null,text,"Click To Open", null, iconResource, activityToOpen );
    }

    public static void PostNotification(Context ctx, String convoID, String title, String text,
                                        HashMap<String, String> hm, int iconResource,
                                        Class<?> activityToOpen){

        int id = 0;
        if("ChatListenerService".equalsIgnoreCase(ctx.getClass().getSimpleName())){
            id = ((ChatListenerService)ctx).mNotificationId++;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(iconResource)
                        .setContentTitle(title)
                        .setContentText(text);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(ctx, activityToOpen);
        Bundle bdl = new Bundle();
        if(convoID != null && !convoID.isEmpty()){
            bdl.putString(Constants.CONVO_KEY, convoID);
        }

        if(hm != null && !hm.isEmpty()){
            bdl.putSerializable(Constants.CURR_USER_KEY, hm);
        }

        resultIntent.putExtras(bdl);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(activityToOpen);
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
                mNotificationManager.notify(id, mBuilder.build());
    }


    public static Intent GetServiceIntent(Context ctx){
        return new Intent(ctx, Constants.SERVICE_CLASS_TKN);
    }

    public static Intent GetBroadcastIntent(){
        return new Intent(Constants.BROADCAST_ACTION);
    }

    public static  void StartService(final Context ctx, final Intent i){
        final Bundle bundle = new Bundle();
        final GlobalState state = (GlobalState) ctx;
        //get my converations
        new FirebaseManager(ctx).GetConversationKeys(state.getCurrUid(), new ListCallback() {
            @Override
            public void onListFetched(ArrayList<String> list) {
                bundle.putStringArrayList(Constants.SERVICE_CONVO_IDS, list);
                bundle.putSerializable(Constants.CURR_USER_KEY, state.GetCurrUser().ToHashMap());
                i.putExtras(bundle);
                ctx.startService(i);
            }
        });
    }
}
