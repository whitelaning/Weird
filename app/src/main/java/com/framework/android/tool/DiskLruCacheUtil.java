package com.framework.android.tool;

import com.framework.android.application.FrameworkApplication;

import java.io.File;
import java.io.IOException;

import com.other.io.DiskLruCache;

/**
 * Created by Zack White on 2016/1/7.
 */
public class DiskLruCacheUtil {
    private static DiskLruCache instance;

    public static DiskLruCache getInstance() {
        if (instance == null) {
            synchronized (DiskLruCacheUtil.class) {
                try {
                    File cacheDir = FolderUtils.getDiskCacheDir(FrameworkApplication.getContext(), "bitmap");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    instance = DiskLruCache.open(cacheDir, DeviceUtils.getAppVersion(FrameworkApplication.getContext()), 1, 10 * 1024 * 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }
}
