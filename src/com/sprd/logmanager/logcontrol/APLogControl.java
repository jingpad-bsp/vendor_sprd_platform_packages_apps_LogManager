package com.sprd.logmanager.logcontrol;

import java.io.File;

import android.os.Build;
import android.util.Log;

import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.logui.LogInfo;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.StorageUtil;

public class APLogControl extends LogControl {
    public static final String YLOG_SOCKET_NAME = "ylog_cli";
    public static final String YLOG_LITE_SOCKET_NAME = "ylog_cli_lite";
    public static final String YLOG_SOCKET_NAME_HIDL = "ylog_cli_cmd";



    public static final String CLEAR_AP_LOG_CMD = "clear";
    public static final String SET_AP_CAP_SIZE_CMD = "tcpcapsize";

    public static final String SUB_LOG_ANDROID_TYPE = "android";
    public static final String SUB_LOG_KERNEL_TYPE = "kernel";
    public static final String SUB_LOG_TRACE_TYPE = "trace";
    public static final String SUB_LOG_SGM_TYPE = "sgm";
    public static final String SUB_LOG_SYS_INFO_TYPE = "sysinfo";
    public static final String SUB_LOG_YLOG_DEBUG_TYPE = "ylogdebug";
    public static final String SUB_LOG_PHONEINFO_TYPE = "phoneinfo";
    public static final String SUB_LOG_HCI_TYPE = "hcidump";
    public static final String SUB_LOG_TCPDUMP_TYPE = "tcpdump";
    public static final String SUB_LOG_LASTLOG_TYPE = "lastlog";
    public static final String SUB_LOG_UBOOT_TYPE = "uboot";
    public static final String GET_LOG_STATUS_CMD = "get started";
    public static final String ROTATE_LOG_CMD = "rotatelog";
    public static final String SINGLE_FOLDER_SIZE_CMD = "aplogfilesize";
    public static final String AP_TOTAL_SIZE_CMD = "aplogmaxsize";
    public static final String REST_CONFIG_CMD = "resetsettings";
    public static final String CHANGE_STORAGE_PATH_CMD = "sroot";
    public static final String GET_STORAGE_PATH_CMD = "rootdir";
    public static final String GET_TIME_CMD = "time";
    public static final String LOG_LEVLE_CMD = "outloglevel";
    public static final String START_CMD = "op";
    public static final String STOP_CMD = "cl";
    public static final String JOURNAL_CMD = "log2journal";
    public static final String VERSION_CMD = "version";
    public static final String EANBLE_CMD = "enable";
    public static final String DISABLE_CMD = "disable";
    public static final String SELECT_LOGD_DEFAULT_SIZE_PROPERTY = "ro.logd.size";
    public static final String SELECT_LOGD_DEFAULT_SIZE_VALUE = "262144";
    public static final String SELECT_LOGD_SVELTE_DEFAULT_SIZE_VALUE = "65536";

    private static final String TAG = "APLogControl";
    private boolean mGSI = false;

    private static APLogControl sApLogControl = null;

    private APLogControl(String socketName) {
        super(socketName);
        mGSI = CommonUtils.isGSIVersion();


    }

    @Override
    public boolean enableSubLog(String subLogType, boolean enable) {
        // TODO Auto-generated method stub
        String cmd = STOP_CMD;
        if (enable) {
            cmd = START_CMD;
        }
        String result = sendCmd( cmd+ " "+subLogType  ,true);
        boolean res = checkResult(result);
        return res;
    }

    @Override
    public boolean setLogStatus(boolean start) {
        // TODO Auto-generated method stub
        String enable = "0";
        String status = "running";
        boolean res = false;
        int maxCount = 20;
        if (start) {
            enable = "1";
        }else {
            status = "stopped";
            CPLogStorageSwitch.getInstance().saveCplogDest();
        }
        boolean ret=false;
        if (enable.equals("1")){
            ret=enableAPLog();
        }else{
            ret=disableAPLog();
        }
        if (!ret) {
            return false;
        }
        LogManagerPreference.getInstanse().setLogEnable(start);
        return true;
    }

    public boolean enableAPLog() {
        Log.d(TAG, "ap log will set enable");
        String ret=sendCmd(EANBLE_CMD, true);
        Log.d(TAG, "ap log  enable result:"+ret);
        return ret.contains("enable");
    }

    public boolean disableAPLog() {
        Log.d(TAG, "ap log will set disable");
        String ret=sendCmd(DISABLE_CMD, true);
        Log.d(TAG, "ap log  disable result:"+ret);
         return ret.contains("disable");
    }

