package com.lin1987www.app;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fix.android.support.v4.app.FragmentUtils;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.support.v4.app.FragmentTransaction.TRANSIT_NONE;

/**
 * Created by lin on 2015/6/25.
 * 主要功能是負責 將 Fragment顯示出來 !
 * 1. add
 * 2. replace
 * 3. reset
 * <p/>
 * Class BackStackRecord implements interface of FragmentTransaction.
 * TODO 超級可能性
 * 當因為 onBackStack 發生時，FragmentActivity 處理 onBackStack時，觸發FragmentTransactionBuilder2.popBackStack()   Stong Ref 到那個Fragment ，然後監聽 onBackStackChanged() 時，再將Fragment 丟回去。
 * 當 onBackPressed 去呼叫特定 FragmentManager 去執行 popBackStack，但執行前，透過預先儲存在 BackEntry中Name屬性的相關資料，得知這Fragment是"誰"呼叫，然後使用addOnBackStackChangedListener當 Fragment 被移除的時候，丟回去原本呼叫的地方(FragmentActivity, Fragment, View)。
 */
public class FragmentTransactionBuilder2 {
    public static final String TAG = FragmentTransactionBuilder2.class.getName();
    public static boolean DEBUG = true;

    @IntDef({TRANSIT_NONE, TRANSIT_FRAGMENT_OPEN, TRANSIT_FRAGMENT_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Transit {
    }

    private Content content;
    private Fragment fragment;
    private Class<? extends Fragment> fragmentClass;
    private Bundle fragmentArgs;
    private Fragment containerFragment;
    //
    public static int defaultContainerViewId = android.R.id.content;
    private int containerViewId = defaultContainerViewId;
    private Action action = Action.add;
    public static boolean defaultTraceable = false;
    private boolean isTraceable = defaultTraceable;
    private String fragmentTag;
    private String srcFragmentTag;
    private String assignBackStackName;
    private int srcViewId = 0;
    private String srcViewIndexChainListString = "";
    private final static String delimiter = ",";
    private long builtTimeMillis = 0;
    // addBackStack
    private int enter = 0;
    private int exit = 0;
    // onBackPressed trigger popEnter and popExit
    private int popEnter = 0;
    private int popExit = 0;
    //
    private int styleRes = 0;
    private
    @Transit
    int transition = TRANSIT_NONE;
    //

    private void setAnimations(FragmentTransaction ft) {
        ft.setCustomAnimations(enter, exit, popEnter, popExit);
        ft.setTransition(transition);
        ft.setTransitionStyle(styleRes);
    }

    public FragmentTransactionBuilder2 setCustomAnimations(int enter, int exit) {
        return setCustomAnimations(enter, exit, 0, 0);
    }

    public FragmentTransactionBuilder2 setCustomAnimations(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
        return this;
    }


    public FragmentTransactionBuilder2 setTransition(@Transit int transition) {
        this.transition = transition;
        return this;
    }

    public FragmentTransactionBuilder2 setTransitionStyle(int styleRes) {
        this.styleRes = styleRes;
        return this;
    }

    private FragmentManager getFragmentManager() {
        return content.getFragmentManager(containerViewId);
    }

    private FragmentTransactionBuilder2() {
    }

    private FragmentTransactionBuilder2(FragmentActivity srcFragmentActivity, Fragment srcFragment, int srcViewId) {
        this.content = new Content(srcFragmentActivity, srcFragment);
        this.srcFragmentTag = (srcFragment == null) ? "" : srcFragment.getTag();
        setSrcViewId(srcViewId);
    }

    public static FragmentTransactionBuilder2 create(View srcView) {
        FragmentTransactionBuilder2 builder = new FragmentTransactionBuilder2((FragmentActivity) srcView.getContext(), null, srcView.getId());
        ArrayList<Integer> srcViewPathList = new ArrayList<Integer>();
        findFragmentIndexChain(srcViewPathList, srcView);
        if (srcViewPathList.size() > 0) {
            builder.srcViewIndexChainListString = TextUtils.join(delimiter, srcViewPathList);
        }
        View findSrcView = findSrcView((FragmentActivity) srcView.getContext(), builder.srcViewIndexChainListString, srcView.getId());
        if (!findSrcView.equals(srcView)) {
            throw new RuntimeException(String.format("Could not match source view. You need unique id for %s.", srcView.toString()));
        } else {
            if (DEBUG) {
                Toast.makeText(srcView.getContext(), String.format("indexChain: %s", builder.srcViewIndexChainListString), Toast.LENGTH_SHORT).show();
            }
        }
        return builder;
    }

    public static FragmentTransactionBuilder2 create(FragmentActivity srcFragmentActivity) {
        return new FragmentTransactionBuilder2(srcFragmentActivity, null, 0);
    }

    public static FragmentTransactionBuilder2 create(Fragment srcFragment) {
        FragmentTransactionBuilder2 builder = new FragmentTransactionBuilder2(null, srcFragment, 0);
        // TODO 仿照 srcViewIndexChainListString 的作法，可以找到 srcFragmentManager
        // 目前只有在 popBackStack的時候，才知道自己是被誰呼叫的
        return builder;
    }

    public FragmentTransactionBuilder2 addToBackStack(String name) {
        this.assignBackStackName = (name == null) ? "" : name;
        setTraceable(true);
        return this;
    }

    public boolean isTraceable() {
        return isTraceable;
    }

    public FragmentTransactionBuilder2 setTraceable(boolean isTraceable) {
        this.isTraceable = isTraceable;
        if (!isTraceable && !TextUtils.isEmpty(assignBackStackName)) {
            throw new RuntimeException("If call addToBackStack method the isTraceable will be TRUE.");
        }
        return this;
    }

    public FragmentTransactionBuilder2 untraceable() {
        setTraceable(false);
        return this;
    }

    public FragmentTransactionBuilder2 traceable() {
        setTraceable(true);
        return this;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public FragmentTransactionBuilder2 setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
        return this;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    public FragmentTransactionBuilder2 setFragment(Fragment fragment, String tag) {
        if (this.fragmentClass != null) {
            throw new RuntimeException("Don't set fragment twice.");
        }
        this.fragment = fragment;
        this.fragmentTag = (tag == null) ? fragment.getClass().getName() : tag;
        return this;
    }

    public FragmentTransactionBuilder2 setFragment(Class<? extends Fragment> fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    public FragmentTransactionBuilder2 setFragment(Class<? extends Fragment> fragmentClass, String tag) {
        if (this.fragment != null) {
            throw new RuntimeException("Don't set fragment twice.");
        }
        this.fragmentClass = fragmentClass;
        this.fragmentTag = (tag == null) ? fragmentClass.getName() : tag;
        return this;
    }

    public FragmentTransactionBuilder2 setArgs(Bundle fragmentArgs) {
        this.fragmentArgs = fragmentArgs;
        return this;
    }

    public int getSrcViewId() {
        return this.srcViewId;
    }

    public FragmentTransactionBuilder2 setSrcViewId(int srcViewId) {
        if (srcViewId > 0) {
            if (content.srcFragmentActivity != null) {
                if (content.srcFragmentActivity.findViewById(srcViewId) == null) {
                    throw new RuntimeException("Didn't find sourceContentView");
                }
            } else {
                if (content.srcFragment.getView().findViewById(srcViewId) == null) {
                    throw new RuntimeException("Didn't find sourceContentView");
                }
            }
        }
        this.srcViewId = srcViewId;
        return this;
    }

    public FragmentTransactionBuilder2 reset() {
        this.action = Action.reset;
        return this;
    }

    public FragmentTransactionBuilder2 reset(int containerViewId, Fragment fragment) {
        add(containerViewId, fragment, null);
        return this;
    }

    public FragmentTransactionBuilder2 reset(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        reset();
        return this;
    }

    public FragmentTransactionBuilder2 add() {
        this.action = Action.add;
        return this;
    }

    public FragmentTransactionBuilder2 add(int containerViewId, Fragment fragment) {
        add(containerViewId, fragment, null);
        return this;
    }

    public FragmentTransactionBuilder2 add(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        add();
        return this;
    }

    public FragmentTransactionBuilder2 replace() {
        this.action = Action.replace;
        return this;
    }

    public FragmentTransactionBuilder2 replace(int containerViewId, Fragment fragment) {
        return replace(containerViewId, fragment, null);
    }

    public FragmentTransactionBuilder2 replace(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        replace();
        return this;
    }

    private void setCommon(int containerViewId, Fragment fragment, String tag) {
        setContainerViewId(containerViewId);
        setFragment(fragment, tag);
    }

    private static void doIfResetAction(FragmentTransactionBuilder2 builder) {
        // If do something in same containerViewId,call popBackStack() and reset new action
        if (Action.reset != builder.action) {
            return;
        }
        FragmentManager fragmentManager = builder.getFragmentManager();

        FragmentManager.BackStackEntry lastBackStack = null;
        if (fragmentManager.getBackStackEntryCount() > 0) {
            lastBackStack = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
        }
        if (lastBackStack != null) {
            FragmentTransactionBuilder2 lastBuilder = parse(lastBackStack);
            if (lastBuilder.containerViewId == builder.containerViewId) {
                fragmentManager.popBackStack();
                //
                builder.action = lastBuilder.action;
                builder.isTraceable = lastBuilder.isTraceable;
                builder.srcFragmentTag = lastBuilder.srcFragmentTag;
                builder.srcViewId = lastBuilder.srcViewId;
                //
                builder.enter = lastBuilder.enter;
                builder.exit = lastBuilder.exit;
                builder.popEnter = lastBuilder.popEnter;
                builder.popExit = lastBuilder.popExit;
                //
                builder.srcViewIndexChainListString = lastBuilder.srcViewIndexChainListString;

                // Find containerFragment in containerView
                builder.containerFragment = null;
                FragmentManager.BackStackEntry beforeLastStackEntry = null;
                // The before last BackStack is last action's containerFragment
                // Start from second-last to 0
                int index = fragmentManager.getBackStackEntryCount() - 2;
                if (index > -1) {
                    do {
                        beforeLastStackEntry = fragmentManager.getBackStackEntryAt(index--);
                        if (beforeLastStackEntry == null) {
                            continue;
                        }
                        FragmentTransactionBuilder2 beforeLastBuilder = parse(beforeLastStackEntry);
                        if (beforeLastBuilder == null) {
                            continue;
                        }
                        if (beforeLastBuilder.containerViewId == builder.containerViewId) {
                            builder.containerFragment = fragmentManager.findFragmentByTag(beforeLastBuilder.fragmentTag);
                            break;
                        }
                    } while (index > -1);
                }
            }
        }
    }

    public void buildImmediate() {
        if (content == null) {
            throw new RuntimeException("Forbid build!");
        }
        FragmentManager fragmentManager = getFragmentManager();

        // Check fragment already exist
        final Fragment fragmentAlreadyExist = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragmentAlreadyExist != null) {
            Log.w(TAG, String.format("Fragment is exist in fragmentManager. tag: %s", fragmentTag));
            // TODO 更新參數後
            return;
        }
        // Setting FragmentArgs
        if (fragment == null) {
            if (fragmentArgs == null) {
                fragment = Fragment.instantiate(content.getFragmentActivity(), fragmentClass.getName());
            } else {
                fragment = Fragment.instantiate(content.getFragmentActivity(), fragmentClass.getName(), fragmentArgs);
            }
        } else {
            if (fragmentArgs != null) {
                FragmentUtils.putArguments(fragment, fragmentArgs);
            }
        }
        // Maybe execute  action of reset
        containerFragment = fragmentManager.findFragmentById(containerViewId);
        doIfResetAction(this);
        // Prepare transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (isTraceable()) {
            builtTimeMillis = System.currentTimeMillis();
            String backStackName = generateBackStackName(this);
            ft.addToBackStack(backStackName);
        }
        // Animation setting
        setAnimations(ft);
        // TODO  Action.add != action
        if (Action.replace == action) {
            // Affect containerFragment.
            if (containerFragment != null) {
                if (isTraceable()) {
                    ft.detach(containerFragment);
                } else {
                    ft.remove(containerFragment);
                }
            }
        }
        // Show fragment
        ft.add(containerViewId, fragment, fragmentTag);
        ft.commit();
    }

    public void build() {
        if (content == null) {
            throw new RuntimeException("Forbid build!");
        }
        content.getDecorView().post(new Runnable() {
            @Override
            public void run() {
                buildImmediate();
            }
        });
    }

    public enum Action {
        add, replace, reset
    }

    public static class Content {
        private static String noExistContainerView = "Didn't find container view.";
        public final FragmentActivity srcFragmentActivity;
        public final Fragment srcFragment;

        public Content(FragmentActivity srcFragmentActivity, Fragment srcFragment) {
            this.srcFragmentActivity = srcFragmentActivity;
            this.srcFragment = srcFragment;
        }

        private FragmentManager fragmentManager = null;

        public FragmentActivity getFragmentActivity() {
            if (srcFragmentActivity != null) {
                return srcFragmentActivity;
            } else {
                return srcFragment.getActivity();
            }
        }

        public View getDecorView() {
            View rootView = null;
            if (srcFragmentActivity != null) {
                rootView = srcFragmentActivity.getWindow().getDecorView();
            } else {
                rootView = srcFragment.getActivity().getWindow().getDecorView();
            }
            return rootView;
        }

        public FragmentManager getFragmentManager(int containerViewId) {
            if (fragmentManager != null) {
                return fragmentManager;
            }
            if (null != srcFragment) {
                if (null != srcFragment.getView().findViewById(containerViewId)) {
                    fragmentManager = srcFragment.getChildFragmentManager();
                } else if (null != srcFragment.getActivity().findViewById(containerViewId)) {
                    fragmentManager = srcFragment.getActivity().getSupportFragmentManager();
                } else {
                    throw new RuntimeException(noExistContainerView);
                }
            } else if (null != srcFragmentActivity.findViewById(containerViewId)) {
                fragmentManager = srcFragmentActivity.getSupportFragmentManager();
            } else {
                throw new RuntimeException(noExistContainerView);
            }
            return fragmentManager;
        }
    }

    public static String generateBackStackName(FragmentTransactionBuilder2 builder) {
        String backStackName;
        backStackName = String.format("%s %s %s [%s][%s][%s][%s] %s %s,%s,%s,%s,%s,%s [%s]",
                builder.containerViewId,
                builder.action.toString(),
                builder.isTraceable,
                builder.fragmentTag,
                builder.srcFragmentTag,
                builder.srcViewId,
                builder.assignBackStackName,
                builder.builtTimeMillis,
                builder.transition, builder.styleRes, builder.enter, builder.exit, builder.popEnter, builder.popExit,
                builder.srcViewIndexChainListString
        );
        return backStackName;
    }

    public static FragmentTransactionBuilder2 parse(BackStackEntry stackEntry) {
        FragmentTransactionBuilder2 builder = null;
        Pattern p = Pattern.compile("(\\d+) (\\S+) (\\S+) \\[(.*)\\]\\[(.*)\\]\\[(.*)\\]\\[(.*)\\] (\\d+) (\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+) \\[(.*)\\]");
        Matcher m = p.matcher(stackEntry.getName());
        if (m.find()) {
            int i = 1;
            builder = new FragmentTransactionBuilder2();
            builder.containerViewId = Integer.parseInt(m.group(i++));
            builder.action = Action.valueOf(m.group(i++));
            builder.isTraceable = Boolean.parseBoolean(m.group(i++));
            builder.fragmentTag = m.group(i++);
            builder.srcFragmentTag = m.group(i++);
            builder.srcViewId = Integer.parseInt(m.group(i++));
            builder.assignBackStackName = m.group(i++);
            //
            builder.builtTimeMillis = Long.parseLong(m.group(i++));
            //
            builder.transition = covertToTransit(Integer.parseInt(m.group(i++)));
            builder.styleRes = Integer.parseInt(m.group(i++));
            builder.enter = Integer.parseInt(m.group(i++));
            builder.exit = Integer.parseInt(m.group(i++));
            builder.popEnter = Integer.parseInt(m.group(i++));
            builder.popExit = Integer.parseInt(m.group(i++));
            //
            builder.srcViewIndexChainListString = m.group(i++);
        }
        return builder;
    }

    public static boolean hasPopBackStack(FragmentActivity activity) {
        ArrayList<BackStackEntry> list = new ArrayList<BackStackEntry>();
        HashMap<BackStackEntry, FragmentManager> map = new HashMap<BackStackEntry, FragmentManager>();
        HashMap<BackStackEntry, Fragment> srcFragMap = new HashMap<BackStackEntry, Fragment>();
        findLeavesBackStack(null, srcFragMap, activity.getSupportFragmentManager(), list, map);
        if (list.size() > 0) {
            BackStackEntry lastEntry = list.remove(0);
            FragmentTransactionBuilder2 lastBuilder = parse(lastEntry);
            for (BackStackEntry entry : list) {
                if (parse(entry).builtTimeMillis > lastBuilder.builtTimeMillis) {
                    lastEntry = entry;
                    lastBuilder = parse(lastEntry);
                }
            }
            FragmentManager srcFragmentManager = map.get(lastEntry);
            Fragment srcFragment = srcFragMap.get(lastEntry);
            Fragment popFragment = srcFragmentManager.findFragmentByTag(lastBuilder.fragmentTag);
            new NotifyPopFragment(activity, srcFragment, srcFragmentManager, popFragment, lastBuilder);
            srcFragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    private static void findLeavesBackStack(Fragment srcFrag, Map<BackStackEntry, Fragment> srcFragMap, FragmentManager fm, List<BackStackEntry> leaves, Map<BackStackEntry, FragmentManager> map) {
        if (fm != null) {
            if (fm.getBackStackEntryCount() > 0) {
                BackStackEntry backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
                leaves.add(backStackEntry);
                map.put(backStackEntry, fm);
                srcFragMap.put(backStackEntry, srcFrag);
            }
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) {
                        continue;
                    }
                    if (frag.isVisible()) {
                        findLeavesBackStack(frag, srcFragMap, frag.getChildFragmentManager(), leaves, map);
                    }
                }
            }
        }
    }

    // ******findFragmentIndexChain***** START
    public static View findSrcView(FragmentActivity activity, String indexChainListString, int srcViewId) {
        List<Integer> indexChainList = new ArrayList<Integer>();
        for (String index : TextUtils.split(indexChainListString, delimiter)) {
            indexChainList.add(Integer.parseInt(index));
        }
        return findSrcView(activity, indexChainList, srcViewId);
    }

    public static View findSrcView(FragmentActivity activity, List<Integer> indexChainList, int srcViewId) {
        View view = null;
        if (indexChainList == null || indexChainList.size() == 0) {
            view = activity.findViewById(srcViewId);
        } else {
            FragmentManager fm = activity.getSupportFragmentManager();
            Fragment fragment = null;
            for (Integer index : indexChainList) {
                fragment = fm.getFragments().get(index);
                fm = fragment.getChildFragmentManager();
            }
            view = fragment.getView().findViewById(srcViewId);
        }
        return view;
    }

    public static void findFragmentIndexChain(List<Integer> indexChainList, View srcView) {
        FragmentActivity activity = (FragmentActivity) srcView.getContext();
        ArrayList<Fragment> fragmentArrayList = new ArrayList<Fragment>();
        HashMap<Fragment, Fragment> parentMap = new HashMap<Fragment, Fragment>();
        HashMap<Fragment, Integer> fragmentIndexMap = new HashMap<Fragment, Integer>();
        findAllFragments(null, parentMap, activity.getSupportFragmentManager(), fragmentArrayList, fragmentIndexMap);
        if (fragmentArrayList.size() > 0) {
            Fragment srcFragment = findFragmentByView(fragmentArrayList, srcView);
            if (srcFragment != null) {
                buildFragmentIndexChain(indexChainList, parentMap, fragmentIndexMap, srcFragment);
            }
        }
    }

    private static void findAllFragments(Fragment parent, Map<Fragment, Fragment> parentMap, FragmentManager fm, List<Fragment> fragmentList, Map<Fragment, Integer> fragmentIndexMap) {
        if (fm != null) {
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                int index = -1;
                for (Fragment frag : fragList) {
                    index = index + 1;
                    if (frag == null) {
                        continue;
                    }
                    fragmentList.add(frag);
                    parentMap.put(frag, parent);
                    fragmentIndexMap.put(frag, index);
                    findAllFragments(frag, parentMap, frag.getChildFragmentManager(), fragmentList, fragmentIndexMap);
                }
            }
        }
    }

