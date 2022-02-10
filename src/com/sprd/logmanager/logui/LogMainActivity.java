package com.sprd.logmanager.logui;

/*
 * Copyright (C) 2013 Spreadtrum Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.database.LogSceneManager;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch;
import com.sprd.logmanager.logcontrol.WcnControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch.OnLogDestChangeListener;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.LogManagerFileUtils;
import com.sprd.logmanager.utils.PropUtils;
import com.sprd.logmanager.utils.ShellUtils;
import com.sprd.logmanager.utils.StorageUtil;
import android.app.ActivityManager;
import com.sprd.logmanager.LogManagerApplication;


public class LogMainActivity extends Activity implements OnClickListener {
    private ToggleButton mGeneral;
    private ProgressBar mStorageUsage;
    private static final String TAG = "LogMainActivity";
    private static final int CLEAR_LOG = 0;
    private static final int START_YLOG = CLEAR_LOG + 1;
    private static final int STOP_YLOG = START_YLOG + 1;
    private static final int UPDATE_SCENE = STOP_YLOG + 1;
    private static final int UPDATE_SUB_SCENE_1 = UPDATE_SCENE + 1;
    private static final int UPDATE_SUB_SCENE_2 = UPDATE_SUB_SCENE_1 + 1;
    private static final int UPDATE_SUB_SCENE_3 = UPDATE_SUB_SCENE_2 + 1;
    private static final int UPDATE_SUB_SCENE_4 = UPDATE_SUB_SCENE_3 + 1;

    private static final int UPDATE_TIME = UPDATE_SUB_SCENE_4 + 1;
    private static final int UPDATE_PATH = UPDATE_TIME + 1;
    private static final int UPDATE_PROGRESS = UPDATE_PATH + 1;
    private static final int DELETE_TIME_OUT_MSG = UPDATE_PROGRESS + 1;
    private static final int UPDATE_TIME2 = DELETE_TIME_OUT_MSG + 1;
    private static final int GETALLLOG = UPDATE_TIME2 + 1;
    private static final int UPDATE_USAGE = GETALLLOG + 1;
    private static final int CHECK_MODEM_ALIVE = UPDATE_USAGE + 1;
    private static final int SHOW_WARNING_DIALOG = CHECK_MODEM_ALIVE + 1;
    private static final int DELETE_MAX_TIME = 5 * 60 * 1000;
    private Button mClear;
    private Button mScene;
    private Button mTool;
    private TextView mSceneInfo1, mSceneInfo2, mSceneInfo3, mSceneInfo4;
    private TextView mSceneTime;
    private TextView mSceneSavePath;
    private TextView mStorageUsed;
    private TextView mStorageFree;
    private YlogHandler mYlogHandler;
    protected Handler mMainThreadHandler;
    private ProgressDialog mProgressDialog;
    private PopupWindow mPopupWindow;
    private String timeelaps = "";
    private APLogControl mApLogControl;
    private CPLogControl mCpLogControl;
    private WcnControl mWcnControl;
    HandlerThread mYlogHandlerThread;
    private LogManagerPreference managerPreference;
    private boolean logSwitching = false;
    private int tick = 0;
    private OnLogDestChangeListener mChangeListener = new OnLogDestChangeListener() {
        @Override
        public void OnLogDestChange(int logType) {
            Message updateScene = mYlogHandler.obtainMessage(UPDATE_SCENE);
            mYlogHandler.sendMessage(updateScene);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((CommonUtils.isGSIVersion())||(!LogManagerApplication.getInstance().isAdminUser())) {
            Log.i(TAG, "gsi version or guest account ,not start");
            int resID=R.string.not_support;
            if ((!LogManagerApplication.getInstance().isAdminUser())) {
                resID=R.string.gueset_not_support;
            }
            Toast.makeText(
                    LogMainActivity.this,
                    LogMainActivity.this.getString(resID),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.layout_slog_main);
        mYlogHandlerThread = new HandlerThread(TAG);
        mYlogHandlerThread.start();
        mYlogHandler = new YlogHandler(mYlogHandlerThread.getLooper());

        mGeneral = (ToggleButton) findViewById(R.id.general);
        mClear = (Button) findViewById(R.id.clearlog);
        mTool = (Button) findViewById(R.id.btn_tool);
        mScene = (Button) findViewById(R.id.btn_scene);

        mSceneInfo1 = (TextView) findViewById(R.id.tv_scene1);
        mSceneInfo2 = (TextView) findViewById(R.id.tv_scene2);
        mSceneInfo3 = (TextView) findViewById(R.id.tv_scene3);
        mSceneInfo4 = (TextView) findViewById(R.id.tv_scene4);
        mSceneTime = (TextView) findViewById(R.id.tv_logtime);
        mSceneSavePath = (TextView) findViewById(R.id.tv_logpath);
        mStorageUsed = (TextView) findViewById(R.id.storage_usage_used);
        mStorageFree = (TextView) findViewById(R.id.storage_usage_free);
        mStorageUsage = (ProgressBar) findViewById(R.id.storage_usage);
        mSceneTime.setText("00:00:00");
        mGeneral.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mTool.setOnClickListener(this);
        mGeneral.setTextColor(Color.WHITE);

        mScene.setOnClickListener(this);
        mMainThreadHandler = new Handler(getMainLooper());

        findViewById(R.id.about_ylog).setOnClickListener(this);
        mApLogControl = APLogControl.getInstance();
        mCpLogControl = CPLogControl.getInstance();
        mWcnControl = WcnControl.getInstance();
        managerPreference = LogManagerPreference.getInstanse();
        ((TextView)findViewById(R.id.tv_logversion)).setText(getYlogVersion());
        ((TextView)findViewById(R.id.tv_logmanagerversion)).setText(getLogManagerVersion());

        // am start -a android.intent.action.MAIN -n
        // com.sprd.logmanager.logui/com.sprd.logmanager.logui.LogMainActivity
        // --ei magic 902 --es android "1" --es bthci "1" --es apcap "1" --es ps
        // "1" --es armpcm "1" --es dsp "1" --es wifibt "1" --es gnss "1" --es
        // hub "1" --es dsppcm "1" --es cpcap "1" --es abe "0"
        Intent intent = getIntent();
        if (intent != null) {
            int magic = intent.getIntExtra("magic", -1);
            if (902 == magic) {
                String android = intent.getStringExtra("android");
                String bthci = intent.getStringExtra("bthci");
                String apcap = intent.getStringExtra("apcap");
                String pslog = intent.getStringExtra("ps");
                String armpcm = intent.getStringExtra("armpcm");
                String dsplog = intent.getStringExtra("dsp");
                String wifibt = intent.getStringExtra("wifibt");
                String gnss = intent.getStringExtra("gnss");
                String hub = intent.getStringExtra("hub");
                String dsppcm = intent.getStringExtra("dsppcm");
                String cpcap = intent.getStringExtra("cpcap");
                String abe = intent.getStringExtra("abe");
                LogInfo.getInstance().startCustomScene(LogMainActivity.this,
                        android, bthci, apcap, pslog, armpcm, dsplog, wifibt,
                        gnss, hub, dsppcm, cpcap, abe);
                Log.w(TAG, " intent startscene finished");
            } else {
                Log.w(TAG, " intent magic wrong");
            }
        } else {
            Log.w(TAG, "no intent");
        }
        Log.d(TAG, "onCreate");
        if (ActivityManager.isUserAMonkey()) {
            finish();
        }
        Message getAllLog = mYlogHandler.obtainMessage(GETALLLOG);
        mYlogHandler.sendMessage(getAllLog);


    }



    @Override
    public void onResume() {
        super.onResume();
        if (ActivityManager.isUserAMonkey()) {
            finish();
            return;
        }
        LogInfo.getInstance().setSceneStatus(this);
        mGeneral.setText(LogSceneManager.getInstance()
                .getCurrentSelectedSceneName(this));
        boolean ylogOnOff = mApLogControl.isLogStarted();
        //for bug 1041534,when log is switching ,do not update mGeneral ui
        if (logSwitching == false){
            mGeneral.setChecked(ylogOnOff);
        }
        if (LogInfo.getInstance().getSlogStartTime() == 0) {
            LogInfo.getInstance().resetStartTime();
        }
        CPLogStorageSwitch.getInstance().addListener(mChangeListener);
        Message updateScene = mYlogHandler.obtainMessage(UPDATE_SCENE);
        mYlogHandler.sendMessage(updateScene);
        Message updatePath = mYlogHandler.obtainMessage(UPDATE_PATH);
        mYlogHandler.sendMessage(updatePath);
        // SPRD: Bug 557464 normal log button display is not same with scence
        // activity
        if (ylogOnOff) {
            Log.d(TAG, "onResume start runnable_Time");
            mStop = false;
            new Thread(runnable_Time).start();
        } else {
            mStop = true;
            Log.d(TAG, "onResume  runnable_Time not start");
        }
//        Message checkModemAlive = mYlogHandler.obtainMessage(CHECK_MODEM_ALIVE);
//        mYlogHandler.sendMessage(checkModemAlive);
        updatePath = mYlogHandler.obtainMessage(UPDATE_PATH);
        mYlogHandler.sendMessageDelayed(updatePath,10000);
    }

    @Override
    public void onPause() {
        mStop = true;
        super.onPause();
        CPLogStorageSwitch.getInstance().removeListener(mChangeListener);
    }

    protected void initPopuptWindow() {
        View popupWindow_view = getLayoutInflater().inflate(
                R.layout.about_ylog, null, false);
        mPopupWindow = new PopupWindow(popupWindow_view,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
    }

    private void getPopupWindow() {
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
            return;
        } else {
            initPopuptWindow();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.general:
            /*
             * SPRD: Bug 564177 set the log scene during capture the armlog,
             * EngineerMode is no responding @{
             */
            mProgressDialog = ProgressDialog.show(LogMainActivity.this,
                    getResources().getString(R.string.scene_switching),
                    getResources().getString(R.string.scene_switching_wait),
                    true, false);
            /* @} */
            if (mGeneral.isChecked()) {
                Message startYlogService = mYlogHandler
                        .obtainMessage(START_YLOG);
                mYlogHandler.sendMessage(startYlogService);
            } else {
                Message stopYlogService = mYlogHandler.obtainMessage(STOP_YLOG);
                mYlogHandler.sendMessage(stopYlogService);
            }
            break;
        case R.id.clearlog:
            new AlertDialog.Builder(this)
                    .setTitle("clear")
                    .setMessage(this.getString(R.string.slog_want_clear))
                    .setPositiveButton(this.getString(R.string.alertdialog_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    Message clearLogService = mYlogHandler
                                            .obtainMessage(CLEAR_LOG);
                                    mYlogHandler.sendMessage(clearLogService);
                                }
                            })
                    .setNegativeButton(
                            this.getString(R.string.alertdialog_cancel), null)
                    .show();
            break;
        case R.id.btn_tool:
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dump_full_log_tips)
                    .setMessage(R.string.dump_full_log)
                    .setPositiveButton(this.getString(R.string.alertdialog_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    getFullLog(false);
                                }
                            })
                    .setNegativeButton(
                            this.getString(R.string.alertdialog_cancel), null)
                    .show();
            break;
        case R.id.btn_scene:
            Intent iScene = new Intent(this, SceneSettingActivity.class);
            startActivity(iScene);
            break;
        case R.id.about_ylog:
            getPopupWindow();
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            mPopupWindow.showAsDropDown(v, 38, 45);
            break;
        }
    }


    public void getFullLog(boolean isOnCreate) {
        final String DATE_PROP="vendor.logman.date";
        final String PHASE_PROP="vendor.logman.phase";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date(System.currentTimeMillis());
        final String nowDate=sdf.format(date);
        final String lastDate=LogManagerPreference.getInstanse().getProp(DATE_PROP);
        final String phase =LogManagerPreference.getInstanse().getProp(PHASE_PROP);
        final boolean isAPdumpOver=phase.equals("");


        if(isOnCreate){
            boolean res = ShellUtils.enableCmdservice(false);
            Log.i(TAG, "onCreate& dumping action " + "["+lastDate+ "]["+phase+"]");
            LogManagerPreference.getInstanse().setProp(DATE_PROP, "");
            LogManagerPreference.getInstanse().setProp(PHASE_PROP, "");
            return;
         }
        boolean res = ShellUtils.enableCmdservice(true);
        if (!res) {
            Log.i(TAG, "cmd service is not enable ,res is :" + res);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = new ProgressDialog(
                                LogMainActivity.this);
                        mProgressDialog.setTitle(getResources().getString(
                                R.string.dump_full_log_tips));
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_ap_log_message));
                        mProgressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setProgress(0);
                        mProgressDialog.setMax(100);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                    }
                });
                Log.d(TAG, "get_full_log_debug ylog apdumper begin..");

                String logDate="";
                if(lastDate.equals("")){
                     LogManagerPreference.getInstanse().setProp(DATE_PROP, nowDate);
                     logDate=nowDate;
                     Log.i(TAG, "start new dump action" + "last["+lastDate+ "]phase["+phase+"]logdate["+logDate+"]");
                }else{
                    logDate=lastDate;
                    Log.i(TAG, "resume last dump action" + "last["+lastDate+ "]phase["+phase+"]logdate["+logDate+"]");
                }
                boolean hasSdcard = StorageUtil.getExternalStorageState();
                String defautStorage  = StorageUtil.getExternalStorage();
                if (hasSdcard){
                    defautStorage = StorageUtil.getExternalStorage();
                }else {
                    defautStorage = "/storage/emulated/0/";
                }
                final String buffDumpPath = defautStorage+ "/ylog/ap/logbuffer/"+ logDate + "/";
                final String phoneDumpPath = defautStorage+ "/ylog/ap/phonedata/"+ logDate + "/";
                if (managerPreference.getBugreportEnable()) {
                    if(isAPdumpOver){
                        String cmd="ASYNC:apdumper -r -f /system/etc/buffdump.conf -d "+ buffDumpPath;
                        ShellUtils.runCommand2(cmd);
                    }
                }

                if(isAPdumpOver){
                    String cmd="ASYNC:apdumper -r -f /system/etc/phonedump.conf -d "+ phoneDumpPath;
                    ShellUtils.runCommand2(cmd);
                    LogManagerPreference.getInstanse().setProp(PHASE_PROP, "1");
                    Log.i(TAG, "dump cmd send over" + "last["+lastDate+ "]phase["+phase+"]logdate["+logDate+"]");
                }

                 runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       mHandler.removeMessages(UPDATE_PROGRESS);
                       mProgressDialog.setProgress(10);

                    }
                });

              int count=300;//10 minutes
               while(true){
                  File  filePath = new File(phoneDumpPath + "/result.log");
                  boolean phoneDone=(filePath.exists() && filePath.isFile());
                  boolean logBuffDone=true;
                  if (managerPreference.getBugreportEnable()) {
                      filePath = new File(buffDumpPath + "/result.log");
                      logBuffDone=(filePath.exists() && filePath.isFile());
                  }
                  if(phoneDone && logBuffDone)    {
                      break;
                  }else{
                       try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                             e.printStackTrace();
                        }
                        count--;
                        if(count<=0){
                          Log.i(TAG, "waiting ap dump timeout" );
                           break;
                        }
                        Log.i(TAG, "waiting ap dump result ..." );
                   }
                }

              if(count<=0){//timeout
                  LogManagerPreference.getInstanse().setProp(DATE_PROP, "");
                  LogManagerPreference.getInstanse().setProp(PHASE_PROP, "");
                  Log.i(TAG, "dump ap failed" + "last["+lastDate+ "]phase["+phase+"]logdate["+logDate+"]");
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            }
                      Toast.makeText(
                                    LogMainActivity.this,
                                    LogMainActivity.this
                                            .getString(R.string.dumptimeout_tips),
                                    Toast.LENGTH_SHORT).show();

                    }
                });
                 return;
                }

                 runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                mHandler.removeMessages(UPDATE_PROGRESS);
                mProgressDialog.setProgress(20);
                mProgressDialog.setMessage(getResources().getString(R.string.dump_cp0_log_message));

                    }
                });
                Log.d(TAG, "get_full_log_debug ylog apdumper end..");
                Log.i(TAG, "dump cp start " + "["+lastDate+ "]["+phase+"]"+logDate);
                mWcnControl.flashWcnLog();
                mCpLogControl.collectModemLog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(40);
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_sensorhub_log_message));
                    }
                });
                Log.d(TAG, "get_full_log_debug COLLECT_LOG PM_SH begin..");
                mCpLogControl.collectLog(CPLogControl.SUB_LOG_AG_PM_SH_TYPE);
                Log.d(TAG, "get_full_log_debug COLLECT_LOG PM_SH end..");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(50);
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_wcn_log_message));
                    }
                });

                Log.d(TAG, "get_full_log_debug COLLECT_LOG WCN begin..");
                mCpLogControl.collectLog(CPLogControl.SUB_LOG_WCN_TYPE);
                Log.d(TAG, "get_full_log_debug COLLECT_LOG WCN end..");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(60);
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_gnss_log_message));
                    }
                });

                Log.d(TAG, "get_full_log_debug COLLECT_LOG GNSS begin..");
                mCpLogControl.collectLog(CPLogControl.SUB_LOG_GNSS_TYPE);
                Log.d(TAG, "get_full_log_debug COLLECT_LOG GNSS end..");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(80);
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_audio_log_message));
                    }
                });

                Log.d(TAG, "get_full_log_debug COLLECT_LOG AG-DSP begin..");
                mCpLogControl.collectLog(CPLogControl.SUB_LOG_AG_DSP_TYPE);
                Log.d(TAG, "get_full_log_debug COLLECT_LOG AG-DSP end..");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(90);
                        mProgressDialog.setMessage(getResources().getString(
                                R.string.dump_log2pc_log_message));
                    }
                });
                Log.d(TAG,"get_full_log_debug COLLECT_LOG all end dissmisdialog begin..");

                LogManagerPreference.getInstanse().setProp(DATE_PROP, "");
                LogManagerPreference.getInstanse().setProp(PHASE_PROP, "");
                //Log.i(TAG, "all dump over " + "["+lastDate+ "]["+phase+"]"+logDate);
                 Log.i(TAG, "all dump  over" + "last["+lastDate+ "]phase["+phase+"]logdate["+logDate+"]");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            Log.d(TAG,
                                    "get_full_log_debug COLLECT_LOG all end dissmisdialog end..");
                        }


                        Toast.makeText(
                                LogMainActivity.this,
                                LogMainActivity.this
                                        .getString(R.string.dump_full_log_success),
                                Toast.LENGTH_LONG).show();

                    }
                });
            }
        }).start();
    }

    private void updateStoragePath(){
        String logSavePath = mApLogControl.getLogSavePath();
        Message msg = mHandler.obtainMessage(UPDATE_PATH);
        msg.obj = logSavePath;
        mHandler.sendMessage(msg);
    }

    Runnable runnable_Time = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnable_Time started");
            tick = 0;
            while (mStop != true) {
                if (tick == 60 || tick == 0){
                    mHandler.obtainMessage(UPDATE_TIME).sendToTarget();
                    tick = 0;
                }
                tick ++;
                Message msg = mHandler.obtainMessage(UPDATE_USAGE);
                mHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "runnable_Time exited");
        }
    };

    @Override
    public void onDestroy() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mYlogHandlerThread != null) {
            mYlogHandlerThread.quitSafely();
        }
        super.onDestroy();
    }

    private void setUsage() {

        if (mStorageUsed == null || mStorageUsage == null
                || mStorageFree == null) {
            Log.e(TAG, "setUsage storage error");
            return ;
        }
        mYlogHandler.post(new Runnable() {
            @Override
            public void run() {
                String path = "";
                if (mApLogControl.isLogStarted()) {
                    path = mApLogControl.getLogSavePath();
                    Message msg = mHandler.obtainMessage(UPDATE_PATH);
                    msg.obj = path;
                    mHandler.sendMessage(msg);

                } else {
                    path = mSceneSavePath.getText().toString().trim();
                    if (path != null && !path.equals("")) {
                        try {
                            path = path.split(":")[1].trim();
                        } catch (Exception e) {
                            path = null;
                        }

                    }
                }
                if (path == null || path.equals("")) {
                    if (StorageUtil.getExternalStorageState()) {
                        path = StorageUtil.getExternalStorage();
                    } else {
                        path = Environment.getDataDirectory().getAbsolutePath();
                    }

                }
                //Log.i(TAG, "update usage path = " + path);
                if (path.contains("emulated")) {
                    //path = Environment.getDataDirectory().getAbsolutePath();
                }
                final long total = StorageUtil.getTotalSpace(path);
                final long freespace = StorageUtil.getFreeSpace(path);
                Log.d(TAG, "update usage path = " +path+"  total:" + String.valueOf(total) + " free:"+String.valueOf(freespace));
                if (total <= 0L) {
                    Log.e(TAG, "get path:" + path + " storage size error");
                    return;
                }
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String freespaceStr=Formatter.formatFileSize(LogMainActivity.this, freespace);
                        mStorageFree.setText(freespaceStr+ " "+ getText(R.string.storage_free));
                        String usedspaceStr=Formatter.formatFileSize(LogMainActivity.this, total - freespace);
                        mStorageUsed.setText(usedspaceStr+ " "+ getText(R.string.storage_usage));
                        int progress = (int) ((total - freespace) * 100 / total);
                        if (progress == 0 && (total - freespace) > 0) {
                            progress = 1;
                        }
                        Log.d(TAG, "update UI done " +"  total:" + freespaceStr + " free:"+freespaceStr+ "progress:"+ String.valueOf(progress));
                        mStorageUsage.setProgress(progress);
                    }
                });
            }
        });
    }

    private boolean mStop;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_TIME2:
                mSceneTime.setText(timeelaps);
                break;
            case UPDATE_TIME:
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                new Thread(new Runnable() {
                    public void run() {
                        String timestr = mApLogControl.getLogTime();
                        if ((null != timestr)&&(timestr.lastIndexOf(':')!=-1)) {
                            timeelaps=timestr.substring(0,  timestr.lastIndexOf(':'));
                        } else {
                            timeelaps = "......";
                        }
                        Message msg = obtainMessage(UPDATE_TIME2);
                        sendMessage(msg);
                    }
                }).start();
                break;
            case UPDATE_PATH:
                if (mApLogControl.isLogStarted()) {
                    mSceneSavePath.setText("Path: " + msg.obj);
                }
                break;
            case UPDATE_USAGE:
                setUsage();
                break;
            case UPDATE_SUB_SCENE_1:
                mSceneInfo1.setText("" + msg.obj);
                break;
            case UPDATE_SUB_SCENE_2:
                if (CPLogStorageSwitch.getInstance().isLogToPc(
                        CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
                    mSceneInfo2.setText(R.string.slog_modem_to_pc_lowercase);
                } else {
                    mSceneInfo2.setText("" + msg.obj);
                }
                break;
            case UPDATE_SUB_SCENE_3:
                mSceneInfo3.setText("" + msg.obj);
                break;
            case UPDATE_SUB_SCENE_4:
                mSceneInfo4.setText("" + msg.obj);
                break;
            case UPDATE_PROGRESS:
                break;
            case SHOW_WARNING_DIALOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(LogMainActivity.this);
                builder.setMessage(R.string.warning_modem_not_alive);
                builder.setPositiveButton(R.string.go_on, null);
                builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        LogMainActivity.this.finish();
                    }});
                builder.show();
                break;
            default:
                break;
            }
        }
    };

    // bug 860344
    private void setCPLogNoDest() {
        CPLogStorageSwitch logPcSwitch = CPLogStorageSwitch.getInstance();
        logPcSwitch.setCPLogDest(CPLogStorageSwitch.NO_LOG,
                CPLogStorageSwitch.WCN_LOG_TYPE);
        logPcSwitch.setCPLogDest(CPLogStorageSwitch.NO_LOG,
                CPLogStorageSwitch.MOEDEM_LOG_TYPE);
    }

    class YlogHandler extends Handler {
        public YlogHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DELETE_TIME_OUT_MSG: {
                Log.w(TAG, "clear log time out");
                break;
            }
            case CLEAR_LOG: {
                clearLogThread(); /* modified by sprd for Bug 763470 */
                break;
            }
            case START_YLOG: {
                logSwitching = true;
                boolean ret=mApLogControl.setLogStatus(true);
                if (!ret) {
                    Log.w(TAG, "start ap  log failed");
                    logSwitching = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGeneral.setChecked(false);
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                        }
                    });
                    break;
                }
                LogInfo.getInstance().open(
                        LogInfo.getInstance().getSceneStatus());
                Log.i(TAG, "START_YLOG current scene is :"
                        + LogInfo.getInstance().toString());
                Log.d(TAG, "Ylog open");
                logSwitching = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStop = false;
                        // SPRD: Bug 557464 normal log button display is not
                        // same with scence activity
                        if (mApLogControl.isLogStarted()) {
                            new Thread(runnable_Time).start();
                            updateStoragePath();
                            Message updatePath = mYlogHandler.obtainMessage(UPDATE_PATH);
                            mYlogHandler.sendMessageDelayed(updatePath,10000);
                        }
                        // SPRD: Bug 564177 set the log scene during capture
                        // the armlog, EngineerMode is no responding
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
                break;
            }
            case STOP_YLOG: {
                // SPRD: Bug 557464 normal log button display is not same
                // with scence activity
                Log.d(TAG, "ap log will be close ");
                logSwitching = true;
                mApLogControl.disableAPLog();//opt perf by async close ap log

                if (mApLogControl.isLogStarted()) {
                    LogInfo.getInstance().closeScene();
                }
                mApLogControl.setLogStatus(false);
                setCPLogNoDest();
                logSwitching = false;
                Log.d(TAG, "log close");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStop = true;
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
                break;
            }
            case UPDATE_SCENE: {
                String sceneInfo = LogInfo.getInstance().getAPSceneInfo(
                        LogMainActivity.this);
                if (sceneInfo.contains("ap cap log")
                        && (mApLogControl.isLogStarted())) {
                    long apCapSize = mApLogControl.getCapSize();
                    Log.d(TAG, "apCapSize is : " + apCapSize);
                    String capwithsize = "ap cap log(" + apCapSize + ")";
                    sceneInfo = sceneInfo.replace("ap cap log", capwithsize);
                }
                Log.d(TAG, "getAPSceneInfo:" + sceneInfo);
                Message updateScene = mHandler
                        .obtainMessage(UPDATE_SUB_SCENE_1);
                updateScene.obj = sceneInfo;
                mHandler.sendMessage(updateScene);

                sceneInfo = LogInfo.getInstance().getModemSceneInfo(
                        LogMainActivity.this);
                Log.d(TAG, "getModemSceneInfo:" + sceneInfo);
                updateScene = mHandler.obtainMessage(UPDATE_SUB_SCENE_2);
                updateScene.obj = sceneInfo;
                mHandler.sendMessage(updateScene);

                sceneInfo = LogInfo.getInstance().getConnectivitySceneInfo(
                        LogMainActivity.this);
                Log.d(TAG, "getConnectivitySceneInfo:" + sceneInfo);
                updateScene = mHandler.obtainMessage(UPDATE_SUB_SCENE_3);
                Log.i("xiaogang", "connectivity scene info is :" + sceneInfo);
                if (sceneInfo.contains("wcn")) {
                    if (CPLogStorageSwitch.getInstance().isLogToPc(
                            CPLogStorageSwitch.WCN_LOG_TYPE)) {
                        Log.i(TAG, "wcn log to pc in sceneInfo");
                        String newString = sceneInfo.replaceFirst("log",
                                "to pc");
                        sceneInfo = newString;
                    }
                }
                updateScene.obj = sceneInfo;
                mHandler.sendMessage(updateScene);

                sceneInfo = LogInfo.getInstance().getOtherSceneInfo(
                        LogMainActivity.this);
                Log.d(TAG, "getOtherSceneInfo:" + sceneInfo);
                if (sceneInfo.contains("sensorhub")) {
                    if (CPLogStorageSwitch.getInstance().isLogToPc(
                            CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
                        Log.i(TAG, "modmem log to pc in sceneInfo,");
                        String newString = sceneInfo.replace("sensorhub log",
                                "sensorhub to pc");
                        sceneInfo = newString;
                    }
                }
                updateScene = mHandler.obtainMessage(UPDATE_SUB_SCENE_4);
                updateScene.obj = sceneInfo;
                mHandler.sendMessage(updateScene);
                break;
            }
            case UPDATE_PATH: {
                String logSavePath = mApLogControl.getLogSavePath();
                Message updatePath = mHandler.obtainMessage(UPDATE_PATH);
                updatePath.obj = logSavePath;
                mHandler.sendMessage(updatePath);
            }
            break;
            case GETALLLOG:
                getFullLog(true);
                break;
            case CHECK_MODEM_ALIVE:
                boolean isModemAlive = mCpLogControl.isModemAlive();
                if (!isModemAlive) {
                    mHandler.sendEmptyMessage(SHOW_WARNING_DIALOG);
                }
                break;
            default:
                break;
            }
        }
    }

    public void deleteYlogDirectory() {
        if (StorageUtil.getExternalStorageState()) {
            LogManagerFileUtils.delete(new File(StorageUtil
                    .getExternalStorage() + File.separator + "ylog"));
            LogManagerFileUtils.delete(new File(StorageUtil
                    .getExternalStorage() + File.separator + "modem_log"));
        }
        LogManagerFileUtils.delete(new File(Environment.getDataDirectory()
                .getAbsolutePath()
                + File.separator
                + "ylog"
                + File.separator
                + "modem_log"));
        LogManagerFileUtils.delete(new File("storage/emulated/0/ylog"));
        LogManagerFileUtils.delete(new File("storage/emulated/0/modem_log"));
    }

    /* SPRD bug 831726/833673:Delete ylog Directory except poweron dir. */
    public void deleteYlogDirectoryOnly() {
        try {
            Log.d(TAG, "deleteYlogDirectoryOnly work!");
            String[] logPaths = new String[] { "/data/ylog/ap",
                    "/storage/emulated/0/ylog/ap/", "/storage/sdcard0/ylog/ap/" };
            for (int i = 0; i < logPaths.length; i++) {
                String cmd = String.format("rm -rf %s ", logPaths[i]);
                ShellUtils.runCommand(cmd);
            }
            return;
            /*
             * if (StorageUtil.getExternalStorageState()) { Log.d(TAG,
             * "deleteYlogDirectoryOnly getExternalStorage!");
             * LogManagerFileUtils.deleteByFilter( new
             * File(StorageUtil.getExternalStorage() + File.separator + "ylog"),
             * StorageUtil.getExternalStorage() + File.separator + "ylog" +
             * File.separator + "poweron"); } Log.d(TAG,
             * "deleteYlogDirectoryOnly getInternalStorage!");
             * LogManagerFileUtils.deleteByFilter(new File(
             * "storage/emulated/0/ylog"), "/storage/emulated/0/ylog/poweron");
             * LogManagerFileUtils.deleteByFilter(new File("data/ylog/ap"),
             * "/data/ylog/poweron");
             */
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /* @} */

    /* added by sprd for Bug 763470 start */
    public void clearLogThread() {
        Log.d(TAG, "clear log begin..");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!LogMainActivity.this.isFinishing()) {
                    closeProgressDialog();
                    mProgressDialog = ProgressDialog
                            .show(LogMainActivity.this,
                                getResources().getString(
                                            R.string.clear_log_title),
                                    getResources().getString(
                                            R.string.scene_switching_wait),
                                    true, false);
                }
            }
        });
        Message deleteTimeoutMsg = mYlogHandler
                .obtainMessage(DELETE_TIME_OUT_MSG);
        Log.i(TAG, "start delete all log ...");
        mYlogHandler.sendMessageDelayed(deleteTimeoutMsg, DELETE_MAX_TIME);
        boolean mResult = false;
        if (true) {
            mResult = mCpLogControl.clearLog();
            Log.i(TAG, "mCpLogControl.clearLog done:" + mResult);
            if (!mResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(
                                LogMainActivity.this,
                                LogMainActivity.this
                                        .getString(R.string.clear_action_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            boolean yResult = mApLogControl.clearLog();
            Log.d(TAG, String.format(
                    "clear ylog result = %s, mlog result = %s", yResult,
                    mResult));
            mYlogHandler.removeMessages(DELETE_TIME_OUT_MSG);
            if (yResult && mResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(
                                LogMainActivity.this,
                                LogMainActivity.this
                                        .getString(R.string.clear_action_successed),
                                Toast.LENGTH_LONG).show();
                        LogInfo.getInstance().resetStartTime();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(
                                LogMainActivity.this,
                                LogMainActivity.this
                                        .getString(R.string.clear_action_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        Message updateTimeMsg = mYlogHandler.obtainMessage(UPDATE_TIME);
        mHandler.sendMessage(updateTimeMsg);
        Message updateUsageMsg = mHandler.obtainMessage(UPDATE_USAGE);
        mHandler.sendMessage(updateUsageMsg);
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private String getYlogVersion() {
        return mApLogControl.getLogVersion();
    }

    private String getLogManagerVersion() {
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version =  pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}
