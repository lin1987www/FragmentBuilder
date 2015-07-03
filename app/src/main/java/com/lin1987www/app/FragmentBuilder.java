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
 * 1. Wizard Steps
 * 2. Switch
 * 2. Fragment -> Fragment ( Outside )
 * 2. View -> Fragment ( Outside )
 * 3. FragmentActivity -> Fragment ( Inside * Only way )
 * 3. Fragment -> Fragment (Inside)
 * 4. View -> Fragment ( Inside ) * 應該不會用到
 */
public class FragmentBuilder {
    public static final String TAG = FragmentBuilder.class.getName();

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
    private PreAction preAction = PreAction.none;
    // isKeepTarget, isKeepContainer are for PreAction:back.
    private boolean isKeepTarget = false;
    private boolean isKeepContainer = false;
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
    private String targetFragmentPathString = "";
    // originFragmentTag 用於Debug 得知從何處發起
    private String originFragmentTag;
    private int targetViewId = 0;
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
        this.originFragmentTag = (srcFragment == null) ? "" : srcFragment.getTag();
        if (srcView != null) {
            setTargetViewId(srcView.getId());
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

    public int getTargetViewId() {
        return this.targetViewId;
    }

    public FragmentBuilder setTargetViewId(int targetViewId) {
        if (targetViewId > 0) {
            if (content.srcFragmentActivity != null) {
                if (content.srcFragmentActivity.findViewById(targetViewId) == null) {
                    throw new RuntimeException("Didn't find targetViewId");
                }
            } else if (content.srcFragment != null) {
                if (content.srcFragment.getView().findViewById(targetViewId) == null) {
                    throw new RuntimeException("Didn't find targetViewId");
                }
            } else {
                if (((FragmentActivity) content.srcView.getContext()).findViewById(targetViewId) == null) {
                    throw new RuntimeException("Didn't find targetViewId");
                }
            }
        }
        this.targetViewId = targetViewId;
        return this;
    }

    public FragmentBuilder back() {
        return back(true, true);
    }

    public FragmentBuilder backContainer() {
        return back(true, false);
    }

    public FragmentBuilder backTarget() {
        return back(false, true);
    }

    public FragmentBuilder back(boolean isKeepContainer, boolean isKeepTarget) {
        this.preAction = PreAction.back;
        this.isKeepTarget = isKeepTarget;
        this.isKeepContainer = isKeepContainer;
        return this;
    }


    public FragmentBuilder reset() {
        this.preAction = PreAction.reset;
        this.isKeepTarget = true;
        this.isKeepContainer = true;
        return this;
    }

    public FragmentBuilder reset(int containerViewId, Fragment fragment) {
        reset(containerViewId, fragment, null);
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

    private static void doPreAction(FragmentBuilder builder) {
        if (builder.preAction.equals(PreAction.none)) {
            return;
        }
        FragmentPath fragmentPath = FragmentPath.getFragmentPath(builder.content);
        Fragment hereFrag = FragmentPath.findFragment(builder.content.getFragmentActivity(), fragmentPath);
        fragmentPath.back();
        String backFragmentPathString = FragmentPath.covert(fragmentPath);
        FragmentManager backFm = FragmentPath.getFragmentManager(builder.content, fragmentPath);
        //
        FragmentBuilder backBuilder = findBackFragmentBuilder(builder.content);
        //
        if (backBuilder != null) {
            if (builder.preAction.equals(PreAction.reset)) {
                backFm.popBackStack();
                overrideBuilder(backBuilder, builder, true, true);
            } else if (builder.preAction.equals(PreAction.back)) {
                overrideBuilder(backBuilder, builder, builder.isKeepContainer, builder.isKeepTarget);
            }
        } else {
            if (builder.isKeepContainer) {
                if (null != hereFrag) {
                    builder.containerViewId = hereFrag.getId();
                }
            }
            if (builder.isKeepTarget) {
                // TODO  需要驗證
                builder.targetFragmentPathString = backFragmentPathString;
                builder.targetViewId = 0;
            }
        }
    }

    public static FragmentBuilder findBackFragmentBuilder(Content content) {
        FragmentBuilder backBuilder = null;
        BackStackEntry backStackEntry;
        FragmentPath fragmentPath = FragmentPath.getFragmentPath(content);
        Fragment hereFrag = FragmentPath.findFragment(content.getFragmentActivity(), fragmentPath);
        if (hereFrag == null) {
            return null;
        }
        String hereFragTag = hereFrag.getTag();
        // Then fragmentPath is back!!
        fragmentPath.back();
        FragmentManager backFm = FragmentPath.getFragmentManager(content, fragmentPath);
        for (int i = backFm.getBackStackEntryCount() - 1; i > -1; i--) {
            backStackEntry = backFm.getBackStackEntryAt(i);
            backBuilder = parse(backStackEntry);
            if (backBuilder.fragmentTag.equals(hereFragTag)) {
                break;
            }
            backBuilder = null;
        }
        return backBuilder;
    }

    public static void overrideBuilder(FragmentBuilder defaultBuilder, FragmentBuilder builder, boolean isKeepContainer, boolean isKeepTarget) {
        if (isKeepContainer) {
            // replace containerViewId
            builder.containerViewId = defaultBuilder.containerViewId;
        }

        if (isKeepTarget) {
            builder.targetViewId = defaultBuilder.targetViewId;
            builder.targetFragmentPathString = defaultBuilder.targetFragmentPathString;
        }

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
        // targetFragmentPathString maybe be modified by PreAction.
        this.targetFragmentPathString = FragmentPath.getFragmentPathString(content);
        doPreAction(this);
        if (action.equals(Action.none)) {
            action = defaultAction;
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
        // Maybe execute action of reset
        containerFragment = fragmentManager.findFragmentById(containerViewId);
        // Prepare transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (isTraceable()) {
            builtTimeMillis = System.currentTimeMillis();
            String backStackName = generateBackStackName(this);
            ft.addToBackStack(backStackName);
        }
        // Animation setting
        setAnimations(ft);
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
        add, replace, none
    }

    protected enum PreAction {
        back, reset, none
    }

    /**
     * Provider
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
                    // TODO 保存 originFragmentTag ? 應該不需要  因為他只是建立 Fragemnt 的Fragment  並非實際目的地的Fragment
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
                builder.originFragmentTag,
                builder.targetViewId,
                builder.assignBackStackName,
                //
                builder.builtTimeMillis,
                //
                builder.transition, builder.styleRes, builder.enter, builder.exit, builder.popEnter, builder.popExit,
                //
                builder.targetFragmentPathString
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
            builder.originFragmentTag = m.group(i++);
            builder.targetViewId = Integer.parseInt(m.group(i++));
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
            builder.targetFragmentPathString = m.group(i++);
        }
        return builder;
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
        List<PopFragmentSender> notifyPopFragmentList = new ArrayList<PopFragmentSender>();
        for (int i = list.size() - 1; i > -1; i--) {
            BackStackEntry backStackEntry = list.get(i);
            FragmentBuilder builder = parse(list.get(i));
            FragmentManager srcFragmentManager = srcFmMap.get(backStackEntry);
            Fragment srcFragment = srcFragMap.get(backStackEntry);
            Fragment popFragment = srcFragmentManager.findFragmentByTag(builder.fragmentTag);
            if (name != null) {
                if (builder.assignBackStackName.equals(name)) {
                    if (flags == FragmentManager.POP_BACK_STACK_INCLUSIVE) {
                        notifyPopFragmentList.add(new PopFragmentSender(activity, srcFragment, srcFragmentManager, popFragment, builder));
                    }
                    break;
                } else {
                    notifyPopFragmentList.add(new PopFragmentSender(activity, srcFragment, srcFragmentManager, popFragment, builder));
                }
            } else {
                notifyPopFragmentList.add(new PopFragmentSender(activity, srcFragment, srcFragmentManager, popFragment, builder));
            }
        }
        // 串聯的方式 popStack
        for (int i = 0; i < (notifyPopFragmentList.size() - 1); i++) {
            PopFragmentSender sender = notifyPopFragmentList.get(i);
            PopFragmentSender next = notifyPopFragmentList.get(i + 1);
            sender.nextSender = next;
        }
        notifyPopFragmentList.get(0).popBackStack();
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
            new PopFragmentSender(activity, srcFragment, srcFragmentManager, popFragment, lastBuilder).popBackStack();
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

    private static class PopFragmentSender implements FragmentManager.OnBackStackChangedListener {
        private FragmentActivity fragmentActivity;
        private Fragment hookFragment;
        private FragmentManager hookFragmentManager;
        private Fragment targetFragment;
        private Fragment popFragment;
        private FragmentBuilder builder;
        private boolean isSentObj = false;
        public PopFragmentSender nextSender;

        public PopFragmentSender(FragmentActivity fragmentActivity, Fragment hookFragment, FragmentManager hookFragmentManager, Fragment popFragment, FragmentBuilder builder) {
            this.fragmentActivity = fragmentActivity;
            this.hookFragment = hookFragment;
            this.hookFragmentManager = hookFragmentManager;
            this.popFragment = popFragment;
            this.builder = builder;
            this.targetFragment = FragmentPath.findFragment(fragmentActivity, builder.targetFragmentPathString);
        }

        public void popBackStack() {
            fragmentActivity.getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    // 不可以馬上移除Listener 不然會對正在觸發的onBackStackChanged 造成影響
                    hookFragmentManager.addOnBackStackChangedListener(PopFragmentSender.this);
                    hookFragmentManager.popBackStack();
                }
            });
        }

        @Override
        public void onBackStackChanged() {
            Object srcObject;
            if (isSentObj) {
                return;
            }
            // 串聯呼叫地的方式
            if (nextSender != null) {
                nextSender.popBackStack();
                nextSender = null;
            }
            if ((targetFragment == null || targetFragment.isVisible()) && !popFragment.isVisible()) {
                // Match srcFragment and popFragment
            } else {
                // Didn't match do nothing.
                return;
            }
            if (TextUtils.isEmpty(builder.targetFragmentPathString)) {
                if (builder.targetViewId == 0) {
                    srcObject = fragmentActivity;
                } else {
                    srcObject = fragmentActivity.findViewById(builder.targetViewId);
                }
            } else {
                if (builder.targetViewId == 0) {
                    srcObject = targetFragment;
                } else {
                    srcObject = targetFragment.getView().findViewById(builder.targetViewId);
                }
            }
            if (srcObject != null) {
                isSentObj = true;
                sendOnPopFragment(srcObject, popFragment);
                // Remove listener
                fragmentActivity.getWindow().getDecorView().post(new Runnable() {
                    @Override
                    public void run() {
                        // 不可以馬上移除Listener 不然會對正在觸發的onBackStackChanged 造成影響
                        hookFragmentManager.removeOnBackStackChangedListener(PopFragmentSender.this);
                    }
                });
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
        public static boolean DEBUG = false;
        private final static String delimiter = ",";

        public int back() {
            if (size() > 0) {
                this.remove(size() - 1);
            }
            return this.size();
        }

        public static FragmentManager getFragmentManager(Content content, List<Integer> fragmentPath) {
            FragmentManager fm;
            if (fragmentPath.size() == 0) {
                fm = content.getFragmentActivity().getSupportFragmentManager();
            } else {
                fm = findFragment(content.getFragmentActivity(), fragmentPath).getChildFragmentManager();
            }
            return fm;
        }

        public static String getFragmentPathString(Content content) {
            FragmentPath srcFragmentPath = getFragmentPath(content);
            String srcFragmentPathString = covert(srcFragmentPath);
            return srcFragmentPathString;
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
            View view;
            if (fragmentPath.size() == 0) {
                view = activity.findViewById(viewId);
            } else {
                Fragment fragment = findFragment(activity, fragmentPath);
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



