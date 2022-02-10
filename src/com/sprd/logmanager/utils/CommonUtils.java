package com.sprd.logmanager.utils;

import java.text.DecimalFormat;

import android.os.Build;
import android.util.Log;

public class CommonUtils {
    static final String TAG = "CommonUtils";
    static final int FILE_SIZE_DIVIDER = 1024;

    public static boolean isGSIVersion() {
        String deviceString = Build.DEVICE;
        boolean gsiVersion = Build.DEVICE.contains("generic");
        Log.d(TAG, "ro.product.device= " + deviceString);
        return gsiVersion;
    }

    public static boolean isUserBuild() {
        boolean isUser = Build.TYPE.equalsIgnoreCase("user");
        return isUser;
    }

    public static String formatFileSize(long size) {
        DecimalFormat format = new DecimalFormat("0.0");
        if (size < FILE_SIZE_DIVIDER) {
            return String.valueOf(size) + "B";
        }
        double d = (double) size;
        if ((d = d / FILE_SIZE_DIVIDER) < FILE_SIZE_DIVIDER) {
            return format.format(d) + "KB";
        }
        if ((d = d / FILE_SIZE_DIVIDER) < FILE_SIZE_DIVIDER) {
            return String.valueOf(size) + "MB";
        }
        if ((d = d / FILE_SIZE_DIVIDER) < FILE_SIZE_DIVIDER) {
            return format.format(d) + "GB";
        }
        if ((d = d / FILE_SIZE_DIVIDER) < FILE_SIZE_DIVIDER) {
            return format.format(d) + "TB";
        }
        return format.format(d / FILE_SIZE_DIVIDER) + "PB";
    }
}
