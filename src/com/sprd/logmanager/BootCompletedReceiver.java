
package com.sprd.logmanager;

import com.sprd.logmanager.logcontrol.ATCommand;
import com.sprd.logmanager.logcontrol.ATControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logui.LogInfo;
import com.sprd.logmanager.logui.SceneInfo;
import com.sprd.logmanager.database.LogSceneManager;
public class BootCompletedReceiver extends BroadcastReceiver {

    public static final String TAG = "LogManagerBootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        /* for bug 1041283  start,  sometime later will remove this function*/ 
        if(APLogControl.getInstance().isLogStarted()&& (SceneInfo.SCENE_VOICE.equals(LogSceneManager.getInstance()
                .getCurrentSelectedSceneDB(context)))){
            Log.d(TAG, "voice scene ,need resend at commond to open dsp pcm log");
            new Thread(new Runnable() {
                public void run() {
                    ATControl.sendAt(ATCommand.ENG_SET_DSP_OPEN, "atchannel0");
                    Log.d(TAG, "reopen voice scene done ");
                }
            }).start();
        }
        /* for bug 1041283  end*/
    }

    public  void doBootAction(Context context){
        /**
         * create modem assert listener thread under situation as follow: 1.boot completed 2.when
         * EngineerMode Process has been killed
         */
        //Uri uri = intent.getData();
        Log.i(TAG, "receive boot complete");
    }

   /* private static void changeLogdBufferSizeForCTS() {
         //for cts test failed bug:828166
        String currentValue = SystemProperties.get("persist.logd.size");
        if (TextUtils.isEmpty(currentValue)) {
            currentValue = LogUtils.defaultLogdSizeValue();
            try {
                long size = Long.parseLong(currentValue);
                if(size < 262144){
                    SystemProperties.set("persist.logd.size", "262144");
                    SystemProperties.set("ctl.start", "logd-reinit");
                }
            }catch(Exception e) {

            }
        }
    }*/

}
