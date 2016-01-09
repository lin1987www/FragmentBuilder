package com.lin1987www.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by lin on 2014/11/10.
 */
public abstract class ViewHolder<T> implements OnLoadPageListener {
    private View view;
    private T data;
    private int position;
    private int viewResId;
    private Context context;
    private ArrayList<View> holdViewArray;

    protected static <T> View createView(int viewResId, ViewHolder<T> holder, Context context, ViewGroup parent, T data) {
        // 讓系統自己去釋放Old view
        if (holder.view != null) {
            // 先釋放 Old View
            holder.view.setTag(null);
            holder.view = null;
        }
        // 建立新的View
        holder.view = LayoutInflater.from(context).inflate(viewResId, parent, false);
        holder.bindView(holder.view);
        // 設定 Tag 跟 viewResId
        holder.view.setTag(holder);
        holder.viewResId = viewResId;
        return holder.view;
    }

    protected void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    public abstract void setData(int position, T data, ViewGroup parent, Context context);

    public abstract int newViewResId(int position, T data, ViewGroup parent, Context context);

    /**
     * Using for getView() method to create new ViewHolder according thisViewHolder.
     *
     * @return
     */
    public abstract ViewHolder<T> cloneInstance();

    public abstract void onPreSetData();

    public View getView(int position, Context context, T data) {
        if (holdViewArray == null) {
            holdViewArray = new ArrayList<>();
        }
        for (; holdViewArray.size() <= position; ) {
            holdViewArray.add(null);
        }
        if (holdViewArray.get(position) == null) {
            View view = getView(position, null, null, data, context);
            holdViewArray.set(position, view);
        }
        return holdViewArray.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent, T data) {
        return getView(position, convertView, parent, data, parent.getContext());
    }

    private View getView(int position, View convertView, ViewGroup parent, T data, Context context) {
        this.context = context;
        int newViewResId = newViewResId(position, data, parent, context);
        View view = convertView;
        boolean needNewInstance = false;
        if (view == null) {
            needNewInstance = true;
        } else {
            if (view.getTag() != null) {
                needNewInstance = ((ViewHolder<T>) view.getTag()).viewResId != newViewResId;
            }
        }
        ViewHolder<T> holder;
        if (needNewInstance) {
            holder = cloneInstance();
        } else {
            holder = (ViewHolder<T>) view.getTag();
        }
        // 設定好資料，使得 onPreSetData 可以事先知道 data 和 position
        holder.data = data;
        holder.position = position;
        holder.context = context;
        if (needNewInstance) {
            view = createView(newViewResId, holder, context, parent, data);
        }
        holder.onPreSetData();
        holder.setData(holder.position, holder.data, parent, context);
        return view;
    }

    public int getViewResId() {
        return viewResId;
    }

    public int getPosition() {
        return position;
    }

    public T getData() {
        return data;
    }

    public View getView() {
        return view;
    }

    public Context getContext() {
        return context;
    }
}
