package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Selector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by Administrator on 2015/6/26.
 */
public class F1Fragment extends FragmentFix {
    TextView mTextView;
    FrameLayout mContainer;

    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";
    private TextView textView;
    private android.support.v7.widget.Selector selector;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f1, container, false);
        this.selector = (Selector) view.findViewById(R.id.selector);
        this.textView = (TextView) view.findViewById(R.id.textView);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f1);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F1Fragment.this)
                        .setContainerViewId(R.id.container_f1)
                        .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                        .replace()
                        .addToBackStack(F11Fragment.BACK_STACK_NAME)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .build();
            }
        });

        ArrayList<SelectItem> selectItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            selectItems.add(new SelectItem(i));
        }
        selector.setSelections(selectItems);
        selector.setName("Select Item");
        selector.setViewMode(AbsListView.CHOICE_MODE_MULTIPLE);

        return view;
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        if (isFinish) {
            result = String.format("F1 Result:\n%s\n%s\n%s", f11text, f12text, f13text);
            Toast.makeText(this.getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPopFragment(F12Fragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F12NewFragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F13Fragment fragment) {
        isFinish = fragment.isFinish;
        f13text = fragment.result;
    }

    public static class SelectItem implements Selector.Item {
        public int id = 0;

        public SelectItem(int id) {
            this.id = id;
        }

        @Override
        public String selectItemName() {
            return String.format("id=%s", id);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
        }

        public SelectItem() {
        }

        protected SelectItem(Parcel in) {
            this.id = in.readInt();
        }

        public static final Creator<SelectItem> CREATOR = new Creator<SelectItem>() {
            @Override
            public SelectItem createFromParcel(Parcel source) {
                return new SelectItem(source);
            }

            @Override
            public SelectItem[] newArray(int size) {
                return new SelectItem[size];
            }
        };
    }
}
