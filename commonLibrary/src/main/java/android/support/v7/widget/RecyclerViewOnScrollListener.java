package android.support.v7.widget;

import java.lang.ref.WeakReference;

/**
 * 用於Scroll 觸發載入資料
 * Created by Administrator on 2016/10/20.
 */
public class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
    public Delegate mDelegate;

    public RecyclerViewOnScrollListener(Delegate adapter) {
        this.mDelegate = adapter;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int firstVisibleItem = 0;
        int visibleItemCount = 0;
        int totalItemCount = 0;
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        if (layoutManager instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            firstVisibleItem = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositionInt();
        }
        mDelegate.onScrolled(recyclerView, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public interface Delegate {
        void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int visibleItemCount, int itemCount);
    }

    public static class ScrollRunnable implements Runnable {
        private WeakReference<RecyclerView> mRecyclerViewWeakReference;
        private int mDx;
        private int mDy;

        public ScrollRunnable(RecyclerView recyclerView, int dx, int dy) {
            mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
            mDx = dx;
            mDy = dy;
        }

        @Override
        public void run() {
            RecyclerView recyclerView = mRecyclerViewWeakReference.get();
            if (recyclerView != null) {
                RecyclerViewAdapter adapter = (RecyclerViewAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.recyclerViewHolder.mRecyclerViewOnScrollListener.onScrolled(recyclerView, mDx, mDy);
                }
            }
        }
    }
}
