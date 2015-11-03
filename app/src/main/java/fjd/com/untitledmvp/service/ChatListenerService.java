package fjd.com.untitledmvp.service;

import android.app.IntentService;
import android.content.Intent;

public class ChatListenerService extends IntentService {

    public ChatListenerService() {
        super("Myservice");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }


}
