package com.lin1987www.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/2/16.
 *
    @Override
    protected Parcelable onSaveInstanceState() {
        viewGroupKeeper = new ViewGroupKeeper();
        // save data
        // viewGroupKeeper.bundle.putString(KEY_data, JacksonHelper.toJson(data));
        viewGroupKeeper.onSaveInstanceState(this, super.onSaveInstanceState());
        return viewGroupKeeper.bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        viewGroupKeeper = new ViewGroupKeeper(state);
        //restore data
        super.onRestoreInstanceState(viewGroupKeeper.getState());
        viewGroupKeeper.restore(this);
    }
 *
 */
public class ViewGroupKeeper {
    private static final String KEY_ViewGroupState = "KEY_ViewGroupState";
    private static final String KEY_ChildViewStateMap = "KEY_ChildViewStateMap";
    private static final String KEY_ChildViewIdMap = "KEY_ChildViewIdMap";

    public final Bundle bundle;

    public ViewGroupKeeper() {
        bundle = new Bundle();
    }

    public ViewGroupKeeper(Parcelable state) {
        bundle = (Bundle) state;
    }

    public Parcelable getState() {
        return bundle.getParcelable(KEY_ViewGroupState);
    }

    public void setState(Parcelable saveInstanceState) {
        bundle.putParcelable(KEY_ViewGroupState, saveInstanceState);
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

//
    public Parcelable onSaveInstanceState(ViewGroup viewGroup, Parcelable absListViewState) {
        setState(absListViewState);
        setChildStateMap(new HashMap<Integer, SparseArray<Parcelable>>());
        setChildViewIdMap(new HashMap<Integer, Integer>());
        for (int index = 0; index < viewGroup.getChildCount(); index++) {
            SparseArray<Parcelable> saveHierarchyStateSparseArray = new SparseArray<>();
            View view = viewGroup.getChildAt(index);
            int viewId = view.getId();
            if (viewId == View.NO_ID) {
                view.setId(index);
            }
            view.saveHierarchyState(saveHierarchyStateSparseArray);
            view.setId(viewId);
            getChildStateMap().put(index, saveHierarchyStateSparseArray);
            getChildViewIdMap().put(index, viewId);
        }
        return bundle;
    }

    private void restoreChildViewState(final ViewGroup viewGroup) {
        for (int index = 0; index < viewGroup.getChildCount(); index++) {
            View view = viewGroup.getChildAt(index);
            Integer viewId = getChildViewIdMap().get(index);
            SparseArray<Parcelable> sparseArray = getChildStateMap().get(index);
            if (viewId == View.NO_ID) {
                view.setId(index);
            }
            view.restoreHierarchyState(sparseArray);
            view.setId(viewId);
        }
    }

    public void restore(final ViewGroup viewGroup) {
        restoreChildViewState(viewGroup);
    }

    public void postRestore(final ViewGroup viewGroup) {
        final Runnable restoreChildViewState = new Runnable() {
            @Override
            public void run() {
                restoreChildViewState(viewGroup);
            }
        };
        viewGroup.post(restoreChildViewState);
    }
}
