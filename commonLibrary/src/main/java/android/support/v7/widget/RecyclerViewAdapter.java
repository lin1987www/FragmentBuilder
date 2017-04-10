package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelperFix;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.lin1987www.common.R;
import com.lin1987www.common.Utility;

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
    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    public RecyclerViewAdapter() {
        super();
        registerAdapterDataObserver(adapterDataObserver);
    }

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

    protected RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                recyclerViewHolder.onInserted(positionStart);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            // Adjust Selected Position
            for (int i = getSelectedPositions().size() - 1; i > -1; i--) {
                Integer selectedPosition = getSelectedPositions().get(i);
                if (selectedPosition >= positionStart && selectedPosition < positionStart + itemCount) {
                    getSelectedPositions().remove(i);
                } else if (selectedPosition >= positionStart + itemCount) {
                    Integer newSelectedPosition = selectedPosition - itemCount;
                    getSelectedPositions().set(i, newSelectedPosition);
                }
            }
            // Remove data
            for (int i = itemCount - 1; i > -1; i--) {
                getItemList().remove(positionStart);
                recyclerViewHolder.onSwiped(positionStart);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            //
            for (int i = 0; i < itemCount; i++) {
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
                fromPosition++;
                toPosition++;
            }
        }
    };

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
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
        recyclerViewHolder.onDetachedFromRecyclerView(recyclerView);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    /*
    *
    *
    *
    * */

    public abstract <ITEM extends T> List<ITEM> getItemList();

    public <ITEM extends Parcelable> ITEM getItem(int position) {
        ITEM item = (ITEM) getItemList().get(position);
        return item;
    }

    protected HashMap<Integer, Class<? extends ViewHolder>> mResId2ViewHolderClassMap = new HashMap<>();

    public <VH extends ViewHolder> VH newViewHolder(ViewGroup parent, int resId) {
        Class<? extends ViewHolder> viewHolderClass = mResId2ViewHolderClassMap.get(resId);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(ViewHolder.getLayoutResId(viewHolderClass), parent, false);
        itemView = wrapViewHolderItemView(parent, itemView, resId);
        VH viewHolder = (VH) ViewHolder.create(viewHolderClass, itemView);
        return viewHolder;
    }

    public int getWrapViewHolderItemViewLayoutId() {
        return 0;
    }

    public View wrapViewHolderItemView(ViewGroup parent, View itemView, int resId) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup wrapper = null;
        int wrapperResId = getWrapViewHolderItemViewLayoutId();
        if (View.NO_ID == wrapperResId) {
            return itemView;
        } else if (0 == wrapperResId) {
            switch (getViewMode()) {
                case AbsListView.CHOICE_MODE_MULTIPLE:
                    wrapperResId = R.layout.viewholder_wrapper_multiple;
                    break;
                default:
                    wrapperResId = R.layout.viewholder_wrapper_single;
                    break;
            }
        }
        if (wrapperResId != 0) {
            wrapper = (ViewGroup) inflater.inflate(wrapperResId, parent, false);
            ViewGroup viewHolderContainer = (ViewGroup) wrapper.findViewById(R.id.viewHolderContainer);
            switch (getViewMode()) {
                case AbsListView.CHOICE_MODE_MULTIPLE:
                    wrapViewHolderMultiple(wrapper, viewHolderContainer, itemView, resId);
                    break;
                default:
                    wrapViewHolderSingle(wrapper, viewHolderContainer, itemView, resId);
                    break;
            }

            viewHolderContainer.addView(itemView);
        }
        return (wrapper != null) ? wrapper : itemView;
    }

    public void wrapViewHolderSingle(View wrapper, ViewGroup viewHolderContainer, View itemView, int resId) {
        ViewGroup.LayoutParams wrapperLayoutParams = wrapper.getLayoutParams();
        ViewGroup.LayoutParams viewHolderLayoutParams = viewHolderContainer.getLayoutParams();
        ViewGroup.LayoutParams itemLayoutParams = itemView.getLayoutParams();
        if (itemLayoutParams.width < 0) {
            wrapperLayoutParams.width = itemLayoutParams.width;
            viewHolderLayoutParams.width = itemLayoutParams.width;
        }
        if (itemLayoutParams.height < 0) {
            wrapperLayoutParams.height = itemLayoutParams.height;
            viewHolderLayoutParams.height = itemLayoutParams.height;
        }
    }

    public void wrapViewHolderMultiple(View wrapper, ViewGroup viewHolderContainer, View itemView, int resId) {
        ViewGroup.LayoutParams wrapperLayoutParams = wrapper.getLayoutParams();
        ViewGroup.LayoutParams viewHolderLayoutParams = viewHolderContainer.getLayoutParams();
        ViewGroup.LayoutParams itemLayoutParams = itemView.getLayoutParams();
        if (itemLayoutParams.width < 0) {
            wrapperLayoutParams.width = itemLayoutParams.width;
            // viewHolderLayoutParams.width = itemLayoutParams.width;
        }
        if (itemLayoutParams.height < 0) {
            wrapperLayoutParams.height = itemLayoutParams.height;
            // viewHolderLayoutParams.height = itemLayoutParams.height;
        }
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

    public static class RecyclerViewHolder implements Runnable {
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
            RecyclerView array[] = new RecyclerView[mRecyclerViewItemTouchHelperWeakHashMap.keySet().size()];
            mRecyclerViewItemTouchHelperWeakHashMap.keySet().toArray(array);
            for (RecyclerView recyclerView : array) {
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
                //
                recyclerView.setAdapter(null);
            }
            outState.putSparseParcelableArray(KEY_RecyclerViewHolder, mRecyclerViewStateSparseArray);
        }

        public void restoreState(Bundle savedInstanceState) {
            RecyclerView lastRecyclerView = null;
            mRecyclerViewStateSparseArray = savedInstanceState.getSparseParcelableArray(KEY_RecyclerViewHolder);
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                getRecyclerViewState(recyclerView).restore(recyclerView);
                lastRecyclerView = recyclerView;
            }
            if (lastRecyclerView != null) {
                lastRecyclerView.post(this);
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
            ItemTouchHelper itemTouchHelper = new ItemTouchHelperFix(new ItemTouchHelperCallback(mRecyclerViewAdapter));
            itemTouchHelper.attachToRecyclerView(recyclerView);
            mRecyclerViewItemTouchHelperWeakHashMap.put(recyclerView, itemTouchHelper);
        }

        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            recyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
            recyclerView.removeOnItemTouchListener(mRecyclerViewOnItemTouchListener);
            ItemTouchHelper itemTouchHelper = mRecyclerViewItemTouchHelperWeakHashMap.get(recyclerView);
            itemTouchHelper.attachToRecyclerView(null);
            mRecyclerViewItemTouchHelperWeakHashMap.remove(recyclerView);
        }

        public void adjustGridSpan() {
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                if (recyclerView == null) {
                    continue;
                }
                adjustGridSpan(recyclerView);
            }
        }

        public void adjustGridSpan(RecyclerView recyclerView) {
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
                    if (numColumns > 0) {
                        gridLayoutManager.setSpanCount(numColumns);
                    }
                    // Refresh RecyclerView
                    recyclerView.invalidate();
                    if (recyclerView.getParent() instanceof View) {
                        ((View) recyclerView.getParent()).invalidate();
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
                viewHolderStateArrayList.remove(position);

            }
        }

        public void onInserted(int position) {
            for (RecyclerView rv : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                RecyclerViewState recyclerViewState = getRecyclerViewState(rv);
                ArrayList<ViewHolderState> viewHolderStateArrayList = recyclerViewState.viewHolderStateList;
                while (position > viewHolderStateArrayList.size()) {
                    viewHolderStateArrayList.add(null);
                }
                viewHolderStateArrayList.add(position, null);
            }
        }

        @Override
        public void run() {
            for (RecyclerView recyclerView : mRecyclerViewItemTouchHelperWeakHashMap.keySet()) {
                RecyclerViewState recyclerViewState = getRecyclerViewState(recyclerView);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.onRestoreInstanceState(recyclerViewState.layoutManagerSavedState);
                }
            }
            adjustGridSpan();
            scrollForFillSpace();
        }
    }

    public static class RecyclerViewState implements Parcelable {
        public int viewId;
        public Parcelable layoutManagerSavedState;
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
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManagerSavedState = layoutManager.onSaveInstanceState();
            }
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
            dest.writeParcelable(this.layoutManagerSavedState, flags);
            dest.writeTypedList(this.viewHolderStateList);
        }

        protected RecyclerViewState(Parcel in) {
            this.viewId = in.readInt();
            this.savedState = in.readSparseArray(Utility.getClassLoader());
            this.layoutManagerSavedState = in.readParcelable(Utility.getClassLoader());
            this.viewHolderStateList = in.createTypedArrayList(ViewHolderState.CREATOR);
        }

        public static final Creator<RecyclerViewState> CREATOR = new Creator<RecyclerViewState>() {
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
            this.savedState = in.readSparseArray(Utility.getClassLoader());
        }

        public static final Creator<ViewHolderState> CREATOR = new Creator<ViewHolderState>() {
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
}
