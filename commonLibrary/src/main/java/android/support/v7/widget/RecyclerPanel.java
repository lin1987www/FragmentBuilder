package android.support.v7.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lin1987www.common.R;

/**
 * Created by Administrator on 2016/10/17.
 */

public class RecyclerPanel extends FrameLayout {
    private LinearLayout mLayout;
    private LinearLayout mHeader;
    private RecyclerView mRecyclerView;
    private LinearLayout mFooter;

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mLayout = (LinearLayout) layoutInflater.inflate(R.layout.recycler_panel, this, false);
        mHeader = (LinearLayout) mLayout.findViewById(R.id.header);
        mRecyclerView = (RecyclerView) mLayout.findViewById(R.id.recyclerView);
        mFooter = (LinearLayout) mLayout.findViewById(R.id.footer);
        if (isInEditMode()) {
            return;
        }
        //
        //doSameHeight();
        addView(mLayout);
    }
/*
    private void doSameHeight() {
        if (mLayout != null) {
            int height = getHeight();
            mLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
    }
*/
    @Override
    public void requestLayout() {
        super.requestLayout();
        //doSameHeight();
    }
}
