package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/7/9.
 */
public interface TakeListener<T extends Take<?>> {
    void apply(T take);
}

