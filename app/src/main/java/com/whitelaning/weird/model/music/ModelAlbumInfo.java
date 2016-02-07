package com.whitelaning.weird.model.music;


import org.litepal.crud.DataSupport;

public class ModelAlbumInfo extends DataSupport {
    private String albumName;
    private int albumId;
    private int numberOfSongs;
    private String albumArt;
    private String artist;
    private String albumPicPath;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumPicPath() {
        return albumPicPath;
    }

    public void setAlbumPicPath(String albumPicPath) {
        this.albumPicPath = albumPicPath;
    }
}
