package com.sprd.logmanager.logcontrol;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.media.AudioManager;
import android.content.Context;

import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPControl;
import com.sprd.logmanager.utils.StorageUtil;

public class CPLogControl extends LogControl {
    static final String TAG = "CPLogControl";
    public static final String SLOGMODEM_SOCKET_NAME = "slogmodem";
    public static final String ENABLE_LOG_CMD = "ENABLE_LOG";
    public static final String DISABLE_LOG_CMD = "DISABLE_LOG";
    public static final String GET_LOG_OVERWRITE_CMD = "GET_LOG_OVERWRITE";
    public static final String ENABLE_LOG_OVERWRITE_CMD = "ENABLE_LOG_OVERWRITE";
    public static final String DISABLE_LOG_OVERWRITE_CMD = "DISABLE_LOG_OVERWRITE";
    public static final String GET_LOG_STATE_CMD = "GET_LOG_STATE";
    public static final String SET_AGDSP_PCM_OUTPUT_CMD = "SET_AGDSP_PCM_OUTPUT";
    public static final String SET_AGDSP_LOG_OUTPUT_CMD = "SET_AGDSP_LOG_OUTPUT";
    public static final String CLEAR_LOG_CMD = "slogctl clear\n";
    public static final String SAVE_SLEEP_LOG_CMD = "SAVE_SLEEP_LOG";
    public static final String SAVE_BUFFER_LOG_CMD = "SAVE_RINGBUF";
    public static final String GET_LOG_FILE_SIZE_CMD = "GET_LOG_FILE_SIZE";
    public static final String GET_CP_TOTAL_LOG_SIZE_CMD = "GET_CP_LOG_SIZE";
    public static final String SET_LOG_FILE_SIZE_CMD = "SET_LOG_FILE_SIZE";
    public static final String SET_CP_TOTAL_LOG_SIZE_CMD = "SET_CP_LOG_SIZE";
    public static final String SET_STORAGE_CHOICE_CMD = "SET_STORAGE_CHOICE";
    public static final String COLLECT_LOG_CMD = "COLLECT_LOG";
    public static String RESET_SETTING_CMD = "RESET_SETTING";

    public static final String OK = "OK";
    public static final String FAIL = "FAIL";
    public static final String ENABLE = "ENABLE";
    public static final String DISABLE = "DISABLE";

    public static final String SUB_LOG_WCDMA_TYPE = "WCDMA";
    public static final String SUB_LOG_TD_TYPE = "TD";
    public static final String SUB_LOG_WCN_TYPE = "WCN";
    public static final String SUB_LOG_TDD_LTE_TYPE = "TDD-LTE";
    public static final String SUB_LOG_FDD_LTE_TYPE = "FDD-LTE";
    public static final String SUB_LOG_5MODE_TYPE = "5MODE";
    public static final String SUB_LOG_GNSS_TYPE = "GNSS";
    public static final String SUB_LOG_AG_DSP_TYPE = "AG-DSP";
    public static final String SUB_LOG_AG_PM_SH_TYPE = "PM_SH";

    public static final String SUB_LOG_ORCA_AP = "ORCAAP";
    public static final String SUB_LOG_ORCA_DP = "ORCADP";

    public static String MODEM_RO_W_PROP = "ro.vendor.modem.w.enable";
    public static String MODEM_PERSIST_W_PROP = "persist.vendor.modem.w.enable";
    public static String MODEM_RO_T_PROP = "ro.vendor.modem.t.enable";
    public static String MODEM_PERSIST_T_PROP = "persist.vendor.modem.t.enable";
    public static String MODEM_RO_WCN_PROP = "ro.vendor.modem.wcn.enable";
    public static String MODEM_PERSIST_TL_PROP = "persist.vendor.modem.tl.enable";
    public static String MODEM_PERSIST_LF_PROP = "persist.vendor.modem.lf.enable";
    public static String MODEM_PERSIST_L_PROP = "persist.vendor.modem.l.enable";
    public static String MODEM_PERSIST_R_PROP = "persist.vendor.modem.nr.enable";
    public static String MODEM_RESET_PROP = "persist.vendor.sys.modemreset";
    public static String MODEM_SAVEDUMP_PROP = "persist.vendor.sys.modem.save_dump";
    public static String RIL_SERVICE_PROP = "init.svc.vendor.ril-daemon";
    public static String AGDSP_ENABLE_PROP = "persist.vendor.sys.ag.enable";
    public static String AGDSP_LOG_PROP = "ro.vendor.modem.ag.log";

