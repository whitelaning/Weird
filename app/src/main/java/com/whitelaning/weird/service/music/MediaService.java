package com.whitelaning.weird.service.music;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.framework.android.application.FrameworkApplication;
import com.framework.android.tool.PreferencesUtils;
import com.framework.android.tool.StringUtils;
import com.framework.android.tool.ToastUtils;
import com.whitelaning.weird.binder.MediaBinder;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.model.music.EventNowPlayMusicInformation;
import com.whitelaning.weird.model.music.ModelMusicInfo;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 控制播放服务
 */
public class MediaService extends Service {

    public static final int CONTROL_COMMAND_PLAY = 0;//----控制命令：播放或者暂停
    public static final int CONTROL_COMMAND_PREVIOUS = 1;//----控制命令：上一首
    public static final int CONTROL_COMMAND_NEXT = 2;//----控制命令：下一首
    public static final int CONTROL_COMMAND_MODE = 3;//----控制命令：播放模式切换
    public static final int CONTROL_COMMAND_REWIND = 4;//----控制命令：快退
    public static final int CONTROL_COMMAND_FORWARD = 5;//----控制命令：快进
    public static final int CONTROL_COMMAND_REPLAY = 6;//----控制命令：用于快退、快进后的继续播放

    private static final int MEDIA_PLAY_ERROR = 0;
    private static final int MEDIA_PLAY_START = 1;
    private static final int MEDIA_PLAY_UPDATE = 2;
    private static final int MEDIA_PLAY_COMPLETE = 3;
    private static final int MEDIA_PLAY_REWIND = 5;
    private static final int MEDIA_PLAY_FORWARD = 6;

    private final int MODE_NORMAL = 0;//----顺序播放，放到最后一首停止
    private final int MODE_REPEAT_ONE = 1;//----单曲循环
    private final int MODE_REPEAT_ALL = 2;//----全部循环
    private final int MODE_RANDOM = 3;//----随即播放
    private final int UPDATE_UI_TIME = 1000;//----UI更新间隔1秒

    public static MediaPlayer mediaPlayer;

    private String mp3Path;//----mp3文件路径

    private int mode = MODE_NORMAL;//----播放模式(默认顺序播放)

    private int mp3CurrentTime = 0;//----歌曲当前时间
    private int mp3DurationTime = 0;//----歌曲总时间


    private MediaBinder mBinder;
    private ServiceHandler mHandler;
    private ServiceReceiver receiver;
    private int position = -1;
    private int type = -1;
    private String select;//搜索条件

    private ModelMusicInfo mMusicInfo;

