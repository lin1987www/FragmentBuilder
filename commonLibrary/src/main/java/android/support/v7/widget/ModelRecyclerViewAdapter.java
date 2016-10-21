package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcelable;

import com.lin1987www.app.PageArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2016/10/20.
 */

public abstract class ModelRecyclerViewAdapter extends RecyclerViewAdapter {
    public final static String KEY_ModelRecyclerViewAdapter = "KEY_ModelRecyclerViewAdapter";
    private final static String KEY_page = "KEY_page";
    private final static String KEY_viewMode = "KEY_viewMode";
    private final static String KEY_selectedPositions = "KEY_selectedPositions";

    private PageArrayList mPageArrayList = new PageArrayList();

    public <T extends Parcelable> PageArrayList<T> getPageArrayList() {
        return (PageArrayList<T>) mPageArrayList;
    }

    @Override
    public <T extends Parcelable> List<T> getItemList() {
        return (List<T>) getPageArrayList().getList();
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

    @Override
    public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int visibleItemCount, int itemCount) {
        int visibleThreshold = visibleItemCount;
        if (!isLoading()) {
            //if ((getItemCount() - visibleItemCount) <= (firstVisibleItem + visibleThreshold))
            int remain = (getItemCount() - firstVisibleItem - 1) - visibleItemCount - visibleThreshold;
            if (remain <= 0) {
                onLoadPage(getPageArrayList().getNextPage());
                mIsLoading = true;
            }
        }
    }

    public abstract void onLoadPage(int page);

    public void addPageData(Collection<? extends Parcelable> pageData, int page) {
        if (pageData == null) {
            pageData = new ArrayList<>();
        }
        int selection = getPageArrayList().setDataAndGetCurrentIndex(pageData, page);
        mIsLoading = false;
        if (page == getPageArrayList().getNextPageNonRecord()) {
            notifyItemRangeInserted(selection, pageData.size());
        } else {
            notifyDataSetChanged();
        }
        // Fill all RecyclerView space
        int pageSize = getPageArrayList().getPageSize();
        if (pageSize > 0 && pageSize == pageData.size()) {
            recyclerViewHolder.scrollForFillSpace();
        }
        // Auto adjust grid span
        recyclerViewHolder.adjustGridSpan();
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
