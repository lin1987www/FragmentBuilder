package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcelable;

import com.lin1987www.app.PageArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by John Lin on 2016/10/20.
 * 雖然同一個 Adapter 可以被多個 RecyclerView 所連接
 * 但是關鍵的ViewHolder 會因為 getItemViewType(int position) 被資料所綁定
 * 跟所使用的 RecyclerView 沒有關聯，而RecyclerView會根據使用LayoutManager不同，而影響 ViewHolder
 * 因此只能用於 RecyclerView 對應一個 Adapter 的情況
 */
public abstract class ModelRecyclerViewAdapter<T extends Parcelable> extends RecyclerViewAdapter<T> {
    public final static String KEY_ModelRecyclerViewAdapter = "KEY_ModelRecyclerViewAdapter";
    private final static String KEY_page = "KEY_page";
    private final static String KEY_viewMode = "KEY_viewMode";
    private final static String KEY_selectedPositions = "KEY_selectedPositions";

    private PageArrayList mPageArrayList = new PageArrayList();

    public <ITEM extends Parcelable> PageArrayList<ITEM> getPageArrayList() {
        return (PageArrayList<ITEM>) mPageArrayList;
    }

    @Override
    public <ITEM extends T> List<ITEM> getItemList() {
        return (List<ITEM>) getPageArrayList().getList();
    }

    private ArrayList<Integer> mSelectedAdapterPositions = new ArrayList<>();

    @Override
    public List<Integer> getSelectedPositions() {
        return mSelectedAdapterPositions;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return ItemTouchHelperCallback.getDefaultMovementFlags(recyclerView, viewHolder);
    }

    @Override
    public int getItemCount() {
        return getItemList().size();
    }

    private boolean mIsLoading = false;

    public boolean isLoading() {
        return mIsLoading;
    }

    private int mLoadingPage = -1;

    public boolean isOnLoadPageDuringScrollCallback = false;

    @Override
    public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int visibleItemCount, int itemCount) {
        int visibleThreshold = visibleItemCount / 2;
        //if ((getItemCount() - visibleItemCount) <= (firstVisibleItem + visibleThreshold))
        int remain = (getItemCount() - firstVisibleItem - 1) - visibleItemCount - visibleThreshold;
        if (!isLoading()) {
            if (remain <= 0) {
                mIsLoading = true;
                mLoadingPage = getPageArrayList().getNextPage();
                isOnLoadPageDuringScrollCallback = true;
                onLoadPage(mLoadingPage);
                isOnLoadPageDuringScrollCallback = false;
            } else {
                recyclerView.requestLayout();
            }
        }
    }

    public abstract void onLoadPage(int page);

    public void addPageData(Collection<? extends T> pageData, int page) {
        if (pageData == null) {
            pageData = new ArrayList<>();
        }
        int selection = getPageArrayList().setDataAndGetCurrentIndex(pageData, page);
        mIsLoading = false;
        if (isOnLoadPageDuringScrollCallback) {
            // Skip notify to fix: Cannot call this method in a scroll callback. Scroll callbacks might be run during a measure & layout pass where you cannot change the RecyclerView data. Any method call that might change the structure of the RecyclerView or the adapter contents should be postponed to the next frame.
        } else if (page == mLoadingPage) {
            notifyItemRangeInserted(selection, pageData.size());
            mLoadingPage = -1;
        } else {
            notifyDataSetChanged();
            return;
        }
        // Auto adjust grid span
        recyclerViewHolder.adjustGridSpan();
        // Fill all RecyclerView space
        int pageSize = getPageArrayList().getPageSize();
        if (pageSize > 0 && pageSize == pageData.size()) {
            recyclerViewHolder.scrollForFillSpace();
        }
    }

    public void clear() {
        getPageArrayList().clear();
        mIsLoading = false;
        notifyDataSetChanged();
    }

    public void saveState(Bundle outState) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_page, getPageArrayList());
        bundle.putIntegerArrayList(KEY_selectedPositions, mSelectedAdapterPositions);
        bundle.putInt(KEY_viewMode, mViewMode);
        recyclerViewHolder.saveState(bundle);
        //
        outState.putParcelable(KEY_ModelRecyclerViewAdapter, bundle);
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (!savedInstanceState.containsKey(KEY_ModelRecyclerViewAdapter)) {
            return;
        }
        Bundle bundle = savedInstanceState.getParcelable(KEY_ModelRecyclerViewAdapter);
        mPageArrayList = bundle.getParcelable(KEY_page);
        mSelectedAdapterPositions = bundle.getIntegerArrayList(KEY_selectedPositions);
        setViewMode(covertViewMode(bundle.getInt(KEY_viewMode)));
        recyclerViewHolder.restoreState(bundle);
    }
}
