package com.sprd.logmanager.utils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.OutputStream;
import java.io.InputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import android.util.Log;
import com.sprd.logmanager.LogManagerApplication;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SocketUtils {

    public static final String TAG = "LogManagerSocketUtils";
    public static final String OK = "OK";
    public static final String FAIL = "FAIL";

    private String mSocketName = null;
    private LocalSocket mSocketClient = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private LocalSocketAddress mSocketAddress;

    public SocketUtils(String socketName) {
        mSocketName = socketName;
        Log.d(TAG, " mSocketName is " + mSocketName);
    }

    public String sendCmdAndRecResultNoRetry(String strcmd_param) {
        Thread thread = Thread.currentThread();
        String hash_tid = "[" + Integer.toHexString(this.hashCode()) + "]["
                + thread.getId() + "] ";
        String strcmd = hash_tid + "<" + strcmd_param.replace('\n', '\\') + ">";
        Log.d(TAG, mSocketName + " send cmd: " + strcmd);
        byte[] buf = new byte[255];
        int retryCount = 5;
        String result = null;
        if (mOutputStream == null || mInputStream == null
                || mSocketClient == null || !mSocketClient.isConnected()) {
            if (mSocketClient != null) {
                Log.i(TAG, strcmd + "disconnect with server,close socket first");
                closeSocket();
            }
            mSocketClient = new LocalSocket();
            mSocketAddress = new LocalSocketAddress(mSocketName,
                    LocalSocketAddress.Namespace.ABSTRACT);
            for (int i = 0; i < retryCount; i++) {
                try {
                    mSocketClient.connect(mSocketAddress);
                    if (mSocketClient.isConnected()) {
                        break;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.w(TAG, strcmd + "connect " + mSocketName + " for " + i
                            + " times failed", e);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            if (mSocketClient.isConnected()) {
                Log.i(TAG, strcmd + "connect " + mSocketName + " success");
                try {
                    mOutputStream = mSocketClient.getOutputStream();
                    mInputStream = mSocketClient.getInputStream();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.w(TAG, strcmd
                            + "getOutputStream or getInputStream failed", e);
                    try {
                        mSocketClient.close();
                        mSocketClient = null;
                        mOutputStream = null;
                        mInputStream = null;
                    } catch (IOException e1) {
                        Log.d(TAG, strcmd
                                + "getStream error, catch exception is " + e);
                    }
                    return null;
                }
            } else {
                Log.w(TAG, strcmd + "connect failed");
                mSocketClient = null;
                return null;
            }
        }
        Watchdog wd = null;
        try {
            final StringBuilder cmdBuilder = new StringBuilder(strcmd_param)
                    .append('\0');
            final String cmd = cmdBuilder.toString();
            mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            Log.d(TAG, strcmd + " write done , flush data next");
            mOutputStream.flush();
            Log.d(TAG, strcmd + " result read beg...");

            wd = new Watchdog(mSocketName, strcmd_param);
            wd.wantEat();
            int count = mInputStream.read(buf, 0, 255);
            if(1==wd.trigger){
                Log.d(TAG, strcmd + " result read timeout");
                return null;
            }
            wd.feedFood();
            Log.d(TAG, strcmd + " result read done");
            result = "read count is -1";
            if (count != -1) {
                byte[] temp = new byte[count];
                System.arraycopy(buf, 0, temp, 0, count);
                result = new String(temp, "utf-8");
            }
            Log.d(TAG, strcmd + "count = " + count + ", result is " + result);
        } catch (IOException e) {
            Log.e(TAG,
                    strcmd + "send cmd error or read result error"
                            + e.toString());
            if (wd != null) {
                wd.feedFood();
            }
            closeSocket();
            return null;
        }
        Log.d(TAG, strcmd + "handle over and result is :" + result);
        return result;
    }

    public String sendCmdAndRecResult(String strcmd) {
        int retryCount = 3;
        while (retryCount-- != 0) {
            String tmp = sendCmdAndRecResultNoRetry(strcmd);
            if (tmp != null) {
                return tmp;
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            Log.d(TAG, "send cmd :" + strcmd + "error ,try again " + retryCount);
        }
        return null;
    }

    public void closeSocket() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mInputStream != null) {
                mInputStream.close();
                mOutputStream = null;
            }
            if (mSocketClient != null) {
                mSocketClient.close();
                mSocketClient = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "closeSocket error, catch exception is " + e);
        }
    }
}


class Watchdog {
    public static final String TAG = "SocketW";
    private volatile int food = 0;
    private String server = "SERVER";
    private String cmd = "CMD";
    public int trigger=0;
    final int WATCHDOG_TIME=180;
    Thread mThread=null;
    private String logDate="";
    public Watchdog(String s, String c) {
        server = s;
        cmd = c;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
         Date date = new Date(System.currentTimeMillis());
         logDate=sdf.format(date);
    }

    void wantEat() {
        if(null==cmd){
             Log.d(TAG,   "invalid watching  " +server+" ["+cmd+"]");
             return;
        }
        food = 0;
        mThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count=0;
                    while(count<WATCHDOG_TIME){
                        if (1 == food){
                            mThread=null;
                            return;
                         }
                        Thread.sleep(1* 1000);
                        count++;
                    }
                } catch (InterruptedException e) {
                    return;
                }

                if (0 == food) {
                    try {
                        if ((cmd != null)
                                && (cmd.indexOf("apdumper") != -1
                                        || cmd.contains("tar")
                                        || cmd.contains("rm -rf") || cmd
                                            .contains("rylogr")|| cmd.contains("clear"))) {
                            return;
                        }
                        if (null == LogManagerApplication.getInstance().getCurrentActivity()) {
                            return;
                        }

                        trigger=1;

                        ((Activity) LogManagerApplication.getInstance().getCurrentActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                         Log.d(TAG,   "watchdog triggered " +server+" ["+cmd+"] with "+WATCHDOG_TIME);
                                        (new DialogCaller()).showDialog(server +" "+cmd+ " error "+logDate ,"["+cmd+"] Blocked!"+ "\n click OK to EXIT",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog,int which) {
                                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                            }
                                                        });
                                    }
                        });
                    } catch (Exception e) {
                        Log.w(TAG, "wantEat error" + e);
                    }
                }
            }
        });
        mThread.start();
    }

    void feedFood() {
        food = 1;
        try{
            if(null!=mThread){
                mThread.interrupt();
            }
         } catch (Exception e) {
            Log.w(SocketUtils.TAG, "feedFood error" + e);

         }
    }

class DialogCaller {
        public void showDialog(String title, String message,
                DialogInterface.OnClickListener onClickListener) {
             if (null == LogManagerApplication.getInstance().getCurrentActivity()) {
                 return;
             }
             if (LogManagerApplication.getInstance().getCurrentActivity().isFinishing()||LogManagerApplication.getInstance().getCurrentActivity().isDestroyed()) {
                 Log.w(TAG, "activity is not run,not show dialog");
                 return;
             }
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    LogManagerApplication.getInstance().getCurrentActivity());
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setPositiveButton("OK", onClickListener);
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}

