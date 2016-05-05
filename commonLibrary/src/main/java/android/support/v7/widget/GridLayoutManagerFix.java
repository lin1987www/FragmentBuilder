package android.support.v7.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2016/2/15.
 */
public class GridLayoutManagerFix extends GridLayoutManager {
    public GridLayoutManagerFix(Context context, int spanCount) {
        super(context, spanCount);
    }

    public GridLayoutManagerFix(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int height = 0;
        Log.i("msg", "onMeasure---MeasureSpec-" + View.MeasureSpec.getSize(heightSpec));
        int childCount = getItemCount();
        for (int i = 0; i < childCount; i++) {
            View child = recycler.getViewForPosition(i);
            measureChild(child, widthSpec, heightSpec);
            if (i % getSpanCount() == 0) {
                int measuredHeight = child.getMeasuredHeight() + getDecoratedBottom(child);
                height += measuredHeight;
            }
        }
        Log.i("msg", "onMeasure---height-" + height);
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
    }
}
