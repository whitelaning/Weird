package com.whitelaning.weird.model.album;

import android.content.Context;

import com.framework.android.model.BaseModel;

import java.util.ArrayList;

/**
 * Created by Zack White on 2016/1/10.
 */
public class ModelImageGroup extends BaseModel  {

    public ModelImageGroup(Context mContext) {
        super(mContext);
    }

    /**
     * 文件夹名
     */
    private String dirName = "";

    /**
     * 文件夹下所有图片
     */
    private ArrayList<String> images = new ArrayList<>();

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * 获取第一张图片的路径(作为封面)
     *
     * @return
     */
    public String getFirstImgPath() {
        if (images.size() > 0) {
            return images.get(0);
        }
        return "";
    }

    /**
     * 获取图片数量
     *
     * @return
     */
    public int getImageCount() {
        return images.size();
    }

    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * 添加一张图片
     *
     * @param image
     */
    public void addImage(String image) {
        if (images == null) {
            images = new ArrayList<String>();
        }
        images.add(image);
    }

    @Override
    public String toString() {
        return "ImageGroup [firstImgPath=" + getFirstImgPath() + ", dirName=" + dirName
                + ", imageCount=" + getImageCount() + "]";
    }

    /**
     * 重写该方法
     * 使只要图片所在的文件夹名称(dirName)相同就属于同一个图片组
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ModelImageGroup)) {
            return false;
        }
        return dirName.equals(((ModelImageGroup) o).dirName);
    }
}
