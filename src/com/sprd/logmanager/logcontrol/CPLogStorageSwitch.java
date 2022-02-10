package com.sprd.logmanager.logcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

class EngpcRequest {
    static final String LOG_TAG = "EngpcRequest";

    // ***** Class Variables
    static Random sRandom = new Random();
    static AtomicInteger sNextSerial = new AtomicInteger(0);
    private static Object sPoolSync = new Object();
    private static EngpcRequest sPool = null;
    private static int sPoolSize = 0;
    private static final int MAX_POOL_SIZE = 4;

    // ***** Instance Variables
    int mSerial;
    byte[] mRequest = new byte[3];
    EngpcRequest mNext;

    static EngpcRequest obtain() {
        EngpcRequest rr = null;

        synchronized (sPoolSync) {
            if (sPool != null) {
                rr = sPool;
                sPool = rr.mNext;
                rr.mNext = null;
                sPoolSize--;
            }
        }

        if (rr == null) {
            rr = new EngpcRequest();
        }
        if (rr.mSerial == 50) {
            resetSerial();
        }
        rr.mSerial = sNextSerial.getAndIncrement();
        return rr;
    }

    void release() {
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                mNext = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    private EngpcRequest() {
    }

    static void resetSerial() {
        sNextSerial.set(0);
    }

    void onError(int error, Object ret) {
        Log.e(LOG_TAG, "send cmd error");
    }
}

public class CPLogStorageSwitch {
    public static String MODEM_LOG_DEST_PROP = "persist.vendor.modem.log_dest";
    public static String WCN_LOG_DEST_PROP = "persist.vendor.wcn.log_dest";
    public static final int WCN_LOG_TYPE = 2;// "wcn";
    public static final int MOEDEM_LOG_TYPE = 1;// "modem";
    public static final String ENGPC_SOCKET = "engpc_soc.l";
    public static final String LOG_TAG = "CPLogStorageSwitch";
    static final int SOCKET_OPEN_RETRY_MILLIS = 4 * 1000;
    static final int MAX_COMMAND_BYTES = 8;
    static final int RESPONSE_BYTES = 3;
    public static final int LOG_PC = 1;
    public static final int LOG_PHONE = 2;
    public static final int NO_LOG = 0;
    // ***** Events
    static final int EVENT_SEND = 1;

    static final int RESPONSE_SOLICITED = 0;
    static final int RESPONSE_UNSOLICITED = -1;
    static final int SOCKET_NOT_AVAILABLE = 1;
    static final int SOCKET_WRITE_ERROR = 2;

    private LocalSocket mSocket = null;
    private static CPLogStorageSwitch sCPLogPcSwitch = null;
    private EngpcReceiver mEngpcReceiver = null;
    private Thread mReceiverThread;
    private HandlerThread mSenderThread = null;
    private EngpcSender mEngpcSender = null;
    ArrayList<OnLogDestChangeListener> mLogDestChangeListeners = new ArrayList<OnLogDestChangeListener>();
    SparseArray<EngpcRequest> mRequestList = new SparseArray<EngpcRequest>();

    public static boolean isEngpcRun() {
        /*
         * String result = SystemProperties.get("init.svc.engpc", "stopped");
         * Log.d(LOG_TAG, "isEngpcRun result = " + result); if
         * (result.equals("running")) { return true; }
         */
        return true;
    }

    public boolean isLogToPc(int type) {
        boolean res = false;
        if (type == (WCN_LOG_TYPE)) {
            if (PropUtils.getInt(WCN_LOG_DEST_PROP, 2) == LOG_PC) {
                Log.i(LOG_TAG, "isLogToPc wcn log is to pc");
                res = true;// PC
            } else {
                Log.i(LOG_TAG, "isLogToPc wcn log to is phone");
                res = false;// SD | data
            }
            return res;
        }
        if (type == (MOEDEM_LOG_TYPE)) {
            if (PropUtils.getInt(MODEM_LOG_DEST_PROP, 2) == LOG_PC) {
                Log.i(LOG_TAG, "isLogToPc modem log is to pc");
                return true;// PC
            } else {
                Log.i(LOG_TAG, "isLogToPc modem log is to phone");
                return false;// SD | data
            }
        }
        return res;
    }

