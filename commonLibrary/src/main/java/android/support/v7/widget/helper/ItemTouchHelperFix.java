package android.support.v7.widget.helper;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/1/5.
 */

public class ItemTouchHelperFix extends ItemTouchHelper {
    /**
     * Creates an ItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach ItemTouchHelper to a RecyclerView via
     * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     *
     * @param callback The Callback which controls the behavior of this touch helper.
     */
    public ItemTouchHelperFix(Callback callback) {
        super(callback);
    }

    @Override
    View findChildView(MotionEvent event) {
        View view;
        if (mRecyclerView == null) {
            view = null;
        } else {
            view = super.findChildView(event);
        }
        return view;
    }
}
