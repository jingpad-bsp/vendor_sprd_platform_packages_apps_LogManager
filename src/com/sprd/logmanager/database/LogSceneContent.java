package com.sprd.logmanager.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class LogSceneContent {
    public static final String AUTHORITY = "com.sprd.logmanager.provider.LogSceneProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String RECORD_ID = "_id";

    public static final String[] COUNT_COLUMNS = new String[] { "count(*)" };
    public static final String[] ID_PROJECTION = new String[] { RECORD_ID };
    public static final int ID_PROJECTION_COLUMN = 0;

    public static final String ID_SELECTION = RECORD_ID + " =?";

    public static class SceneInfo extends LogSceneContent {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                LogSceneContent.CONTENT_URI, "sceneinfo");

        public static String[] PROJECTION = new String[] { DBHelper.ID,
                DBHelper.NAME, DBHelper.CHECKED, };

        public static final int INDEX_ID = 0;
        public static final int INDEX_NAME = 1;
        public static final int INDEX_CHECKED = 2;
    }

    public static class CloseDatabase extends LogSceneContent {
        private static final Uri CONTENT_URI = Uri.withAppendedPath(
                LogSceneContent.CONTENT_URI, "closedatabase");

        public static void closeDatabase(ContentResolver cr) {
            Cursor c =cr.query(CONTENT_URI, null, null, null, null);
            c.close();
        }
    }

    public static long existed(ContentResolver cr, Uri uri, String where,
            String[] selectionArgs) {
        long id = -1;
        Cursor c = cr.query(uri, ID_PROJECTION, where, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                id = c.getLong(0);
            }
            c.close();
        }
        return id;
    }

    public static int update(ContentResolver cr, Uri uri, ContentValues values,
            String where, String[] selectionArgs) {
        return cr.update(uri, values, where, selectionArgs);
    }

    public static Uri insert(ContentResolver cr, Uri uri, ContentValues values) {
        return cr.insert(uri, values);
    }

    public static int delete(ContentResolver cr, Uri uri, String where,
            String[] selectionArgs) {
        return cr.delete(uri, where, selectionArgs);
    }
}
