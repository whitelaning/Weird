package com.framework.android.tool;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Zack White on 2016/1/7.
 */
public class VideoUtils {

    public static Bitmap getVideoThumbnail(String path, int width, int height) {
        return getVideoThumbnail(path, width, height, -1);
    }

    /**
     * 获取视频的缩略图
     * 提供了一个统一的接口用于从一个输入媒体文件中取得帧和元数据。
     *
     * @param path   视频的路径
     * @param width  缩略图的宽
     * @param height 缩略图的高
     * @return 缩略图
     */
    public static Bitmap getVideoThumbnail(String path, int width, int height, long time) {
        Bitmap bitmap = null;
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
            bitmap = mmr.getFrameAtTime(time, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
            if (bitmap != null) {
                if (bitmap.getWidth() > width) {// 如果图片宽度规格超过640px,则进行压缩
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                            width, height,
                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
        return bitmap;
    }
}
