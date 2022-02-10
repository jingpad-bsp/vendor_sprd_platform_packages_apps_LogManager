/*
- * Copyright (C) 2013 Spreadtrum Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprd.logmanager.utils;

import java.io.File;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class StorageUtil {
    public static final String DEFAULT_INTERNAL_PATH = "/storage/emulated/0/";
    public static final String DEFAULT_EXTERNAL_PATH = "/storage/sdcard0/";
    public static final String SDCARD_PATH_PROP = "vold.sdcard0.path";
    public static final String SDCARD_MOUNTED_PROP = "vold.sdcard0.state";

    static final String TAG = "StorageUitl";

    public static String getExternalStorage() {
        String path = PropUtils.getProp(SDCARD_PATH_PROP);
        if (path != null) {
            String stateString = PropUtils.getProp(SDCARD_MOUNTED_PROP);
            if (stateString != null && stateString.trim().equals("mounted")) {
                return path;
            }else if(stateString != null && stateString.trim().equals("unmounted")){
           // return false;
            }
            //return path;
        }
        path = MiscUtils.getSdPath();
        if (path == null) {
            return DEFAULT_EXTERNAL_PATH;
        }
        return path.trim();
    }

    public static boolean getExternalStorageState() {
        boolean res = true;
        String stateString = PropUtils.getProp(SDCARD_MOUNTED_PROP);
        if (stateString != null && stateString.trim().equals("mounted")) {
            return true;
        }else if(stateString != null && stateString.trim().equals("unmounted")){
           // return false;
        }
        String state = MiscUtils.getSdState();
        if (state != null && state.contains("0")) {
            res = false;
        }
        if (state != null && state.contains("1")) {
            res = true;
        }
        return res;
    }

    public static long getTotalSpace(File storageLocation) {
        if (storageLocation == null) {
            Log.e(TAG, "storageLocation is null, return 0");
            return 0;
        }

        return storageLocation.getTotalSpace();

    }

    public static long getStorageTotalSize(boolean isExternal) {
        try {
            File file = new File(isExternal ? DEFAULT_EXTERNAL_PATH
                    : DEFAULT_INTERNAL_PATH);
            if (file == null || !file.exists())
                file = Environment.getDataDirectory();
            return file.getTotalSpace();
        } catch (Exception e) {
            Log.d(TAG, "getStorageTotalSize exception", e);
            return 0;
        }
    }

    public static long getStorageFreeSize(boolean isExternal) {
        try {
            File file = new File(isExternal ? DEFAULT_EXTERNAL_PATH
                    : DEFAULT_INTERNAL_PATH);
            if (file == null || !file.exists())
                file = Environment.getDataDirectory();
            return file.getFreeSpace();
        } catch (Exception e) {
            Log.d(TAG, "getStorageTotalSize exception", e);
            return 0;
        }
    }

    public static long getFreeSpace(String path) {
        Log.i(TAG, "getFreeSpace path is " + path);
        final File file = new File(path);
        if (file.exists()) {
            return file.getFreeSpace();
        }
        Log.w(TAG, "file is not exist :" + path);
        return 0;
    }

    public static long getFreeSpace(File file) {
        if (file !=null && file.exists()) {
            return file.getFreeSpace();
        }
        return 0;
    }


    public static long getTotalSpace(String path) {
        Log.i(TAG, "getTotalSpace path is " + path);
        final File file = new File(path);
        if (file.exists()) {
            return file.getTotalSpace();
        }
        Log.i(TAG, "path not exist: " + path);
        return 0;
    }
}
