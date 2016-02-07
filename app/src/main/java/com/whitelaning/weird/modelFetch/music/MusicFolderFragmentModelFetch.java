package com.whitelaning.weird.modelFetch.music;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.framework.android.modelFetch.BaseModelFetch;
import com.whitelaning.weird.model.music.ModelAlbumInfo;
import com.whitelaning.weird.model.music.ModelFolderInfo;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicFolderFragmentModelFetch extends BaseModelFetch {

    public ArrayList<ModelFolderInfo> list = new ArrayList<>();

    public MusicFolderFragmentModelFetch(Context mContext, Handler mHandler) {
        super(mContext, mHandler);
    }

    public void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (DataSupport.count(ModelAlbumInfo.class) > 0) {
                    list.clear();
                    list.addAll(DataSupport.findAll(ModelFolderInfo.class));
                    Message msg = Message.obtain();
                    msg.what = 1;
                    mHandler.sendMessageDelayed(msg, 2000);
                } else {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    mHandler.sendMessageDelayed(msg, 2000);
                }
            }
        }).start();
    }

}
