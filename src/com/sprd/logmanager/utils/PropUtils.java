package com.sprd.logmanager.utils;

import com.sprd.logmanager.logcontrol.APLogControl;

import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

public class PropUtils {
    static final String LOG_TAG = "PropUtils";
    public static void setProp(String prop, String value) {
        boolean useHidl = true;
        if (prop.contains(".vendor") || prop.contains("vendor.")) {
            useHidl = false;
        }
        if (useHidl) {
            APLogControl.getInstance().setPropThroughHidl(prop, value);
        } else {
            // SystemProperties.set(prop, value);
            MiscUtils.setProp(prop, value);
        }

    }

    public static String getProp(String prop) {
        String string = null;
        boolean useHidl = true;
        if (prop.contains(".vendor") || prop.contains("vendor.")) {
            useHidl = false;
        }
        if (Build.VERSION.SDK_INT < 28) {
            useHidl = false;
        }
        if (!useHidl) {
            string = SystemProperties.get(prop);
            return string;
        }
        string = APLogControl.getInstance().getPropThroughHidl(prop);
        if (string != null && string.trim().equals("")) {
            string = null;
        }
        if (string != null) {
            string = string.trim();
        }
        return string;
    }

    public static boolean getBoolean(String prop, boolean defualt) {
        int len = -1;
        boolean result = defualt;
        String value = getProp(prop);
        if (value != null) {
            value = value.trim();
            len =value.length();
            if (value.length() == 1) {
                if (value.equals("0") || value.equals("n"))
                    result = false;
                else if (value.equals("1") || value.equals("y"))
                    result = true;
            } else if (len > 1) {
                if (value.equals("no") || value.equals("false")
                        || value.equals("off")) {
                    result = false;
                } else if (value.equals("yes") || value.equals("true")
                        || value.equals("on")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public static String getString(String prop, String defualt) {
        String result = defualt;
        String value = getProp(prop);
        if (value != null) {
            value = value.trim();
            if (value.length() >= 1) {
                result = value;
            }
        }
        return result;
    }

    public static int getInt(String prop, int defualt) {
        int result = defualt;
        String value = getProp(prop);
        int err=-1;
        if (value != null) {
            value = value.trim();
            if (value.length() > 0) {
                try {
                    result = Integer.parseInt(value);
                    err=0;
                } catch (Exception e) {
                    e.printStackTrace();
                    result = defualt;
                }
            }
        }
        Log.e(LOG_TAG, "getInt("+prop+") = "+result+" "+(value!=null?value:"null")+" "+err);
        return result;
    }
}
