package com.framework.android.tool;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

/**
 * Created by Whitelaning on 2015/7/16.
 * Email: Whitelaning@gmail.com
 */
public class SpannableStringUtils {
    /**
     * 默认红色
     * @param str
     * @param start
     * @param end
     * @return
     */
    public static SpannableStringBuilder getColorString(String str, int start, int end) {
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        style.setSpan(new ForegroundColorSpan(0xfffe5417), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }

    public static SpannableStringBuilder getColorString(String str, int start, int end, int color) {
        SpannableStringBuilder style = new SpannableStringBuilder(str);
        style.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return style;
    }
}
