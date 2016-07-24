package android.support.v7.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.lin1987www.app.PageArrayList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Administrator on 2016/2/15.
 */
public abstract class RecyclerViewAdapter<VHD extends Parcelable, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements RecyclerView.OnItemTouchListener, ItemTouchHelperAdapter {
    public final static String KEY_RecyclerViewAdapter = "KEY_RecyclerViewAdapter";
    private final static String KEY_page = "KEY_page";
    private final static String KEY_viewIdList = "KEY_viewIdList";
    private final static String KEY_savedStateList = "KEY_savedStateList";
    private final static String KEY_viewMode = "KEY_viewMode";
    private final static String KEY_recyclerViewSavedState = "KEY_recyclerViewSavedState";
    private final static String KEY_selectedPositions = "KEY_selectedPositions";


    private PageArrayList<VHD> mPageArrayList = new PageArrayList<>();

    public <T extends VHD> PageArrayList<T> getPageArrayList() {
        return (PageArrayList<T>) mPageArrayList;
    }

    public <T extends VHD> ArrayList<T> getList() {
        return (ArrayList<T>) mPageArrayList.getList();
    }

    private ArrayList<Integer> mViewIdList = new ArrayList<>();

    public ArrayList<Integer> getViewIdList() {
        return mViewIdList;
    }

    public void setViewIdList(ArrayList<Integer> value) {
        mViewIdList = value;
    }

    private ArrayList<SparseArray<Parcelable>> mSavedStateList = new ArrayList<>();

    public ArrayList<SparseArray<Parcelable>> getSaveHierarchyState() {
        return mSavedStateList;
    }

