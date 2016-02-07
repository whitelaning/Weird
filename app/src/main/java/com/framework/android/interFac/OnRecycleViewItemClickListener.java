package com.framework.android.interFac;

import android.view.View;

/**
 * Created by Zack White on 2016/1/6.
 */
public interface OnRecycleViewItemClickListener {
    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);
}
