package com.framework.android.tool;

import android.content.Context;
import android.view.View;

import org.jetbrains.annotations.NotNull;

public class DynamicSizeUtils {
    public static int defaultDisplayWidth;//屏幕的宽度
    public static int defaultDisplayHeight;//屏幕的高度

    public static void setWidth(Context context, View view, int widthPx) {
        init(context);
        android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = widthPx;
    }

    public static void setHeight(Context context, View view, int heightPx) {
        init(context);
        android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = heightPx;
    }

    public static void setHeightAndWidth(Context context, View view, int heightPx, int widthPx) {
        init(context);
        android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = heightPx;
        lp.width = widthPx;
    }

    /**
     * 通过屏幕宽度和View比例适配高度
     *
     * @param context
     * @param view
     */
    public static void adaptiveViewByScreenWidth(@NotNull Context context, @NotNull View view, int viewHeight, int viewWidth) {
        init(context);
        android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = defaultDisplayWidth;
        lp.height = defaultDisplayWidth * viewHeight / viewWidth;
    }

    private static void init(Context context) {
        defaultDisplayWidth = ScreenUtils.getWidth();
        defaultDisplayHeight = ScreenUtils.getHeight();
    }
}
