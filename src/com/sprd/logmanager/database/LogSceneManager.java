package com.sprd.logmanager.database;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.sprd.logmanager.R;
import com.sprd.logmanager.logui.LogSettingItemListActivity;
import com.sprd.logmanager.logui.SceneInfo;
import com.sprd.logmanager.logui.SceneListAdapter;
import com.sprd.logmanager.logui.SceneSettingActivity;
import com.sprd.logmanager.logui.UserDefinedActivity;

public class LogSceneManager {
    private static final String TAG = "LogSceneManager";
    private static LogSceneManager mInstance = new LogSceneManager();

    public static final String EXTRA_CUSTOM_NAME = "custom_name";
    public static final String EXTRA_CUSTOM_EDIT = "custom_edit";

    private LogSceneManager() {
    }

    public static LogSceneManager getInstance() {
        return mInstance;
    }

    public Cursor getAllSceneInfo(Context context) {
        return SceneInfo.getAllSceneInfo(context.getContentResolver());
    }

    public List<SceneInfo> getAllSceneInfoList(Context context) {
        List<SceneInfo> sceneList = new ArrayList<SceneInfo>();
        SceneInfo sceneinfo = null;
        Cursor cursor = getAllSceneInfo(context);
        while (cursor.moveToNext()) {
            sceneinfo = new SceneInfo();
            sceneinfo.setSceneId(cursor.getString(cursor
                    .getColumnIndex(DBHelper.ID)));
            String name = cursor.getString(cursor
                    .getColumnIndex(DBHelper.NAME));
            sceneinfo.setSceneName(getSceneString(context, name));
            sceneinfo.setCurrentChecked(cursor.getString(
                    cursor.getColumnIndex(DBHelper.CHECKED)).equals("1"));
            sceneList.add(sceneinfo);
        }
        cursor.close();
        return sceneList;
    }

    public Cursor getSceneInfoByName(Context context, String sceneName) {
        if(TextUtils.isEmpty(sceneName)) {
            sceneName = SceneInfo.SCENE_NORMAL;
        }
        return SceneInfo.getSceneInfoByName(context.getContentResolver(), sceneName);
    }

    public void updateCurrentSelected(Context context, int position) {
        SceneInfo.updateCurrentSelected(context.getContentResolver(),
                getDBSceneColumn(context, getAllSceneInfoList(context).get(position).getSceneName()));
    }

    public void updateCurrentSelected(Context context, String name) {
        SceneInfo.updateCurrentSelected(context.getContentResolver(),
                getDBSceneColumn(context, name));
    }

    public String getCurrentSelectedSceneName(Context context) {
        return getSceneString(context, SceneInfo.getCurrentSelected(context.getContentResolver()));
    }

    public String getCurrentSelectedSceneDB(Context context) {
        return SceneInfo.getCurrentSelected(context.getContentResolver());
    }

    public boolean deleteSceneByName(Context context, String name) {
        return SceneInfo.deleteSceneByName(context.getContentResolver(), name);
    }

    public boolean deleteCustomerScene(Context context) {
        return SceneInfo.deleteCustomerScene(context.getContentResolver());
    }

    public void createSceneDialog(final Context context, final SceneListAdapter sceneListAdapter) {
        View view = LayoutInflater.from(context).inflate(R.layout.ylog_scene_add_dialog, null);
        final EditText mEditText = (EditText) view.findViewById(R.id.scene_edit_text);
        mEditText.setText(SceneInfo.SCENE_CUSTOMER + "_" + createFileName(context));

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setTitle(R.string.add_scene_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (TextUtils.isEmpty(mEditText.getText()
                                        .toString())) {
                                    Toast.makeText(context,
                                            R.string.input_empty_tips,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (mEditText.getText()
                                        .toString().length() > 20) {
                                    Toast.makeText(context,
                                            R.string.input_too_large_tips,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (SceneInfo.exists(
                                        context.getContentResolver(),
                                        mEditText.getText().toString())) {
                                    Toast.makeText(context,
                                            R.string.input_already_exists,
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Intent intent = new Intent();
                                intent.setClass(context, UserDefinedActivity.class);
                                intent.putExtra(EXTRA_CUSTOM_NAME, mEditText.getText().toString());
                                ((SceneSettingActivity)context).startActivityForResult(intent, 0);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }
                        });
        final AlertDialog mAlertDialog = alertBuilder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
    }

    private String createFileName(Context context) {
        long count = SceneInfo.count(context.getContentResolver()) - SceneInfo.SCENE_DEFAULT_COUNT;
        return String.format("%03d", count);
    }

    public void startDetailsActivity(Context context, String name) {
        Intent intent = new Intent(context, LogSettingItemListActivity.class);
        intent.putExtra("name", name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public String getSceneString(Context context, String name) {
        if(SceneInfo.SCENE_NORMAL.equals(name)) {
            return context.getString(R.string.scene_normal);
        } else if(SceneInfo.SCENE_DATA.equals(name)) {
            return context.getString(R.string.scene_data);
        } else if(SceneInfo.SCENE_VOICE.equals(name)) {
            return context.getString(R.string.scene_voice);
        } else if(SceneInfo.SCENE_WCN.equals(name)) {
            return context.getString(R.string.scene_wcn);
        } else if(SceneInfo.SCENE_USER.equals(name)) {
            return context.getString(R.string.scene_user);
        }else if(SceneInfo.SCENE_CUSTOMER.equals(name)) {
            return context.getString(R.string.scene_customer);
        }
        return name;
    }

    public String getDBSceneColumn(Context context, String name) {
        if(context.getString(R.string.scene_normal).equals(name)) {
            return SceneInfo.SCENE_NORMAL;
        } else if(context.getString(R.string.scene_data).equals(name)) {
            return SceneInfo.SCENE_DATA;
        } else if(context.getString(R.string.scene_voice).equals(name)) {
            return SceneInfo.SCENE_VOICE;
        } else if(context.getString(R.string.scene_wcn).equals(name)) {
            return SceneInfo.SCENE_WCN;
        } else if(context.getString(R.string.scene_user).equals(name)) {
            return SceneInfo.SCENE_USER;
        }  else if(context.getString(R.string.scene_customer).equals(name)) {
            return SceneInfo.SCENE_CUSTOMER;
        }
        return name;
    }
}