    private static Fragment findFragmentByView(List<Fragment> fragmentList, View srcView) {
        for (Fragment frag : fragmentList) {
            if (frag.getView() != null) {
                if (frag.getView().equals(srcView)) {
                    return frag;
                }
            }
        }
        if (srcView.getParent().equals(srcView.getRootView())) {
            return null;
        }
        View parent = (View) srcView.getParent();
        if (parent != null) {
            return findFragmentByView(fragmentList, parent);
        } else {
            return null;
        }
    }

    private static void buildFragmentIndexChain(List<Integer> indexChainList, Map<Fragment, Fragment> parentMap, Map<Fragment, Integer> fragmentIndexMap, Fragment srcFragment) {
        indexChainList.add(0, fragmentIndexMap.get(srcFragment));
        Fragment parentFragment = parentMap.get(srcFragment);
        if (parentFragment == null) {
            return;
        } else {
            buildFragmentIndexChain(indexChainList, parentMap, fragmentIndexMap, parentFragment);
        }
    }
    // ******findFragmentIndexChain***** END

    private static class NotifyPopFragment implements FragmentManager.OnBackStackChangedListener {
        private FragmentActivity srcFragmentActivity;
        private Fragment srcFragment;
        private FragmentManager srcFragmentManager;
        private Fragment popFragment;

