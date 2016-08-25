package android.support.v4.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.lin1987www.app.SwipeHelper;

/**
 * Created by lin on 2014/12/23.
 */
public class ViewPagerFix extends ViewPager {
    SwipeHelper swipeHelper = new SwipeHelper();

    public ViewPagerFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result;
        result = super.onInterceptTouchEvent(ev);
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(false);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean canScroll = true;
        swipeHelper.clearActionDown(ev);
        if (swipeHelper.getTouchSlop(ev) > 0) {
            if (swipeHelper.getDirection(ev) == SwipeHelper.Direction.right) {
                canScroll = canScrollHorizontally(-1);
            } else if (swipeHelper.getDirection(ev) == SwipeHelper.Direction.left) {
                canScroll = canScrollHorizontally(1);
            }
        }
        swipeHelper.recordMotionEvent(ev);
        return canScroll ? super.onTouchEvent(ev) : false;
    }
}
