package android.support.v7.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by Administrator on 2016/2/15.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mListener;

    public void setItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    // 似乎有更好的做法
    GestureDetector mGestureDetector;

    RecyclerViewAdapter mRecyclerViewAdapter;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    public void setRecyclerViewAdapter(RecyclerViewAdapter recyclerViewAdapter) {
        this.mRecyclerViewAdapter = recyclerViewAdapter;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && childView.isEnabled() && mGestureDetector.onTouchEvent(e)) {
            int adapterPosition = view.getChildAdapterPosition(childView);
            if (mListener != null) {
                mListener.onItemClick(childView, adapterPosition);
            }
            if (mRecyclerViewAdapter != null) {
                boolean isSelected = mRecyclerViewAdapter.clickAdapterPositionIsSelected(adapterPosition);
                if (mRecyclerViewAdapter.getViewMode() == AbsListView.CHOICE_MODE_SINGLE) {
                    RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                    if (mRecyclerViewAdapter.getLastSelectedAdapterPosition() > -1) {
                        View lastSelectedView = layoutManager.findViewByPosition(mRecyclerViewAdapter.getLastSelectedAdapterPosition());
                        if (lastSelectedView != null) {
                            lastSelectedView.setSelected(false);
                        }
                    }
                }
                childView.setSelected(isSelected);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}