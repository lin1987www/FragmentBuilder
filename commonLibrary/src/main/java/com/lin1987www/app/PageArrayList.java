package com.lin1987www.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/22.
 */
public class PageArrayList<T> {
    private static final int NONE = -1;

    private int mFloorPage = 1;
    private int mCeilingPage = Integer.MAX_VALUE;

    private final ArrayList<T> mList = new ArrayList<>();
    private int mPageSize;
    private int mDefaultLoadPage = mFloorPage;
    private int mStartPage = NONE;
    private int mEndPage = NONE;
    private int mLastRecordPage = NONE;

    public boolean refreshFirstPageClearAll = true;

    public List<T> getList() {
        return mList;
    }

    public int getPageSize() {
        return mPageSize;
    }

    public int getDefaultLoadPage() {
        int page = mDefaultLoadPage;
        if (page == NONE) {
            page = mFloorPage;
        }
        return page;
    }

    public PageArrayList<T> init(int pageSize, int firstLoadPage) {
        return setPageSize(pageSize).setDefaultLoadPage(firstLoadPage);
    }

    public PageArrayList<T> setPageSize(int pageSize) {
        mPageSize = pageSize;
        return this;
    }

    public PageArrayList<T> setDefaultLoadPage(int defaultLoadPage) {
        mDefaultLoadPage = defaultLoadPage;
        return this;
    }

    /**
     * 限制住Page的範圍
     *
     * @param floorPage
     * @param ceilingPage
     * @return
     */
    public PageArrayList<T> setPageBoundary(int floorPage, int ceilingPage) {
        mFloorPage = floorPage;
        mCeilingPage = ceilingPage;
        return this;
    }

    public int getPrevPage() {
        mLastRecordPage = getPrevPageNonRecord();
        return mLastRecordPage;
    }

    private int getPrevPageNonRecord() {
        int page;
        if (mStartPage == NONE) {
            page = getDefaultLoadPage();
        } else if (mStartPage == mFloorPage) {
            page = mFloorPage;
        } else {
            page = mStartPage - 1;
        }
        return page;
    }

    public int getNextPage() {
        mLastRecordPage = getNextPageNonRecord();
        return mLastRecordPage;
    }

    public int getNextPageNonRecord() {
        int page;
        if (mEndPage == NONE) {
            page = getDefaultLoadPage();
        } else if (mEndPage == mCeilingPage) {
            page = mCeilingPage;
        } else {
            page = mEndPage + 1;
        }
        return page;
    }

    public int getLastRecordPage() {
        int page = mLastRecordPage;
        if (page == NONE) {
            page = getDefaultLoadPage();
        }
        return page;
    }

    public void clear() {
        mStartPage = NONE;
        mEndPage = NONE;
        mList.clear();
    }

    private void refreshPageData(int page, List<T> pageData) {
        if (refreshFirstPageClearAll && page == 1) {
            mList.clear();
            mList.addAll(pageData);
        } else {
            int minRemoveIndex = (page - mStartPage) * mPageSize;
            int maxRemoveIndex = minRemoveIndex + mPageSize - 1;
            for (; maxRemoveIndex >= minRemoveIndex; maxRemoveIndex--) {
                mList.remove(maxRemoveIndex);
            }
            mList.addAll(minRemoveIndex, pageData);
        }
    }

    /**
     * return position When ListView click item
     *
     * @param offsetPosition
     * @return
     */
    public int toPosition(int offsetPosition) {
        int offset = (mStartPage - 1) * mPageSize;
        return offset + offsetPosition;
    }

    /**
     * get page of position
     *
     * @param position
     * @return
     */
    public int toPage(int position) {
        return (int) Math.ceil((position + 1.0f) / (float) mPageSize);
    }

    /**
     * get selection of position
     *
     * @param position
     * @return
     */
    public int toSelection(int position) {
        return position % mPageSize;
    }

    public int setDataAndGetCurrentIndex(List<T> pageData, int page) {
        boolean isRefreshPage = false;
        // True if it is next page, false if it is prev page
        Boolean isNextPage = null;
        if (mStartPage == NONE) {
            // First Time Load Page
            isRefreshPage = false;
        } else if (mStartPage <= page && page <= mEndPage) {
            isRefreshPage = true;
        } else if (page == getNextPageNonRecord()) {
            isNextPage = true;
        } else if (getPrevPageNonRecord() == page) {
            // Load Page
            isNextPage = false;
        } else {
            throw new RuntimeException("Page不在允許的範圍內");
        }
        int selection = -1;
        if (isNextPage == null) {
            if (isRefreshPage) {
                // Refresh Page
                refreshPageData(page, pageData);
            } else {
                // 第一次載入資料
                mList.addAll(pageData);
                mStartPage = page;
                mEndPage = page;
                selection = 0;
            }
        } else if (isNextPage) {
            // Load Next Page
            mList.addAll(pageData);
            mEndPage = page;
            selection = (mEndPage - mStartPage) * mPageSize;
        } else {
            // Load Prev Page
            mList.addAll(0, pageData);
            mStartPage = page;
            selection = mPageSize - 1;
        }
        return selection;
    }
}