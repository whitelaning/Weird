package com.framework.android.tool.logger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.framework.android.tool.CertificateUtls;
import com.framework.android.tool.DiskLruCacheUtil;
import com.framework.android.tool.StringUtils;
import com.framework.android.tool.VideoUtils;
import com.other.io.DiskLruCache;
import com.whitelaning.weird.model.video.ModelVideoInfor;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Zack White on 2016/1/6.
 */
public class FileUtils {

    /**
     * 获取指定文件类型列表的全路径，如果对应文件存在则返回文件的路径，如果对应文件不存在，则路径为空
     * eg:
     * List<String> filePathList = FileUtils.getExistFiles(this,new String[]{"ptr","hst","dct"});
     */
    public static List<String> getExistFiles(final Context context, final String[] fileNames) {
        if (null == context || null == fileNames || fileNames.length == 0) {
            return null;
        }

        String filePath;
        String suffix;
        String linkType;

        List<String> filePathList = new ArrayList<>();

        for (String fileName : fileNames) {

            suffix = fileName;
            linkType = " LIKE '%" + suffix + "'";

            ContentResolver contentResolver = context.getContentResolver();

            if (null != contentResolver) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                Cursor cursor = contentResolver.query(
                        uri,
                        new String[]{
                                MediaStore.Files.FileColumns.DATA
                        },
                        MediaStore.Files.FileColumns.DATA + linkType, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                            filePathList.add(filePath);
                        } while (cursor.moveToNext());
                    }

                    if (!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        }

        return filePathList;
    }

