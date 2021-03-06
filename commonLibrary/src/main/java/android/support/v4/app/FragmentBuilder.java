package android.support.v4.app;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lin1987www.common.Utility;
import com.lin1987www.jackson.JacksonHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.ExceptionHelper;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.support.v4.app.FragmentTransaction.TRANSIT_NONE;

/**
 * Created by lin on 2015/6/25.
 */
public class FragmentBuilder {
    @JsonIgnore
    public static final String TAG = FragmentBuilder.class.getSimpleName();
    //
    @JsonIgnore
    public static boolean enableDefaultFragmentArgs = true;
    @JsonIgnore
    public static final int systemContainerViewId = android.R.id.content;
    @JsonIgnore
    public static int defaultContainerViewId = systemContainerViewId;
    @JsonIgnore
    public static Bundle defaultFragmentArgs = new Bundle();
    @JsonIgnore
    public static Action defaultAction = Action.add;
    @JsonIgnore
    public static ExistPolicy defaultExistPolicy = ExistPolicy.doNothing;
    @JsonIgnore
    public static Comparator<BackStackEntry> sortBackStack = new Comparator<BackStackEntry>() {
        @Override
        public int compare(BackStackEntry o1, BackStackEntry o2) {
            // 先進後出  越早執行的Index越大，越晚執行的Index越小
            return (int) (parse(o2).builtTimeMillis - parse(o1).builtTimeMillis);
        }
    };

    // Temp Object
    @JsonIgnore
    private final static Executor fragmentBuilderExecutor = new Executor();
    @JsonIgnore
    private FragContent content;
    @JsonIgnore
    private Fragment fragment;
    @JsonIgnore
    private Fragment fragmentAlreadyExist;
    @JsonIgnore
    private Class<? extends Fragment> fragmentClass;
    @JsonIgnore
    private Bundle fragmentArgs;
    @JsonIgnore
    private boolean needToFindContainerFragment = true;
    @JsonIgnore
    private Fragment containerFragment;

    //
    //
    @JsonProperty
    private PreAction preAction = PreAction.none;
    @JsonProperty
    private Action action = Action.none;
    // isKeepDelegate, isKeepContainer are for PreAction:back.
    @JsonProperty
    private boolean isKeepDelegate = false;
    @JsonProperty
    private boolean isKeepContainer = false;
    @JsonProperty
    private int containerViewId = 0;
    @JsonProperty
    private boolean hasAddToBackStack = false;
    @JsonProperty
    private ExistPolicy ifExistPolicy = defaultExistPolicy;
    //
    @JsonProperty
    private String assignBackStackName = "";
    @JsonProperty
    private String fragmentTag;
    //
    // delegate:  onPopFragment 的目標
    @JsonProperty
    private FragContentPath delegateFragContentPath;
    //
    @JsonProperty
    private String srcFragmentTag;
    // 用於Debug 得知從何處發起 src
    @JsonProperty
    private FragContentPath srcFragContentPath;
    //
    @JsonProperty
    private long builtTimeMillis = 0;
    // Animations
    @JsonProperty
    @Transit
    private int transition = TRANSIT_NONE;
    @JsonProperty
    private int styleRes = 0;
    // addBackStack
    @JsonProperty
    private int enter = 0;
    @JsonProperty
    private int exit = 0;
    // onBackPressed trigger popEnter and popExit
    @JsonProperty
    private int popEnter = 0;
    @JsonProperty
    private int popExit = 0;

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

    private FragmentManagerImpl getFragmentManager() {
        return (FragmentManagerImpl) content.getContainerFragmentManager(containerViewId);
    }

