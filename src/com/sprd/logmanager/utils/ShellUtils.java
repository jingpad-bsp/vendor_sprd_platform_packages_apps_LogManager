package com.sprd.logmanager.utils;

import com.sprd.logmanager.logcontrol.APLogControl;

import android.os.Build;
import android.util.Log;

public class ShellUtils {
    static final String TAG = "ShellUtils";
    public static final String CMD_SERVICE_ENABLE_PROP = "persist.sys.cmdservice.enable";
    public static final String GET_CMD_SERVICE_RUNNING_PROP = "init.svc.cmd_services";
    private static final String PROCESS_NAME = "cmd_skt ";
    static boolean useHidl = (Build.VERSION.SDK_INT >= 27);

    public static synchronized String runCommand(String cmd) {
        Log.d(TAG, "run cmd:" + cmd);
        SocketUtils socketUtils = null;
        String tempCmdString = cmd;
        String socketName = PROCESS_NAME;
        if (useHidl) {
            socketName = APLogControl.YLOG_SOCKET_NAME_HIDL;
            tempCmdString = PROCESS_NAME + cmd;
        }
        boolean enable = enableCmdservice(true);
        String result = null;
        if (enable) {
            socketUtils = new SocketUtils(socketName);
            result = socketUtils.sendCmdAndRecResult(tempCmdString);
        } else {
            Log.e(TAG, "cmd_services is not run");
        }
        enableCmdservice(false);
        return result;
    }

    public static synchronized String runCommand2(String cmd) {
        Log.d(TAG, "run cmd:" + cmd);
        SocketUtils socketUtils = null;
        String tempCmdString = cmd;
        String socketName = PROCESS_NAME;
        if (useHidl) {
            socketName = APLogControl.YLOG_SOCKET_NAME_HIDL;
            tempCmdString = PROCESS_NAME + cmd;
        }
        String result = null;
        socketUtils = new SocketUtils(socketName);
        result = socketUtils.sendCmdAndRecResult(tempCmdString);
        return result;
    }

    public static boolean enableCmdservice(boolean enable) {
        String control = "enable";
        String status = "running";
        boolean res = false;
        int maxCount = 20;
        if (!enable) {
            control = "disable";
            status = "stopped";
        }
        Log.i(TAG, "enableCmdservice :" + enable);
        PropUtils.setProp(CMD_SERVICE_ENABLE_PROP, control);
        while (!res && maxCount-- > 0) {
            String result = PropUtils.getString(GET_CMD_SERVICE_RUNNING_PROP,
                    "error");
            Log.d(TAG, "isCmdserviceOpen result = " + result + ",prop is "
                    + PropUtils.getString(CMD_SERVICE_ENABLE_PROP, "error"));
            if (result.equals(status)) {
                res = true;
                break;
            } else {
                res = false;
                PropUtils.setProp(CMD_SERVICE_ENABLE_PROP, control);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }
}