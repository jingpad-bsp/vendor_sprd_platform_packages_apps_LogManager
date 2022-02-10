package com.sprd.logmanager.logui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.logmanager.R;

public class SettingPreferenceView extends RelativeLayout {
    private TextView mTitle, mSummary, mTips;
    private ImageView mSelected;
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_PREFS_SETTINGS_PREF = "settingpreference";
    private boolean mIsChecked;

    public SettingPreferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.setting_preference, this);
        mTitle = (TextView) findViewById(R.id.title);
        mTips = (TextView) findViewById(R.id.tips);
        mSummary = (TextView) findViewById(R.id.summary);
        mSelected = (ImageView) findViewById(R.id.checkbox);
        setClickable(true);
        final TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.settingpreference);
        mTitle.setText(typeArray.getString(R.styleable.settingpreference_title));
        mSummary.setText(typeArray.getString(R.styleable.settingpreference_summary));
        mTips.setText(typeArray.getString(R.styleable.settingpreference_tips));
        String summaryViable = typeArray.getString(R.styleable.settingpreference_summaryVisibility);
        String checkViable = typeArray.getString(R.styleable.settingpreference_checkVisibility);
        mSummary.setVisibility(TextUtils.isEmpty(summaryViable) || !summaryViable.equals("visiable") ? View.GONE : View.VISIBLE);
        mSelected.setVisibility(TextUtils.isEmpty(checkViable) || !checkViable.equals("visiable") ? View.GONE : View.VISIBLE);
        String showTips = typeArray.getString(R.styleable.settingpreference_tipsVisibility);
        mTips.setVisibility(TextUtils.isEmpty(showTips) || !showTips.equals("visiable") ? View.GONE : View.VISIBLE);

        if(mSelected.getVisibility() == View.VISIBLE) {
            mSharedPreferences = context.getSharedPreferences(SHARED_PREFS_SETTINGS_PREF,
                    Context.MODE_PRIVATE);
            mIsChecked = mSharedPreferences.getBoolean(mTitle.getText().toString(), typeArray.getBoolean(R.styleable.settingpreference_checked, false));
            if(mIsChecked) {
                mSelected.setImageResource(R.drawable.checkbox_on);
            } else {
                mSelected.setImageResource(R.drawable.checkbox_off);
            }
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsChecked) {
                        mSelected.setImageResource(R.drawable.checkbox_off);
                    } else {
                        mSelected.setImageResource(R.drawable.checkbox_on);
                    }
                    mIsChecked = !mIsChecked;
                    mSharedPreferences.edit().putBoolean(mTitle.getText().toString(), mIsChecked).apply();
                }
            });
        }
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public CharSequence getTitle() {
        return mTitle.getText();
    }

    public void setSummary(String summary) {
        mSummary.setText(summary);
    }

    public String getSummary() {
        return mSummary.getText().toString();
    }

    public void setSummaryVisibility(int visibility) {
        mSummary.setVisibility(visibility);
    }

    public void setTips(String tips) {
        if(mTips.getVisibility() == View.GONE) {
            mSummary.setText(tips);
        } else {
            mTips.setText(tips);
        }
    }

    public void setTipsVisibility(int visibility) {
        mTips.setVisibility(visibility);
    }

    public String getTips() {
        return mTips.getText().toString();
    }

    public void setChecked(boolean check) {
        mIsChecked = check;
        if(check) {
            mSelected.setImageResource(R.drawable.checkbox_on);
        } else {
            mSelected.setImageResource(R.drawable.checkbox_off);
        }
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled) {
            setTitleTextColor(getColor(R.color.black));
            setTipsTextColor(getColor(R.color.gray));
            setSummaryTextColor(getColor(R.color.gray));
        } else {
            setTitleTextColor(getColor(R.color.lightgray));
            setTipsTextColor(getColor(R.color.lightgray));
            setSummaryTextColor(getColor(R.color.lightgray));
        }
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setTitleTextColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setSummaryTextColor(int color) {
        mSummary.setTextColor(color);
    }

    public void setTipsTextColor(int color) {
        mTips.setTextColor(color);
    }

    private int getColor(int color) {
        return getResources().getColor(color);
    }
}
