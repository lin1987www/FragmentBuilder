package android.support.v4.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerViewAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.lin1987www.common.R;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Administrator on 2016/7/6.
 */
public class Selector<T extends Selector.Item> extends TextView implements View.OnClickListener, Runnable {
    public final static String KEY_ON_SAVE_INSTANCE_STATE = "KEY_ON_SAVE_INSTANCE_STATE";
    public final static String KEY_separator = "KEY_separator";
    public final static String KEY_selections = "KEY_selections";
    public final static String KEY_selectedPositions = "KEY_selectedPositions";
    public final static String KEY_viewMode = "KEY_viewMode";
    public final static String KEY_isDisableTouch = "KEY_isDisableTouch";

    private String mSeparator = ", ";
    private final ArrayList<T> mSelections = new ArrayList<>();
    private final ArrayList<Integer> mSelectedPositions = new ArrayList<>();

    private boolean mIsDisableTouch = false;

    public boolean isDisableTouch() {
        return mIsDisableTouch;
    }

    public void setDisableTouch(boolean value) {
        mIsDisableTouch = value;
    }

    private
    @RecyclerViewAdapter.ViewMode
    int mViewMode = AbsListView.CHOICE_MODE_NONE;

    public
    @RecyclerViewAdapter.ViewMode
    int getViewMode() {
        return this.mViewMode;
    }

    public void setViewMode(@RecyclerViewAdapter.ViewMode int value) {
        this.mViewMode = value;
    }

    public void setSeparator(String value) {
        mSeparator = value;
        delayRun();
    }

    public ArrayList<T> getSelections() {
        ArrayList<T> temp = new ArrayList<>();
        temp.addAll(mSelections);
        return temp;
    }

    public void setSelections(ArrayList<T> value) {
        mSelections.clear();
        mSelections.addAll(value);
        delayRun();
    }

    public ArrayList<Integer> getSelectedPositions() {
        ArrayList<Integer> temp = new ArrayList<>();
        temp.addAll(mSelectedPositions);
        return temp;
    }

    public void setSelectedPositions(ArrayList<Integer> value) {
        mSelectedPositions.clear();
        mSelectedPositions.addAll(value);
        delayRun();
    }

    public ArrayList<T> getSelectionItems() {
        ArrayList<T> temp = new ArrayList<>();
        ListIterator<Integer> iterator = mSelectedPositions.listIterator(0);
        while (iterator.hasNext()) {
            T item = mSelections.get(iterator.next());
            temp.add(item);
        }
        return temp;
    }

    private void bindData() {
        ListIterator<Integer> iterator = mSelectedPositions.listIterator(0);
        while (iterator.hasNext()) {
            Integer position = iterator.next();
            if (position >= mSelections.size()) {
                // 超出有效範圍移除
                iterator.remove();
            }
        }

        if (mSelectedPositions.size() == 0) {
            setText(null);
        } else {
            iterator = mSelectedPositions.listIterator(0);
            StringBuilder builder = new StringBuilder();
            if (iterator.hasNext()) {
                Item item = mSelections.get(iterator.next());
                builder.append(item.selectItemName());
            }
            while (iterator.hasNext()) {
                builder.append(mSeparator);
                Item item = mSelections.get(iterator.next());
                builder.append(item.selectItemName());
            }
            setText(builder.toString());
        }
    }

    public Selector(Context context) {
        this(context, null);
    }

    public Selector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOnClickListener(this);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable(KEY_ON_SAVE_INSTANCE_STATE);
        super.onRestoreInstanceState(state);
        setSeparator(bundle.getString(KEY_separator));
        ArrayList<T> temp = bundle.getParcelableArrayList(KEY_selections);
        setSelections(temp);
        setSelectedPositions(bundle.getIntegerArrayList(KEY_selectedPositions));
        setViewMode(RecyclerViewAdapter.covertViewMode(bundle.getInt(KEY_viewMode)));
        setDisableTouch(bundle.getBoolean(KEY_isDisableTouch));
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ON_SAVE_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putString(KEY_separator, mSeparator);
        bundle.putParcelableArrayList(KEY_selections, mSelections);
        bundle.putIntegerArrayList(KEY_selectedPositions, mSelectedPositions);
        bundle.putInt(KEY_viewMode, mViewMode);
        bundle.putBoolean(KEY_isDisableTouch, mIsDisableTouch);
        return bundle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsDisableTouch) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        SelectorFragArgs args = new SelectorFragArgs();
        args.setSelections(getSelections());
        args.setSelectedPositions(getSelectedPositions());
        args.setViewMode(getViewMode());
        // TODO set FragmentTag
        FragmentBuilder.create(this)
                .addToBackStack()
                .add()
                .setFragment(SelectorFrag.class)
                .setArgs(args.bundle)
                .setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out)
                .ifExistDoNothing()
                .build();
    }

    public void onPopFragment(SelectorFrag frag) {
        if (frag.selectedPositions.size() > 0) {
            setSelectedPositions(frag.selectedPositions);
        }
    }

    private boolean isPostRun = false;

    private void delayRun() {
        if (!isPostRun) {
            isPostRun = true;
            post(this);
        }
    }

    @Override
    public void run() {
        bindData();
        isPostRun = false;
    }

    public interface Item extends Parcelable {
        String selectItemName();
    }
}
