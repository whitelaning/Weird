package com.whitelaning.weird.model.album;

import com.framework.android.model.BaseEvent;

/**
 * Created by Zack White on 2016/1/23.
 */
public class EventDeleteImage extends BaseEvent {

    private String deletePathString;

    public EventDeleteImage(int TAG) {
        super(TAG);
    }

    public String getDeletePathString() {
        return deletePathString;
    }

    public void setDeletePathString(String deletePathString) {
        this.deletePathString = deletePathString;
    }
}
