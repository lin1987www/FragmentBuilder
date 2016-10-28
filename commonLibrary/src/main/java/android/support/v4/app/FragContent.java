package android.support.v4.app;

import android.support.annotation.IdRes;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fix.java.util.concurrent.Duty;

/**
 * Provider
 */
public class FragContent {
    private static String noExistContainerView = "Didn't find container view.";
    public final FragmentActivity srcFragmentActivity;
    public final Fragment srcFragment;
    public final View srcView;
    private FragmentManager fragmentManager = null;

    public FragContent(FragmentActivity srcFragmentActivity) {
        this(srcFragmentActivity, null, null);
        if (srcFragmentActivity == null) {
            throw new NullPointerException();
        }
    }

    public FragContent(Fragment srcFragment) {
        this(null, srcFragment, null);
        if (srcFragment == null) {
            throw new NullPointerException();
        }
    }

    public FragContent(View srcView) {
        this(null, null, srcView);
        if (srcView == null) {
            throw new NullPointerException();
        }
    }

    public static FragContent create(Object obj) {
        FragContent content = null;
        if (obj instanceof FragmentActivity) {
            content = new FragContent((FragmentActivity) obj);
        } else if (obj instanceof Fragment) {
            content = new FragContent((Fragment) obj);
        } else if (obj instanceof View) {
            content = new FragContent((View) obj);
        }
        return content;
    }

