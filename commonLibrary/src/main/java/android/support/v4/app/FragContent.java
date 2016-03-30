package android.support.v4.app;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

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

    public FragmentManager getFragmentManager(int containerViewId) {
        if (fragmentManager != null) {
            return fragmentManager;
        } else {
            setContainerViewId(containerViewId);
        }
        return fragmentManager;
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
        if (mDecorView == null) {
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
            mAllFragments = getAllFragments(getFragmentActivity().getSupportFragmentManager());
        }
        return mAllFragments;
    }

    public static ArrayList<Fragment> getAllFragments(FragmentManager fm) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fillAllFragments(fm, fragments);
        return fragments;
    }

    public static void fillAllFragments(FragmentManager fm, List<Fragment> fragmentList) {
        if (fm != null) {
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) {
                        continue;
                    }
                    fragmentList.add(frag);
                    fillAllFragments(frag.getChildFragmentManager(), fragmentList);
                }
            }
        }
    }

    public Fragment findFragmentByView(View srcView) {
        Fragment fragment = findFragmentByView(getAllFragments(), srcView, getContentView());
        return fragment;
    }

    public static Fragment findFragmentByView(List<Fragment> fragmentList, View srcView, View contentView) {
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

    public ArrayList<Integer> getFragPath() {
        ArrayList<Integer> path = new ArrayList<>(getFragContentPath().fragPath);
        return path;
    }

    public ArrayList<Integer> getViewPath() {
        ArrayList<Integer> path = new ArrayList<>(getFragContentPath().viewPath);
        return path;
    }

    public static void fillFragmentPath(ArrayList<Integer> path, Fragment frag) {
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

    public static void fillViewPath(ArrayList<Integer> path, View srcView, View contentView) {
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
}
