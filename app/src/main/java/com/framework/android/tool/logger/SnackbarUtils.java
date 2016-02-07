package com.framework.android.tool.logger;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Zack White on 2015/12/29.
 */
public class SnackbarUtils {
    public static void show(View view, String context) {
        Snackbar.make(view, context, Snackbar.LENGTH_LONG).show();
//                .setAction("Action", null)
    }
}
