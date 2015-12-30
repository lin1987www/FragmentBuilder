package com.lin1987www.app;

import android.support.v4.view.ViewConfigurationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/6/4.
 * http://stackoverflow.com/questions/13095494/how-to-detect-swipe-direction-between-left-right-and-up-down
 */
public class SwipeHelper {
    private List<MotionEvent> mMotionEventList = new ArrayList<MotionEvent>();

    public MotionEvent getLastMotionEvent() {
        MotionEvent lastMotionEvent = null;
        if (mMotionEventList.size() > 0) {
            lastMotionEvent = mMotionEventList.get(mMotionEventList.size() - 1);
        }
        return lastMotionEvent;
    }

    public MotionEvent getLastMotionEvent(int action) {
        MotionEvent lastMotionEvent = getLastMotionEvent();
        if (lastMotionEvent != null) {
            lastMotionEvent.setAction(action);
        }
        return lastMotionEvent;
    }

    public void clearActionDown(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mMotionEventList.clear();
        }
    }

    /**
     * @param ev
     * @return last MotionEvent
     */
    public MotionEvent recordMotionEvent(MotionEvent ev) {
        MotionEvent lastMotionEvent = getLastMotionEvent();
        mMotionEventList.add(MotionEvent.obtain(ev));
        return lastMotionEvent;
    }

    public double getTouchSlop(MotionEvent e2) {
        MotionEvent lastMotionEvent = getLastMotionEvent();
        double touchSlop = 0;
        if (lastMotionEvent != null) {
            touchSlop = getTouchSlop(lastMotionEvent, e2);
        }
        return touchSlop;
    }

    public Direction getDirection(MotionEvent e2) {
        MotionEvent lastMotionEvent = getLastMotionEvent();
        Direction direction = null;
        if (lastMotionEvent != null) {
            direction = getDirection(lastMotionEvent, e2);
        }
        return direction;
    }

    public void dittoOnTouchEvent(View view) {
        for (MotionEvent ev : mMotionEventList) {
            view.onTouchEvent(ev);
        }
    }

    public static int getTouchSlop(View view) {
        int touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(view.getContext()));
        return touchSlop;
    }

    public static double getTouchSlop(MotionEvent e1, MotionEvent e2) {
        double touchSlop = Math.hypot((e2.getX() - e1.getX()), (e2.getY() - e1.getY()));
        return touchSlop;
    }

    public static Direction getDirection(MotionEvent e1, MotionEvent e2) {
        // Grab two events located on the plane at e1=(x1, y1) and e2=(x2, y2)
        // Let e1 be the initial event
        // e2 can be located at 4 different positions, consider the following diagram
        // (Assume that lines are separated by 90 degrees.)
        //
        //
        //         \ A  /
        //          \  /
        //       D   e1   B
        //          /  \
        //         / C  \
        //
        // So if (x2,y2) falls in region:
        //  A => it's an UP swipe
        //  B => it's a RIGHT swipe
        //  C => it's a DOWN swipe
        //  D => it's a LEFT swipe
        //
        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        Direction direction = getDirection(x1, y1, x2, y2);
        return direction;
    }

    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    public static Direction getDirection(float x1, float y1, float x2, float y2) {
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get(angle);
    }

    /**
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    public static double getAngle(float x1, float y1, float x2, float y2) {
        double rad = Math.atan2(y1 - y2, x2 - x1) + Math.PI;
        return (rad * 180 / Math.PI + 180) % 360;
    }

    public enum Direction {
        up,
        down,
        left,
        right;

        /**
         * Returns a direction given an angle.
         * Directions are defined as follows:
         * <p/>
         * Up: [45, 135]
         * Right: [0,45] and [315, 360]
         * Down: [225, 315]
         * Left: [135, 225]
         *
         * @param angle an angle from 0 to 360 - e
         * @return the direction of an angle
         */
        public static Direction get(double angle) {
            if (inRange(angle, 45, 135)) {
                return Direction.up;
            } else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
                return Direction.right;
            } else if (inRange(angle, 225, 315)) {
                return Direction.down;
            } else {
                return Direction.left;
            }

        }

        /**
         * @param angle an angle
         * @param init  the initial bound
         * @param end   the final bound
         * @return returns true if the given angle is in the interval [init, end).
         */
        private static boolean inRange(double angle, float init, float end) {
            return (angle >= init) && (angle < end);
        }
    }
}