package com.whitelaning.weird.modelFetch.music;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.framework.android.modelFetch.BaseModelFetch;
import com.whitelaning.weird.model.music.ModelArtistInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicSingerFragmentModelFetch extends BaseModelFetch {
    public List<ModelArtistInfo> list = new ArrayList<>();

    public MusicSingerFragmentModelFetch(Context mContext, Handler mHandler) {
        super(mContext, mHandler);
    }

    public void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (DataSupport.count(ModelMusicInfo.class) > 0) {
                    list.clear();
                    list.addAll(DataSupport.findAll(ModelArtistInfo.class));
                    Message msg = Message.obtain();
                    msg.what = 1;
                    mHandler.sendMessageDelayed(msg, 1200);
                } else {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    mHandler.sendMessageDelayed(msg, 1200);
                }
            }
        }).start();
    }
}
