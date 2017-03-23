package android.support.v4.app;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/22.
 */

public class ExecCommit implements Runnable {
    FragmentManagerImpl fm;
    ArrayList<Runnable> actions = new ArrayList<>();
    Runnable execActionsTask = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            int end = actions.size();
            while (i < end) {
                while (i < end) {
                    for (; i < end; i++) {
                        Runnable runnable = actions.get(i);
                        runnable.run();
                    }
                    end = actions.size();
                }
                for (; i > 0; i--) {
                    actions.remove(0);
                }
                end = actions.size();
            }
        }
    };

    public ExecCommit(FragmentManager fragmentManager) {
        fm = (FragmentManagerImpl) fragmentManager;
        fm.mExecCommit = this;
    }

    @Override
    public void run() {
        fm.execPendingActions();
        execActionsTask.run();
        // fm.mHost.getHandler().post(execActionsTask);
    }

    public static void wrap(FragmentManager fragmentManager) {
        if (fragmentManager == null) {
            return;
        }
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        if (!(fm.mExecCommit instanceof ExecCommit)) {
            ExecCommit execCommit = new ExecCommit(fragmentManager);
        }
    }

    public static void enqueueAction(FragmentManager fragmentManage, Runnable action) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManage;
        ExecCommit execCommit = (ExecCommit) fm.mExecCommit;
        execCommit.actions.add(action);
    }
}
