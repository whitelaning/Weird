package com.whitelaning.weird.model.music;

import com.framework.android.model.BaseEvent;

/**
 * Created by Zack White on 2016/2/7.
 */
public class EventNowPlayMusicInformation extends BaseEvent {

    private int songId;
    private int albumId;
    private String artist;
    private String artistPicPath;
    private String path;
    private String musicName;
    private int duration;

    public EventNowPlayMusicInformation(int TAG) {
        super(TAG);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setArtistPicPath(String artistPicPath) {
        this.artistPicPath = artistPicPath;
    }

    public String getArtistPicPath() {
        return artistPicPath;
    }
}

