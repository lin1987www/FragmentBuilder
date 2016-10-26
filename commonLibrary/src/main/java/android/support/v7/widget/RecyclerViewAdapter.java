package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;

/**
 * Created by Administrator on 2016/10/17.
 * View<--->Adapter<--->Model
 * ItemTouchHelper.Callback  on Adapter
 */
public abstract class RecyclerViewAdapter<T extends Parcelable> extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperCallback.Delegate, RecyclerViewOnScrollListener.Delegate, RecyclerViewOnItemTouchListener.OnItemClickListener {
    @IntDef({AbsListView.CHOICE_MODE_NONE, AbsListView.CHOICE_MODE_SINGLE, AbsListView.CHOICE_MODE_MULTIPLE})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    public @interface ViewMode {
    }

    protected
    @ViewMode
    int mViewMode = AbsListView.CHOICE_MODE_NONE;

    public void setViewMode(@ViewMode int viewMode) {
        mViewMode = viewMode;
    }

    public
    @ViewMode
    int getViewMode() {
        return mViewMode;
    }

    /*
    *
    *
    *
    * */

    public abstract List<Integer> getSelectedPositions();

    public <ITEM extends T> ArrayList<ITEM> getSelectedItems() {
        ArrayList<ITEM> arrayList = new ArrayList<>();
        ListIterator<Integer> iterator = getSelectedPositions().listIterator(0);
        while (iterator.hasNext()) {
            ITEM item = (ITEM) getItemList().get(iterator.next());
            arrayList.add(item);
        }
        return arrayList;
    }

