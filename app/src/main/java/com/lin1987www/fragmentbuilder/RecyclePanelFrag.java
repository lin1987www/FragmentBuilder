package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ExecutorSet;
import android.support.v4.app.FragmentFix;
import android.support.v7.widget.LinearLayoutManagerFix;
import android.support.v7.widget.ModelRecyclerViewAdapter;
import android.support.v7.widget.RecyclerPanel;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAdapter;
import android.support.v7.widget.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/8.
 */

public class RecyclePanelFrag extends FragmentFix {
    private ModelViewAdapter modelViewAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private android.support.v7.widget.RecyclerPanel recyclerPanel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclepanel, container, false);
        this.recyclerPanel = (RecyclerPanel) view.findViewById(R.id.recyclerPanel);
        layoutManager = new LinearLayoutManagerFix(getContext(), RecyclerView.VERTICAL, false);
        recyclerPanel.getRecyclerView().setLayoutManager(layoutManager);
        //
        modelViewAdapter = new ModelViewAdapter();
        modelViewAdapter.setViewMode(AbsListView.CHOICE_MODE_SINGLE);
        modelViewAdapter.getPageArrayList().setDefaultLoadPage(1);
        modelViewAdapter.getPageArrayList().setPageSize(10);
        //
        recyclerPanel.getRecyclerView().setAdapter(modelViewAdapter);
        return view;
    }

    public static class ModelViewAdapter extends ModelRecyclerViewAdapter {
        @Override
        public void onLoadPage(final int page) {
            ExecutorSet.mainThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    int startNumber = (page - 1) * getPageArrayList().getPageSize() + 1;
                    int endNumber = startNumber + getPageArrayList().getPageSize();
                    ArrayList<NumberSeat> numberSeats = new ArrayList<>();
                    for (int i = startNumber; i < endNumber; i++) {
                        NumberSeat numberSeat = new NumberSeat(i);
                        numberSeats.add(numberSeat);
                    }
                    addPageData(numberSeats, page);
                }
            });
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
        public void onItemClick(RecyclerView recyclerView, int position) {
            String message = String.format("Click position %s", position);
            Toast.makeText(recyclerView.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static class NumberSeat implements Parcelable {
        public int number;

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
        }

        protected NumberSeat(Parcel in) {
            this.number = in.readInt();
        }

        public static final Parcelable.Creator<NumberSeat> CREATOR = new Parcelable.Creator<NumberSeat>() {
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
