package com.whitelaning.weird.model.video;

import android.content.Context;

import com.framework.android.model.BaseModel;

import java.util.ArrayList;

/**
 * Created by Zack White on 1/27/2016.
 */
public class ModelVideoFolderInfor extends BaseModel {
    private String folderName;
    private ArrayList<ModelVideoInfor> videoList = new ArrayList<>();

    public ModelVideoFolderInfor(Context mContext) {
        super(mContext);
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public ArrayList<ModelVideoInfor> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<ModelVideoInfor> videoList) {
        this.videoList = videoList;
    }

    public void addVideo(ModelVideoInfor modelVideoInfor) {
        videoList.add(modelVideoInfor);
    }

    /**
     * 重写该方法
     * 使只要视频所在的文件夹名称(dirName)相同就属于同一个视频组
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof ModelVideoFolderInfor && folderName.equals(((ModelVideoFolderInfor) o).folderName);
    }
}
