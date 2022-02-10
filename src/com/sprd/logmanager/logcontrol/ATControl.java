package com.sprd.logmanager.logcontrol;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.util.Log;
import android.telephony.TelephonyManager;

import com.sprd.logmanager.LogManagerApplication;
import com.sprd.logmanager.utils.MiscUtils;
import com.sprd.logmanager.utils.SocketUtils;
import com.sprd.logmanager.logcontrol.APLogControl;

public class ATControl {
    private static final int OPEN_CP2 = 0;
    public static final int GET_ARM_LOG = 1;
    public static final int SET_ARM_LOG_OPEN = 2;
    public static final int SET_ARM_LOG_CLOSE = 3;
    public static final int GET_DSP_LOG = 4;
    public static final int SET_DSP_LOG = 5;
    private static final int GET_CAP_LOG = 6;
    public static final int SET_CAP_LOG_OPEN = 7;
    public static final int SET_CAP_LOG_CLOSE = 8;
    private static final int GET_AUDIO_LOG = 9;
    private static final int SET_AUDIO_LOG_OPEN = 10;
    private static final int SET_AUDIO_LOG_CLOSE = 11;
    private static final int GET_CP2_LOG = 12;
    private static final int SET_CP2_LOG_OPEN = 13;
    private static final int SET_CP2_LOG_CLOSE = 14;
    private static final int MEMORY_LEAK = 15;
    private static final int SET_LOG_SCENARIOS_STATUS = 16;
    private static final int SET_LOG_OUTPUT_STYLE = 17;
    private static final int SET_SAVE_SLEEPLOG = 18;
    private static final int SET_SAVE_RINGBUF = 19;
    private static final int GET_ENABLE_DUMP_MARLIN = 20;
    private static final int SET_ENABLE_DUMP_MARLIN_OPEN = 21;
    private static final int SET_ENABLE_DUMP_MARLIN_CLOSE = 22;
    private static final int SET_DUMP_MARLIN_MEM = 23;

    private static final String TAG = "ATControl";
    public static String AT_FAIL = "AT FAILED";
    public static String AT_OK = "OK";
    public static String AT_CONNECT = "CONNECT";
    public static String AT_NOT_SUPPORT = "ERROR: 4";

    public static String sendAt(final String cmd, final String serverName) {
        Log.i(TAG, "send at cmd : " + cmd);
        if (!CPLogControl.getInstance().isModemAlive()) {
            Log.d(TAG, "modem at not Aviable, return");
            return AT_FAIL;
        }

        // here we fix AT blocking time, we return AT fail after 2 seconds
        // later.
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        Callable<String> call = new Callable<String>() {
            public String call() throws Exception {
                String strTmp = sendATCmd(cmd, serverName);
                APLogControl.getInstance().print2journal("AT "+serverName+":"+cmd+":"+strTmp);
                return strTmp;
            }
        };
        String futureObj = null;
        try {
            Future<String> future = exec.submit(call);
            futureObj = future.get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Log.d(TAG, "modem at timeout", ex);
            return AT_FAIL;
        } catch (Exception e) {
            Log.d(TAG, "modem at exception", e);
            return AT_FAIL;
        }
        exec.shutdown();
        return futureObj != null ? futureObj : AT_FAIL;
    }

    public static String analysisResponse(String response, int type) {
        Log.d(TAG, "analysisResponse response= " + response + "type = " + type);
        if (response != null && response.contains(ATControl.AT_OK)) {
            if (type == GET_ARM_LOG || type == GET_CAP_LOG
                    || type == GET_DSP_LOG) {
                String[] str = response.split("\n");
                String[] str1 = str[0].split(":");
                Log.d(TAG, type + "  " + str1[1]);
                return str1[1].trim();
            } else if (type == SET_ARM_LOG_CLOSE || type == SET_ARM_LOG_OPEN
                    || type == SET_CAP_LOG_CLOSE || type == SET_CAP_LOG_OPEN
                    || type == SET_AUDIO_LOG_CLOSE
                    || type == SET_AUDIO_LOG_OPEN || type == SET_DSP_LOG) {
                return ATControl.AT_OK;
            }
        }

        if (type == GET_CP2_LOG || type == SET_CP2_LOG_OPEN
                || type == SET_CP2_LOG_CLOSE) {
            if (response != null && !response.startsWith("Fail")) {
                if (type == GET_CP2_LOG) {
                    if (response.contains("FAIL")) {
                        return ATControl.AT_FAIL;
                    } else {
                        String[] str1 = response.split(":");
                        Log.d(TAG, type + "  " + str1[1]);
                        return str1[1].trim();
                    }
                } else if (type == SET_CP2_LOG_OPEN
                        || type == SET_CP2_LOG_CLOSE) {
                    return ATControl.AT_OK;
                }
            }
        }
        return ATControl.AT_FAIL;

    }

    /**
     * send AT cmd to modem.
     * 
     * @param cmd
     *            :specific AT commands to be sent.
     * @param serverName
     *            :phoneId.
     * @return at command return value.
     * @hide
     */
    public static synchronized String sendATCmd(String cmd, String serverName) {
        Log.d(TAG, "begin sendATCmdto " + serverName + " , and cmd = " + cmd);
        String strTmp = "error service can't get";
        int mPhoneCount = LogManagerApplication.getTelephonyManager()
                .getPhoneCount();
        int phoneId = 0;
        if (mPhoneCount == 1) {
            Log.d(TAG, "phone count is 1");
            serverName = "atchannel";
        }
        if (serverName.contains("atchannel0")) {
            phoneId = 0;
            Log.d(TAG, "<0> mAtChannel = " + phoneId + " , and cmd = " + cmd);
        } else if (serverName.contains("atchannel1")) {
            phoneId = 1;
            Log.d(TAG, "<1> mAtChannel = " + phoneId + " , and cmd = " + cmd);
        } else {
            phoneId = 0;
            Log.d(TAG, "<atchannel> mAtChannel = " + phoneId + " , and cmd = "
                    + cmd);
        }
        strTmp = MiscUtils.sendAt(phoneId + " " + cmd);
        if (serverName.contains("atchannel0")) {
            Log.d(TAG, "<0> AT response " + strTmp);
        } else if (serverName.contains("atchannel1")) {
            Log.d(TAG, "<1> AT response " + strTmp);
        } else {
            Log.d(TAG, "<atchannel> AT response " + strTmp);
        }
        Log.d(TAG, "end sendATCmdto " + serverName + " , and cmd = " + cmd);
        return strTmp;
    }
}