    public boolean setPropThroughHidl(String prop, String value) {
        String result = sendCmd("setprop " + prop + " " + value);
        boolean res = checkResult(result);
        return res;
    }

    public String getPropThroughHidl(String prop) {
        String result = sendCmd("getprop " + prop);
        if (result != null) {
            String[] temp = result.split("=");
            if (temp.length == 1) {
                return null;
            } else {
                return temp[1].trim();
            }
        }
        return null;
    }

    public static APLogControl getInstance() {
        String socketName = YLOG_SOCKET_NAME;
        boolean useHidl = (Build.VERSION.SDK_INT >= 28);
        boolean newProp = true;//(Build.VERSION.SDK_INT > 28);
        boolean gsi = CommonUtils.isGSIVersion();
        if (useHidl) {
            socketName = YLOG_SOCKET_NAME_HIDL;
        }
        if (gsi) {
            socketName = YLOG_LITE_SOCKET_NAME;
        }

        if (sApLogControl == null) {
            sApLogControl = new APLogControl(socketName);
        }
        return sApLogControl;
    }

    @Override
    public boolean clearLog() {
        // TODO Auto-generated method stub
        String string = sendCmd(CLEAR_AP_LOG_CMD, true);
        return true;
    }

    @Override
    public boolean setCapSize(long size) {
        // TODO Auto-generated method stub
        String result = sendCmd(SET_AP_CAP_SIZE_CMD + " " + size, true);
        if (result.contains("" + size)) {
            LogManagerPreference.getInstanse().setTcpCapSize(size);
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkResult(String string) {
        // TODO Auto-generated method stub
        boolean res = false;
        if (string.contains("running") || string.contains("stopped")
                || string.contains("1")) {
            res = true;
        }
        return res;
    }

    @Override
    protected boolean isLogOverwrite(String subLogType) {
        // TODO Auto-generated method stub
        String result = sendCmd(ROTATE_LOG_CMD, true);
        if (result != null && result.contains("disable")) {
            return false;
        }
        return true;
    }

    public boolean isLogOverwrite() {
        // TODO Auto-generated method stub
        boolean res = isLogOverwrite(null);
        return res;
    }

    @Override
    protected boolean enableLogOverwrite(String subLogType, boolean enable) {
        // TODO Auto-generated method stub
        String value = "disable";
        if (enable) {
            value = "enable";
        }
        String result = sendCmd(ROTATE_LOG_CMD + " " + value, true);
        if (result != null && (result.contains("0") || result.contains("1"))) {
            return true;
        }
        return false;
    }

    public boolean enableLogOverwrite(boolean enable) {
        // TODO Auto-generated method stub
        boolean res = enableLogOverwrite(null, enable);
        return res;
    }

    @Override
    protected boolean getSubLogStatus(String subLogType) {
        // TODO Auto-generated method stub
        boolean res = false;
        String strTmp = sendCmd(subLogType + " " + GET_LOG_STATUS_CMD, true);
        if (strTmp.contains("1")) {
            res = true;
        }
        return res;
    }

    @Override
    public boolean setLogLevel(int level) {
        // TODO Auto-generated method stub
        LogManagerPreference.getInstanse().setApLogLevel(level);
        String result = sendCmd(LOG_LEVLE_CMD+ " " + level, true);
        if (result != null && result.contains("" + level)) {
            return true;
        }
        return false;
    }

    public String getLogSavePath() {
        String strTmp = "data";
        try {
            strTmp = sendCmd(CHANGE_STORAGE_PATH_CMD, true);
            String[] str = strTmp.split("\n");
            if(str.length>0){
                strTmp = str[0].trim();
            }else{
                strTmp = "";
            }
            if (strTmp.equals("")) {
                strTmp = sendCmd(GET_STORAGE_PATH_CMD, true);
                str = strTmp.split("\n");
                strTmp = str[0].trim();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if(strTmp.contains("failed")){
            return "";
        }
        return strTmp ;
    }

    public String getLogVersion() {
         String strTmp = sendCmd(VERSION_CMD, true);
        if(strTmp!=null){
                String[]   str = strTmp.split("\n");
                strTmp = str[0].trim();
            }else{
                strTmp="ERRVER";
            }
        return strTmp;

    }

    @Override
    public int getLogLevel() {
        // TODO Auto-generated method stub
        int logLevel = -1;
        String result = sendCmd(LOG_LEVLE_CMD, true);
        try {
            logLevel = Integer.parseInt(result.substring(0, 1));
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "get ap log level error", e);
            logLevel = -1;
        }
        if (logLevel < 0) {
            Log.w(TAG, "get log level from ylog error,use preference");
            logLevel = LogManagerPreference.getInstanse().getApLogLevel();
        }
        return logLevel;
    }

    public String defaultLogdSizeValue() {
        String defaultValue = getPropThroughHidl(SELECT_LOGD_DEFAULT_SIZE_PROPERTY);
        if ((defaultValue == null) || (defaultValue.length() == 0)) {
            String lowRaw=getPropThroughHidl("ro.config.low_ram");
            if ((lowRaw!=null)&&(lowRaw.equals("true"))) {
                defaultValue = SELECT_LOGD_SVELTE_DEFAULT_SIZE_VALUE;
            } else {
                defaultValue = SELECT_LOGD_DEFAULT_SIZE_VALUE;
            }
        }
        return defaultValue;
    }

    @Override
    public long getCapSize() {
        // TODO Auto-generated method stub
        long capSize = -1;
        String result = sendCmd(SET_AP_CAP_SIZE_CMD, true);
        try {
            String size = result.split("\\n")[0];
            capSize = Long.parseLong(size.substring(size.indexOf(" ")+1));
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "get ap cap size error,get it in another way", e);
        }

        if (capSize < 0) {
            int defaultSize = LogInfo.getInstance().getDefautApCapSize();
            capSize = LogManagerPreference.getInstanse().getTcpCapSize(defaultSize);
        }
        return capSize;
    }

    public long getSingleFolderSize() {
        long size = -1;
        String result = sendCmd(SINGLE_FOLDER_SIZE_CMD, true);
        String str[] = result.split("\\n");
        String logSize = str[0];
        try {
            if (logSize.contains(".")) {
                size = Long.parseLong(logSize.substring(0, logSize.indexOf(".")));
            }else {
                size = Long.parseLong(logSize);
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "get ap single folder size error", e);
        }

        return size;
    }

    public long getApTotalSize() {
        long totalSize = -1;
        String result = sendCmd(AP_TOTAL_SIZE_CMD, true);
        try {
            String size = result.split("\\n")[0];
            totalSize = Long.parseLong(size);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "get ap total size error", e);
        }

        return totalSize;
    }

    public boolean setSingleFolderSize(long size) {
        // TODO Auto-generated method stub
        String result = sendCmd(SINGLE_FOLDER_SIZE_CMD + " " + size, true);
        if (result.contains("" + size)) {
            return true;
        }
        return false;
    }

    public boolean setApTotalSize(long size) {
        // TODO Auto-generated method stub
        String result = sendCmd(AP_TOTAL_SIZE_CMD + " " + size, true);
        if (result.contains("" + size)) {
            return true;
        }
        return false;
    }

    public boolean isLogStarted() {
        //always anr ,use preference
        return LogManagerPreference.getInstanse().isLogEnable();
//        String result = PropUtils.getString(YLOG_STATUS_PROP, "stopped");
//        Log.d(TAG, "isLogStarted result = " + result);
//        /* SRPD bug 818804:ylog state wrong after reset by notification */
//        if (result.equals("running") || result.equals("restarting")) {
//            return true;
//        }
//        return false;
    }

    // clear ylog.conf
    public boolean reset() {
        sendCmd(REST_CONFIG_CMD, true);
        enableAPLog();
        //setLogLevel(0);
        //setSingleFolderSize(500);
        //setApTotalSize(4096);
        //enableLogOverwrite(true);
        return true;
    }

    public String sendCmd(String cmd, boolean needApend) {
        // TODO Auto-generated method stub
        if (needApend) {
            cmd = cmd + "\n";
        }
        String result = super.sendCmd(cmd);
        //mSocketUtils.closeSocket();
        return result;
    }

    @Override
    public boolean setStorageInSd(boolean external) {
        // TODO Auto-generated method stub
        String cmd = CHANGE_STORAGE_PATH_CMD + " "
                + StorageUtil.getExternalStorage();
        if (!external) {
            cmd = CHANGE_STORAGE_PATH_CMD + " "
                    + StorageUtil.DEFAULT_INTERNAL_PATH;
        }
        String response = sendCmd(cmd, true);
        Log.i(TAG, "set Storage path is :" + response);
        if(response.contains("failed")){
            return false;
        }
        return true;

    }

    public String getLogTime() {
        String strTmp = sendCmd(GET_TIME_CMD, true);
        if (strTmp != null)
            strTmp = strTmp.trim();
        return strTmp;
    }

    public String print2journal(String log) {
        //String strTmp = sendCmd(JOURNAL_CMD+" "+log, true);
        //if (strTmp != null)
        //  strTmp = strTmp.trim();
        return null;
    }
}
