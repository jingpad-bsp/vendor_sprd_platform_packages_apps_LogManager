
package com.sprd.logmanager.logui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.sprd.logmanager.database.DBHelper;
import com.sprd.logmanager.database.LogSceneContent;

import android.content.ContentProviderOperation;

import java.util.ArrayList;

public class SceneInfo {
    private String mID;
    private String mSceneName;
    private boolean mCurrentChecked;
    public static final int SCENE_DEFAULT_COUNT = 5; // not contain custom scene
    public static final String SCENE_NORMAL = "normal";
    public static final String SCENE_DATA = "data";
    public static final String SCENE_VOICE = "voice";
    public static final String SCENE_MODEM = "modem";
    public static final String SCENE_WCN = "wcn";
    public static final String SCENE_USER = "user";
    public static final String SCENE_CUSTOMER = "custom";
    public static final String SCENE_CLOSE = "close";
    public static final String WHERE_SCENE_NAME = DBHelper.NAME + "=?";
    public static final String WHERE_NOT_SCENE_NAME = DBHelper.NAME + "!=?";
    public static final String WHERE_SCENE_SELECTED = DBHelper.CHECKED + "=1";
    public static final String WHERE_CUSTOMER_SCENE = DBHelper.ID + ">6";

    public static String[] SCENE_PROJECTION_LESS = new String[] {
            DBHelper.ID, DBHelper.NAME, DBHelper.CHECKED
    };

    public static String[] SCENE_PROJECTION = new String[] {
            DBHelper.ID, DBHelper.NAME, DBHelper.CHECKED, DBHelper.KEY_ANDROID,  DBHelper.KEY_KERNEL,
            DBHelper.KEY_APCAP, DBHelper.KEY_BTHCI, DBHelper.KEY_MODEM,
            DBHelper.KEY_CPCAP, DBHelper.KEY_CM4, DBHelper.KEY_ARM, DBHelper.KEY_ARMPCM,
            DBHelper.KEY_AGDSP, DBHelper.KEY_AGDSP_PCM, DBHelper.KEY_AGDSP_OUTPUT,
            DBHelper.KEY_DSP, DBHelper.KEY_DSP_PCM, DBHelper.KEY_WCN, DBHelper.KEY_GPS,
            DBHelper.KEY_CAP_LENGTH,DBHelper.KEY_EVENTMONITOR,DBHelper.KEY_ORCAAP,DBHelper.KEY_ORCADP
    };

    public static String[] SCENE_AP_PROJECTION = new String[] {
            DBHelper.KEY_ANDROID, DBHelper.KEY_KERNEL, DBHelper.KEY_APCAP, DBHelper.KEY_BTHCI,
    };

    public static String[] SCENE_MODEM_PROJECTION = new String[] {
            DBHelper.KEY_ARM, DBHelper.KEY_ARMPCM, DBHelper.KEY_AGDSP, DBHelper.KEY_AGDSP_PCM,
            DBHelper.KEY_AGDSP_OUTPUT, DBHelper.KEY_DSP
    };

    public static String[] SCENE_CONNECTIVITY_PROJECTION = new String[] {
            DBHelper.KEY_WCN, DBHelper.KEY_GPS
    };

    public static String[] SCENE_OTHER_PROJECTION = new String[] {
            DBHelper.KEY_CPCAP, DBHelper.KEY_CM4, DBHelper.KEY_DSP_PCM, DBHelper.KEY_CAP_LENGTH,DBHelper.KEY_EVENTMONITOR,DBHelper.KEY_ORCAAP,DBHelper.KEY_ORCADP
    };

    public static final String[] SCENE_ARRAY = new String[] {
           "android" ,"kernel log",
            "ap cap log",  "cp cap log","bt hci log", "cm4 log", "ps log", "arm pcm log",
            "agdsp log", "agdsp pcm log", "agdsp output", "dsp log", "dsp pcm log", "wcn log",
            "gnss log",  "cap length","modem event monitor","orca ap log","orca dp log"
    };

    public static final String[] SCENE_AP_ARRAY = new String[] {
            "android","kernel log",
            "ap cap log", "bt hci log"
    };

    public static final String[] SCENE_MODEM_ARRAY = new String[] {
            "ps log", "arm pcm log", "agdsp log", "agdsp pcm log", "agdsp output", "dsp log"
    };

    public static final String[] SCENE_CONNECTIVITY_ARRAY = new String[] {
            "wcn log", "gnss log"
    };

