package android.support.v7.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2016/2/15.
 */
public class LinearLayoutManagerFix extends LinearLayoutManager {
    public LinearLayoutManagerFix(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        if (getOrientation() == HORIZONTAL) {
            int height = 0;
            Log.i("msg", "onMeasure---MeasureSpec-" + View.MeasureSpec.getSize(heightSpec));
            int childCount = getItemCount();
            for (int i = 0; i < childCount; i++) {
                View child = recycler.getViewForPosition(i);
                measureChild(child, widthSpec, heightSpec);
                int measuredHeight = child.getMeasuredHeight() + getDecoratedBottom(child);
                if (measuredHeight > height) {
                    height = measuredHeight;
                }
            }
            Log.i("msg", "onMeasure---height-" + height);
            setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }
}
