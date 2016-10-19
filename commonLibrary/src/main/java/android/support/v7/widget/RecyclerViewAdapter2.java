package android.support.v7.widget;

import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */

public abstract class RecyclerViewAdapter2 extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter, RecyclerViewOnItemClickListener {
    
    @IntDef({AbsListView.CHOICE_MODE_NONE, AbsListView.CHOICE_MODE_SINGLE, AbsListView.CHOICE_MODE_MULTIPLE})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    public @interface ViewMode {
    }

    private
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

    private boolean mEnableMove = false;

    public void enableMove(boolean enable) {
        mEnableMove = enable;
    }

    public boolean isEnableMove() {
        return mEnableMove;
    }

    /*
    *
    *
    *
    * */

    public abstract List<Integer> getSelectedPositions();

    public abstract List<Integer> getViewIdList();

    public abstract List<SparseArray<Parcelable>> getSaveHierarchyState();

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        int maxIndex = fromPosition > toPosition ? fromPosition : toPosition;

        Collections.swap(getItemList(), fromPosition, toPosition);
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
        getItemList().remove(position);
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

    public boolean clickItem(RecyclerView recyclerView, View itemView) {
        int adapterPosition = recyclerView.getChildAdapterPosition(itemView);
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        boolean isSelected = false;
        int lastSelectedPosition = -1;
        if (getSelectedPositions().contains(adapterPosition)) {
            getSelectedPositions().remove(adapterPosition);
        } else {
            if (mViewMode == AbsListView.CHOICE_MODE_SINGLE) {
                if (getSelectedPositions().size() == 1) {
                    lastSelectedPosition = getSelectedPositions().get(0);
                }
                getSelectedPositions().clear();
            }
            getSelectedPositions().add(adapterPosition);
            isSelected = true;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (getViewMode() == AbsListView.CHOICE_MODE_SINGLE) {
            if (lastSelectedPosition > -1) {
                View lastSelectedView = layoutManager.findViewByPosition(lastSelectedPosition);
                if (lastSelectedView != null) {
                    lastSelectedView.setSelected(false);
                }
            }
        }
        itemView.setSelected(isSelected);
        onItemClick(recyclerView, adapterPosition, isSelected);

        return isSelected;
    }

    /*
     * Keep in mind that same adapter may be observed by multiple RecyclerViews.
     * */
    private RecyclerViewOnItemTouchListener mRecyclerViewOnItemTouchListener;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (null == mRecyclerViewOnItemTouchListener) {
            mRecyclerViewOnItemTouchListener = new RecyclerViewOnItemTouchListener(recyclerView.getContext());
        }
        recyclerView.addOnItemTouchListener(mRecyclerViewOnItemTouchListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnItemTouchListener(mRecyclerViewOnItemTouchListener);
    }

    /*
    *
    *
    *
    * */

    public abstract <T extends Parcelable> List<T> getItemList();

    public abstract <T extends Parcelable> T getItem(int position);

    protected HashMap<Integer, Class<? extends ViewHolder>> mResId2ViewHolderClassMap = new HashMap<>();

    public <T extends ViewHolder> T newViewHolder(ViewGroup parent, int resId) {
        Class<? extends ViewHolder> viewHolderClass = mResId2ViewHolderClassMap.get(resId);
        T viewHolder = (T) ViewHolder.create(parent, viewHolderClass);
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
        holder.onBind(data);
        /* TODO 這段程式碼應該不用寫  腦惹
        boolean isSelected = getSelectedPositions().contains(position);
        if (isSelected) {
            holder.itemView.setSelected(isSelected);
        } else {
            boolean isViewSelected = holder.itemView.isSelected();
            if (isViewSelected) {
                clickAdapterPositionIsSelected(position);
            }
        }
        */
        // restore state
        SparseArray<Parcelable> savedState = null;
        if (position < getSaveHierarchyState().size()) {
            savedState = getSaveHierarchyState().get(position);
        }
        if (savedState != null) {
            View view = holder.itemView;
            Integer viewId = getViewIdList().get(position);
            if (viewId == View.NO_ID) {
                view.setId(position);
            }
            view.restoreHierarchyState(savedState);
            view.setId(viewId);
        }
    }
}
