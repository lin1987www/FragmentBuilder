package android.support.v7.widget;

import android.os.Bundle;
import android.support.v4.app.FragmentArgs;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/6.
 */
public class SelectorFragArgs extends FragmentArgs {
    public final static String KEY_selections = "KEY_selections";
    public final static String KEY_selectedPositions = "KEY_selectedPositions";
    public final static String KEY_viewMode = "KEY_viewMode";

    public SelectorFragArgs() {
        super();
    }

    public SelectorFragArgs(Bundle bundle) {
        super(bundle);
    }

    private ArrayList<Selector.Item> mSelections;

    public <T extends Selector.Item> ArrayList<T> getSelections() {
        if (mSelections == null) {
            mSelections = bundle.getParcelableArrayList(KEY_selections);
        }
        return (ArrayList<T>) mSelections;
    }

    public <T extends Selector.Item> SelectorFragArgs setSelections(ArrayList<T> value) {
        bundle.putParcelableArrayList(KEY_selections, value);
        return this;
    }

    private ArrayList<Integer> mSelectedPositions;

    public ArrayList<Integer> getSelectedPositions() {
        if (mSelectedPositions == null) {
            mSelectedPositions = bundle.getIntegerArrayList(KEY_selectedPositions);
        }
        return mSelectedPositions;
    }

    public SelectorFragArgs setSelectedPositions(ArrayList<Integer> value) {
        bundle.putIntegerArrayList(KEY_selectedPositions, value);
        return this;
    }

    public
    @RecyclerViewAdapter.ViewMode
    int
    getViewMode() {
        return RecyclerViewAdapter.covertViewMode(bundle.getInt(KEY_viewMode));
    }

    public SelectorFragArgs setViewMode(@RecyclerViewAdapter.ViewMode int value) {
        bundle.putInt(KEY_viewMode, value);
        return this;
    }

}
