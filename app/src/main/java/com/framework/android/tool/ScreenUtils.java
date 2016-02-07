package com.framework.android.tool;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.framework.android.application.FrameworkApplication;

public class ScreenUtils {

    public static final Point outSize = new Point();

    static {
        ((WindowManager) FrameworkApplication.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(outSize);
    }

    public static int getDisplayMetricsWidth() {
        return Math.min(outSize.x, outSize.y);
    }

    public static int getWidth() {
        return outSize.x;
    }

    public static int getHeight() {
        return outSize.y;
    }

    public static float getAspectRatio(int width, int height) {
        return outSize.y / outSize.x;
    }

    /**
     * 获取屏幕的宽高
     *
     * @return
     */
    public static Point getDeviceSize(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        Point size = new Point();
        size.x = dm.widthPixels;
        size.y = dm.heightPixels;
        return size;
    }
}
