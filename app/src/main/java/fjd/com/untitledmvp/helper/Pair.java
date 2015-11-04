package fjd.com.untitledmvp.helper;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by wzhjtn on 10/12/2015.
 */
public class Pair<L,R> {
    public L key = null;
    public R value = null;
    public Map keys = new HashMap<String, L>();
    public Map vals = new HashMap<String, R>();

    public Pair(){}

    public Pair(L left, R right){
        this.key = left;
        this.value = right;
    }

    public Pair(String keyAlias, L left, String valAlias, R right){
        this.key = left;
        this.value = right;
        keys.put(keyAlias, left);
        vals.put(valAlias, right);
    }
}
