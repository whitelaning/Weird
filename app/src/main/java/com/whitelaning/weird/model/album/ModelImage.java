package com.whitelaning.weird.model.album;

import java.io.File;

/**
 * Created by Zack White on 2016/1/23.
 */
public class ModelImage{
    private File file;
    private String folder;
    private String path;

    public ModelImage(File file) {
        this.file = file;
        this.folder = file.getParent();
        this.path = file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
