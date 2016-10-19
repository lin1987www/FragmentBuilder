package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.support.v7.widget.LinearLayoutManagerFix;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/10/8.
 */

public class RecycleViewFrag extends FragmentFix {

    private android.support.v7.widget.RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycleview, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManagerFix(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 單筆資料  資料集(分頁)  資料Adapter ->　RecycleView(作為資料存體)　　　動作　　


        return view;
    }

    public static class ItemAdapter extends RecyclerViewAdapter {
        @Override
        public void onLoadMore(int currentPage) {

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }
    }
}
