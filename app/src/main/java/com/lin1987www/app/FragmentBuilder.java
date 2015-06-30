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
import java.util.Collections;
import java.util.Comparator;
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
 * TODO 策畫所以常用Use Case
 * 0. popStackName(   name of a previous back state )
 * onPushFragmentListener(  ) -> fragment detach notify src
 * onBuildFragmentListener( add ) ->  fragment add notify src
 * back() -> back  from FragmentPath
 * <p/>
 * 1. Wizard Step
 * 2. Fragment -> Fragment ( Outside )
 * 2. View -> Fragment ( Outside )
 * 3. FragmentActivity -> Fragment ( Inside * Only way )
 * 3. Fragment -> Fragment (Inside)
 * 4. View -> Fragment ( Inside ) * 應該不會用到
 * TODO 超級可能性
 * 當因為 onBackStack 發生時，FragmentActivity 處理 onBackStack時，觸發FragmentTransactionBuilder2.popBackStack()   Stong Ref 到那個Fragment ，然後監聽 onBackStackChanged() 時，再將Fragment 丟回去。
 * 當 onBackPressed 去呼叫特定 FragmentManager 去執行 popBackStack，但執行前，透過預先儲存在 BackEntry中Name屬性的相關資料，得知這Fragment是"誰"呼叫，然後使用addOnBackStackChangedListener當 Fragment 被移除的時候，丟回去原本呼叫的地方(FragmentActivity, Fragment, View)。
 */
public class FragmentBuilder {
    public static final String TAG = FragmentBuilder.class.getName();
    public static boolean DEBUG = true;

