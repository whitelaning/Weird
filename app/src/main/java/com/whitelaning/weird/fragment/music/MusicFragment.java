package com.whitelaning.weird.fragment.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.framework.android.application.FrameworkApplication;
import com.framework.android.fragment.BaseFragment;
import com.framework.android.model.BaseEvent;
import com.framework.android.tool.PreferencesUtils;
import com.framework.android.view.ProgressLayout;
import com.framework.android.view.ViewPagerRelativeLayout;
import com.other.pagerslidingtabstrip.PagerSlidingTabStrip;
import com.whitelaning.weird.R;
import com.whitelaning.weird.activity.music.MusicPlayActivity;
import com.whitelaning.weird.binder.MediaBinder;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.fragment.music.child.MusicAlbumFragment;
import com.whitelaning.weird.fragment.music.child.MusicFolderFragment;
import com.whitelaning.weird.fragment.music.child.MusicSingerFragment;
import com.whitelaning.weird.fragment.music.child.MusicSingleFragment;
import com.whitelaning.weird.model.EventChangeToolBar;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.modelFetch.music.MusicFragmentModelFetch;
import com.whitelaning.weird.service.music.MediaService;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by Zack White on 1/28/2016.
 * 音乐页面的主布局
 */
public class MusicFragment extends BaseFragment {
    public final static String TAG = "MusicFragment.TAG";
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @Bind(R.id.viewPager)
    ViewPager viewPager;
    @Bind(R.id.viewPagerRelativeLayout)
    ViewPagerRelativeLayout viewPagerRelativeLayout;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;
    @Bind(R.id.songAlbum)
    ImageView songAlbum;
    @Bind(R.id.songNext)
    ImageView songNext;
    @Bind(R.id.songPlay)
    ImageView songPlay;
    @Bind(R.id.songName)
    TextView songName;
    @Bind(R.id.singerName)
    TextView singerName;
    @Bind(R.id.playControlRootLayout)
    RelativeLayout playControlRootLayout;

    private Context mContext;
    private MyPagerAdapter adapter;
    private MusicFragmentModelFetch mModelFetch;

    private MusicSingleFragment mMusicSingleFragment;
    private MusicSingerFragment mMusicSingerFragment;
    private MusicAlbumFragment mMusicAlbumFragment;
    private MusicFolderFragment mMusicFolderFragment;

    private int indexFragment;
    private Intent playIntent;
    private MediaBinder binder;
    private ServiceConnection serviceConnection;
    private ModelMusicInfo mMusicInfo;//当前正在播放的音乐的信息

    private boolean bindState = false;// ----服务绑定状态

    private Animation myAnimation_Translate_alpha; // ----淡化推出动画

    public MusicFragment() {
        // Required empty public constructor
    }

