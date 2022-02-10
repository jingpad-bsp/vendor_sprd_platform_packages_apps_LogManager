package com.sprd.logmanager.logui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.logmanager.R;

public class RadioButtonView extends RelativeLayout {
    private TextView mTitle, mSummary;
    private RadioButton mSelected;

    public RadioButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.radio_button, this);
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mSelected = (RadioButton) findViewById(R.id.radiobutton);
        setClickable(true);
        final TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.settingpreference);
        mTitle.setText(typeArray.getString(R.styleable.settingpreference_title));
        mSummary.setText(typeArray.getString(R.styleable.settingpreference_summary));
        typeArray.recycle();
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setSummary(String summary) {
        mSummary.setText(summary);
    }

    public void setChecked(boolean check) {
        mSelected.setChecked(check);
    }

    public boolean isChecked() {
        return mSelected.isChecked();
    }

    public void setEnabled(boolean enabled) {
        mSelected.setEnabled(enabled);
        if(!enabled) {
            mTitle.setTextColor(Color.LTGRAY);
            mSummary.setTextColor(Color.LTGRAY);
        } else {
            mTitle.setTextColor(Color.BLACK);
            mSummary.setTextColor(Color.GRAY);
        }
    }
}
