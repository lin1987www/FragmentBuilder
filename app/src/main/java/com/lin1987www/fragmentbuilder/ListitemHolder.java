package com.lin1987www.fragmentbuilder;

import android.os.Parcelable;
import android.support.v7.widget.RecyclerViewAdapter;
import android.support.v7.widget.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

@ViewHolder.LayoutResId(id = R.layout.list_item)
public class ListItemHolder extends ViewHolder {
    private ImageView image;
    private TextView name;

    public ListItemHolder(View view) {
        super(view);
        image = (ImageView) view.findViewById(R.id.image);
        name = (TextView) view.findViewById(R.id.name);
    }

    @Override
    public void onBind(RecyclerViewAdapter adapter, int position) {
        Parcelable data = adapter.getItem(position);
        if (data instanceof RecyclePanelFrag.NumberSeat) {
            RecyclePanelFrag.NumberSeat numberSeat = (RecyclePanelFrag.NumberSeat) data;
            name.setText(String.format("Number %s", numberSeat.number));
        }
    }

    @Override
    public void init() {
        name.setText(null);
        image.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    public ImageView getImage() {
        return image;
    }

    public TextView getName() {
        return name;
    }
}