    public void setSaveHierarchyState(ArrayList<SparseArray<Parcelable>> value) {
        mSavedStateList = value;
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

    private ArrayList<Integer> mSelectedAdapterPositions = new ArrayList<>();

    public ArrayList<Integer> getSelectedPositions() {
        return mSelectedAdapterPositions;
    }

    public void setSelectedPositions(ArrayList<Integer> value) {
        mSelectedAdapterPositions.clear();
        mSelectedAdapterPositions.addAll(value);
        // 重新繪製
        notifyDataSetChanged();
    }

    public <T extends VHD> ArrayList<T> getSelectedItems() {
        ArrayList<T> arrayList = new ArrayList<>();
        ListIterator<Integer> iterator = getSelectedPositions().listIterator(0);
        while (iterator.hasNext()) {
            VHD item = getList().get(iterator.next());
            arrayList.add((T) item);
        }
        return arrayList;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // TODO  測試
        int maxIndex = fromPosition > toPosition ? fromPosition : toPosition;

        Collections.swap(getList(), fromPosition, toPosition);
        if (maxIndex < getSaveHierarchyState().size()) {
            Collections.swap(getSaveHierarchyState(), fromPosition, toPosition);
        }
        if (maxIndex < getViewIdList().size()) {
            Collections.swap(getViewIdList(), fromPosition, toPosition);
        }
        if (getSelectedPositions().contains(fromPosition) && !getSelectedPositions().contains(toPosition)) {
            int index = getSelectedPositions().indexOf(fromPosition);
            int newPosition = toPosition;
            getSelectedPositions().set(index, newPosition);
        } else if (!getSelectedPositions().contains(fromPosition) && getSelectedPositions().contains(toPosition)) {
            int index = getSelectedPositions().indexOf(toPosition);
            int newPosition = toPosition;
            if (fromPosition < toPosition) {
                newPosition--;
            } else {
                newPosition++;
            }
            getSelectedPositions().set(index, newPosition);
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        int maxIndex = position;
        getList().remove(position);
        if (maxIndex < getSaveHierarchyState().size()) {
            getSaveHierarchyState().remove(position);
        }
        if (maxIndex < getViewIdList().size()) {
            getViewIdList().remove(position);
        }
        if (getSelectedPositions().contains(position)) {
            getSelectedPositions().remove((Integer) position);
        }
        notifyItemRemoved(position);
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

    private ItemTouchHelperCallback itemTouchHelperCallback;

    public void init(Context context, RecyclerView recyclerView, int firstLoadPage, int pageSize, Bundle bundle) {
        mRecyclerViewWeak = new WeakReference<>(recyclerView);
        loadSavedState(bundle);
        init(context, recyclerView, firstLoadPage, pageSize);
    }

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

        itemTouchHelperCallback = new ItemTouchHelperCallback(this);
        ItemTouchHelper helper = new ItemTouchHelper(itemTouchHelperCallback);
        helper.attachToRecyclerView(recyclerView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public boolean clickAdapterPositionIsSelected(Integer adapterPosition) {
        boolean isSelected = false;
        if (mSelectedAdapterPositions.contains(adapterPosition)) {
            mSelectedAdapterPositions.remove(adapterPosition);
            mLastSelectedAdapterPosition = -1;
        } else {
            if (mViewMode == AbsListView.CHOICE_MODE_SINGLE) {
                if (mSelectedAdapterPositions.size() == 1) {
                    mLastSelectedAdapterPosition = mSelectedAdapterPositions.get(0);
                }
                mSelectedAdapterPositions.clear();
            }
            mSelectedAdapterPositions.add(adapterPosition);
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

    private void addPageDataImmediately(Collection<? extends VHD> pageData, final int page) {
        if (pageData == null) {
            pageData = new ArrayList<>();
        }
        int selection = getPageArrayList().setDataAndGetCurrentIndex((Collection<VHD>) pageData, page);
        notifyDataSetChanged();
        //
        if (page == 1) {
            loading = false;
            previousTotal = 0;
        }
        // fill all space
        RecyclerView view = getRecyclerView();
        int pageSize = getPageArrayList().getPageSize();
        if (pageSize > 0 && pageSize == pageData.size()) {
            if (view != null) {
                view.post(mLoadMoreToFillSpaceRunnable);
            }
        }
        adjustGridSpan();
        view.invalidate();
        if (view.getParent() instanceof View) {
            ((View) view.getParent()).invalidate();
        }
    }

    public void addPageData(Collection<? extends VHD> pageData, final int page) {
        addPageDataImmediately(pageData, page);
    }

    private void adjustGridSpan() {
        if (getRecyclerView() == null) {
            return;
        }
        if (getRecyclerView().getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) getRecyclerView().getLayoutManager();
            if (gridLayoutManager.getSpanCount() == 1) {
                RecyclerView.ViewHolder viewHolder = createViewHolder(getRecyclerView(), getItemViewType(0));
                View view = viewHolder.itemView;
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int itemWidth = view.getMeasuredWidth();
                int gridWidth = getRecyclerView().getWidth();
                int maxColumns = (int) Math.floor(gridWidth / (double) itemWidth);
                int numColumns = getList().size() > maxColumns ? maxColumns : getList().size();
                gridLayoutManager.setSpanCount(numColumns);
            }
        }
    }

    public void clear() {
        getPageArrayList().clear();
        loading = false;
        previousTotal = 0;
        notifyDataSetChanged();
    }

    private void saveState(VH holder) {
        if (holder == null) {
            return;
        }
        SparseArray<Parcelable> savedState = new SparseArray<>();
        View view = holder.itemView;
        int position = holder.getAdapterPosition();
        if (position < 0) {
            return;
        }
        int viewId = view.getId();
        if (viewId == View.NO_ID) {
            view.setId(position);
        }
        view.saveHierarchyState(savedState);
        view.setId(viewId);
        while (mSavedStateList.size() <= position) {
            mSavedStateList.add(null);
        }
        mSavedStateList.set(position, savedState);
        while (mViewIdList.size() <= position) {
            mViewIdList.add(null);
        }
        mViewIdList.set(position, viewId);
    }

    public void saveState(Bundle outState) {
        int firstPosition = -1;
        int lastPosition = -1;
        if (getRecyclerView() != null) {
            int visibleItemCount = getRecyclerView().getChildCount();
            if (getRecyclerView().getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager manager = (LinearLayoutManager) getRecyclerView().getLayoutManager();
                firstPosition = manager.findFirstVisibleItemPosition();
                lastPosition = manager.findLastVisibleItemPosition();
            } else if (getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) getRecyclerView().getLayoutManager();
                firstPosition = manager.findFirstVisibleItemPositionInt();
            }
            lastPosition = firstPosition + visibleItemCount;
        }
        for (int i = firstPosition; i <= lastPosition; i++) {
            VH holder = (VH) getRecyclerView().findViewHolderForAdapterPosition(i);
            saveState(holder);
        }
        Bundle bundle = new Bundle();

        bundle.putParcelable(KEY_page, getPageArrayList());
        bundle.putIntegerArrayList(KEY_selectedPositions, getSelectedPositions());
        bundle.putIntegerArrayList(KEY_viewIdList, getViewIdList());
        bundle.putSerializable(KEY_savedStateList, getSaveHierarchyState());
        bundle.putInt(KEY_viewMode, mViewMode);
        SparseArray<Parcelable> savedState = new SparseArray<>();
        getRecyclerView().saveHierarchyState(savedState);
        bundle.putSparseParcelableArray(KEY_recyclerViewSavedState, savedState);


        outState.putParcelable(KEY_RecyclerViewAdapter, bundle);
    }

    public void loadSavedState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (!savedInstanceState.containsKey(KEY_RecyclerViewAdapter)) {
            return;
        }
        Bundle bundle = savedInstanceState.getParcelable(KEY_RecyclerViewAdapter);
        mPageArrayList = bundle.getParcelable(KEY_page);
        mSelectedAdapterPositions = bundle.getIntegerArrayList(KEY_selectedPositions);
        mViewIdList = bundle.getIntegerArrayList(KEY_viewIdList);
        mSavedStateList = (ArrayList<SparseArray<Parcelable>>) bundle.getSerializable(KEY_savedStateList);
        setViewMode(covertViewMode(bundle.getInt(KEY_viewMode)));
        SparseArray<Parcelable> savedState = bundle.getSparseParcelableArray(KEY_recyclerViewSavedState);
        getRecyclerView().restoreHierarchyState(savedState);
    }

    @CallSuper
    @Override
    public void onViewRecycled(VH holder) {
        saveState(holder);
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }

    @Override
    public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, position);
        boolean isSelected = mSelectedAdapterPositions.contains(position);
        if (isSelected) {
            holder.itemView.setSelected(isSelected);
        } else {
            boolean isViewSelected = holder.itemView.isSelected();
            if (isViewSelected) {
                clickAdapterPositionIsSelected(position);
            }
        }
        // restore state
        SparseArray<Parcelable> savedState = null;
        if (position < mSavedStateList.size()) {
            savedState = mSavedStateList.get(position);
        }
        if (savedState != null) {
            View view = holder.itemView;
            Integer viewId = mViewIdList.get(position);
            if (viewId == View.NO_ID) {
                view.setId(position);
            }
            view.restoreHierarchyState(savedState);
            view.setId(viewId);
        }
    }

    // Item click and item select
    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null) {
            // For trigger  ChildView  Button or Checkbox or something else.
            boolean origin = childView.isClickable();
            childView.setClickable(false);
            boolean isHandled = childView.dispatchTouchEvent(e);
            if (isHandled) {
                return false;
            }
            childView.setClickable(origin);
        }
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

    public static
    @ViewMode
    int covertViewMode(int value) {
        @ViewMode int result = AbsListView.CHOICE_MODE_NONE;
        switch (value) {
            case AbsListView.CHOICE_MODE_NONE:
                result = AbsListView.CHOICE_MODE_NONE;
                break;
            case AbsListView.CHOICE_MODE_SINGLE:
                result = AbsListView.CHOICE_MODE_SINGLE;
                break;
            case AbsListView.CHOICE_MODE_MULTIPLE:
                result = AbsListView.CHOICE_MODE_MULTIPLE;
                break;
        }
        return result;
    }

    /*
    public void addPageDataPost(Collection<? extends VHD> pageData, final int page) {
        final RecyclerView view = getRecyclerView();
        if (view != null) {
            view.post(new AddPageDataRunnable(this, pageData, page));
        }
    }

    private static class AddPageDataRunnable implements Runnable {
        WeakReference<RecyclerViewAdapter> adapterWeakReference;
        int page;
        Collection<? extends Parcelable> pageData;

        public AddPageDataRunnable(RecyclerViewAdapter adapter, Collection<? extends Parcelable> pageData, int page) {
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
    */
}
