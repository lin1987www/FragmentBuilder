package android.support.v4.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * Created by Administrator on 2017/2/22.
 */

public class BackStackRecordShell implements Runnable {
    public BackStackRecord backStackRecord;

    public BackStackRecordShell(Runnable runnable) {
        backStackRecord = (BackStackRecord) runnable;
    }

    @Override
    public void run() {
        // before Enter
        // 可以用於執行前檢查
        backStackRecord.run();
        // after Enter
        // 通知執行完畢  大概也只能用於 print 狀態
    }

    public static void wrap(Object obj, FragmentManager fragmentManager) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        if (!(fm.mPendingActions instanceof PendingActions)) {
            PendingActions pendingActions = new PendingActions(fm.mPendingActions);
            fm.mPendingActions = pendingActions;
        }
        if (!(fm.mBackStack instanceof BackStacks)) {
            BackStacks backStacks = new BackStacks(fm.mBackStack);
            fm.mBackStack = backStacks;
        }
    }

    public static class BackStacks extends ArrayList<BackStackRecord> {
        public BackStacks(ArrayList<BackStackRecord> origin) {
            if (origin != null) {
                addAll(origin);
            }
        }

        @Override
        public BackStackRecord remove(int index) {
            BackStackRecord record = super.remove(index);
            triggerPopBackStackRecord(record);
            /*
            FragmentManagerImpl fragmentManager = record.mManager;
            PendingActions pendingActions = (PendingActions) fragmentManager.mPendingActions;
            BackStackRecordShell recordShell = pendingActions.getBackStackRecordShell(record);
            */
            // 如果因為記憶體不足被釋放  即使使用 addOnBackStackChangedListener 也會因為無法被回復而無法使用
            // fragmentManager.addOnBackStackChangedListener(recordShell);
            return record;
        }
    }

    public static class PendingActions extends ArrayList<Runnable> {
        public PendingActions(ArrayList<Runnable> origin) {
            if (origin != null) {
                addAll(origin);
            }
        }

        @Override
        public boolean add(Runnable element) {
            return super.add(wrap(element));
        }

        @Override
        public void add(int index, Runnable element) {
            super.add(index, wrap(element));
        }

        @Override
        public boolean addAll(Collection<? extends Runnable> c) {
            return super.addAll(wrap(c));
        }

        @Override
        public boolean addAll(int index, Collection<? extends Runnable> c) {
            return super.addAll(index, wrap(c));
        }

        protected Collection<? extends Runnable> wrap(Collection<? extends Runnable> collection) {
            ArrayList<Runnable> temps = new ArrayList<>();
            for (Runnable runnable : collection) {
                temps.add(wrap(runnable));
            }
            collection.clear();
            ((Collection<Runnable>) collection).addAll(temps);
            return collection;
        }

        protected WeakHashMap<BackStackRecord, BackStackRecordShell> mBackStackRecordShellMap = new WeakHashMap<>();

        public BackStackRecordShell getBackStackRecordShell(BackStackRecord record) {
            return mBackStackRecordShellMap.get(record);
        }

        protected Runnable wrap(Runnable runnable) {
            if (runnable instanceof BackStackRecord) {
                BackStackRecord record = (BackStackRecord) runnable;
                BackStackRecordShell recordShell = new BackStackRecordShell(record);
                mBackStackRecordShellMap.put(record, recordShell);
                return recordShell;
            }
            return runnable;
        }
    }

    public interface OnPopBackStackRecordListener {
        void onPopBackStackRecord(BackStackRecord record);
    }

    static ArrayList<OnPopBackStackRecordListener> listeners = new ArrayList<>();

    public static void addListener(OnPopBackStackRecordListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(OnPopBackStackRecordListener listener) {
        listeners.remove(listener);
    }

    static void triggerPopBackStackRecord(BackStackRecord record) {
        Iterator<OnPopBackStackRecordListener> iterator = listeners.listIterator();
        while (iterator.hasNext()) {
            OnPopBackStackRecordListener listener = iterator.next();
            listener.onPopBackStackRecord(record);
        }
    }
}