package android.support.v4.app;

/**
 * Created by Administrator on 2015/8/12.
 */
public abstract class ComboRunnable implements Runnable {
    protected Runnable mTask;
    protected Runnable mNextTask;
    protected ComboRunnable mPointerTask;

    public ComboRunnable() {
        mPointerTask = this;
    }

    public void nextTask(Runnable task) {
        if (mPointerTask.mTask == null) {
            mPointerTask.mTask = task;
        } else {
            ComboRunnable newPointer = create();
            newPointer.nextTask(task);
            mPointerTask.mNextTask = newPointer;
            mPointerTask = newPointer;
        }
    }

    public abstract void postRunnable(Runnable task);

    public abstract ComboRunnable create();

    @Override
    public void run() {
        // Some task need to wait. Example: TakeSnapshot.
        ExecutorSet.nonBlockExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mTask != null) {
                            try {
                                mTask.run();
                                if (mNextTask != null) {
                                    postRunnable(mNextTask);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }
}
