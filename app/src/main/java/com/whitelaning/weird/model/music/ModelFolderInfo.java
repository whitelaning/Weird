package com.whitelaning.weird.model.music;

import org.litepal.crud.DataSupport;

public class ModelFolderInfo extends DataSupport {
    public String folderName;
    public String folderPath;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
