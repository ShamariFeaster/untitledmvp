package fjd.com.untitledmvp.helper;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by wzhjtn on 10/12/2015.
 */
public class GeoQueryWrapper {
    private Boolean mIsMock = false;
    private int mResultLimit = 10;

    public GeoQueryWrapper(Boolean isMock){
        this.mIsMock = isMock;
    }

    public Queue<String> Query(float latitude, float longitude, float radius){
        Queue<String> results = new ArrayDeque<>();
        if(this.mIsMock == true){
            results.add("f1429716-e226-4b8e-b538-27f8ec0d92ff");
            results.add("23d4646f-ebf1-43fb-8de1-f198cc268310");
            results.add("73fdc83e-f123-4b83-92dc-acc568c6b5be");
            results.add("facebook:1006907856026402");
            results.add("facebook:723263297817907");
        }else{

        }

        return results;
    }

    public void StoreLoaction(String key, float latitude, float longitude){

    }


}


