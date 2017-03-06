package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by Administrator on 2015/6/26.
 */
public class EnterTextFragment extends FragmentFix {
    private static final String KEY_enterName = "KEY_enterName";
    public String enterName;

    EditText mEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_text, container, false);
        mEditText = (EditText) view.findViewById(R.id.editText);
        enterName = getFragmentArgs().bundle.getString(KEY_enterName);
        mEditText.setText(enterName);
        return view;
    }

    @Override
    public void onDestroyView() {
        enterName = mEditText.getText().toString();
        getFragmentArgs().bundle.putString(KEY_enterName, enterName);
        super.onDestroyView();
    }
}
