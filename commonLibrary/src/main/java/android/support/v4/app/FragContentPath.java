package android.support.v4.app;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/29.
 */
public class FragContentPath {
    public int viewId = View.NO_ID;
    public ArrayList<Integer> fragPath = new ArrayList<>();
    public ArrayList<Integer> viewPath = new ArrayList<>();

    public static Object findObject(FragmentActivity activity, FragContentPath fragContentPath) {
        if (fragContentPath == null || activity == null) {
            return null;
        }
        Object obj = activity;
        FragContent content = new FragContent(activity);
        ViewGroup parent = (ViewGroup) content.getContentView();
        if (fragContentPath.fragPath.size() > 0) {
            Fragment frag = findFragment(activity, fragContentPath.fragPath);
            if (frag == null) {
                return null;
            }
            obj = frag;
            parent = (ViewGroup) frag.getView();
        }
        if (fragContentPath.viewPath.size() > 0) {
            View view = findViewByPath(parent, fragContentPath.viewPath, 0);
            if (view == null) {
                return null;
            }
            obj = view;
        }
        return obj;
    }

    private static Fragment findFragment(FragmentActivity activity, List<Integer> fragmentPath) {
        Fragment fragment = null;
        if (fragmentPath == null || fragmentPath.size() == 0) {
            fragment = null;
        } else {
            FragmentManager fm = activity.getSupportFragmentManager();
            if (fm != null) {
                for (Integer index : fragmentPath) {
                    List<Fragment> fragments = fm.getFragments();
                    if (fragments != null && index < fragments.size()) {
                        fragment = fm.getFragments().get(index);
                    }
                    if (fragment == null) {
                        break;
                    }
                    fm = fragment.mChildFragmentManager;
                }
            }
        }
        return fragment;
    }

    private static View findViewByPath(View parent, ArrayList<Integer> viewPath, int index) {
        if (parent == null || !(parent instanceof ViewGroup)) {
            return null;
        }
        int i = viewPath.get(index);
        ViewGroup viewGroup = (ViewGroup) parent;
        View view = viewGroup.getChildAt(i);
        if (viewPath.size() == (index + 1)) {
            return view;
        } else {
            return findViewByPath(view, viewPath, index + 1);
        }
    }

    /*
            Use this method to avoid finding fragment in ViewPager
    */
    public static View findAncestorOrSelf(View srcView, Class<?> targetClass) {
        if (srcView == null) {
            return null;
        }
        View view = srcView;
        View targetView = srcView;
        FragContent content = new FragContent(srcView);
        View contentView = content.getContentView();
        if (contentView == null) {
            return targetView;
        }
        do {
            if (targetClass.isInstance(view)) {
                targetView = view;
            }
            view = (View) view.getParent();
        } while (view != null && !view.equals(contentView));
        return targetView;
    }
}
