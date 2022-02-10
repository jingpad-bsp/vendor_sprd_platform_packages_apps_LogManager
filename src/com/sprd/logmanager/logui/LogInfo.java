package com.sprd.logmanager.logui;

import android.util.*;
import android.content.Context;
import android.text.TextUtils;
import android.content.ContentValues;
import android.database.Cursor;

import com.sprd.logmanager.database.DBHelper;
import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.database.LogSceneManager;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch;
import com.sprd.logmanager.logcontrol.LogSwichForScene;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SPREADTRUM\zhengxu.zhang on 9/8/15.
 */
public class LogInfo {

    private static LogInfo sLogInfo = null;
    public Context mContext;
    private static final String TAG = "LogInfo";

    private static final String PACKAGE_NAME = "com.sprd.logmanager.logcontrol";

    public static final String KEY_ANDROID_LOG = "LogSwichForScene_AndroidLogController";
    public static final String KEY_KERNAL_LOG = "LogSwichForScene_KernelLogController";
    public static final String KEY_AP_CAP_LOG = "LogSwichForScene_APCapLogController";
    public static final String KEY_HCI_LOG = "LogSwichForScene_BtHciLogController";
    public static final String KEY_MODEM_LOG = "LogSwichForScene_ModemLogController";
    public static final String KEY_CP_CAP_LOG = "LogSwichForScene_CpCapLogController";
    public static final String KEY_CM4_LOG = "LogSwichForScene_Cm4LogController";
    public static final String KEY_ARM_LOG = "LogSwichForScene_ArmLogController";
    public static final String KEY_ARM_PCM_LOG = "LogSwichForScene_ArmPcmDataController";
    public static final String KEY_AGDSP_LOG = "LogSwichForScene_AGDspLogController";
    public static final String KEY_AGDSP_PCM_DUMP_LOG = "LogSwichForScene_AGDspPcmDumpLogController";
    public static final String KEY_AGDSP_OUTPUT_LOG = "LogSwichForScene_AGDspOutputController";
    public static final String KEY_DSP_LOG = "LogSwichForScene_DspLogController";
    public static final String KEY_DSP_PCM_LOG = "LogSwichForScene_DspPcmDataController";
    public static final String KEY_WCN_LOG = "LogSwichForScene_WcnLogController";
    public static final String KEY_GPS_LOG = "LogSwichForScene_GpsLogController";
    public static final String KEY_CAP_LENGTH = "LogSwichForScene_CapLogLengthController";
    public static final String KEY_EVENT_MONITOR = "LogSwichForScene_EventMonitorController";
    public static final String KEY_MINI_ORCAAP = "LogSwichForScene_OrcaapController";
    public static final String KEY_MINI_ORCADP = "LogSwichForScene_OrcadpController";

    public static boolean AGDSP_SUPPORT;
    public static boolean USE_REFLECT = false;
    private LogManagerPreference mLogManagerPreference;
    private LogSwichForScene mLogSwichForScene;
    private SceneStatus mCurrentSceneStatus = SceneStatus.close;

    private LinkedHashMap<String, String> mCustomCacheMap = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> mCustomMap = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> mSwitchParamsMap = new LinkedHashMap<String, String>();
    private Timer slogStartTime;
    private long slogRunningTime;
    private RequestTimerTask timerTask;