    private CPLogStorageSwitch() {
        mEngpcReceiver = new EngpcReceiver();
        mReceiverThread = new Thread(mEngpcReceiver, "engpcreceiver");
        mReceiverThread.start();
        mSenderThread = new HandlerThread("engpcSender");
        mSenderThread.start();
        Looper looper = mSenderThread.getLooper();
        mEngpcSender = new EngpcSender(looper);
    }

    public static CPLogStorageSwitch getInstance() {
        if (sCPLogPcSwitch == null) {
            sCPLogPcSwitch = new CPLogStorageSwitch();
        }
        if (Build.VERSION.SDK_INT < 28) {
            MODEM_LOG_DEST_PROP = "persist.sys.modem.log_dest";
            WCN_LOG_DEST_PROP = "persist.sys.wcn.log_dest";
        }
        return sCPLogPcSwitch;
    }

    public void addListener(OnLogDestChangeListener listener) {
        mLogDestChangeListeners.add(listener);
    }

    public void removeListener(OnLogDestChangeListener listener) {
        mLogDestChangeListeners.remove(listener);
    }

    public void setModemLogToPC() {
        EngpcRequest request = EngpcRequest.obtain();
        request.mRequest[0] = (byte) request.mSerial;
        request.mRequest[1] = (byte) MOEDEM_LOG_TYPE;
        request.mRequest[2] = (byte) (LOG_PC);
        send(request);
    }

    public int getCPLogDest(int type) {
        int res = -1;
        if (type == (WCN_LOG_TYPE)) {
            res = PropUtils.getInt(WCN_LOG_DEST_PROP, 2);
        }
        if (type == (MOEDEM_LOG_TYPE)) {
            res = PropUtils.getInt(MODEM_LOG_DEST_PROP, 2);
        }
        return res;
    }

    public void setCPLogDest(int dest, int type) {
        Log.e(LOG_TAG, "setCPLogDest dest is " + dest + ", type is " + type);
        EngpcRequest request = EngpcRequest.obtain();
        request.mRequest[0] = (byte) request.mSerial;
        request.mRequest[1] = (byte) type;
        request.mRequest[2] = (byte) (dest);
        send(request);
    }

    private void send(EngpcRequest rr) {
        Message msg = null;
        if (mSocket == null) {
            Log.e(LOG_TAG, "socket is null,not connected");
            rr.release();
            return;
        }
        Log.i(LOG_TAG, "send cmd :" + rr.mRequest[0] + rr.mRequest[1]
                + rr.mRequest[2]);
        msg = mEngpcSender.obtainMessage(EVENT_SEND, rr);
        msg.sendToTarget();
    }

    public void setWcnLogToPC() {
        EngpcRequest request = EngpcRequest.obtain();
        request.mRequest[0] = (byte) request.mSerial;
        request.mRequest[1] = (byte) WCN_LOG_TYPE;
        request.mRequest[2] = (byte) (LOG_PC);
        ;
        send(request);
    }

    public void setModemLogToPhone() {
        if (isEngpcRun()) {
            EngpcRequest request = EngpcRequest.obtain();
            request.mRequest[0] = (byte) request.mSerial;
            request.mRequest[1] = (byte) MOEDEM_LOG_TYPE;
            request.mRequest[2] = (byte) (LOG_PHONE);
            ;
            send(request);
        }
    }

    public void setWcnLogToPhone() {
        if (isEngpcRun()) {
            EngpcRequest request = EngpcRequest.obtain();
            request.mRequest[0] = (byte) request.mSerial;
            request.mRequest[1] = (byte) WCN_LOG_TYPE;
            request.mRequest[2] = (byte) (LOG_PHONE);
            ;
            send(request);
        }
    }

