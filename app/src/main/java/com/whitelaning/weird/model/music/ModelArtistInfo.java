package com.whitelaning.weird.model.music;

import org.litepal.crud.DataSupport;

public class ModelArtistInfo extends DataSupport {
    private String artistName;
    private int numberOfTracks;
    private String artistPicPath;

    public String getArtistPicPath() {
        return artistPicPath;
    }

    public void setArtistPicPath(String artistPicPath) {
        this.artistPicPath = artistPicPath;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }
}
