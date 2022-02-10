package com.sprd.logmanager.utils;

import android.util.Log;

public class MiscUtils {
    public static String MISC_SOCKET_NAME = "miscserver";
    public static final String GET_SDCARD_STATE = "getSdState";
    public static final String GET_SDCARD_PATH = "getSdPath";
    public static final String SET_VENDOR_PROP = "setProp";
    public static final String SEND_AT = "sendAt";
    static final String TAG = "MiscUtils";

    public static void setProp(String prop, String value) {
        SocketUtils socketUtils = new SocketUtils(MISC_SOCKET_NAME);
        String response = socketUtils.sendCmdAndRecResult(SET_VENDOR_PROP + " "
                + prop + " " + value);
        Log.i(TAG, "use miscserver to setProp:" + prop + "to value: " + value
                + ",result : " + response);
        socketUtils.closeSocket();
    }

    public static String getSdState() {
        SocketUtils socketUtils = new SocketUtils(MISC_SOCKET_NAME);
        String response = socketUtils.sendCmdAndRecResult(GET_SDCARD_STATE);
        Log.i(TAG, "use miscserver to getSdState result : " + response);
        socketUtils.closeSocket();
        return response;
    }

    public static String getSdPath() {
        SocketUtils socketUtils = new SocketUtils(MISC_SOCKET_NAME);
        String response = socketUtils.sendCmdAndRecResult(GET_SDCARD_PATH);
        Log.i(TAG, "use miscserver to getSdPath result : " + response);
        socketUtils.closeSocket();
        return response;
    }

    public static String sendAt(String cmd) {
        SocketUtils socketUtils = new SocketUtils(MISC_SOCKET_NAME);
        String response = socketUtils.sendCmdAndRecResult(SEND_AT + " " + cmd);
        Log.i(TAG, "use miscserver to sendAt result : " + response);
        socketUtils.closeSocket();
        return response;
    }
}
