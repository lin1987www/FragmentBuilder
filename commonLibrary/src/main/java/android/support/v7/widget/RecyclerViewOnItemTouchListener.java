package android.support.v7.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2016/10/19.
 */

public class RecyclerViewOnItemTouchListener implements RecyclerView.OnItemTouchListener {
    private GestureDetector mGestureDetector;

    public GestureDetector getGestureDetector() {
        return mGestureDetector;
    }

    public RecyclerViewOnItemTouchListener(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {
        if (!(recyclerView.getAdapter() instanceof RecyclerViewAdapter)) {
            String message = String.format("%s only must apply %s!", getClass().getSimpleName(), RecyclerViewAdapter.class.getSimpleName());
            throw new RuntimeException(message);
        }
        RecyclerViewAdapter adapter = (RecyclerViewAdapter) recyclerView.getAdapter();
        @RecyclerViewAdapter.ViewMode int viewMode = adapter.getViewMode();
        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (childView != null) {
            // For trigger  ChildView  Button or Checkbox or something else.
            boolean origin = childView.isClickable();
            childView.setClickable(false);
            int offsetX = recyclerView.getLayoutManager().getDecoratedLeft(childView);
            int offsetY = recyclerView.getLayoutManager().getDecoratedTop(childView);
            e.offsetLocation(-offsetX, -offsetY);
            boolean isHandled = childView.dispatchTouchEvent(e);
            e.offsetLocation(offsetX, offsetY);
            childView.setClickable(origin);
            if (isHandled) {
                return false;
            }
        }

        if (childView != null && getGestureDetector().onTouchEvent(e)) {
            /* && childView.isEnabled() */
            adapter.clickItem(recyclerView, childView);
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

    /**
     * Created by Administrator on 2016/10/19.
     */
    public interface OnItemClickListener {
        void onItemClick(RecyclerView recyclerView, int position, boolean isSelected);
    }
}
