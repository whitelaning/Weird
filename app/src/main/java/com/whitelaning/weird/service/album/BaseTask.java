/**
 * BaseTask.java
 * ImageChooser
 * <p>
 * Created by likebamboo on 2014-4-22
 * Copyright (c) 1998-2014 http://likebamboo.github.io/ All rights reserved.
 */

package com.whitelaning.weird.service.album;

import android.os.AsyncTask;

/**
 * 异步任务基类
 */
public abstract class BaseTask extends AsyncTask<Void, Void, Boolean> {

    /**
     * 失败的时候的错误提示
     */
    protected String error;

    /**
     * 是否被终止
     */
    protected boolean interrupt = false;

    /**
     * 结果
     */
    protected Object result;

    /**
     * 异步任务执行完后的回调接口
     */
    protected OnTaskResultListener resultListener;

    @Override
    protected void onPostExecute(Boolean success) {
        if (!interrupt && resultListener != null) {
            resultListener.onResult(success, error, result);
        }
    }

    /**
     * 中断异步任务
     */
    public void cancel() {
        super.cancel(true);
        interrupt = true;
    }

    public void setOnResultListener(OnTaskResultListener listener) {
        resultListener = listener;
    }

    /**
     * 异步任务执行完后回调接口
     */
    public interface OnTaskResultListener {
        /**
         * 回调函数
         *
         * @param success 是否成功
         * @param error 错误信息，[成功的时候错误信息为空]
         * @param result 获取到的结果
         */
        void onResult(final boolean success, final String error, final Object result);
    }
}
