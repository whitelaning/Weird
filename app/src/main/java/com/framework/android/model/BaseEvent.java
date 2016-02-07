package com.framework.android.model;

/**
 * Created by Zack White on 2016/1/23.
 */
public class BaseEvent {
    protected int TAG;

    public BaseEvent(int TAG) {
        this.TAG = TAG;
    }

    public int getTAG() {
        return TAG;
    }
}
