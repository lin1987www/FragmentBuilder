package android.support.v4.app;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lin1987www.common.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/6.
 */
public class SelectorFrag extends FragmentFix implements RecyclerViewAdapter.OnItemClickListener<Selector.Item>, View.OnClickListener {
    SelectorFragArgs fragArgs;

    View mContentView;
    RecyclerView.LayoutManager mLayoutManager;
    SelectorItemAdapter mAdapter;
    RecyclerView mRecyclerView;
    Button mCancelButton;
    Button mOkButton;

    public ArrayList<Selector.Item> selectedItems = new ArrayList<>();
    public ArrayList<Integer> selectedPositions = new ArrayList<>();

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
            if (fragArgs.bundle.containsKey(RecyclerViewAdapter.KEY_RecyclerViewAdapter)) {
                mAdapter.init(getActivity(), mRecyclerView, 1, 0, fragArgs.bundle);
            } else {
                mAdapter.setViewMode(fragArgs.getViewMode());
                mAdapter.init(getActivity(), mRecyclerView, 1, 0, null);
                mAdapter.addPageData((ArrayList<Selector.Item>) fragArgs.getSelections(), 1);
                mAdapter.setSelectedPositions(fragArgs.getSelectedPositions());
                mAdapter.setOnItemClickListener(this);
            }
        }

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
    public void onItemClick(View view, int position, Selector.Item data) {
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

    public static class SelectorItemViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        ImageView mSelectedImageView;

        public SelectorItemViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.nameTextView);
            mSelectedImageView = (ImageView) view.findViewById(R.id.selectedImageView);
        }

        public void bind(Selector.Item item, int viewMode) {
            mName.setText(item.selectItemName());
            int imageResId = android.R.color.transparent;
            switch (viewMode) {
                case AbsListView.CHOICE_MODE_SINGLE:
                    imageResId = R.drawable.bg_selector_item_single_selected;
                    break;
                case AbsListView.CHOICE_MODE_MULTIPLE:
                    imageResId = R.drawable.bg_selector_item_multiple_selected;
                    break;
            }
            mSelectedImageView.setImageResource(imageResId);
        }

        public static SelectorItemViewHolder create(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.listitem_selector_item, parent, false);
            SelectorItemViewHolder holder = new SelectorItemViewHolder(view);
            return holder;
        }
    }

    public static class SelectorItemAdapter extends RecyclerViewAdapter<Selector.Item, SelectorItemViewHolder> {
        @Override
        public void onLoadMore(int currentPage) {

        }

        @Override
        public SelectorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SelectorItemViewHolder holder = SelectorItemViewHolder.create(parent);
            return holder;
        }

        @Override
        public void onBindViewHolder(SelectorItemViewHolder holder, int position) {
            Selector.Item item = getList().get(position);
            holder.bind(item, getViewMode());
        }
    }
}
