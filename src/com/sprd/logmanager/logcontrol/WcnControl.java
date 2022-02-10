package com.sprd.logmanager.logcontrol;

import java.util.Calendar;

import android.os.Build;
import android.util.Log;

import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;
import com.sprd.logmanager.utils.SocketUtils;

public class WcnControl {
    public static final String WCND_SOCKET_NAME = "wcnd";
    public static final String WCN_POWER_ON_CMD = "wcn poweron";
    public static final String WCN_POWER_OFF_CMD = "wcn poweroff";
    public static final String WCN_AT_LOG_CMD = "wcn at+armlog=";
    public static final String WCN_AT_TIME_CMD = "wcn at+aptime=";
    public static final String WCN_AT_SET_LOG_LEVEL_CMD = "wcn at+loglevel=";
    public static final String WCN_AT_GET_LOG_LEVEL_CMD = "wcn at+loglevel?";
    public static final String WCN_DUMP_ENABLE_CMD = "wcn dump_enable";
    public static final String WCN_DUMP_DISABLE_CMD = "wcn dump_disable";
    public static final String MANUAL_CP2_ASSERT_CMD = "wcn at+spatassert=1\r";
    public static final String WCN_DUMP_MEM_CMD = "wcn dumpmem";
    public static final String WCN_GET_DUMP_CMD = "wcn dump?";
    public static final String FLUSH_LOG_CMD = "wcn at+flushwcnlog";
    public static String WCN_REST_PROP = "persist.vendor.sys.wcnreset";
    public static String WCN_ENABLE_PROP = "ro.vendor.modem.wcn.enable";
    public static String WCN_PRODUCT_PROP = "ro.vendor.wcn.hardware.product";
    public static  String WCN_LOG_LEVEL_PROP = "persist.vendor.sys.loglevel";
    public static final String OK = "OK";
    private SocketUtils mSocketUtils;
    private CPLogControl mCpLogControl;
    static WcnControl sControl;
    static final String TAG = "WcnControl";

    private WcnControl() {
        mSocketUtils = new SocketUtils(WCND_SOCKET_NAME);
        mCpLogControl = CPLogControl.getInstance();
    }

    public static WcnControl getInstance() {
        if (sControl == null) {
            sControl = new WcnControl();
        }
        if (Build.VERSION.SDK_INT < 28) {
            WCN_REST_PROP = "persist.sys.wcnreset";
            WCN_ENABLE_PROP = "ro.modem.wcn.enable";
            WCN_PRODUCT_PROP = "ro.wcn.hardware.product";
        }
        return sControl;
    }

    public boolean enbleWcnLog(boolean enable) {
        boolean res = false;
        String atValue = "0";
        if (enable) {
            atValue = "1";
        }
        Log.d(TAG,
                String.format("wcn log want to %s", enable ? "open" : "close"));
        boolean logToPC = CPLogStorageSwitch.getInstance().isLogToPc(
                CPLogStorageSwitch.WCN_LOG_TYPE);
        if (logToPC) {
            Log.d(TAG, "wcn log is to pc,do nothing");
            return true;
        }
        String strTmp = sendCmd2(WCN_AT_LOG_CMD + atValue);
        Log.d(TAG, "at+armlog=" + strTmp);
        if (enable) {
            Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);
            int millisecond = c.get(Calendar.MILLISECOND);
            String cmd = WCN_AT_TIME_CMD + month + "," + day + "," + hour + ","
                    + minute + "," + second + "," + millisecond + "\r";
            strTmp = sendCmd2(cmd);
            Log.d(TAG, "set wcn log time result: " + strTmp);
        }
        if (CPLogControl.getInstance().isCP2Enable()) {
            res = mCpLogControl.enableSubLog(CPLogControl.SUB_LOG_WCN_TYPE,
                    enable);
        }
        return true;
    }

    public String sendCmd2(String cmd) {
        return sendCmd3(cmd,true);
    }


    public String sendCmd3(String cmd,boolean sendPowerCmd) {
        synchronized (this) {
            String result = "";
            String response = "";
            if (CPLogControl.getInstance().isCP2Enable()) {
                if(sendPowerCmd){
                result = sendCmd(WCN_POWER_ON_CMD);
                Log.d(TAG, "wcn power on: " + result);
                }
                response = sendCmd(cmd);
                Log.d(TAG, "wcn send " + cmd + " result :" + response);
                if(sendPowerCmd){
                result = sendCmd(WCN_POWER_OFF_CMD);
                Log.d(TAG, "wcn power off: " + result);
                }
            }
            return response;
        }
    }

    private String sendCmd(String cmd) {
        String result = mSocketUtils.sendCmdAndRecResult(cmd);
        if (result == null) {
            result = "";
        }
        return result;
    }

    public boolean setLogLevel(int level) {
        String result = sendCmd3(WCN_AT_SET_LOG_LEVEL_CMD + level,true);
        if (result != null && result.contains(OK)) {
            return true;
        }
        return false;
    }

    public int getLogLevel() {
        int logLevel = -1;

        String result = sendCmd3(WCN_AT_GET_LOG_LEVEL_CMD,false);
        Log.d(TAG, "getLogLevel result2:" + result);
        if (result != null && result.contains("LOGLEVEL")) {
            int index = result.indexOf("LOGLEVEL:");
            if (index != -1) {
                try {
                    logLevel = Integer.parseInt(result.substring(index + 10,
                            index + 11));
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e(TAG, "get wcn log level error", e);
                }
            }
        }

        return logLevel;
    }

    public void dumpWcnMem() {
        boolean isUser = CommonUtils.isUserBuild();
        if (isUser) {
            sendCmd2(WCN_DUMP_ENABLE_CMD);
        }
        String strTmp = sendCmd2(WCN_DUMP_MEM_CMD);
        Log.d(TAG, "dumpwcnMem response:" + strTmp);
    }

    public boolean getMarlinStatus() {
        String response = sendCmd2(WCN_GET_DUMP_CMD);
        Log.d(TAG, "getMarlinStatus response:" + response);
        if (response != null && response.contains(ATControl.AT_OK)) {
            if (response.contains("1")) {
                return true;
            }
        }
        return false;
    }

    public boolean enableMarlinDump(boolean enable) {
        String cmd = WCN_DUMP_ENABLE_CMD;
        if (!enable) {
            cmd = WCN_DUMP_DISABLE_CMD;
        }
        String response = sendCmd2(cmd);
        Log.d(TAG, "enableMarlinDump atResponse:" + response);
        if (response != null && response.contains(OK)) {
            return true;
        }
        return false;
    }

    public String manualAssert() {
        String cmd = MANUAL_CP2_ASSERT_CMD;
        String response = sendCmd2(cmd);
        Log.d(TAG, "manualAssert atResponse:" + response);
        return response;
    }

    public void reset() {
        if (CommonUtils.isUserBuild()) {
            PropUtils.setProp(WCN_REST_PROP, "1");
        } else {
            PropUtils.setProp(WCN_REST_PROP, "0");
        }
        enableMarlinDump(false);
        setLogLevel(3);
    }

    public void setWcnReset(boolean enable) {
        if (enable) {
            PropUtils.setProp(WCN_REST_PROP, "1");
        } else {
            PropUtils.setProp(WCN_REST_PROP, "0");
        }
    }

    public boolean isWcnReset() {
        boolean reset = PropUtils.getBoolean(WCN_REST_PROP, false);
        return reset;
    }

    public void flashWcnLog() {
        sendCmd2(FLUSH_LOG_CMD);
    }

    public String getWcnProduct() {
        return PropUtils.getProp(WCN_PRODUCT_PROP);
    }
}
