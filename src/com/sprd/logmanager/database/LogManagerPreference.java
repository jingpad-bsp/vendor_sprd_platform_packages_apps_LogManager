package com.sprd.logmanager.database;

import com.sprd.logmanager.logui.LogInfo;
import com.sprd.logmanager.utils.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class LogManagerPreference {
    private final static String TAG = "LogManagerPreference";
    private final static String COMMON_PREFERENCE = "common";
    private final static String SETTINGS_PREFERENCE = "settings";
    private final static String FIRST_BOOT_FLAG = "first_boot";
    private final static String MANUAL_MODE = "key_manualassert";
    private final static String SHOW_LOG_MANAGER_IN_NOTIFICATION = "show_logmanager";
    private final static String SYS_DUMP = "sysdump";
    private final static String LOG_EANBLE = "log_enable";
    private final static String FIRST_TIME_START_LOG = "first_start_log";
    private final static String LOG_SCENE = "log_scene";
    private final static String AP_LOG_LEVEL = "ap_log_level";

    private Context mContext;
    private SharedPreferences mSettingsPreferences;
    private SharedPreferences mCommonPreferences;
    private SharedPreferences mDefaultPreferences;
    private SharedPreferences.Editor mCommonEditor;
    private SharedPreferences.Editor mSettingsEditor;
    private SharedPreferences.Editor mDefaultEditor;
    static LogManagerPreference sLogManagerPreference;


    public String getProp(String name) {
       String res = mCommonPreferences.getString(name, "");
       return res;
    }

    public void setProp(String name,String value) {
        mCommonEditor.putString(name, value);
        mCommonEditor.commit();
    }



    private LogManagerPreference() {
        // TODO Auto-generated constructor stub

    }

    public void init(Context context) {
        mContext = context;
        mCommonPreferences = context.getSharedPreferences(COMMON_PREFERENCE,
                Context.MODE_PRIVATE);
        mSettingsPreferences = context.getSharedPreferences(
                SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        mDefaultPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mCommonEditor = mCommonPreferences.edit();
        mSettingsEditor = mSettingsPreferences.edit();
        mDefaultEditor = mDefaultPreferences.edit();
    }

    public static LogManagerPreference getInstanse() {
        if (sLogManagerPreference == null) {
            sLogManagerPreference = new LogManagerPreference();
        }
        return sLogManagerPreference;
    }

    public boolean isFirstBoot() {
        boolean res = mCommonPreferences.getBoolean(FIRST_BOOT_FLAG, true);
        return res;
    }

    public void setFirstBoot(boolean firstBoot) {
        mCommonEditor.putBoolean(FIRST_BOOT_FLAG, firstBoot);
        mCommonEditor.commit();
    }

    public long getTcpCapSize(long defaultSize) {
        /* SRPD bug 823985:Set Bugreport Enable enable default. */
        long size = mSettingsPreferences.getLong("tcpcap_size", defaultSize);
        return size;
    }

    public void setTcpCapSize(long size) {
        mSettingsEditor.putLong("tcpcap_size", size);
        mSettingsEditor.commit();
        //LogInfo.getInstance().setCustomer(LogInfo.KEY_CAP_LENGTH, "" + size);
        //Log.d(TAG, "set YlogCore_CapLogLengthController size :" + size);
        //LogInfo.getInstance().writeData(
              //  mContext,
             //   LogSceneManager.getInstance().getCurrentSelectedSceneDB(
              //          mContext));
    }

    public void reset() {
        mSettingsEditor.remove("tcpcap_size");
        mSettingsEditor.remove("wcn_dest");
        mSettingsEditor.remove("modem_dest");
        mSettingsEditor.remove("bugreport_enable");
        mSettingsEditor.commit();
    }

    public int getWcnDest(int defaultDest) {
        int dest = mSettingsPreferences.getInt("wcn_dest", defaultDest);
        return dest;
    }

    public void setWcnDest(int dest) {
        mSettingsEditor.putInt("wcn_dest", dest);
        mSettingsEditor.commit();
    }

    public int getModemDest(int defaultDest) {
        int dest = mSettingsPreferences.getInt("modem_dest", defaultDest);
        return dest;
    }

    public void setModemDest(int dest) {
        mSettingsEditor.putInt("modem_dest", dest);
        mSettingsEditor.commit();
    }

    public boolean getBugreportEnable() {
        boolean enable = mSettingsPreferences.getBoolean("bugreport_enable",
                true);
        return enable;
    }

    public void setBugreportEnable(boolean enable) {
        mSettingsEditor.putBoolean("bugreport_enable", enable);
        mSettingsEditor.commit();
    }

    public boolean showLogManagerInNotification() {
        boolean show = mSettingsPreferences.getBoolean(
                SHOW_LOG_MANAGER_IN_NOTIFICATION, false);
        return show;
    }

    public void setshowLogManagerInNotification(boolean show) {
        mSettingsEditor.putBoolean(SHOW_LOG_MANAGER_IN_NOTIFICATION, show);
        mSettingsEditor.commit();
    }

    public void setSysDumpEnable(boolean enable) {
        mSettingsEditor.putBoolean(SYS_DUMP, enable);
        mSettingsEditor.commit();
    }

    public boolean isSysDumpEnable() {
        boolean enable = mSettingsPreferences.getBoolean(SYS_DUMP, false);
        return enable;
    }

    public boolean isFirstStartLog() {
        boolean first = mCommonPreferences.getBoolean(FIRST_TIME_START_LOG,
                true);
        return first;
    }

    public void setFirstStartLog(boolean first) {
        mCommonEditor.putBoolean(FIRST_TIME_START_LOG, first);
        mCommonEditor.commit();
    }

    public String getScene() {
        String scene = mSettingsPreferences.getString(LOG_SCENE, "");
        return scene;
    }

    public void setScene(String scene) {
        mSettingsEditor.putString(LOG_SCENE, scene);
        mSettingsEditor.commit();
    }

    public void setLogEnable(boolean enable) {
        mSettingsEditor.putBoolean(LOG_EANBLE, enable);
        mSettingsEditor.commit();
    }

    public boolean isLogEnable() {
        boolean defaultValue = true;
        if (CommonUtils.isUserBuild()) {
            defaultValue = false;
        }
        boolean enable = mSettingsPreferences.getBoolean(LOG_EANBLE,
                defaultValue);
        return enable;
    }

    public int getApLogLevel() {
        int level = mSettingsPreferences.getInt(AP_LOG_LEVEL, 0);
        return level;
    }

    public void setApLogLevel(int level) {
        mSettingsEditor.putInt(AP_LOG_LEVEL, level);
        mSettingsEditor.commit();
    }
}