    public FragContent(FragmentActivity srcFragmentActivity, Fragment srcFragment, View srcView) {
        this.srcFragmentActivity = srcFragmentActivity;
        this.srcFragment = srcFragment;
        this.srcView = srcView;
        if (srcView != null) {
            /*
            if (srcView.getId() <= 0) {
                throw new RuntimeException(String.format("You must set id to %s", srcView));
            }
            */
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

    public void setContainerViewId(int containerViewId) {
        Fragment frag = getSrcFragment();
        while (null != frag) {
            if (null != frag.getView().findViewById(containerViewId)) {
                fragmentManager = frag.getChildFragmentManager();
                break;
            } else {
                frag = frag.getParentFragment();
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

    public FragmentManager getContainerFragmentManager(int containerViewId) {
        if (fragmentManager != null) {
            return fragmentManager;
        } else {
            setContainerViewId(containerViewId);
        }
        return fragmentManager;
    }

    public FragmentManager getParentFragmentManager() {
        Fragment fragment = getSrcFragment();
        if (fragment != null) {
            Fragment parentFragment = fragment.getParentFragment();
            if (parentFragment != null) {
                return parentFragment.getChildFragmentManager();
            }
        }
        return getFragmentActivity().getSupportFragmentManager();
    }

    private View mSrcView = null;

    public View getSrcView() {
        if (mSrcView == null) {
            if (srcView != null) {
                mSrcView = srcView;
            } else if (srcFragment != null) {
                mSrcView = srcFragment.getView();
            }
        }
        return mSrcView;
    }

    private Fragment mSrcFragment = null;

    public Fragment getSrcFragment() {
        if (mSrcFragment == null) {
            if (srcFragment != null) {
                mSrcFragment = srcFragment;
            } else if (srcView != null) {
                mSrcFragment = findFragmentByView(srcView);
            }
        }
        return mSrcFragment;
    }

    private View mRootView = null;

    public View getRootView() {
        // 考慮到 View 不在畫面上的問題!
        if (mRootView == null) {
            if (srcView != null) {
                mRootView = srcView.getRootView();
            } else if (srcFragment != null && srcFragment.getView() != null) {
                mRootView = srcFragment.getView().getRootView();
            } else if (srcFragmentActivity != null) {
                mRootView = getDecorView().getRootView();
            }
        }
        return mRootView;
    }

    private View mDecorView = null;

    public View getDecorView() {
        if (getFragmentActivity() != null && mDecorView == null) {
            mDecorView = getFragmentActivity().getWindow().getDecorView();
        }
        return mDecorView;
    }

    private View mContentView = null;

    public View getContentView() {
        if (mContentView == null) {
            if (getRootView() != null) {
                mContentView = getRootView().findViewById(FragmentBuilder.systemContainerViewId);
            }
        }
        return mContentView;
    }

    private ArrayList<Fragment> mAllFragments = null;

    public ArrayList<Fragment> getAllFragments() {
        if (mAllFragments == null) {
            fillAllFragmentAndManagerAndRecord();
        }
        return mAllFragments;
    }

    private ArrayList<FragmentManager> mAllFragmentManagers = null;

    public ArrayList<FragmentManager> getAllFragmentManagers() {
        if (mAllFragmentManagers == null) {
            fillAllFragmentAndManagerAndRecord();
        }
        return mAllFragmentManagers;
    }

    private ArrayList<BackStackRecord> mAllBackStackRecords = null;

    public ArrayList<BackStackRecord> getAllBackStackRecords() {
        if (mAllBackStackRecords == null) {
            fillAllFragmentAndManagerAndRecord();
        }
        return mAllBackStackRecords;
    }

    public ArrayList<Fragment> findFragmentById(@IdRes int id) {
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        for (int i = (getAllFragments().size() - 1); i > -1; i--) {
            Fragment f = getAllFragments().get(i);
            if (f != null && f.getId() == id && f.isAdded()) {
                fragmentArrayList.add(f);
            }
        }
        return fragmentArrayList;
    }

    private void fillAllFragmentAndManagerAndRecord() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<FragmentManager> fragmentManagers = new ArrayList<>();
        ArrayList<BackStackRecord> backStackRecords = new ArrayList<>();
        if (getFragmentActivity() == null) {
            return;
        }
        fillAllFragmentAndManagerAndRecord(getFragmentActivity().getSupportFragmentManager(), fragments, fragmentManagers, backStackRecords);
        mAllFragments = fragments;
        mAllFragmentManagers = fragmentManagers;
        mAllBackStackRecords = backStackRecords;
    }

    private static void fillAllFragmentAndManagerAndRecord(FragmentManager fm, List<Fragment> fragmentList, List<FragmentManager> fragmentManagerList, ArrayList<BackStackRecord> backStackRecords) {
        if (fm != null) {
            fragmentManagerList.add(fm);
            if (fm.getBackStackEntryCount() > 0) {
                for (int i = fm.getBackStackEntryCount() - 1; i > -1; i--) {
                    BackStackRecord backStackRecord = (BackStackRecord) fm.getBackStackEntryAt(i);
                    backStackRecords.add(backStackRecord);
                }
            }
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) {
                        continue;
                    }
                    fragmentList.add(frag);
                    fillAllFragmentAndManagerAndRecord(frag.getChildFragmentManager(), fragmentList, fragmentManagerList, backStackRecords);
                }
            }
        }
    }

    public Fragment findFragmentByView(View srcView) {
        Fragment fragment = findFragmentByView(getAllFragments(), srcView, getContentView());
        return fragment;
    }

    private static Fragment findFragmentByView(List<Fragment> fragmentList, View srcView, View contentView) {
        if (fragmentList.size() == 0 || srcView == null) {
            return null;
        }
        for (Fragment frag : fragmentList) {
            //If Fragment view is null, Fragment must be invisible.
            if (frag.getView() != null) {
                if (frag.getView().equals(srcView)) {
                    return frag;
                }
            }
        }
        View parent = (View) srcView.getParent();
        if (contentView.equals(parent)) {
            //If it is top view, return null
            return null;
        }
        if (parent != null) {
            return findFragmentByView(fragmentList, parent, contentView);
        } else {
            return null;
        }
    }

    public int getSafeContainerViewId() {
        int containerViewId = 0;
        View view = FragContentPath.findAncestorOrSelf(getSrcView(), ViewPager.class);
        if (view != null) {
            FragContent content = new FragContent(view);
            Fragment fragment = content.getSrcFragment();
            if (fragment != null) {
                containerViewId = fragment.getId();
            }
        }
        return containerViewId;
    }

    private FragContentPath mFragContentPath = null;

    public FragContentPath getFragContentPath() {
        if (mFragContentPath == null) {
            mFragContentPath = new FragContentPath();
            Fragment frag = findFragmentByView(getSrcView());
            if (frag != null) {
                fillFragmentPath(mFragContentPath.fragPath, frag);
                fillViewPath(mFragContentPath.viewPath, getSrcView(), frag.getView());
            } else {
                fillViewPath(mFragContentPath.viewPath, getSrcView(), getContentView());
            }
            if (getSrcView() != null) {
                mFragContentPath.viewId = getSrcView().getId();
            }
            checkPath(this, mFragContentPath);
        }
        return mFragContentPath;
    }

    private FragContentPath mParentFragContentPath = null;

    public FragContentPath getParentFragContentPath() {
        if (mParentFragContentPath == null) {
            if (getSrcFragment() != null) {
                if (getSrcFragment().getParentFragment() != null) {
                    FragContent parentContent = new FragContent(getSrcFragment().getParentFragment());
                    mParentFragContentPath = parentContent.getFragContentPath();
                }
            }
            if (mParentFragContentPath == null) {
                FragContent parentContent = new FragContent(getFragmentActivity());
                mParentFragContentPath = parentContent.getFragContentPath();
            }
        }
        return mParentFragContentPath;
    }

    public void post(Duty duty) {
        Fragment fragment = getSrcFragment();
        if (fragment != null && fragment instanceof FragmentFix) {
            FragmentFix fragmentFix = (FragmentFix) fragment;
            fragmentFix.duty(duty);
        } else if (getFragmentActivity() != null) {
            getDecorView().post(duty);
        }
    }

    public boolean isResumed() {
        boolean isResumed = false;
        if (getSrcFragment() != null) {
            isResumed = getSrcFragment().isResumed();
        } else if (getFragmentActivity() != null) {
            isResumed = getFragmentActivity().mResumed;
        }
        return isResumed;
    }

    private static void fillFragmentPath(ArrayList<Integer> path, Fragment frag) {
        Fragment parentFrag = frag.getParentFragment();
        int index;
        if (parentFrag == null) {
            index = frag.getActivity().getSupportFragmentManager().getFragments().indexOf(frag);
        } else {
            index = parentFrag.getChildFragmentManager().getFragments().indexOf(frag);
        }
        if (index < 0) {
            throw new RuntimeException("Didn't find fragment!");
        }
        path.add(0, index);
        if (parentFrag != null) {
            fillFragmentPath(path, parentFrag);
        }
    }

    private static void fillViewPath(ArrayList<Integer> path, View srcView, View contentView) {
        if (srcView == null || srcView.equals(contentView)) {
            return;
        }
        View parent = (View) srcView.getParent();
        if (parent == null) {
            path.clear();
            return;
        }
        ViewGroup parentGroup = null;
        if (parent instanceof ViewGroup) {
            parentGroup = (ViewGroup) parent;
            int index = parentGroup.indexOfChild(srcView);
            path.add(0, index);
            fillViewPath(path, parent, contentView);
        } else {
            throw new RuntimeException(String.format("ViewParent isn't ViewGroup %s", parent));
        }
    }

    private static void checkPath(FragContent content, FragContentPath path) {
        FragmentActivity activity = content.getFragmentActivity();
        Object obj = FragContentPath.findObject(activity, path);
        if (obj == null) {
            throw new RuntimeException("Didn't match.");
        }
        if (obj instanceof View) {
            if (!content.srcView.equals(obj)) {
                throw new RuntimeException("Didn't match View.");
            }
        } else if (obj instanceof Fragment) {
            if (!content.srcFragment.equals(obj)) {
                throw new RuntimeException("Didn't match Fragment.");
            }
        } else if (obj instanceof FragmentActivity) {
            if (!content.srcFragmentActivity.equals(obj)) {
                throw new RuntimeException("Didn't match FragmentActivity.");
            }
        }
    }

    public static Fragment findAddFragment(BackStackRecord backStackRecord) {
        BackStackRecord.Op op = backStackRecord.mHead;
        while (op != null) {
            if (op.cmd == BackStackRecord.OP_ADD) {
                return op.fragment;
            }
            op = op.next;
        }
        return null;
    }

    public static Fragment findInBackStackFragment(BackStackRecord backStackRecord) {
        // 未來可能會出現  一次把兩個以上 推入 InBackStack 的 Fragment
        BackStackRecord.Op op = backStackRecord.mHead;
        while (op != null) {
            if (op.cmd == BackStackRecord.OP_REMOVE || op.cmd == BackStackRecord.OP_DETACH) {
                return op.fragment;
            }
            op = op.next;
        }
        return null;
    }


}
