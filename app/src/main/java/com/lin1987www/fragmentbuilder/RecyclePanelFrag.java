package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentFix;
import android.support.v7.widget.LinearLayoutManagerFix;
import android.support.v7.widget.ModelRecyclerViewAdapter;
import android.support.v7.widget.RecyclerPanel;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/8.
 */

public class RecyclePanelFrag extends FragmentFix {
    private ModelViewAdapter modelViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.LayoutManager layoutManager2;

    private RecyclerPanel recyclerPanel;
    private RecyclerPanel recyclerPanel2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recyclepanel, container, false);
        this.recyclerPanel2 = (RecyclerPanel) view.findViewById(R.id.recyclerPanel2);
        this.recyclerPanel = (RecyclerPanel) view.findViewById(R.id.recyclerPanel);
        layoutManager = new LinearLayoutManagerFix(getContext(), RecyclerView.VERTICAL, false);
        layoutManager2 = new LinearLayoutManagerFix(getContext(), RecyclerView.VERTICAL, false);
        recyclerPanel.getRecyclerView().setLayoutManager(layoutManager);
        recyclerPanel2.getRecyclerView().setLayoutManager(layoutManager2);
        //
        if (modelViewAdapter == null) {
            modelViewAdapter = new ModelViewAdapter();
            modelViewAdapter.setViewMode(AbsListView.CHOICE_MODE_SINGLE);
            modelViewAdapter.getPageArrayList().setDefaultLoadPage(1);
            modelViewAdapter.getPageArrayList().setPageSize(10);
        }
        //
        recyclerPanel.getRecyclerView().setAdapter(modelViewAdapter);
        recyclerPanel2.getRecyclerView().setAdapter(modelViewAdapter);
        //
        modelViewAdapter.restoreState(savedInstanceState);
        //

        Toolbar mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        // App Logo
        mToolbar.setLogo(android.R.drawable.ic_menu_gallery);
        // Title
        mToolbar.setTitle("My Title");
        // Sub Title
        mToolbar.setSubtitle("Sub title");
        mToolbar.setNavigationIcon(android.R.drawable.ic_delete);
        mToolbar.inflateMenu(R.menu.menu_recycler_panel);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        modelViewAdapter.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    public static class ModelViewAdapter extends ModelRecyclerViewAdapter<NumberSeat> {
        @Override
        public void onLoadPage(final int page) {
            /*
            ExecutorSet.mainThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    */
            int startNumber = (page - 1) * getPageArrayList().getPageSize() + 1;
            int endNumber = startNumber + getPageArrayList().getPageSize();
            ArrayList<NumberSeat> numberSeats = new ArrayList<>();
            for (int i = startNumber; i < endNumber; i++) {
                NumberSeat numberSeat = new NumberSeat(i);
                numberSeats.add(numberSeat);
            }
            addPageData(numberSeats, page);
                    /*
                }
            });
            */
        }

        @Override
        public Class<? extends ViewHolder> getItemViewHolderClass(int position) {
            Parcelable data = getItem(position);
            if (data instanceof NumberSeat) {
                return ListItemHolder.class;
            }
            return null;
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
            if (isSelected) {
                NumberSeat numberSeat = getItem(position);
                numberSeat.clickCount++;
                notifyItemChanged(position);
            }
        }
    }

    public static class NumberSeat implements Parcelable {
        public int number;
        public int clickCount;

        public NumberSeat(int number) {
            this.number = number;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.number);
            dest.writeInt(this.clickCount);
        }

        protected NumberSeat(Parcel in) {
            this.number = in.readInt();
            this.clickCount = in.readInt();
        }

        public static final Creator<NumberSeat> CREATOR = new Creator<NumberSeat>() {
            @Override
            public NumberSeat createFromParcel(Parcel source) {
                return new NumberSeat(source);
            }

            @Override
            public NumberSeat[] newArray(int size) {
                return new NumberSeat[size];
            }
        };
    }
}
