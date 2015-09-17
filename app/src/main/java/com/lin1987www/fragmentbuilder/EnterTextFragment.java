package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class EnterTextFragment extends FragmentFix {
    public String enterName;

    EditText mEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_text, container, false);
        mEditText = (EditText) view.findViewById(R.id.editText);
        this.onDestroyView();
        return view;
    }

    @Override
    public void onDestroyView() {
        enterName = mEditText.getText().toString();
        super.onDestroyView();
    }
}
