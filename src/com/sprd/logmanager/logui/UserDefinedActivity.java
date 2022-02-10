package com.sprd.logmanager.logui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.app.ProgressDialog;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogSceneManager;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch;
import com.sprd.logmanager.logui.LogInfo.SceneStatus;

//import android.telephony.TelephonyManagerSprd;

/**
 * Created by SPREADTRUM\zhengxu.zhang on 9/6/15.
 */
public class UserDefinedActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UserDefinedActivity";
    // private ImageView ivBack;
    private Button btnCommit;

    private RelativeLayout rlAndroidLog;
    private ImageView ivAndroidLog;

    private RelativeLayout rlTcpipLog;
    private ImageView ivTcpipLog;

    private RelativeLayout rlAPTcpipLog;
    private ImageView ivAPTcpipLog;

    private RelativeLayout rlBtLog;
    private ImageView ivBtLog;

    private RelativeLayout rlModemLog;
    private ImageView ivModemLog;

    private RelativeLayout rlDspLog;
    private ImageView ivDspLog;

    private RelativeLayout rlDspMore;
    private ImageView ivDspMore;
    private LinearLayout llMoreDspOption;

    private RelativeLayout rlCpBtLog;
    private ImageView ivCpBtLog;

    private RelativeLayout rlGpsLog;
    private ImageView ivGpsLog;

    private RelativeLayout rlAG_DspLog;
    private ImageView ivAG_DspLog;

    private RelativeLayout rlCm4Log;
    private ImageView ivCm4Log;

    private RelativeLayout rlArmPcmLog;
    private ImageView ivArmPcmLog;

    private RelativeLayout rlDspPcmLog;
    private ImageView ivDspPcmLog;

    private RadioButton rbOutputUart;
    private RadioButton rbOutputModemLog;

    private RelativeLayout rlAG_DspPcmDumpLog;
    private ImageView ivAG_DspPcmDumpLog;
    private String mCustomName;
    private ProgressDialog mProgressDialog;
    private boolean mIsEdit;

    private boolean AGDSP_SUPPORT = false;

    private RelativeLayout rlEventModeLog;
    private ImageView ivEventModeLog;

    private RelativeLayout rlOrcaAPLog;
    private ImageView ivOrcaAPLog;
    private RelativeLayout rlOrcaDPLog;
    private ImageView ivOrcaDPLog;


    private static final int UPDATE_DSP_LOG_STATUS = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_DSP_LOG_STATUS:
                int dspOutput = msg.arg1;

                if (rbOutputUart != null) {
                    rbOutputUart.setChecked(dspOutput == 1 ? true : false);
                }
                if (rbOutputModemLog != null) {
                    rbOutputModemLog.setChecked(dspOutput == 2 ? true : false);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_log_user_defined);
        AGDSP_SUPPORT = CPLogControl.getInstance().isSupportAGDSP();
        mCustomName = getIntent().getStringExtra("custom_name");
        if (mCustomName == null) {
            Log.w(TAG, "mCustomName can not be null");
            finish();
            return;
        }
        mIsEdit = SceneInfo.exists(getContentResolver(), mCustomName);
        LogInfo.getInstance().loadCustomerOrder(mCustomName);
        // ivBack = (ImageView)findViewById(R.id.iv_back);
        // ivBack.setOnClickListener(this);
        btnCommit = (Button) findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(this);

        rlAndroidLog = (RelativeLayout) findViewById(R.id.rl_android_log);
        rlAndroidLog.setOnClickListener(this);
        ivAndroidLog = (ImageView) findViewById(R.id.iv_android_log);

        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_ANDROID_LOG) .equals("1")) {
            ivAndroidLog.setImageResource(R.drawable.checkbox_on);
        } else {
            ivAndroidLog.setImageResource(R.drawable.checkbox_off);
        }

        rlAPTcpipLog = (RelativeLayout) findViewById(R.id.rl_ap_tcpip_log);
        ivAPTcpipLog = (ImageView) findViewById(R.id.iv_ap_tcpip_log);
        initCheckBox(rlAPTcpipLog, ivAPTcpipLog, LogInfo.KEY_AP_CAP_LOG);

        rlTcpipLog = (RelativeLayout) findViewById(R.id.rl_tcpip_log);
        ivTcpipLog = (ImageView) findViewById(R.id.iv_tcpip_log);
        initCheckBox(rlTcpipLog, ivTcpipLog, LogInfo.KEY_CP_CAP_LOG);

        rlBtLog = (RelativeLayout) findViewById(R.id.rl_bt_log);
        ivBtLog = (ImageView) findViewById(R.id.iv_bt_log);
        initCheckBox(rlBtLog, ivBtLog, LogInfo.KEY_HCI_LOG);

        rlModemLog = (RelativeLayout) findViewById(R.id.rl_modem_log);
        ivModemLog = (ImageView) findViewById(R.id.iv_modem_log);
        initCheckBox2(rlModemLog, ivModemLog, LogInfo.KEY_ARM_LOG);

        rlDspLog = (RelativeLayout) findViewById(R.id.rl_dsp_log);
        ivDspLog = (ImageView) findViewById(R.id.iv_dsp_log);
        initCheckBox(rlDspLog, ivDspLog, LogInfo.KEY_DSP_LOG);

        rlDspMore = (RelativeLayout) findViewById(R.id.rl_dsp_more_option);
        rlDspMore.setOnClickListener(this);
        ivDspMore = (ImageView) findViewById(R.id.iv_dsp_more_option);
        llMoreDspOption = (LinearLayout) findViewById(R.id.ll_more_dsp_log);
        llMoreDspOption.setVisibility(View.GONE);

        rlCpBtLog = (RelativeLayout) findViewById(R.id.rl_cp_bt_log);
        ivCpBtLog = (ImageView) findViewById(R.id.iv_cp_bt_log);
        initCheckBox(rlCpBtLog, ivCpBtLog, LogInfo.KEY_WCN_LOG);

        rlGpsLog = (RelativeLayout) findViewById(R.id.rl_gps_log);
        ivGpsLog = (ImageView) findViewById(R.id.iv_gps_log);
        initCheckBox(rlGpsLog, ivGpsLog, LogInfo.KEY_GPS_LOG);

        rlAG_DspLog = (RelativeLayout) findViewById(R.id.rl_ag_dsp_log);
        ivAG_DspLog = (ImageView) findViewById(R.id.iv_ag_dsp_log);
        if (!AGDSP_SUPPORT) {
            rlAG_DspLog.setVisibility(View.GONE);
        } else {
            initCheckBox(rlAG_DspLog, ivAG_DspLog, LogInfo.KEY_AGDSP_OUTPUT_LOG);
        }

        rlCm4Log = (RelativeLayout) findViewById(R.id.rl_cm4_log);
        ivCm4Log = (ImageView) findViewById(R.id.iv_cm4_log);
        initCheckBox(rlCm4Log, ivCm4Log, LogInfo.KEY_CM4_LOG);

        rlArmPcmLog = (RelativeLayout) findViewById(R.id.rl_arm_pcm_log);
        ivArmPcmLog = (ImageView) findViewById(R.id.iv_arm_pcm_log);
        initCheckBox(rlArmPcmLog, ivArmPcmLog, LogInfo.KEY_ARM_PCM_LOG);

        rlDspPcmLog = (RelativeLayout) findViewById(R.id.rl_dsp_pcm_log);
        ivDspPcmLog = (ImageView) findViewById(R.id.iv_dsp_pcm_log);
        initCheckBox(rlDspPcmLog, ivDspPcmLog, LogInfo.KEY_DSP_PCM_LOG);

        /* @} */
        rbOutputUart = (RadioButton) findViewById(R.id.rb_output_uart);
        rbOutputUart.setOnClickListener(this);
        rbOutputModemLog = (RadioButton) findViewById(R.id.rb_output_modemlog);
        rbOutputModemLog.setOnClickListener(this);
        // fix bug 639276
        new Thread(new Runnable() {
            @Override
            public void run() {
                int dspOutput = getDspOutput();
                Message msg = mHandler.obtainMessage(UPDATE_DSP_LOG_STATUS);
                msg.arg1 = dspOutput;
                mHandler.sendMessage(msg);
            }
        }).start();

        rlAG_DspPcmDumpLog = (RelativeLayout) findViewById(R.id.rl_ag_dsp_dump_log);
        ivAG_DspPcmDumpLog = (ImageView) findViewById(R.id.iv_ag_dsp_dump_log);
        ivAG_DspPcmDumpLog.setOnClickListener(this);
        if (!AGDSP_SUPPORT) {
            rlAG_DspPcmDumpLog.setVisibility(View.GONE);
        } else {
            initCheckBox(rlAG_DspPcmDumpLog, ivAG_DspPcmDumpLog,
                    LogInfo.KEY_AGDSP_PCM_DUMP_LOG);
        }

        rlEventModeLog = (RelativeLayout) findViewById(R.id.rl_eventmonitor_log);
        ivEventModeLog = (ImageView) findViewById(R.id.iv_eventmonitor_log);
        initCheckBox(rlEventModeLog, ivEventModeLog, LogInfo.KEY_EVENT_MONITOR);
        rlOrcaAPLog = (RelativeLayout) findViewById(R.id.rl_orcaap_log);
        ivOrcaAPLog = (ImageView) findViewById(R.id.iv_orcaap_log);
        initCheckBox(rlOrcaAPLog, ivOrcaAPLog, LogInfo.KEY_MINI_ORCAAP);

        rlOrcaDPLog = (RelativeLayout) findViewById(R.id.rl_orcadp_log);
        ivOrcaDPLog = (ImageView) findViewById(R.id.iv_orcadp_log);
        initCheckBox(rlOrcaDPLog, ivOrcaDPLog, LogInfo.KEY_MINI_ORCADP);

    }

    private void initCheckBox(RelativeLayout rl, ImageView iv, String YlogCoreName) {
        rl.setOnClickListener(this);
        iv.setImageResource(LogInfo.getInstance()
                .getCustomerDefined(YlogCoreName).equals("0") ? R.drawable.checkbox_off
                : R.drawable.checkbox_on);
    }

    private void initCheckBox2(RelativeLayout rl, ImageView iv, String YlogCoreName) {
        rl.setOnClickListener(this);
        iv.setImageResource(LogInfo.getInstance()
                .getCustomerDefined(YlogCoreName).equals("1") ? R.drawable.checkbox_on
                : R.drawable.checkbox_off);
    }

    private void onClickCheckBox(ImageView iv, String key) {
        if (LogInfo.getInstance().getCustomer(key).equals("1")) {
            LogInfo.getInstance().setCustomer(key, "0");
            iv.setImageResource(R.drawable.checkbox_off);
        } else {
            LogInfo.getInstance().setCustomer(key, "1");
            iv.setImageResource(R.drawable.checkbox_on);
        }
    }

    private void syncModemLog() {
        Log.d(TAG, "syncModemLog");
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_ARM_LOG).equals("1")
                || LogInfo.getInstance().getCustomer(LogInfo.KEY_CP_CAP_LOG)
                        .equals("1")
                || LogInfo.getInstance().getCustomer(LogInfo.KEY_DSP_LOG)
                        .equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "1");
        } else {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "0");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "0");
        }

        ivModemLog
                .setImageResource(LogInfo.getInstance()
                        .getCustomer(LogInfo.KEY_ARM_LOG).equals("1") ? R.drawable.checkbox_on
                        : R.drawable.checkbox_off);
        ivDspLog.setImageResource(LogInfo.getInstance()
                .getCustomer(LogInfo.KEY_DSP_LOG).equals("0") ? R.drawable.checkbox_off
                : R.drawable.checkbox_on);
    }

    private void syncDSPLog() {
        Log.d(TAG, "syncDSPLog");
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_CP_CAP_LOG)
                .equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "1");
        }
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_ARM_LOG).equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_DSP_LOG, "1");
        }

        ivModemLog
                .setImageResource(LogInfo.getInstance()
                        .getCustomer(LogInfo.KEY_ARM_LOG).equals("1") ? R.drawable.checkbox_on
                        : R.drawable.checkbox_off);
        ivDspLog.setImageResource(LogInfo.getInstance()
                .getCustomer(LogInfo.KEY_DSP_LOG).equals("0") ? R.drawable.checkbox_off
                : R.drawable.checkbox_on);
    }

    private void syncPSLog() {
        Log.d(TAG, "syncPSLog");
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_CP_CAP_LOG)
                .equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "1");
        }
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_DSP_LOG).equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "1");

        }

        ivModemLog
                .setImageResource(LogInfo.getInstance()
                        .getCustomer(LogInfo.KEY_ARM_LOG).equals("1") ? R.drawable.checkbox_on
                        : R.drawable.checkbox_off);
        ivDspLog.setImageResource(LogInfo.getInstance()
                .getCustomer(LogInfo.KEY_DSP_LOG).equals("0") ? R.drawable.checkbox_off
                : R.drawable.checkbox_on);
    }
    private void setCPNormalLogStatus(String value){
                LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_CP_CAP_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_CM4_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_PCM_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_PCM_DUMP_LOG,
                    value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_OUTPUT_LOG,
                    value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_DSP_LOG,
                    value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_DSP_PCM_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_WCN_LOG, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_GPS_LOG, value);

            ivDspLog.setImageResource(value.equals("1") ? R.drawable.checkbox_on
                    : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_arm_pcm_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_dsp_log)).setImageResource(value
                    .equals("1") ? R.drawable.checkbox_on
                    : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_cp_bt_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_gps_log)).setImageResource(value
                    .equals("1") ? R.drawable.checkbox_on
                    : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_cm4_log)).setImageResource(value
                    .equals("1") ? R.drawable.checkbox_on
                    : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_dsp_pcm_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_tcpip_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);

            ((ImageView) findViewById(R.id.iv_ag_dsp_dump_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_ag_dsp_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
    }

    private void syncEventModemLog() {
        Log.d(TAG, "syncEventModemLog");
        ivEventModeLog
                .setImageResource(LogInfo.getInstance()
                        .getCustomer(LogInfo.KEY_EVENT_MONITOR).equals("0") ? R.drawable.checkbox_off
                        : R.drawable.checkbox_on);

        String value = "1";
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_EVENT_MONITOR)
                .equals("1")) {
            value = "0";
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "2");
        } else {
            value = "1";
            LogInfo.getInstance().setCustomer(LogInfo.KEY_ARM_LOG, "1");
        }

        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_EVENT_MONITOR)
                .equals("1")) {
            setCPNormalLogStatus(value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MINI_ORCAAP, value);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MINI_ORCADP, value);
            ((ImageView) findViewById(R.id.iv_orcaap_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
            ((ImageView) findViewById(R.id.iv_orcadp_log))
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
        }

        ivModemLog
                    .setImageResource(value.equals("1") ? R.drawable.checkbox_on
                            : R.drawable.checkbox_off);
    }

    private void initEventLogStauts() {
        String[] params = { "LogSwichForScene_ArmLogController",
                "LogSwichForScene_DspLogController",
                "LogSwichForScene_CpCapLogController",
                "LogSwichForScene_Cm4LogController",
                "LogSwichForScene_ArmPcmDataController",
                "LogSwichForScene_AGDspLogController",
                "LogSwichForScene_AGDspPcmDumpLogController",
                "LogSwichForScene_AGDspOutputController",
                "LogSwichForScene_DspLogController",
                "LogSwichForScene_DspPcmDataController",
                "LogSwichForScene_WcnLogController",
                "LogSwichForScene_GpsLogController",
                "LogSwichForScene_CpCapLogController",
                "LogSwichForScene_OrcaapController" ,
                "LogSwichForScene_OrcadpController"};

        for (String x : params) {
            if (LogInfo.getInstance().getCustomer(x).equals("1")) {
                LogInfo.getInstance().setCustomer(
                        "LogSwichForScene_EventMonitorController", "0");
                ivEventModeLog.setImageResource(LogInfo.getInstance()
                        .getCustomer("LogSwichForScene_EventMonitorController")
                        .equals("0") ? R.drawable.checkbox_off
                        : R.drawable.checkbox_on);
                break;
            }
        }

    }

    private void syncAGDspLog(String YlogCoreName) {
        Log.d(TAG, "syncAGDspLog");

        if (LogInfo.getInstance().getCustomer("LogSwichForScene_AGDspOutputController").equals("0")
            && LogInfo.getInstance().getCustomer("LogSwichForScene_AGDspPcmDumpLogController").equals("0")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_LOG, "0");
        }else{
            LogInfo.getInstance().setCustomer(LogInfo.KEY_AGDSP_LOG, "1");
        }
        ivAG_DspPcmDumpLog.setImageResource(LogInfo.getInstance().getCustomer(LogInfo.KEY_AGDSP_PCM_DUMP_LOG).equals("0") ? R.drawable.checkbox_off: R.drawable.checkbox_on);
        ivAG_DspLog.setImageResource(LogInfo.getInstance().getCustomer(LogInfo.KEY_AGDSP_OUTPUT_LOG).equals("0") ? R.drawable.checkbox_off: R.drawable.checkbox_on);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.btn_commit:
            mProgressDialog = ProgressDialog.show(UserDefinedActivity.this,
                    getResources().getString(R.string.scene_switching),
                    getResources().getString(R.string.scene_switching_wait),
                    true, false);
            commitCustomerOrderAsync();
            break;

        case R.id.rl_android_log:
            onClickCheckBox(ivAndroidLog, LogInfo.KEY_ANDROID_LOG);
            String arg = LogInfo.getInstance().getCustomer(
                    LogInfo.KEY_ANDROID_LOG);
            LogInfo.getInstance().setCustomer(LogInfo.KEY_KERNAL_LOG, arg);
            break;
        case R.id.rl_tcpip_log:
            // onClickCheckBox(ivTcpipLog,
            // "LogSwichForScene_APCapLogController");
            onClickCheckBox(ivTcpipLog, LogInfo.KEY_CP_CAP_LOG);
            syncModemLog();
            break;
        case R.id.rl_ap_tcpip_log:
            onClickCheckBox(ivAPTcpipLog, LogInfo.KEY_AP_CAP_LOG);
            break;
        case R.id.rl_bt_log:
            onClickCheckBox(ivBtLog, LogInfo.KEY_HCI_LOG);
            break;
        case R.id.rl_modem_log:
            onClickCheckBox(ivModemLog, LogInfo.KEY_ARM_LOG);
            syncDSPLog();
            break;
        case R.id.rl_dsp_log:
            onClickCheckBox(ivDspLog, LogInfo.KEY_DSP_LOG);
            syncPSLog();
            break;

        case R.id.iv_ag_dsp_dump_log:
            onClickCheckBox(ivAG_DspPcmDumpLog, LogInfo.KEY_AGDSP_PCM_DUMP_LOG);
            syncAGDspLog(LogInfo.KEY_AGDSP_PCM_DUMP_LOG);
            break;

        case R.id.rl_ag_dsp_log:
            onClickCheckBox(ivAG_DspLog, LogInfo.KEY_AGDSP_OUTPUT_LOG);
            syncAGDspLog(LogInfo.KEY_AGDSP_OUTPUT_LOG);
            break;

        /* SPRD: Bug 568186 add CM4 log in EngineerMode @{ */
        case R.id.rl_cm4_log:
            onClickCheckBox(ivCm4Log, LogInfo.KEY_CM4_LOG);
            break;
        /* @} */

        /* SPRD: add voice and wcn log in EngineerMode @{ */
        case R.id.rl_dsp_pcm_log:
            onClickCheckBox(ivDspPcmLog, LogInfo.KEY_DSP_PCM_LOG);
            break;

        case R.id.rl_arm_pcm_log:
            onClickCheckBox(ivArmPcmLog, LogInfo.KEY_ARM_PCM_LOG);
            break;
        /* @} */
        case R.id.rl_dsp_more_option:
            if (llMoreDspOption.getVisibility() == View.GONE) {
                ivDspMore.setImageResource(R.drawable.slogui_option_expanded);
                llMoreDspOption.setVisibility(View.VISIBLE);
            } else if (llMoreDspOption.getVisibility() == View.VISIBLE) {
                ivDspMore.setImageResource(R.drawable.slogui_option_collceted);
                llMoreDspOption.setVisibility(View.GONE);
            }
            break;

        case R.id.rl_cp_bt_log:
            onClickCheckBox(ivCpBtLog, LogInfo.KEY_WCN_LOG);
            break;
        case R.id.rl_gps_log:
            onClickCheckBox(ivGpsLog, LogInfo.KEY_GPS_LOG);
            break;

        case R.id.rb_output_uart:

            CPLogControl.getInstance().setDspOutPut(1);
            rbOutputModemLog.setChecked(CPLogControl.getInstance().getDspOutPut() == 2);
            rbOutputUart
                    .setChecked(CPLogControl.getInstance().getDspOutPut() == 1);
            break;
        case R.id.rb_output_modemlog:
            CPLogControl.getInstance().setDspOutPut(2);
            rbOutputModemLog.setChecked(CPLogControl.getInstance().getDspOutPut() == 2);
            rbOutputUart
                    .setChecked(CPLogControl.getInstance().getDspOutPut() == 1);
            break;



        case R.id.rl_eventmonitor_log:
            onClickCheckBox(ivEventModeLog, LogInfo.KEY_EVENT_MONITOR);
            syncEventModemLog();
            break;

            case R.id.rl_orcaap_log:
                onClickCheckBox(ivOrcaAPLog, LogInfo.KEY_MINI_ORCAAP);
                break;
            case R.id.rl_orcadp_log:
                onClickCheckBox(ivOrcaDPLog, LogInfo.KEY_MINI_ORCADP);
            break;
        default:
            break;
        }

        initEventLogStauts();
        if (LogInfo.getInstance().getCustomer(LogInfo.KEY_ARM_LOG).equals("1")) {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "1");
        } else {
            LogInfo.getInstance().setCustomer(LogInfo.KEY_MODEM_LOG, "0");
        }
    }

    private void commitCustomerOrderAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!APLogControl.getInstance().isLogStarted()) {
                            APLogControl.getInstance().setLogStatus(true);
                        }
                        CPLogStorageSwitch.getInstance().restoreCPLogDest(false);
                        // in custom scence, caplog length set 80 byte
                        // YlogInfo.setCustomer("LogSwichForScene_CapLogLengthController",
                        // "" + YlogUtils.getTcpCapSize(80));
                        LogInfo.getInstance().setSceneStatus(
                                SceneStatus.customer);
                        if (mIsEdit
                                && !mCustomName.equals(LogSceneManager
                                        .getInstance()
                                        .getCurrentSelectedSceneName(
                                                UserDefinedActivity.this))) {
                            LogInfo.getInstance().writeData(
                                    UserDefinedActivity.this, mCustomName);
                        } else {
                            LogInfo.getInstance().commitCustomerOrder(
                                    UserDefinedActivity.this, mCustomName);
                        }
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.putExtra(
                                        LogSceneManager.EXTRA_CUSTOM_NAME,
                                        mCustomName);
                                intent.putExtra(
                                        LogSceneManager.EXTRA_CUSTOM_EDIT,
                                        mIsEdit);
                                UserDefinedActivity.this.setResult(RESULT_OK,
                                        intent);
                                UserDefinedActivity.this.finish();
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
                            }
                        }, 500);
                    }
                }).start();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public int getDspOutput() {

        int output = CPLogControl.getInstance().getDspLogStatus();
        return output;
    }

}