    protected RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(this);

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        //
        Collections.swap(getItemList(), fromPosition, toPosition);
        recyclerViewHolder.onMove(fromPosition, toPosition);
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
        //
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        //
        getItemList().remove(position);
        recyclerViewHolder.onSwiped(position);
        //
        if (getSelectedPositions().contains(position)) {
            getSelectedPositions().remove((Integer) position);
        }
        notifyItemRemoved(position);
    }

    public void clickItem(RecyclerView recyclerView, View itemView) {
        int adapterPosition = recyclerView.getChildAdapterPosition(itemView);
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        boolean isSelected = false;
        if (getViewMode() == AbsListView.CHOICE_MODE_SINGLE || getViewMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            int lastSelectedPosition = -1;
            if (getSelectedPositions().contains(adapterPosition)) {
                getSelectedPositions().remove((Integer) adapterPosition);
            } else {
                if (mViewMode == AbsListView.CHOICE_MODE_SINGLE) {
                    if (getSelectedPositions().size() == 1) {
                        lastSelectedPosition = getSelectedPositions().get(0);
                    }
                    getSelectedPositions().clear();
                    notifyItemChanged(lastSelectedPosition);
                }
                getSelectedPositions().add(adapterPosition);
                isSelected = true;
            }
            notifyItemChanged(adapterPosition);
        }
        onItemClick(recyclerView, adapterPosition, isSelected);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerViewHolder.onAttachedToRecyclerView(recyclerView);
        recyclerViewHolder.scrollForFillSpace();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerViewHolder.onDetachedFromRecyclerView(recyclerView);
    }

    /*
    *
    *
    *
    * */

    public abstract <ITEM extends T> List<ITEM> getItemList();

    public <ITEM extends T> ITEM getItem(int position) {
        ITEM item = (ITEM) getItemList().get(position);
        return item;
    }

    protected HashMap<Integer, Class<? extends ViewHolder>> mResId2ViewHolderClassMap = new HashMap<>();

    public <VH extends ViewHolder> VH newViewHolder(ViewGroup parent, int resId) {
        Class<? extends ViewHolder> viewHolderClass = mResId2ViewHolderClassMap.get(resId);
        VH viewHolder = (VH) ViewHolder.create(parent, viewHolderClass);
        return viewHolder;
    }

    /*
    *
    *
    *
    * */

    @Override
    public int getItemViewType(int position) {
        return getItemLayoutResId(position);
    }

    @LayoutRes
    public int getItemLayoutResId(int position) {
        int resId;
        Class<? extends ViewHolder> viewHolderClass = getItemViewHolderClass(position);
        resId = ViewHolder.getLayoutResId(viewHolderClass);
        if (0 == resId) {
            String message = String.format("%s have to use ViewHolder.LayoutResId Annotation!", viewHolderClass.getSimpleName());
            throw new RuntimeException(message);
        }
        mResId2ViewHolderClassMap.put(resId, viewHolderClass);
        return resId;
    }

    public abstract Class<? extends ViewHolder> getItemViewHolderClass(int position);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = newViewHolder(parent, viewType);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Parcelable data = getItem(position);
        holder.init();
        holder.onBind(this, position);

        boolean isSelected = getSelectedPositions().contains(position);
        holder.itemView.setSelected(isSelected);

        // restore state
        RecyclerViewState recyclerViewState = recyclerViewHolder.getRecyclerViewState(holder.mOwnerRecyclerView);
        if (recyclerViewState != null) {
            if (position < recyclerViewState.viewHolderStateList.size()) {
                ViewHolderState viewHolderState = recyclerViewState.viewHolderStateList.get(position);
                if (viewHolderState != null) {
                    viewHolderState.restore(holder);
                }
            }
        }
    }

    @CallSuper
    @Override
    public void onViewRecycled(ViewHolder holder) {
        recyclerViewHolder.saveViewHolder(holder);
    }

    public static class RecyclerViewHolder {
        public final static String KEY_RecyclerViewHolder = "KEY_RecyclerViewHolder";

        private RecyclerViewAdapter mRecyclerViewAdapter;

        private SparseArray<RecyclerViewState> mRecyclerViewStateSparseArray;

        public RecyclerViewHolder(RecyclerViewAdapter recyclerViewAdapter) {
            mRecyclerViewAdapter = recyclerViewAdapter;
            mRecyclerViewStateSparseArray = new SparseArray<>();
        }

        public RecyclerViewState getRecyclerViewState(RecyclerView recyclerView) {
            int id = recyclerView.getId();
            if (id == View.NO_ID) {
                String message = String.format("%s must assign id on view!", recyclerView.toString());
                throw new RuntimeException(message);
            }
            RecyclerViewState recyclerViewState = mRecyclerViewStateSparseArray.get(id);
            if (recyclerViewState == null) {
                recyclerViewState = RecyclerViewState.newRecyclerViewState(recyclerView);
                mRecyclerViewStateSparseArray.put(id, recyclerViewState);
            }
            return recyclerViewState;
        }

        public void saveViewHolder(ViewHolder viewHolder) {
            if (viewHolder == null) {
                return;
            }
            RecyclerView recyclerView = viewHolder.mOwnerRecyclerView;
            saveViewHolder(viewHolder, recyclerView);
        }

        private void saveViewHolder(ViewHolder viewHolder, RecyclerView recyclerView) {
            if (viewHolder != null && recyclerView != null) {
                RecyclerViewState recyclerViewState = getRecyclerViewState(recyclerView);
                recyclerViewState.saveViewHolderState(viewHolder);
            }
        }

        public void saveState(Bundle outState) {
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                if (recyclerView == null) {
                    continue;
                }
                // save all visible ViewHolder
                int firstPosition = -1;
                int lastPosition = -1;
                int visibleItemCount = recyclerView.getChildCount();
                if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    firstPosition = manager.findFirstVisibleItemPosition();
                    lastPosition = manager.findLastVisibleItemPosition();
                } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    firstPosition = manager.findFirstVisibleItemPositionInt();
                }
                lastPosition = firstPosition + visibleItemCount;
                for (int i = firstPosition; i <= lastPosition; i++) {
                    ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    saveViewHolder(holder, recyclerView);
                }
                // save RecyclerView state
                getRecyclerViewState(recyclerView).save(recyclerView);
            }
            outState.putSparseParcelableArray(KEY_RecyclerViewHolder, mRecyclerViewStateSparseArray);
        }

        public void restoreState(Bundle savedInstanceState) {
            mRecyclerViewStateSparseArray = savedInstanceState.getSparseParcelableArray(KEY_RecyclerViewHolder);
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                getRecyclerViewState(recyclerView).restore(recyclerView);
            }
        }

        /*
         *  用於 Scroll 可能載入更多資料上，所有RecyclerView共用同一個 RecyclerViewOnScrollListener
         * */
        protected RecyclerViewOnScrollListener mRecyclerViewOnScrollListener;
        /*
         * 用於　Item Click 上，所有RecycleView 共用同一個RecyclerViewOnItemTouchListener
         * Keep in mind that same adapter may be observed by multiple RecyclerViews.
         * */
        protected RecyclerViewOnItemTouchListener mRecyclerViewOnItemTouchListener;
        /*
        * 每個 RecyclerView 都有屬於自己的 ItemTouchHelper
        * */
        private WeakHashMap<RecyclerView, ItemTouchHelper> mRecyclerViewItemTouchHelperWeakHashMap = new WeakHashMap<>();

        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            if (null == mRecyclerViewOnItemTouchListener) {
                mRecyclerViewOnItemTouchListener = new RecyclerViewOnItemTouchListener(recyclerView.getContext());
            }
            if (null == mRecyclerViewOnScrollListener) {
                mRecyclerViewOnScrollListener = new RecyclerViewOnScrollListener(mRecyclerViewAdapter);
            }
            recyclerView.addOnItemTouchListener(mRecyclerViewOnItemTouchListener);
            recyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);
            //
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mRecyclerViewAdapter));
            itemTouchHelper.attachToRecyclerView(recyclerView);
            mRecyclerViewItemTouchHelperWeakHashMap.put(recyclerView, itemTouchHelper);
        }

        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            ItemTouchHelper itemTouchHelper = mRecyclerViewItemTouchHelperWeakHashMap.get(recyclerView);
            itemTouchHelper.attachToRecyclerView(null);
            recyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
            recyclerView.removeOnItemTouchListener(mRecyclerViewOnItemTouchListener);
            mRecyclerViewItemTouchHelperWeakHashMap.remove(recyclerView);
        }

        public void adjustGridSpan() {
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                if (recyclerView == null) {
                    continue;
                }
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (gridLayoutManager.getSpanCount() == 1) {
                        RecyclerView.ViewHolder viewHolder = mRecyclerViewAdapter.createViewHolder(recyclerView, mRecyclerViewAdapter.getItemViewType(0));
                        View view = viewHolder.itemView;
                        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int itemWidth = view.getMeasuredWidth();
                        int gridWidth = recyclerView.getWidth();
                        int maxColumns = (int) Math.floor(gridWidth / (double) itemWidth);
                        int numColumns = mRecyclerViewAdapter.getItemCount() > maxColumns ? maxColumns : mRecyclerViewAdapter.getItemCount();
                        gridLayoutManager.setSpanCount(numColumns);
                        // Refresh RecyclerView
                        recyclerView.invalidate();
                        if (recyclerView.getParent() instanceof View) {
                            ((View) recyclerView.getParent()).invalidate();
                        }
                    }
                }
            }
        }

        public void scrollForFillSpace() {
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                if (recyclerView == null) {
                    continue;
                }
                recyclerView.post(new RecyclerViewOnScrollListener.ScrollRunnable(recyclerView, 0, 0));
            }
        }

        public void onMove(int fromPosition, int toPosition) {
            int maxIndex = fromPosition > toPosition ? fromPosition : toPosition;
            for (RecyclerView rv : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                RecyclerViewState recyclerViewState = getRecyclerViewState(rv);
                ArrayList<ViewHolderState> viewHolderStateArrayList = recyclerViewState.viewHolderStateList;
                if (maxIndex < viewHolderStateArrayList.size()) {
                    Collections.swap(viewHolderStateArrayList, fromPosition, toPosition);
                }
            }
        }

        public void onSwiped(int position) {
            for (RecyclerView rv : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                RecyclerViewState recyclerViewState = getRecyclerViewState(rv);
                ArrayList<ViewHolderState> viewHolderStateArrayList = recyclerViewState.viewHolderStateList;
                if (position < viewHolderStateArrayList.size()) {
                    viewHolderStateArrayList.remove(position);
                }
            }
        }
    }

    public static class RecyclerViewState implements Parcelable {
        public int viewId;
        public SparseArray<Parcelable> savedState;
        public ArrayList<ViewHolderState> viewHolderStateList;

        public RecyclerViewState(int viewId) {
            this.viewId = viewId;
            this.viewHolderStateList = new ArrayList<>();
        }

        public static RecyclerViewState newRecyclerViewState(RecyclerView recyclerView) {
            if (recyclerView == null) {
                return null;
            }
            RecyclerViewState recyclerViewState = new RecyclerViewState(recyclerView.getId());
            return recyclerViewState;
        }

        public void save(RecyclerView recyclerView) {
            savedState = new SparseArray<>();
            recyclerView.saveHierarchyState(savedState);
        }

        public void restore(RecyclerView recyclerView) {
            recyclerView.restoreHierarchyState(savedState);
        }

        public void saveViewHolderState(ViewHolder viewHolder) {
            int adapterPosition = viewHolder.getAdapterPosition();
            ViewHolderState viewHolderState = ViewHolderState.newViewHolderState(viewHolder);
            if (viewHolderState != null) {
                while (viewHolderStateList.size() <= adapterPosition) {
                    viewHolderStateList.add(null);
                }
                viewHolderStateList.set(adapterPosition, viewHolderState);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.viewId);
            dest.writeSparseArray((SparseArray) this.savedState);
            dest.writeTypedList(this.viewHolderStateList);
        }

        protected RecyclerViewState(Parcel in) {
            this.viewId = in.readInt();
            this.savedState = in.readSparseArray(getClass().getClassLoader());
            this.viewHolderStateList = in.createTypedArrayList(ViewHolderState.CREATOR);
        }

        public static final Parcelable.Creator<RecyclerViewState> CREATOR = new Parcelable.Creator<RecyclerViewState>() {
            @Override
            public RecyclerViewState createFromParcel(Parcel source) {
                return new RecyclerViewState(source);
            }

            @Override
            public RecyclerViewState[] newArray(int size) {
                return new RecyclerViewState[size];
            }
        };
    }

    public static class ViewHolderState implements Parcelable {
        public int adapterPosition;
        public int viewId;
        public SparseArray<Parcelable> savedState;

        public static ViewHolderState newViewHolderState(ViewHolder viewHolder) {
            SparseArray<Parcelable> savedState = new SparseArray<>();
            View view = viewHolder.itemView;
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition < 0) {
                return null;
            }
            int viewId = view.getId();
            if (viewId == View.NO_ID) {
                view.setId(adapterPosition);
            }
            view.saveHierarchyState(savedState);
            view.setId(viewId);
            ViewHolderState viewHolderState = new ViewHolderState(adapterPosition, viewId, savedState);
            return viewHolderState;
        }

        public void restore(ViewHolder viewHolder) {
            if (savedState != null) {
                View view = viewHolder.itemView;
                if (viewId == View.NO_ID) {
                    view.setId(adapterPosition);
                }
                view.restoreHierarchyState(savedState);
                view.setId(viewId);
            }
        }

        public ViewHolderState(int adapterPosition, int viewId, SparseArray<Parcelable> savedState) {
            this.adapterPosition = adapterPosition;
            this.viewId = viewId;
            this.savedState = savedState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.adapterPosition);
            dest.writeInt(this.viewId);
            dest.writeSparseArray((SparseArray) this.savedState);
        }

        protected ViewHolderState(Parcel in) {
            this.adapterPosition = in.readInt();
            this.viewId = in.readInt();
            this.savedState = in.readSparseArray(getClass().getClassLoader());
        }

        public static final Parcelable.Creator<ViewHolderState> CREATOR = new Parcelable.Creator<ViewHolderState>() {
            @Override
            public ViewHolderState createFromParcel(Parcel source) {
                return new ViewHolderState(source);
            }

            @Override
            public ViewHolderState[] newArray(int size) {
                return new ViewHolderState[size];
            }
        };
    }

    public static
    @ViewMode
    int covertViewMode(int value) {
        @RecyclerViewAdapter.ViewMode int result = AbsListView.CHOICE_MODE_NONE;
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
}
