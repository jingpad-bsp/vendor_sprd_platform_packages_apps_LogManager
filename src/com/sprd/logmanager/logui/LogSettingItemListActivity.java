package com.sprd.logmanager.logui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.sprd.logmanager.R;
import com.sprd.logmanager.logcontrol.APLogControl;

public class LogSettingItemListActivity extends Activity implements
        OnClickListener {
    private static final String TAG = "LogSettingItemListActivity";

    private SettingPreferenceView mApSetting, mModemSetting,
            mConnectivitySetting, mOtherSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (TextUtils.isEmpty(getIntent().getStringExtra("name"))) {
            Log.d(TAG, "unknow error");
            return;
        }
        Log.d(TAG, "scene name:" + getIntent().getStringExtra("name"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ylog_item_list);

        initView();
    }

    private void initView() {
        mApSetting = (SettingPreferenceView) findViewById(R.id.ap_setting);
        mModemSetting = (SettingPreferenceView) findViewById(R.id.modem_setting);
        mConnectivitySetting = (SettingPreferenceView) findViewById(R.id.connectivity_setting);
        mOtherSetting = (SettingPreferenceView) findViewById(R.id.other_setting);

        mApSetting.setOnClickListener(this);
        mModemSetting.setOnClickListener(this);
        mConnectivitySetting.setOnClickListener(this);
        mOtherSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String settingType = null;
        switch (v.getId()) {
        case R.id.ap_setting:
            if(!APLogControl.getInstance().isLogStarted()) {
                Toast.makeText(this, R.string.open_ylog_tips, Toast.LENGTH_SHORT).show();
                return;
            }
            settingType = getString(R.string.ap_setting);
            break;
        case R.id.modem_setting:
            settingType = getString(R.string.modem_setting);
            break;
        case R.id.connectivity_setting:
            settingType = getString(R.string.connectivity_setting);
            break;
        case R.id.other_setting:
             if(!APLogControl.getInstance().isLogStarted()) {
                Toast.makeText(this, R.string.open_ylog_tips, Toast.LENGTH_SHORT).show();
                return;
            }
            settingType = getString(R.string.other_setting);
            break;
        default:
            break;
        }
        Intent intent = new Intent();
        intent.setClass(LogSettingItemListActivity.this, LogSettingDetailActivity.class);
        intent.putExtra("setting_type", settingType);
        startActivity(intent);
    }
}
