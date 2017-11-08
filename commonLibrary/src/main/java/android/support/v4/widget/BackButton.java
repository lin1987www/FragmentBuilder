package android.support.v4.widget;

import android.content.Context;
import android.support.v4.app.FragContent;
import android.support.v4.app.FragmentUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.lin1987www.common.R;


/**
 * Created by Administrator on 2016/6/8.
 */
public class BackButton extends FrameLayout {
    View backButton;

    public BackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        backButton = LayoutInflater.from(context).inflate(R.layout.back_button, this, false);
        addView(backButton);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        boolean isVisible = false;
        FragContent content = new FragContent(this);
        if (content.getSrcFragment() != null) {
            isVisible = FragmentUtils.isInBackStack(content.getSrcFragment());
        }
        backButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }
}
