package com.lin1987www.fragmentbuilder;

import android.support.v7.widget.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ListItemHolder extends ViewHolder {
    private ImageView image;
    private TextView name;

    public ListItemHolder(View view) {
        super(view);
        image = (ImageView) view.findViewById(R.id.image);
        name = (TextView) view.findViewById(R.id.name);
    }

    @Override
    public int viewResId() {
        return R.layout.list_item;
    }

    @Override
    public ViewHolder newViewHolder(View itemView) {
        return new ListItemHolder(itemView);
    }

    public ImageView getImage() {
        return image;
    }

    public TextView getName() {
        return name;
    }
}
