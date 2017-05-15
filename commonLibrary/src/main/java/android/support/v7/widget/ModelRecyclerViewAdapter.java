package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.lin1987www.app.PageArrayList;

import java.lang.ref.WeakReference;
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
    private final static String KEY_itemsBundle = "KEY_itemsBundle";

    private Bundle mItemsBundle;

    private String getItemBundleKey(int position) {
        return String.format("%s", position);
    }

    public Bundle getItemBundle(int position) {
        Bundle bundle;
        if (mItemsBundle == null) {
            mItemsBundle = new Bundle();
        }
        String key = getItemBundleKey(position);
        if (mItemsBundle.containsKey(key)) {
            bundle = mItemsBundle.getBundle(key);
        } else {
            bundle = new Bundle();
            mItemsBundle.putBundle(key, bundle);
        }
        return bundle;
    }

    public ArrayList<Bundle> getSelectedItemBundle() {
        ArrayList<Bundle> arrayList = new ArrayList<>();
        if (getSelectedPositions().size() > 0) {
            for (int position : getSelectedPositions()) {
                Bundle bundle = getItemBundle(position);
                arrayList.add(bundle);
            }
        }
        return arrayList;
    }

    private WeakReference<Fragment> mFragmentWeakReference;

    public <T extends Fragment> void setFragment(T fragment) {
        mFragmentWeakReference = new WeakReference<>(fragment);
    }

    public <T extends Fragment> T getFragment() {
        return (T) mFragmentWeakReference.get();
    }

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

    public ModelRecyclerViewAdapter<T> setLoading(boolean isLoading) {
        mIsLoading = isLoading;
        return this;
    }

    private int mLoadingPage = -1;

    public int getLoadingPage() {
        return mLoadingPage;
    }

    public boolean isOnLoadPageDuringScrollCallback = false;

    @Override
    public void onScrolled(RecyclerView recyclerView, int firstVisibleItem, int visibleItemCount, int itemCount) {
        int visibleThreshold = visibleItemCount / 2;
        //if ((getItemCount() - visibleItemCount) <= (firstVisibleItem + visibleThreshold))
        int remain = (getItemCount() - firstVisibleItem - 1) - visibleItemCount - visibleThreshold;
        if (!isLoading()) {
            if (remain <= 0 && getPageArrayList().hasNextPage()) {
                setLoading(true);
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

    public <DATA extends T> void addPageData(Collection<DATA> pageData, int page) {
        setLoading(false);
        if (pageData == null) {
            pageData = new ArrayList<>();
        }
        if (page == 1) {
            if (getPageArrayList().getList() == pageData) {
                ArrayList<DATA> tempPageData = new ArrayList<>();
                tempPageData.addAll(pageData);
                clear();
                pageData.addAll(tempPageData);
            } else {
                clear();
            }
        } else if (getPageArrayList().getPageSize() == 0 && page == 2) {
            getPageArrayList().setPageSize(getPageArrayList().getList().size());
        }
        int selection = getPageArrayList().setDataAndGetCurrentIndex(pageData, page);
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

    public void insertData(Collection<? extends T> dataCollection, int position) {
        // Adjust Selected Position
        for (int i = 0; i < getSelectedPositions().size(); i++) {
            Integer selectedPosition = getSelectedPositions().get(i);
            if (selectedPosition >= position) {
                Integer newSelectedPosition = selectedPosition + dataCollection.size();
                getSelectedPositions().set(i, newSelectedPosition);
            }
        }
        // Insert data
        getPageArrayList().getList().addAll(position, dataCollection);
        //
        notifyItemRangeInserted(position, dataCollection.size());
        // Auto adjust grid span
        recyclerViewHolder.adjustGridSpan();
    }

    public void removeData(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void clear() {
        getPageArrayList().clear();
        getSelectedPositions().clear();
        if (mItemsBundle != null) {
            mItemsBundle.clear();
        }
        setLoading(false);
        notifyDataSetChanged();
    }

    public String getSaveStateKey() {
        return String.format("%s_%s", KEY_ModelRecyclerViewAdapter, getClass().getName());
    }

    public void saveState(Bundle outState) {
        if (getPageArrayList().getList().size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_page, getPageArrayList());
            bundle.putIntegerArrayList(KEY_selectedPositions, mSelectedAdapterPositions);
            bundle.putInt(KEY_viewMode, mViewMode);
            bundle.putBundle(KEY_itemsBundle, mItemsBundle);
            recyclerViewHolder.saveState(bundle);
            //
            outState.putBundle(getSaveStateKey(), bundle);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        // 當在正在入時，離開了Fragment後，載入任務被取消，返回但因為 adapter 依然存在，因此在等待載入已被取消的任務
        setLoading(false);
        if (savedInstanceState == null) {
            return;
        }
        if (!savedInstanceState.containsKey(getSaveStateKey())) {
            return;
        }
        try {
            Bundle bundle = savedInstanceState.getBundle(getSaveStateKey());
            // setLoading(false);
            mPageArrayList = bundle.getParcelable(KEY_page);
            mSelectedAdapterPositions = bundle.getIntegerArrayList(KEY_selectedPositions);
            setViewMode(covertViewMode(bundle.getInt(KEY_viewMode)));
            mItemsBundle = bundle.getBundle(KEY_itemsBundle);
            recyclerViewHolder.restoreState(bundle);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            clear();
        }
    }
}
