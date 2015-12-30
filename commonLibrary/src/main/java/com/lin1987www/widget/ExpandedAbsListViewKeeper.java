package com.lin1987www.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import java.util.HashMap;

/**
 * Created by Administrator on 2015/4/4.
 */
public class ExpandedAbsListViewKeeper {
    private static final String KEY_AbsListViewState = "KEY_AbsListViewState";
    private static final String KEY_ChildViewStateMap = "KEY_ChildViewStateMap";
    private static final String KEY_ChildViewIdMap = "KEY_ChildViewIdMap";
    private static final String KEY_ScrollX = "KEY_ScrollX";
    private static final String KEY_ScrollY = "KEY_ScrollY";
    public final Bundle bundle;

    public ExpandedAbsListViewKeeper() {
        bundle = new Bundle();
    }

    public ExpandedAbsListViewKeeper(Parcelable state) {
        bundle = (Bundle) state;
    }

    public Parcelable getAbsListViewState() {
        return bundle.getParcelable(KEY_AbsListViewState);
    }

    public void setAbsListViewState(Parcelable saveInstanceState) {
        bundle.putParcelable(KEY_AbsListViewState, saveInstanceState);
    }

    public HashMap<Integer, SparseArray<Parcelable>> getChildStateMap() {
        return (HashMap<Integer, SparseArray<Parcelable>>) bundle.getSerializable(KEY_ChildViewStateMap);
    }

    public void setChildStateMap(HashMap<Integer, SparseArray<Parcelable>> childStateMap) {
        bundle.putSerializable(KEY_ChildViewStateMap, childStateMap);
    }

    public HashMap<Integer, Integer> getChildViewIdMap() {
        return (HashMap<Integer, Integer>) bundle.getSerializable(KEY_ChildViewIdMap);
    }

    public void setChildViewIdMap(HashMap<Integer, Integer> childViewIdMap) {
        bundle.putSerializable(KEY_ChildViewIdMap, childViewIdMap);
    }

    public int getScrollX() {
        return (int) bundle.getInt(KEY_ScrollX);
    }

    public void setScrollX(int scrollX) {
        bundle.putInt(KEY_ScrollX, scrollX);
    }

    public int getScrollY() {
        return bundle.getInt(KEY_ScrollY);
    }

    public void setScrollY(int scrollY) {
        bundle.putInt(KEY_ScrollY, scrollY);
    }

    public Parcelable onSaveInstanceState(AbsListView absListView, Parcelable absListViewState) {
        setAbsListViewState(absListViewState);
        setChildStateMap(new HashMap<Integer, SparseArray<Parcelable>>());
        setChildViewIdMap(new HashMap<Integer, Integer>());
        //  記錄Scroll位置
        if (absListView.getParent() instanceof FrameLayout) {
            FrameLayout wrapperView = (FrameLayout) absListView.getParent();
            setScrollX(wrapperView.getScrollX());
            setScrollY(wrapperView.getScrollY());
        }
        // 如果全部顯示才進行儲存
        if(absListView.getCount() == absListView.getChildCount()) {
            for (int index = 0; index < absListView.getChildCount(); index++) {
                SparseArray<Parcelable> saveHierarchyStateSparseArray = new SparseArray<Parcelable>();
                View view = absListView.getChildAt(index);
                view.saveHierarchyState(saveHierarchyStateSparseArray);
                getChildStateMap().put(index, saveHierarchyStateSparseArray);
                getChildViewIdMap().put(index, view.getId());
            }
        }
        return bundle;
    }

    private boolean isSameCountAndViewId(AbsListView  absListView) {
        if (absListView.getChildCount() == getChildViewIdMap().keySet().size()) {
            for (Integer index : getChildViewIdMap().keySet()) {
                View view = absListView.getChildAt(index);
                Integer viewId = getChildViewIdMap().get(index);
                if (!viewId.equals(view.getId())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void restoreState(final AbsListView absListView) {
        absListView.onRestoreInstanceState(bundle);
    }

    private void restoreChildViewState(final AbsListView absListView) {
        for (Integer index : getChildViewIdMap().keySet()) {
            View view = absListView.getChildAt(index);
            Integer viewId = getChildViewIdMap().get(index);
            if (viewId.equals(view.getId())) {
                view.restoreHierarchyState(getChildStateMap().get(index));
            }
        }
    }

    private void restoreParentScroll(final AbsListView absListView) {
        if (absListView.getParent() instanceof FrameLayout) {
            FrameLayout wrapperView = (FrameLayout) absListView.getParent();
            int x = getScrollX();
            int y = getScrollY();
            wrapperView.scrollTo(x, y);
        }
    }

    public void restore(final AbsListView absListView) {
        final Runnable restoreChildViewState = new Runnable() {
            @Override
            public void run() {
                restoreChildViewState(absListView);
            }
        };
        final Runnable restoreParentScroll = new Runnable() {
            @Override
            public void run() {
                restoreParentScroll(absListView);
            }
        };
        final Runnable restoreState = new Runnable() {
            @Override
            public void run() {
                restoreState(absListView);
                if(isSameCountAndViewId(absListView)) {
                    absListView.post(restoreChildViewState);
                    absListView.post(restoreParentScroll);
                }
            }
        };
        absListView.post(restoreState);
    }
}