    public static MusicFragment newInstance(Bundle args) {
        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 获取Activity传递过来的数据
        }
        setRetainInstance(true);
        initIntent();
        initServiceConnection();// ----初始化服务绑定
        EventBus.getDefault().register(this);
    }

    private void initIntent() {
        playIntent = new Intent(FrameworkApplication.getContext(), MediaService.class);
    }

    private void initModelFetch() {
        mModelFetch = new MusicFragmentModelFetch(mContext, mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        initModelFetch();
        initPagerSlidingTabStrip();
        return view;
    }

    private void initView() {
        mProgressLayout.showProgress();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mModelFetch.checkData();
    }

    private void initData() {
        mContext = getActivity();

        // ----初始化动画
        myAnimation_Translate_alpha = AnimationUtils.loadAnimation(mContext,
                R.anim.translate_alpha);

        adapter = new MyPagerAdapter(getChildFragmentManager());
        viewPagerRelativeLayout.setChild_viewpager(viewPager);
        viewPager.setOffscreenPageLimit(3);//设置缓存view 的个数（实际有3个，缓存2个+正在显示的1个）
        viewPager.setAdapter(adapter);

        mMusicInfo = new ModelMusicInfo();
        mMusicInfo.setArtistPicPath(PreferencesUtils.getString("lastSongArtistPicPath"));
        mMusicInfo.setSongId(PreferencesUtils.getInt("lastSongSongId"));
        mMusicInfo.setArtist(PreferencesUtils.getString("lastSongArtist"));
        mMusicInfo.setMusicName(PreferencesUtils.getString("lastSongMusicName"));

        Glide.with(mContext)
                .load(mMusicInfo.getArtistPicPath())
                .override(160, 160)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_disk_play_program)
                .error(R.drawable.placeholder_disk_play_program)
                .fallback(R.drawable.placeholder_disk_play_program)
                .into(songAlbum);

        songName.setText(mMusicInfo.getMusicName());
        singerName.setText(mMusicInfo.getArtist());
    }

    private void initPagerSlidingTabStrip() {
        tabs.setIndicatorColorResource(R.color.colorRedDark);
        tabs.setSelectedTabTextColorResource(R.color.colorRedDark);
        tabs.setAllCaps(false);
        tabs.setShouldExpand(true);
        tabs.setIndicatorHeight(5);
        tabs.setUnderlineHeight(5);
        tabs.setViewPager(viewPager);
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageSelected(int position) {
                indexFragment = position;
                initToolbar();
            }
        });
    }

    private void initToolbar() {
        EventChangeToolBar object = new EventChangeToolBar(EventCode.EVENT_CHANGE_TOOLBAR_FROM_MUSIC_FRAGMENT);
        object.setColor(R.color.colorRedDark);

        switch (indexFragment) {
            case 0:
                object.setType(0);
                break;
            case 1:
                if (mMusicSingerFragment.getIsFolder()) {
                    object.setType(0);
                } else {
                    object.setType(1);
                    object.setTitle(mMusicSingerFragment.getLastSelectArtistName());
                }
                break;
            case 2:
                if (mMusicAlbumFragment.getIsFolder()) {
                    object.setType(0);
                } else {
                    object.setType(1);
                    object.setTitle(mMusicAlbumFragment.getLastSelectAlbumName());
                }
                break;
            case 3:
                if (mMusicFolderFragment.getIsFolder()) {
                    object.setType(0);
                } else {
                    object.setType(1);
                    object.setTitle(mMusicFolderFragment.getLastSelectFolderName());
                }
                break;
        }

        EventBus.getDefault().post(object);
    }

    public int getIndex() {
        return indexFragment;
    }

    public Fragment getFragment(int position) {
        return adapter.getItem(position);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"Single", "Singer", "Album", "Folder"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mMusicSingleFragment == null) {
                        mMusicSingleFragment = MusicSingleFragment.newInstance(null);
                    }
                    return mMusicSingleFragment;
                case 1:
                    if (mMusicSingerFragment == null) {
                        mMusicSingerFragment = new MusicSingerFragment();
                    }
                    return mMusicSingerFragment;
                case 2:
                    if (mMusicAlbumFragment == null) {
                        mMusicAlbumFragment = new MusicAlbumFragment();
                    }
                    return mMusicAlbumFragment;
                case 3:
                    if (mMusicFolderFragment == null) {
                        mMusicFolderFragment = new MusicFolderFragment();
                    }
                    return mMusicFolderFragment;
            }

            return null;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        // ----绑定服务
        if (!bindState) {
            bindState = FrameworkApplication.getContext().bindService(playIntent, serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (bindState) {
            FrameworkApplication.getContext().unbindService(serviceConnection);// ----解除服务绑定
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<MusicFragment> mFragment;

        MyHandler(MusicFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicFragment fragment = mFragment.get();
            switch (msg.what) {
                case 1://检测数据返回
                    fragment.mProgressLayout.showContent();
                    break;
                case 7007://test code
                    break;
            }
        }
    }


    /**
     * 初始化服务绑定
     */
    private void initServiceConnection() {
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MediaBinder) service;
                if (binder != null) {
                    binder.setOnPlayStartListener(new MediaBinder.OnPlayStartListener() {

                        // ----播放歌曲初始化
                        @Override
                        public void onStart(ModelMusicInfo info) {

                            mMusicInfo = info;

                            Glide.with(mContext)
                                    .load(info.getArtistPicPath())
                                    .override(160, 160)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.placeholder_disk_play_program)
                                    .error(R.drawable.placeholder_disk_play_program)
                                    .fallback(R.drawable.placeholder_disk_play_program)
                                    .into(songAlbum);

                            songName.setText(info.getMusicName());
                            singerName.setText(info.getArtist());

                            songPlay.setImageResource(R.drawable.playbar_btn_pause);
                        }
                    });

                    // ----播放开始时
                    binder.setOnPlayingListener(new MediaBinder.OnPlayingListener() {

                        @Override
                        public void onPlay(int currentPosition) {

                        }
                    });

                    // ----暂停时
                    binder.setOnPlayPauseListener(new MediaBinder.OnPlayPauseListener() {
                        @Override
                        public void onPause() {
                            songPlay.setImageResource(R.drawable.playbar_btn_play);
                        }
                    });

                    // ----播放完成时
                    binder.setOnPlayCompletionListener(new MediaBinder.OnPlayCompleteListener() {

                        @Override
                        public void onPlayComplete() {
                            songPlay.setImageResource(R.drawable.playbar_btn_play);
                        }
                    });

                    // ----播放错误时
                    binder.setOnPlayErrorListener(new MediaBinder.OnPlayErrorListener() {

                        @Override
                        public void onPlayError() {
                            songPlay.setImageResource(R.drawable.playbar_btn_play);
                        }
                    });
                }
            }
        };
    }

    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent != null) {
            switch (baseEvent.getTAG()) {
                case EventCode.EVENT_MUSIC_PLAY_MUSIC_INFORMATION:
                    break;
            }
        }
    }

    @OnClick({R.id.songPlay, R.id.songNext, R.id.playControlRootLayout})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.songPlay:
                if (binder != null) {
                    songPlay.startAnimation(myAnimation_Translate_alpha);
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
                }
                break;
            case R.id.songNext:
                if (binder != null) {
                    songNext.startAnimation(myAnimation_Translate_alpha);
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
                }
                break;
            case R.id.playControlRootLayout://跳转到唱片页面
                if (binder != null && mMusicInfo != null) {
                    FrameworkApplication.getContext().unbindService(serviceConnection);// ----解除服务绑定;// ----解除绑定
                    bindState = false;// ----状态更新
                    MusicPlayActivity.startActivityForResult(mContext, 1000, mMusicInfo);
                }
                break;
        }
    }
}
