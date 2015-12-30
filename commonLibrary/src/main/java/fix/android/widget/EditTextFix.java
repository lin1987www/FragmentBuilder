package fix.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by lin on 2014/10/8.
 */
public class EditTextFix extends EditText {
    public EditTextFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (onKeyPreImeListener != null && onKeyPreImeListener.onKeyPreIme(keyCode, event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
        // 將手機殼上的按鈕 Home Back 轉化成 dispatchKeyEvent，本來應該處理的應該是 dispatchKeyEventPreIme
        // return super.dispatchKeyEvent(event);
    }

    private OnKeyPreImeListener onKeyPreImeListener;

    public void setOnKeyPreImeListener(OnKeyPreImeListener onKeyPreImeListener) {
        this.onKeyPreImeListener = onKeyPreImeListener;
    }

    public interface OnKeyPreImeListener {
        boolean onKeyPreIme(int keyCode, KeyEvent event);
    }
}
