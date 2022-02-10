package com.sprd.logmanager.logui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.app.ProgressDialog;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.WcnControl;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.StorageUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

public class LogSettingDetailActivity extends Activity implements
        OnClickListener {
    private static final String TAG = "LogSettingDetailActivity";
    private String mType = null;
    private SettingPreferenceView mAPFolderSize, mApTotalMaxSize,
            mApLogBugreport, mApLogOverride;
    private SettingPreferenceView mIQModeSetting, mEtbMode, mModemReboot,mModemDumpReboot,
            mModemSignalLogSize, mModemExternalSize, mModemInternalSize,
            mModemOverride;
    private SettingPreferenceView mCp2Reboot, mDumpMarlin, mCp2SignalLogSize,
            mCp2ExternalSize, mCp2InternalSize, mCp2Overrite;
    private SettingPreferenceView mApCapLength, mCpCapLength, mAudoSize,
            mSensorHubSize, mOtherLogMaxSize, mOtherLogOverride;
    private CPLogControl mCpLogControl;
    private APLogControl mApLogControl;
    private LogManagerPreference mLogManagerPreference;
    private WcnControl mWcnControl;
    private LogSettingHandler mThreadHandler;
    private AlertDialog mAlertDialog = null;
    private LayoutInflater mLayoutInflater;
    HandlerThread mWorkThread;
    /** UNISOC Bug:1289343 @{  **/
    private ProgressDialog mProgressDialog;
    /**  }@ **/


    private static final int MIN_CAP_SIZE = 1;
    private static final int MAX_CAP_SIZE = 10000;

    private static final int GET_MODEM_LOG_OVERWRITE_STATUE = 0;
    private static final int SET_MODEM_LOG_OVERWRITE = GET_MODEM_LOG_OVERWRITE_STATUE + 1;
    private static final int GET_AP_LOG_OVERWRITE_STATUE = SET_MODEM_LOG_OVERWRITE + 1;
    private static final int SET_AP_LOG_OVERWRITE = GET_AP_LOG_OVERWRITE_STATUE + 1;
    private static final int GET_ETB_STATE = SET_AP_LOG_OVERWRITE + 1;
    private static final int SET_ETB_STATE = GET_ETB_STATE + 1;
    private static final int GET_CP2_LOG_OVERWRITE_STATUE = 9;
    private static final int SET_CP2_LOG_OVERWRITE = 10;

    private static final int SET_AP_FOLDER_SIZE = 12;
    private static final int SET_AP_TOTAL_SIZE = 13;
    private static final int SET_AP_CAP_SIZE = 14;
    private static final int SET_CP_SINGLE_FILE_SIZE = 15;
    private static final int SET_CP_TOTAL_INTERNAL_SIZE = 16;
    private static final int SET_CP_TOTAL_EXTERNAL_SIZE = 17;
    private static final int SET_CP_CAP_SIZE = 18;
    private static final int SET_CP2_SINGLE_FILE_SIZE = 19;
    private static final int SET_CP2_TOTAL_INTERNAL_SIZE = 20;
    private static final int SET_CP2_TOTAL_EXTERNAL_SIZE = 21;
    private static final int SET_WCN_DUMP_STATUS = 22;

    private static final int GET_AP_FOLDER_SIZE = 23;
    private static final int GET_AP_TOTAL_SIZE = 24;
    private static final int GET_AP_LOG_LEVEL = 25;
    private static final int GET_AP_CAP_SIZE = 26;
    private static final int SET_AP_LOG_LEVEL = 27;

    private static final int GET_PS_LOG_LEVEL = 30;
    private static final int GET_CP_SINGLE_FILE_SIZE = 31;
    private static final int GET_CP_EXTERNAL_SIZE = 32;
    private static final int GET_CP_INTERNAL_FILE_SIZE = 33;
    private static final int GET_CP_CAP_SIZE = 34;

    private static final int GET_CONNECTIVITY_SINGLE_FILE_SIZE = 40;
    private static final int GET_CONNECTIVITY_EXTERNAL_SIZE = 41;
    private static final int GET_CONNECTIVITY_INTERNAL_FILE_SIZE = 42;

    private int[] mApLogLevelViewsId = new int[] { R.id.aplog_level_0,
            R.id.aplog_level_1, R.id.aplog_level_2, R.id.aplog_level_3,
            R.id.aplog_level_4 /* , R.id.aplog_level_5 */
    };
    private int[] mPsLogLevelViewsId = new int[] { R.id.ps_log_level_0,
            R.id.ps_log_level_1, R.id.ps_log_level_2, R.id.ps_log_level_3 };
    private int[] mConnectivityLogLevelViewsId = new int[] {
            R.id.cp2_log_level_0, R.id.cp2_log_level_1, R.id.cp2_log_level_2,
            R.id.cp2_log_level_3, R.id.cp2_log_level_4 };
    private static final int MIN_AP_SIZE = 100;
    private static final int MAX_AP_SIZE = 500;
    private static final int MIN_AP_TOTAL_SIZE = 256;
    private static final int MIN_MODEM_SINGLE_SIZE = 200;
    private static final int MAX_MODEM_SINGLE_SIZE = 512;
    private static final int MIN_MODEM_DATA_SIZE = 50;
    private static final int MAX_MODEM_DATA_SIZE = 100;
    private static final int MIN_MODEM_TOTAL_SIZE = 600;
    private static final int MIN_WCN_SINGLE_SIZE = 50;
    private static final int MAX_WCN_SINGLE_SIZE = 128;
    private static final int MIN_WCN_DATA_SIZE = 50;
    private static final int MAX_WCN_DATA_SIZE = 100;
    private static final int MIN_WCN_TOTAL_SIZE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getIntent().getStringExtra("setting_type");
        if (TextUtils.isEmpty(mType)) {
            Log.d(TAG, "unknow error");
            finish();
            return;
        }
        mWorkThread = new HandlerThread(TAG);
        mWorkThread.start();
        mLogManagerPreference = LogManagerPreference.getInstanse();
        mCpLogControl = CPLogControl.getInstance();
        mApLogControl = APLogControl.getInstance();
        mWcnControl = WcnControl.getInstance();
        mThreadHandler = new LogSettingHandler(mWorkThread.getLooper());
        Log.d(TAG, "setting type:" + mType);
        mLayoutInflater = LayoutInflater.from(this);
        if (mType.equals(getString(R.string.ap_setting))) {
            setContentView(R.layout.ylog_ap_log_setting);
            initAPLogSettingView();
        } else if (mType.equals(getString(R.string.modem_setting))) {
            setContentView(R.layout.ylog_modem_log_setting);
            initModemLogSettingView();
        } else if (mType.equals(getString(R.string.connectivity_setting))) {
            setContentView(R.layout.ylog_connectivity_log_setting);
            initConnectivityLogSettingView();
        } else if (mType.equals(getString(R.string.other_setting))) {
            setContentView(R.layout.ylog_other_log_setting);
            initOtherLogSettingView();
        }
        TextView title = (TextView) findViewById(R.id.setting_title);
        title.setText(getIntent().getStringExtra("setting_type"));
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mWorkThread != null) {
            mWorkThread.quit();
        }
    }

    private void initAPLogSettingView() {
        for (int i = 0; i < mApLogLevelViewsId.length; i++) {
            ((RadioButtonView) findViewById(mApLogLevelViewsId[i]))
                    .setOnClickListener(this);
        }
        mAPFolderSize = (SettingPreferenceView) findViewById(R.id.ap_max_size);
        mApTotalMaxSize = (SettingPreferenceView) findViewById(R.id.ap_total_max_size);
        mApLogBugreport = (SettingPreferenceView) findViewById(R.id.ap_log_bugreport_enable);
        mApLogOverride = (SettingPreferenceView) findViewById(R.id.ap_log_override);

        mAPFolderSize.setOnClickListener(this);
        mApTotalMaxSize.setOnClickListener(this);
        mApLogOverride.setOnClickListener(this);
        mApLogBugreport.setOnClickListener(this);
        mApLogBugreport.setChecked(mLogManagerPreference.getBugreportEnable());
        getApLogOverride();
        getApFoldSizeSummary();
        getApSizeSummary();
        getApLogLevel();
    }

    private long getTotalMem() {
        long mTotal=0;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
      if (content != null) {
        int begin = content.indexOf(':');
        int end = content.indexOf('k');
        content = content.substring(begin + 1, end).trim();
        mTotal = Integer.parseInt(content);
      }
        return mTotal;
    }

    private void initModemLogSettingView() {
        for (int i = 0; i < mPsLogLevelViewsId.length; i++) {
            ((RadioButtonView) findViewById(mPsLogLevelViewsId[i]))
                    .setOnClickListener(this);
        }
        mIQModeSetting = (SettingPreferenceView) findViewById(R.id.iq_mode_setting);
        mEtbMode = (SettingPreferenceView) findViewById(R.id.etb_modle);
        mModemReboot = (SettingPreferenceView) findViewById(R.id.modem_reboot);
        mModemDumpReboot = (SettingPreferenceView) findViewById(R.id.modem_dumpreboot);
        mModemSignalLogSize = (SettingPreferenceView) findViewById(R.id.modem_single_log_size);
        mModemInternalSize = (SettingPreferenceView) findViewById(R.id.modem_internal_log_size);
        mModemExternalSize = (SettingPreferenceView) findViewById(R.id.modem_external_log_size);
        mModemOverride = (SettingPreferenceView) findViewById(R.id.modem_log_override);

        mIQModeSetting.setOnClickListener(this);
        long totalMem = getTotalMem();
        Log.d(TAG, "totalMem is :" + totalMem);
        if (totalMem < 512 * 1024) {
            mIQModeSetting.setEnabled(false);
        }
        mEtbMode.setOnClickListener(this);
        mModemReboot.setOnClickListener(this);
        mModemDumpReboot.setOnClickListener(this);

        mModemSignalLogSize.setOnClickListener(this);
        mModemInternalSize.setOnClickListener(this);
        mModemExternalSize.setOnClickListener(this);
        mModemOverride.setOnClickListener(this);
        mModemReboot.setChecked(mCpLogControl.isModemReset());
        mModemDumpReboot.setChecked(mCpLogControl.isModemSaveDump());
        getPsLogLevel();
        getModemLogOverwrite();
        getEtbStatus();
        getCpSingleFileSize();
        getCpExternalSize();
        getCpInternalSize();
    }

    private void getModemLogOverwrite() {
        mThreadHandler.sendEmptyMessage(GET_MODEM_LOG_OVERWRITE_STATUE);
    }

    private void getEtbStatus() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                int result = mCpLogControl.getETBMode();
                if (result == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setChecked(true);
                        }
                    });
                } else if (result == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setChecked(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setEnabled(false);
                            mEtbMode.setBackgroundColor(Color.GRAY);
                            mEtbMode.setTitleTextColor(Color.GRAY);
                        }
                    });
                }
            }
        }).start();
    }

    private void getCpSingleFileSize() {
        mThreadHandler.sendEmptyMessage(GET_CP_SINGLE_FILE_SIZE);
    }

    private void getCpInternalSize() {
        mThreadHandler.sendEmptyMessage(GET_CP_INTERNAL_FILE_SIZE);

    }

    private void getCpExternalSize() {
        mThreadHandler.sendEmptyMessage(GET_CP_EXTERNAL_SIZE);

    }

    private void initConnectivityLogSettingView() {
        mCp2Reboot = (SettingPreferenceView) findViewById(R.id.cp2_reboot);
        mDumpMarlin = (SettingPreferenceView) findViewById(R.id.dump_marlin);
        mCp2SignalLogSize = (SettingPreferenceView) findViewById(R.id.cp2_single_log_size);
        mCp2InternalSize = (SettingPreferenceView) findViewById(R.id.cp2_interal_log_size);
        mCp2ExternalSize = (SettingPreferenceView) findViewById(R.id.cp2_external_log_size);
        mCp2Overrite = (SettingPreferenceView) findViewById(R.id.cp2_log_override);
        // current not support
        for (int i = 0; i < mConnectivityLogLevelViewsId.length; i++) {
            ((RadioButtonView) findViewById(mConnectivityLogLevelViewsId[i]))
                    .setOnClickListener(this);
        }

        if (mCp2Overrite != null) {
            mThreadHandler.sendEmptyMessage(GET_CP2_LOG_OVERWRITE_STATUE);
        }

        mCp2Reboot.setOnClickListener(this);
        mDumpMarlin.setOnClickListener(this);
        mCp2SignalLogSize.setOnClickListener(this);
        mCp2InternalSize.setOnClickListener(this);
        mCp2ExternalSize.setOnClickListener(this);
        mCp2Overrite.setOnClickListener(this);

        mCp2Reboot.setChecked(mWcnControl.isWcnReset());
        //getMarlinStatus();
        getWcnSingleFileSize();
        getWcnInternalSize();
        getWcnExternalSize();
        if (!CommonUtils.isUserBuild()) {
            mCp2Reboot.setSummaryVisibility(View.VISIBLE);
            mCp2Reboot.setSummary(getString(R.string.feature_not_support));
            mCp2Reboot.setEnabled(false);
        }
        getWCNLogLevel();
    }

    private void getWcnSingleFileSize() {
        mThreadHandler.sendEmptyMessage(GET_CONNECTIVITY_SINGLE_FILE_SIZE);
    }

    private void getWcnInternalSize() {
        mThreadHandler.sendEmptyMessage(GET_CONNECTIVITY_INTERNAL_FILE_SIZE);
    }

    private void getWcnExternalSize() {
        mThreadHandler.sendEmptyMessage(GET_CONNECTIVITY_EXTERNAL_SIZE);
    }

    private void initOtherLogSettingView() {
        mApCapLength = (SettingPreferenceView) findViewById(R.id.ap_cap_log_packet_length);
        mCpCapLength = (SettingPreferenceView) findViewById(R.id.modem_cap_log_packet_length);
        mAudoSize = (SettingPreferenceView) findViewById(R.id.audio_log_max_size);
        mSensorHubSize = (SettingPreferenceView) findViewById(R.id.sensor_hub_max_size);
        mOtherLogMaxSize = (SettingPreferenceView) findViewById(R.id.other_total_max_size);
        mOtherLogOverride = (SettingPreferenceView) findViewById(R.id.other_log_override);

        mApCapLength.setOnClickListener(this);
        mCpCapLength.setOnClickListener(this);
        mAudoSize.setOnClickListener(this);
        mSensorHubSize.setOnClickListener(this);
        mOtherLogMaxSize.setOnClickListener(this);
        mOtherLogOverride.setOnClickListener(this);

        mAudoSize.setEnabled(false);
        mSensorHubSize.setEnabled(false);
        mOtherLogMaxSize.setEnabled(false);
        mOtherLogOverride.setEnabled(false);

        getApCapSize();
        getCPCapLength();
    }

    private void getApCapSize() {
        mThreadHandler.sendEmptyMessage(GET_AP_CAP_SIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getMarlinStatus() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final boolean enable = mWcnControl.getMarlinStatus();
                Log.d(TAG, "getMarlinStatus :" + enable);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDumpMarlin.setChecked(enable);
                    }
                });
            }
        });
    }

    private void getCPCapLength() {
        mThreadHandler.sendEmptyMessage(GET_CP_CAP_SIZE);
    }

    private void getApFoldSizeSummary() {
        mThreadHandler.sendEmptyMessage(GET_AP_FOLDER_SIZE);
    }

    private void getApSizeSummary() {
        mThreadHandler.sendEmptyMessage(GET_AP_TOTAL_SIZE);
    }

    private void commitPsLogLevel(final int level) {
        new Thread(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                boolean result = mCpLogControl.setLogLevel(level);
                Log.d(TAG, "commitLogLevel result:" + result);
                if (result) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setRadioButtonChecked(level, mPsLogLevelViewsId);
                        }
                    });
                }
            }
        }).start();
    }

    private void getPsLogLevel() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final int psLogLevel = mCpLogControl.getLogLevel();
                Log.d(TAG, "get cp log level result:" + psLogLevel);
                if (psLogLevel >= 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            int index = psLogLevel;
                            if (index > 3){
                                index = 3;
                            }
                            setRadioButtonChecked(index,
                                    mPsLogLevelViewsId);
                        }
                    });
                }

            }
        }).start();
    }

    private void commitApLogLevel(final int level) {
        Message message = mThreadHandler.obtainMessage();
        message.what = SET_AP_LOG_LEVEL;
        message.obj = level;
        mThreadHandler.sendMessage(message);
    }

    private void getWCNLogLevel() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                final int logLevel = mWcnControl.getLogLevel();
                Log.d(TAG, "getWCNLogLevel result:" + logLevel);
                if (logLevel >= 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setRadioButtonChecked(logLevel,
                                    mConnectivityLogLevelViewsId);
                        }
                    });
                }
            }
        });
    }
    /** UNISOC Bug:1289343 @{  **/
    private void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = ProgressDialog.show(LogSettingDetailActivity.this, getResources()
                                .getString(R.string.scene_switching),
                        getResources().getString(R.string.scene_switching_wait), true, false);
            }
        });
    }

    private void dismissProgressDialog(final ProgressDialog progressDialog) {
        try {
            if ((progressDialog != null) && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            Log.w(TAG, "exception when dismissing dialog");
        } catch (final Exception e) {
            Log.w(TAG, "exception when dismissing dialog." + e);
        }
    }

    private void dismissProgressDialog() {
        Log.d(TAG, "dismissProgressDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog(mProgressDialog);
                mProgressDialog = null;
            }
        });
    }
    /**  }@ **/

    private synchronized void commitWCNLogLevel(final int level) {
        new Thread(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                /** UNISOC Bug:1289343 @{  **/
                showProgressDialog();
                boolean result = mWcnControl.setLogLevel(level);
                Log.d(TAG, "commitWCNLogLevel result:" + result);
                if (result) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setRadioButtonChecked(level,
                                    mConnectivityLogLevelViewsId);
                        }
                    });
                }
                dismissProgressDialog();
                /**  }@ **/
            }
        }).start();
    }

    private void getApLogOverride() {
        mThreadHandler.sendEmptyMessage(GET_AP_LOG_OVERWRITE_STATUE);
    }

    private void getApLogLevel() {
        mThreadHandler.sendEmptyMessage(GET_AP_LOG_LEVEL);
    }

    private void setRadioButtonChecked(int index, int[] viewsId) {
        for (int i = 0; i < viewsId.length; i++) {
            ((RadioButtonView) findViewById(viewsId[i])).setChecked(false);
        }
        if (index < 0) {
            ((RadioButtonView) findViewById(viewsId[viewsId.length - 1]))
                    .setChecked(true);
        } else {
            ((RadioButtonView) findViewById(viewsId[index])).setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        boolean isUser = CommonUtils.isUserBuild();
        String defualtSize = "";
        switch (v.getId()) {
        /*
         * case R.id.aplog_level_0: commitLogLevel(5); break;
         */
        case R.id.aplog_level_0:
            commitApLogLevel(4);
            break;
        case R.id.aplog_level_1:
            commitApLogLevel(3);
            break;
        case R.id.aplog_level_2:
            commitApLogLevel(2);
            break;
        case R.id.aplog_level_3:
            commitApLogLevel(1);
            break;
        case R.id.aplog_level_4:
            commitApLogLevel(0);
            break;
        case R.id.ps_log_level_0:
            commitPsLogLevel(0);
            break;
        case R.id.ps_log_level_1:
            commitPsLogLevel(1);
            break;
        case R.id.ps_log_level_2:
            commitPsLogLevel(2);
            break;
        case R.id.ps_log_level_3:
            commitPsLogLevel(3);
            break;
        case R.id.ap_max_size:
            String tip = getResources().getString(R.string.ap_size_dialog_tips);
            defualtSize = mAPFolderSize.getTips();
            showSetSizeDialg(defualtSize, mAPFolderSize, SET_AP_FOLDER_SIZE,
                    tip, MIN_AP_SIZE, MAX_AP_SIZE);
            break;
        case R.id.ap_total_max_size:
            String apSizeTip = getResources().getString(
                    R.string.ap_total_size_dialog_tips);
            defualtSize = mApTotalMaxSize.getTips();
            showSetSizeDialg(defualtSize, mApTotalMaxSize, SET_AP_TOTAL_SIZE,
                    apSizeTip, MIN_AP_TOTAL_SIZE, getApTotalMaxSize());
            break;
        case R.id.ap_log_override:
            boolean overwrite = !mApLogOverride.isChecked();
            Message message = mThreadHandler.obtainMessage();
            message.what = SET_AP_LOG_OVERWRITE;
            message.obj = overwrite;
            mThreadHandler.sendMessage(message);
            break;
        case R.id.iq_mode_setting:
            setIQMode();
            break;
        case R.id.ap_log_bugreport_enable:
            boolean enable = mApLogBugreport.isChecked();
            LogManagerPreference.getInstanse().setBugreportEnable(!enable);
            mApLogBugreport.setChecked(!enable);
            break;
        case R.id.etb_modle:
            boolean enableEtb = !mEtbMode.isChecked();
            Message setEtbState = mThreadHandler.obtainMessage();
            setEtbState.what = SET_ETB_STATE;
            setEtbState.obj = enableEtb;
            mThreadHandler.sendMessage(setEtbState);
            break;
        case R.id.modem_reboot:
            mCpLogControl
                    .setModemReset(mModemReboot.isChecked() ? false : true);
            mModemReboot.setChecked(!mModemReboot.isChecked());
            break;
        case R.id.modem_dumpreboot:
           mCpLogControl
                         .setModemSaveDump(mModemDumpReboot.isChecked() ? false : true);
           mModemDumpReboot.setChecked(!mModemDumpReboot.isChecked());
            break;
        case R.id.modem_single_log_size:
            String singleLogtitle = getResources().getString(
                    R.string.single_log_size_tips);
            defualtSize = mModemSignalLogSize.getSummary();
            showSetSizeDialg(defualtSize, mModemSignalLogSize,
                    SET_CP_SINGLE_FILE_SIZE, singleLogtitle,
                    MIN_MODEM_SINGLE_SIZE, MAX_MODEM_SINGLE_SIZE);
            break;
        case R.id.modem_internal_log_size:
            String internalLogTitle = getResources().getString(
                    R.string.internal_log_size_tips);
            defualtSize = mModemInternalSize.getSummary();
             int intersize= (int) (StorageUtil.getStorageTotalSize(false) / 1024 / 1024);
            showSetSizeDialg(defualtSize, mModemInternalSize,
                    SET_CP_TOTAL_INTERNAL_SIZE, internalLogTitle,
                    isUser ? MIN_MODEM_DATA_SIZE : 1024,
                    intersize);
            break;
        case R.id.modem_external_log_size:
            String externalLogTitle = getResources().getString(
                    R.string.external_log_size_tips);
            String modemSize=mModemExternalSize.getSummary();
            externalLogTitle=externalLogTitle.replace("8192",modemSize);
            defualtSize = mModemExternalSize.getSummary();
            showSetSizeDialg(defualtSize, mModemExternalSize,
                    SET_CP_TOTAL_EXTERNAL_SIZE, externalLogTitle,
                    MIN_MODEM_TOTAL_SIZE, getApTotalMaxSize());
            break;
        case R.id.modem_log_override:
            boolean modemOverwrite = !mModemOverride.isChecked();
            Message overwriteMsg = mThreadHandler.obtainMessage();
            overwriteMsg.what = SET_MODEM_LOG_OVERWRITE;
            overwriteMsg.obj = modemOverwrite;
            mThreadHandler.sendMessage(overwriteMsg);
            break;
        case R.id.ap_cap_log_packet_length:
            String apCapTile = getResources().getString(
                    R.string.ap_cap_log_packet_length)
                    + " "
                    + this.getResources().getString(
                            R.string.cap_log_length_warning);
            defualtSize = mApCapLength.getSummary();
            showSetSizeDialgEx(defualtSize, mApCapLength, SET_AP_CAP_SIZE,
                    apCapTile, MIN_CAP_SIZE, MAX_CAP_SIZE,R.string.cap_size_unit);
            break;
        case R.id.modem_cap_log_packet_length:
            String cpCapTile = getResources().getString(
                    R.string.modem_cap_log_packet_length)
                    + " "
                    + this.getResources().getString(
                            R.string.cap_log_length_warning);
            defualtSize = mCpCapLength.getSummary();
            showSetSizeDialgEx(defualtSize, mCpCapLength, SET_CP_CAP_SIZE,
                    cpCapTile, MIN_CAP_SIZE, MAX_CAP_SIZE,R.string.cap_size_unit);
            break;
        case R.id.audio_log_max_size:
            Toast.makeText(this, "current not support", Toast.LENGTH_SHORT)
                    .show();
            break;
        case R.id.sensor_hub_max_size:
            Toast.makeText(this, "current not support", Toast.LENGTH_SHORT)
                    .show();
            break;
        case R.id.other_total_max_size:
            Toast.makeText(this, "current not support", Toast.LENGTH_SHORT)
                    .show();
            break;
        case R.id.other_log_override:
            Toast.makeText(this, "current not support", Toast.LENGTH_SHORT)
                    .show();
            break;
        case R.id.cp2_reboot:
            boolean reset = !mCp2Reboot.isChecked();
            mWcnControl.setWcnReset(reset);
            mCp2Reboot.setChecked(reset);
            break;
        case R.id.dump_marlin:
            boolean enableMarlin = !mDumpMarlin.isChecked();
            Message marlinMessage = mThreadHandler.obtainMessage();
            marlinMessage.what = SET_WCN_DUMP_STATUS;
            marlinMessage.obj = enableMarlin;
            break;
        case R.id.cp2_single_log_size:
            String wcnSingleLogTitle = getResources().getString(
                    R.string.cp2_single_log_size_tips);
            defualtSize = mCp2SignalLogSize.getSummary();
            showSetSizeDialg(defualtSize, mCp2SignalLogSize,
                    SET_CP2_SINGLE_FILE_SIZE, wcnSingleLogTitle,
                    MIN_WCN_SINGLE_SIZE, MAX_WCN_SINGLE_SIZE);
            break;
        case R.id.cp2_interal_log_size:
            String wcnInteralLogTitle = getResources().getString(
                    R.string.cp2_internal_log_size_tips);
            defualtSize = mCp2InternalSize.getSummary();
            int intersize1 = (int) (StorageUtil.getStorageTotalSize(false) / 1024 / 1024);
            showSetSizeDialg(defualtSize, mCp2InternalSize,
                    SET_CP2_TOTAL_INTERNAL_SIZE, wcnInteralLogTitle,
                    isUser ? MIN_WCN_DATA_SIZE : 256,
                    intersize1);
            break;
        case R.id.cp2_external_log_size:
            String wcnExteralLogTitle = getResources().getString(
                    R.string.cp2_external_log_size_tips);
            defualtSize = mCp2ExternalSize.getSummary();
            showSetSizeDialg(defualtSize, mCp2ExternalSize,
                    SET_CP2_TOTAL_EXTERNAL_SIZE, wcnExteralLogTitle,
                    MIN_WCN_TOTAL_SIZE, getWcnTotalMaxSize());
            break;
        case R.id.cp2_log_override:
            boolean cp2Overwrite = !mCp2Overrite.isChecked();
            Message cp2OverwriteMsg = mThreadHandler.obtainMessage();
            cp2OverwriteMsg.what = SET_CP2_LOG_OVERWRITE;
            cp2OverwriteMsg.obj = cp2Overwrite;
            mThreadHandler.sendMessage(cp2OverwriteMsg);
            break;
        case R.id.cp2_log_level_0:
            commitWCNLogLevel(0);
            break;
        case R.id.cp2_log_level_1:
            commitWCNLogLevel(1);
            break;
        case R.id.cp2_log_level_2:
            commitWCNLogLevel(2);
            break;
        case R.id.cp2_log_level_3:
            commitWCNLogLevel(3);
            break;
        case R.id.cp2_log_level_4:
            commitWCNLogLevel(4);
            break;
        }
    }

    private int getApTotalMaxSize() {
       return (int) (StorageUtil.getStorageTotalSize(StorageUtil
                .getExternalStorageState()) / 1024 / 1024 );
    }

    private int getWcnTotalMaxSize() {
        return (int)( ((double)(StorageUtil.getStorageTotalSize(StorageUtil
                .getExternalStorageState()) / 1024 / 10204)) * 0.2);
    }

    private int getTotalSize() {
        return (int) (StorageUtil.getStorageTotalSize(StorageUtil
                .getExternalStorageState()) / 1024 / 1024);
    }

    private void showSetSizeDialg(String defaultSize,
            final SettingPreferenceView view, final int msg, String string,
            final int minSize, final int maxSize){
         showSetSizeDialgEx( defaultSize,view,   msg,  string,minSize,  maxSize, -1);
    }

    private void showSetSizeDialgEx(String defaultSize,
            final SettingPreferenceView view, final int msg, String string,
            final int minSize, final int maxSize,int editHint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                LogSettingDetailActivity.this);
        View dialogView = mLayoutInflater.inflate(R.layout.ylog_input_dialog,
                null);
        ((TextView) dialogView.findViewById(R.id.dialog_textview))
                .setText(string);
        final EditText editText = (EditText) dialogView
                .findViewById(R.id.dialog_edittext);
        if (-1!=editHint) {
            editText.setHint(getResources().getString(editHint));
        }

        try {
            if (defaultSize.contains(".")) {
                editText.setText(defaultSize.substring(0,
                        defaultSize.indexOf(".")));
            } else if (defaultSize.contains("M")) {
                editText.setText(defaultSize.substring(0,
                        defaultSize.indexOf("M")));
            } else {
                int size = Integer.parseInt(defaultSize);
                editText.setText(size + "");
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "set defualt value to edit text error ", e);
        }

        builder.setTitle(view.getTitle().toString())
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                final String setSize = (String) editText
                                        .getText().toString();
                                Log.d(TAG, "Set Log size value: " + setSize);
                                if (TextUtils.isEmpty(setSize)) {
                                    Toast.makeText(
                                            LogSettingDetailActivity.this,
                                            "Input cannot be null , please check!",
                                            Toast.LENGTH_SHORT).show();
                                } else if ((msg == SET_CP_TOTAL_EXTERNAL_SIZE) && Integer.parseInt(setSize) < minSize && Integer.parseInt(setSize) != 0
                                        || Integer.parseInt(setSize) > maxSize ) {
                                    Toast.makeText(
                                            LogSettingDetailActivity.this,
                                            "Input must be 0 or between " + minSize
                                                    + " and " + maxSize
                                                    + " , please check!",
                                            Toast.LENGTH_SHORT).show();
                                } else if(msg != SET_CP_TOTAL_EXTERNAL_SIZE && Integer.parseInt(setSize) < minSize
                                        || Integer.parseInt(setSize) > maxSize ) {
                                    Toast.makeText(
                                            LogSettingDetailActivity.this,
                                            "Input must be between " + minSize
                                                    + " and " + maxSize
                                                    + " , please check!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Message message = mThreadHandler
                                            .obtainMessage();
                                    message.what = msg;
                                    message.obj = setSize;
                                    mThreadHandler.sendMessage(message);
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, null);
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }

    private void setIQMode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                LogSettingDetailActivity.this);
        builder.setTitle("IQ Mode")
                .setItems(new String[] { "WCDMA IQ", "GSM IQ" },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                Intent iq = null;
                                switch (which) {
                                case 0:
                                    iq = new Intent(
                                            getApplication(),
                                            com.sprd.logmanager.logui.WCDMAIQActivity.class);
                                    break;
                                case 1:
                                    iq = new Intent(
                                            getApplication(),
                                            com.sprd.logmanager.logui.GSMIQActivity.class);
                                    break;
                                }
                                if(null!=iq){
                                    startActivity(iq);
                                }
                            }
                        })
                .setNegativeButton(this.getString(R.string.redirection_close),
                        null);
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }

    class LogSettingHandler extends Handler {
        public LogSettingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_ETB_STATE:
                int result = mCpLogControl.getETBMode();
                if (result == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setChecked(true);
                        }
                    });
                } else if (result == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setChecked(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEtbMode.setEnabled(false);
                            mEtbMode.setBackgroundColor(Color.GRAY);
                            mEtbMode.setTitleTextColor(Color.GRAY);
                        }
                    });
                }
                break;
            case SET_ETB_STATE:
                final boolean enableEtb = (Boolean) msg.obj;
                mCpLogControl.enableETBMode(enableEtb);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEtbMode.setChecked(enableEtb);
                    }
                });
                break;
            case GET_MODEM_LOG_OVERWRITE_STATUE:
                final boolean overwrite = mCpLogControl.isModemLogOverwrite();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mModemOverride.setChecked(overwrite);
                    }
                });
                break;

            case GET_CP2_LOG_OVERWRITE_STATUE:
                final boolean cp2Overwrite = mCpLogControl.isWcnLogOverwrite();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCp2Overrite.setChecked(cp2Overwrite);
                    }
                });
                break;
            case SET_CP2_LOG_OVERWRITE:
                final boolean cp2OverwriteEnable = (Boolean) msg.obj;
                mCpLogControl.enableGnssLogOverwrite(cp2OverwriteEnable);
                mCpLogControl.enableWcnLogOverwrite(cp2OverwriteEnable);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCp2Overrite.setChecked(cp2OverwriteEnable);
                    }
                });
                break;
            case SET_AP_LOG_OVERWRITE:
                final boolean enable = (Boolean) msg.obj;
                mApLogControl.enableLogOverwrite(enable);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mApLogOverride.setChecked(enable);
                    }
                });
                break;
            case SET_AP_FOLDER_SIZE:
                final String apFolderSizeString = (String) msg.obj;
                long apFoldersize = Long.parseLong(apFolderSizeString);
                mApLogControl.setSingleFolderSize(apFoldersize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mAPFolderSize.setTips(apFolderSizeString);
                    }
                });
                break;
            case SET_AP_TOTAL_SIZE:
                final String apTotalSizeString = (String) msg.obj;
                long apTotalSize = Long.parseLong(apTotalSizeString);
                mApLogControl.setApTotalSize(apTotalSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mApTotalMaxSize.setTips(apTotalSizeString);
                    }
                });
                break;
            case SET_AP_CAP_SIZE:
                final String apCapSizeString = (String) msg.obj;
                long apCapSize = Long.parseLong(apCapSizeString);
                mApLogControl.setCapSize(apCapSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mApCapLength.setSummary(apCapSizeString);
                    }
                });
                break;
            case SET_CP_SINGLE_FILE_SIZE:
                final String cpSingleFileString = (String) msg.obj;
                long cpSingleFileSize = Long.parseLong(cpSingleFileString);
                mCpLogControl.setSingleFileSize(
                        CPLogControl.SUB_LOG_5MODE_TYPE, cpSingleFileSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mModemSignalLogSize.setSummary(cpSingleFileString);
                    }
                });
                break;
            case SET_CP_CAP_SIZE:
                final String cpCapString = (String) msg.obj;
                long cpCapSize = Long.parseLong(cpCapString);
                mCpLogControl.setCapSize(cpCapSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mCpCapLength.setSummary(cpCapString);
                    }
                });
                break;
            case SET_CP_TOTAL_EXTERNAL_SIZE:
                final String cpTotalExternalString = (String) msg.obj;
                long cpTotalExternal = Long.parseLong(cpTotalExternalString);
                mCpLogControl
                        .setTotalCpLogSize(CPLogControl.SUB_LOG_5MODE_TYPE,
                                false, cpTotalExternal);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mModemExternalSize.setSummary(cpTotalExternalString);
                    }
                });
                break;
            case SET_CP_TOTAL_INTERNAL_SIZE:
                final String cpTotalInternalString = (String) msg.obj;
                long cpTotalInternalSize = Long
                        .parseLong(cpTotalInternalString);
                mCpLogControl.setTotalCpLogSize(
                        CPLogControl.SUB_LOG_5MODE_TYPE, true,
                        cpTotalInternalSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mModemInternalSize.setSummary(cpTotalInternalString);
                    }
                });
                break;
            case SET_CP2_SINGLE_FILE_SIZE:
                final String cp2SingleSizeString = (String) msg.obj;
                long cp2Singlesize = Long.parseLong(cp2SingleSizeString);
                mCpLogControl.setSingleFileSize(CPLogControl.SUB_LOG_WCN_TYPE,
                        cp2Singlesize);
                mCpLogControl.setSingleFileSize(CPLogControl.SUB_LOG_GNSS_TYPE,
                        cp2Singlesize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mCp2SignalLogSize.setSummary(cp2SingleSizeString);
                    }
                });
                break;
            case SET_CP2_TOTAL_INTERNAL_SIZE:
                final String cp2TotalInternalString = (String) msg.obj;
                long cp2TotalInternalSize = Long
                        .parseLong(cp2TotalInternalString);
                mCpLogControl.setTotalCpLogSize(CPLogControl.SUB_LOG_WCN_TYPE,
                        true, cp2TotalInternalSize);
                mCpLogControl.setTotalCpLogSize(CPLogControl.SUB_LOG_GNSS_TYPE,
                        true, cp2TotalInternalSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mCp2InternalSize.setSummary(cp2TotalInternalString);
                    }
                });
                break;
            case SET_CP2_TOTAL_EXTERNAL_SIZE:
                final String cp2TotalExternalString = (String) msg.obj;
                long cp2TotalExternalSize = Long
                        .parseLong(cp2TotalExternalString);
                mCpLogControl.setTotalCpLogSize(CPLogControl.SUB_LOG_WCN_TYPE,
                        false, cp2TotalExternalSize);
                mCpLogControl.setTotalCpLogSize(CPLogControl.SUB_LOG_GNSS_TYPE,
                        false, cp2TotalExternalSize);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mCp2ExternalSize.setSummary(cp2TotalExternalString);
                    }
                });
                break;
            case SET_WCN_DUMP_STATUS:
                final boolean enableDump = (Boolean) msg.obj;
                mWcnControl.enableMarlinDump(enableDump);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mDumpMarlin.setChecked(enableDump);
                    }
                });
                break;
            case SET_MODEM_LOG_OVERWRITE:
                final boolean modemOverwrite = (Boolean) msg.obj;
                mCpLogControl.enableModemLogOverwrite(modemOverwrite);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mModemOverride.setChecked(modemOverwrite);
                    }
                });
                break;
            case GET_AP_LOG_OVERWRITE_STATUE:
                final boolean apLogOverwrite = mApLogControl.isLogOverwrite();
                Log.d(TAG, "isLogOverwrite result:" + apLogOverwrite);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mApLogOverride.setChecked(apLogOverwrite);
                    }
                });
                break;
            case GET_AP_FOLDER_SIZE:
                final long singleFolderSize = mApLogControl
                        .getSingleFolderSize();
                Log.d(TAG, "ap singleFolderSize is : " + singleFolderSize);
                if (singleFolderSize > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAPFolderSize.setTips("" + singleFolderSize);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAPFolderSize.setTips("product not support");
                            mAPFolderSize.setEnabled(false);
                        }
                    });
                }
                break;
            case GET_AP_TOTAL_SIZE:
                final long apTotal = mApLogControl.getApTotalSize();
                Log.d(TAG, "getApTotalSize : " + apTotal);
                if (apTotal > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mApTotalMaxSize.setTips(apTotal + "");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mApTotalMaxSize.setTips("product not support");
                            mApTotalMaxSize.setEnabled(false);
                        }
                    });
                }
                break;
            case GET_AP_LOG_LEVEL:
                final int apLogLevel = mApLogControl.getLogLevel();
                Log.d(TAG, "getApLogLevel result:" + apLogLevel);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRadioButtonChecked(
                                (mApLogLevelViewsId.length - apLogLevel) - 1,
                                mApLogLevelViewsId);
                    }
                });
                break;
            case GET_PS_LOG_LEVEL:
                final int psLogLevel = mCpLogControl.getLogLevel();
                Log.d(TAG, "get cp log level result:" + psLogLevel);
                if (psLogLevel >= 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setRadioButtonChecked(psLogLevel,
                                    mPsLogLevelViewsId);
                        }
                    });
                }
                break;
            case GET_CP_SINGLE_FILE_SIZE:
                final long size = mCpLogControl
                        .getSingleFileSize(CPLogControl.SUB_LOG_5MODE_TYPE);
                Log.d(TAG, "getSingleFileSize: " + size);
                if (size >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemSignalLogSize.setSummary("" + size);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemSignalLogSize.setSummary("error");
                        }
                    });
                }
                break;
            case GET_CP_EXTERNAL_SIZE:

                final long cpExternalSize = mCpLogControl.getTotalCpLogSize(
                        CPLogControl.SUB_LOG_5MODE_TYPE, false);
                Log.d(TAG, "getCpExternalSize: " + cpExternalSize);
                if (cpExternalSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemExternalSize.setSummary("" + cpExternalSize);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemExternalSize.setSummary("error");
                        }
                    });
                }

                break;
            case GET_CP_INTERNAL_FILE_SIZE:

                final long cpInternalSize = mCpLogControl.getTotalCpLogSize(
                        CPLogControl.SUB_LOG_5MODE_TYPE, true);
                Log.d(TAG, "getCpInternalSize: " + cpInternalSize);
                if (cpInternalSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemInternalSize.setSummary("" + cpInternalSize);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModemInternalSize.setSummary("error");
                        }
                    });
                }

                break;
            case GET_AP_CAP_SIZE:
                final long capSize = mApLogControl.getCapSize();
                Log.d(TAG, "getCpExternalSize: " + capSize);
                if (capSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mApCapLength.setSummary(capSize + "");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mApCapLength.setSummary("error");
                        }
                    });
                }
                break;
            case GET_CP_CAP_SIZE:
                final long cpCapLen = mCpLogControl.getCapSize();
                if (cpCapLen > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCpCapLength.setSummary(cpCapLen + "");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCpCapLength.setSummary("product not support");
                            mCpCapLength.setEnabled(false);
                        }
                    });
                }
                break;
            case GET_CONNECTIVITY_SINGLE_FILE_SIZE:
                final long cp2SingleFileSize = mCpLogControl
                        .getSingleFileSize(CPLogControl.SUB_LOG_WCN_TYPE);
                Log.d(TAG, "getWcnSingleFileSize: " + cp2SingleFileSize);
                if (cp2SingleFileSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2SignalLogSize
                                    .setSummary("" + cp2SingleFileSize);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2SignalLogSize.setSummary("error");
                        }
                    });
                }

                break;
            case GET_CONNECTIVITY_EXTERNAL_SIZE:

                final long wcnInternalSize = mCpLogControl.getTotalCpLogSize(
                        CPLogControl.SUB_LOG_WCN_TYPE, false);
                Log.d(TAG, "getWcnExternalSize: " + wcnInternalSize);
                if (wcnInternalSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2ExternalSize.setSummary(wcnInternalSize + "");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2ExternalSize.setSummary("error");
                        }
                    });
                }

                break;
            case GET_CONNECTIVITY_INTERNAL_FILE_SIZE:

                final long cp2InternalSize = mCpLogControl.getTotalCpLogSize(
                        CPLogControl.SUB_LOG_WCN_TYPE, true);
                Log.d(TAG, "getCpInternalSize: " + cp2InternalSize);
                if (cp2InternalSize >= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2InternalSize.setSummary("" + cp2InternalSize);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCp2InternalSize.setSummary("error");
                        }
                    });
                }

                break;
            case SET_AP_LOG_LEVEL:
                // TODO Auto-generated method stub
                final int logLevel = (Integer) (msg.obj);
                boolean setResult = mApLogControl.setLogLevel(logLevel);
                Log.d(TAG, "commitApLogLevel result:" + setResult);
                if (setResult) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setRadioButtonChecked(mApLogLevelViewsId.length
                                    - logLevel - 1, mApLogLevelViewsId);
                        }
                    });
                }

            }
        }
    }
}
