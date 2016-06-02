package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.lin1987www.app.PageArrayList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2016/2/15.
 */
public abstract class RecyclerViewAdapter<VHD, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements RecyclerView.OnItemTouchListener {
    private final PageArrayList<VHD> mPageArrayList = new PageArrayList<>();

    public PageArrayList<VHD> getPageArrayList() {
        return mPageArrayList;
    }

    public List<VHD> getList() {
        return mPageArrayList.getList();
    }

    private WeakReference<Context> mContextWeak = new WeakReference<>(null);

    public Context getContext() {
        return mContextWeak.get();
    }

    private WeakReference<RecyclerView> mRecyclerViewWeak = new WeakReference<>(null);

    public RecyclerView getRecyclerView() {
        return mRecyclerViewWeak.get();
    }

    @IntDef({AbsListView.CHOICE_MODE_NONE, AbsListView.CHOICE_MODE_SINGLE, AbsListView.CHOICE_MODE_MULTIPLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewMode {
    }

    private
    @ViewMode
    int mViewMode = AbsListView.CHOICE_MODE_NONE;

    public void setViewMode(@ViewMode int viewMode) {
        this.mViewMode = viewMode;
    }

    public int getViewMode() {
        return this.mViewMode;
    }

    private GestureDetector mGestureDetector;

    private int mLastSelectedAdapterPosition = -1;

    public int getLastSelectedAdapterPosition() {
        return mLastSelectedAdapterPosition;
    }

    private final ArrayList<Integer> mSelectedAdapterPositionArray = new ArrayList<>();

    public ArrayList<Integer> getSelectedPositionArray() {
        return mSelectedAdapterPositionArray;
    }

    private OnItemClickListener mItemClickListener;

    public abstract void onLoadMore(int currentPage);

    private boolean loading = false;
    // The total number of items in the dataset after the last load
    private int previousTotal = 0;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        private int visibleThreshold = 3; // The minimum amount of items to have below your current scroll position before loading more.
        int firstVisibleItem, visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = layoutManager.getItemCount();
            if (layoutManager instanceof LinearLayoutManager) {
                firstVisibleItem = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                firstVisibleItem = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositionInt();
            }
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                onLoadMore(getPageArrayList().getNextPage());
                loading = true;
            }
        }
    };

    public void init(Context context, RecyclerView recyclerView, int firstLoadPage, int pageSize) {
        mContextWeak = new WeakReference<>(context);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        mRecyclerViewWeak = new WeakReference<>(recyclerView);

        recyclerView.removeOnItemTouchListener(this);
        recyclerView.addOnItemTouchListener(this);
        recyclerView.removeOnScrollListener(mOnScrollListener);
        recyclerView.addOnScrollListener(mOnScrollListener);
        if (getPageArrayList().getList().size() == 0) {
            getPageArrayList().init(pageSize, firstLoadPage);
            previousTotal = 0;
        } else {
            // Do nothing! if attach again, the data exist.
        }
        loading = false;
        recyclerView.setAdapter(this);
        mOnScrollListener.onScrolled(recyclerView, 0, 0);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public boolean clickAdapterPositionIsSelected(Integer adapterPosition) {
        boolean isSelected = false;
        if (mSelectedAdapterPositionArray.contains(adapterPosition)) {
            mSelectedAdapterPositionArray.remove(adapterPosition);
            mLastSelectedAdapterPosition = -1;
        } else {
            if (mViewMode == AbsListView.CHOICE_MODE_SINGLE) {
                if (mSelectedAdapterPositionArray.size() == 1) {
                    mLastSelectedAdapterPosition = mSelectedAdapterPositionArray.get(0);
                }
                mSelectedAdapterPositionArray.clear();
            }
            mSelectedAdapterPositionArray.add(adapterPosition);
            isSelected = true;
        }
        return isSelected;
    }

    private Runnable mLoadMoreToFillSpaceRunnable = new Runnable() {
        @Override
        public void run() {
            RecyclerView view = getRecyclerView();
            if (view != null) {
                mOnScrollListener.onScrolled(view, 0, 0);
            }
        }
    };

    private static class AddPageDataRunnable implements Runnable {
        WeakReference<RecyclerViewAdapter> adapterWeakReference;
        int page;
        Object pageData;

        public AddPageDataRunnable(RecyclerViewAdapter adapter, Object pageData, int page) {
            adapterWeakReference = new WeakReference<>(adapter);
            this.pageData = pageData;
            this.page = page;
        }

        @Override
        public void run() {
            RecyclerViewAdapter adapter = adapterWeakReference.get();
            if (adapter != null && adapter.getRecyclerView() != null) {
                adapter.addPageDataImmediately(pageData, page);
            }
        }
    }

    private void addPageDataImmediately(Object data, final int page) {
        Collection<VHD> pageData = (Collection<VHD>) data;
        if (pageData == null) {
            pageData = new ArrayList<>();
        }
        int selection = getPageArrayList().setDataAndGetCurrentIndex(pageData, page);
        notifyDataSetChanged();
        //
        if (page == 1) {
            loading = false;
            previousTotal = 0;
        }
        // fill all space
        int pageSize = getPageArrayList().getPageSize();
        RecyclerView view = getRecyclerView();
        if (pageSize > 0 && pageSize == pageData.size()) {
            if (view != null) {
                view.post(mLoadMoreToFillSpaceRunnable);
            }
        }
    }

    public void addPageDataPost(final Collection<VHD> pageData, final int page) {
        final RecyclerView view = getRecyclerView();
        if (view != null) {
            view.post(new AddPageDataRunnable(this, pageData, page));
        }
    }

    public void addPageData(final Collection<VHD> pageData, final int page) {
        addPageDataImmediately(pageData, page);
    }

    public void clear() {
        getPageArrayList().clear();
        loading = false;
        previousTotal = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }

    @Override
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
        boolean isSelected = mSelectedAdapterPositionArray.contains(position);
        if (isSelected) {
            holder.itemView.setSelected(isSelected);
        } else {
            boolean isViewSelected = holder.itemView.isSelected();
            if (isViewSelected) {
                clickAdapterPositionIsSelected(position);
            }
        }
    }

    public abstract
    @LayoutRes
    int onCreateViewHolderLayoutResId(ViewGroup parent, int viewType);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int layoutResId = onCreateViewHolderLayoutResId(parent, viewType);
        View view = inflater.inflate(layoutResId, parent, false);
        VH viewHolder = onCreateViewHolder(parent, viewType, view);
        return viewHolder;
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType, View view);

    // Item click and item select
    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null
                //&& childView.isEnabled()
                && mGestureDetector.onTouchEvent(e)) {
            int adapterPosition = view.getChildAdapterPosition(childView);
            if (mViewMode != AbsListView.CHOICE_MODE_NONE && adapterPosition != RecyclerView.NO_POSITION) {
                boolean isSelected = clickAdapterPositionIsSelected(adapterPosition);
                if (mViewMode == AbsListView.CHOICE_MODE_SINGLE) {
                    RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                    if (mLastSelectedAdapterPosition > -1) {
                        View lastSelectedView = layoutManager.findViewByPosition(mLastSelectedAdapterPosition);
                        if (lastSelectedView != null) {
                            lastSelectedView.setSelected(false);
                        }
                    }
                }
                childView.setSelected(isSelected);
            }
            if (mItemClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                mItemClickListener.onItemClick(childView, adapterPosition, getList().get(adapterPosition));
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

    public interface OnItemClickListener<VHD> {
        void onItemClick(View view, int position, VHD data);
    }
}
