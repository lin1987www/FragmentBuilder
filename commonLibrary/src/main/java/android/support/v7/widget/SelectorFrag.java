package android.support.v7.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lin1987www.common.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public class SelectorFrag extends FragmentFix implements View.OnClickListener {
    SelectorFragArgs fragArgs;

    View mContentView;
    RecyclerView.LayoutManager mLayoutManager;
    SelectorItemAdapter mAdapter;
    RecyclerView mRecyclerView;
    Button mCancelButton;
    Button mOkButton;

    public List<Selector.Item> selectedItems = new ArrayList<>();
    public List<Integer> selectedPositions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_selector, container, false);
        fragArgs = new SelectorFragArgs(getArguments());
        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.recyclerView);
        mCancelButton = (Button) mContentView.findViewById(R.id.cancelButton);
        mOkButton = (Button) mContentView.findViewById(R.id.okButton);

        mCancelButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mContentView.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        if (mAdapter == null) {
            mAdapter = new SelectorItemAdapter();
            mAdapter.setViewMode(fragArgs.getViewMode());
            mAdapter.getPageArrayList().setDefaultLoadPage(1).setPageSize(0);
            mAdapter.addPageData(fragArgs.getSelections(), 1);
            mAdapter.getSelectedPositions().addAll(fragArgs.getSelectedPositions());
        }
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.restoreState(fragArgs.bundle);

        switch (fragArgs.getViewMode()) {
            case AbsListView.CHOICE_MODE_NONE:
            case AbsListView.CHOICE_MODE_SINGLE:
                mOkButton.setVisibility(View.GONE);
                break;
            case AbsListView.CHOICE_MODE_MULTIPLE:
                break;
        }

        return mContentView;
    }

    @Override
    public void onDestroyView() {
        mAdapter.saveState(fragArgs.bundle);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        if (view == mOkButton) {
            selectedItems.addAll(mAdapter.getSelectedItems());
            selectedPositions.addAll(mAdapter.getSelectedPositions());
            FragmentBuilder.hasPopBackStack(getActivity());
        } else if (view == mCancelButton) {
            FragmentBuilder.hasPopBackStack(getActivity());
        } else if (view == mContentView) {
            FragmentBuilder.hasPopBackStack(getActivity());
        }
    }

    public class SelectorItemAdapter extends ModelRecyclerViewAdapter<Selector.Item> {
        @Override
        public Class<? extends ViewHolder> getItemViewHolderClass(int position) {
            return SelectorItemViewHolder.class;
        }

        @Override
        public void onLoadPage(int page) {
        }

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
            switch (fragArgs.getViewMode()) {
                case AbsListView.CHOICE_MODE_NONE:
                case AbsListView.CHOICE_MODE_SINGLE:
                    if (mAdapter.getSelectedPositions().size() == 1) {
                        selectedItems.addAll(mAdapter.getSelectedItems());
                        selectedPositions.addAll(mAdapter.getSelectedPositions());
                        FragmentBuilder.hasPopBackStack(getActivity());
                    }
                    break;
                case AbsListView.CHOICE_MODE_MULTIPLE:
                    break;
            }
        }
    }

    /**
     * Created by Administrator on 2016/10/24.
     */
    @ViewHolder.LayoutResName(layout = R.layout.class, name = "listitem_selector_item")
    public static class SelectorItemViewHolder extends ViewHolder {
        TextView mName;
        ImageView mSelectedImageView;

        public SelectorItemViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.nameTextView);
            mSelectedImageView = (ImageView) view.findViewById(R.id.selectedImageView);
        }

        @Override
        public void onBind(Parcelable data) {
            Selector.Item item = (Selector.Item) data;
            mName.setText(item.selectItemName());
        }

        @Override
        public void init() {
            mName.setText(null);
            mSelectedImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
}