    @JsonCreator
    public FragmentBuilder(
            @JsonProperty("preAction") PreAction preAction,
            @JsonProperty("action") Action action,
            @JsonProperty("isKeepDelegate") boolean isKeepDelegate,
            @JsonProperty("isKeepContainer") boolean isKeepContainer,
            @JsonProperty("containerViewId") int containerViewId,
            @JsonProperty("hasAddToBackStack") boolean hasAddToBackStack,
            @JsonProperty("ifExistPolicy") ExistPolicy ifExistPolicy,
            @JsonProperty("assignBackStackName") String assignBackStackName,
            @JsonProperty("delegateFragContentPath") FragContentPath delegateFragContentPath,
            @JsonProperty("srcFragmentTag") String srcFragmentTag,
            @JsonProperty("srcFragContentPath") FragContentPath srcFragContentPath,
            @JsonProperty("builtTimeMillis") long builtTimeMillis,
            @JsonProperty("transition") int transition,
            @JsonProperty("styleRes") int styleRes,
            @JsonProperty("enter") int enter,
            @JsonProperty("exit") int exit,
            @JsonProperty("popEnter") int popEnter,
            @JsonProperty("popExit") int popExit
    ) {
        this.preAction = preAction;
        this.action = action;
        this.isKeepDelegate = isKeepDelegate;
        this.isKeepContainer = isKeepContainer;
        this.containerViewId = containerViewId;
        this.hasAddToBackStack = hasAddToBackStack;
        this.ifExistPolicy = ifExistPolicy;
        this.assignBackStackName = assignBackStackName;
        this.delegateFragContentPath = delegateFragContentPath;
        this.srcFragmentTag = srcFragmentTag;
        this.srcFragContentPath = srcFragContentPath;
        this.builtTimeMillis = builtTimeMillis;
        this.transition = transition;
        this.styleRes = styleRes;
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

    private FragmentBuilder(FragmentActivity srcFragmentActivity, Fragment srcFragment, View srcView) {
        this.content = new FragContent(srcFragmentActivity, srcFragment, srcView);
        this.srcFragmentTag = (srcFragment == null) ? "" : srcFragment.getTag();
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

    public static FragmentBuilder create(Object obj) {
        FragmentBuilder builder = null;
        if (obj instanceof FragmentActivity) {
            builder = create((FragmentActivity) obj);
        } else if (obj instanceof Fragment) {
            builder = create((Fragment) obj);
        } else if (obj instanceof View) {
            builder = create((View) obj);
        }
        return builder;
    }

    public FragmentBuilder addToBackStack() {
        return addToBackStack(null);
    }

    public FragmentBuilder addToBackStack(String name) {
        if (TextUtils.isEmpty(this.assignBackStackName)) {
            this.assignBackStackName = (name == null) ? "" : name;
        }
        this.hasAddToBackStack = true;
        return this;
    }

    public boolean hasAddToBackStack() {
        return hasAddToBackStack;
    }

    public FragmentBuilder ifExistDoNothing() {
        this.ifExistPolicy = ExistPolicy.doNothing;
        return this;
    }

    public FragmentBuilder ifExistReAttach() {
        this.ifExistPolicy = ExistPolicy.reAttach;
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
        this.fragmentTag = (tag == null) ? fragment.getClass().getSimpleName() : tag;
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
        this.fragmentTag = (tag == null) ? fragmentClass.getSimpleName() : tag;
        return this;
    }

    public FragmentBuilder setArgs(Bundle fragmentArgs) {
        this.fragmentArgs = fragmentArgs;
        return this;
    }

    public FragmentBuilder back() {
        return back(true, true);
    }

    /*
    僅用於 FragmentPager 中的Fragment 彈出 Fragment，能覆蓋 FragmentPager 的範圍
     */
    public FragmentBuilder backContainer() {
        return back(true, false);
    }

    public FragmentBuilder backDelegate() {
        return back(false, true);
    }

    public FragmentBuilder back(boolean isKeepContainer, boolean isKeepTarget) {
        this.preAction = PreAction.back;
        this.isKeepDelegate = isKeepTarget;
        this.isKeepContainer = isKeepContainer;
        return this;
    }

    public FragmentBuilder reset() {
        this.preAction = PreAction.reset;
        this.isKeepDelegate = true;
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

    //  Action      addToBackStack    containerFragment
    //-----------------------------------------------------
    //  add           True/False        add
    //  replace       True/False        add & remove
    //  attach        True(Non False)   add & detach

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

    public FragmentBuilder attach() {
        this.action = Action.attach;
        addToBackStack();
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
        Fragment srcFrag = builder.content.getSrcFragment();
        //
        FragmentManager backFm = builder.content.getParentFragmentManager();
        //
        FragmentBuilder contentBuilder = findFragmentBuilder(builder.content);
        //
        if (contentBuilder != null && contentBuilder.hasAddToBackStack()) {
            // Use addToBackStack
            if (builder.preAction.equals(PreAction.reset)) {
                builder.needToFindContainerFragment = false;
                // reset 的話 真正的發起來會 等等就會被移除掉
                Fragment contentInBackStack = null;
                for (int i = 0; i < backFm.getBackStackEntryCount(); i++) {
                    BackStackRecord record = (BackStackRecord) backFm.getBackStackEntryAt(i);
                    FragmentBuilder fragmentBuilder = parse(record);
                    if (fragmentBuilder.fragmentTag.equals(contentBuilder.fragmentTag)) {
                        contentInBackStack = FragContent.findStillInBackStackFragment(record);
                        break;
                    }
                }
                builder.srcFragContentPath = contentBuilder.srcFragContentPath;
                builder.containerFragment = contentInBackStack;
                backFm.popBackStack();
                overrideBuilder(contentBuilder, builder, builder.isKeepContainer, builder.isKeepDelegate);
            } else if (builder.preAction.equals(PreAction.back)) {
                // back 會參考目前的 建立的 builder 建立
                overrideBuilder(contentBuilder, builder, builder.isKeepContainer, builder.isKeepDelegate);
            }
        } else {
            // 可能是使用 FragmentAdapter  或者直接在 Activity 中產生
            if (builder.isKeepContainer) {
                if (null != srcFrag) {
                    // Fix fragment in ViewPager non available containerViewId cause problem!
                    builder.containerViewId = builder.content.getSafeContainerViewId();
                }
            }
            if (builder.isKeepDelegate) {
                // 由 FragmentPager 的 Fragment 發出，那最後回傳到 FragmentPager 所在的 Fragment
                // 也就是 back Fragment
                // 如果是 FragmentPager 裡面的 Fragment 要彈出  Fragment的話，且返回就是
                builder.delegateFragContentPath = builder.content.getParentFragContentPath();
            }
        }
    }

    public static void overrideBuilder(FragmentBuilder defaultBuilder, FragmentBuilder builder, boolean isKeepContainer, boolean isKeepDelegate) {
        if (isKeepContainer) {
            // replace containerViewId
            builder.containerViewId = defaultBuilder.containerViewId;
        }

        if (isKeepDelegate) {
            builder.delegateFragContentPath = defaultBuilder.delegateFragContentPath;
        }

        if (builder.action == Action.none) {
            builder.action = defaultBuilder.action;
            builder.hasAddToBackStack = defaultBuilder.hasAddToBackStack;
            builder.ifExistPolicy = defaultBuilder.ifExistPolicy;
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

    public boolean isAvailable() {
        if (content == null) {
            Log.e(TAG, String.format("FragmentBuilder FragContent is null [%s] %s", fragmentClass, getFragmentTag()));
            return false;
        }
        if (content.getFragmentActivity() == null) {
            Log.e(TAG, String.format("FragmentBuilder lose FragmentActivity [%s] %s", fragmentClass, getFragmentTag()));
            return false;
        }
        return true;
    }

    public void buildImmediate() {
        if (!isAvailable()) {
            return;
        }
        srcFragContentPath = content.getFragContentPath();
        delegateFragContentPath = srcFragContentPath;
        // delegateFragmentPathString maybe be modified by PreAction.
        doPreAction(this);
        if (action.equals(Action.none)) {
            action = defaultAction;
        }
        if (containerViewId == 0) {
            containerViewId = content.getSafeContainerViewId();
            if (containerViewId == 0) {
                containerViewId = defaultContainerViewId;
            }
        }
        FragmentManagerImpl fragmentManager = getFragmentManager();
        if (FragmentUtils.isStateLoss(fragmentManager)) {
            return;
        }
        // Check fragment already exist
        fragmentAlreadyExist = fragmentManager.findFragmentByTag(fragmentTag);
        if (FragmentUtils.isFragmentExist(fragmentAlreadyExist)) {
            // If isRemoving() is true, the fragment maybe be popped out during animation.
            if (Utility.DEBUG) {
                Log.e(TAG, String.format("Fragment exist in fragmentManager. tag: %s %s", fragmentTag, fragmentAlreadyExist.isRemoving()));
            }
            if (ifExistPolicy.equals(ExistPolicy.doNothing)) {
                return;
            } else if (ifExistPolicy.equals(ExistPolicy.reAttach)) {
                // Do reAttach Update
                FragmentUtils.putArguments(fragmentAlreadyExist, fragmentArgs);
                if (fragmentAlreadyExist.isVisible()) {
                    // If Fragment isVisible Re-attach
                    fragmentManager
                            .beginTransaction()
                            .detach(fragmentAlreadyExist)
                            .attach(fragmentAlreadyExist)
                            .commit();
                } else {
                    FragmentBuilder builder = findFragmentBuilder(new FragContent(fragmentAlreadyExist));
                    if (builder != null && builder.hasAddToBackStack) {
                        // If exist backStackEntry popStackEntry util that Fragment exist.
                        popBackStackRecord(content.getFragmentActivity(), builder.assignBackStackName, 0).popBackStack();
                    }
                }
                return;
            }
            return;
        }
        // Setting FragmentArgs
        if (fragment == null) {
            if (fragmentArgs == null && enableDefaultFragmentArgs) {
                fragmentArgs = new Bundle();
                fragmentArgs.putAll(defaultFragmentArgs);
            }
            fragment = Fragment.instantiate(content.getFragmentActivity(), fragmentClass.getName(), fragmentArgs);
        } else {
            if (fragmentArgs != null) {
                FragmentUtils.putArguments(fragment, fragmentArgs);
            }
        }

        ArrayList<Fragment> containerFragments;
        if (needToFindContainerFragment) {
            // TODO  登入切換有問題 可能會出現多個 Fragment 在同一個 ContainerView 的情況! 但可能用不到
            // 可能要檢查所有 fragment.isAdded()  fragment.getId()
            // If needToFindContainerFragment is false, containerFragment must be written by PreAction of reset
            containerFragments = content.findFragmentById(containerViewId);
            containerFragment = fragmentManager.findFragmentById(containerViewId);
        }
        // Prepare transaction
        BackStackRecord ft = (BackStackRecord) fragmentManager.beginTransaction();
        builtTimeMillis = System.currentTimeMillis();
        String fragmentBuilderText = generateFragmentBuilderText(this);
        FragmentArgs fragArgs = new FragmentArgs(fragmentArgs);
        fragArgs.setFragmentBuilderText(fragmentBuilderText);
        if (hasAddToBackStack()) {
            ft.addToBackStack(fragmentBuilderText);
        }
        // Animation setting
        setAnimations(ft);
        if (Action.add == action) {
            // Do nothing
        } else if (Action.replace == action) {
            // Affect containerFragment.
            if (containerFragment != null) {
                ft.remove(containerFragment);
            }
        } else if (Action.attach == action) {
            if (containerFragment != null) {
                if (hasAddToBackStack()) {
                    ft.detach(containerFragment);
                } else {
                    // Unreachable statement
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
        fragmentBuilderExecutor.queue(this);
    }

    public static class Executor implements Runnable {
        @JsonIgnore
        public final LinkedList<FragmentBuilder> fragmentBuilderQueue = new LinkedList<>();

        @JsonIgnore
        private final AtomicBoolean mExecutingActions = new AtomicBoolean(false);

        public FragmentBuilder getAvailableFragmentBuilder() {
            FragmentBuilder fragmentBuilder;
            synchronized (Executor.class) {
                fragmentBuilder = fragmentBuilderQueue.peek();
                for (; fragmentBuilderQueue.size() > 0 && ((fragmentBuilder == null) || (fragmentBuilder != null && !fragmentBuilder.isAvailable())); fragmentBuilder = fragmentBuilderQueue.peek()) {
                    fragmentBuilderQueue.poll();
                    Log.e(TAG, String.format("FragmentBuilder.Executor.getAvailableFragmentBuilder isStateLoss [%s] %s", fragmentBuilder.fragmentClass, fragmentBuilder.getFragmentTag()));
                }
            }
            return fragmentBuilder;
        }

        public void queue(FragmentBuilder queueBuilder) {
            synchronized (Executor.class) {
                fragmentBuilderQueue.offer(queueBuilder);
                FragmentBuilder fragmentBuilder = getAvailableFragmentBuilder();
                if (fragmentBuilder != null) {
                    if (!mExecutingActions.getAndSet(true)) {
                        execute(fragmentBuilder.content);
                    }
                }
                if (Utility.DEBUG) {
                    Log.d(TAG, String.format("FragmentBuilder.Executor.queue size %s", fragmentBuilderQueue.size()));
                }
            }
        }

        private void execute(FragContent content) {
            if (content.isResumed()) {
                run();
            } else {
                content.getDecorView().post(this);
            }
        }

        @Override
        public void run() {
            synchronized (Executor.class) {
                FragmentBuilder currentBuilder = getAvailableFragmentBuilder();
                if (currentBuilder != null) {
                    fragmentBuilderQueue.poll();
                    currentBuilder.buildImmediate();
                    FragContent.post(currentBuilder.getFragmentManager(), this);
                } else {
                    if (fragmentBuilderQueue.size() == 0) {
                        mExecutingActions.set(false);
                    } else {
                        String msg = String.format("FragmentBuilder.Executor.fragmentBuilderQueue.size() %s, but currentBuilder==null", fragmentBuilderQueue.size());
                        throw new RuntimeException(msg);
                    }
                }
                if (Utility.DEBUG) {
                    Log.d(TAG, String.format("FragmentBuilder.Executor.run size %s", fragmentBuilderQueue.size()));
                }
            }
        }
    }

    //  0b000 none
    //  0b001 add
    //  0b011 attach
    //  0b101 replace
    public enum Action {
        add, attach, replace, none
    }

    //  0b00000 none
    //  0b01000 back
    //  0b10000 reset
    protected enum PreAction {
        back, reset, none
    }

    //  0b000000 doNothing
    //  0b100000 reAttach
    public enum ExistPolicy {
        doNothing, reAttach
    }

    public static String generateFragmentBuilderText(FragmentBuilder builder) {
        String json = JacksonHelper.toJson(builder);
        return json;
    }

    public static FragmentBuilder parse(BackStackEntry stackEntry) {
        return parse(stackEntry.getName());
    }

    public static FragmentBuilder parse(String fragmentBuilderText) {
        FragmentBuilder builder = JacksonHelper.Parse(fragmentBuilderText, JacksonHelper.GenericType(FragmentBuilder.class));
        return builder;
    }

    public static FragmentBuilder findFragmentBuilder(FragContent content) {
        FragmentBuilder builder = null;
        if (content.getSrcFragment() != null) {
            // 由 FragmentArgs 獲得
            FragmentArgs fragmentArgs = new FragmentArgs(content.getSrcFragment().getArguments());
            String fragmentBuilderText = fragmentArgs.getFragmentBuilderText();
            if (!TextUtils.isEmpty(fragmentBuilderText)) {
                builder = parse(fragmentBuilderText);
            }
            // 由 BackStackRecord 獲得
            if (builder == null) {
                //builder = findFragmentBuilder(content.getSrcFragment());
                FragmentManager fm = content.getParentFragmentManager();
                for (int i = fm.getBackStackEntryCount() - 1; i > -1; i--) {
                    BackStackRecord record = (BackStackRecord) fm.getBackStackEntryAt(i);
                    FragmentBuilder b = parse(record);
                    if (b.fragmentTag.equals(content.getSrcFragment().getTag())) {
                        builder = b;
                        break;
                    }
                }
            }
        }
        return builder;
    }

    public static boolean hasPopBackStack(final FragmentActivity activity) {
        ContextHelper.hideKeyboard(activity);
        FragCarrier carrier = new FragCarrier(activity);
        if (carrier.getWillPopRecord() != null) {
            carrier.popBackStack();
            return true;
        }
        return false;
    }

    public static FragCarrier popBackStackRecord(final FragmentActivity activity) {
        return popBackStackRecord(activity, null, 0);
    }

    public static FragCarrier popBackStackRecord(FragmentActivity activity, String name, @PopFlag int flags) {
        FragCarrier fragCarrier = new FragCarrier(activity);
        fragCarrier.setData(name, flags);
        return fragCarrier;
    }

    public interface Predicate {
        boolean apply(BackStackEntry entry, FragmentBuilder builder);
    }

    public static class PredicateBackStackName implements Predicate {
        String value;

        public PredicateBackStackName(String value) {
            this.value = value;
        }

        @Override
        public boolean apply(BackStackEntry entry, FragmentBuilder builder) {
            if (value == null) {
                return false;
            }
            boolean result = builder.assignBackStackName.equals(value);
            return result;
        }
    }

    public static class PredicateBackStackEntry implements Predicate {
        BackStackEntry value;

        public PredicateBackStackEntry(BackStackEntry value) {
            this.value = value;
        }

        @Override
        public boolean apply(BackStackEntry entry, FragmentBuilder builder) {
            if (value == null) {
                return false;
            }
            boolean result = entry.equals(value);
            return result;
        }
    }

    public static class FragCarrier {
        private FragmentActivity fragmentActivity;
        private FragContent content;
        private ArrayList<FragPackage> fragPackages = new ArrayList<>();

        // Temp data
        private FragmentManagerImpl lastDoPopFragmentManager;
        private FragmentManagerImpl doPopFragmentManager;
        private boolean didFillData = false;
        private ArrayList<BackStackRecord> availableRecords;
        private BackStackRecord willPopRecord;

        private OnPopBackStackListener popStackListener;

        public FragCarrier setPopBackStackListener(OnPopBackStackListener listener) {
            this.popStackListener = listener;
            return this;
        }

        private
        @PopFlag
        int flags;
        private String backStackRecordName;
        private boolean didMatchBackStackRecord = false;

        public FragCarrier setData(String backStackRecordName, @PopFlag int flags) {
            this.flags = flags;
            this.backStackRecordName = backStackRecordName;
            return this;
        }

        public FragCarrier(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
            this.content = new FragContent(fragmentActivity);
            this.lastDoPopFragmentManager = (FragmentManagerImpl) fragmentActivity.getSupportFragmentManager();
        }

        public BackStackRecord getWillPopRecord() {
            if (!didFillData) {
                didFillData = true;
                content.fillAllFragmentAndManagerAndRecord(lastDoPopFragmentManager);
                if (content.getAllBackStackRecords().size() > 1) {
                    Collections.sort(content.getAllBackStackRecords(), sortBackStack);
                }
                availableRecords = new ArrayList<>(content.getAllBackStackRecords());
                for (int i = availableRecords.size() - 1; i > -1; i--) {
                    BackStackRecord record = availableRecords.get(i);
                    Fragment f = FragContent.findAddedFragment(record);
                    if (f != null) {
                        // 尚未被使用者看到 視為不適合的 BackStackRecord
                        if (!FragmentUtils.isFragmentAvailable(f) || !FragmentUtils.getUserVisibleHintAllParent(f)) {
                            availableRecords.remove(i);
                            continue;
                        }
                    }
                }
                if (availableRecords.size() > 0) {
                    willPopRecord = availableRecords.get(0);
                }
            }
            return willPopRecord;
        }

        public void popBackStack() {
            BackStackRecord willPopRecord = getWillPopRecord();
            if (willPopRecord == null) {
                finish();
            } else {
                FragPackage fragPackage = new FragPackage(fragmentActivity, willPopRecord);
                if (backStackRecordName != null) {
                    PredicateBackStackName predicate = new PredicateBackStackName(backStackRecordName);
                    didMatchBackStackRecord = predicate.apply(fragPackage.record, fragPackage.builder);
                    if (didMatchBackStackRecord) {
                        if (flags == FragmentManager.POP_BACK_STACK_INCLUSIVE) {
                            // do nothing
                        } else {
                            finish();
                            return;
                        }
                    }
                }
                fragPackages.add(fragPackage);
                // 將要出現的 Fragment 的Ready狀態全部略過，等全部執行完後再視情況執行
                fragPackage.disableReady();
                //
                doPopFragmentManager = willPopRecord.mManager;
                //
                // solution 1
                new PopBackStackTrigger(doPopFragmentManager, this, willPopRecord);
                // solution 2
                // ExecCommit.enqueueAction(doPopFragmentManager, onAfterPopBackStackTask);
                //
                //
                doPopFragmentManager.popBackStack();
            }
        }

        private Runnable onAfterPopBackStackTask = new Runnable() {
            @Override
            public void run() {
                onAfterPopBackStack();
            }
        };

        private void onAfterPopBackStack() {
            for (FragPackage fragPackage : fragPackages) {
                fragPackage.tryToSend();
            }
            if (flags != FragmentManager.POP_BACK_STACK_INCLUSIVE && backStackRecordName == null) {
                finish();
            } else {
                if (didMatchBackStackRecord) {
                    finish();
                } else {
                    // try pop next one
                    lastDoPopFragmentManager = doPopFragmentManager;
                    doPopFragmentManager = null;
                    didFillData = false;
                    availableRecords = null;
                    willPopRecord = null;
                    popBackStack();
                }
            }
        }

        private void finish() {
             /*
            下列情況一次Pop一個BackStackRecord a, b, c 在單次Pop後需要執行 performResumeIfReady
            a->A, b->B, c->C
            |a->A|
            |b->B|
            |c->C|
            以下情況是一口氣 Pop 到a 因此 a 需要在Pop後執行 performResumeIfReady
            a->A->b->B->c->C
            |a->A|
            |A->b|
            |b->B|
            |B->c|
            |c->C|
            令 Op.Add 或 Op.Attach 為 1 , Op.Remove 或 Op.Detach 為 -1
            瀏覽所有 Op 統計以上值為 -1 則執行 performResumeIfReady
            */
            if (fragPackages.size() == 0) {
                return;
            }
            HashMap<Fragment, Integer> map = new HashMap<>();
            for (FragPackage fragPackage : fragPackages) {
                if (!fragPackage.didSent) {
                    if (Utility.DEBUG) {
                        Log.e(TAG, String.format("Didn't send %s", fragPackage.packageFrag));
                    }
                }
                if (fragPackage.inBackStackFrag != null) {
                    int value = 0;
                    if (map.containsKey(fragPackage.inBackStackFrag)) {
                        value = map.get(fragPackage.inBackStackFrag);
                    }
                    value = value - 1;
                    map.put(fragPackage.inBackStackFrag, value);
                }
                if (fragPackage.packageFrag != null) {
                    int value = 0;
                    if (map.containsKey(fragPackage.packageFrag)) {
                        value = map.get(fragPackage.packageFrag);
                    }
                    value = value + 1;
                    map.put(fragPackage.packageFrag, value);
                }
            }
            // -1 代表要執行 ready
            for (Fragment fragment : map.keySet()) {
                int value = map.get(fragment);
                if (value == -1) {
                    FragmentFix f = (FragmentFix) fragment;
                    f.getFragmentArgs().isConsumeReady();
                    f.performResumeIfReady("FragCarrier finish.");
                }
            }
            if (popStackListener != null) {
                FragPackage fragPackage = fragPackages.get(fragPackages.size() - 1);
                popStackListener.onPopBackStack(fragPackage.recipient, fragPackage.packageFrag, this);
            }
        }

        private static class PopBackStackTrigger implements Runnable, FragmentManager.OnBackStackChangedListener {
            FragmentManagerImpl popFragmentManager;
            BackStackRecord popRecord;
            FragCarrier fragCarrier;
            private boolean didPopBackStack = false;

            public PopBackStackTrigger(FragmentManagerImpl fragmentManager, FragCarrier fragCarrier, BackStackRecord popRecord) {
                this.popFragmentManager = fragmentManager;
                this.popRecord = popRecord;
                this.fragCarrier = fragCarrier;
                popFragmentManager.addOnBackStackChangedListener(this);
            }

            @Override
            public void run() {
                popFragmentManager.removeOnBackStackChangedListener(this);
            }

            @Override
            public void onBackStackChanged() {
                if (!popFragmentManager.mBackStack.contains(popRecord)) {
                    if (!didPopBackStack) {
                        didPopBackStack = true;
                        ExecCommit.enqueueAction(popFragmentManager, this);
                        fragCarrier.onAfterPopBackStack();
                    }
                }
            }
        }

        private static class FragPackage {
            private FragmentActivity fragmentActivity;
            BackStackRecord record;
            Fragment packageFrag;
            Fragment inBackStackFrag;
            private FragmentBuilder builder;

            boolean didSent = false;
            Object recipient;

            public FragPackage(FragmentActivity fragmentActivity, BackStackRecord record) {
                this.fragmentActivity = fragmentActivity;
                this.record = record;
                this.packageFrag = FragContent.findAddedFragment(record);
                this.inBackStackFrag = FragContent.findStillInBackStackFragment(record);
                this.builder = FragmentBuilder.parse(record);
                FragmentUtils.putAnim(record, builder.transition, builder.styleRes, builder.enter, builder.exit, builder.popEnter, builder.popExit);
            }

            public void disableReady() {
                if (inBackStackFrag != null) {
                    FragmentArgs fragmentArgs = new FragmentArgs(inBackStackFrag.getArguments());
                    fragmentArgs.consumeReady();
                }
            }

            public void tryToSend() {
                if (!didSent) {
                    recipient = FragContentPath.findObject(fragmentActivity, builder.delegateFragContentPath);
                    if (recipient != null) {
                        didSent = true;
                        sendPackageFragment(recipient, packageFrag);
                    }
                }
            }

            private static void sendPackageFragment(Object onPopFragmentObject, Fragment popFragment) {
                if (onPopFragmentObject == null) {
                    return;
                }
                if (onPopFragmentObject instanceof OnPopFragmentListener) {
                    ((OnPopFragmentListener) onPopFragmentObject).onPopFragment(popFragment);
                    return;
                }
                try {
                    Class<?> targetClass = onPopFragmentObject.getClass();
                    Method method = targetClass.getDeclaredMethod("onPopFragment", popFragment.getClass());
                    if (method != null) {
                        method.invoke(onPopFragmentObject, popFragment);
                        return;
                    }
                } catch (Throwable ex) {
                    ExceptionHelper.printException(String.format("onPopFragment on %s", onPopFragmentObject), ex);
                }
            }
        }
    }

    public interface OnPopBackStackListener {
        void onPopBackStack(Object recipient, Fragment packageFrag, FragCarrier fragCarrier);
    }

    public interface OnPopFragmentListener {
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

    @IntDef({TRANSIT_NONE, TRANSIT_FRAGMENT_OPEN, TRANSIT_FRAGMENT_CLOSE, TRANSIT_FRAGMENT_FADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Transit {
    }

    @IntDef({0, FragmentManager.POP_BACK_STACK_INCLUSIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PopFlag {
    }
}