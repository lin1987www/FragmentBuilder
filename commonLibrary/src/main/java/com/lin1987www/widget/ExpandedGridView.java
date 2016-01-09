package com.lin1987www.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by Administrator on 2015/4/21.
 */
public class ExpandedGridView extends GridView {
    private final static String KEY_expanded = "KEY_expanded";
    private final static String KEY_numColumns = "KEY_numColumns";

    int numColumns = GridView.AUTO_FIT;
    boolean expanded = false;
    private ExpandedAbsListViewKeeper expandedViewGroupKeeper;

    public ExpandedGridView(Context context) {
        super(context);
    }

    public ExpandedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        super.setNumColumns(numColumns);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded()) {
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void restoreByKeeper() {
        if (expandedViewGroupKeeper != null) {
            expandedViewGroupKeeper.restore(this);
        }
    }

    @Override
    public android.os.Parcelable onSaveInstanceState() {
        expandedViewGroupKeeper = new ExpandedAbsListViewKeeper();
        expandedViewGroupKeeper.onSaveInstanceState(this, super.onSaveInstanceState());
        Bundle bundle = expandedViewGroupKeeper.bundle;
        // save bundle
        bundle.putBoolean(KEY_expanded, expanded);
        bundle.putInt(KEY_numColumns, numColumns);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(android.os.Parcelable state) {
        if ((expandedViewGroupKeeper == null)
                ||
                (expandedViewGroupKeeper != null && expandedViewGroupKeeper.bundle != state)) {
            // 如果已經還原過的話，再呼叫一次只會調用預設的復原狀態
            expandedViewGroupKeeper = new ExpandedAbsListViewKeeper(state);
            Bundle bundle = expandedViewGroupKeeper.bundle;
            // restore bundle
            expanded = bundle.getBoolean(KEY_expanded);
            setNumColumns(bundle.getInt(KEY_numColumns));
        }
        super.onRestoreInstanceState(expandedViewGroupKeeper.getAbsListViewState());
    }
}
