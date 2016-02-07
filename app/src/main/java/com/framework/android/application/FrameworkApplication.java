package com.framework.android.application;


import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

/**
 * Created by zack on 6/29/15.
 */
public class FrameworkApplication extends LitePalApplication {

    private static FrameworkApplication instance;//全局上下文实例

    public synchronized static FrameworkApplication getInstance() {
        if (instance == null) {
            instance = new FrameworkApplication();
        }
        return instance;
    }

    public synchronized static FrameworkApplication getContext() {
        return getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Connector.getDatabase();//初始化litepal数据库
    }

    private int playingMusicId;

    public int getPlayingMusicId() {
        return playingMusicId;
    }

    public void setPlayingMusicId(int playingMusicId) {
        this.playingMusicId = playingMusicId;
    }
}
