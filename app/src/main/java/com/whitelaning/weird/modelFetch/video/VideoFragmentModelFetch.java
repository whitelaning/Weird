package com.whitelaning.weird.modelFetch.video;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.framework.android.modelFetch.BaseModelFetch;
import com.framework.android.tool.MediaScanner;
import com.whitelaning.weird.model.video.ModelVideoFolderInfor;
import com.whitelaning.weird.model.video.ModelVideoInfor;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Zack White on 2016/1/10.
 */
public class VideoFragmentModelFetch extends BaseModelFetch {
    public ArrayList<ModelVideoInfor> allVideoList = new ArrayList<>();
    public ArrayList<ModelVideoFolderInfor> allVideoFolderList = new ArrayList<>();

    private boolean isFirst = true;//用于第一次进入没有视频缓存的时候扫描后台数据

    public VideoFragmentModelFetch(Context mContext, Handler mHandler) {
        super(mContext, mHandler);
    }

    public void toFindVideoByLitePal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromLitePal();
                setVideoFolderListData();
                sendDealWithMsg();
            }
        }).start();
    }

    private void getDataFromLitePal() {
        allVideoList.clear();
        allVideoFolderList.clear();
        allVideoList.addAll(DataSupport.findAll(ModelVideoInfor.class));
    }

    private void setVideoFolderListData() {
        for (ModelVideoInfor modelVideoInfor : allVideoList) {
            ModelVideoFolderInfor item = new ModelVideoFolderInfor(mContext);
            item.setFolderName(modelVideoInfor.getParentName());
            item.addVideo(modelVideoInfor);

            int searchIdx = allVideoFolderList.indexOf(item);
            if (searchIdx >= 0) {
                ModelVideoFolderInfor videoGroup = allVideoFolderList.get(searchIdx);
                videoGroup.addVideo(modelVideoInfor);
            } else {
                allVideoFolderList.add(item);
            }
        }
    }

    private void sendDealWithMsg() {
        if (allVideoList != null && allVideoList.size() > 0) {
            if (mHandler != null) {
                Message msg = Message.obtain();
                msg.what = 0;
                mHandler.sendMessage(msg);
            }
        } else {
            if (isFirst) {
                isFirst = false;
                Message msg = Message.obtain();
                msg.what = 4;
                mHandler.sendMessage(msg);
            } else {
                Message msg = Message.obtain();
                msg.what = 5;
                mHandler.sendMessage(msg);
            }
        }
    }

    public void clearErrorPath() {
        for (int i = 0; i < allVideoList.size(); i++) {
            ModelVideoInfor item = allVideoList.get(i);
            File file = new File(item.getPath());
            if (file.exists()) {
                if (!file.isFile()) {
                    deleteErrorPath(i, item);
                }
            } else {
                deleteErrorPath(i, item);
            }
        }
    }

    private void deleteErrorPath(int i, ModelVideoInfor item) {
        allVideoList.remove(i);
        setVideoFolderListData();
        DataSupport.deleteAll(ModelVideoInfor.class, "path = ?", item.getPath());
    }

    public void startMediaScanner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaScanner.getInstance().scanVideoInDirectory(
                        Environment.getExternalStorageDirectory().getPath());
            }
        }).start();
    }

    public ModelVideoInfor remove(int arg1) {
        ModelVideoInfor item = allVideoList.remove(arg1);
        setVideoFolderListData();
        return item;
    }

    public void addVideo(int deletePosition, ModelVideoInfor deleteVideoInforModel) {
        allVideoList.add(deletePosition, deleteVideoInforModel);
        setVideoFolderListData();
    }
}
