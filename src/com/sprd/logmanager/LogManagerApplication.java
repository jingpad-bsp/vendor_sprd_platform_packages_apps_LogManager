package com.sprd.logmanager;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.ATCommand;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.ATControl;
import com.sprd.logmanager.logcontrol.WcnControl;
import com.sprd.logmanager.logui.LogInfo;
import com.sprd.logmanager.utils.CommonUtils;
import android.app.Activity;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Process;

public class LogManagerApplication extends Application {

    private APLogControl mApLogControl;
    private CPLogControl mCpLogControl;
    private WcnControl mWcnControl;
    private Notification mNotification;
    private LogManagerPreference mLogManagerPreference;
    private static TelephonyManager sTelephonyManager;
    private NotificationManager mNotificationManager;
    private LogInfo mLogInfo;
    private static final String TAG = "LogManagerApplication";
    private static LogManagerApplication mInstance;
    private static Context mContext;
    private Activity mActivity = null;
    @Override
    public void onCreate() {
        super.onCreate();
        sTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLogManagerPreference = LogManagerPreference.getInstanse();
        mLogManagerPreference.init(getApplicationContext());
        initLogController();
        checkLogNotification();
        mLogInfo = LogInfo.getInstance();
        mLogInfo.setContext(getApplicationContext());
        mContext = getApplicationContext();
        mInstance = this;
        initGlobeActivity();
    }

    private void checkLogNotification() {
        // fix bug 735256
        /*
         * UserManager userManager = (UserManager)
         * context.getSystemService(Context.USER_SERVICE); if(userManager ==
         * null || userManager.isGuestUser()) { return; }
         */
        if (LogManagerPreference.getInstanse().showLogManagerInNotification()) {
            Log.d(TAG, "log notification opened, start ylog service");
            mNotificationManager.notify(0, mNotification);
        }
    }

    private void initLogController() {
        mApLogControl = APLogControl.getInstance();
        mCpLogControl = CPLogControl.getInstance();
        mWcnControl = WcnControl.getInstance();
        mApLogControl.setContext(getApplicationContext());
        mCpLogControl.setContext(getApplicationContext());
        initCpLog();
    }

    private void initCpLog() {
        boolean isUser = CommonUtils.isUserBuild();
        boolean isFirstBoot = mLogManagerPreference.isFirstBoot();
        if (isFirstBoot) {
            Log.e(TAG, "firstboot");
            mLogManagerPreference.setFirstBoot(false);
            if (!isUser) {
                Log.e(TAG, "is userdebug");
               // enableCpLog(true);
            } else {
                Log.e(TAG, "is user");
                //enableCpLog(false);
            }
        } else {
            Log.e(TAG, "not firstboot");
        }
        new Thread(new Runnable() {
            public void run() {
                String armLogSwitch = ATControl.sendAt(
                        ATCommand.ENG_AT_GETARMLOG1, "atchannel0");
                Log.d(TAG, "ARM LOG Switch status is: " + armLogSwitch);
            }
        }).start();
    }

    public static TelephonyManager getTelephonyManager() {
        return sTelephonyManager;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    public  Context getContext(){
        return mContext;
    }

    private void initGlobeActivity(){
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mActivity = activity;
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
            /** Unused implementation **/
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
            }
            @Override
            public void onActivityPaused(Activity activity) {
            }
            @Override
            public void onActivityStopped(Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
        });
    }

    public static LogManagerApplication getInstance(){
        return mInstance;
    }

    public Activity getCurrentActivity() {
        return mActivity;
    }



    public boolean isAdminUser(){
      Context context=mContext;
      UserHandle uh = Process.myUserHandle();
      UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
      if(null != um){
          long userSerialNumber = um.getSerialNumberForUser(uh);
          return 0 == userSerialNumber;
      }else{
          return false;
      }
    }

}
