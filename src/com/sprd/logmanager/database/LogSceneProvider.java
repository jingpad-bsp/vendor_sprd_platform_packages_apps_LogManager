package com.sprd.logmanager.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import java.util.ArrayList;

public class LogSceneProvider extends ContentProvider {
    private static final String TAG = "LogSceneProvider";
    private SQLiteDatabase mContactDatabase;
    private String[] mTables = new String[] { DBHelper.TABLE_NAME_SCENE_INFO, };
    private static final int BASE = 8;

    private static final int SCENE = 0x0000;
    private static final int SCENE_ID = 0x0001;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        // URI matching table
        UriMatcher matcher = sURIMatcher;
        matcher.addURI(LogSceneContent.AUTHORITY, "sceneinfo", SCENE);
        matcher.addURI(LogSceneContent.AUTHORITY, "sceneinfo/#", SCENE_ID);
    }

    synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mContactDatabase != null) {
            return mContactDatabase;
        }

        DBHelper helper = new DBHelper(context);
        mContactDatabase = helper.getWritableDatabase();
        return mContactDatabase;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    /**
     * Wrap the UriMatcher call so we can throw a runtime exception if an
     * unknown Uri is passed in
     *
     * @param uri
     *            the Uri to match
     * @return the match value
     */
    private static int findMatch(Uri uri, String methodName) {
        int match = sURIMatcher.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        Log.d(TAG, methodName + ": uri=" + uri + ", match is " + match);
        return match;
    }

    private void notifyChange(int match) {
        Context context = getContext();
        Uri notify = LogSceneContent.CONTENT_URI;
        switch (match) {
        case SCENE:
        case SCENE_ID:
            notify = LogSceneContent.SceneInfo.CONTENT_URI;
            break;
        }
        ContentResolver resolver = context.getContentResolver();
        resolver.notifyChange(notify, null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = findMatch(uri, "delete");
        Context context = getContext();

        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match >> BASE];
        Log.d(TAG, "delete data from table " + table);
        int count = 0;
        switch (match) {
        // case SCENE:
        // case SCENE_ID:
        default:
            count = db.delete(table, buildSelection(match, uri, selection),
                    selectionArgs);
        }
        if (count > 0)
            notifyChange(match);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = findMatch(uri, "insert");
        Context context = getContext();

        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match >> BASE];
        Log.d(TAG, "insert values into table " + table);
        if (values.containsKey(DBHelper.ID)) {
            values.remove(DBHelper.ID);
        }
        long id = db.insert(table, null, values);
        if (id > 0) {
            notifyChange(match);
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        int match = findMatch(uri, "query");
        Context context = getContext();
        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match >> BASE];
        Log.d(TAG, "query table " + table);
        Cursor result = null;
        switch (match) {
        // case SCENE:
        // case SCENE_ID:
        default:
              try{
                  result = db.query(table, projection, selection, selectionArgs,null, null, sortOrder);
              }catch(RuntimeException e){
                  e.printStackTrace();
                  DBHelper helper = new DBHelper(context);
                  db.execSQL("DROP TABLE IF EXISTS " + "sceneinfo");
                  helper.onCreate(db);
                  db = getDatabase(context);
                  result = db.query(table, projection, selection, selectionArgs,null, null, sortOrder);
              }
        }
        if (result != null)
            result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int match = findMatch(uri, "update");
        Context context = getContext();

        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match >> BASE];
        Log.d(TAG, "update data for table " + table);
        int count = 0;
        switch (match) {
        // case SCENE:
        // case SCENE_ID:
        default:
            count = db.update(table, values,
                    buildSelection(match, uri, selection), selectionArgs);
        }
        if (count > 0)
            notifyChange(match);
        return count;
    }

    private String buildSelection(int match, Uri uri, String selection) {
        long id = -1;
        switch (match) {
        case SCENE_ID:
            try {
                id = ContentUris.parseId(uri);
            } catch (java.lang.NumberFormatException e) {
                e.printStackTrace();
            }
            break;
        }

        if (id == -1) {
            return selection;
        }
        Log.d(TAG, "find id from Uri#" + id);
        StringBuilder sb = new StringBuilder();
        sb.append(DBHelper.ID);
        sb.append("=").append(id);
        if (!TextUtils.isEmpty(selection)) {
            sb.append(" and ");
            sb.append(selection);
        }
        Log.d(TAG, "rebuild selection#" + sb.toString());
        return sb.toString();
    }

       //added by sprd  for Bug 796296 start
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation>operations)
                throws OperationApplicationException{
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        db.beginTransaction();
        try {
            ContentProviderResult[]results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
       //modified by sprd  for Bug 796296 end
}
