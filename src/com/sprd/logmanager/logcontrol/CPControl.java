package com.sprd.logmanager.logcontrol;

import android.os.Build;

import com.sprd.logmanager.utils.SocketUtils;

public class CPControl {
    public static String CP_CONTROL_SOCKET_NAME = "modem_log_service";
    public static String SET_CMD_LOG_SERVICE = "SET";
    public static String GET_CMD_LOG_SERVICE = "GET";
    public static String ON = "ON";
    public static String OFF = "OFF";
    public static String SUB_LOG_TYPE_ARM = "ARM";
    public static String SUB_LOG_TYPE_DSP = "DSP";
    public static String SUB_LOG_TYPE_ARM_PCM = "ARMPCM";
    public static String SUB_LOG_TYPE_ETB = "ETB";
    public static String SUB_LOG_TYPE_CAP = "CAP";
    public static String SUB_LOG_TYPE_EVENT = "EVENT";

    public static String ORCAAP_CMD = "SET ORCAAP";
    public static String ORCADP_CMD = "SET ORCADP";

    public static String LOG_LEVEL = "LOGLEVEL";
    public static String SUB_LOG_TYPE_DSPPCM = "DSPPCM";
    private SocketUtils mSocketUtils;
    private static CPControl sCPControl;
    static final String TAG = "CPControl";
    private CPControl(){
        mSocketUtils = new SocketUtils(CP_CONTROL_SOCKET_NAME);
    }
    public static CPControl getInstance() {
        if (sCPControl == null) {
            sCPControl = new CPControl();
        }
        return sCPControl;
    }

    public boolean enableArmLog(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_ARM + " " + state;
        sendCmd(cmd);
        return true;
    }
    public boolean enableEvent(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_EVENT + " " + state;
        sendCmd(cmd);
        return true;
    }
    public boolean enableOrcaap(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = ORCAAP_CMD + " " + state;
        sendCmd(cmd);
        return true;
    }
    public boolean enableOrcadp(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = ORCADP_CMD + " " + state;
        sendCmd(cmd);
        return true;
    }
    public boolean enableEtb(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_ETB + " " + state;
        sendCmd(cmd);
        return true;
    }

    public boolean enableCap(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_CAP + " " + state;
        sendCmd(cmd);
        return true;
    }

    public boolean setLogLevel(int level){

        String cmd = SET_CMD_LOG_SERVICE + " "+ LOG_LEVEL + " " + level;
        sendCmd(cmd);
        return true;
    }

    public boolean enableDsp(boolean enable, String output){
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_DSP + " " + OFF;
        if (enable){
            cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_DSP + " " + output;
        }
        sendCmd(cmd);
        return true;
    }

    public boolean enableArmPcm(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_ARM_PCM + " " + state;
        sendCmd(cmd);
        return true;
    }

    public boolean enableDspPcmLog(boolean enable){
        String state = OFF;
        if (enable){
            state = ON;
        }
        String cmd = SET_CMD_LOG_SERVICE + " "+ SUB_LOG_TYPE_DSPPCM + " " + state;
        sendCmd(cmd);
        return true;

    }

    private synchronized String sendCmd(String cmd) {
        // TODO Auto-generated method stub
        String response = mSocketUtils.sendCmdAndRecResult(cmd);
        if (response == null) {
            response = "";
        }
        return response;
    }

}
