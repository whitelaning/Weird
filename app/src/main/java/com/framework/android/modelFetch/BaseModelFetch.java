package com.framework.android.modelFetch;

import android.content.Context;
import android.os.Handler;

/**
 * Created by Zack White on 2016/1/10.
 */
public class BaseModelFetch {
    protected Context mContext;
    protected Handler mHandler;

    public BaseModelFetch(Context mContext) {
        this(mContext, null);
    }

    public BaseModelFetch(Context mContext, Handler mHandler) {
        this.mContext = mContext;
        this.mHandler = mHandler;
    }
}