    public static LogInfo getInstance() {
        if (sLogInfo == null) {
            sLogInfo = new LogInfo();
            sLogInfo.readLogScene();
            sLogInfo.slogStartTime = new Timer();
            AGDSP_SUPPORT = CPLogControl.getInstance().isSupportAGDSP();
        }
        return sLogInfo;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    private LogInfo() {
        // TODO Auto-generated constructor stub
        mLogManagerPreference = LogManagerPreference.getInstanse();
        mLogSwichForScene = new LogSwichForScene();
    }

    class RequestTimerTask extends TimerTask {
        public void run() {
            slogRunningTime += 1000;
        }
    }

    public void resetStartTime() {
        if (slogStartTime != null) {
            slogStartTime.cancel();
        }
        slogStartTime = new Timer();
        timerTask = new RequestTimerTask();
        slogRunningTime = 0;
        slogStartTime.schedule(timerTask, 1000, 1000);
    }

    public void instanceCloseScene() {
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "0");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "0");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "0");
        mSwitchParamsMap.put(KEY_HCI_LOG, "0");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "0");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "0");
        mSwitchParamsMap.put(KEY_CM4_LOG, "0");
        mSwitchParamsMap.put(KEY_ARM_LOG, "0");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "0");
        mSwitchParamsMap.put(KEY_GPS_LOG, "0");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "0");
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "0");
    }

    public void instanceNormalScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(10000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "1");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "1");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "1");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "1");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "0");
        mSwitchParamsMap.put(KEY_CM4_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "1");
        mSwitchParamsMap.put(KEY_GPS_LOG, "1");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public void instanceUserScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(10000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "1");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "1");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "1");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "0");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "0");
        mSwitchParamsMap.put(KEY_CM4_LOG, "0");
        mSwitchParamsMap.put(KEY_ARM_LOG, "0");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "0");
        mSwitchParamsMap.put(KEY_GPS_LOG, "0");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public void instanceDataScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(10000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "1");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "1");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "1");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "1");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_CM4_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "1");
        mSwitchParamsMap.put(KEY_GPS_LOG, "1");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public void instanceVoiceScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(3000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "1");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "1");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "1");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "1");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_CM4_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "1");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "1");
        mSwitchParamsMap.put(KEY_WCN_LOG, "1");
        mSwitchParamsMap.put(KEY_GPS_LOG, "1");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public void instanceModemScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(10000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "0");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "0");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "0");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "1");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_CM4_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_LOG, "1");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "1");
        mSwitchParamsMap.put(KEY_GPS_LOG, "0");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public void instanceWcnScene() {
        long defaultApCapSize = mLogManagerPreference.getTcpCapSize(3000);
        mSwitchParamsMap.clear();
        mSwitchParamsMap.put(KEY_ANDROID_LOG, "1");
        mSwitchParamsMap.put(KEY_KERNAL_LOG, "1");
        mSwitchParamsMap.put(KEY_AP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_HCI_LOG, "1");
        mSwitchParamsMap.put(KEY_MODEM_LOG, "1");
        mSwitchParamsMap.put(KEY_CP_CAP_LOG, "1");
        mSwitchParamsMap.put(KEY_CM4_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_LOG, "1");
        mSwitchParamsMap.put(KEY_ARM_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, "0");
        mSwitchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, "0");
        mSwitchParamsMap.put(KEY_DSP_LOG, "2");
        mSwitchParamsMap.put(KEY_DSP_PCM_LOG, "0");
        mSwitchParamsMap.put(KEY_WCN_LOG, "1");
        mSwitchParamsMap.put(KEY_GPS_LOG, "1");
        mSwitchParamsMap.put(KEY_CAP_LENGTH, "" + defaultApCapSize);
        mSwitchParamsMap.put(KEY_EVENT_MONITOR, "0");
        mSwitchParamsMap.put(KEY_MINI_ORCAAP, "1");
        mSwitchParamsMap.put(KEY_MINI_ORCADP, "1");
    }

    public int getDefautApCapSize(){
        String scene = LogSceneManager.getInstance().getCurrentSelectedSceneName(mContext);
        int size = 10000;
        if (SceneInfo.SCENE_VOICE.equals(scene) ||SceneInfo.SCENE_WCN.equals(scene)) {
            size = 3000;
        }
        return size;
    }

    public String getSceneParam(String scene) {
        StringBuffer sb = new StringBuffer();
        if (SceneInfo.SCENE_NORMAL.equals(scene)) {
            instanceNormalScene();
        } else if (SceneInfo.SCENE_DATA.equals(scene)) {
            instanceDataScene();
        } else if (SceneInfo.SCENE_VOICE.equals(scene)) {
            instanceVoiceScene();
        } else if (SceneInfo.SCENE_WCN.equals(scene)) {
            instanceWcnScene();
        } else if (SceneInfo.SCENE_USER.equals(scene)) {
            instanceUserScene();
        } else {
            instanceCloseScene();
        }

        for (String value : mSwitchParamsMap.values()) {
            sb.append(",");
            sb.append(value);
        }
        return sb.toString();
    }

    public void closeScene() {
        Log.d(TAG, "close Scene");
        instanceCloseScene();
        //mCurrentSceneStatus = SceneStatus.close;
        //excute();
        excuteClose();
    }

    public void openNormalScene() {
        Log.d(TAG, "open normal Scene2");
        instanceNormalScene();
        Log.d(TAG, "open normal Scene3");

        mCurrentSceneStatus = SceneStatus.normal;
        excute();
        resetStartTime();
    }

    public void openDataScene() {
        Log.d(TAG, "open data Scene2");
        instanceDataScene();
        Log.d(TAG, "open data Scene3");

        mCurrentSceneStatus = SceneStatus.data;
        excute();
        resetStartTime();
    }

    public void openVoiceScene() {
        Log.d(TAG, "open voice Scene2");
        instanceVoiceScene();
        Log.d(TAG, "open voice Scene3");

        mCurrentSceneStatus = SceneStatus.voice;
        excute();
        resetStartTime();
    }

    public void openModemScene() {
        Log.d(TAG, "open modem Scene2");
        instanceModemScene();
        Log.d(TAG, "open modem Scene3");

        mCurrentSceneStatus = SceneStatus.modem;
        excute();
        resetStartTime();
    }

    public void openWcnScene() {
        Log.d(TAG, "open wcn Scene2");
        instanceWcnScene();
        Log.d(TAG, "open wcn Scene3");
        mCurrentSceneStatus = SceneStatus.wcn;
        excute();
        resetStartTime();
    }

    public void openUserScene() {
        Log.d(TAG, "open user Scene2");
        instanceUserScene();
        Log.d(TAG, "open user Scene3");

        mCurrentSceneStatus = SceneStatus.user;
        excute();
        Log.d(TAG, "user Scene,set modem log and wcn log to phone");
        CPLogStorageSwitch.getInstance().saveCplogDest();
        if (CPLogStorageSwitch.getInstance().isLogToPc(
                CPLogStorageSwitch.WCN_LOG_TYPE)) {
            CPLogStorageSwitch.getInstance().setWcnLogToPhone();
        }
        if (CPLogStorageSwitch.getInstance().isLogToPc(
                CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
            CPLogStorageSwitch.getInstance().setModemLogToPhone();
        }
        resetStartTime();
    }

    public void openCustomerScene() {
        mCurrentSceneStatus = SceneStatus.customer;
        String sceneName = LogSceneManager.getInstance()
                .getCurrentSelectedSceneDB(mContext);
        Log.d(TAG, "openCustomerScene" + sceneName);
        LogInfo.getInstance().readData(mContext, sceneName);
        selectCustomerOrder(sceneName);
        resetStartTime();
    }

    public void open(SceneStatus s) {
        switch (s) {
        case close:
            // SPRD: Bug 557464 normal log button display is not same with
            // scence activity
            // open(slog_tmp);
            break;
        case normal:
            openNormalScene();
            break;
        case data:
            openDataScene();
            break;
        case voice:
            openVoiceScene();
            break;
        case modem:
            openModemScene();
            break;
        case wcn:
            openWcnScene();
            break;
        case user:
            openUserScene();
            break;
        case customer:
            openCustomerScene();
        }
        CPLogStorageSwitch.getInstance().restoreCPLogDest(false);
    }

    public String toString() {
        switch (mCurrentSceneStatus) {
        case close:
            return SceneInfo.SCENE_CLOSE;
        case normal:
            return SceneInfo.SCENE_NORMAL;
        case data:
            return SceneInfo.SCENE_DATA;
        case voice:
            return SceneInfo.SCENE_VOICE;
        case modem:
            return SceneInfo.SCENE_MODEM;
        case wcn:
            return SceneInfo.SCENE_WCN;
        case user:
            return SceneInfo.SCENE_USER;
        case customer:
            return SceneInfo.SCENE_CUSTOMER;
        }
        return SceneInfo.SCENE_CLOSE;
    }

    private void excute() {
        try {
            for (Entry<String, String> entry : mSwitchParamsMap.entrySet()) {
                Log.d(TAG, entry.getKey() + entry.getValue());
                excute(entry.getKey(), entry.getValue());
            }
            saveLogScene();
        } catch (Exception e) {
            Log.d(TAG, "Exception is " + e);
        }
    }


    private void excuteClose() {
        //close ap log after modem log closed for debug
        LinkedHashMap<String, String> switchParamsMap = new LinkedHashMap<String, String>();
        switchParamsMap.put(KEY_CP_CAP_LOG, mSwitchParamsMap.get(KEY_CP_CAP_LOG));
        switchParamsMap.put(KEY_CM4_LOG, mSwitchParamsMap.get(KEY_CM4_LOG));
        switchParamsMap.put(KEY_ARM_LOG, mSwitchParamsMap.get(KEY_ARM_LOG));
        switchParamsMap.put(KEY_ARM_PCM_LOG, mSwitchParamsMap.get(KEY_ARM_PCM_LOG));
        switchParamsMap.put(KEY_AGDSP_LOG, mSwitchParamsMap.get(KEY_AGDSP_LOG));
        switchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, mSwitchParamsMap.get(KEY_AGDSP_PCM_DUMP_LOG));
        switchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, mSwitchParamsMap.get(KEY_AGDSP_OUTPUT_LOG));
        switchParamsMap.put(KEY_DSP_LOG, mSwitchParamsMap.get(KEY_DSP_LOG));
        switchParamsMap.put(KEY_DSP_PCM_LOG, mSwitchParamsMap.get(KEY_DSP_PCM_LOG));
        switchParamsMap.put(KEY_WCN_LOG, mSwitchParamsMap.get(KEY_WCN_LOG));
        switchParamsMap.put(KEY_GPS_LOG, mSwitchParamsMap.get(KEY_GPS_LOG));
        switchParamsMap.put(KEY_CAP_LENGTH, "" + mSwitchParamsMap.get(KEY_CAP_LENGTH));
        switchParamsMap.put(KEY_EVENT_MONITOR, mSwitchParamsMap.get(KEY_EVENT_MONITOR));
        switchParamsMap.put(KEY_MINI_ORCAAP, mSwitchParamsMap.get(KEY_MINI_ORCAAP));
        switchParamsMap.put(KEY_MINI_ORCADP, mSwitchParamsMap.get(KEY_MINI_ORCADP));
        switchParamsMap.put(KEY_MODEM_LOG, mSwitchParamsMap.get(KEY_MODEM_LOG));
        switchParamsMap.put(KEY_AP_CAP_LOG,  mSwitchParamsMap.get(KEY_AP_CAP_LOG));
        switchParamsMap.put(KEY_HCI_LOG, mSwitchParamsMap.get(KEY_HCI_LOG));
        switchParamsMap.put(KEY_ANDROID_LOG, mSwitchParamsMap.get(KEY_ANDROID_LOG));
        switchParamsMap.put(KEY_KERNAL_LOG, mSwitchParamsMap.get(KEY_KERNAL_LOG));
        try {
            for (Entry<String, String> entry : switchParamsMap.entrySet()) {
                Log.d(TAG, entry.getKey() + entry.getValue());
                excute(entry.getKey(), entry.getValue());
            }
            saveLogScene();
        } catch (Exception e) {
            Log.d(TAG, "Exception is " + e);
        }
    }

    public SceneStatus getSceneStatus() {
        return mCurrentSceneStatus;
    }

    public void setSceneStatus(SceneStatus status) {
        mCurrentSceneStatus = status;
    }

    public void setSceneStatus(Context context) {
        String sceneName = LogSceneManager.getInstance()
                .getCurrentSelectedSceneDB(context);
        if (SceneInfo.SCENE_NORMAL.equals(sceneName)) {
            mCurrentSceneStatus = SceneStatus.normal;
        } else if (SceneInfo.SCENE_DATA.equals(sceneName)) {
            mCurrentSceneStatus = SceneStatus.data;
        } else if (SceneInfo.SCENE_VOICE.equals(sceneName)) {
            mCurrentSceneStatus = SceneStatus.voice;
        } else if (SceneInfo.SCENE_WCN.equals(sceneName)) {
            mCurrentSceneStatus = SceneStatus.wcn;
        } else if (SceneInfo.SCENE_USER.equals(sceneName)) {
            mCurrentSceneStatus = SceneStatus.user;
        } else {
            mCurrentSceneStatus = SceneStatus.customer;
        }
        Log.i(TAG, "setSceneStatus scene name is :" + sceneName);
    }

    public enum SceneStatus {
        close, normal, data, voice, modem, wcn, user, customer
    }

    public String getAPSceneInfo(Context context) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                context,
                LogSceneManager.getInstance().getDBSceneColumn(
                        context,
                        LogSceneManager.getInstance()
                                .getCurrentSelectedSceneName(context)));
        boolean isMore = false;
        while (cursor.moveToNext()) {
            for (int i = 0; i < SceneInfo.SCENE_AP_ARRAY.length; i++) {
                String value = cursor.getString(cursor
                        .getColumnIndex(SceneInfo.SCENE_AP_PROJECTION[i]));
                if ("1".equals(value)) {
                    if (isMore) {
                        sb.append(", ");
                    }
                    sb.append(SceneInfo.SCENE_AP_ARRAY[i]);
                    isMore = true;
                }
            }
        }
        cursor.close();
        return sb.toString();
    }

    public String getModemSceneInfo(Context context) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                context,
                LogSceneManager.getInstance().getDBSceneColumn(
                        context,
                        LogSceneManager.getInstance()
                                .getCurrentSelectedSceneName(context)));
        boolean isMore = false;
        while (cursor.moveToNext()) {
            for (int i = 0; i < SceneInfo.SCENE_MODEM_ARRAY.length; i++) {
                String value = cursor.getString(cursor
                        .getColumnIndex(SceneInfo.SCENE_MODEM_PROJECTION[i]));
                if ("1".equals(value)
                        || ("2".equals(value) && "dsp log"
                                .equals(SceneInfo.SCENE_MODEM_ARRAY[i]))) {
                    if (!AGDSP_SUPPORT
                            && SceneInfo.SCENE_MODEM_ARRAY[i].contains("agdsp")) {
                        continue;
                    }
                    if (isMore) {
                        sb.append(", ");
                    }
                    sb.append(SceneInfo.SCENE_MODEM_ARRAY[i]);
                    isMore = true;
                }
            }
        }
        cursor.close();
        return sb.toString();
    }

    public String getConnectivitySceneInfo(Context context) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                context,
                LogSceneManager.getInstance().getDBSceneColumn(
                        context,
                        LogSceneManager.getInstance()
                                .getCurrentSelectedSceneName(context)));
        boolean isMore = false;
        while (cursor.moveToNext()) {
            for (int i = 0; i < SceneInfo.SCENE_CONNECTIVITY_ARRAY.length; i++) {
                String value = cursor
                        .getString(cursor
                                .getColumnIndex(SceneInfo.SCENE_CONNECTIVITY_PROJECTION[i]));
                if ("1".equals(value)) {
                    if (isMore) {
                        sb.append(", ");
                    }
                    sb.append(SceneInfo.SCENE_CONNECTIVITY_ARRAY[i]);
                    isMore = true;
                }
            }
        }
        cursor.close();
        return sb.toString();
    }

    public String getOtherSceneInfo(Context context) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                context,
                LogSceneManager.getInstance().getDBSceneColumn(
                        context,
                        LogSceneManager.getInstance()
                                .getCurrentSelectedSceneName(context)));
        boolean isMore = false;
        while (cursor.moveToNext()) {
            for (int i = 0; i < SceneInfo.SCENE_OTHER_ARRAY.length; i++) {
                String value = cursor.getString(cursor
                        .getColumnIndex(SceneInfo.SCENE_OTHER_PROJECTION[i]));
                if ("1".equals(value)) {
                    if (isMore) {
                        sb.append(", ");
                    }
                    sb.append(SceneInfo.SCENE_OTHER_ARRAY[i]);
                    isMore = true;
                }
            }
        }
        cursor.close();
        return sb.toString();
    }

    public String getOtherSceneInfoByName(Context context,String sceneName) {
        StringBuffer sb = new StringBuffer("");
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                context,
                LogSceneManager.getInstance().getDBSceneColumn(
                        context,
                        sceneName));
        boolean isMore = false;
        while (cursor.moveToNext()) {
            for (int i = 0; i < SceneInfo.SCENE_OTHER_ARRAY.length; i++) {
                String value = cursor.getString(cursor
                        .getColumnIndex(SceneInfo.SCENE_OTHER_PROJECTION[i]));
                if ("1".equals(value)) {
                    if (isMore) {
                        sb.append(", ");
                    }
                    sb.append(SceneInfo.SCENE_OTHER_ARRAY[i]);
                    isMore = true;
                }
            }
        }
        cursor.close();
        return sb.toString();
    }

    boolean isModemEventMonitorLogOpened(String sceneName){
        String sceneInfo = LogInfo.getInstance().getOtherSceneInfoByName(mContext,sceneName);
        return sceneInfo.contains("monitor");

    }
    public long getSlogStartTime() {
        return slogRunningTime;
    }

    public void saveLogScene() {
        Log.d(TAG, "write log scene");

        mLogManagerPreference.setScene(toString());
        Log.d("TAG", "write name:" + LogInfo.getInstance().toString());
        APLogControl.getInstance().print2journal("select scene  "+LogInfo.getInstance().toString());
    }

    public String readLogScene() {
        Log.d("slog", "read log scene");
        if (mContext == null) {
            mCurrentSceneStatus = SceneStatus.normal;
            instanceNormalScene();
            return SceneInfo.SCENE_NORMAL;
        }
        String name = mLogManagerPreference.getScene();
        Log.d(TAG, "read scene name:" + name);
        if (name.equals(SceneInfo.SCENE_CLOSE)) {
            mCurrentSceneStatus = SceneStatus.close;
            instanceCloseScene();
        } else if (name.equals(SceneInfo.SCENE_DATA)) {
            mCurrentSceneStatus = SceneStatus.data;
            instanceDataScene();
        } else if (name.equals(SceneInfo.SCENE_NORMAL)) {
            mCurrentSceneStatus = SceneStatus.normal;
            instanceNormalScene();
        } else if (name.equals(SceneInfo.SCENE_VOICE)) {
            mCurrentSceneStatus = SceneStatus.voice;
            instanceVoiceScene();
        } else if (name.equals(SceneInfo.SCENE_MODEM)) {
            mCurrentSceneStatus = SceneStatus.modem;
            instanceModemScene();
        } else if (name.equals(SceneInfo.SCENE_WCN)) {
            mCurrentSceneStatus = SceneStatus.wcn;
            instanceWcnScene();
        } else if (name.equals(SceneInfo.SCENE_USER)) {
            mCurrentSceneStatus = SceneStatus.user;
            instanceUserScene();
        } else if (name.equals(SceneInfo.SCENE_CUSTOMER)) {
            mCurrentSceneStatus = SceneStatus.customer;
            loadCustomerOrder();
            // commitCustomerOrder();
        } else if (CommonUtils.isUserBuild()) {
            mCurrentSceneStatus = SceneStatus.close;
            instanceCloseScene();
        } else {
            mCurrentSceneStatus = SceneStatus.normal;
            instanceNormalScene();
        }
        return name;
    }

    public void writeData(Context context, String sceneName) {
        Log.d(TAG, "writeData to db:" + sceneName);
        if (TextUtils.isEmpty(sceneName) || context == null) {
            return;
        }
        if (SceneInfo.exists(context.getContentResolver(), sceneName)) {
            mCustomMap = new LinkedHashMap<String, String>(mCustomCacheMap);
            SceneInfo.update(context.getContentResolver(),
                    buildContentValues(mCustomMap, sceneName, context),
                    sceneName);
        } else {
            SceneInfo.insert(context.getContentResolver(),
                    buildContentValues(mCustomMap, sceneName, context));
        }
    }

    public void readData(Context context, String sceneName) {
        if (SceneInfo.SCENE_NORMAL.equals(LogSceneManager.getInstance()
                .getDBSceneColumn(context, sceneName))) {
            mCurrentSceneStatus = SceneStatus.normal;
            instanceNormalScene();
        } else if (SceneInfo.SCENE_DATA.equals(LogSceneManager.getInstance()
                .getDBSceneColumn(context, sceneName))) {
            mCurrentSceneStatus = SceneStatus.data;
            instanceDataScene();
        } else if (SceneInfo.SCENE_VOICE.equals(LogSceneManager.getInstance()
                .getDBSceneColumn(context, sceneName))) {
            mCurrentSceneStatus = SceneStatus.voice;
            instanceVoiceScene();
        } else if (SceneInfo.SCENE_WCN.equals(LogSceneManager.getInstance()
                .getDBSceneColumn(context, sceneName))) {
            mCurrentSceneStatus = SceneStatus.wcn;
            instanceWcnScene();
        } else if (SceneInfo.SCENE_USER.equals(LogSceneManager.getInstance()
                .getDBSceneColumn(context, sceneName))) {
            mCurrentSceneStatus = SceneStatus.user;
            instanceUserScene();
        } else if (SceneInfo.SCENE_CUSTOMER.equals(LogSceneManager
                .getInstance().getDBSceneColumn(context, sceneName))) {
            Log.d(TAG, "readData to db(ignore):" + sceneName);
            // ignore
        } else {
            Log.d(TAG, "readData to db:" + sceneName);
            mCurrentSceneStatus = SceneStatus.customer;
            loadCustomerOrder(sceneName);
        }
    }

    private ContentValues buildContentValues(
            LinkedHashMap<String, String> customMap, String sceneName,
            Context context) {
        ContentValues value = new ContentValues();
        value.put(DBHelper.NAME, sceneName);
        if (sceneName.equals(LogSceneManager.getInstance()
                .getCurrentSelectedSceneName(context))) {
            value.put(DBHelper.CHECKED, 1);
        } else {
            value.put(DBHelper.CHECKED, 0);
        }
        int i = 0;
        for (Entry<String, String> entry : customMap.entrySet()) {
            Log.i(TAG, "save: key = "+entry.getKey()+",value = "+entry.getValue());
            value.put(SceneInfo.SCENE_PROJECTION[i + 3], entry.getValue());
            i++;
        }
        return value;
    }

    public String getCustomer(String name) {
        if (mCustomCacheMap.containsKey(name)) {
            return mCustomCacheMap.get(name);
        }
        Log.d(TAG, String.format("%s , this arg not existed return 0 ", name));
        return "0";
    }

    public String getCustomerDefined(String name) {
        if (mCustomMap.containsKey(name)) {
            return mCustomMap.get(name);
        }
        Log.d(TAG, String.format("%s , this arg not existed return 0 ", name));
        return "0";
    }

    public void setCustomer(String name, String value) {
        mCustomCacheMap.put(name, value);
        Log.d(TAG, String.format("add:%s value:%s", name, value));
    }

    public void setLog2PcByModemEventLog(String sceneName){
         if(LogInfo.getInstance().isModemEventMonitorLogOpened(sceneName)){
            Log.d(TAG, "Scene opened  with modem event log,set modem log and wcn log to phone,scene is "+sceneName);
            CPLogStorageSwitch.getInstance().saveCplogDest();
            if (CPLogStorageSwitch.getInstance().isLogToPc(CPLogStorageSwitch.WCN_LOG_TYPE)) {
                CPLogStorageSwitch.getInstance().setWcnLogToPhone();
            }
            if (CPLogStorageSwitch.getInstance().isLogToPc(
                    CPLogStorageSwitch.MOEDEM_LOG_TYPE)) {
                CPLogStorageSwitch.getInstance().setModemLogToPhone();
            }
        }
    }

    public void selectCustomerOrder(String sceneName) {
        Log.d(TAG, "user select order:"+sceneName);
        LinkedHashMap<String, String> switchParamsMap = new LinkedHashMap<String, String>();
        switchParamsMap.put(KEY_CP_CAP_LOG, mCustomMap.get(KEY_CP_CAP_LOG));
        switchParamsMap.put(KEY_CM4_LOG, mCustomMap.get(KEY_CM4_LOG));
        switchParamsMap.put(KEY_ARM_LOG, mCustomMap.get(KEY_ARM_LOG));
        switchParamsMap.put(KEY_ARM_PCM_LOG, mCustomMap.get(KEY_ARM_PCM_LOG));
        switchParamsMap.put(KEY_AGDSP_LOG, mCustomMap.get(KEY_AGDSP_LOG));
        switchParamsMap.put(KEY_AGDSP_PCM_DUMP_LOG, mCustomMap.get(KEY_AGDSP_PCM_DUMP_LOG));
        switchParamsMap.put(KEY_AGDSP_OUTPUT_LOG, mCustomMap.get(KEY_AGDSP_OUTPUT_LOG));
        switchParamsMap.put(KEY_DSP_LOG, mCustomMap.get(KEY_DSP_LOG));
        switchParamsMap.put(KEY_DSP_PCM_LOG, mCustomMap.get(KEY_DSP_PCM_LOG));
        switchParamsMap.put(KEY_WCN_LOG, mCustomMap.get(KEY_WCN_LOG));
        switchParamsMap.put(KEY_GPS_LOG, mCustomMap.get(KEY_GPS_LOG));
        switchParamsMap.put(KEY_CAP_LENGTH, "" + mCustomMap.get(KEY_CAP_LENGTH));
        switchParamsMap.put(KEY_EVENT_MONITOR, mCustomMap.get(KEY_EVENT_MONITOR));
        switchParamsMap.put(KEY_MINI_ORCAAP, mCustomMap.get(KEY_MINI_ORCAAP));
        switchParamsMap.put(KEY_MINI_ORCADP, mCustomMap.get(KEY_MINI_ORCADP));
        Log.i(TAG, "selectCustomerOrder set modem log state last");
        switchParamsMap.put(KEY_MODEM_LOG, mCustomMap.get(KEY_MODEM_LOG));
        switchParamsMap.put(KEY_AP_CAP_LOG,  mCustomMap.get(KEY_AP_CAP_LOG));
        switchParamsMap.put(KEY_HCI_LOG, mCustomMap.get(KEY_HCI_LOG));
        switchParamsMap.put(KEY_ANDROID_LOG, mCustomMap.get(KEY_ANDROID_LOG));
        switchParamsMap.put(KEY_KERNAL_LOG, mCustomMap.get(KEY_KERNAL_LOG));
        for (Entry<String, String> entry : switchParamsMap.entrySet()) {
            Log.d(TAG, entry.getKey() + entry.getValue());
            excute(entry.getKey(), entry.getValue());
        }
        setLog2PcByModemEventLog(sceneName);
    }

    private boolean excute(String name, String arg) {
        boolean res = false;
        Log.d(TAG, String.format("cmd name:%s  arg: %s", name, arg));
        if (USE_REFLECT) {
            try {
                String className = name.split("_")[0];
                Log.d(TAG, String.format("package: %s  class: %s  arg: %s",
                        PACKAGE_NAME, className, arg));
                Class classType = Class.forName(String.format("%s.%s",
                        PACKAGE_NAME, className));

                Log.d(TAG, "1" + classType.toString());
                Method method = classType.getMethod(name, String.class);
                Log.d(TAG, "2" + method.toString());
                Object result = method.invoke(classType.newInstance(), arg);
                Log.d(TAG, "3" + result);

                return true;
            } catch (Exception e) {
                Log.d(TAG, String.format(
                        "cmd name: [%s] arg: [%s] has exception[%s]", name,
                        arg, e.toString()));
                e.printStackTrace();
                return false;
            }
        } else {
            if (name.equals(KEY_ANDROID_LOG)) {
                mLogSwichForScene.LogSwichForScene_AndroidLogController(arg);
            } else if (name.equals(KEY_KERNAL_LOG)) {
                mLogSwichForScene.LogSwichForScene_KernelLogController(arg);
            } else if (name.equals(KEY_AP_CAP_LOG)) {
                mLogSwichForScene.LogSwichForScene_APCapLogController(arg);
            } else if (name.equals(KEY_HCI_LOG)) {
                mLogSwichForScene.LogSwichForScene_BtHciLogController(arg);
            } else if (name.equals(KEY_MODEM_LOG)) {
                mLogSwichForScene.LogSwichForScene_ModemLogController(arg);
            } else if (name.equals(KEY_CP_CAP_LOG)) {
                mLogSwichForScene.LogSwichForScene_CpCapLogController(arg);
            } else if (name.equals(KEY_CM4_LOG)) {
                mLogSwichForScene.LogSwichForScene_Cm4LogController(arg);
            } else if (name.equals(KEY_ARM_LOG)) {
                mLogSwichForScene.LogSwichForScene_ArmLogController(arg);
            } else if (name.equals(KEY_ARM_PCM_LOG)) {
                mLogSwichForScene.LogSwichForScene_ArmPcmDataController(arg);
            } else if (name.equals(KEY_AGDSP_LOG)) {
                mLogSwichForScene.LogSwichForScene_AGDspLogController(arg);
            } else if (name.equals(KEY_AGDSP_PCM_DUMP_LOG)) {
                mLogSwichForScene.LogSwichForScene_AGDspPcmDumpLogController(arg);
            } else if (name.equals(KEY_AGDSP_OUTPUT_LOG)) {
                mLogSwichForScene.LogSwichForScene_AGDspOutputController(arg);
            } else if (name.equals(KEY_DSP_LOG)) {
                mLogSwichForScene.LogSwichForScene_DspLogController(arg);
            } else if (name.equals(KEY_DSP_PCM_LOG)) {
                mLogSwichForScene.LogSwichForScene_DspPcmDataController(arg);
            } else if (name.equals(KEY_WCN_LOG)) {
                mLogSwichForScene.LogSwichForScene_WcnLogController(arg);
            } else if (name.equals(KEY_GPS_LOG)) {
                mLogSwichForScene.LogSwichForScene_GpsLogController(arg);
            }  else if (name.equals(KEY_CAP_LENGTH)) {
                mLogSwichForScene.LogSwichForScene_CapLogLengthController(arg);
            } else if (name.equals(KEY_EVENT_MONITOR)) {
                mLogSwichForScene.LogSwichForScene_EventMonitorController( arg );
            } else if (name.equals(KEY_MINI_ORCAAP)) {
                    mLogSwichForScene.LogSwichForScene_OrcaapController(arg);
            } else if (name.equals(KEY_MINI_ORCADP)) {
                    mLogSwichForScene.LogSwichForScene_OrcadpController(arg);
            }
            return true;
        }

    }

    public void commitCustomerOrder(Context context, String customName) {
        Log.d(TAG, "user commit order:");
        mCustomMap = new LinkedHashMap<String, String>(mCustomCacheMap);
        // saveCustomerOrder();
        for (Entry<String, String> entry : mCustomMap.entrySet()) {
            Log.d(TAG, entry.getKey() + entry.getValue());
            excute(entry.getKey(), entry.getValue());
        }
        writeData(context, customName);
        setLog2PcByModemEventLog(customName);
    }

    public void loadCustomerOrder() {
        Log.i(TAG, "loadCustomerOrder");
    }

    public void loadCustomerOrder(String sceneName) {
        Log.d(TAG, "loadCustomerOrder:" + sceneName);
        instanceCloseScene();
        if (!SceneInfo.exists(mContext.getContentResolver(), sceneName)) {
            sceneName = LogSceneManager.getInstance()
                    .getCurrentSelectedSceneName(mContext);
        }
        Cursor cursor = LogSceneManager.getInstance().getSceneInfoByName(
                mContext,
                LogSceneManager.getInstance().getDBSceneColumn(mContext,
                        sceneName));
        while (cursor.moveToNext()) {
            int i = 0;
            for (Entry<String, String> entry : mSwitchParamsMap.entrySet()) {
                String value = cursor.getString(cursor
                        .getColumnIndex(SceneInfo.SCENE_PROJECTION[i + 3]));
                mCustomCacheMap.put(entry.getKey(), value);
                i++;
            }
        }
        mCustomMap = new LinkedHashMap<String, String>(mCustomCacheMap);
        for (Entry<String, String> entry : mCustomMap.entrySet()) {
            Log.i(TAG, "load: key = "+entry.getKey()+",value = "+entry.getValue());
        }
        cursor.close();
    }

    public void startCustomScene(Context context, String android, String bthci,
            String apcap, String pslog, String armpcm, String dsplog,
            String wifibt, String gnss, String hub, String dsppcm,
            String cpcap, String abe) {
        if (!APLogControl.getInstance().isLogStarted()) {
            APLogControl.getInstance().setLogStatus(true);
        }
        setSceneStatus(SceneStatus.customer);
        setCustomer(KEY_ANDROID_LOG, android);
        setCustomer(KEY_KERNAL_LOG, android);
        setCustomer(KEY_AP_CAP_LOG, apcap);
        setCustomer(KEY_HCI_LOG, bthci);
        setCustomer(KEY_MODEM_LOG, "1");
        setCustomer(KEY_CP_CAP_LOG, cpcap);
        setCustomer(KEY_CM4_LOG, hub);
        setCustomer(KEY_ARM_LOG, pslog);
        setCustomer(KEY_ARM_PCM_LOG, armpcm);
        setCustomer(KEY_AGDSP_LOG, "1");
        setCustomer(KEY_AGDSP_PCM_DUMP_LOG, "1");
        setCustomer(KEY_AGDSP_OUTPUT_LOG, "1");
        setCustomer(KEY_DSP_LOG, dsplog);
        setCustomer(KEY_DSP_PCM_LOG, dsppcm);
        setCustomer(KEY_WCN_LOG, wifibt);
        setCustomer(KEY_GPS_LOG, gnss);
        setCustomer(KEY_CAP_LENGTH, "10000");
        setCustomer(KEY_EVENT_MONITOR, abe);
        setCustomer(KEY_MINI_ORCAAP, "1");
        setCustomer(KEY_MINI_ORCADP, "1");
        new Thread(new Runnable() {
            @Override
            public void run() {

                String sceneName = "IntertCustom";
                commitCustomerOrder(mContext, sceneName);
                mCurrentSceneStatus = SceneStatus.customer;
                LogInfo.getInstance().readData(mContext, sceneName);
                LogSceneManager.getInstance().updateCurrentSelected(mContext,
                        sceneName);
                System.exit(0);
            }
        }).start();

    }

}
