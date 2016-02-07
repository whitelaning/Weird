package com.whitelaning.weird.model.video;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;

import com.framework.android.tool.CertificateUtls;
import com.framework.android.tool.DiskLruCacheUtil;
import com.framework.android.tool.StringUtils;
import com.framework.android.tool.VideoUtils;
import com.other.io.DiskLruCache;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.OutputStream;

/**
 * Created by Zack White on 2016/1/6.
 */
public class ModelVideoInfor extends DataSupport implements Parcelable {
    private String path;
    private String size;
    private String duration;
    private String title;
    private String displayName;
    private String imageDiskLruCacheKey;
    private String width;
    private String height;
    private String parentName;
    private long addTime;

    public ModelVideoInfor() {
    }

    public ModelVideoInfor(File file) {
        initData(file);
    }

    private void initData(File file) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        try {
            mmr.setDataSource(file.getAbsolutePath());
            path = file.getAbsolutePath();
            size = String.valueOf(file.length());
            duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            title = file.getName();
            displayName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);
            width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            addTime = System.currentTimeMillis();

            if (file.getParentFile() != null) {
                parentName = file.getParentFile().getName();
            } else {
                parentName = file.getName();
            }

            if (StringUtils.isBlank(displayName)) {
                displayName = title;
            }

            String key;//缓存的key值

            Bitmap bitmap = VideoUtils.getVideoThumbnail(path, 320, 240, Long.parseLong(duration) * 1000 / 2);
            key = CertificateUtls.hashKeyForDisk(path);
            DiskLruCache.Editor editor = DiskLruCacheUtil.getInstance().edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
            DiskLruCacheUtil.getInstance().flush();

            imageDiskLruCacheKey = key;

        } catch (Exception ex) {
            //转换错误，排除
        } finally {
            mmr.release();
        }
    }

    public String getImageDiskLruCacheKey() {
        return imageDiskLruCacheKey;
    }

    public void setImageDiskLruCacheKey(String imageDiskLruCacheKey) {
        this.imageDiskLruCacheKey = imageDiskLruCacheKey;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        try {
            File file = new File(path);
            if (file.getParentFile() != null) {
                parentName = file.getParentFile().getName();
            } else {
                parentName = file.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeString(this.size);
        dest.writeString(this.duration);
        dest.writeString(this.title);
        dest.writeString(this.displayName);
        dest.writeString(this.imageDiskLruCacheKey);
        dest.writeString(this.width);
        dest.writeString(this.height);
        dest.writeString(this.parentName);
        dest.writeLong(this.addTime);
    }

    protected ModelVideoInfor(Parcel in) {
        this.path = in.readString();
        this.size = in.readString();
        this.duration = in.readString();
        this.title = in.readString();
        this.displayName = in.readString();
        this.imageDiskLruCacheKey = in.readString();
        this.width = in.readString();
        this.height = in.readString();
        this.parentName = in.readString();
        this.addTime = in.readLong();
    }

    public static final Parcelable.Creator<ModelVideoInfor> CREATOR = new Parcelable.Creator<ModelVideoInfor>() {
        public ModelVideoInfor createFromParcel(Parcel source) {
            return new ModelVideoInfor(source);
        }

        public ModelVideoInfor[] newArray(int size) {
            return new ModelVideoInfor[size];
        }
    };
}