    // bug 860344
    public void restoreCPLogDest(boolean switchScene) {
        int defaultModemDest = -1;
        int defaultWcnDest = -1;
        boolean modemHasSet = false;
        boolean wcnHasSet = false;
        if(!switchScene){
            defaultModemDest = CPLogStorageSwitch.LOG_PHONE;
            defaultWcnDest = CPLogStorageSwitch.LOG_PHONE;
        }
        int wcnDest = LogManagerPreference.getInstanse().getWcnDest(defaultWcnDest);
        int modemDest = LogManagerPreference.getInstanse().getModemDest(defaultModemDest);
        Log.i(LOG_TAG,
                "restore cp log dest after start log or switch log to other scene except  scene opened eventmonitor log");
        CPLogStorageSwitch logPcSwitch = CPLogStorageSwitch.getInstance();
        if (wcnDest == 0) {
            wcnHasSet = true;
            Log.i(LOG_TAG,"wcn no log,force restore to phone");
            logPcSwitch.setCPLogDest(CPLogStorageSwitch.LOG_PHONE, CPLogStorageSwitch.WCN_LOG_TYPE);
        }
        if (modemDest == 0) {
            modemHasSet = true;
            Log.i(LOG_TAG,"modem no log,force restore to phone");
            logPcSwitch.setCPLogDest(CPLogStorageSwitch.LOG_PHONE, CPLogStorageSwitch.MOEDEM_LOG_TYPE);
        }
        if (wcnDest != -1 && wcnHasSet == false) {
            logPcSwitch.setCPLogDest(wcnDest, CPLogStorageSwitch.WCN_LOG_TYPE);
        }
        if (modemDest != -1 && modemHasSet == false) {
            logPcSwitch.setCPLogDest(modemDest,
                    CPLogStorageSwitch.MOEDEM_LOG_TYPE);
        }
    }

    public void saveCplogDest() {
        int wcnDest = PropUtils.getInt(CPLogStorageSwitch.WCN_LOG_DEST_PROP, 2);
        int modemDest = PropUtils.getInt(CPLogStorageSwitch.MODEM_LOG_DEST_PROP, 2);
        Log.i(LOG_TAG,"save cp log dest to share preference");
        LogManagerPreference.getInstanse().setWcnDest(wcnDest);
        LogManagerPreference.getInstanse().setModemDest(modemDest);
    }

    public void reset() {
        setCPLogDest(LOG_PHONE, WCN_LOG_TYPE);
        setCPLogDest(LOG_PHONE, MOEDEM_LOG_TYPE);
    }

    public interface OnLogDestChangeListener {
        void OnLogDestChange(int logType);
    }

    class EngpcReceiver implements Runnable {
        byte[] buffer;

        EngpcReceiver() {
            buffer = new byte[MAX_COMMAND_BYTES];
        }

        @Override
        public void run() {
            int retryCount = 0;
            String engpcSocket = ENGPC_SOCKET;
            try {
                for (;;) {
                    LocalSocket s = null;
                    LocalSocketAddress l;
                    try {
                        s = new LocalSocket();
                        l = new LocalSocketAddress(engpcSocket,
                                LocalSocketAddress.Namespace.ABSTRACT);
                        s.connect(l);
                    } catch (IOException ex) {
                        Log.w(LOG_TAG, ex.toString());
                        try {
                            if (s != null) {
                                s.close();
                            }
                        } catch (IOException ex2) {
                            // ignore failure to close after failure to connect
                        }

                        // don't print an error message after the the first time
                        // or after the 8th time
                        if (retryCount == 8) {
                            Log.e(LOG_TAG, "Couldn't find '" + engpcSocket
                                    + "' socket after " + retryCount
                                    + " times, continuing to retry silently");
                        } else if (retryCount >= 0 && retryCount < 8) {
                            Log.i(LOG_TAG, "Couldn't find '" + engpcSocket
                                    + "' socket; retrying after timeout");
                        }

                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }

                        retryCount++;
                        continue;
                    }

                    retryCount = 0;

                    mSocket = s;
                    Log.i(LOG_TAG, " Connected to '" + engpcSocket + "' socket");
                    int length = 0;
                    try {
                        InputStream is = mSocket.getInputStream();

                        for (;;) {
                            length = readMessage(is, buffer);
                            if (length < 0) {
                                // End-of-stream reached
                                break;
                            }
                            processResponse(buffer, length);
                        }
                    } catch (java.io.IOException ex) {
                        Log.i(LOG_TAG, "'" + engpcSocket + "' socket closed",
                                ex);
                    } catch (Throwable tr) {
                        Log.e(LOG_TAG, "Uncaught exception read length="
                                + length + "Exception:" + tr.toString());
                    }

                    Log.i(LOG_TAG, " Disconnected from '" + engpcSocket
                            + "' socket");

                    try {
                        mSocket.close();
                    } catch (IOException ex) {
                    }

                    mSocket = null;
                    EngpcRequest.resetSerial();

                    // Clear request list on close
                    clearRequestList(SOCKET_NOT_AVAILABLE);
                }
            } catch (Throwable tr) {
                Log.e(LOG_TAG, "Uncaught exception", tr);
            }
        }
    }

