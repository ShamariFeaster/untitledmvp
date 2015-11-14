package fjd.com.untitledmvp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import fjd.com.untitledmvp.receiver.ActivityReceiver;
import fjd.com.untitledmvp.util.Constants;
import fjd.com.untitledmvp.util.Util;

public class BaseActivity extends AppCompatActivity {
    private Boolean mIsVisible = false;
    public Boolean GetVisibility(){return mIsVisible;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);
        filter.setPriority(1);
        /*Broadcasts received here means app is visible and should signal user with toast*/
        registerReceiver(new ActivityReceiver(), filter);

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
    protected void onStop(){ super.onStop();mIsVisible = false;}

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
