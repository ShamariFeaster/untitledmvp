package fjd.com.untitledmvp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

public class BaseActivity extends AppCompatActivity {
    private Boolean mIsVisible = false;
    private String mClassName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mClassName = Util.GetClassName(this);
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);
        filter.setPriority(1);
        /*Broadcasts received here means app is visible and should signal user with toast*/
        registerReceiver(new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                switch(Util.GetBroadCastType(getResultData())){
                    case Constants.BROADCAST_TYPE_NEW_MATCH:


                        if(mIsVisible){

                            Util.ShowToast(
                                    context,
                                    "You Matched with "
                                        + extras.get(Constants.BC_NEW_MSG_EXTRAS_SENDER)
                            );

                            this.setResultCode(Constants.BROADCAST_HANDLED);
                            this.abortBroadcast();
                        }


                        break;

                    case Constants.BROADCAST_TYPE_NEW_MESSAGE:

                        if(!mClassName.equalsIgnoreCase("ChatActivity") && mIsVisible){

                            Util.ShowToast(
                                context,
                                "New Message from " + extras.get(Constants.BC_NEW_MSG_EXTRAS_SENDER)
                            );

                            this.setResultCode(Constants.BROADCAST_HANDLED);
                            this.abortBroadcast();
                        }else if(mClassName.equalsIgnoreCase("ChatActivity") && mIsVisible){
                            this.setResultCode(Constants.BROADCAST_HANDLED);
                            this.abortBroadcast();
                        }

                        break;
                }

            }
        }, filter);

    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onStart(){super.onStart(); mIsVisible = true;}

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){ super.onStop();mIsVisible = true;}

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
