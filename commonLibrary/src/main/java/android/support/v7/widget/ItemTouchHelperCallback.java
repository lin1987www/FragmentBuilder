package android.support.v7.widget;

import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * 用於 Move 與 Drag 是 ItemTouchHelper　的建立參數
 * Created by Administrator on 2016/7/17.
 */
public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    public Delegate mDelegate;

    public ItemTouchHelperCallback(Delegate adapter) {
        this.mDelegate = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mDelegate.isLongPressDragEnabled();
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return mDelegate.isItemViewSwipeEnabled();
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return mDelegate.getMovementFlags(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return mDelegate.onMove(recyclerView, viewHolder, target);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mDelegate.onSwiped(viewHolder, direction);
    }

    public static int getDefaultMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = 0;
        int swipeFlags = 0;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            if (layoutManager instanceof LinearLayoutManager) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            } else if (layoutManager instanceof GridLayoutManager) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
            }
        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /*
    * 將功能抽象化
    * */
    public interface Delegate {
        boolean isItemViewSwipeEnabled();

        boolean isLongPressDragEnabled();

        int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

        /**
         * @see RecyclerView#getAdapterPositionFor(RecyclerView.ViewHolder)
         * @see RecyclerView.ViewHolder#getAdapterPosition()
         */
        boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

        /**
         * @see RecyclerView#getAdapterPositionFor(RecyclerView.ViewHolder)
         * @see RecyclerView.ViewHolder#getAdapterPosition()
         */
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);
    }
}