package com.lin1987www.app;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import java.util.List;

/**
 * Created by Administrator on 2015/5/22.
 */
public class ViewHolderAdapter<T, VH extends ViewHolder<T>> implements ListAdapter, AbsListView.OnScrollListener {
    private final PageArrayList<T> mPageArrayList = new PageArrayList<T>();
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final VH mViewHolder;

    public ViewHolderAdapter(VH viewHolder) {
        this.mViewHolder = viewHolder;
    }

    public PageArrayList<T> getPageArrayList() {
        return mPageArrayList;
    }

    public List<T> getList() {
        return mPageArrayList.getList();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
        notifyDataSetInvalidated();
    }

    /**
     * Notifies the attached observers that the underlying data is no longer valid
     * or available. Once invoked this adapter is no longer valid and should
     * not report further data set changes.
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getCount() {
        return mPageArrayList.getList().size();
    }

    @Override
    public Object getItem(int position) {
        return mPageArrayList.getList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public View getView(int position, Context context) {
        return mViewHolder.getView(position, context, (T) getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        return mViewHolder.getView(position, convertView, viewGroup, (T) getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        // Return View Resource Id
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        // 這有跟沒有一樣... 應該不會用到
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    private AbsListView mAbsListView;

    public void init(AbsListView listView, int defaultPage) {
        mAbsListView = listView;
        getPageArrayList().setDefaultLoadPage(defaultPage);
        mAbsListView.setOnScrollListener(this);
    }


    public void addPageData(List<T> pageData, int page) {
        Parcelable state = mAbsListView.onSaveInstanceState();
        int selection = getPageArrayList().setDataAndGetCurrentIndex(pageData, page);
        if (android.os.Build.VERSION.SDK_INT > 10) {
            mAbsListView.setAdapter(this);
        } else {
            notifyDataSetChanged();
        }
        mAbsListView.onRestoreInstanceState(state);
        mIsLoading = false;
    }

    private boolean mIsLoading = false;

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int visibleThreshold = visibleItemCount;
        if (!mIsLoading) {
            if ((totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached
                mIsLoading = true;
                mViewHolder.onLoadPage(getPageArrayList().getNextPage());
            }
        }
    }
}
