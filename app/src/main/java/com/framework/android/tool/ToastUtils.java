package com.framework.android.tool;

import android.content.Context;
import android.widget.Toast;

import com.framework.android.application.FrameworkApplication;


/**
 * ToastUtils
 */
public class ToastUtils {

    private ToastUtils() {
        throw new AssertionError();
    }

    public static void show(int resId) {
        show(FrameworkApplication.getContext(), FrameworkApplication.getContext().getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration) {
        show(FrameworkApplication.getContext(), FrameworkApplication.getContext().getResources().getText(resId), duration);
    }

    public static void show(CharSequence text) {
        show(FrameworkApplication.getContext(), text, Toast.LENGTH_SHORT);
    }

    public static void show(CharSequence text, int duration) {
        Toast.makeText(FrameworkApplication.getContext(), text, duration).show();
    }

    public static void show(int resId, Object... args) {
        show(FrameworkApplication.getContext(), String.format(FrameworkApplication.getContext().getResources().getString(resId), args), Toast.LENGTH_SHORT);
    }

    public static void show(String format, Object... args) {
        show(FrameworkApplication.getContext(), String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration, Object... args) {
        show(FrameworkApplication.getContext(), String.format(FrameworkApplication.getContext().getResources().getString(resId), args), duration);
    }

    public static void show(String format, int duration, Object... args) {
        show(FrameworkApplication.getContext(), String.format(format, args), duration);
    }

    public static void show(Context context, int resId) {
        show(context, context.getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId, int duration) {
        show(context, context.getResources().getText(resId), duration);
    }

    public static void show(Context context, CharSequence text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, CharSequence text, int duration) {
        Toast.makeText(context, text, duration).show();
    }

    public static void show(Context context, int resId, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String format, Object... args) {
        show(context, String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId, int duration, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), duration);
    }

    public static void show(Context context, String format, int duration, Object... args) {
        show(context, String.format(format, args), duration);
    }
}
