package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ModelRecyclerViewAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAdapter;
import android.support.v7.widget.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lin1987www.common.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/11.
 */

public class NumberPickerFrag extends FragmentFix implements View.OnClickListener {
    private NumberPickerFragArgs fragArgs;

    private ConstraintLayout mLayout;
    private LinearLayout mContentLayout;
    private RecyclerView mRecyclerView;
    private Button mCancelButton;
    private Button mOkButton;

    LinearLayoutManager layoutManager;
    NumberItemAdapter adapter;

    public Integer value = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragArgs = new NumberPickerFragArgs(getArguments());

        View view = inflater.inflate(R.layout.fragment_number_picker, container, false);

        mLayout = (ConstraintLayout) view.findViewById(R.id.layout);
        mContentLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mCancelButton = (Button) view.findViewById(R.id.cancelButton);
        mOkButton = (Button) view.findViewById(R.id.okButton);

        mLayout.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        if (adapter == null) {
            adapter = new NumberItemAdapter();
            ArrayList<NumberItem> numberItems = new ArrayList<>();
            int selectedPosition = -1;
            int selectedValue = fragArgs.value();
            int position = -1;
            for (int i = fragArgs.min(); i <= fragArgs.max(); i++) {
                position++;
                if (i == fragArgs.value()) {
                    selectedPosition = position;
                }
                NumberItem item = new NumberItem(i);
                numberItems.add(item);
            }
            adapter.addPageData(numberItems, 1);
            if (fragArgs.value() != NumberPickerFragArgs.NULL && selectedPosition > -1) {
                adapter.getSelectedPositions().add(selectedPosition);
                layoutManager.scrollToPosition(selectedPosition);
            }
        }
        adapter.setFragment(this);
        adapter.setViewMode(AbsListView.CHOICE_MODE_SINGLE);
        mRecyclerView.setAdapter(adapter);
        adapter.restoreState(getFragmentArgs().bundle);

        return view;
    }

    @Override
    public void onUserVisible() {
        super.onUserVisible();
    }

    @Override
    public void onSaveState() {
        if (adapter != null) {
            adapter.saveState(getFragmentArgs().bundle);
        }
        super.onSaveState();
    }

    @Override
    public void onClick(View view) {
        if (view == mOkButton) {
            if (adapter.getSelectedPositions().size() > 0) {
                value = adapter.getSelectedItems().get(0).value;
            }
            FragmentBuilder.hasPopBackStack(getActivity());
        } else if (view == mCancelButton || view == mLayout) {
            FragmentBuilder.hasPopBackStack(getActivity());
        }
    }

    public static class NumberItem implements Parcelable {
        public int value;

        public NumberItem(int value) {
            this.value = value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.value);
        }

        public NumberItem() {
        }

        protected NumberItem(Parcel in) {
            this.value = in.readInt();
        }

        public static final Creator<NumberItem> CREATOR = new Creator<NumberItem>() {
            @Override
            public NumberItem createFromParcel(Parcel source) {
                return new NumberItem(source);
            }

            @Override
            public NumberItem[] newArray(int size) {
                return new NumberItem[size];
            }
        };
    }

    @ViewHolder.LayoutResName(layout = R.layout.class, name = "list_item_number")
    public static class NumberItemHolder extends ViewHolder {
        private TextView mNameTextView;

        public NumberItemHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        }

        @Override
        public void onBind(RecyclerViewAdapter<? extends Parcelable> adapter, int position) {
            NumberItemAdapter numberItemAdapter = (NumberItemAdapter) adapter;
            NumberItem numberItem = numberItemAdapter.getItem(position);
            mNameTextView.setText(String.format("%s", numberItem.value));
        }

        @Override
        public void init() {
            mNameTextView.setText(null);
        }
    }

    public static class NumberItemAdapter extends ModelRecyclerViewAdapter<NumberItem> {
        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onItemClick(RecyclerView recyclerView, int position, boolean isSelected) {
            // Keep only one value
            if (!isSelected && getSelectedPositions().size() == 0) {
                getSelectedPositions().add(position);
            }
        }

        @Override
        public void onLoadPage(int page) {
        }

        @Override
        public Class<? extends ViewHolder> getItemViewHolderClass(int position) {
            return NumberItemHolder.class;
        }
    }
}
