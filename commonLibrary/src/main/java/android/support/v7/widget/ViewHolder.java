package android.support.v7.widget;

import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2016/10/17.
 */

public abstract class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind(Parcelable data);

    /**
     * Reset ViewHolder
     */
    public abstract void init();

    public void onBindViewToData(Parcelable data){
    }

    public void bindViewToData() {
        RecyclerView recyclerView = mOwnerRecyclerView;
        RecyclerViewAdapter adapter = null;
        if (recyclerView != null) {
            adapter = (RecyclerViewAdapter) recyclerView.getAdapter();
        }
        if (adapter != null) {
            int position = getAdapterPosition();
            Parcelable data = adapter.getItem(position);
            onBindViewToData(data);
            adapter.notifyItemChanged(position);
        }
    }

    /**
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface LayoutResId {
        @LayoutRes int id();
    }

    @LayoutRes
    public static int getLayoutResId(Class<? extends ViewHolder> viewHolderClass) {
        int resId = 0;
        ViewHolder.LayoutResId layoutResIdAnnotation = viewHolderClass.getAnnotation(ViewHolder.LayoutResId.class);
        if (null != layoutResIdAnnotation) {
            resId = layoutResIdAnnotation.id();
        }
        return resId;
    }

    public static <T extends ViewHolder> T create(ViewGroup parent, Class<T> viewHolderClass) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(getLayoutResId(viewHolderClass), parent, false);
        view = wrap(view);
        T viewHolder;
        try {
            Constructor constructor = viewHolderClass.getConstructor(View.class);
            viewHolder = (T) constructor.newInstance(view);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return viewHolder;
    }

    public static View wrap(View itemView) {
        View view;
        if (itemView instanceof RelativeLayout) {
            view = itemView;
        } else {
            RelativeLayout wrapper = new RelativeLayout(itemView.getContext());
            wrapper.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            wrapper.addView(itemView);
            view = wrapper;
        }
        return view;
    }
}
