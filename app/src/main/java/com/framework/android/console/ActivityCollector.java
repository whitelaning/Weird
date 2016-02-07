package com.framework.android.console;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    // 将开打的Activity添加进入activities
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    // 将开打的Activity从activities中移除
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    // 结束所有还没用被系统收回的Activity
    public static void finishAll() {
        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }

    // 结束所有还没用被系统收回的Activity,保留mActivity
    public static void finishAll(Activity mActivity) {
        for (Activity activity : activities) {
            if (!activity.equals(mActivity)) {
                activity.finish();
            }
        }
    }

    public static void finishList(ArrayList<Class> list) {
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).getName();
            for (Activity activity : activities) {
                if (activity.getClass().getName().equals(name)) {
                    activity.finish();
                }
            }
        }
    }
}
