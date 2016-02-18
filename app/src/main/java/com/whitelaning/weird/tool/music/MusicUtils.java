package com.whitelaning.weird.tool.music;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.music.ModelAlbumInfo;
import com.whitelaning.weird.model.music.ModelArtistInfo;
import com.whitelaning.weird.model.music.ModelFolderInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.tool.StringHelper;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 查询各主页信息，获取封面图片等
 */
public class MusicUtils {

    private static String[] proj_music = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED};

    private static String[] proj_album = new String[]{
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART};

    private static String[] proj_artist = new String[]{
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS};

    private static String[] proj_folder = new String[]{MediaStore.Files.FileColumns.DATA};

    public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟

    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final HashMap<Long, Bitmap> sArtCache = new HashMap<>();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    static {
        // for the cache,
        // 565 is faster to decode and display
        // and we don't want to dither here because the image will be scaled
        // down later
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;

        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;
    }


    /**
     * 获取包含音频文件的文件夹信息
     *
     * @param context
     * @return
     */
    public static List<ModelFolderInfo> queryFolder(Context context) {
        List<ModelFolderInfo> list = new ArrayList<>();

        if (DataSupport.count(ModelFolderInfo.class) > 0) {
            list.addAll(DataSupport.findAll(ModelFolderInfo.class));
            return list;
        } else {

            Uri uri = MediaStore.Files.getContentUri("external");
            ContentResolver cr = context.getContentResolver();

            StringBuilder mSelection = new StringBuilder(MediaStore.Files.FileColumns.MEDIA_TYPE
                    + " = " + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO + " and " + "("
                    + MediaStore.Files.FileColumns.DATA + " like'%.mp3' or " + MediaStore.Audio.Media.DATA
                    + " like'%.wma')");
            // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
            mSelection.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
            mSelection.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);
            mSelection.append(") group by ( " + MediaStore.Files.FileColumns.PARENT);

            list.addAll(getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null, null)));

            for (ModelFolderInfo item : list) {
                if (item.updateAll("folderPath = ?", item.getFolderPath()) <= 0) {
                    item.save();
                }
            }

            return list;
        }
    }

    /**
     * 获取歌手信息
     *
     * @param context
     * @return
     */
    public static List<ModelArtistInfo> queryArtist(Context context) {
        List<ModelArtistInfo> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        if (DataSupport.count(ModelArtistInfo.class) > 0) {
            list.addAll(DataSupport.findAll(ModelArtistInfo.class));
            return list;
        } else {
            list.addAll(getArtistList(cr.query(uri, proj_artist,
                    null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                            + " desc")));

            for (ModelArtistInfo item : list) {
                if (item.updateAll("artistName = ?", item.getArtistName()) <= 0) {
                    item.save();
                }
            }

            return list;
        }
    }

    /**
     * 获取专辑信息
     *
     * @param context
     * @return
     */
    public static List<ModelAlbumInfo> queryAlbums(Context context) {
        List<ModelAlbumInfo> list = new ArrayList<>();

        if (DataSupport.count(ModelAlbumInfo.class) > 0) {
            list.addAll(DataSupport.findAll(ModelAlbumInfo.class));
            return list;
        } else {
            Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            ContentResolver cr = context.getContentResolver();
            StringBuilder where = new StringBuilder(MediaStore.Audio.Albums._ID
                    + " in (select distinct " + MediaStore.Audio.Media.ALBUM_ID
                    + " from audio_meta where (1=1 ");

            where.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
            where.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);
            where.append("))");

            list.addAll(getAlbumList(cr.query(uri, proj_album,
                    where.toString(), null, MediaStore.Audio.Media.ALBUM_KEY)));

            for (ModelAlbumInfo item : list) {
                if (item.updateAll("albumName = ? and artist = ?", item.getAlbumName(), item.getArtist()) <= 0) {
                    item.save();
                }
            }

            return list;
        }
    }

    /**
     * @param context
     * @param from    不同的界面进来要做不同的查询
     * @return
     */
    public static List<ModelMusicInfo> queryMusic(Context context, int from) {
        return queryMusic(context, from, null);
    }

    public static List<ModelMusicInfo> queryMusic(Context context, int from, String arg) {

        List<ModelMusicInfo> list = new ArrayList<>();

        switch (from) {
            case IConstants.START_FROM_LOCAL:
                if (DataSupport.count(ModelMusicInfo.class) > 0) {
                    //从本地数据库中读取
                    list.addAll(DataSupport.findAll(ModelMusicInfo.class));
                    return list;
                } else {
                    //从系统数据库中读取
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver cr = context.getContentResolver();

                    StringBuilder select = new StringBuilder(" 1=1 ");
                    // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
                    select.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
                    select.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

                    list.clear();
                    list.addAll(getMusicList(cr.query(uri, proj_music,
                            select.toString(), null,
                            MediaStore.Audio.Media.DATE_ADDED)));

                    for (ModelMusicInfo item : list) {
                        if (item.updateAll("data = ?", item.getData()) <= 0) {
                            item.save();
                        }
                    }

                    return list;
                }
            case IConstants.START_FROM_ARTIST:
                return DataSupport.where("artist = ?", arg).find(ModelMusicInfo.class);
            case IConstants.START_FROM_ALBUM:
                return DataSupport.where("albumId = ?", arg).find(ModelMusicInfo.class);
            case IConstants.START_FROM_FOLDER:
                return DataSupport.where("folder = ?", arg).find(ModelMusicInfo.class);
            default:
                return list;
        }
    }

    public static ArrayList<ModelMusicInfo> getMusicList(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<ModelMusicInfo> musicList = new ArrayList<>();
        while (cursor.moveToNext()) {
            ModelMusicInfo music = new ModelMusicInfo();
            music.setSongId(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID)));
            music.setAlbumId(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            music.setAlbum(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            music.setAlbumKey(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)));
            music.setDuration(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION)));
            music.setMusicName(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));
            music.setArtist(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            music.setAddTime(cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)));

            String filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));
            music.setData(filePath);
            music.setFolder(filePath.substring(0,
                    filePath.lastIndexOf(File.separator)));
            music.setMusicNameKey(StringHelper.getPingYin(music.getMusicName()));
            music.setArtistKey(StringHelper.getPingYin(music.getArtist()));
            musicList.add(music);
        }
        cursor.close();
        return musicList;
    }

    public static List<ModelAlbumInfo> getAlbumList(Cursor cursor) {
        List<ModelAlbumInfo> list = new ArrayList<ModelAlbumInfo>();
        while (cursor.moveToNext()) {
            ModelAlbumInfo info = new ModelAlbumInfo();
            info.setArtist(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ARTIST)));
            info.setAlbumName(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
            info.setAlbumId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)));
            info.setNumberOfSongs(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)));
            info.setAlbumArt(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static List<ModelArtistInfo> getArtistList(Cursor cursor) {
        List<ModelArtistInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            ModelArtistInfo info = new ModelArtistInfo();
            info.setArtistName(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
            info.setNumberOfTracks(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static List<ModelFolderInfo> getFolderList(Cursor cursor) {
        List<ModelFolderInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            ModelFolderInfo info = new ModelFolderInfo();
            String filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Files.FileColumns.DATA));
            info.setFolderPath(filePath.substring(0,
                    filePath.lastIndexOf(File.separator)));
            info.setFolderName(info.getFolderPath().substring(info.getFolderPath()
                    .lastIndexOf(File.separator) + 1));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

    public static Bitmap getCachedArtwork(Context context, long artIndex,
                                          Bitmap defaultArtwork) {
        Bitmap bitmap = null;
        synchronized (sArtCache) {
            bitmap = sArtCache.get(artIndex);
        }
        if (context == null) {
            return null;
        }
        if (bitmap == null) {
            bitmap = defaultArtwork;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if (b != null) {
                bitmap = b;
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Bitmap value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, bitmap);
                    } else {
                        bitmap = value;
                    }
                }
            }
        }
        return bitmap;
    }

    // A really simple BitmapDrawable-like class, that doesn't do
    // scaling, dithering or filtering.
    /*
     * private static class FastBitmapDrawable extends Drawable { private Bitmap
	 * mBitmap; public FastBitmapDrawable(Bitmap b) { mBitmap = b; }
	 * 
	 * @Override public void draw(Canvas canvas) { canvas.drawBitmap(mBitmap, 0,
	 * 0, null); }
	 * 
	 * @Override public int getOpacity() { return PixelFormat.OPAQUE; }
	 * 
	 * @Override public void setAlpha(int alpha) { }
	 * 
	 * @Override public void setColorFilter(ColorFilter cf) { } }
	 */

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    public static Bitmap getArtworkQuick(Context context, long album_id, int w,
                                         int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
                        null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w
                            || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if (tmp != b)
                            b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * 根据歌曲的ID，寻找出歌曲在当前播放列表中的位置
     *
     * @param list
     * @param id
     * @return
     */
    public static int seekPosInListById(List<ModelMusicInfo> list, int id) {
        if (id == -1) {
            return -1;
        }
        int result = -1;
        if (list != null) {

            for (int i = 0; i < list.size(); i++) {
                if (id == list.get(i).getSongId()) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead) This method always returns
     * the default album art icon when no album art is found.
     */
    /*
     * public static Bitmap getArtwork(Context context, long song_id, long
	 * album_id) { return getArtwork(context, song_id, album_id, true); }
	 */

    /**
     * Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    /*
     * public static Bitmap getArtwork(Context context, long song_id, long
	 * album_id, boolean allowdefault) {
	 * 
	 * // This is something that is not in the database, so get the album // art
	 * directly // from the file. if (song_id >= 0) { Bitmap bm =
	 * getArtworkFromFile(context, song_id, -1); if (bm != null) { return bm; }
	 * else { return getArtwork(context, -1, album_id); } } else if (album_id >=
	 * 0) {
	 * 
	 * ContentResolver res = context.getContentResolver(); Uri uri =
	 * ContentUris.withAppendedId(sArtworkUri, album_id); if (uri != null) {
	 * InputStream in = null; try { in = res.openInputStream(uri); return
	 * BitmapFactory.decodeStream(in, null, sBitmapOptions); } catch
	 * (FileNotFoundException ex) { // The album art thumbnail does not actually
	 * exist. Maybe // the // user deleted it, or // maybe it never existed to
	 * begin with. Bitmap bm = getArtworkFromFile(context, song_id, album_id);
	 * if (bm != null) { if (bm.getConfig() == null) { bm =
	 * bm.copy(Bitmap.Config.RGB_565, false); if (bm == null && allowdefault) {
	 * return getDefaultArtwork(context); } } } else if (allowdefault) { bm =
	 * getDefaultArtwork(context); } return bm; } finally { try { if (in !=
	 * null) { in.close(); } } catch (IOException ex) { } } }
	 * 
	 * }
	 * 
	 * return null; }
	 * 
	 * // get album art for specified file private static final String
	 * sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
	 * .toString(); private static Bitmap mCachedBit = null;
	 * 
	 * private static Bitmap getArtworkFromFile(Context context, long songid,
	 * long albumid) { Bitmap bm = null; byte[] art = null; String path = null;
	 * 
	 * if (albumid < 0 && songid < 0) { throw new IllegalArgumentException(
	 * "Must specify an album or a song id"); }
	 * 
	 * try { if (songid >= 0) { Uri uri =
	 * Uri.parse("content://media/external/audio/media/" + songid +
	 * "/albumart"); ParcelFileDescriptor pfd = context.getContentResolver()
	 * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
	 * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); }
	 * else { return getArtworkFromFile(context, -1, albumid); } } else if
	 * (albumid >= 0) { Uri uri = ContentUris.withAppendedId(sArtworkUri,
	 * albumid); ParcelFileDescriptor pfd = context.getContentResolver()
	 * .openFileDescriptor(uri, "r"); if (pfd != null) { FileDescriptor fd =
	 * pfd.getFileDescriptor(); bm = BitmapFactory.decodeFileDescriptor(fd); } }
	 * } catch (IllegalStateException ex) { } catch (FileNotFoundException ex) {
	 * } if (bm != null) { mCachedBit = bm; } return bm; }
	 * 
	 * private static Bitmap getDefaultArtwork(Context context) {
	 * BitmapFactory.Options opts = new BitmapFactory.Options();
	 * opts.inPreferredConfig = Bitmap.Config.ARGB_8888; return
	 * BitmapFactory.decodeStream(context.getResources()
	 * .openRawResource(R.drawable.img_album_background), null, opts); }
	 */
    public static void clearCache() {
        sArtCache.clear();
    }
}
