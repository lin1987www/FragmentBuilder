package android.support.v7.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

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
        if (!(recyclerView.getAdapter() instanceof RecyclerViewAdapter2)) {
            String message = String.format("%s only must apply %s!", getClass().getSimpleName(), RecyclerViewAdapter2.class.getSimpleName());
            throw new RuntimeException(message);
        }
        RecyclerViewAdapter2 adapter = (RecyclerViewAdapter2) recyclerView.getAdapter();
        @RecyclerViewAdapter2.ViewMode int viewMode = adapter.getViewMode();
        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (childView != null) {
            // For trigger  ChildView  Button or Checkbox or something else.
            boolean origin = childView.isClickable();
            childView.setClickable(false);
            boolean isHandled = childView.dispatchTouchEvent(e);
            if (isHandled) {
                return false;
            }
            childView.setClickable(origin);
        }

        if (childView != null
                //&& childView.isEnabled()
                && getGestureDetector().onTouchEvent(e)) {
            if (viewMode != AbsListView.CHOICE_MODE_NONE) {
                adapter.clickItem(recyclerView, childView);
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
