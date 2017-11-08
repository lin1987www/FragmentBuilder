package android.support.v7.widget;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/10/3.
 */

// Adapter - Selectable仲介者 -  viewHolder
// Selectable provides information to viewHolder how to show
// Selectable provides information to adapter isSelected or not
// Selectable support nest structure
// Fragment 裡面實作復原機制
public abstract class SelectController implements Parcelable {
    public abstract int nameResId(Parcelable data);

    public abstract String name(Parcelable data);

    private HashMap<Parcelable, Selectable> mChildrenMap = new HashMap<>();

    public Selectable getSelectable(Parcelable parent) {
        Selectable selectable;
        if (mChildrenMap.containsKey(parent)) {
            selectable = mChildrenMap.get(parent);
        } else {
            selectable = new Selectable();
            mChildrenMap.put(parent, selectable);
        }
        return selectable;
    }

    public ArrayList<Parcelable> getChildren(Parcelable parent) {
        Selectable selectable = getSelectable(parent);
        ArrayList<Parcelable> children = selectable.mChildren;
        return children;
    }

    public ArrayList<Integer> getSelectedIndexes(Parcelable parent) {
        Selectable selectable = getSelectable(parent);
        ArrayList<Integer> selectedIndexes = selectable.mSelectedIndexes;
        return selectedIndexes;
    }
}
