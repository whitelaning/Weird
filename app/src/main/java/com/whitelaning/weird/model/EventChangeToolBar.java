package com.whitelaning.weird.model;

import com.framework.android.model.BaseEvent;

/**
 * Created by Zack White on 2016/2/1.
 */
public class EventChangeToolBar extends BaseEvent {

    private String title;
    private int color;
    private int type;

    public EventChangeToolBar(int TAG) {
        super(TAG);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