    private void clearRequestList(int error) {
        EngpcRequest rr;
        synchronized (mRequestList) {
            int count = mRequestList.size();
            for (int i = 0; i < count; i++) {
                rr = mRequestList.valueAt(i);
                rr.onError(error, null);
                rr.release();
            }
            mRequestList.clear();
        }
    }

    private EngpcRequest findAndRemoveRequestFromList(int serial) {
        EngpcRequest rr = null;
        synchronized (mRequestList) {
            rr = mRequestList.get(serial);
            if (rr != null) {
                mRequestList.remove(serial);
            }
        }

        return rr;
    }

    private void processResponse(byte[] buffer, int len) {
        int serial = -1;
        int logType = -1;
        Log.i(LOG_TAG, "receive data is :" + buffer[0] + buffer[1] + buffer[2]);
        if (len != RESPONSE_BYTES) {
            Log.e(LOG_TAG, "the response length is error:" + buffer.length);
            return;
        }
        if (buffer[0] == RESPONSE_UNSOLICITED) {
            Log.i(LOG_TAG, "receive data from engpc RESPONSE_UNSOLICITED");
        } else {
            serial = buffer[0];
            EngpcRequest rr = findAndRemoveRequestFromList(serial);
            Log.i(LOG_TAG, "receive data serial is :" + serial
                    + " and state is :" + buffer[2]);
            if (buffer[2] == 2) {
                Log.e(LOG_TAG, "serial " + serial + "cmd failed");
                return;
            }
            if (rr != null) {
                rr.release();
            }
        }
        logType = buffer[1];
        for (OnLogDestChangeListener listener : mLogDestChangeListeners) {
            listener.OnLogDestChange(logType);
        }
    }

    private int readMessage(InputStream is, byte[] buffer) throws IOException {
        int countRead = is.read(buffer, 0, RESPONSE_BYTES);

        if (countRead < 0) {
            Log.e(LOG_TAG, "Hit EOS reading message length");
            return -1;
        }
        return countRead;
    }

    class EngpcSender extends Handler implements Runnable {
        public EngpcSender(Looper looper) {
            super(looper);
        }

        byte[] dataLength = new byte[4];

        @Override
        public void run() {
            // setup if needed
        }

        // ***** Handler implementation
        @Override
        public void handleMessage(Message msg) {
            EngpcRequest rr = (EngpcRequest) (msg.obj);
            EngpcRequest req = null;

            switch (msg.what) {
            case EVENT_SEND:
                try {
                    LocalSocket s = mSocket;

                    if (s == null) {
                        rr.onError(SOCKET_NOT_AVAILABLE, null);
                        rr.release();
                        return;
                    }

                    synchronized (mRequestList) {
                        mRequestList.append(rr.mSerial, rr);
                    }

                    s.getOutputStream().write(rr.mRequest);
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "IOException", ex);
                    req = findAndRemoveRequestFromList(rr.mSerial);
                    if (req != null) {
                        rr.onError(SOCKET_NOT_AVAILABLE, null);
                        rr.release();
                    }
                } catch (RuntimeException exc) {
                    Log.e(LOG_TAG, "Uncaught exception ", exc);
                    req = findAndRemoveRequestFromList(rr.mSerial);
                    if (req != null) {
                        rr.onError(SOCKET_NOT_AVAILABLE, null);
                        rr.release();
                    }
                }

                break;
            }
        }
    }

}
