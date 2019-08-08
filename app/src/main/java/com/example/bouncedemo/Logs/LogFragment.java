package com.example.bouncedemo.Logs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LogFragment extends Fragment {
    private ScrollView mScrollView;
    private LogView mLogView;

    public LogView getLogView() {
        return mLogView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // assign the views to the output
        View output = setUpViews();

        mLogView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        return output;
    }

    public LogFragment() {
    }

    public View setUpViews(){
        // initialize the scrollView
        mScrollView = new ScrollView(getActivity());
        ViewGroup.LayoutParams scrolls = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(scrolls);

        // initialize the logView
        mLogView = new LogView(getActivity());
        ViewGroup.LayoutParams logs = new ViewGroup.LayoutParams(scrolls);
        logs.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLogView.setLayoutParams(logs);
        mLogView.setClickable(true);
        mLogView.setFocusable(true);
        mLogView.setTypeface(Typeface.MONOSPACE);

        // some calculations to enable us set the padding
        int dps = 16;
        double scale = getResources().getDisplayMetrics().density;
        int pixelDimens = (int) ((dps * scale) + .5);
        mLogView.setPadding(pixelDimens, pixelDimens, pixelDimens, pixelDimens);

        mLogView.setGravity(Gravity.BOTTOM);
        mLogView.setTextAppearance(getActivity(),
                android.R.style.TextAppearance_DeviceDefault_Medium);

        mScrollView.addView(mLogView);
        return mScrollView;
    }
}