    /**
     * 扫描一个新添加的文件到媒体库中
     *
     * @param context
     * @param filename
     */
    private static void updateGallery(Context context, String filename) {//filename是我们的文件全名，包括后缀哦
        MediaScannerConnection.scanFile(context,
                new String[]{filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }


    public static List<ModelVideoInfor> getVideoFile(Context context) {

        String[] VIDEO_COLUMN = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.WIDTH,
                MediaStore.Audio.Media.HEIGHT
        };

        List<ModelVideoInfor> filePathList = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                VIDEO_COLUMN, null, null, null);

        if (cursor != null) {
            DataSupport.deleteAll(ModelVideoInfor.class);
            if (cursor.moveToFirst()) {
                do {
                    String path = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[0]));
                    String size = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[1]));
                    String duration = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[2]));
                    String title = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[3]));
                    String displayName = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[4]));
                    String width = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[5]));
                    String height = cursor.getString(cursor.getColumnIndex(VIDEO_COLUMN[6]));

                    if (Long.parseLong(duration) < 10000) {
                        continue;
                    }

                    ModelVideoInfor mVideoModel = new ModelVideoInfor();

                    mVideoModel.setPath(path);
                    mVideoModel.setSize(size);
                    mVideoModel.setDuration(duration);
                    mVideoModel.setWidth(width);
                    mVideoModel.setHeight(height);
                    mVideoModel.setAddTime(System.currentTimeMillis());

                    if (StringUtils.isBlank(title)) {
                        mVideoModel.setTitle(path.substring(path.lastIndexOf("/"), path.lastIndexOf(".")));
                    } else {
                        mVideoModel.setTitle(title);
                    }

                    if (StringUtils.isBlank(displayName)) {
                        mVideoModel.setDisplayName(path.substring(path.lastIndexOf("/")));
                    } else {
                        mVideoModel.setDisplayName(displayName);
                    }

                    String key = null;//缓存的key值

                    try {
                        Bitmap bitmap = VideoUtils.getVideoThumbnail(path, 320, 240, Long.parseLong(mVideoModel.getDuration()) * 1000 / 2);
                        key = CertificateUtls.hashKeyForDisk(mVideoModel.getPath());
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mVideoModel.setImageDiskLruCacheKey(key);

                    filePathList.add(mVideoModel);
                } while (cursor.moveToNext());
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return filePathList;
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    // http://www.fileinfo.com/filetypes/video , "dat" , "bin" , "rms"
    public static final String[] VIDEO_EXTENSIONS = { "264", "3g2", "3gp", "3gp2", "3gpp", "3gpp2", "3mm", "3p2", "60d", "aep", "ajp", "amv", "amx", "arf", "asf", "asx", "avb", "avd", "avi", "avs", "avs", "axm", "bdm", "bdmv", "bik", "bix", "bmk", "box", "bs4", "bsf", "byu", "camre", "clpi", "cpi", "cvc", "d2v", "d3v", "dav", "dce", "dck", "ddat", "dif", "dir", "divx", "dlx", "dmb", "dmsm", "dmss", "dnc", "dpg", "dream", "dsy", "dv", "dv-avi", "dv4", "dvdmedia", "dvr-ms", "dvx", "dxr", "dzm", "dzp", "dzt", "evo", "eye", "f4p", "f4v", "fbr", "fbr", "fbz", "fcp", "flc", "flh", "fli", "flv", "flx", "gl", "grasp", "gts", "gvi", "gvp", "hdmov", "hkm", "ifo", "imovi", "imovi", "iva", "ivf", "ivr", "ivs", "izz", "izzy", "jts", "lsf", "lsx", "m15", "m1pg", "m1v", "m21", "m21", "m2a", "m2p", "m2t", "m2ts", "m2v", "m4e", "m4u", "m4v", "m75", "meta", "mgv", "mj2", "mjp", "mjpg", "mkv", "mmv", "mnv", "mod", "modd", "moff", "moi", "moov", "mov", "movie", "mp21", "mp21", "mp2v", "mp4", "mp4v", "mpe", "mpeg", "mpeg4", "mpf", "mpg", "mpg2", "mpgin", "mpl", "mpls", "mpv", "mpv2", "mqv", "msdvd", "msh", "mswmm", "mts", "mtv", "mvb", "mvc", "mvd", "mve", "mvp", "mxf", "mys", "ncor", "nsv", "nvc", "ogm", "ogv", "ogx", "osp", "par", "pds", "pgi", "piv", "playlist", "pmf", "prel", "pro", "prproj", "psh", "pva", "pvr", "pxv", "qt", "qtch", "qtl", "qtm", "qtz", "rcproject", "rdb", "rec", "rm", "rmd", "rmp", "rmvb", "roq", "rp", "rts", "rts", "rum", "rv", "sbk", "sbt", "scm", "scm", "scn", "sec", "seq", "sfvidcap", "smil", "smk", "sml", "smv", "spl", "ssm", "str", "stx", "svi", "swf", "swi", "swt", "tda3mt", "tivo", "tix", "tod", "tp", "tp0", "tpd", "tpr", "trp", "ts", "tvs", "vc1", "vcr", "vcv", "vdo", "vdr", "veg", "vem", "vf", "vfw", "vfz", "vgz", "vid", "viewlet", "viv", "vivo", "vlab", "vob", "vp3", "vp6", "vp7", "vpj", "vro", "vsp", "w32", "wcp", "webm", "wm", "wmd", "wmmp", "wmv", "wmx", "wp3", "wpl", "wtv", "wvx", "xfl", "xvid", "yuv", "zm1", "zm2", "zm3", "zmv" };
    // http://www.fileinfo.com/filetypes/audio , "spx" , "mid" , "sf"
    public static final String[] AUDIO_EXTENSIONS = { "4mp", "669", "6cm", "8cm", "8med", "8svx", "a2m", "aa", "aa3", "aac", "aax", "abc", "abm", "ac3", "acd", "acd-bak", "acd-zip", "acm", "act", "adg", "afc", "agm", "ahx", "aif", "aifc", "aiff", "ais", "akp", "al", "alaw", "all", "amf", "amr", "ams", "ams", "aob", "ape", "apf", "apl", "ase", "at3", "atrac", "au", "aud", "aup", "avr", "awb", "band", "bap", "bdd", "box", "bun", "bwf", "c01", "caf", "cda", "cdda", "cdr", "cel", "cfa", "cidb", "cmf", "copy", "cpr", "cpt", "csh", "cwp", "d00", "d01", "dcf", "dcm", "dct", "ddt", "dewf", "df2", "dfc", "dig", "dig", "dls", "dm", "dmf", "dmsa", "dmse", "drg", "dsf", "dsm", "dsp", "dss", "dtm", "dts", "dtshd", "dvf", "dwd", "ear", "efa", "efe", "efk", "efq", "efs", "efv", "emd", "emp", "emx", "esps", "f2r", "f32", "f3r", "f4a", "f64", "far", "fff", "flac", "flp", "fls", "frg", "fsm", "fzb", "fzf", "fzv", "g721", "g723", "g726", "gig", "gp5", "gpk", "gsm", "gsm", "h0", "hdp", "hma", "hsb", "ics", "iff", "imf", "imp", "ins", "ins", "it", "iti", "its", "jam", "k25", "k26", "kar", "kin", "kit", "kmp", "koz", "koz", "kpl", "krz", "ksc", "ksf", "kt2", "kt3", "ktp", "l", "la", "lqt", "lso", "lvp", "lwv", "m1a", "m3u", "m4a", "m4b", "m4p", "m4r", "ma1", "mdl", "med", "mgv", "midi", "miniusf", "mka", "mlp", "mmf", "mmm", "mmp", "mo3", "mod", "mp1", "mp2", "mp3", "mpa", "mpc", "mpga", "mpu", "mp_", "mscx", "mscz", "msv", "mt2", "mt9", "mte", "mti", "mtm", "mtp", "mts", "mus", "mws", "mxl", "mzp", "nap", "nki", "nra", "nrt", "nsa", "nsf", "nst", "ntn", "nvf", "nwc", "odm", "oga", "ogg", "okt", "oma", "omf", "omg", "omx", "ots", "ove", "ovw", "pac", "pat", "pbf", "pca", "pcast", "pcg", "pcm", "peak", "phy", "pk", "pla", "pls", "pna", "ppc", "ppcx", "prg", "prg", "psf", "psm", "ptf", "ptm", "pts", "pvc", "qcp", "r", "r1m", "ra", "ram", "raw", "rax", "rbs", "rcy", "rex", "rfl", "rmf", "rmi", "rmj", "rmm", "rmx", "rng", "rns", "rol", "rsn", "rso", "rti", "rtm", "rts", "rvx", "rx2", "s3i", "s3m", "s3z", "saf", "sam", "sb", "sbg", "sbi", "sbk", "sc2", "sd", "sd", "sd2", "sd2f", "sdat", "sdii", "sds", "sdt", "sdx", "seg", "seq", "ses", "sf2", "sfk", "sfl", "shn", "sib", "sid", "sid", "smf", "smp", "snd", "snd", "snd", "sng", "sng", "sou", "sppack", "sprg", "sseq", "sseq", "ssnd", "stm", "stx", "sty", "svx", "sw", "swa", "syh", "syw", "syx", "td0", "tfmx", "thx", "toc", "tsp", "txw", "u", "ub", "ulaw", "ult", "ulw", "uni", "usf", "usflib", "uw", "uwf", "vag", "val", "vc3", "vmd", "vmf", "vmf", "voc", "voi", "vox", "vpm", "vqf", "vrf", "vyf", "w01", "wav", "wav", "wave", "wax", "wfb", "wfd", "wfp", "wma", "wow", "wpk", "wproj", "wrk", "wus", "wut", "wv", "wvc", "wve", "wwu", "xa", "xa", "xfs", "xi", "xm", "xmf", "xmi", "xmz", "xp", "xrns", "xsb", "xspf", "xt", "xwb", "ym", "zvd", "zvr" };
    public static final String[] IMAGE_EXTENSIONS = { "png", "jpg"};

    private static final HashSet<String> mHashVideo;
    private static final HashSet<String> mHashAudio;
    private static final HashSet<String> mHashImage;

    private static final double KB = 1024.0;
    private static final double MB = KB * KB;
    private static final double GB = KB * KB * KB;

    private static HashMap<String, String> mMimeType = new HashMap<String, String>();

    static {
        mHashVideo = new HashSet<String>(Arrays.asList(VIDEO_EXTENSIONS));
        mHashAudio = new HashSet<String>(Arrays.asList(AUDIO_EXTENSIONS));
        mHashImage = new HashSet<String>(Arrays.asList(IMAGE_EXTENSIONS));

        mMimeType.put("M1V", "video/mpeg");
        mMimeType.put("MP2", "video/mpeg");
        mMimeType.put("MPE", "video/mpeg");
        mMimeType.put("MPG", "video/mpeg");
        mMimeType.put("MPEG", "video/mpeg");
        mMimeType.put("MP4", "video/mp4");
        mMimeType.put("M4V", "video/mp4");
        mMimeType.put("3GP", "video/3gpp");
        mMimeType.put("3GPP", "video/3gpp");
        mMimeType.put("3G2", "video/3gpp2");
        mMimeType.put("3GPP2", "video/3gpp2");
        mMimeType.put("MKV", "video/x-matroska");
        mMimeType.put("WEBM", "video/x-matroska");
        mMimeType.put("MTS", "video/mp2ts");
        mMimeType.put("TS", "video/mp2ts");
        mMimeType.put("TP", "video/mp2ts");
        mMimeType.put("WMV", "video/x-ms-wmv");
        mMimeType.put("ASF", "video/x-ms-asf");
        mMimeType.put("ASX", "video/x-ms-asf");
        mMimeType.put("FLV", "video/x-flv");
        mMimeType.put("MOV", "video/quicktime");
        mMimeType.put("QT", "video/quicktime");
        mMimeType.put("RM", "video/x-pn-realvideo");
        mMimeType.put("RMVB", "video/x-pn-realvideo");
        mMimeType.put("VOB", "video/dvd");
        mMimeType.put("DAT", "video/dvd");
        mMimeType.put("AVI", "video/x-divx");
        mMimeType.put("OGV", "video/ogg");
        mMimeType.put("OGG", "video/ogg");
        mMimeType.put("VIV", "video/vnd.vivo");
        mMimeType.put("VIVO", "video/vnd.vivo");
        mMimeType.put("WTV", "video/wtv");
        mMimeType.put("AVS", "video/avs-video");
        mMimeType.put("SWF", "video/x-shockwave-flash");
        mMimeType.put("YUV", "video/x-raw-yuv");
    }

    /** 是否是音频或者视频 */
    public static boolean isVideoOrAudio(File f) {
        final String ext = getFileExtension(f);
        return mHashVideo.contains(ext) || mHashAudio.contains(ext);
    }

    public static boolean isVideoOrAudio(String f) {
        final String ext = getUrlExtension(f);
        return mHashVideo.contains(ext) || mHashAudio.contains(ext);
    }

    public static boolean isVideo(File f) {
        final String ext = getFileExtension(f);
        return mHashVideo.contains(ext);
    }

    public static boolean isImage(File f) {
        final String ext = getFileExtension(f);
        return mHashImage.contains(ext);
    }

    /** 获取文件后缀 */
    public static String getFileExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    public static String getUrlFileName(String url) {
        int slashIndex = url.lastIndexOf('/');
        int dotIndex = url.lastIndexOf('.');
        String filenameWithoutExtension;
        if (dotIndex == -1) {
            filenameWithoutExtension = url.substring(slashIndex + 1);
        } else {
            filenameWithoutExtension = url.substring(slashIndex + 1, dotIndex);
        }
        return filenameWithoutExtension;
    }

    public static String getUrlExtension(String url) {
        if (!StringUtils.isEmpty(url)) {
            int i = url.lastIndexOf('.');
            if (i > 0 && i < url.length() - 1) {
                return url.substring(i + 1).toLowerCase();
            }
        }
        return "";
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String showFileSize(long size) {
        String fileSize;
        if (size < KB)
            fileSize = size + "B";
        else if (size < MB)
            fileSize = String.format("%.1f", size / KB) + "KB";
        else if (size < GB)
            fileSize = String.format("%.1f", size / MB) + "MB";
        else
            fileSize = String.format("%.1f", size / GB) + "GB";

        return fileSize;
    }

    /** 显示SD卡剩余空间 */
    @SuppressWarnings("deprecation")
    public static String showFileAvailable() {
        String result = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
            return showFileSize(availCount * blockSize) + " / " + showFileSize(blockSize * blockCount);
        }
        return result;
    }

    /** 如果不存在就创建 */
    public static boolean createIfNoExists(String path) {
        File file = new File(path);
        boolean mk = false;
        if (!file.exists()) {
            mk = file.mkdirs();
        }
        return mk;
    }

    /** 获取MIME */
    public static String getMimeType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0)
            return null;

        return mMimeType.get(path.substring(lastDot + 1).toUpperCase());
    }

    /** 多个SD卡时 取外置SD卡 */
    public static String getExternalStorageDirectory() {
        //参考文章
        //http://blog.csdn.net/bbmiku/article/details/7937745
        Map<String, String> map = System.getenv();
        String[] values = new String[map.values().size()];
        map.values().toArray(values);
        String path = values[values.length - 1];
        if (path.startsWith("/mnt/") && !Environment.getExternalStorageDirectory().getAbsolutePath().equals(path))
            return path;
        else
            return null;
    }

    /**
     * <p>
     * 对本地文件路径格式化
     * <p>
     * 其实就是加上 file://
     */
    public static String getFormatFilePath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if (path.startsWith("file://")) {
            return path;
        }
        return "file://" + path;
    }
}
