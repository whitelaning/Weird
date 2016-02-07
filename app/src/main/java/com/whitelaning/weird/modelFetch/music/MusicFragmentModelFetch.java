package com.whitelaning.weird.modelFetch.music;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.framework.android.modelFetch.BaseModelFetch;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.tool.music.MusicUtils;

/**
 * Created by Zack White on 2016/1/30.
 */
public class MusicFragmentModelFetch extends BaseModelFetch {

    public MusicFragmentModelFetch(Context mContext, Handler mHandler) {
        super(mContext, mHandler);
    }

    public void checkData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);
                MusicUtils.queryArtist(mContext);
                MusicUtils.queryAlbums(mContext);
                MusicUtils.queryFolder(mContext);
                Message msg = Message.obtain();
                msg.what = 1;
                mHandler.sendMessageDelayed(msg, 1600);
            }
        }).start();
    }
}
