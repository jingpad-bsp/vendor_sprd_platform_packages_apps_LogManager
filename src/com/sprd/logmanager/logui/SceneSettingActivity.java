
package com.sprd.logmanager.logui;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sprd.logmanager.R;
import com.sprd.logmanager.database.LogManagerPreference;
import com.sprd.logmanager.database.LogSceneManager;
import com.sprd.logmanager.logcontrol.APLogControl;
import com.sprd.logmanager.logcontrol.CPLogControl;
import com.sprd.logmanager.logcontrol.CPLogStorageSwitch;
import com.sprd.logmanager.logcontrol.WcnControl;
import com.sprd.logmanager.utils.CommonUtils;
import com.sprd.logmanager.utils.PropUtils;

public class SceneSettingActivity extends Activity implements View.OnClickListener,
        OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = "SceneSettingActivity";

    private ProgressDialog mProgressDialog;
    private ListView mListView;
    private SceneListAdapter mSceneListAdapter;
    private SettingPreferenceView mModemMonitor, mSystemDump;
    private APLogControl mApLogControl;
    private CPLogControl mCpLogControl;
    private LogManagerPreference mLogManagerPreference;
    private String SYSDUMP_CONTROL_PROP = "debug.sysdump.enabled";
    private String SYSDUMP_STATUS_PROP = "persist.sys.sysdump";
    private String SYSDUMP_RESET_PROP = "persist.vendor.eng.reset";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ylog_scene);
        mApLogControl = APLogControl.getInstance();
        mCpLogControl = CPLogControl.getInstance();
        mLogManagerPreference = LogManagerPreference.getInstanse();
        if (Build.VERSION.SDK_INT >= 28) {
            SYSDUMP_CONTROL_PROP = "vendor.debug.sysdump.enabled";
            SYSDUMP_STATUS_PROP = "persist.vendor.sysdump";
        }
        initViews();
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.scenelist);
        setListViewHeightBasedOnChildren(mListView);
        mSceneListAdapter = new SceneListAdapter(this, (ArrayList<SceneInfo>) LogSceneManager
                .getInstance().getAllSceneInfoList(this));
        mListView.setAdapter(mSceneListAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mModemMonitor = (SettingPreferenceView) findViewById(R.id.ylog_modem_monitor);
        mSystemDump = (SettingPreferenceView) findViewById(R.id.system_dump);

        findViewById(R.id.debug_tool).setOnClickListener(this);
        findViewById(R.id.log_setting).setOnClickListener(this);
        findViewById(R.id.reset_to_default).setOnClickListener(this);
        mModemMonitor.setOnClickListener(this);
        mSystemDump.setOnClickListener(this);


        mModemMonitor.setEnabled(false);
        boolean sysdumpEnable = PropUtils.getString(SYSDUMP_STATUS_PROP, "off").equals("on");
        Log.i(TAG, "sysdumpEnable "+sysdumpEnable+",get prop value:"+PropUtils.getString(SYSDUMP_STATUS_PROP, "error"));
        mSystemDump.setChecked(PropUtils.getString(SYSDUMP_STATUS_PROP, "off").equals("on"));
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        dismissProgressDialog(mProgressDialog);
        mProgressDialog = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                String mCustomName = data.getStringExtra(LogSceneManager.EXTRA_CUSTOM_NAME);
                boolean isEdit = data.getBooleanExtra(LogSceneManager.EXTRA_CUSTOM_EDIT, false);
                if (isEdit) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.userdefine_scene)+ mCustomName + getResources().getString(R.string.userdefine_edit_ok), Toast.LENGTH_LONG)
                            .show();
                } else {
                    mSceneListAdapter.queryDataSetChanged();
                    LogSceneManager.getInstance().updateCurrentSelected(SceneSettingActivity.this,
                            mCustomName);
                    mSceneListAdapter.queryDataSetChanged();
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.userdefine_scene) + mCustomName + getResources().getString(R.string.userdefin_open_ok), Toast.LENGTH_LONG)
                            .show();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null) {
            return;
        }
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        listView.setLayoutParams(params);
    }

    private void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = ProgressDialog.show(SceneSettingActivity.this, getResources()
                        .getString(R.string.scene_switching),
                        getResources().getString(R.string.scene_switching_wait), true, false);
            }
        });
    }

    private void dismissProgressDialog(final ProgressDialog progressDialog) {
        try {
            if ((progressDialog != null) && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            Log.w(TAG, "exception when dismissing dialog");
        } catch (final Exception e) {
            Log.w(TAG, "exception when dismissing dialog." + e);
        }
    }

    private void dismissProgressDialog() {
        Log.d(TAG, "dismissProgressDialog");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog(mProgressDialog);
                mProgressDialog = null;
           }
        });
    }

    private void enableSysDump(boolean enable){
        Log.d(TAG, "open or close sysdump :" + enable);
        String open = "false";
        String reset = "1";

        if (enable){
            open = "true";
            reset = "0";
        }
        PropUtils.setProp(SYSDUMP_CONTROL_PROP, open);
        // hard reset
        if (CommonUtils.isUserBuild()) {
            PropUtils.setProp(SYSDUMP_RESET_PROP, reset);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, " enableSysDump result is  :" + PropUtils.getString(SYSDUMP_STATUS_PROP, "off"));
                mSystemDump.setChecked(PropUtils.getString(
                SYSDUMP_STATUS_PROP, "off").equals("on"));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.debug_tool:
                Intent intent = new Intent();
                intent.setClass(SceneSettingActivity.this, DebugSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.ylog_modem_monitor:
                Toast.makeText(this, "current not support", Toast.LENGTH_SHORT).show();
                break;
            case R.id.system_dump:
                if (PropUtils.getString(SYSDUMP_STATUS_PROP, "off").equals("on")) {
                    enableSysDump(false);
                } else {
                    enableSysDump(true);
                }
                break;
            case R.id.log_setting:
                intent = new Intent();
                intent.putExtra("name", "todo");
                intent.setClass(SceneSettingActivity.this, LogSettingItemListActivity.class);
                startActivity(intent);
                break;
            case R.id.reset_to_default:
                resetAllSettingsToDefault();
                break;
        }
    }

    private void resetAllSettingsToDefault() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_to_default_tips)
                .setMessage(this.getString(R.string.reset_to_default_warning))
                .setPositiveButton(this.getString(R.string.alertdialog_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgressDialog();
                                        resetAPLogSetting();
                                        resetModemLogSetting();
                                        resetConnectivityLogSetting();
                                        resetOtherLogSetting();
                                        resetSceneSetting();
                                        dismissProgressDialog();

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SceneSettingActivity.this,
                                                        R.string.reset_all_settings_success, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).start();
                            }
                        })
                .setNegativeButton(this.getString(R.string.alertdialog_cancel), null)
                .show();
    }

    protected void resetAPLogSetting() {
        Log.d(TAG, "reset ap log settings");
        mApLogControl.reset();
        mLogManagerPreference.reset();
    }

    protected void resetModemLogSetting() {
        Log.d(TAG, "reset modem log settings");
        mCpLogControl.reset();
        if (CommonUtils.isUserBuild()) {
            mCpLogControl.enableETBMode(false);
            mCpLogControl.setModemSaveDump(false);
        }else{
            mCpLogControl.setModemSaveDump(true);
            mCpLogControl.enableETBMode(true);
        }
    }

    protected void resetConnectivityLogSetting() {
        WcnControl.getInstance().reset();
    }

    protected void resetOtherLogSetting() {
        Log.d(TAG, "reset other log settings");
        resetSystemDumpSetting();
        Log.d(TAG, "cp log storage reset to phone");
        CPLogStorageSwitch.getInstance().reset();

    }

    protected void resetSystemDumpSetting() {
        Log.d(TAG, "reset systemdump settings");
        if (CommonUtils.isUserBuild()) {
            enableSysDump(false);
        } else {
            enableSysDump(true);
        }
    }

    protected void resetSceneSetting() {
        Log.d(TAG, "reset scene settings");
        LogSceneManager.getInstance().deleteCustomerScene(SceneSettingActivity.this);
        if (mApLogControl.isLogStarted()) {
            LogInfo.getInstance().openNormalScene();
        } else {
            LogInfo.getInstance().setSceneStatus(LogInfo.SceneStatus.normal);
            LogInfo.getInstance().openNormalScene();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogSceneManager.getInstance().updateCurrentSelected(SceneSettingActivity.this, 0);
                mSceneListAdapter.queryDataSetChanged();
            }
        });
    }

    private void openScene(String sceneName){
        if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_NORMAL)
                        .equals(sceneName)) {
                    LogInfo.getInstance().openNormalScene();
                } else if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_DATA)
                        .equals(sceneName)) {
                    LogInfo.getInstance().openDataScene();
                } else if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_VOICE)
                        .equals(sceneName)) {
                    LogInfo.getInstance().openVoiceScene();
                } else if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_WCN)
                        .equals(sceneName)) {
                    LogInfo.getInstance().openWcnScene();
                } else if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_USER)
                        .equals(sceneName)) {
                    LogInfo.getInstance().openUserScene();
                } else if (LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_CUSTOMER)
                        .equals(sceneName)) {
                    // ignore
                } else {
                    LogInfo.getInstance().readData(SceneSettingActivity.this, sceneName);
                    LogInfo.getInstance().selectCustomerOrder(sceneName);
                }

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "position:" + position);
        final int tempPosition = position;
