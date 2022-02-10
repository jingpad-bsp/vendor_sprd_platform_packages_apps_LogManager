package com.sprd.logmanager.logui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.os.Environment;
import android.net.LocalSocket;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.ViewGroup;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch;
import com.sprd.logmanager.logcontrol.WcnControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch.OnLogDestChangeListener;
import com.sprd.logmanager.logui.SceneInfo;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;
import com.sprd.logmanager.utils.ShellUtils;
import com.sprd.logmanager.utils.StorageUtil;
import com.sprd.logmanager.utils.ZipUtil;

public class DebugSettingActivity extends Activity implements OnClickListener {
    public static final String TAG = "DebugSettingActivity";
    private SettingPreferenceView mMoveData, mSaveStorage;
    private SettingPreferenceView mModemAssert,mSpAssert, mCp2Assert, mDumpWCN;
    private SettingPreferenceView mSaveSleepLog, mModemToPC, mWcnToPC,
            mPackLog;
    private APLogControl mApLogControl;
    private CPLogControl mCpLogControl;
    private WcnControl mWcnControl;
    private LogManagerPreference mManagerPreference;
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private int mPathIndex;
    LocalSocket mSocket = null;
    static final int UPDATE_PROGRESS = 0;
    static final int UPDATE_TO_PC_DISPLAY = 1;
    static final int CHECK_CP2 = 2;
    static final int UPDATE_SAVE_MODEM_SLEEP = 3;
    private boolean isUser = CommonUtils.isUserBuild();
    private static CPLogStorageSwitch mCPLogPcSwitch = null;
    private boolean isPaused = false;

    interface ConfirmAction {
        void execute();
    }