    public static final String[] SCENE_OTHER_ARRAY = new String[] {
            "cp cap log", "sensorhub log", "dsp pcm log", "cap length", "modem event monitor","orca ap log","orca dp log"
    };

    public void setSceneId(String id) {
        mID = id;
    }

    public void setSceneName(String name) {
        mSceneName = name;
    }

    public void setCurrentChecked(boolean check) {
        mCurrentChecked = check;
    }

    public String getSceneId() {
        return mID;
    }

    public String getSceneName() {
        return mSceneName;
    }

    public boolean getCurrentChecked() {
        return mCurrentChecked;
    }

    public static Cursor getAllSceneInfo(ContentResolver cr) {
        return cr.query(LogSceneContent.SceneInfo.CONTENT_URI, SCENE_PROJECTION, null, null, null);
    }

    public static Cursor getSceneInfoByName(ContentResolver cr, String sceneName) {
        return cr.query(LogSceneContent.SceneInfo.CONTENT_URI, SCENE_PROJECTION, WHERE_SCENE_NAME,
                new String[] {
                    sceneName
                }, null);
    }

    public static void updateCurrentSelected(ContentResolver cr, String sceneName) {
       //modified by sprd  for Bug 796296 start
       /* ContentValues value = new ContentValues();
        value.put(DBHelper.CHECKED, 0);
        cr.update(LogSceneContent.SceneInfo.CONTENT_URI, value, WHERE_NOT_SCENE_NAME, new String[] {
            sceneName
        });
        value.put(DBHelper.CHECKED, 1);
        cr.update(LogSceneContent.SceneInfo.CONTENT_URI, value, WHERE_SCENE_NAME, new String[] {
            sceneName
        });*/
        ArrayList<ContentProviderOperation>ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newUpdate(LogSceneContent.SceneInfo.CONTENT_URI)
                    .withSelection(WHERE_NOT_SCENE_NAME, new String[]{sceneName})
                    .withValue(DBHelper.CHECKED, "0")
                    .build());
        ops.add(ContentProviderOperation.newUpdate(LogSceneContent.SceneInfo.CONTENT_URI)
                    .withSelection(WHERE_SCENE_NAME, new String[]{sceneName})
                    .withValue(DBHelper.CHECKED, "1")
                    .build());
        try {
            cr.applyBatch(LogSceneContent.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
       //modified by sprd  for Bug 796296 end
    }

    public static boolean deleteSceneByName(ContentResolver cr, String sceneName) {
        int deleted = cr.delete(LogSceneContent.SceneInfo.CONTENT_URI, WHERE_SCENE_NAME,
                new String[] {
                    sceneName
                });
        return deleted > 0;
    }

    public static boolean deleteCustomerScene(ContentResolver cr) {
        int deleted = cr.delete(LogSceneContent.SceneInfo.CONTENT_URI, WHERE_CUSTOMER_SCENE, null);
        return deleted > 0;
    }

    public static String getCurrentSelected(ContentResolver cr) {
        Cursor cursor = cr.query(LogSceneContent.SceneInfo.CONTENT_URI, SCENE_PROJECTION_LESS,
                WHERE_SCENE_SELECTED, null, null);

        if (cursor != null && cursor.moveToNext()) {
            String ret=cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            cursor.close();
            return ret;
        }
        if (cursor != null){
            cursor.close();
        }
        return null;
    }

    public static boolean exists(ContentResolver cr, String sceneName) {
        return LogSceneContent.existed(cr, LogSceneContent.SceneInfo.CONTENT_URI, WHERE_SCENE_NAME,
                new String[] {
                    sceneName
                }) > 0;
    }

    public static boolean insert(ContentResolver cr, ContentValues value) {
        return cr.insert(LogSceneContent.SceneInfo.CONTENT_URI, value) != null;
    }

    public static boolean update(ContentResolver cr, ContentValues value, String sceneName) {
        return cr.update(LogSceneContent.SceneInfo.CONTENT_URI, value, WHERE_SCENE_NAME,
                new String[] {
                    sceneName
                }) > 0;
    }

    public static long count(ContentResolver cr) {
        Cursor cursor = cr.query(LogSceneContent.SceneInfo.CONTENT_URI, SCENE_PROJECTION_LESS,
                null, null, null);
        if (cursor != null){
            long ret=cursor.getCount();
            cursor.close();
            return ret;
        }
        return 0;
    }
}
