package com.lin1987www.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.app.Choice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin on 2014/11/25.
 */
public class ChoiceTextView<T> extends TextView {
    public final static String KEY_ON_SAVE_INSTANCE_STATE = "onSaveInstanceState";

    public final static String KEY_CHECKED_ITEM_ID_CSV = "checkedItemIdCsv";

    private Choice<T> choice;

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (choice != null) {
                choice.showAlertDialog(getContext());
            }
        }
    };
    private String checkedItemIdCsv;
    private DialogInterface.OnClickListener delegateAlertDialogOnClickListener;
    private DialogInterface.OnClickListener alertDialogOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            toSetText();
            checkedItemIdCsv = choice.getCheckedItemIdCsv();
            if (delegateAlertDialogOnClickListener != null) {
                delegateAlertDialogOnClickListener.onClick(dialog, which);
            }
        }
    };

    public ChoiceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Choice<T> getChoice() {
        return choice;
    }

    public ChoiceTextView<T> setAlertDialogOnClickListener(DialogInterface.OnClickListener delegateAlertDialogOnClickListener) {
        this.delegateAlertDialogOnClickListener = delegateAlertDialogOnClickListener;
        return this;
    }

    public ChoiceTextView<T> setSingleChoice(Choice<T> choice, int defaultCheckIndex) {
        this.choice = choice;
        if (choice.getCheckedItem() == null) {
            if (checkedItemIdCsv != null) {
                choice.parse(checkedItemIdCsv);
            } else {
                choice.check(defaultCheckIndex);
            }
        }
        toSetText();
        choice.setAlertDialogOnClickListener(alertDialogOnClickListener);
        setOnClickListener(onClickListener);
        return this;
    }

    private void toSetText() {
        List<T> list = choice.getCheckedItemList();
        if (list != null && list.size() > 0) {
            ArrayList<String> nameList = new ArrayList<String>();
            for (T item : list) {
                nameList.add(choice.getItemName(item));
            }
            String joinNameList = TextUtils.join(choice.getSeparator(), nameList);
            setText(joinNameList);
        } else {
            setText("(none)");
        }
    }

    @Override
    public android.os.Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ON_SAVE_INSTANCE_STATE, super.onSaveInstanceState());
        if (choice != null) {
            bundle.putString(KEY_CHECKED_ITEM_ID_CSV, choice.getCheckedItemIdCsv());
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(android.os.Parcelable state) {
        Bundle bundle = (Bundle) state;
        checkedItemIdCsv = bundle.getString(KEY_CHECKED_ITEM_ID_CSV);
        state = bundle.getParcelable(KEY_ON_SAVE_INSTANCE_STATE);
        super.onRestoreInstanceState(state);
    }
}
