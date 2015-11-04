package fjd.com.untitledmvp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fjd.com.untitledmvp.R;
import fjd.com.untitledmvp.util.Constants;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected  void onStart(){
        super.onStart();
        //check if service started, stop it if it is
    }

    @Override
    protected void onStop(){
        super.onStop();
        //check if service stopped, start it if it is
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = null;
        switch(id){
            case R.id.action_matches:
                intent = new Intent(getApplicationContext(), ChatListActivity.class);
                break;

            case R.id.action_swipe:
                intent = new Intent(getApplicationContext(), MatchActivity.class);
                break;

            case R.id.action_my_profile:
                intent = new Intent(getApplicationContext(), ProfileActivity.class);
                break;

            case R.id.action_logout:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra(Constants.LOGOUT_KEY, "1");
                break;
        }

        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
