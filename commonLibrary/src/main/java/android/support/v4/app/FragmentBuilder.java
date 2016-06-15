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
import com.lin1987www.jackson.JacksonHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fix.java.util.concurrent.Duty;
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
    @JsonIgnore
    private final static LinkedList<BackStackRecord> backStackRecordQueue = new LinkedList<>();
    @JsonIgnore
    public final static Runnable commitTransaction = new Runnable() {
        @Override
        public void run() {
            synchronized (commitTransaction) {
                backStackRecordQueue.poll();
                BackStackRecord nextBackStackRecord = backStackRecordQueue.peek();
                if (nextBackStackRecord != null && nextBackStackRecord.mCommitted == false) {
                    nextBackStackRecord.commit();
                    nextBackStackRecord.mManager.enqueueAction(this, false);
                }
            }
        }
    };
    //
    // Temp Object
    @JsonIgnore
    private FragContent content;
    @JsonIgnore
    private Fragment fragment;
    @JsonIgnore
    private Class<? extends Fragment> fragmentClass;
    @JsonIgnore
    private Bundle fragmentArgs;
    @JsonIgnore
    private boolean needToFindContainerFragment = true;
    @JsonIgnore
    private Fragment containerFragment;
    @JsonIgnore
    private Duty buildDuty = new Duty() {
        @Override
        public void doTask(Object context, Duty previousDuty) throws Throwable {
            buildImmediate();
        }
    }.setExecutorService(ExecutorSet.nonBlockExecutor);
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

    private FragmentManager getFragmentManager() {
        return content.getContainerFragmentManager(containerViewId);
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
                        contentInBackStack = FragContent.findInBackStackFragment(record);
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

    public void buildImmediate() {
        if (content == null) {
            throw new RuntimeException("Forbid build!");
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
        FragmentManager fragmentManager = getFragmentManager();
        // Check fragment already exist
        final Fragment fragmentAlreadyExist = fragmentManager.findFragmentByTag(fragmentTag);
        if (FragmentUtils.isFragmentExist(fragmentAlreadyExist)) {
            // If isRemoving() is true, the fragment maybe be popped out during animation.
            Log.w(TAG, String.format("Fragment is exist in fragmentManager. tag: %s %s", fragmentTag, fragmentAlreadyExist.isRemoving()));
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
            int size = containerFragments.size();
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
        synchronized (commitTransaction) {
            backStackRecordQueue.offer(ft);
            if (backStackRecordQueue.size() == 1) {
                ft.commit();
                ft.mManager.enqueueAction(commitTransaction, false);
            }
        }
    }


    public void build() {
        if (content == null) {
            throw new RuntimeException("Forbid build!");
        }
        if (content.isResumed()) {
            buildImmediate();
        } else {
            content.post(buildDuty);
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
        FragmentBuilder builder = null;
        try {
            builder = JacksonHelper.Parse(fragmentBuilderText, JacksonHelper.GenericType(FragmentBuilder.class));
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        PopBackStackRecord record = popBackStackRecord(activity);
        if (record != null) {
            record.popBackStack();
            return true;
        }
        return false;
    }

    public static PopBackStackRecord popBackStackRecord(final FragmentActivity activity) {
        FragContent content = new FragContent(activity);
        if (content.getAllBackStackRecords().size() > 1) {
            Collections.sort(content.getAllBackStackRecords(), sortBackStack);
        }
        ArrayList<BackStackRecord> list = content.getAllBackStackRecords();
        if (list.size() > 0) {
            FragmentBuilder lastBuilder = parse(list.get(0));
            String name = lastBuilder.assignBackStackName;
            return popBackStackRecord(activity, name, FragmentManager.POP_BACK_STACK_INCLUSIVE, content);
        }
        return null;
    }

    public static PopBackStackRecord popBackStackRecord(FragmentActivity activity, String name, @PopFlag int flags) {
        return popBackStackRecord(activity, new PredicateBackStackName(name), flags, null);
    }

    public static PopBackStackRecord popBackStackRecord(FragmentActivity activity, String name, @PopFlag int flags, FragContent content) {
        return popBackStackRecord(activity, new PredicateBackStackName(name), flags, content);
    }

    public static PopBackStackRecord popBackStackRecord(FragmentActivity activity, Predicate predicate, @PopFlag int flags) {
        return popBackStackRecord(activity, predicate, flags, null);
    }

    public static PopBackStackRecord popBackStackRecord(FragmentActivity activity, Predicate predicate, @PopFlag int flags, FragContent content) {
        if (content == null) {
            content = new FragContent(activity);
            if (content.getAllBackStackRecords().size() > 1) {
                Collections.sort(content.getAllBackStackRecords(), sortBackStack);
            }
        }
        ArrayList<BackStackRecord> list = content.getAllBackStackRecords();
        ArrayList<BackStackRecord> popList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BackStackRecord backStackRecord = list.get(i);
            FragmentBuilder builder = parse(backStackRecord);
            if (predicate.apply(backStackRecord, builder)) {
                if (flags == FragmentManager.POP_BACK_STACK_INCLUSIVE) {
                    popList.add(backStackRecord);
                }
                break;
            } else {
                popList.add(backStackRecord);
            }
        }
        // 執行完後，某些Fragment只會出現一下又消失，因此SkipOnResume能使那些Fragment略過 OnResume 增進效能
        skipOnResume(popList);
        ArrayList<PopFragmentSender> popFragmentSenderList = new ArrayList<>();
        PopFragmentSender prevSender = null;
        for (int i = 0; i < popList.size(); i++) {
            BackStackRecord backStackRecord = popList.get(i);
            FragmentBuilder builder = parse(backStackRecord);
            FragmentUtils.putAnim(backStackRecord, builder.transition, builder.styleRes, builder.enter, builder.exit, builder.popEnter, builder.popExit);
            PopFragmentSender sender = new PopFragmentSender(builder, backStackRecord);
            if (prevSender != null) {
                prevSender.nextSender = sender;
                sender.prevSender = prevSender;
            }
            popFragmentSenderList.add(sender);
            prevSender = sender;
        }
        PopBackStackRecord record = null;
        if (popFragmentSenderList.size() > 0) {
            record = new PopBackStackRecord(popFragmentSenderList);
        }
        return record;
    }

    private static void skipOnResume(ArrayList<BackStackRecord> popList) {
        /*
        下列情況沒有需要 skipOnResume 的 Fragment
        a->A, b->B, c->C
        |a->A|
        |b->B|
        |c->C|
        以下情況需要 skipOnResume
        a->A->b->B->c->C
        |a->A|
        |A->b|
        |b->B|
        |B->c|
        |c->C|
        需要skipOnResume的Fragment有 A,b,B,c
        令 Op.Add 或 Op.Attach 為 1 , Op.Remove 或 Op.Detach 為 -1
        瀏覽所有 Op 統計以上值為 0 則 skipOnResume
        */
        HashMap<Fragment, Integer> map = new HashMap<>();
        for (BackStackRecord record : popList) {
            BackStackRecord.Op op = record.mHead;
            while (op != null) {
                Fragment fragment = op.fragment;
                int value = 0;
                if (map.containsKey(fragment)) {
                    value = map.get(fragment);
                }
                if (op.cmd == BackStackRecord.OP_ADD | op.cmd == BackStackRecord.OP_ATTACH) {
                    value = value + 1;
                } else if (op.cmd == BackStackRecord.OP_REMOVE | op.cmd == BackStackRecord.OP_DETACH) {
                    value = value - 1;
                }
                map.put(fragment, value);
                op = op.next;
            }
        }
        for (Fragment fragment : map.keySet()) {
            int value = map.get(fragment);
            if (value == 0) {
                FragmentArgs resumeFragmentArgs = new FragmentArgs(fragment.getArguments());
                resumeFragmentArgs.skipPopOnResume();
                // 其 Child Fragment 也要一併 skipOnResume
                skipOnResume(fragment.getChildFragmentManager());
            }
        }
    }

    private static void skipOnResume(FragmentManager childFragmentManager) {
        if (childFragmentManager == null) {
            return;
        }
        List<Fragment> fragList = childFragmentManager.getFragments();
        if (fragList != null && fragList.size() > 0) {
            for (Fragment frag : fragList) {
                if (frag != null) {
                    FragmentArgs resumeFragmentArgs = new FragmentArgs(frag.getArguments());
                    resumeFragmentArgs.skipPopOnResume();
                    skipOnResume(frag.getChildFragmentManager());
                }
            }
        }
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

    private static class PopFragmentSender extends Duty implements FragmentManager.OnBackStackChangedListener {
        private BackStackRecord record;

        private FragmentActivity fragmentActivity;
        private FragmentManager hookFragmentManager;

        private Fragment popFragment;
        private Object onPopFragmentObject;

        private FragmentBuilder builder;

        private boolean isSent = false;
        private boolean isRemoveBackStackChangedListener = false;
        private boolean isPopBackStack = false;

        public PopFragmentSender prevSender;
        public PopFragmentSender nextSender;

        private PopBackStackListener popStackListener;

        public void setPopStackListener(PopBackStackListener listener) {
            this.popStackListener = listener;
        }

        public PopFragmentSender(FragmentBuilder builder, BackStackRecord entry) {
            this.popFragment = FragContent.findAddFragment(entry);
            this.fragmentActivity = popFragment.getActivity();
            this.hookFragmentManager = popFragment.getFragmentManager();
            this.builder = builder;
            this.record = entry;
            setExecutorService(ExecutorSet.mainThreadExecutor);
            setAsync(false);
        }

        public void popBackStack() {
            // 不使用延遲
            hookFragmentManager.addOnBackStackChangedListener(PopFragmentSender.this);
            hookFragmentManager.popBackStack();
        }

        @Override
        public void onBackStackChanged() {
            if (prevSender != null) {
                prevSender.onBackStackChanged();
            }
            if (isSent) {
                return;
            }
            if (!isRemoveBackStackChangedListener) {
                isRemoveBackStackChangedListener = true;
                hookFragmentManager.removeOnBackStackChangedListener(PopFragmentSender.this);
            }
            onPopFragmentObject = FragContentPath.findObject(fragmentActivity, builder.delegateFragContentPath);
            if (onPopFragmentObject != null) {
                isSent = true;
                sendOnPopFragment(onPopFragmentObject, popFragment);
            }
            if (nextSender != null && !isPopBackStack) {
                nextSender.popBackStack();
                isPopBackStack = true;
            }
            if (popStackListener != null && isSent) {
                // TODO  等狀態穩定再執行
                FragContent content = FragContent.create(onPopFragmentObject);
                if (content.isResumed()) {
                    submit();
                } else {
                    content.post(this);
                }
            }
        }

        private static void sendOnPopFragment(Object onPopFragmentObject, Fragment popFragment) {
            if (onPopFragmentObject == null) {
                return;
            }
            Class<?> targetClass = onPopFragmentObject.getClass();
            try {
                Method method = targetClass.getDeclaredMethod("onPopFragment", popFragment.getClass());
                if (method != null) {
                    method.invoke(onPopFragmentObject, popFragment);
                    return;
                }
            } catch (Throwable ex) {
                ExceptionHelper.printException(String.format("onPopFragment on %s", onPopFragmentObject), ex);
            }
            if (onPopFragmentObject instanceof PopFragmentListener) {
                ((PopFragmentListener) onPopFragmentObject).onPopFragment(popFragment);
            }
        }

        @Override
        public void doTask(Object context, Duty previousDuty) throws Throwable {
            popStackListener.onPopBackStack(onPopFragmentObject, popFragment);
            popStackListener = null;
        }
    }

    public static class PopBackStackRecord {
        public final ArrayList<PopFragmentSender> notifyPopFragmentList;

        public PopBackStackRecord(ArrayList<PopFragmentSender> list) {
            this.notifyPopFragmentList = list;
        }

        public PopFragmentSender getHead() {
            return notifyPopFragmentList.get(0);
        }

        public PopFragmentSender getTail() {
            return notifyPopFragmentList.get(notifyPopFragmentList.size() - 1);
        }

        public void popBackStack() {
            getHead().popBackStack();
        }

        public PopBackStackRecord setTailPopStackListener(PopBackStackListener listener) {
            getTail().setPopStackListener(listener);
            return this;
        }
    }

    public interface PopBackStackListener {
        void onPopBackStack(Object onPopFragmentObject, Fragment popFragment);
    }

    public interface PopFragmentListener {
        void onPopFragment(Fragment popFragment);
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