    private OnLogDestChangeListener mChangeListener = new OnLogDestChangeListener() {
        @Override
        public void OnLogDestChange(int logType) {
            // TODO Auto-generated method stub
            Message msg = Message.obtain();
            msg.what = UPDATE_TO_PC_DISPLAY;
            msg.arg1 = logType;
            Log.i(TAG, "OnLogDestChange and log type is " + logType);
            mHandler.sendMessage(msg);

            msg = Message.obtain();
            msg.what = UPDATE_SAVE_MODEM_SLEEP;
            mHandler.sendMessage(msg);
        }
    };
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_PROGRESS:
                /*
                 * Log.d(TAG, "progress != null:" + (mProgressDialog != null) +
                 * " showing:" + mProgressDialog.isShowing() + " progress:" +
                 * SystemProperties.getInt("cmd_services.apdump.progress", 0));
                 */
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.setProgress(PropUtils.getInt(
                            "cmd_services.apdump.progress", 0));
                    mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                }
                break;
            case UPDATE_SAVE_MODEM_SLEEP:
                new Thread(new Runnable() {
                    public void run() {
                        final boolean enable = mCpLogControl.getSubLogStatus(CPLogControl.SUB_LOG_5MODE_TYPE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSaveSleepLog.setEnabled(enable);
                            }
                        });
                    }
                }).start();
                break;
            case UPDATE_TO_PC_DISPLAY:
                if (msg.arg1 == CPLogStorageSwitch.MOEDEM_LOG_TYPE
                        && mModemToPC != null) {
                    mModemToPC.setChecked(mCPLogPcSwitch
                            .isLogToPc(CPLogStorageSwitch.MOEDEM_LOG_TYPE));
                    updateModemToPCStatus();
                }
                if (msg.arg1 == CPLogStorageSwitch.WCN_LOG_TYPE
                        && mWcnToPC != null) {
                    mWcnToPC.setChecked(mCPLogPcSwitch
                            .isLogToPc(CPLogStorageSwitch.WCN_LOG_TYPE));
                    updateWcnToPCStatus();
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ylog_debug_setting);
        mCPLogPcSwitch = CPLogStorageSwitch.getInstance();
        mApLogControl = APLogControl.getInstance();
        mCpLogControl = CPLogControl.getInstance();
        mWcnControl = WcnControl.getInstance();
        mManagerPreference = LogManagerPreference.getInstanse();
        initViews();
    }

    void updateWcnToPCStatus(){
        boolean enablelWcnToPC=(mCPLogPcSwitch.getCPLogDest(CPLogStorageSwitch.WCN_LOG_TYPE) != CPLogStorageSwitch.NO_LOG);
        mWcnToPC.setEnabled(enablelWcnToPC);
        Log.i(TAG,"mWcnToPC set enable " + enablelWcnToPC);
    }

    void updateModemToPCStatus(){
        boolean enableModemToPC=(mCPLogPcSwitch.getCPLogDest(CPLogStorageSwitch.MOEDEM_LOG_TYPE) != CPLogStorageSwitch.NO_LOG);
        mModemToPC.setEnabled(enableModemToPC);
        Log.i(TAG,"mModemToPC set enable  " + enableModemToPC);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isPaused = false;
        mCPLogPcSwitch.addListener(mChangeListener);
        boolean modemEventMonitorLogOpened=false;
        String sceneInfo = LogInfo.getInstance().getOtherSceneInfo(DebugSettingActivity.this);
        if (sceneInfo.contains("monitor")) {
            modemEventMonitorLogOpened=true;
        }
        if (SceneInfo.getCurrentSelected(this.getContentResolver()).equals(
                SceneInfo.SCENE_USER)||modemEventMonitorLogOpened) {
            mWcnToPC.setEnabled(false);
            mModemToPC.setEnabled(false);
            Log.i(TAG,"mWcnToPC&mModemToPC set enable false SCENE_USER");
        } else {
            updateWcnToPCStatus();
            updateModemToPCStatus();
        }
        if (mWcnControl.isWcnReset() || !mApLogControl.isLogStarted()) {
            mDumpWCN.setEnabled(false);
        } else {
            mDumpWCN.setEnabled(true);
        }
        Message msg = Message.obtain();
        msg.what = UPDATE_SAVE_MODEM_SLEEP;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        isPaused = true;
        mCPLogPcSwitch.removeListener(mChangeListener);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initViews() {
        mMoveData = (SettingPreferenceView) findViewById(R.id.move_data);
        mSaveStorage = (SettingPreferenceView) findViewById(R.id.storage_path);
        mModemAssert = (SettingPreferenceView) findViewById(R.id.modem_assert);
        mSpAssert = (SettingPreferenceView) findViewById(R.id.sp_assert);
        mCp2Assert = (SettingPreferenceView) findViewById(R.id.cp2_assert);
        mDumpWCN = (SettingPreferenceView) findViewById(R.id.dump_wcn);
        mSaveSleepLog = (SettingPreferenceView) findViewById(R.id.save_sleep_log);
        mModemToPC = (SettingPreferenceView) findViewById(R.id.modem_to_pc);
        mWcnToPC = (SettingPreferenceView) findViewById(R.id.wcn_to_pc);
        mPackLog = (SettingPreferenceView) findViewById(R.id.pac_log);
        mMoveData.setOnClickListener(this);
        mSaveStorage.setOnClickListener(this);
        mModemAssert.setOnClickListener(this);
        mSpAssert.setOnClickListener(this);
        mCp2Assert.setOnClickListener(this);
        mDumpWCN.setOnClickListener(this);
        mSaveSleepLog.setOnClickListener(this);
        mModemToPC.setOnClickListener(this);
        mWcnToPC.setOnClickListener(this);
        mPackLog.setOnClickListener(this);

        mModemToPC.setChecked(mCPLogPcSwitch
                .isLogToPc(CPLogStorageSwitch.MOEDEM_LOG_TYPE));
        mWcnToPC.setChecked(mCPLogPcSwitch
                .isLogToPc(CPLogStorageSwitch.WCN_LOG_TYPE));
        // mModemAssert.setEnabled(mCpLogControl.waitRildReady(1));
        getModemAssertEnable();
        mModemAssert.setSummaryVisibility(View.VISIBLE);
        mModemAssert.setSummary(getString(R.string.modem_assert_summary));
        mDumpWCN.setSummaryVisibility(View.VISIBLE);
        mDumpWCN.setSummary(getString(R.string.wcn_dump_summary));
        mCp2Assert.setSummaryVisibility(View.VISIBLE);
        mCp2Assert.setSummary(getString(R.string.wcn_assert_summary));
        mSpAssert.setSummaryVisibility(View.VISIBLE);
        mSpAssert.setSummary(getString(R.string.sp_assert_summary));
        if (!mCpLogControl.isCP2Enable()) {
            mCp2Assert.setSummaryVisibility(View.VISIBLE);
            mCp2Assert.setSummary(getString(R.string.feature_not_support));
            mCp2Assert.setEnabled(false);
        }
        //if (!mApLogControl.isLogStarted()) {
        //    mCp2Assert.setEnabled(false);
        //}
    }

    private void getModemAssertEnable() {
        boolean isRildAlive = PropUtils.getString(
                CPLogControl.RIL_SERVICE_PROP, "stopped").contains("run");
        mModemAssert.setEnabled(isRildAlive);
    }

    private void doModemAssert() {
        mModemAssert.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start doModemAssert");
                mCpLogControl.sendModemAssert();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(DebugSettingActivity.this, "success",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

        if (mCpLogControl.isModemReset()) {
            mProgressDialog = ProgressDialog.show(DebugSettingActivity.this,
                    "modem assert...", "Please wait...", false, false);

            new Thread(new Runnable() {
                public void run() {
                    boolean resetOK = mCpLogControl.waitRildReady(20);
                    Log.i(TAG, "wait for modem reset result = " + resetOK);
                    dismissProgressDialog();
                    if (mModemAssert != null && resetOK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mModemAssert.setEnabled(true);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void dumpWcnMem() {
        Log.d(TAG, "dumpWcnMem", new Exception("debugException"));
        Toast.makeText(getApplicationContext(), "start dump wcn mem",
                Toast.LENGTH_LONG).show();
        mDumpWCN.setEnabled(false);
        new Thread(runnable_dump).start();
    }

    private void doCp2Assert() {
        mCp2Assert.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                boolean result0 = mWcnControl.isWcnReset();
                Log.i(TAG, "Cp2Reset result: " + result0);
                // Add delay time to avoid misplacement of the replyed command
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                String result = mWcnControl.manualAssert();
                Log.i(TAG, "doCp2Assert result: " + result);
                if (result != null && result0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mCp2Assert.setEnabled(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(DebugSettingActivity.this, R.string.cp2_assert_tips,
                                        Toast.LENGTH_SHORT).show();
                            }
                    });
                }
                dismissProgressDialog();
            }
        }).start();

    }

    void doTokenConfirm(ConfirmAction ca, int title_id) {
        int ok = (int) (Math.random() * 13) % 3 + 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                DebugSettingActivity.this);
        builder.setTitle(title_id);
        builder.setMessage(getString(R.string.pin_confirm1) + " " + ok + " "
                + getString(R.string.pin_confirm2));
        builder.setPositiveButton("                     1   ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ok == 1) {
                            ca.execute();
                        }
                    }
                });
        builder.setNeutralButton("                     3   ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ok == 3) {
                            ca.execute();
                        }
                    }
                });
        builder.setNegativeButton("                     2   ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ok == 2) {
                            ca.execute();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onClick(View v) {
        final boolean  cp2Reset = mWcnControl.isWcnReset();
        switch (v.getId()) {
        case R.id.move_data:
            moveData();
            break;
        case R.id.storage_path:
            if (!mApLogControl.isLogStarted()) {
                Toast.makeText(this, R.string.slog_not_start,
                        Toast.LENGTH_SHORT).show();
            } else {
                changeStoragePath();
            }
            break;
        case R.id.sp_assert:
            mSpAssert.setEnabled(false);
		    new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    ShellUtils.runCommand("echo \"assert on\" > dev/sctl_pm");
                }
            }).start();
            break;
        case R.id.modem_assert: {
            ConfirmAction ca = new ConfirmAction() {
                public void execute() {
                    boolean result = mCpLogControl.isModemReset();
                    if (result) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                DebugSettingActivity.this);
                        builder.setMessage(R.string.modem_assert_alert_discription);
                        builder.setPositiveButton(R.string.go_on,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                            int arg1) {
                                        // TODO Auto-generated method stub
                                        doModemAssert();
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    } else {
                        doModemAssert();
                    }
                }
            };
            doTokenConfirm(ca, R.string.modem_assert);
        }
            break;
        case R.id.cp2_assert: {
            boolean logEnable = mApLogControl.isLogStarted();
            ConfirmAction ca = new ConfirmAction() {
                public void execute() {
                    if (cp2Reset || !logEnable) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                DebugSettingActivity.this);
                        if (cp2Reset && logEnable){
                            builder.setMessage(R.string.wcn_assert_alert_discription);
                        }else if(cp2Reset && !logEnable){
                            builder.setMessage(R.string.wcn_assert_alert_discription1);
                        }else{
                            builder.setMessage(R.string.wcn_assert_alert_discription2);
                        }
                        builder.setPositiveButton(R.string.go_on,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                            int arg1) {
                                        // TODO Auto-generated method stub
                                        doCp2Assert();
                                        mProgressDialog = ProgressDialog.show(
                                                DebugSettingActivity.this,
                                                "Setting...", "Please wait...",
                                                true, false);
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    } else {
                        doCp2Assert();
                    }
                }
            };
            doTokenConfirm(ca, R.string.cp2_assert);
        }
            break;
        case R.id.dump_wcn: {
            ConfirmAction ca = new ConfirmAction() {
                public void execute() {
                    boolean reset = mWcnControl.isWcnReset();
                    if (reset) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                DebugSettingActivity.this);
                        builder.setMessage(R.string.wcn_dump_alert_discription);
                        builder.setPositiveButton(R.string.go_on,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                            int arg1) {
                                        // TODO Auto-generated method stub
                                        dumpWcnMem();
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.show();
                    } else {
                        dumpWcnMem();
                    }
                }
            };
            doTokenConfirm(ca, R.string.dump_wcn);
        }
            break;

        case R.id.save_sleep_log:
            new Thread(sleepLogThread).start();
            break;
        case R.id.modem_to_pc:
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCPLogPcSwitch
                            .isLogToPc(CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
                        mCPLogPcSwitch.setModemLogToPhone();
                    } else {
                        mCPLogPcSwitch.setModemLogToPC();
                    }
                }
            }).start();
            break;
        case R.id.wcn_to_pc:
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCPLogPcSwitch
                            .isLogToPc(CPLogStorageSwitch.WCN_LOG_TYPE)) {
                        Log.i(TAG, "set wcn to phone");
                        mCPLogPcSwitch.setWcnLogToPhone();
                    } else {
                        Log.i(TAG, "set wcn to pc");
                        mCPLogPcSwitch.setWcnLogToPC();
                    }
                }
            }).start();
            break;
        case R.id.pac_log:
            pacLogAync();
            break;
        }
    }

    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void changeStoragePath() {
        final String[] titles = getResources().getStringArray(
                R.array.mini_dump_entries);
        String path = mApLogControl.getLogSavePath();
        if (path == null || path.contains("emulated")|| path.contains("/data/")) {
            mPathIndex = 1;
        } else {
            mPathIndex = 0;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.storage_path))
                .setSingleChoiceItems(titles, mPathIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                boolean isExternal = StorageUtil
                                        .getExternalStorageState();
                                long freeSpace = 0;
                                long internalFree = 0;
                                if (which == 0) {
                                    freeSpace = StorageUtil
                                            .getFreeSpace(StorageUtil
                                                    .getExternalStorage());
                                }else{
                                    freeSpace = StorageUtil
                                            .getFreeSpace(Environment
                                                    .getDataDirectory());
                                }
                                Log.i(TAG, "internel free space is " + freeSpace
                                        + ",and external state is " + isExternal);
                                if ( which == 0 && !isExternal ) {
                                    if (mAlertDialog != null
                                            && mAlertDialog.isShowing()) {
                                        mAlertDialog.dismiss();
                                        mAlertDialog = null;
                                    }
                                    Toast.makeText(DebugSettingActivity.this,
                                            R.string.insert_sdcard_tips,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (freeSpace < 100*1024*1024 && which != mPathIndex) {
                                    if (mAlertDialog != null
                                            && mAlertDialog.isShowing()) {
                                        mAlertDialog.dismiss();
                                        mAlertDialog = null;
                                    }
                                    if ( which == 0  ) {
                                        Toast.makeText(DebugSettingActivity.this,
                                            R.string.sdcard_error_tips,
                                            Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    return;
                                }
                                if (which == mPathIndex) {
                                    if (mAlertDialog != null
                                            && mAlertDialog.isShowing()) {
                                        mAlertDialog.dismiss();
                                        mAlertDialog = null;
                                    }
                                    return;
                                }
                                mSaveStorage.setEnabled(false);
                                final boolean setExternal = (which == 0);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final boolean setOK=mApLogControl
                                                .setStorageInSd(setExternal);
                                        if (setOK){
                                            mCpLogControl
                                                .setStorageInSd(setExternal);
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSaveStorage.setEnabled(true);
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        setOK?R.string.logmanager_setlogpath_success:R.string.logmanager_setlogpath_fail,
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });
                                    }
                                }).start();
                                if (mAlertDialog != null
                                        && mAlertDialog.isShowing()) {
                                    mAlertDialog.dismiss();
                                    mAlertDialog = null;
                                }
                            }
                        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        if (!isPaused) {
            mAlertDialog.show();
        }
    }

    private void moveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = new ProgressDialog(
                                DebugSettingActivity.this);
                        mProgressDialog.setTitle(getResources().getString(
                                R.string.moving_log));
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.moving_log_message));
                        // mProgressDialog
                        // .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        // mProgressDialog.setProgress(0);
                        // mProgressDialog.setMax(100);
                        mProgressDialog.setCancelable(false);
                        if (!isPaused) {
                            mProgressDialog.show();
                        }
                        // mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,
                        // 500);
                    }
                });
                Log.d(TAG, " ylog minidump begin..");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
                Date date = new Date(System.currentTimeMillis());
                ShellUtils
                        .runCommand("apdumper -r -f /system/etc/phonedump.conf -d /storage/emulated/0/ylog/ap/phonedata/phonedump_"
                                + sdf.format(date) + "/");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null
                                && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        Toast.makeText(getApplicationContext(), "move success",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void pacLogAync() {
        mAlertDialog = new AlertDialog.Builder(DebugSettingActivity.this)
                .setTitle("Pack Log")
                .setMessage(
                        DebugSettingActivity.this
                                .getString(R.string.logmanager_pac_log_confirm))
                .setPositiveButton(
                        DebugSettingActivity.this
                                .getString(R.string.alertdialog_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (mAlertDialog != null
                                        && mAlertDialog.isShowing()) {
                                    mAlertDialog.dismiss();
                                }
                                mProgressDialog = ProgressDialog.show(
                                        DebugSettingActivity.this,
                                        "Packing ...", "Packing Wait...", true,
                                        false);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String[] logPaths = new String[] {
                                                "/data/ylog/",
                                                mApLogControl.getLogSavePath() };
                                        ContentResolver cv = DebugSettingActivity.this
                                                .getContentResolver();
                                        SimpleDateFormat formatter = new SimpleDateFormat(
                                                "yyyy-MM-dd-HH-mm-ss");
                                        Date curDate = new Date(System
                                                .currentTimeMillis());
                                        String fileName = formatter
                                                .format(curDate);
                                        if (!mApLogControl.isLogStarted()) {
                                            if (StorageUtil
                                                    .getExternalStorageState()) {
                                                logPaths[1] = StorageUtil
                                                        .getExternalStorage()
                                                        + "/ylog";
                                            } else {
                                                logPaths[1] = "storage/emulated/0/ylog";
                                            }
                                        }
                                        final String logSavePath = logPaths[1]
                                                + "_" + fileName + ".tar.gz";
                                        try {
                                            String cmd = String
                                                    .format("tar -czf %s /cache/ylog %s %s ",
                                                            logSavePath,
                                                            logPaths[0],
                                                            logPaths[1]);
                                            ShellUtils.runCommand(cmd);
                                            scanFileAsync(logSavePath);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            DebugSettingActivity.this,
                                                            DebugSettingActivity.this
                                                                    .getString(
                                                                            R.string.logmanager_pac_log_success,
                                                                            logSavePath),
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            Log.d(TAG, "packing log error:", e);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            DebugSettingActivity.this,
                                                            DebugSettingActivity.this
                                                                    .getString(R.string.logmanager_pac_log_fail),
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            });
                                        }
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                }).start();
                            }
                        })
                .setNegativeButton(
                        DebugSettingActivity.this
                                .getString(R.string.alertdialog_cancel),
                        null).show();
    }

    private void scanFileAsync(String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        sendBroadcast(scanIntent);
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        super.onDestroy();
    }

    private Runnable runnable_dump = new Runnable() {
        @Override
        public void run() {
            mWcnControl.dumpWcnMem();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "dump wcn mem finished", Toast.LENGTH_LONG).show();
                    // mDumpWCN.setEnabled(true);
                }
            });
        }
    };

    private Runnable sleepLogThread = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSaveSleepLog.setEnabled(false);
                }
            });
            Log.d("DebugSettingActivity", "save sleeplog start");
            final int ret = mCpLogControl.saveSleepLog();
            Log.d("DebugSettingActivity", "save sleeplog finish");
            if (ret == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "save sleeplog finished", Toast.LENGTH_SHORT)
                                .show();
                        mSaveSleepLog.setEnabled(true);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String tips = null;
                        switch (ret) {
                        case 1:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_1);
                            break;
                        case 2:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_2);
                            break;
                        case 3:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_3);
                            break;
                        case 4:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_4);
                            break;
                        case 5:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_5);
                            break;
                        case 6:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_6);
                            break;
                        case 7:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_7);
                            break;
                        case 8:
                            tips = getApplicationContext().getString(
                                    R.string.slogmodem_error_8);
                            break;
                        default:
                            tips = "error no:" + ret;
                            break;
                        }
                        Toast.makeText(getApplicationContext(), tips,
                                Toast.LENGTH_SHORT).show();
                        mSaveSleepLog.setEnabled(true);
                    }
                });
            }
        }
    };
}
