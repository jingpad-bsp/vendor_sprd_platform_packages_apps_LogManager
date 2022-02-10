package com.sprd.logmanager.logcontrol;
import android.util.Log;

public class LogSwichForScene {

    private static final String TAG = "LogSwichForScene";
    APLogControl mApLogControl;
    CPLogControl mCpLogControl;
    WcnControl mWcnControl;

    public LogSwichForScene() {
        mApLogControl = APLogControl.getInstance();
        mCpLogControl = CPLogControl.getInstance();
        mWcnControl = WcnControl.getInstance();
    }

    // pass
    public boolean LogSwichForScene_KernelLogController(String arg) {
        boolean enable = arg.equals("1");
        Log.d(TAG, String.format("kernellog want to %s",
                arg.equals("1") ? "open" : "close"));
        boolean res = mApLogControl.enableSubLog(
                APLogControl.SUB_LOG_KERNEL_TYPE, enable);
        return res;
    }

    public boolean LogSwichForScene_AndroidLogController(String arg) {
        boolean enable = arg.equals("1");
        Log.d(TAG, String.format("andorid log want to %s",
                arg.equals("1") ? "open" : "close"));
        mApLogControl.enableSubLog(APLogControl.SUB_LOG_ANDROID_TYPE, enable);
        mApLogControl.enableSubLog(APLogControl.SUB_LOG_TRACE_TYPE, enable);
        mApLogControl.enableSubLog(APLogControl.SUB_LOG_SGM_TYPE, enable);
        mApLogControl.enableSubLog(APLogControl.SUB_LOG_SYS_INFO_TYPE, enable);
        mApLogControl
                .enableSubLog(APLogControl.SUB_LOG_YLOG_DEBUG_TYPE, enable);
        mApLogControl
                .enableSubLog(APLogControl.SUB_LOG_PHONEINFO_TYPE, enable);
        mApLogControl
                .enableSubLog(APLogControl.SUB_LOG_LASTLOG_TYPE, enable);
        mApLogControl
                .enableSubLog(APLogControl.SUB_LOG_UBOOT_TYPE, enable);
        return true;
    }

    // pass
    public boolean LogSwichForScene_BtHciLogController(String arg) {
        boolean enable = arg.equals("1");
        Log.d(TAG, String.format("hci want to %s", arg.equals("1") ? "open"
                : "close"));
        boolean res = mApLogControl.enableSubLog(APLogControl.SUB_LOG_HCI_TYPE,
                enable);
        return res;

    }

    // pass
    public boolean LogSwichForScene_APCapLogController(String arg) {
        boolean enable = arg.equals("1");
        Log.d(TAG, String.format("apcaplog want to %s",
                arg.equals("1") ? "open" : "close"));
        boolean res = mApLogControl.enableSubLog(
                APLogControl.SUB_LOG_TCPDUMP_TYPE, enable);
        return res;
    }

    // pass
    public boolean LogSwichForScene_CapLogLengthController(String arg) {

        Log.d(TAG, String.format("ap caplog want to set size %s", arg));
        try {
            long size = Long.parseLong(arg);
            mApLogControl.setCapSize(size);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    // pass
    public boolean LogSwichForScene_ModemLogController(String arg) {
        boolean start = arg.equals("1");
        Log.d(TAG, String.format("modemlog want to %s",
                arg.equals("1") ? "open" : "close"));
        boolean res = mCpLogControl.enableSubLog(CPLogControl.SUB_LOG_5MODE_TYPE, start);
        return res;
    }

    public boolean LogSwichForScene_DspLogController(String arg) {
        boolean enable = !arg.equals("0");
        boolean res = mCpLogControl.enableDspLog(enable);
        return res;
    }

    public boolean LogSwichForScene_AGDspLogController(String arg) {
        boolean enable = arg.equals("1");
        boolean res = mCpLogControl.enableAgDsp(enable);
        return res;
    }

    public boolean LogSwichForScene_Cm4LogController(String arg) {
        boolean enable = arg.equals("1");
        boolean res = mCpLogControl.enableSubLog(
                CPLogControl.SUB_LOG_AG_PM_SH_TYPE, enable);
        return res;
    }

    public boolean LogSwichForScene_AGDspPcmDumpLogController(String arg) {
        boolean enable = !arg.equals("0");
        boolean res = mCpLogControl.enableAGDspPcmDumpLog(enable);
        return res;
    }

    public boolean LogSwichForScene_AGDspOutputController(String arg) {
        try {
            int output = Integer.parseInt(arg);
            mCpLogControl.setAAGDspOutputStatus(output);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "LogSwichForScene_AGDspOutputController fail ,arg is "
                    + arg + ",and exception " + e);
            return false;
        }
        return true;
    }

    // pass
    public boolean LogSwichForScene_WcnLogController(String arg) {

        Log.d(TAG, String.format("btlog want to %s", arg.equals("1") ? "open"
                : "close"));
        boolean res = mWcnControl.enbleWcnLog(arg.equals("1"));
        return res;
    }

    public boolean LogSwichForScene_GpsLogController(String arg) {
        boolean enable = arg.equals("1");
        boolean res = mCpLogControl.enableSubLog(
                CPLogControl.SUB_LOG_GNSS_TYPE, enable);
        return res;
    }

    public boolean LogSwichForScene_CpCapLogController(String arg) {

        boolean enable = arg.equals("1");
        boolean res = mCpLogControl.enableCap(enable);
        return res;
    }

    public boolean LogSwichForScene_ArmLogController(String arg) {

        boolean res = mCpLogControl.setArmLog(arg);
        return res;

    }

    public boolean LogSwichForScene_EventMonitorController(String arg) {

        boolean res = mCpLogControl.enableEventMonitor(arg.equals("1"));
        return res;

    }
    public boolean LogSwichForScene_OrcaapController(String arg) {

        boolean res = mCpLogControl.enableOrcaap(arg.equals("1"));
        return res;

    }
    public boolean LogSwichForScene_OrcadpController(String arg) {

        boolean res = mCpLogControl.enableOrcadp(arg.equals("1"));
        return res;

    }
    public boolean LogSwichForScene_DspPcmDataController(String arg) {
        boolean res = mCpLogControl.enableDspPcmData(arg.equals("1"));
        return res;
    }

    public boolean LogSwichForScene_ArmPcmDataController(String arg) {
        boolean res = mCpLogControl.enableArmPcmData(arg.equals("1"));
        return res;
    }
}
