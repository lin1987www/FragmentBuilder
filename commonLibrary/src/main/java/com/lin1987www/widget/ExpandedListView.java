package com.lin1987www.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

/*
* Ref: http://stackoverflow.com/questions/18353515/how-to-make-multiplelistview-in-scrollview/18354096#18354096
* */
public class ExpandedListView extends ListView {
    boolean expanded = false;
    private ExpandedAbsListViewKeeper expandedViewGroupKeeper;

    public ExpandedListView(Context context) {
        super(context);
    }

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        expandedViewGroupKeeper.onSaveInstanceState(this,super.onSaveInstanceState());
        Bundle bundle = expandedViewGroupKeeper.bundle;
        // save bundle
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
        }
        super.onRestoreInstanceState(expandedViewGroupKeeper.getAbsListViewState());
    }
}