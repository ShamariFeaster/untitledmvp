package fjd.com.untitledmvp.helper;


/**
 * Created by wzhjtn on 10/12/2015.
 */
public class Pair<L,R> {
    public L key;
    public R value;
    public Pair(L left, R right){
        this.key = left;
        this.value = right;
    }
}
