package com.framework.android.tool;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;

/**
 * This class is used for background service
 *
 * @author Whitelaning
 * @version 1.0, 2014-11-17 上午8:23:22
 */
public class ServiceUtils {
    /**
     * check service worded or not
     *
     * @param context
     * @param serverName
     * @return is worded return ture，other false
     */
    public static boolean isWorked(Context context, String serverName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) activityManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(serverName)) {
                return true;
            }
        }
        return false;
    }

    public static void stopService(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.stopService(intent);
    }
}
