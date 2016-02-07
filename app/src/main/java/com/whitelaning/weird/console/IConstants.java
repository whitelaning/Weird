package com.whitelaning.weird.console;

/**
 * 常量
 */
public class IConstants {
    //天天动听的接口
    public static final String DONGTING_ARTIST_SEARCH = "http://search.dongting.com/artist/search";
    public static final String DONGTING_ALBUM_SEARCH = "http://search.dongting.com/album/search";

    //歌手和专辑列表点击都会进入MyMusic 此时要传递参数表明是从哪里进入的
    public static final String FROM = "from";
    public static final int START_FROM_ARTIST = 1;
    public static final int START_FROM_ALBUM = 2;
    public static final int START_FROM_LOCAL = 3;
    public static final int START_FROM_FOLDER = 4;
}
