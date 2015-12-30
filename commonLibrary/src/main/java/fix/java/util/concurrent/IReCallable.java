package fix.java.util.concurrent;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/3/6.
 */
public interface IReCallable<T> extends Callable<T>, IHandleException {
}
