package com.lin1987www.fragmentbuilder;

import android.os.Parcelable;
import android.support.v7.widget.RecyclerViewAdapter;
import android.support.v7.widget.ViewHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@ViewHolder.LayoutResId(id = R.layout.list_item)
public class ListItemHolder extends ViewHolder implements View.OnClickListener {
    private ImageView image;
    private TextView name;
    private Button plus2Button;

    public ListItemHolder(View view) {
        super(view);
        image = (ImageView) view.findViewById(R.id.image);
        name = (TextView) view.findViewById(R.id.name);
        plus2Button = (Button) view.findViewById(R.id.plus2Button);
        plus2Button.setOnClickListener(this);
    }

    @Override
    public void onBind(RecyclerViewAdapter adapter, int position) {
        Parcelable data = adapter.getItem(position);
        if (data instanceof RecyclePanelFrag.NumberSeat) {
            RecyclePanelFrag.NumberSeat numberSeat = (RecyclePanelFrag.NumberSeat) data;
            if (numberSeat.clickCount > 0) {
                name.setText(String.format("Number %s +%s", numberSeat.number, numberSeat.clickCount));
            } else {
                name.setText(String.format("Number %s", numberSeat.number));
            }
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

    @Override
    public void onClick(View view) {
        if (plus2Button == view) {
            Parcelable data = getItem();
            if (data instanceof RecyclePanelFrag.NumberSeat) {
                ((RecyclePanelFrag.NumberSeat) data).clickCount += 2;
                bindViewToData();
            }
        }
    }
}
