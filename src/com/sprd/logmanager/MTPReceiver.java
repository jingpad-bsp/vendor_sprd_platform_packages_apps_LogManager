
package com.sprd.logmanager;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sprd.logmanager.utils.StorageUtil;

public class MTPReceiver extends BroadcastReceiver {

    private static final String TAG = "MTPReceiver";
    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "sprd.intent.action.MEDIA_SCANNER_SCAN_DIR";
    private static String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    private static String USB_FUNCTION_MTP = "mtp";
    private static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 0x01000000;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ACTION_USB_STATE.equals(action)) {
            Bundle extras = intent.getExtras();
            boolean mtpEnabled = extras.getBoolean(USB_FUNCTION_MTP);
            if(mtpEnabled && StorageUtil.getExternalStorageState()) {
                String path = StorageUtil.getExternalStorage() + File.separator + "ylog";
                if (new File(path).exists()) {
                    scanDirAsync(context, path);
                }
            }
            if(mtpEnabled) {
                String path = "/storage/emulated/0/ylog";
                if (new File(path).exists()) {
                    scanDirAsync(context, path);
                }
            }
        }
    }

    public void scanDirAsync(Context context, String dir) {
        Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
        scanIntent.putExtra("scan_dir_path", dir);
        scanIntent.addFlags(FLAG_RECEIVER_INCLUDE_BACKGROUND);
        context.sendBroadcast(scanIntent);
    }
}
