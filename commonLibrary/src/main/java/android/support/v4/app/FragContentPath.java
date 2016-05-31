package android.support.v4.app;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/29.
 */
public class FragContentPath {
    private final static String delimiter = ",";

    public int viewId = View.NO_ID;
    public ArrayList<Integer> fragPath = new ArrayList<>();
    public ArrayList<Integer> viewPath = new ArrayList<>();

    public static ArrayList<Integer> back(ArrayList<Integer> arrayList) {
        if (arrayList.size() > 0) {
            arrayList.remove(arrayList.size() - 1);
        }
        return arrayList;
    }

    public static Object findObject(FragmentActivity activity, FragContentPath fragContentPath) {
        Object obj = activity;
        FragContent content = new FragContent(activity);
        ViewGroup parent = (ViewGroup) content.getContentView();
        if (fragContentPath.fragPath.size() > 0) {
            Fragment frag = findFragment(activity, fragContentPath.fragPath);
            obj = frag;
            parent = (ViewGroup) frag.getView();
        }
        if (fragContentPath.viewPath.size() > 0) {
            View view = findViewByPath(parent, fragContentPath.viewPath, 0);
            obj = view;
        }
        return obj;
    }

    public static View findViewByPath(View parent, ArrayList<Integer> viewPath, int index) {
        if (parent == null) {
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

    public static Fragment findFragment(FragmentActivity activity, String fragmentPathString) {
        return findFragment(activity, covert(fragmentPathString));
    }

    public static Fragment findFragment(FragmentActivity activity, List<Integer> fragmentPath) {
        Fragment fragment = null;
        if (fragmentPath == null || fragmentPath.size() == 0) {
            fragment = null;
        } else {
            FragmentManager fm = activity.getSupportFragmentManager();
            for (Integer index : fragmentPath) {
                fragment = fm.getFragments().get(index);
                if (fragment == null) {
                    break;
                }
                fm = fragment.getChildFragmentManager();
            }
        }
        return fragment;
    }

    public static FragmentManager getFragmentManager(FragContent content, List<Integer> fragmentPath) {
        FragmentManager fm;
        if (fragmentPath.size() == 0) {
            fm = content.getFragmentActivity().getSupportFragmentManager();
        } else {
            fm = findFragment(content.getFragmentActivity(), fragmentPath).getChildFragmentManager();
        }
        return fm;
    }

    public static String covert(List<Integer> path) {
        String fragmentPathString = "";
        if (path.size() > 0) {
            fragmentPathString = TextUtils.join(delimiter, path);
        }
        return fragmentPathString;
    }

    public static ArrayList<Integer> covert(String pathString) {
        ArrayList<Integer> path = new ArrayList<>();
        for (String index : TextUtils.split(pathString, delimiter)) {
            path.add(Integer.parseInt(index));
        }
        return path;
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
