package fix.android.support.v4.widget;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lin1987www.app.SwipeHelper;

/**
 * Created by lin on 2014/9/2.
 */
public class SlidingPaneLayoutFix extends SlidingPaneLayout {
    private SwipeHelper swipeHelper = new SwipeHelper();

    public SlidingPaneLayoutFix(android.content.Context context) {
        super(context);
    }

    public SlidingPaneLayoutFix(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingPaneLayoutFix(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handleTouchEvent = super.onTouchEvent(event);
        return handleTouchEvent;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isOpen()) {
            return super.onInterceptTouchEvent(ev);
        }
        swipeHelper.clearActionDown(ev);
        boolean isInterceptTouch = false;
        // If user touch nearly left edge and slide to right, intercept event to slide menu out.
        if (swipeHelper.getTouchSlop(ev) > 0) {
            if (swipeHelper.getDirection(ev) == SwipeHelper.Direction.right) {
                int leftEdge = getLeft() + SwipeHelper.getTouchSlop(this);
                if (getLeft() < swipeHelper.getLastMotionEvent().getX() &&
                        swipeHelper.getLastMotionEvent().getX() < leftEdge) {
                    swipeHelper.dittoOnTouchEvent(this);
                    isInterceptTouch = true;
                }
            }
        }
        // If child view stop consuming the touch event, intercept event and trigger event from ACTION_DOWN
        if (!isInterceptTouch) {
            if (getChildAt(1) != null && !getChildAt(1).dispatchTouchEvent(ev)) {
                swipeHelper.dittoOnTouchEvent(this);
                isInterceptTouch = true;
            }
        }
        swipeHelper.recordMotionEvent(ev);
        return isInterceptTouch;
    }


    public static boolean hitChildView(ViewGroup group, MotionEvent ev) {
        boolean isHit = false;
        final int actionIndex = ev.getActionIndex(); // always 0 for down
        final float x = ev.getX(actionIndex);
        final float y = ev.getY(actionIndex);
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            final View child = group.getChildAt(i);
            if (!canViewReceivePointerEvents(child)
                    || !isTransformedTouchPointInView(group, x, y, child, null)) {
                continue;
            }
            Log.e("Hit", child.toString());
            isHit = true;
        }
        return isHit;
    }

    /**
     * Returns true if a child view can receive pointer events.
     *
     * @hide
     */
    private static boolean canViewReceivePointerEvents(View child) {
        return child.getVisibility() == View.VISIBLE
                || child.getAnimation() != null;
    }

    /**
     * Returns true if a child view contains the specified point when transformed
     * into its coordinate space.
     * Child must not be null.
     *
     * @hide
     */
    protected static boolean isTransformedTouchPointInView(ViewGroup group, float x, float y, View child,
                                                           PointF outLocalPoint) {
        final float[] point = getTempPoint();
        point[0] = x;
        point[1] = y;
        transformPointToViewLocal(group, point, child);
        final boolean isInView = pointInView(child, point[0], point[1]);
        if (isInView && outLocalPoint != null) {
            outLocalPoint.set(point[0], point[1]);
        }
        return isInView;
    }

    /**
     * @hide
     */
    public static void transformPointToViewLocal(ViewGroup group, float[] point, View child) {
        point[0] += group.getScrollX() - child.getLeft();
        point[1] += group.getScrollY() - child.getTop();

        if (child.getMatrix() != null) {
            Matrix inverse = new Matrix();
            child.getMatrix().invert(inverse);
            inverse.mapPoints(point);
        }
    }

    /**
     * Determines whether the given point, in local coordinates is inside the view.
     */
    /*package*/
    final static boolean pointInView(View view, float localX, float localY) {
        return localX >= 0 && localX < (view.getRight() - view.getLeft())
                && localY >= 0 && localY < (view.getBottom() - view.getTop());
    }


    // Lazily-created holder for point computations.
    private static float[] mTempPoint;

    private static float[] getTempPoint() {
        if (mTempPoint == null) {
            mTempPoint = new float[2];
        }
        return mTempPoint;
    }
}