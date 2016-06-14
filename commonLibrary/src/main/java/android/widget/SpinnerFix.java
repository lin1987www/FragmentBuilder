package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class SpinnerFix extends Spinner {

    public SpinnerFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        boolean handled = super.performClick();
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
        return handled;
    }

    private OnClickListener mOnClickListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        // 不管是新舊版本，Spinner本身的行為都會顯示對話框，如果沒有使用setOnClickListener的話。
        // 所以策略上將所有setOnClickListener保存後，在原本的performClick()中執行就好。
        mOnClickListener = l;
    }
}