        private FragmentTransactionBuilder2 builder;

        public NotifyPopFragment(FragmentActivity srcFragmentActivity, Fragment srcFragment, FragmentManager fragmentManager, Fragment popFragment, FragmentTransactionBuilder2 builder) {
            this.srcFragmentActivity = srcFragmentActivity;
            this.srcFragment = srcFragment;
            this.srcFragmentManager = fragmentManager;
            this.popFragment = popFragment;
            this.builder = builder;
            fragmentManager.addOnBackStackChangedListener(this);
        }

        @Override
        public void onBackStackChanged() {
            srcFragmentManager.removeOnBackStackChangedListener(this);
            PopFragmentListener listener = null;
            Object srcObject = null;
            if (srcFragment == null) {
                if (builder.srcViewId == 0) {
                    srcObject = srcFragmentActivity;
                } else {
                    srcObject = findSrcView(srcFragmentActivity, builder.srcViewIndexChainListString, builder.srcViewId);
                }
            } else {
                if (builder.srcViewId == 0) {
                    srcObject = srcFragment;
                } else {
                    srcObject = srcFragment.getView().findViewById(builder.srcViewId);
                }
            }
            if (srcObject != null) {
                sendOnPopFragment(srcObject, popFragment);
            }
        }

        private static void sendOnPopFragment(Object src, Fragment fragment) {
            if (src == null) {
                return;
            }
            Class<?> srcClass = src.getClass();
            try {
                Method method = srcClass.getDeclaredMethod("onPopFragment", fragment.getClass());
                if (method != null) {
                    method.invoke(src, fragment);
                    return;
                }
            } catch (Throwable ex) {
            }
            if (src instanceof PopFragmentListener) {
                ((PopFragmentListener) src).onPopFragment(fragment);
            }
        }
    }

    public interface PopFragmentListener {
        void onPopFragment(Fragment fragment);
    }

    private static
    @Transit
    int covertToTransit(int transition) {
        @Transit int result = TRANSIT_NONE;
        switch (transition) {
            case TRANSIT_FRAGMENT_OPEN:
                result = TRANSIT_FRAGMENT_OPEN;
                break;
            case TRANSIT_FRAGMENT_CLOSE:
                result = TRANSIT_FRAGMENT_CLOSE;
                break;
        }
        return result;
    }
}



