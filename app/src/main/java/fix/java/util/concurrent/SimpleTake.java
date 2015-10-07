package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/9/22.
 */
public abstract class SimpleTake<T> extends Take<T> {
    public SimpleTake() {
        cancelTakeout();
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }
}
