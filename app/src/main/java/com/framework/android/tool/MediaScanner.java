package com.framework.android.tool;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.framework.android.application.FrameworkApplication;
import com.framework.android.tool.logger.FileUtils;
import com.whitelaning.weird.model.video.ModelVideoInfor;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Zack White on 2016/1/7.
 */
public class MediaScanner {

    public final static String FINISHED_SCANNING = "finished_scanning_whitelaning";
    private static MediaScanner instance;

    private MediaScanner() {
    }

    public static MediaScanner getInstance() {
        synchronized (MediaScanner.class) {
            if (instance == null) {
                instance = new MediaScanner();
            }
        }
        return instance;
    }


    /**
     * 扫描一个媒体文件加入系统媒体库
     *
     * @param filePath 要扫描的媒体文件
     */
    public void scanFile(Context context, ScanFile filePath) {
        List<ScanFile> filePaths = new ArrayList<>(1);
        filePaths.add(filePath);
        scanFiles(context, filePaths);
    }

    /**
     * 扫描多个媒体文件加入系统媒体库
     *
     * @param filePaths 要扫描的文件列表
     */
    public void scanFiles(Context context, List<ScanFile> filePaths) {
        SannerClient client = new SannerClient(context, filePaths);
        client.connectAndScan();
    }

    /**
     * 扫描整个路径的所有文件加入系统媒体库
     *
     * @param paths 要扫描的文件
     */
    public void scanFilesByPath(Context context, String paths) {
        File f = new File(paths);
        List<ScanFile> filePaths = new ArrayList<>(1);
        filePaths.add(new ScanFile(f.getAbsolutePath(), null, null));
        scanFiles(context, filePaths);
    }

    /**
     * 扫描整个路径的所有指定格式的文件加入系统媒体库
     *
     * @param paths 要扫描的文件夹
     * @param type  要扫描的文件的类型 type = "mp3,mp4,avi,mkv,rmvb,flv,wmv,mov,3gp"
     */
    public void scanFilesByPath(Context context, String paths, String type) {
        File f = new File(paths);
        List<ScanFile> filePaths = new ArrayList<>(1);
        filePaths.add(new ScanFile(f.getAbsolutePath(), null, type));
        scanFiles(context, filePaths);
    }

    /**
     * 扫描文件夹中的视频
     */
    public void scanVideoInDirectory(String path) {
        DataSupport.deleteAll(ModelVideoInfor.class);
        eachAllMedias(new File(path));
        sendLocalBroadcast();
    }

    /**
     * 递归查找视频
     */
    private void eachAllMedias(File f) {
        if (f != null && f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : f.listFiles()) {
                    if (file.isDirectory()) {
                        //忽略.开头的文件夹
                        if (!file.getName().startsWith(".")) {
                            eachAllMedias(file);
                        }
                    } else if (file.exists() && file.canRead()) {
                        if (FileUtils.isVideo(file)) {
                            saveVideoModel(new ModelVideoInfor(file));
                        } else if (FileUtils.isImage(file)) {
                            updateGallery(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private void updateGallery(String filename) {//filename是我们的文件全名，包括后缀哦
        MediaScannerConnection.scanFile(FrameworkApplication.getContext(),
                new String[]{filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("updateGallery", "updateGalleryPath = " + path);
                    }
                });
    }

    /**
     * 保存入库
     */
    private void saveVideoModel(ModelVideoInfor mVideoModel) {
        //入库
        try {
            if (Long.parseLong(mVideoModel.getDuration()) > 10000) {
                if (mVideoModel.updateAll("path = ?", mVideoModel.getPath()) <= 0) {
                    mVideoModel.save();
                }
            }
        } catch (Exception e) {
            //视频转化错误，排除
        }
    }

    /**
     * 媒体文件扫描对象构造器
     */
    public static class ScanFile {

        /**
         * 要扫描的媒体文件路劲或包含媒体文件的文件夹路径
         */
        public String filePaths;

        /**
         * 要扫描的媒体文件类型 eg: audio/mp3  media/*  application/ogg
         * image/jpeg  image/png  video/mpeg   video/3gpp
         * ......
         */
        public String mineType;
        public String type;

        public ScanFile(String filePaths, String mineType, String type) {
            this.filePaths = filePaths;
            this.mineType = mineType;
            this.type = type.toLowerCase();
        }
    }

    public class SannerClient implements
            MediaScannerConnection.MediaScannerConnectionClient {

        /**
         * 要扫描的文件或文件夹
         */
        private List<ScanFile> scanFiles = null;

        /**
         * 实际要扫描的单个文件集合
         */
        private List<ScanFile> filePaths = null;

        private MediaScannerConnection mediaScanConn;

        public SannerClient(Context context, List<ScanFile> scanFiles) {
            this.scanFiles = scanFiles;
            mediaScanConn = new MediaScannerConnection(context, this);
        }

        /**
         * 连接MediaScanner并开始扫描
         */
        public void connectAndScan() {
            if (scanFiles != null && !scanFiles.isEmpty()) {
                this.filePaths = new ArrayList<>();

                //遍历取得单个文件集合
                for (ScanFile sf : scanFiles) {
                    findFile(sf);
                }

                mediaScanConn.connect();
            }
        }

        private void findFile(ScanFile file) {
            File f = new File(file.filePaths);
            if (f.isFile()) {
                if (TextUtils.isEmpty(file.type)) {
                    filePaths.add(file);
                } else {
                    String path = f.getAbsolutePath();
                    int start = path.lastIndexOf(".");
                    int end = path.length();
                    if (start >= 0) {
                        String suffix = path.substring(start + 1, end);
                        if (file.type.contains(suffix.toLowerCase())) {
                            filePaths.add(file);
                        }
                    }
                }
            } else {
                File[] fs = f.listFiles();
                if (fs != null && fs.length > 0) {
                    for (File cf : fs) {
                        findFile(new ScanFile(cf.getAbsolutePath(), file.mineType, file.type));
                    }
                }
            }
        }

        private void scanNext() {
            if (filePaths != null && !filePaths.isEmpty()) {
                ScanFile sf = filePaths.remove(filePaths.size() - 1);
                mediaScanConn.scanFile(sf.filePaths, sf.mineType);
            } else {
                mediaScanConn.disconnect();
                sendLocalBroadcast();
            }
        }

        @Override
        public void onMediaScannerConnected() {
            scanNext();
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            scanNext();
        }
    }

    /**
     * 发送本地广播
     */
    private void sendLocalBroadcast() {
        Intent mIntent = new Intent(FINISHED_SCANNING);
        LocalBroadcastManager.getInstance(FrameworkApplication.getContext()).sendBroadcast(mIntent);
    }
}
