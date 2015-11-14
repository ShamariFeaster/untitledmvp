package fjd.com.untitledmvp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import fjd.com.untitledmvp.activities.BaseActivity;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

/**
 * Created by WZHJTN on 11/13/2015.
 */
public class ActivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = getResultExtras(true);
        BaseActivity receivingActivity = (BaseActivity)context;
        Boolean isVisible = receivingActivity.GetVisibility();
        String className = Util.GetClassName(receivingActivity);

        switch(Util.GetBroadCastTypeCode(getResultData())){
            case Constants.BROADCAST_CODE_NEW_MATCH:


                if(isVisible){

                    Util.ShowToast(
                            context,
                            "You Matched with "
                                    + extras.get(Constants.BC_NEW_MATCH_EXTRAS_MATCH_FN)
                    );

                    this.setResultCode(Constants.BROADCAST_HANDLED);
                    this.abortBroadcast();
                }


                break;

            case Constants.BROADCAST_CODE_NEW_MESSAGE:

                if(!className.equalsIgnoreCase("ChatActivity") && isVisible){

                    Util.ShowToast(
                            context,
                            "New Message from " + extras.get(Constants.BC_NEW_MSG_EXTRAS_SENDER)
                    );

                    this.setResultCode(Constants.BROADCAST_HANDLED);
                    this.abortBroadcast();
                }else if(className.equalsIgnoreCase("ChatActivity") && isVisible){
                    this.setResultCode(Constants.BROADCAST_HANDLED);
                    this.abortBroadcast();
                }

                break;
        }
    }
}
