package com.sprd.logmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sprd.logmanager.logui.LogInfo;
import com.sprd.logmanager.logui.SceneInfo;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "DBHelper";
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "logmanager.db";
    public static final String TABLE_NAME_SCENE_INFO = "sceneinfo";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String CHECKED = "checked";
    public static final String KEY_ANDROID = "android";
    public static final String KEY_KERNEL = "kernel";
    public static final String KEY_APCAP = "apcap";
    public static final String KEY_BTHCI = "bthci";
    public static final String KEY_MODEM = "modem";
    public static final String KEY_CPCAP = "cpcap";
    public static final String KEY_CM4 = "cm4";
    public static final String KEY_ARM = "arm";
    public static final String KEY_ARMPCM = "armpcm";
    public static final String KEY_AGDSP = "agdsp";
    public static final String KEY_AGDSP_PCM = "agdsp_pcm";
    public static final String KEY_AGDSP_OUTPUT = "agdsp_output";
    public static final String KEY_DSP = "dsp";
    public static final String KEY_DSP_PCM = "dsp_pcm";
    public static final String KEY_WCN = "wcn";
    public static final String KEY_GPS = "gps";
    public static final String KEY_CAP_LENGTH = "cap_length";
    public static final String KEY_EVENTMONITOR = "eventmonitor";
    public static final String KEY_ORCAAP = "orcaap";
    public static final String KEY_ORCADP = "orcadp";
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;

    public synchronized SQLiteDatabase openWritableDatabase() {
        if (mWritableDatabase == null) {
            mWritableDatabase = getWritableDatabase();
        }
        return mWritableDatabase;
    }

    public synchronized SQLiteDatabase openReadableDatabase() {
        if (mReadableDatabase == null) {
            mReadableDatabase = getReadableDatabase();
        }
        return mReadableDatabase;
    }

    public synchronized void closeReadableDatabase() {
        if (mReadableDatabase != null && mReadableDatabase.isOpen()) {
            mReadableDatabase.close();
            mReadableDatabase = null;
        }
    }

    public synchronized void closeWritableDatabase() {
        if (mWritableDatabase != null && mWritableDatabase.isOpen()) {
            mWritableDatabase.close();
            mWritableDatabase = null;
        }
    }

    public synchronized void closeDatabase() {
        closeReadableDatabase();
        closeWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate");

        // Create Scene table
        createSceneTable(sqLiteDatabase);
    }

    private void createSceneTable(SQLiteDatabase sqLiteDatabase) {
        LogInfo logInfo = LogInfo.getInstance();
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME_SCENE_INFO + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, " + CHECKED
                + " TEXT, " + KEY_ANDROID + " TEXT, " + KEY_KERNEL
                + " TEXT, " + KEY_APCAP + " TEXT, " + KEY_BTHCI + " TEXT, " + KEY_MODEM
                + " TEXT, " + KEY_CPCAP + " TEXT, " + KEY_CM4 + " TEXT, " + KEY_ARM
                + " TEXT, " + KEY_ARMPCM + " TEXT, " + KEY_AGDSP + " TEXT, " + KEY_AGDSP_PCM
                + " TEXT, " + KEY_AGDSP_OUTPUT + " TEXT, " + KEY_DSP + " TEXT, " + KEY_DSP_PCM
                + " TEXT, " + KEY_WCN + " TEXT, " + KEY_GPS
                + " TEXT, " + KEY_CAP_LENGTH
                + " TEXT, " + KEY_EVENTMONITOR
                + " TEXT, " + KEY_ORCAAP
                + " TEXT, " + KEY_ORCADP
                + " TEXT);");

        // insert data
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_NORMAL + "'," + 1
                + logInfo.getSceneParam(SceneInfo.SCENE_NORMAL)
                + ");");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_DATA + "'," + 0
                + logInfo.getSceneParam(SceneInfo.SCENE_DATA)
                + ");");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_VOICE + "'," + 0
                + logInfo.getSceneParam(SceneInfo.SCENE_VOICE)
                + ");");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_WCN + "'," + 0
                + logInfo.getSceneParam(SceneInfo.SCENE_WCN)
                + ");");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_USER+ "'," + 0
                + logInfo.getSceneParam(SceneInfo.SCENE_USER)
                + ");");
        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_NAME_SCENE_INFO + " VALUES ("
                + " NULL, "
                + "'" + SceneInfo.SCENE_CUSTOMER + "'," + 0
                + logInfo.getSceneParam(SceneInfo.SCENE_CUSTOMER)
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
            int newVersion) {
        Log.d(TAG, "onUpgrade oldVersion " + oldVersion + " newVersion "
                + newVersion);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SCENE_INFO);
    }

}