    private CPLogStorageSwitch mCPLogStorageSwitch;
    private CPControl mCPcontrol;
    private int mDspOption = 2;
    private static CPLogControl sCpLogControl = null;

    private CPLogControl(String socketName) {
        super(socketName);
        mCPLogStorageSwitch = CPLogStorageSwitch.getInstance();
        mCPcontrol = CPControl.getInstance();
    }

    public boolean isCP0Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_RO_W_PROP, false)
                || PropUtils.getBoolean(MODEM_PERSIST_W_PROP, false);
        return enable;
    }

    public boolean isCP1Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_RO_T_PROP, false)
                || PropUtils.getBoolean(MODEM_PERSIST_T_PROP, false);
        return enable;
    }

    public boolean isCP2Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_RO_WCN_PROP, false);
        return enable;
    }

    public boolean isCP3Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_PERSIST_TL_PROP, false);
        return enable;
    }

    public boolean isCP4Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_PERSIST_LF_PROP, false);
        return enable;
    }

    public boolean isCP5Enable() {
        boolean enable = PropUtils.getBoolean(MODEM_PERSIST_L_PROP, false) || PropUtils.getBoolean(MODEM_PERSIST_R_PROP, false);
        return enable;
    }

    @Override
    protected boolean enableSubLog(String subLogType, boolean enable) {
        // TODO Auto-generated method stub
        String cmd = DISABLE_LOG_CMD + " " + subLogType;
        if (enable) {
            cmd = ENABLE_LOG_CMD + " " + subLogType;
        }
        Log.d(TAG, String.format("%s want to %s", subLogType, enable ? "open"
                : "close"));
        String strResult = sendCmd(cmd);
        print2journal(cmd, strResult);

        return true;
    }

    public boolean enableAgDsp(boolean enable) {
        // TODO Auto-generated method stub
        if (CPLogStorageSwitch.getInstance().isLogToPc(
                CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
            Log.i(TAG, "modem log to pc ,nothing to do");
            return true;
        }
        enableSubLog(CPLogControl.SUB_LOG_AG_DSP_TYPE, enable);
        return true;
    }

    public boolean enableAGDspPcmDumpLog(boolean enable) {
        String status = "OFF";
        String response = "";
        if (enable) {
            status = "ON";
        }
        Log.d(TAG, String.format("AG-DSP pcm dump want to %s", enable ? "close"
                : "open"));
        response = sendCmd(SET_AGDSP_PCM_OUTPUT_CMD + " " + status);
        print2journal(SET_AGDSP_PCM_OUTPUT_CMD + " " + status, response);
        boolean res = checkResult(response);
        return res;
    }

    public boolean setAAGDspOutputStatus(int status) {
        String response = "";
        String cmd = null;
        if (status == 0) {
            cmd = "SET_AGDSP_LOG_OUTPUT OFF";
        } else  {
            cmd = "SET_AGDSP_LOG_OUTPUT USB";
        }
        Log.d(TAG, String.format("set ag-dsp output to %s", cmd));
        if (cmd != null) {
            response = sendCmd(cmd);
            print2journal(cmd, response);
        }
        boolean res = checkResult(response);
        return res;
    }

    @Override
    public boolean setLogStatus(boolean start) {
        // TODO Auto-generated method stub
        String atValue = "0";
        if (start) {
            atValue = "1";
        }
        //String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SETARMLOG1
          //      + atValue, "atchannel0");
        //Log.i(TAG, "startLog send at and response is " + atResponse);
        mCPcontrol.enableArmLog(start);
        boolean log2Pc = mCPLogStorageSwitch
                .isLogToPc(CPLogStorageSwitch.MOEDEM_LOG_TYPE);
        if (log2Pc) {
            Log.d(TAG, "modem log is to pc,do nothing for start cp log");
            return false;
        }
        if (isCP0Enable()) {
            enableSubLog(SUB_LOG_WCDMA_TYPE, start);
        }
        if (isCP1Enable()) {
            enableSubLog(SUB_LOG_TD_TYPE, start);
        }
        if (isCP2Enable()) {
            enableSubLog(SUB_LOG_WCN_TYPE, start);
        }
        if (isCP3Enable()) {
            enableSubLog(SUB_LOG_TDD_LTE_TYPE, start);
        }
        if (isCP4Enable()) {
            enableSubLog(SUB_LOG_FDD_LTE_TYPE, start);
        }
        if (isCP5Enable()) {
            enableSubLog(SUB_LOG_5MODE_TYPE, start);
        }
        return true;
    }

    @Override
    public boolean clearLog() {
        // TODO Auto-generated method stub
        Log.d(TAG, "begin clearLog ");
        String strTmp = sendCmd(CLEAR_LOG_CMD);
        Log.d(TAG, "clearLog finished:%s"+strTmp);
        print2journal(CLEAR_LOG_CMD, strTmp);
        Log.d(TAG, "clearLog print2journal done");
        if (strTmp.contains(OK)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean setCapSize(long size) {
        // TODO Auto-generated method stub
        boolean res = false;
        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOGLENGTH
                + size, "atchannel0");
        Log.d(TAG, "setCapSize response: " + atResponse);
        res = checkResult(atResponse);
        return res;
    }

    @Override
    protected boolean checkResult(String string) {
        // TODO Auto-generated method stub
        boolean res = false;
        if (string.contains(OK)) {
            res = true;
        }
        return res;
    }

    public static CPLogControl getInstance() {
        if (sCpLogControl == null) {
            sCpLogControl = new CPLogControl(SLOGMODEM_SOCKET_NAME);
        }
        if (Build.VERSION.SDK_INT < 28) {
            MODEM_RO_W_PROP = "ro.modem.w.enable";
            MODEM_PERSIST_W_PROP = "persist.modem.w.enable";
            MODEM_RO_T_PROP = "ro.modem.t.enable";
            MODEM_PERSIST_T_PROP = "persist.modem.t.enable";
            MODEM_RO_WCN_PROP = "ro.modem.wcn.enable";
            MODEM_PERSIST_TL_PROP = "persist.modem.tl.enable";
            MODEM_PERSIST_LF_PROP = "persist.modem.lf.enable";
            MODEM_PERSIST_L_PROP = "persist.modem.l.enable";
            MODEM_RESET_PROP = "persist.sys.modemreset";
            RIL_SERVICE_PROP = "init.svc.ril-daemon";
            AGDSP_ENABLE_PROP = "persist.sys.ag.enable";
            AGDSP_LOG_PROP = "ro.modem.ag.log";
        }
        return sCpLogControl;
    }

    public boolean isModemATAvaliable() {
        boolean res = waitRildReady(10);
        if (res) {
            Log.i(TAG, "rild ready ,can send at commond");
        } else {
            Log.i(TAG, "rild not ready ,can't send at commond");
        }
        return res;
    }

    @Override
    protected boolean isLogOverwrite(String subLogType) {
        // TODO Auto-generated method stub
        Log.d(TAG, "get modem log overwrite status");
        String response = null;
        boolean res = false;
        response = sendCmd(GET_LOG_OVERWRITE_CMD + " " + subLogType);
        if (response != null && response.contains(OK)) {
            String str1[] = response.split("\\n");
            String str2[] = str1[0].split("\\s+");
            if (str2[1].contains("ENABLE")) {
                res = true;
            } else {
                res = false;
            }
        }
        return res;
    }

    @Override
    protected boolean enableLogOverwrite(String subLogType, boolean enable) {
        Log.d(TAG, "set log overwrite status " + enable + ",and type is "
                + subLogType);
        String response = null;
        String cmd = DISABLE_LOG_OVERWRITE_CMD;
        boolean res = false;
        if (enable) {
            cmd = ENABLE_LOG_OVERWRITE_CMD;
        }
        response = sendCmd(cmd + " " + subLogType);
        print2journal(cmd + " " + subLogType, response);
        res = checkResult(response);
        return res;
    }

    public boolean isWcnLogOverwrite() {
        // TODO Auto-generated method stub
        Log.d(TAG, "get wcn log overwrite status");
        boolean res = isLogOverwrite(SUB_LOG_WCN_TYPE);
        return res;
    }

    public boolean enableWcnLogOverwrite(boolean enable) {
        boolean res = enableLogOverwrite(SUB_LOG_WCN_TYPE, enable);
        return res;
    }

    public boolean isGnssLogOverwrite() {
        // TODO Auto-generated method stub
        Log.d(TAG, "get gnss log overwrite status");
        boolean res = isLogOverwrite(SUB_LOG_GNSS_TYPE);
        return res;
    }

    public boolean enableGnssLogOverwrite(boolean enable) {
        boolean res = enableLogOverwrite(SUB_LOG_GNSS_TYPE, enable);
        return res;
    }

    public boolean isModemLogOverwrite() {
        Log.d(TAG, "get cp log overwrite status");
        boolean res = false;
        if (isCP0Enable()) {
            res = isLogOverwrite(SUB_LOG_WCDMA_TYPE);
        }
        if (isCP1Enable()) {
            res = isLogOverwrite(SUB_LOG_TD_TYPE);
        }
        if (isCP3Enable()) {
            res = isLogOverwrite(SUB_LOG_TDD_LTE_TYPE);
        }
        if (isCP4Enable()) {
            res = isLogOverwrite(SUB_LOG_FDD_LTE_TYPE);
        }
        if (isCP5Enable()) {
            res = isLogOverwrite(SUB_LOG_5MODE_TYPE);
        }
        return res;
    }

    public boolean enableModemLogOverwrite(boolean enable) {
        Log.d(TAG, "enableModemLogOverwrite " + enable);
        boolean res = false;
        if (isCP0Enable()) {
            res = enableLogOverwrite(SUB_LOG_WCDMA_TYPE, enable);
        }
        if (isCP1Enable()) {
            res = enableLogOverwrite(SUB_LOG_TD_TYPE, enable);
        }
        if (isCP3Enable()) {
            res = enableLogOverwrite(SUB_LOG_TDD_LTE_TYPE, enable);
        }
        if (isCP4Enable()) {
            res = enableLogOverwrite(SUB_LOG_FDD_LTE_TYPE, enable);
        }
        if (isCP5Enable()) {
            res = enableLogOverwrite(SUB_LOG_5MODE_TYPE, enable);
        }
        return res;
    }

    @Override
    public boolean getSubLogStatus(String subLogType) {
        // TODO Auto-generated method stub
        String response = sendCmd(GET_LOG_STATE_CMD + " " + subLogType);
        Log.d(TAG, "the result of GET_LOG_STATE: " + subLogType + " "
                + response);
        if (response != null && response.contains(ATControl.AT_OK)) {
            if (response.contains("ON"))
                return true;
        }
        return false;
    }

    @Override
    public boolean setLogLevel(int level) {
        // TODO Auto-generated method stub
        //String result = ATControl.sendAt(ATCommand.ENG_SET_LOG_LEVEL + "1,"
        //        + level, "atchannel0");
       // Log.d(TAG, "setLogLevel result:" + result);
        mCPcontrol.setLogLevel(level);
        return true;
    }

    public boolean setArmLog(String atValue) {
        Log.d(TAG, String.format("armlog want to be set to :%s", atValue));
        String responValue = ATControl.AT_FAIL;
        //String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SETARMLOG1
        //        + atValue, "atchannel0");
        if (atValue.equals("1")) {
            mCPcontrol.enableArmLog(true);
            mCPcontrol.enableEvent(false);
            if (CommonUtils.isUserBuild()) {
                setLogLevel(3);
            }
           // responValue = ATControl.analysisResponse(atResponse,
                //    ATControl.SET_ARM_LOG_OPEN);
        } else if (atValue.equals("0")) {
            mCPcontrol.enableArmLog(false);
            mCPcontrol.enableEvent(false);
            if (CommonUtils.isUserBuild()) {
               //setLogLevel(0);
            }
           // responValue = ATControl.analysisResponse(atResponse,
                    //ATControl.SET_ARM_LOG_CLOSE);
        }
        else if (atValue.equals("2")) {
            mCPcontrol.enableArmLog(false);
            mCPcontrol.enableEvent(true);
           // responValue = ATControl.analysisResponse(atResponse,
                    //ATControl.SET_ARM_LOG_CLOSE);
        }
        /* @} */
       // boolean res = checkResult(responValue);
        return true;
    }

    public boolean getArmLogStatus() {
        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_GETARMLOG1,
                "atchannel0");
        String responValue = ATControl.analysisResponse(atResponse,
                ATControl.GET_ARM_LOG);
        if (atResponse.contains(ATControl.AT_OK)) {
            if (responValue.trim().equals("1")) {
                return true;
            }
        }
        return false;
    }

    public void enableAllCPLogToOutput(boolean enable) {
        if (enable) {
            ATControl.sendAt(ATCommand.ENG_SET_LOG_LEVEL+ ATCommand.LOG_DEBUG_CMD, "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETARMLOG1 + "1", "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOG1 + "1", "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETDSPLOG1 + "2", "atchannel0");
        } else {
            ATControl.sendAt(ATCommand.ENG_SET_LOG_LEVEL+ ATCommand.LOG_USER_CMD, "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETARMLOG1 + "0", "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOG1 + "0", "atchannel0");
            ATControl.sendAt(ATCommand.ENG_AT_SETDSPLOG1 + "0", "atchannel0");
            ATControl.sendAt(ATCommand.ENG_SET_LOG_LEVEL + "1,0,\"\",\"\"",
                    "atchannel0");
        }
    }

    public boolean enableDspLog(boolean enable) {
        Log.d(TAG,
                String.format("dsplog want to %s", enable ? "open" : "close"));
        if (enable){
            if (mDspOption == 1){
                mCPcontrol.enableDsp(enable,"UART");
            }else {
                mCPcontrol.enableDsp(enable,"AP");
            }
        }else {
            mCPcontrol.enableDsp(enable,"");
        }
        return  true;
    }

    public void setDspOutPut(int output) {
        mDspOption = output;
    }

    public int getDspOutPut() {
        return mDspOption;
    }

    public int getDspLogStatus() {
        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_GETDSPLOG1,
                "atchannel0");
        String responValue = ATControl.analysisResponse(atResponse,
                ATControl.GET_DSP_LOG);
        if (atResponse.contains(ATControl.AT_OK)) {
            if (responValue.trim().equals("1")) {
                mDspOption = 1;
            } else if (responValue.trim().equals("2")) {
                mDspOption = 2;
            }
        }
        return mDspOption;
    }

    public boolean enableEventMonitor(boolean enable) {
        Log.d(TAG, String.format("EventMonitor want to %s", enable ? "close"
                : "open"));
        //mCPcontrol.enableEvent(enable);
        String cmd = "";
        if (enable) {
            cmd = "ENABLE_EVT_LOG 5MODE";
            String response = sendCmd(cmd);
            print2journal(cmd, response);
        }
        return true;
    }

    public boolean enableOrcaap(boolean enable) {
        Log.d(TAG, String.format("orca ap want to %s", enable ? "open": "close"));
        mCPcontrol.enableOrcaap(enable);
        enableSubLog(SUB_LOG_ORCA_AP, enable);
        return true;
    }
    public boolean enableOrcadp(boolean enable) {
        Log.d(TAG, String.format("orca dp want to %s", enable ? "open": "close"));
        mCPcontrol.enableOrcadp(enable);
        enableSubLog(SUB_LOG_ORCA_DP, enable);
        return true;
    }

    public boolean enableDspPcmData(boolean enable) {
        Log.d(TAG, String.format("dsppcmdata want to %s", enable ? "open"
                : "close"));
        //String responValue = ATControl.AT_FAIL;
        //if (enable)
        //    responValue = ATControl.sendAt(ATCommand.ENG_SET_DSP_OPEN,
        //            "atchannel0");
        //else
        //    responValue = ATControl.sendAt(ATCommand.ENG_SET_DSP_CLOSE,
        //            "atchannel0");

        //if (responValue.contains(ATControl.AT_OK)) {
        //    return true;
        //}
        mCPcontrol.enableDspPcmLog(enable);
        return true;
    }

    public boolean enableArmPcmData(boolean enable) {
//        Log.d(TAG, String.format("armpcmdata want to %s", enable ? "open"
//                : "close"));
//        String atValueString = "0";
//        if (enable) {
//            atValueString = "1";
//        }
//        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SET_SPPCMDUMP
//                + atValueString + ",1,1,15", "atchannel0");
//
//        if (atResponse.contains(ATControl.AT_OK)) {
//            return true;
//        }
//        return false;
        mCPcontrol.enableArmPcm(enable);
        return true;
    }

    public boolean enableSimLog(boolean enable) {
        Log.d(TAG,
                String.format("simlog want to %s", enable ? "open" : "close"));
        String atValueString = "0";
        if (enable) {
            atValueString = "1";
        }
        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SET_SIM_TRACE
                + atValueString, "atchannel0");

        if (atResponse.contains(ATControl.AT_OK)) {
            return true;
        }
        return false;
    }

    public void sendModemAssert() {
        Log.d(TAG, "set at cmd to dump modem assert");
        ATControl.sendAt(ATCommand.ENG_AT_SET_MANUAL_ASSERT, "atchannel0");
    }

    public int saveSleepLog() {
        String subLogType = SUB_LOG_5MODE_TYPE;
        if (isCP0Enable()) {
            subLogType = SUB_LOG_WCDMA_TYPE;
        }
        if (isCP1Enable()) {
            subLogType = SUB_LOG_TD_TYPE;
        }
        if (isCP3Enable()) {
            subLogType = SUB_LOG_TDD_LTE_TYPE;
        }
        if (isCP4Enable()) {
            subLogType = SUB_LOG_FDD_LTE_TYPE;
        }
        if (isCP5Enable()) {
            subLogType = SUB_LOG_5MODE_TYPE;
        }
        Log.d(TAG, "save sleeplog  supportmode:" + subLogType);
        String strTmp = sendCmd(SAVE_SLEEP_LOG_CMD + " " + subLogType);
        if (strTmp == null || strTmp.equals("")) {
            return -1;
        }
        if (strTmp != null && strTmp.contains("ERROR")) {
            String[] str = strTmp.split("\n");
            String[] str1 = str[0].trim().split("\\s+");
            String str2 = str1[1];
            Log.d(TAG, "Error is " + str2);
            return Integer.parseInt(str2);
        }

        return 0;
    }

    public int saveLogBuf() {
        String subLogType = "";
        if (isCP0Enable()) {
            subLogType = SUB_LOG_WCDMA_TYPE;
        }
        if (isCP1Enable()) {
            subLogType = SUB_LOG_TD_TYPE;
        }
        if (isCP3Enable()) {
            subLogType = SUB_LOG_TDD_LTE_TYPE;
        }
        if (isCP4Enable()) {
            subLogType = SUB_LOG_FDD_LTE_TYPE;
        }
        if (isCP5Enable()) {
            subLogType = SUB_LOG_5MODE_TYPE;
        }
        Log.d(TAG, "save logbuf supportmode:" + subLogType);
        String strTmp = sendCmd(SAVE_BUFFER_LOG_CMD + " " + subLogType);
        if (strTmp.contains("ERROR")) {
            String[] str = strTmp.split("\n");
            String[] str1 = str[0].trim().split("\\s+");
            String str2 = str1[1];
            Log.d(TAG, "Error is " + str2);
            return Integer.parseInt(str2);
        }
        return 0;
    }

    @Override
    public int getLogLevel() {
        int loglevel = -1;
        String result = ATControl.sendAt(ATCommand.ENG_GET_LOG_LEVEL,
                "atchannel0");
        Log.d(TAG, "getLogLevel result:" + result);
        try {
            loglevel = Integer.parseInt(result.substring(
                    result.lastIndexOf("LEVEL") + 7, result.indexOf(",")));
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "get cp log level error", e);
        }
        return loglevel;
    }

    @Override
    public long getCapSize() {
        long capSize = -1;
        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_GETCAPLOGLENGTH,
                "atchannel0");
        Log.d(TAG, ATCommand.ENG_AT_GETCAPLOGLENGTH + ": " + atResponse);
        if (atResponse != null && atResponse.contains(ATControl.AT_OK)) {
            try {
                String str1[] = atResponse.split("\\n");
                String str2[] = str1[0].split(":");
                final String capLogLength = str2[1];
                Log.d(TAG, "capLogLength: " + capLogLength);
                capSize = Long.parseLong(capLogLength.trim());
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(TAG, "get cp cap size error", e);
            }
        }
        return capSize;
    }

    public long getSingleFileSize(String subLogType) {
        long size = -1;
        String cmd = GET_LOG_FILE_SIZE_CMD + " " + subLogType;
        String response = sendCmd(cmd);
        Log.d(TAG, cmd + ": " + response);
        if (response != null && response.contains(OK)) {
            String str1[] = response.split("\\n");
            String str2[] = str1[0].split("\\s+");
            String logSize = str2[1];
            try {
                size = Long.parseLong(logSize);
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(TAG, "get single File Size error type is :" + subLogType,
                        e);
            }
        }
        return size;
    }

    public boolean setSingleFileSize(String subLogType, long size) {
        String cmd = SET_LOG_FILE_SIZE_CMD + " " + subLogType + " " + size;
        String response = sendCmd(cmd);
        Log.d(TAG, cmd + ": " + response);
        print2journal(cmd, response);
        if (response != null && response.contains(OK)) {
            return true;
        }
        return false;
    }

    public long getTotalCpLogSize(String subLogType, boolean isInternal) {
        long size = -1;
        String cmd = GET_CP_TOTAL_LOG_SIZE_CMD + " " + subLogType + " external";
        if (isInternal) {
            cmd = GET_CP_TOTAL_LOG_SIZE_CMD + " " + subLogType + " internal";
        }
        String response = sendCmd(cmd);
        Log.d(TAG, cmd + ": " + response);
        if (response != null && response.contains(OK)) {
            String str1[] = response.split("\\n");
            String str2[] = str1[0].split("\\s+");
            String logSize = str2[1];
            try {
                size = Long.parseLong(logSize);
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(TAG, "get single File Size error type is :" + subLogType,
                        e);
            }
        }
        return size;
    }

    public boolean setTotalCpLogSize(String subLogType, boolean isInternal,
            long size) {
        String cmd = SET_CP_TOTAL_LOG_SIZE_CMD + " " + subLogType
                + " external " + size;
        if (isInternal) {
            cmd = SET_CP_TOTAL_LOG_SIZE_CMD + " " + subLogType + " internal "
                    + size;
        }
        String response = sendCmd(cmd);
        Log.d(TAG, cmd + ": " + response);
        print2journal(cmd, response);
        if (response != null && response.contains(OK)) {
            return true;
        }
        return false;
    }

    public int getETBMode() {
        int etbStatus = -1;
        String result = ATControl
                .sendAt(ATCommand.ENG_AT_GET_ETB, "atchannel0");
        if (result.contains(ATControl.AT_OK) && result.contains("1,")) {
            etbStatus = 1;
        } else if (result.contains(ATControl.AT_OK) && result.contains("0,")) {
            etbStatus = 0;
        }
        return etbStatus;
    }

    public boolean enableETBMode(boolean etbEnable) {
//        String atValue = "0,0";
//        if (etbEnable) {
//            atValue = "1,0";
//        }
//        String result = ATControl.sendAt(ATCommand.ENG_AT_SET_ETB + atValue,
//                "atchannel0");
//        if (result.contains(ATControl.AT_OK)) {
//            return true;
//        }
//        return false;
        mCPcontrol.enableEtb(etbEnable);
        return true;
    }

    public boolean reset() {
        if (CommonUtils.isUserBuild()) {
            PropUtils.setProp(MODEM_RESET_PROP, "1");
        } else {
            PropUtils.setProp(MODEM_RESET_PROP, "0");
        }
        String response = sendCmd(RESET_SETTING_CMD);
        print2journal(RESET_SETTING_CMD, response);
        // enableModemLogOverwrite(true);
        // enableWcnLogOverwrite(true);
        // enableGnssLogOverwrite(true);
        // if key don't exist, we don't need to reset
        ATControl.sendAt(ATCommand.ENG_AT_SET_ETB + "1,0", "atchannel0");
        ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOGLENGTH + "10000",
                "atchannel0");
        ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOGLENGTH + "10000",
                "atchannel0");
        ATControl.sendAt(ATCommand.ENG_SET_LOG_LEVEL + "1,3", "atchannel0");
        return true;
    }

    public void setModemReset(boolean enable) {
        if (enable) {
            PropUtils.setProp(MODEM_RESET_PROP, "1");
        } else {
            PropUtils.setProp(MODEM_RESET_PROP, "0");
        }
    }

    public void setModemSaveDump(boolean enable) {
        if (enable) {
            PropUtils.setProp(MODEM_SAVEDUMP_PROP, "1");
        } else {
            PropUtils.setProp(MODEM_SAVEDUMP_PROP, "0");
        }
    }

    public boolean isModemReset() {
        boolean isReset = PropUtils.getBoolean(MODEM_RESET_PROP, false);
        return isReset;
    }

    public boolean isModemSaveDump() {
        boolean isReset = PropUtils.getBoolean(MODEM_SAVEDUMP_PROP, false);
        return isReset;
    }

    public boolean waitRildReady(long timeout) {
        long startTime = System.currentTimeMillis();
        long endTime = -1;
        do {
            boolean isRunning = PropUtils
                    .getString(RIL_SERVICE_PROP, "stopped").contains("run");
            if (isRunning) {
                return true;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                endTime = System.currentTimeMillis();
            }
        } while (endTime - startTime < timeout * 1000);

        return PropUtils.getString(RIL_SERVICE_PROP, "stopped").contains("run");
    }

    @Override
    public boolean setStorageInSd(boolean external) {
        // TODO Auto-generated method stub
        String cmd = SET_STORAGE_CHOICE_CMD + " INTERNAL\n";
        if (external) {
            cmd = SET_STORAGE_CHOICE_CMD + " EXTERNAL\n";
        }
        String response = sendCmd(cmd);
        print2journal(cmd, response);
        return true;
    }

    public void collectLog(String subLogType) {
        String strTmp = sendCmd(COLLECT_LOG_CMD + " " + subLogType);
        Log.i(TAG, "collectLog " + subLogType + ", result :" + strTmp);
    }

    public boolean enableCap(boolean enable) {
//        Log.d(TAG,
//                String.format("caplog want to %s", enable ? "open" : "close"));
//        String responValue = ATControl.AT_FAIL;
//        String atValue = "1";
//        if (!enable) {
//            atValue = "0";
//        }
//        String atResponse = ATControl.sendAt(ATCommand.ENG_AT_SETCAPLOG1
//                + atValue, "atchannel0");
//
//        if (enable) {
//            responValue = ATControl.analysisResponse(atResponse,
//                    ATControl.SET_CAP_LOG_OPEN);
//        } else {
//            responValue = ATControl.analysisResponse(atResponse,
//                    ATControl.SET_CAP_LOG_CLOSE);
//        }
//
//        return responValue.contains(ATControl.AT_OK);
        mCPcontrol.enableCap(enable);
        return  true;
    }

    public void collectModemLog() {
        String subLogType = "";
        if (isCP0Enable()) {
            subLogType = SUB_LOG_WCDMA_TYPE;
        }
        if (isCP1Enable()) {
            subLogType = SUB_LOG_TD_TYPE;
        }
        if (isCP3Enable()) {
            subLogType = SUB_LOG_TDD_LTE_TYPE;
        }
        if (isCP4Enable()) {
            subLogType = SUB_LOG_FDD_LTE_TYPE;
        }
        if (isCP5Enable()) {
            subLogType = SUB_LOG_5MODE_TYPE;
        }
        String strTmp = sendCmd(COLLECT_LOG_CMD + " " + subLogType);
        Log.i(TAG, "collectLog " + subLogType + ", result :" + strTmp);
    }

    public boolean isSupportAGDSP() {
        AudioManager audioManager;
        audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        String ret = audioManager.getParameters("isAudioDspExist");
        Log.i(TAG, "isAudioDspExist =" + ret);
        return ret.indexOf("=1") != -1;
    }

    private void print2journal(String cmd, String result) {
        APLogControl.getInstance()
                .print2journal("CPCMD  " + cmd + ":" + result);
    }

    public boolean isModemAlive() {
        boolean noRil = PropUtils.getBoolean("ro.radio.noril",false);
        Log.i(TAG, "noRil = " + noRil);
        if(noRil){
            return false;
        }
        String result = ATControl.sendATCmd(ATCommand.ENG_AT_CGMR, "atchannel0");
        if (result!= null && result.contains(ATControl.AT_OK)) {
            return true;
        } else {
            return false;
        }
    }
}
