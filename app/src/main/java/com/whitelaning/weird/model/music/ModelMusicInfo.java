package com.whitelaning.weird.model.music;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

public class ModelMusicInfo extends DataSupport implements Parcelable {
    private int duration;
    private int songId;
    private int albumId;
    private String album;
    private String albumKey;
    private String data;
    private String folder;
    private String musicName;
    private String musicNameKey;
    private String artist;
    private String artistKey;
    private String artistPicPath;

    public String getArtistPicPath() {
        return artistPicPath;
    }

    public void setArtistPicPath(String artistPicPath) {
        this.artistPicPath = artistPicPath;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getMusicNameKey() {
        return musicNameKey;
    }

    public void setMusicNameKey(String musicNameKey) {
        this.musicNameKey = musicNameKey;
    }

    public String getArtistKey() {
        return artistKey;
    }

    public void setArtistKey(String artistKey) {
        this.artistKey = artistKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public String getAlbumKey() {
        return albumKey;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.duration);
        dest.writeInt(this.songId);
        dest.writeInt(this.albumId);
        dest.writeString(this.album);
        dest.writeString(this.albumKey);
        dest.writeString(this.data);
        dest.writeString(this.folder);
        dest.writeString(this.musicName);
        dest.writeString(this.musicNameKey);
        dest.writeString(this.artist);
        dest.writeString(this.artistKey);
        dest.writeString(this.artistPicPath);
    }

    public ModelMusicInfo() {
    }

    protected ModelMusicInfo(Parcel in) {
        this.duration = in.readInt();
        this.songId = in.readInt();
        this.albumId = in.readInt();
        this.album = in.readString();
        this.albumKey = in.readString();
        this.data = in.readString();
        this.folder = in.readString();
        this.musicName = in.readString();
        this.musicNameKey = in.readString();
        this.artist = in.readString();
        this.artistKey = in.readString();
        this.artistPicPath = in.readString();
    }

    public static final Parcelable.Creator<ModelMusicInfo> CREATOR = new Parcelable.Creator<ModelMusicInfo>() {
        public ModelMusicInfo createFromParcel(Parcel source) {
            return new ModelMusicInfo(source);
        }

        public ModelMusicInfo[] newArray(int size) {
            return new ModelMusicInfo[size];
        }
    };

    @Override
    public String toString() {
        return "ModelMusicInfo{" +
                "duration=" + duration +
                ", songId=" + songId +
                ", albumId=" + albumId +
                ", album='" + album + '\'' +
                ", albumKey='" + albumKey + '\'' +
                ", data='" + data + '\'' +
                ", folder='" + folder + '\'' +
                ", musicName='" + musicName + '\'' +
                ", musicNameKey='" + musicNameKey + '\'' +
                ", artist='" + artist + '\'' +
                ", artistKey='" + artistKey + '\'' +
                ", artistPicPath='" + artistPicPath + '\'' +
                '}';
    }
}