    private static List<ModelMusicInfo> musicList;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        initListener();
        initReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("data");
            if (bundle != null && !bundle.isEmpty()) {
                initSongList(bundle);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initSongList(Bundle bundle) {

        position = bundle.getInt("position");

        //--获取播放歌曲的信息
        if (!StringUtils.equals(select, bundle.getString("select"))) {
            type = bundle.getInt("type");
            select = bundle.getString("select");
            musicList = getMusicList(type, select);
        }

        if (musicList == null || musicList.size() <= 0) {
            ToastUtils.show("something has error");
        } else {

            if (position == -1) {
                int lastSongId = PreferencesUtils.getInt("lastSongSongId");
                for (int i = 0; i < musicList.size(); i++) {
                    if (musicList.get(i).getSongId() == lastSongId && lastSongId != -1) {
                        position = i;
                        break;
                    }
                }
            }

            if (position >= 0) {
                mMusicInfo = musicList.get(position);
                play();
            }
        }
    }

    private List<ModelMusicInfo> getMusicList(int type, String select) {
        switch (type) {
            case 0:
                return DataSupport.findAll(ModelMusicInfo.class);
            case 1:
                return DataSupport.where("artist = ?", select).find(ModelMusicInfo.class);
            case 2:
                return DataSupport.where("albumId = ?", select).find(ModelMusicInfo.class);
            case 3:
                return DataSupport.where("folder = ?", select).find(ModelMusicInfo.class);
        }

        return null;
    }

    private void initListener() {

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                start();
                mp3CurrentTime = 0;//----重置
                prepared();//----准备播放
            }
        });

        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                removeAllMsg();//----移除所有消息
                mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
            }
        });

        mediaPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                removeAllMsg();//----移除所有消息
                mp.reset();
                File file = new File(mp3Path);
                if (file.exists()) {
                    ToastUtils.show("Playing was wrong");
                } else {
                    ToastUtils.show("File is not exist");
                    mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
                }
                mp3Path = null;
                return true;
            }
        });

        mBinder.setOnServiceBinderListener(new MediaBinder.OnServiceBinderListener() {

            @Override
            public void seekBarStartTrackingTouch() {
                if (mediaPlayer.isPlaying()) {
                    removeUpdateMsg();
                }
            }

            @Override
            public void seekBarStopTrackingTouch(int progress) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(progress);
                    update();
                }
            }

            @Override
            public void control(int command) {
                switch (command) {
                    case CONTROL_COMMAND_PLAY://----播放与暂停
                        if (mediaPlayer.isPlaying()) {
                            pause();
                        } else {
                            if (mp3Path != null) {
                                start();
                                prepared();
                            } else { //----无指定情况下获取上次的列表播放上次的歌曲
                                Intent intent = new Intent(FrameworkApplication.getContext(), MediaService.class);

                                Bundle bundle = new Bundle();

                                bundle.putInt("position", -1);
                                bundle.putInt("type", PreferencesUtils.getInt("lastSongType"));
                                bundle.putString("select", PreferencesUtils.getString("lastSongSelect"));

                                intent.putExtra("data", bundle);
                                FrameworkApplication.getContext().startService(intent);
                            }
                        }
                        break;

                    case CONTROL_COMMAND_PREVIOUS://----上一首
                        previous();
                        break;

                    case CONTROL_COMMAND_NEXT://----下一首
                        next();
                        break;

                    case CONTROL_COMMAND_MODE://----播放模式
                        if (mode < MODE_RANDOM) {
                            mode++;
                        } else {
                            mode = MODE_NORMAL;
                        }
                        switch (mode) {
                            case MODE_NORMAL:
                                Toast.makeText(getApplicationContext(), "顺序播放",
                                        Toast.LENGTH_SHORT).show();
                                break;

                            case MODE_REPEAT_ONE:
                                Toast.makeText(getApplicationContext(), "单曲循环",
                                        Toast.LENGTH_SHORT).show();
                                break;

                            case MODE_REPEAT_ALL:
                                Toast.makeText(getApplicationContext(), "全部循环",
                                        Toast.LENGTH_SHORT).show();
                                break;

                            case MODE_RANDOM:
                                Toast.makeText(getApplicationContext(), "随机播放",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                        mBinder.modeChange(mode);
                        break;

                    case CONTROL_COMMAND_REWIND://----快退
                        if (mediaPlayer.isPlaying()) {
                            removeAllMsg();
                            rewind();
                        }
                        break;

                    case CONTROL_COMMAND_FORWARD://----快进
                        if (mediaPlayer.isPlaying()) {
                            removeAllMsg();
                            forward();
                        }
                        break;

                    case CONTROL_COMMAND_REPLAY://----用于快退、快进后的继续播放
                        if (mediaPlayer.isPlaying()) {
                            replay();
                        }
                        break;
                }
            }
        });
    }

    private void initData() {
        mediaPlayer = new MediaPlayer();
        mHandler = new ServiceHandler(this);
        mBinder = new MediaBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            stopForeground(true);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            removeAllMsg();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        removeAllMsg();//----移除所有消息
        return true;//----一定返回true，允许执行onRebind
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (mediaPlayer.isPlaying()) {//----如果正在播放重新绑定服务的时候重新注册
            prepared();//----因为消息已经移除，所有需要重新开启更新操作
        } else {
            if (mp3Path != null) {//----暂停原先播放重新开页面需要恢复原先的状态
                mp3DurationTime = mediaPlayer.getDuration();
                mBinder.playStart(mMusicInfo);
                mp3CurrentTime = mediaPlayer.getCurrentPosition();
                mBinder.playUpdate(mp3CurrentTime);
                mBinder.playPause();
            }
        }
        mBinder.modeChange(mode);
    }

    //----播放操作------------------------------

    private void play() {
        mp3Path = mMusicInfo.getData();
        if (!TextUtils.isEmpty(mp3Path)) {
            initMedia();//----初始化音乐
        } else {
            ToastUtils.show("music path error");
        }
    }

    //----自动播放操作----------------------------

    private void autoPlay() {
        if (mode == MODE_NORMAL) {
            if (position != getSize() - 1) {
                next();
            } else {
                mBinder.playPause();
            }
        } else if (mode == MODE_REPEAT_ONE) {
            play();
        } else {
            next();
        }
    }

    //---上一首操作-----------------------------

    private void previous() {
        int size = getSize();
        if (size > 0) {
            if (mode == MODE_RANDOM) {
                position = (int) (Math.random() * size);
            } else {
                if (position == 0) {
                    position = size - 1;
                } else {
                    position--;
                }
            }
            startServiceCommand();
        }
    }

    //----下一首操作----------------------

    private void next() {
        int size = getSize();
        if (size > 0) {
            if (mode == MODE_RANDOM) {
                position = (int) (Math.random() * size);
            } else {
                if (position == size - 1) {
                    position = 0;
                } else {
                    position++;
                }
            }
            startServiceCommand();
        }
    }

    //----快退-----------------------------

    private void rewind() {
        int current = mp3CurrentTime - 1000;
        mp3CurrentTime = current > 0 ? current : 0;
        mBinder.playUpdate(mp3CurrentTime);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
    }

    //----快进-----------------------------

    private void forward() {
        mp3CurrentTime = mp3CurrentTime + 1000 < mp3DurationTime ? mp3CurrentTime + 1000 : mp3DurationTime;
        mBinder.playUpdate(mp3CurrentTime);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
    }

    //----获得列表歌曲数量--------------------------------

    public static int getSize() {
        if (musicList == null) {
            return 0;
        } else {
            return musicList.size();
        }
    }

    //----用于快退、快进后的继续播放---------------------

    private void replay() {
        if (mHandler.hasMessages(MEDIA_PLAY_REWIND)) {
            mHandler.removeMessages(MEDIA_PLAY_REWIND);
        }
        if (mHandler.hasMessages(MEDIA_PLAY_FORWARD)) {
            mHandler.removeMessages(MEDIA_PLAY_FORWARD);
        }
        mediaPlayer.seekTo(mp3CurrentTime);
        mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE);
    }

    // ----内部模拟生成启动服务的命令
    private void startServiceCommand() {

        Intent intent = new Intent(FrameworkApplication.getContext(), MediaService.class);

        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putInt("type", type);
        bundle.putString("select", select);

        intent.putExtra("data", bundle);
        FrameworkApplication.getContext().startService(intent);
    }

    //----初始化媒体播放器------------------------------

    private void initMedia() {
        try {
            removeAllMsg();//----对于重新播放需要移除所有消息
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mp3Path);
            mediaPlayer.prepareAsync();
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show("something has error");
        }
    }

    //----准备好开始播放工作----------------------------
    private void prepared() {
        mHandler.sendEmptyMessage(MEDIA_PLAY_START);
        sendMusicInformation();
        saveMusicInformationByPreferences();
    }

    private void saveMusicInformationByPreferences() {
        PreferencesUtils.putString("lastSongSelect", select);
        PreferencesUtils.putInt("lastSongType", type);
        PreferencesUtils.putString("lastSongMusicName", mMusicInfo.getMusicName());
        PreferencesUtils.putString("lastSongArtist", mMusicInfo.getArtist());
        PreferencesUtils.putInt("lastSongSongId", mMusicInfo.getSongId());
        PreferencesUtils.putString("lastSongArtistPicPath", mMusicInfo.getArtistPicPath());
    }


    private void sendMusicInformation() {
        EventNowPlayMusicInformation object = new EventNowPlayMusicInformation(EventCode.EVENT_MUSIC_PLAY_MUSIC_INFORMATION);
        object.setPath(mMusicInfo.getData());
        object.setAlbumId(mMusicInfo.getAlbumId());
        object.setArtist(mMusicInfo.getArtist());
        object.setArtistPicPath(mMusicInfo.getArtistPicPath());
        object.setSongId(mMusicInfo.getSongId());
        object.setMusicName(mMusicInfo.getMusicName());
        object.setDuration(mMusicInfo.getDuration());

        EventBus.getDefault().post(object);

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_MUSIC_START);
        intent.putExtra("musicInfor", mMusicInfo);
        localBroadcastManager.sendBroadcast(intent);
    }

    //----开始播放，获得总时间和AudioSessionId，并启动更新UI任务-------------
    private void start() {
        mediaPlayer.start();
        mp3DurationTime = mediaPlayer.getDuration();
        mBinder.playStart(mMusicInfo);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_MUSIC_START);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void update() {
        mp3CurrentTime = mediaPlayer.getCurrentPosition();
        mBinder.playUpdate(mp3CurrentTime);
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
    }

    //----暂停音乐-------------------------
    private void pause() {

        removeAllMsg();//----移除所有消息
        mediaPlayer.pause();
        mBinder.playPause();

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION_MUSIC_PAUSE);
        localBroadcastManager.sendBroadcast(intent);
    }

    //----移除更新UI的消息-------------------
    private void removeUpdateMsg() {
        if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
            mHandler.removeMessages(MEDIA_PLAY_UPDATE);
        }
    }

    //----播放完成-------------------------
    private void complete() {
        mBinder.playComplete();
        mBinder.playUpdate(mp3DurationTime);
        autoPlay();
    }

    //----播放出错------------------------
    private void error() {
        mBinder.playError();
        mBinder.playPause();
    }

    //----移除所有消息------------------------------
    private void removeAllMsg() {
        removeUpdateMsg();
    }

    private static class ServiceHandler extends Handler {
        WeakReference<MediaService> reference;

        ServiceHandler(MediaService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaService theService = reference.get();
            switch (msg.what) {
                case MEDIA_PLAY_START:
                    theService.start();//----播放开始
                    break;

                case MEDIA_PLAY_UPDATE:
                    theService.update();//----更新UI
                    break;

                case MEDIA_PLAY_COMPLETE:
                    theService.complete();//----播放完成
                    break;

                case MEDIA_PLAY_ERROR:
                    theService.error();//----播放出错
                    break;

                case MEDIA_PLAY_REWIND:
                    theService.rewind();//----快退线程
                    break;

                case MEDIA_PLAY_FORWARD:
                    theService.forward();//----快进线程
                    break;
            }
        }
    }

    public static final String BROADCAST_ACTION_MUSIC_START =
            "com.whitelaning.zackwhite.weird.music.start";
    public static final String BROADCAST_ACTION_MUSIC_PAUSE =
            "com.whitelaning.zackwhite.weird.music.pause";
    public static final String BROADCAST_ACTION_MUSIC_PLAY =
            "com.whitelaning.zackwhite.weird.music.play";
    public static final String BROADCAST_ACTION_MUSIC_NEXT =
            "com.whitelaning.zackwhite.weird.music.next";
    public static final String BROADCAST_ACTION_MUSIC_LAST =
            "com.whitelaning.zackwhite.weird.music.last";

    final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                switch (intent.getAction()) {
                    case BROADCAST_ACTION_MUSIC_PLAY:
                        if (mediaPlayer.isPlaying()) {
                            pause();
                        } else {
                            if (mp3Path != null) {
                                start();
                                prepared();
                            } else {
                                Intent intent2 = new Intent(FrameworkApplication.getContext(), MediaService.class);

                                Bundle bundle = new Bundle();

                                bundle.putInt("position", -1);
                                bundle.putInt("type", PreferencesUtils.getInt("lastSongType"));
                                bundle.putString("select", PreferencesUtils.getString("lastSongSelect"));

                                intent2.putExtra("data", bundle);
                                FrameworkApplication.getContext().startService(intent2);
                            }
                        }
                        break;
                    case BROADCAST_ACTION_MUSIC_NEXT:
                        next();
                        break;
                    case BROADCAST_ACTION_MUSIC_LAST:
                        previous();
                        break;
                }
            }
        }
    }

    private void initReceiver() {
        receiver = new ServiceReceiver();//----注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG); //----耳机插入状态广播
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(BROADCAST_ACTION_MUSIC_PLAY);
        intentFilter.addAction(BROADCAST_ACTION_MUSIC_NEXT);
        intentFilter.addAction(BROADCAST_ACTION_MUSIC_LAST);
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }
}


