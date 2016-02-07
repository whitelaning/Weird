package com.whitelaning.weird.modelFetch.album;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.framework.android.modelFetch.BaseModelFetch;
import com.framework.android.tool.TaskUtil;
import com.whitelaning.weird.service.album.BaseTask;
import com.whitelaning.weird.service.album.ImageLoadTask;

import java.util.ArrayList;

/**
 * Created by Zack White on 2016/1/23.
 */
public class AlbumFragmentModelFetch extends BaseModelFetch {
    private ImageLoadTask mLoadTask;

    public AlbumFragmentModelFetch(Context mContext) {
        super(mContext);
    }

    public AlbumFragmentModelFetch(Context mContext, Handler mHandler) {
        super(mContext, mHandler);
    }

    public void toFindImage() {
        // 线程正在执行
        if (mLoadTask != null && mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        mLoadTask = new ImageLoadTask(mContext, new BaseTask.OnTaskResultListener() {
            @Override
            public void onResult(boolean success, String error, Object result) {
                if (success && result != null && result instanceof ArrayList) {
                    if (mHandler != null) {
                        //加载成功
                        Message msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }
                } else {
                    if (mHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
        TaskUtil.execute(mLoadTask);
    }
}
