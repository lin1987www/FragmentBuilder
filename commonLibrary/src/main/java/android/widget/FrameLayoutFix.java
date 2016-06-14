package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2015/6/3.
 */
public class FrameLayoutFix extends FrameLayout {
    public FrameLayoutFix(Context context) {
        super(context);
    }

    public FrameLayoutFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
