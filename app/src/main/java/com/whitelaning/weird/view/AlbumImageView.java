/**
 * MyImageView.java
 * ImageChooser
 * <p>
 * Created by likebamboo on 2014-4-22
 * Copyright (c) 1998-2014 http://likebamboo.github.io/ All rights reserved.
 */

package com.whitelaning.weird.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 自定义View，onMeasure方法中取图片宽和高
 */
public class AlbumImageView extends ImageView {

    /**
     * 记录控件的宽和高
     */
    private Point mPoint = new Point();

    public AlbumImageView(Context context) {
        super(context);
    }

    public AlbumImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mPoint.x = getMeasuredWidth();
        mPoint.y = getMeasuredHeight();
    }

    /**
     * 返回Point
     *
     * @return
     */
    public Point getPoint() {
        return mPoint;
    }
}