//         if (!mCpLogControl.isModemAlive()) {
//              Toast.makeText(SceneSettingActivity.this, SceneSettingActivity.this
//                                    .getString(R.string.modem_not_alive), Toast.LENGTH_LONG).show();
//              return;
//         }
        if (LogSceneManager.getInstance().getSceneString(this, SceneInfo.SCENE_CUSTOMER)
                .equals(((SceneInfo) mSceneListAdapter.getItem(position)).getSceneName())) {
            LogSceneManager.getInstance().createSceneDialog(this, mSceneListAdapter);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                boolean needRestoreCpToPc = true;
                boolean hasResetore = false;
                String sceneName = ((SceneInfo) mSceneListAdapter.getItem(tempPosition)).getSceneName();
                if (!mApLogControl.isLogStarted()) {
                    mApLogControl.setLogStatus(true);
                    CPLogStorageSwitch.getInstance().restoreCPLogDest(false);
                    hasResetore = true;
                }
                String lastScene = LogSceneManager.getInstance().getCurrentSelectedSceneName(
                        SceneSettingActivity.this);
                Log.i(TAG, "last scene is :"+lastScene+",and new scene is "+sceneName);
                if (sceneName.equals(lastScene)) {
                    return;
                }
                showProgressDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogSceneManager.getInstance().updateCurrentSelected(
                                SceneSettingActivity.this, tempPosition);
                        mSceneListAdapter.queryDataSetChanged();
                    }
                });
                Log.d(TAG, "sceneName:" + sceneName);
                boolean lastSceneIsUserScene=LogSceneManager.getInstance()
                        .getSceneString(SceneSettingActivity.this, SceneInfo.SCENE_USER)
                        .equals(lastScene);
                boolean lastSceneOpenedEventMonitorLog=LogInfo.getInstance().isModemEventMonitorLogOpened(lastScene);
                boolean curSceneOpenedEventMonitorLog=LogInfo.getInstance().isModemEventMonitorLogOpened(sceneName);
                if ((lastSceneIsUserScene||lastSceneOpenedEventMonitorLog)&&(!curSceneOpenedEventMonitorLog)){
                    Log.i(TAG, "last scene is " + lastScene+",need restore modme to pc state");
                    needRestoreCpToPc = true;
                }else {
                    needRestoreCpToPc = false;
                }
                openScene(sceneName);
                if (needRestoreCpToPc && hasResetore == false) {
                    CPLogStorageSwitch.getInstance().restoreCPLogDest(true);
                }
                dismissProgressDialog();
            }
        }).start();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int tempPosition = position;
        if (position > SceneInfo.SCENE_DEFAULT_COUNT) {
            final CharSequence[] charSequences = {
                    getString(R.string.scene_edit), getString(R.string.scene_delete)
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(SceneSettingActivity.this);
            builder.setTitle(getString(R.string.scene_edit_title))
                    .setItems(charSequences, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:// edit
                                    Intent intent = new Intent();
                                    intent.setClass(SceneSettingActivity.this,
                                            UserDefinedActivity.class);
                                    intent.putExtra(LogSceneManager.EXTRA_CUSTOM_NAME,
                                            ((SceneInfo) mSceneListAdapter.getItem(tempPosition))
                                                    .getSceneName());
                                    SceneSettingActivity.this.startActivityForResult(intent, 0);
                                    break;
                                case 1:// delete
                                    if (((SceneInfo) mSceneListAdapter.getItem(tempPosition))
                                            .getSceneName().equals(
                                                    LogSceneManager.getInstance()
                                                            .getCurrentSelectedSceneName(
                                                                    SceneSettingActivity.this))) {
                                        LogSceneManager.getInstance().updateCurrentSelected(
                                                SceneSettingActivity.this, 0);
                                        /* UNISOC: bug1395123  @{ */
                                        LogInfo.getInstance().openNormalScene();
                                        /* @} */
                                    }
                                    if (LogSceneManager.getInstance().deleteSceneByName(
                                            SceneSettingActivity.this,
                                            ((SceneInfo) mSceneListAdapter.getItem(tempPosition))
                                                    .getSceneName())) {
                                        Toast.makeText(SceneSettingActivity.this,
                                                charSequences[which] + " success!",
                                                Toast.LENGTH_SHORT).show();
                                        mSceneListAdapter.queryDataSetChanged();
                                    } else {
                                        Toast.makeText(SceneSettingActivity.this,
                                                charSequences[which] + " Fail!", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    break;
                            }
                        }
                    }).show();
            return true;
        }
        return false;
    }
}
