package com.sprd.logmanager.logui;

import com.sprd.logmanager.R;
import com.sprd.logmanager.logcontrol.ATCommand;
import com.sprd.logmanager.logcontrol.ATControl;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.preference.TwoStatePreference;


public class GSMIQActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String TAG = "GSMIQActivity";
    private static final String KEY_AUTIO_SWITCH = "audio_iq";
    private static final String KEY_COMMUNICATE_SWITCH = "communicate_iq";

    private static final int OPEN_AUTIO_SWITCH = 0;
    private static final int CLOSE_AUTIO_SWITCH = 1;
    private static final int OPEN_COMMUNICATE_SWITCH = 2;
    private static final int CLOSE_COMMUNICATE_SWITCH = 3;

    private TwoStatePreference mAutionSwitch;
    private TwoStatePreference mCommunicateSwitch;
    SharedPreferences pref;

    private mGsmIqHandler mGsmIqHandler;
    private Handler uiThread = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.pref_gsm_iq);
        mAutionSwitch = (TwoStatePreference) findPreference(KEY_AUTIO_SWITCH);
        mAutionSwitch.setOnPreferenceChangeListener(this);
        mCommunicateSwitch = (TwoStatePreference) findPreference(KEY_COMMUNICATE_SWITCH);
        mCommunicateSwitch.setOnPreferenceChangeListener(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mGsmIqHandler = new mGsmIqHandler(ht.getLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAutionSwitch != null) {
            mAutionSwitch.setChecked(pref.getBoolean("audio_switch", false));
        }
        if (mCommunicateSwitch != null) {
            mCommunicateSwitch
                    .setChecked(pref.getBoolean("communicate_switch", false));
        }
    }

    @Override
    protected void onDestroy() {
        if (mGsmIqHandler != null) {
            mGsmIqHandler.getLooper().quit();
        }
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object object) {
        int logType = 0;
        if (preference == mAutionSwitch) {
            if (mAutionSwitch.isChecked()) {
                logType = CLOSE_AUTIO_SWITCH;
            } else {
                logType = OPEN_AUTIO_SWITCH;
            }
        } else if (preference == mCommunicateSwitch) {
            if (mCommunicateSwitch.isChecked()) {
                logType = CLOSE_COMMUNICATE_SWITCH;
            } else {
                logType = OPEN_COMMUNICATE_SWITCH;
            }
        }
        Message message = mGsmIqHandler.obtainMessage(logType);
        mGsmIqHandler.sendMessage(message);
        return true;
    }

    class mGsmIqHandler extends Handler {
        public mGsmIqHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String responseValue;
            switch (msg.what) {
            case OPEN_AUTIO_SWITCH:
                responseValue = ATControl.sendAt(ATCommand.ENG_SET_DSP_OPEN, "atchannel0");
                if (responseValue != null
                        && responseValue.contains(ATControl.AT_OK)) {
                    pref.edit().putBoolean("audio_switch", true).commit();
                } else {
                    uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutionSwitch.setChecked(false);
                        }
                    });
                }
                break;
            case CLOSE_AUTIO_SWITCH:
                responseValue = ATControl.sendAt(ATCommand.ENG_SET_DSP_CLOSE, "atchannel0");
                if (responseValue != null
                        && responseValue.contains(ATControl.AT_OK)) {
                    pref.edit().putBoolean("audio_switch", false).commit();
                } else {
                    uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            mAutionSwitch.setChecked(false);
                        }
                    });
                }
                break;
            case OPEN_COMMUNICATE_SWITCH:
                responseValue = ATControl.sendAt(ATCommand.ENG_AT_SETIQLOGOPEN, "atchannel0");
                if (responseValue != null
                        && responseValue.contains(ATControl.AT_OK)) {
                    pref.edit().putBoolean("communicate_switch", true).commit();
                } else {
                    uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            mCommunicateSwitch.setChecked(false);
                        }
                    });
                }
                break;
            case CLOSE_COMMUNICATE_SWITCH:
                responseValue = ATControl.sendAt(ATCommand.ENG_AT_SETIQLOGClose, "atchannel0");
                if (responseValue != null
                        && responseValue.contains(ATControl.AT_OK)) {
                    pref.edit().putBoolean("communicate_switch", false).commit();
                } else {
                    uiThread.post(new Runnable() {
                        @Override
                        public void run() {
                            mCommunicateSwitch.setChecked(true);
                        }
                    });
                }
                break;
            default:
                break;

            }
        }
    }
}