    @IntDef({TRANSIT_NONE, TRANSIT_FRAGMENT_OPEN, TRANSIT_FRAGMENT_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Transit {
    }

    // Temp Object
    private Content content;
    private Fragment fragment;
    private Class<? extends Fragment> fragmentClass;
    private Bundle fragmentArgs;
    private Fragment containerFragment;
    private int backTimes = 0;
    //
    //
    public static int defaultContainerViewId = android.R.id.content;
    private int containerViewId = defaultContainerViewId;
    //
    public static Action defaultAction = Action.add;
    private Action action = Action.none;
    public static boolean defaultTraceable = false;
    private boolean isTraceable = defaultTraceable;
    //
    private String assignBackStackName = "";
    private String fragmentTag;
    private String srcFragmentPathString = "";
    private String srcFragmentTag;
    private int srcViewId = 0;
    private long builtTimeMillis = 0;
    //
    // Animations
    //
    private
    @Transit
    int transition = TRANSIT_NONE;
    private int styleRes = 0;
    // addBackStack
    private int enter = 0;
    private int exit = 0;
    // onBackPressed trigger popEnter and popExit
    private int popEnter = 0;
    private int popExit = 0;
    //

    private void setAnimations(FragmentTransaction ft) {
        ft.setCustomAnimations(enter, exit, popEnter, popExit);
        ft.setTransition(transition);
        ft.setTransitionStyle(styleRes);
    }

    public FragmentBuilder setCustomAnimations(int enter, int exit) {
        return setCustomAnimations(enter, exit, 0, 0);
    }

    public FragmentBuilder setCustomAnimations(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
        return this;
    }

    public FragmentBuilder setTransition(@Transit int transition) {
        this.transition = transition;
        return this;
    }

    public FragmentBuilder setTransitionStyle(int styleRes) {
        this.styleRes = styleRes;
        return this;
    }

    private FragmentManager getFragmentManager() {
        return content.getFragmentManager(containerViewId);
    }

    private FragmentBuilder() {
    }

    private FragmentBuilder(FragmentActivity srcFragmentActivity, Fragment srcFragment, View srcView) {
        this.content = new Content(srcFragmentActivity, srcFragment, srcView);
        this.srcFragmentTag = (srcFragment == null) ? "" : srcFragment.getTag();
        if (srcView != null) {
            setSrcViewId(srcView.getId());
        }
    }

    public static FragmentBuilder create(FragmentActivity srcFragmentActivity) {
        return new FragmentBuilder(srcFragmentActivity, null, null);
    }

    public static FragmentBuilder create(Fragment srcFragment) {
        FragmentBuilder builder = new FragmentBuilder(null, srcFragment, null);
        return builder;
    }

    public static FragmentBuilder create(View srcView) {
        FragmentBuilder builder = new FragmentBuilder(null, null, srcView);
        return builder;
    }

    public FragmentBuilder addToBackStack(String name) {
        this.assignBackStackName = (name == null) ? "" : name;
        setTraceable(true);
        return this;
    }

    public boolean isTraceable() {
        return isTraceable;
    }

    public FragmentBuilder setTraceable(boolean isTraceable) {
        this.isTraceable = isTraceable;
        if (!isTraceable && !TextUtils.isEmpty(assignBackStackName)) {
            throw new RuntimeException("If call addToBackStack method the isTraceable will be TRUE.");
        }
        return this;
    }

    public FragmentBuilder untraceable() {
        setTraceable(false);
        return this;
    }

    public FragmentBuilder traceable() {
        setTraceable(true);
        return this;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public FragmentBuilder setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
        return this;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    public FragmentBuilder setFragment(Fragment fragment, String tag) {
        if (this.fragmentClass != null) {
            throw new RuntimeException("Don't set fragment twice.");
        }
        this.fragment = fragment;
        this.fragmentTag = (tag == null) ? fragment.getClass().getName() : tag;
        return this;
    }

    public FragmentBuilder setFragment(Class<? extends Fragment> fragmentClass) {
        return setFragment(fragmentClass, null);
    }

    public FragmentBuilder setFragment(Class<? extends Fragment> fragmentClass, String tag) {
        if (this.fragment != null) {
            throw new RuntimeException("Don't set fragment twice.");
        }
        this.fragmentClass = fragmentClass;
        this.fragmentTag = (tag == null) ? fragmentClass.getName() : tag;
        return this;
    }

    public FragmentBuilder setArgs(Bundle fragmentArgs) {
        this.fragmentArgs = fragmentArgs;
        return this;
    }

    public int getSrcViewId() {
        return this.srcViewId;
    }

    public FragmentBuilder setSrcViewId(int srcViewId) {
        if (srcViewId > 0) {
            if (content.srcFragmentActivity != null) {
                if (content.srcFragmentActivity.findViewById(srcViewId) == null) {
                    throw new RuntimeException("Didn't find sourceContentView");
                }
            } else if (content.srcFragment != null) {
                if (content.srcFragment.getView().findViewById(srcViewId) == null) {
                    throw new RuntimeException("Didn't find sourceContentView");
                }
            } else {
                if (((FragmentActivity) content.srcView.getContext()).findViewById(srcViewId) == null) {
                    throw new RuntimeException("Didn't find sourceContentView");
                }
            }
        }
        this.srcViewId = srcViewId;
        return this;
    }

    public FragmentBuilder back() {
        return back(1);
    }

    /**
     * back
     *
     * @param goBackFragmentPathTimes
     * @return
     */
    public FragmentBuilder back(int goBackFragmentPathTimes) {
        this.backTimes = goBackFragmentPathTimes;
        if (content.srcFragmentActivity != null) {
            throw new RuntimeException("FragmentActivity couldn't back! It is Root.");
        }
        return this;
    }

    public FragmentBuilder reset() {
        this.action = Action.reset;
        return this;
    }

    public FragmentBuilder reset(int containerViewId, Fragment fragment) {
        add(containerViewId, fragment, null);
        return this;
    }

    public FragmentBuilder reset(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        reset();
        return this;
    }

    public FragmentBuilder add() {
        this.action = Action.add;
        return this;
    }

    public FragmentBuilder add(int containerViewId, Fragment fragment) {
        add(containerViewId, fragment, null);
        return this;
    }

    public FragmentBuilder add(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        add();
        return this;
    }

    public FragmentBuilder replace() {
        this.action = Action.replace;
        return this;
    }

    public FragmentBuilder replace(int containerViewId, Fragment fragment) {
        return replace(containerViewId, fragment, null);
    }

    public FragmentBuilder replace(int containerViewId, Fragment fragment, String tag) {
        setCommon(containerViewId, fragment, tag);
        replace();
        return this;
    }

    private void setCommon(int containerViewId, Fragment fragment, String tag) {
        setContainerViewId(containerViewId);
        setFragment(fragment, tag);
    }

    private static void doIfResetAction(FragmentBuilder builder) {
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
            FragmentBuilder lastBuilder = parse(lastBackStack);
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
                builder.srcFragmentPathString = lastBuilder.srcFragmentPathString;

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
                        FragmentBuilder beforeLastBuilder = parse(beforeLastStackEntry);
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

    private static void doBack(FragmentBuilder builder) {
        if (builder.backTimes <= 0) {
            return;
        }

        String fragmentTag = FragmentPath.findFragmentByView(builder.content.getSrcView()).getTag();
        FragmentBuilder backBuilder = null;
        BackStackEntry backStackEntry;
        FragmentManager fm = getParentFragmentManager(builder.content);
        for (int i = fm.getBackStackEntryCount() - 1; i > -1; i--) {
            backStackEntry = fm.getBackStackEntryAt(i);
            backBuilder = parse(backStackEntry);
            if (backBuilder.fragmentTag.equals(fragmentTag)) {
                break;
            }
            backBuilder = null;
        }
        if (backBuilder != null) {
            // Fill Content Object to backBuilder
            fillContent(backBuilder, builder.content.getFragmentActivity());
            String originFragmentPath = FragmentPath.getFragmentPathString(builder.content);
            Fragment originFragment = FragmentPath.findFragment(builder.content.getFragmentActivity(), originFragmentPath);
            int originViewId = builder.srcViewId;
            overrideBuilder(backBuilder, builder);
            // TODO
            builder.srcFragmentPathString = originFragmentPath;
            builder.srcViewId = originViewId;
            builder.srcFragmentTag = originFragment.getTag();
        } else {
            //TODO
        }
    }

    public static FragmentManager getParentFragmentManager(Content content) {
        return getBackFragmentManager(content, 1);
    }

    public static FragmentManager getBackFragmentManager(Content content, int backTimes) {
        Content backContent = getBackContent(content, backTimes);
        FragmentManager fragmentManager;
        if (backContent.srcFragmentActivity != null) {
            fragmentManager = backContent.srcFragmentActivity.getSupportFragmentManager();
        } else {
            fragmentManager = backContent.srcFragment.getChildFragmentManager();
        }
        return fragmentManager;
    }

    public static Content getBackContent(Content content, int backTimes) {
        if (backTimes <= 0) {
            return null;
        }
        FragmentPath fragmentPath = FragmentPath.getFragmentPath(content);
        Content backContent = null;
        int size = fragmentPath.size() - backTimes;
        if (size < 0) {
            throw new RuntimeException("Go back too many time.");
        } else if (size == 0) {
            // FragmentActivity
            backContent = new Content(content.getFragmentActivity());
        } else {
            // Fragment
            Fragment fragment = FragmentPath.findFragment(content.getFragmentActivity(), fragmentPath.subList(0, size));
            backContent = new Content(fragment);
        }
        return backContent;
    }

    public static void overrideBuilder(FragmentBuilder defaultBuilder, FragmentBuilder builder) {
        builder.content = defaultBuilder.content;
        builder.containerViewId = defaultBuilder.containerViewId;

        if (builder.action == Action.none) {
            builder.action = defaultBuilder.action;
            builder.isTraceable = defaultBuilder.isTraceable;
        }

        if ((builder.styleRes == 0) && (builder.transition == TRANSIT_NONE) &&
                (builder.enter == 0) && (builder.exit == 0) && (builder.popEnter == 0) && (builder.popExit == 0)
                ) {
            builder.styleRes = defaultBuilder.styleRes;
            builder.transition = defaultBuilder.transition;
            builder.enter = defaultBuilder.enter;
            builder.exit = defaultBuilder.exit;
            builder.popEnter = defaultBuilder.popEnter;
            builder.popExit = defaultBuilder.popExit;
        }
    }

    public void buildImmediate() {
        if (content == null) {
            throw new RuntimeException("Forbid build!");
        }
        // doBack(this);
        if (action.equals(Action.none)) {
            action = defaultAction;
        }

        this.srcFragmentPathString = FragmentPath.getFragmentPathString(this);

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
        // Maybe execute action of reset
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

    protected enum Action {
        add, replace, reset, none
    }

    /**
     *  Provider  同時預設也是 接收 onPopFragment 的接收者，但是為了實作 Wizard Steps 可能要再想想...
     */
    public static class Content {
        private static String noExistContainerView = "Didn't find container view.";
        public final FragmentActivity srcFragmentActivity;
        public final Fragment srcFragment;
        public final View srcView;
        private FragmentManager fragmentManager = null;

        public Content(FragmentActivity srcFragmentActivity) {
            this(srcFragmentActivity, null, null);
            if (srcFragmentActivity == null) {
                throw new NullPointerException();
            }
        }

        public Content(Fragment srcFragment) {
            this(null, srcFragment, null);
            if (srcFragment == null) {
                throw new NullPointerException();
            }
        }

        public Content(View srcView) {
            this(null, null, srcView);
            if (srcView == null) {
                throw new NullPointerException();
            }
        }

        public Content(FragmentActivity srcFragmentActivity, Fragment srcFragment, View srcView) {
            this.srcFragmentActivity = srcFragmentActivity;
            this.srcFragment = srcFragment;
            this.srcView = srcView;
            if (srcView != null) {
                if (srcView.getId() <= 0) {
                    throw new RuntimeException(String.format("You must set id to %s", srcView));
                }
                if (!(srcView.getContext() instanceof FragmentActivity)) {
                    throw new RuntimeException(String.format("You must use FragmentActivity. %s", srcView));
                }
            }
        }

        public FragmentActivity getFragmentActivity() {
            if (srcFragmentActivity != null) {
                return srcFragmentActivity;
            } else if (srcFragment != null) {
                return srcFragment.getActivity();
            } else {
                return (FragmentActivity) srcView.getContext();
            }
        }

        public View getDecorView() {
            View rootView = null;
            if (srcFragmentActivity != null) {
                rootView = srcFragmentActivity.getWindow().getDecorView();
            } else if (srcFragment != null) {
                rootView = srcFragment.getActivity().getWindow().getDecorView();
            } else {
                rootView = srcView.getRootView();
            }
            return rootView;
        }

        public void setContainerViewId(int containerViewId) {
            FragmentPath fragmentPath = FragmentPath.getFragmentPath(this);
            while (fragmentPath.size() > 0) {
                Fragment frag = FragmentPath.findFragment(getFragmentActivity(), fragmentPath);
                if (null != frag.getView().findViewById(containerViewId)) {
                    // TODO 保存 srcFragmentTag ? 應該不需要  因為他只是建立 Fragemnt 的Fragment  並非實際目的地的Fragment
                    fragmentManager = frag.getChildFragmentManager();
                    break;
                } else {
                    fragmentPath.back();
                }
            }
            if (fragmentManager == null) {
                if (null != getFragmentActivity().findViewById(containerViewId)) {
                    fragmentManager = getFragmentActivity().getSupportFragmentManager();
                } else {
                    throw new RuntimeException(noExistContainerView);
                }
            }
        }

        public FragmentManager getFragmentManager(int containerViewId) {
            if (fragmentManager != null) {
                return fragmentManager;
            } else {
                setContainerViewId(containerViewId);
            }
            return fragmentManager;
        }

        public View getSrcView() {
            View view = null;
            if (srcView != null) {
                view = srcView;
            } else if (srcFragment != null) {
                view = srcFragment.getView();
            }
            return view;
        }
    }

    public static String generateBackStackName(FragmentBuilder builder) {
        String backStackName;
        backStackName = String.format("%s %s %s [%s][%s][%s][%s] %s %s,%s,%s,%s,%s,%s [%s]",
                builder.containerViewId,
                builder.action.toString(),
                builder.isTraceable,
                //
                builder.fragmentTag,
                builder.srcFragmentTag,
                builder.srcViewId,
                builder.assignBackStackName,
                //
                builder.builtTimeMillis,
                //
                builder.transition, builder.styleRes, builder.enter, builder.exit, builder.popEnter, builder.popExit,
                //
                builder.srcFragmentPathString
        );
        return backStackName;
    }

    public static FragmentBuilder parse(BackStackEntry stackEntry) {
        FragmentBuilder builder = null;
        Pattern p = Pattern.compile("(\\d+) (\\S+) (\\S+) \\[(.*)\\]\\[(.*)\\]\\[(.*)\\]\\[(.*)\\] (\\d+) (\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+) \\[(.*)\\]");
        Matcher m = p.matcher(stackEntry.getName());
        if (m.find()) {
            int i = 1;
            builder = new FragmentBuilder();
            builder.containerViewId = Integer.parseInt(m.group(i++));
            builder.action = Action.valueOf(m.group(i++));
            builder.isTraceable = Boolean.parseBoolean(m.group(i++));
            //
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
            builder.srcFragmentPathString = m.group(i++);
        }
        return builder;
    }

    public static void fillContent(FragmentBuilder builder, FragmentActivity activity) {
        Content content;
        if (builder.srcFragmentPathString.equals("")) {
            if (builder.srcViewId == 0) {
                content = new Content(activity);
            } else {
                content = new Content(activity.findViewById(builder.srcViewId));
            }
        } else {
            Fragment fragment = FragmentPath.findFragment(activity, builder.srcFragmentPathString);
            if (builder.srcViewId == 0) {
                content = new Content(fragment);
            } else {
                content = new Content(fragment.getView().findViewById(builder.srcViewId));
            }
        }
        builder.content = content;
    }

    public static void popBackStack(FragmentActivity activity, String name, int flags) {
        ArrayList<BackStackEntry> list = new ArrayList<BackStackEntry>();
        HashMap<BackStackEntry, FragmentManager> srcFmMap = new HashMap<BackStackEntry, FragmentManager>();
        HashMap<BackStackEntry, Fragment> srcFragMap = new HashMap<BackStackEntry, Fragment>();

        findAllBackStack(null, srcFragMap, activity.getSupportFragmentManager(), list, srcFmMap);
        if (list.size() > 0) {
            Collections.sort(list, new Comparator<BackStackEntry>() {
                @Override
                public int compare(BackStackEntry o1, BackStackEntry o2) {
                    return (int) (parse(o1).builtTimeMillis - parse(o2).builtTimeMillis);
                }
            });
        }
        List<NotifyPopFragment> notifyPopFragmentList = new ArrayList<NotifyPopFragment>();
        for (int i = list.size() - 1; i > -1; i--) {
            BackStackEntry backStackEntry = list.get(i);
            FragmentBuilder builder = parse(list.get(i));
            FragmentManager srcFragmentManager = srcFmMap.get(backStackEntry);
            Fragment srcFragment = srcFragMap.get(backStackEntry);
            Fragment popFragment = srcFragmentManager.findFragmentByTag(builder.fragmentTag);
            if (name != null) {
                if (builder.assignBackStackName.equals(name)) {
                    if (flags == FragmentManager.POP_BACK_STACK_INCLUSIVE) {
                        notifyPopFragmentList.add(new NotifyPopFragment(activity, srcFragment, srcFragmentManager, popFragment, builder));
                    }
                    break;
                } else {
                    notifyPopFragmentList.add(new NotifyPopFragment(activity, srcFragment, srcFragmentManager, popFragment, builder));
                }
            } else {
                notifyPopFragmentList.add(new NotifyPopFragment(activity, srcFragment, srcFragmentManager, popFragment, builder));
            }
        }
        for (NotifyPopFragment notify : notifyPopFragmentList) {
            notify.popBackStack();
        }
    }

    public static boolean hasPopBackStack(FragmentActivity activity) {
        ArrayList<BackStackEntry> list = new ArrayList<BackStackEntry>();
        HashMap<BackStackEntry, FragmentManager> srcFmMap = new HashMap<BackStackEntry, FragmentManager>();
        HashMap<BackStackEntry, Fragment> srcFragMap = new HashMap<BackStackEntry, Fragment>();
        findLeavesBackStack(null, srcFragMap, activity.getSupportFragmentManager(), list, srcFmMap);
        if (list.size() > 0) {
            BackStackEntry lastEntry = list.remove(0);
            FragmentBuilder lastBuilder = parse(lastEntry);
            for (BackStackEntry entry : list) {
                if (parse(entry).builtTimeMillis > lastBuilder.builtTimeMillis) {
                    lastEntry = entry;
                    lastBuilder = parse(lastEntry);
                }
            }
            FragmentManager srcFragmentManager = srcFmMap.get(lastEntry);
            Fragment srcFragment = srcFragMap.get(lastEntry);
            Fragment popFragment = srcFragmentManager.findFragmentByTag(lastBuilder.fragmentTag);
            new NotifyPopFragment(activity, srcFragment, srcFragmentManager, popFragment, lastBuilder).popBackStack();

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

    private static void findAllBackStack(Fragment srcFrag, Map<BackStackEntry, Fragment> fragMap, FragmentManager fm, List<BackStackEntry> leaves, Map<BackStackEntry, FragmentManager> map) {
        if (fm != null) {
            if (fm.getBackStackEntryCount() > 0) {
                for (int i = fm.getBackStackEntryCount() - 1; i > -1; i--) {
                    BackStackEntry backStackEntry = fm.getBackStackEntryAt(i);
                    leaves.add(backStackEntry);
                    map.put(backStackEntry, fm);
                    fragMap.put(backStackEntry, srcFrag);
                }
            }
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) {
                        continue;
                    }
                    findAllBackStack(frag, fragMap, frag.getChildFragmentManager(), leaves, map);
                }
            }
        }
    }

    private static class NotifyPopFragment implements FragmentManager.OnBackStackChangedListener {
        private FragmentActivity srcFragmentActivity;
        // TODO 這應該不需要
        private Fragment srcFragment;
        // TODO 這FragmentManager 也只是過客?
        private FragmentManager srcFragmentManager;
        private Fragment popFragment;
        private FragmentBuilder builder;

        public NotifyPopFragment(FragmentActivity srcFragmentActivity, Fragment srcFragment, FragmentManager srcFragmentManager, Fragment popFragment, FragmentBuilder builder) {
            this.srcFragmentActivity = srcFragmentActivity;
            this.srcFragment = srcFragment;
            this.srcFragmentManager = srcFragmentManager;
            this.popFragment = popFragment;
            this.builder = builder;
            srcFragmentManager.addOnBackStackChangedListener(this);
        }

        public void popBackStack() {
            srcFragmentManager.popBackStack();
        }

        @Override
        public void onBackStackChanged() {
            srcFragmentManager.removeOnBackStackChangedListener(this);
            PopFragmentListener listener = null;
            Object srcObject;

            if (TextUtils.isEmpty(builder.srcFragmentPathString)) {
                if (builder.srcViewId == 0) {
                    srcObject = srcFragmentActivity;
                } else {
                    srcObject = srcFragmentActivity.findViewById(builder.srcViewId);
                }
            } else {
                if (srcFragment == null) {
                    srcFragment = FragmentPath.findFragment(srcFragmentActivity, builder.srcFragmentPathString);
                }
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

    public static class FragmentPath extends ArrayList<Integer> {
        private final static String delimiter = ",";

        public int back() {
            this.remove(size() - 1);
            return this.size();
        }

        public static String getFragmentPathString(FragmentBuilder builder) {
            String fragmentPathString = getFragmentPathString(builder.content);
            return fragmentPathString;
        }

        public static String getFragmentPathString(View srcView) {
            return getFragmentPathString(new Content(srcView));
        }

        public static String getFragmentPathString(Content content) {
            FragmentPath srcFragmentPath = getFragmentPath(content);
            String srcFragmentPathString = covert(srcFragmentPath);
            return srcFragmentPathString;
        }

        public static FragmentPath getFragmentPath(FragmentBuilder builder) {
            FragmentPath fragmentPath = getFragmentPath(builder.content);
            return fragmentPath;
        }

        public static FragmentPath getFragmentPath(View srcView) {
            FragmentPath fragmentPath = getFragmentPath(new Content(srcView));
            return fragmentPath;
        }

        public static FragmentPath getFragmentPath(Content content) {
            FragmentPath fragmentPath = new FragmentPath();
            View srcView = content.getSrcView();
            if (srcView != null) {
                FragmentActivity activity = (FragmentActivity) srcView.getContext();
                ArrayList<Fragment> fragmentArrayList = new ArrayList<Fragment>();
                HashMap<Fragment, Fragment> parentMap = new HashMap<Fragment, Fragment>();
                HashMap<Fragment, Integer> fragmentIndexMap = new HashMap<Fragment, Integer>();
                fillAllFragments(null, parentMap, activity.getSupportFragmentManager(), fragmentArrayList, fragmentIndexMap);
                View contentView = srcView.getRootView().findViewById(android.R.id.content);
                if (fragmentArrayList.size() > 0) {
                    Fragment srcFragment = findFragmentByView(fragmentArrayList, srcView, contentView);
                    if (srcFragment != null) {
                        fillFragmentPathString(fragmentPath, parentMap, fragmentIndexMap, srcFragment);
                    }
                }
            }
            checkFragmentPath(content, fragmentPath);
            return fragmentPath;
        }

        private static void checkFragmentPath(Content content, FragmentPath fragmentPath) {
            // TODO 改一下
            View srcView = content.getSrcView();
            String fragmentPathString = covert(fragmentPath);
            if (srcView == null && fragmentPath.size() == 0) {
                // FragmentActivity
                return;
            }
            if (content.srcFragment != null) {
                Fragment findSrcFragment = FragmentPath.findFragment(content.getFragmentActivity(), fragmentPath);
                if (!content.srcFragment.equals(findSrcFragment)) {
                    throw new RuntimeException(String.format("Could not match source view. You need unique id for %s.", content.srcFragment.toString()));
                } else {
                    if (DEBUG) {
                        Toast.makeText(content.srcFragment.getActivity(), String.format("FragmentTag %s\nFragmentPath: %s\n%s", content.srcFragment.getTag(), fragmentPathString, content.srcFragment), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (content.srcView != null) {
                View findSrcView = FragmentPath.findView(content.getFragmentActivity(), fragmentPath, content.srcView.getId());
                if (!findSrcView.equals(srcView)) {
                    throw new RuntimeException(String.format("Could not match source view. You need unique id for %s.", srcView.toString()));
                } else {
                    if (DEBUG) {
                        Toast.makeText(srcView.getContext(), String.format("SrcViewId: %s\nFragmentPath: %s\n%s", content.srcView.getId(), fragmentPathString, content.srcView), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        private static void fillFragmentPathString(List<Integer> fragmentPath, Map<Fragment, Fragment> parentMap, Map<Fragment, Integer> fragmentIndexMap, Fragment srcFragment) {
            fragmentPath.add(0, fragmentIndexMap.get(srcFragment));
            Fragment parentFragment = parentMap.get(srcFragment);
            if (parentFragment == null) {
                return;
            } else {
                fillFragmentPathString(fragmentPath, parentMap, fragmentIndexMap, parentFragment);
            }
        }

        private static void fillAllFragments(Fragment parent, Map<Fragment, Fragment> parentMap, FragmentManager fm, List<Fragment> fragmentList, Map<Fragment, Integer> fragmentIndexMap) {
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
                        fillAllFragments(frag, parentMap, frag.getChildFragmentManager(), fragmentList, fragmentIndexMap);
                    }
                }
            }
        }

        private static Fragment findFragmentByView(List<Fragment> fragmentList, View srcView, View contentView) {
            for (Fragment frag : fragmentList) {
                //If Fragment view is null, Fragment must be invisible.
                if (frag.getView() != null) {
                    if (frag.getView().equals(srcView)) {
                        return frag;
                    }
                }
            }
            if (srcView.getParent().equals(contentView)) {
                return null;
            }
            View parent = (View) srcView.getParent();
            if (parent != null) {
                return findFragmentByView(fragmentList, parent, contentView);
            } else {
                return null;
            }
        }

        public static Fragment findFragmentByView(View view) {
            Fragment srcFragment = null;
            FragmentActivity activity = (FragmentActivity) view.getContext();
            ArrayList<Fragment> fragmentArrayList = new ArrayList<Fragment>();
            HashMap<Fragment, Fragment> parentMap = new HashMap<Fragment, Fragment>();
            HashMap<Fragment, Integer> fragmentIndexMap = new HashMap<Fragment, Integer>();
            fillAllFragments(null, parentMap, activity.getSupportFragmentManager(), fragmentArrayList, fragmentIndexMap);
            View contentView = view.getRootView().findViewById(android.R.id.content);
            if (fragmentArrayList.size() > 0) {
                srcFragment = findFragmentByView(fragmentArrayList, view, contentView);
            }
            return srcFragment;
        }

        public static View findView(FragmentActivity activity, String fragmentPathString, int viewId) {
            return findView(activity, FragmentPath.covert(fragmentPathString), viewId);
        }

        public static View findView(FragmentActivity activity, List<Integer> fragmentPath, int viewId) {
            View view = null;
            Fragment fragment = findFragment(activity, fragmentPath);
            if (fragment == null) {
                view = activity.findViewById(viewId);
            } else {
                view = fragment.getView().findViewById(viewId);
            }
            return view;
        }

        public static Fragment findFragment(FragmentActivity activity, String fragmentPathString) {
            return findFragment(activity, FragmentPath.covert(fragmentPathString));
        }

        public static Fragment findFragment(FragmentActivity activity, List<Integer> fragmentPath) {
            Fragment fragment = null;
            if (fragmentPath == null || fragmentPath.size() == 0) {
                fragment = null;
            } else {
                FragmentManager fm = activity.getSupportFragmentManager();
                for (Integer index : fragmentPath) {
                    fragment = fm.getFragments().get(index);
                    fm = fragment.getChildFragmentManager();
                }
            }
            return fragment;
        }

        public static FragmentPath covert(String fragmentPathString) {
            FragmentPath fragmentPath = new FragmentPath();
            for (String index : TextUtils.split(fragmentPathString, delimiter)) {
                fragmentPath.add(Integer.parseInt(index));
            }
            return fragmentPath;
        }

        public static String covert(FragmentPath fragmentPath) {
            String fragmentPathString = "";
            if (fragmentPath.size() > 0) {
                fragmentPathString = TextUtils.join(delimiter, fragmentPath);
            }
            return fragmentPathString;
        }
    }
}



