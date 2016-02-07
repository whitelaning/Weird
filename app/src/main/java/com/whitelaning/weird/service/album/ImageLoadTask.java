/**
 * ImageLoadTask.java
 * ImageSelector
 * <p>
 * Created by likebamboo on 2014-4-22
 * Copyright (c) 1998-2014 http://likebamboo.github.io/ All rights reserved.
 */

package com.whitelaning.weird.service.album;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import com.framework.android.application.FrameworkApplication;
import com.whitelaning.weird.model.album.ModelImageGroup;

import java.io.File;
import java.util.ArrayList;

/**
 * 使用contentProvider扫描图片异步任务
 *
 * @author likebamboo
 */
public class ImageLoadTask extends BaseTask {

    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 存放图片<文件夹,该文件夹下的图片列表>键值对
     */
    private ArrayList<ModelImageGroup> mGroupList = new ArrayList<>();

    public ImageLoadTask(Context context, OnTaskResultListener listener) {
        super();
        mContext = context;
        result = mGroupList;
        setOnResultListener(listener);
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = FrameworkApplication.getContext().getContentResolver();
        // 构建查询条件，且只查询jpeg和png的图片
        StringBuilder selection = new StringBuilder();
        selection.append(Media.MIME_TYPE).append("=?");
        selection.append(" or ");
        selection.append(Media.MIME_TYPE).append("=?");

        Cursor mCursor = null;

        try {
            // 初始化游标
            mCursor = mContentResolver.query(mImageUri, null, selection.toString(), new String[]{
                    "image/jpeg", "image/png"
            }, Media.DATE_TAKEN);
            // 遍历结果

            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor.getColumnIndex(Media.DATA));

                    // 获取该图片的所在文件夹的路径
                    File file = new File(path);

                    if (!file.exists()) {
                        continue;
                    }

                    String parentName;
                    if (file.getParentFile() != null) {
                        parentName = file.getParentFile().getName();
                    } else {
                        parentName = file.getName();
                    }
                    // 构建一个imageGroup对象
                    ModelImageGroup item = new ModelImageGroup(FrameworkApplication.getContext());
                    // 设置imageGroup的文件夹名称
                    item.setDirName(parentName);

                    // 寻找该imageGroup是否是其所在的文件夹中的第一张图片
                    int searchIdx = mGroupList.indexOf(item);
                    if (searchIdx >= 0) {
                        // 如果是，该组的图片数量+1
                        ModelImageGroup imageGroup = mGroupList.get(searchIdx);
                        imageGroup.addImage(path);
                    } else {
                        // 否则，将该对象加入到groupList中
                        item.addImage(path);
                        mGroupList.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭游标
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
        }
        return true;
    }
}
