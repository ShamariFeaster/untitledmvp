package fjd.com.untitledmvp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

/**
 * Created by WZHJTN on 11/12/2015.
 */

public class ServiceReceiver extends BroadcastReceiver{
    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, Intent intent) {

        int resultCode = this.getResultCode();
        Bundle extras = intent.getExtras();
        /*Broadcasts received here means app is not visible and should signal user with notification*/
        switch(resultCode){

            case Constants.BROADCAST_UNHANDLED:

                switch(Util.GetBroadCastType(getResultData())) {

                    case Constants.BROADCAST_TYPE_NEW_MATCH:
                        Util.PostNotification(
                                context,
                                "You matched with " + extras.getString(Constants.BC_NEW_MATCH_EXTRAS_MATCH_FN),
                                extras.getInt(Constants.BC_NEW_MSG_EXTRAS_ICON)
                        );
                        break;

                    case Constants.BROADCAST_TYPE_NEW_MESSAGE:
                        Util.PostNotification(
                                context,
                                extras.getString(Constants.BC_NEW_MSG_EXTRAS_CONVO_ID),
                                "New message from " + extras.getString(Constants.BC_NEW_MSG_EXTRAS_SENDER),
                                extras.getString(Constants.BC_NEW_MSG_EXTRAS_TEXT),
                                (HashMap) extras.getSerializable(Constants.BC_NEW_MSG_EXTRAS_USER),
                                extras.getInt(Constants.BC_NEW_MSG_EXTRAS_ICON)
                        );
                        break;
                }

                break;

            default:
                break;
        }
    }
}
