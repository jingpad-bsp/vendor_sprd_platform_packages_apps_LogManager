package com.sprd.logmanager.logcontrol;

import android.content.Context;

import com.sprd.logmanager.utils.SocketUtils;

abstract public class LogControl {
    private String mSokcetName;
    protected Context mContext;
    protected SocketUtils mSocketUtils;

    public LogControl(String socketName) {
        mSocketUtils = new SocketUtils(socketName);

    }

    public void setContext(Context context){
        mContext = context;
    }
    protected abstract boolean enableSubLog(String subLogType, boolean enable);

    protected abstract boolean setLogStatus(boolean start);

    protected abstract boolean clearLog();

    protected abstract boolean setCapSize(long size);

    protected abstract long getCapSize();

    protected abstract boolean checkResult(String string);

    protected abstract boolean isLogOverwrite(String subLogType);

    protected abstract boolean enableLogOverwrite(String subLogType,
            boolean enable);

    protected abstract boolean getSubLogStatus(String subLogType);

    protected abstract boolean setLogLevel(int level);

    protected abstract int getLogLevel();
    protected abstract boolean setStorageInSd(boolean external);
    protected abstract boolean reset();
    public synchronized String sendCmd(String cmd) {
        // TODO Auto-generated method stub
        String response = mSocketUtils.sendCmdAndRecResult(cmd);
        if (response == null) {
            response = "";
        }
        return response;
    }
}
