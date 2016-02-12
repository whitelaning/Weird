package com.whitelaning.weird.activity.music;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.framework.android.activity.BaseActivity;
import com.framework.android.application.FrameworkApplication;
import com.framework.android.tool.TimeUtils;
import com.framework.android.tool.ToastUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.whitelaning.weird.R;
import com.whitelaning.weird.binder.MediaBinder;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.service.music.MediaService;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

public class MusicPlayActivity extends BaseActivity {

    public final static String TAG = "MusicPlayActivity.TAG";

    @Bind(R.id.playNeedle)
    ImageView playNeedle;
    @Bind(R.id.playNeedleShadow)
    ImageView playNeedleShadow;
    @Bind(R.id.playSong)
    ImageView playSong;
    @Bind(R.id.nextSong)
    ImageView nextSong;
    @Bind(R.id.lastSong)
    ImageView lastSong;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.playType)
    ImageView playType;
    @Bind(R.id.playAlbum)
    ImageView playAlbum;
    @Bind(R.id.layoutTop)
    RelativeLayout layoutTop;
    @Bind(R.id.layoutBottom)
    RelativeLayout layoutBottom;
    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;
    @Bind(R.id.mSeekBar)
    DiscreteSeekBar mSeekBar;
    @Bind(R.id.currentTime)
    TextView currentTime;
    @Bind(R.id.musicTime)
    TextView musicTime;

    private boolean isAnimation = false;
    private boolean bindState = false;// ----服务绑定状态

    private Intent playIntent;
    private MediaBinder binder;
    private ServiceConnection serviceConnection;
    private ModelMusicInfo mMusicInfo;
    private ObjectAnimator playAlbumAnimator;
    private boolean mIsStartTrackingTouch = false;
    private int mProgressValue = 0;//当前进度条的进度，最高为100

    private LocalBroadcastManager localBroadcastManager;

    public static void startActivityForResult(Context mContext, int requestCode, ModelMusicInfo mMusicInfo) {
        Intent intent = new Intent(mContext, MusicPlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("musicInfo", mMusicInfo);
        ((Activity) mContext).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        ButterKnife.bind(this);
        initView();
        initIntent();
        initData();
        initServiceConnection();// ----初始化服务绑定
        initToolbar(mMusicInfo.getMusicName(), mMusicInfo.getArtist(), getResources().getColor(R.color.colorRedDark));
        initListener();
    }

    private void initView() {
        mSeekBar.setIndicatorPopupEnabled(true);
        mSeekBar.setIndicatorFormatter("00:00");
        mSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value;
            }
        });

        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                mProgressValue = value;

                if (mIsStartTrackingTouch) {
                    mSeekBar.setIndicatorFormatter(TimeUtils.secToTime(mProgressValue * MediaService.mediaPlayer.getDuration() / 100));
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                mIsStartTrackingTouch = true;
                mSeekBar.setIndicatorFormatter(TimeUtils.secToTime(MediaService.mediaPlayer.getCurrentPosition()));

                if (binder != null) {
                    binder.seekBarStartTrackingTouch();
                }
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                mIsStartTrackingTouch = false;
                if (binder != null) {
                    binder.seekBarStopTrackingTouch(seekBar.getProgress() * MediaService.mediaPlayer.getDuration() / 100);
                }
            }
        });
    }

    private void initData() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        setArtistPic();
        initPlayAlbumAnimator();

        currentTime.setText("00:00");
        musicTime.setText(TimeUtils.secToTime(mMusicInfo.getDuration()));

        if (MediaService.mediaPlayer != null && MediaService.mediaPlayer.isPlaying()) {
            initPlayingAnimator();
        } else {
            //nothing
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

                            setArtistPic();
                            initToolbar(mMusicInfo.getMusicName(), mMusicInfo.getArtist(), getResources().getColor(R.color.colorRedDark));

                            currentTime.setText("00:00");
                            musicTime.setText(TimeUtils.secToTime(mMusicInfo.getDuration()));
                        }
                    });

                    // ----播放开始时
                    binder.setOnPlayingListener(new MediaBinder.OnPlayingListener() {

                        @Override
                        public void onPlay(int currentPosition) {
                            mSeekBar.setProgress(currentPosition * 100 / mMusicInfo.getDuration());
                            currentTime.setText(TimeUtils.secToTime(currentPosition));
                        }
                    });

                    // ----暂停时
                    binder.setOnPlayPauseListener(new MediaBinder.OnPlayPauseListener() {
                        @Override
                        public void onPause() {
                        }
                    });

                    // ----播放完成时
                    binder.setOnPlayCompletionListener(new MediaBinder.OnPlayCompleteListener() {

                        @Override
                        public void onPlayComplete() {
                        }
                    });

                    // ----播放错误时
                    binder.setOnPlayErrorListener(new MediaBinder.OnPlayErrorListener() {

                        @Override
                        public void onPlayError() {
                        }
                    });
                }
            }
        };
    }

    private void setArtistPic() {
        Glide.with(this)
                .load(mMusicInfo.getArtistPicPath())
                .override(320, 320)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.playing_cover_lp)
                .error(R.drawable.playing_cover_lp)
                .fallback(R.drawable.playing_cover_lp)
                .into(playAlbum);
    }

    private void initPlayAlbumAnimator() {
        LinearInterpolator lin = new LinearInterpolator();
        playAlbumAnimator = ObjectAnimator.ofFloat(playAlbum, "rotation", 0, +360).setDuration(4000);
        playAlbumAnimator.setRepeatCount(-1);
        playAlbumAnimator.setRepeatMode(ValueAnimator.INFINITE);
        playAlbumAnimator.setInterpolator(lin);
        playAlbumAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bindState) {
            FrameworkApplication.getContext().unbindService(serviceConnection);// ----解除服务绑定
        }
    }

    private void initIntent() {
        mMusicInfo = getIntent().getParcelableExtra("musicInfo");
        if (mMusicInfo == null) {
            finish();
            ToastUtils.show("something is mistake");
            return;
        }

        playIntent = new Intent(FrameworkApplication.getContext(), MediaService.class);
    }

    private void initToolbar(String title, String subTitle, @ColorInt int color) {
        mToolbar.setTitle(title);
        mToolbar.setSubtitle(subTitle);
        mToolbar.setBackgroundColor(color);
        this.getWindow().setStatusBarColor(color);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initListener() {

    }

    private void changePlayNeedleState() {
        if (MediaService.mediaPlayer != null && MediaService.mediaPlayer.isPlaying()) {
            isAnimation = true;
            playendAnimator();
        } else {
            isAnimation = true;
            playingAnimator();
        }
    }

    private void playendAnimator() {
        changeMusicState();
        ObjectAnimator animator1 = initPlayendAnimator();
        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @NonNull
    private ObjectAnimator initPlayendAnimator() {
        ViewHelper.setPivotX(playNeedle, 98);
        ViewHelper.setPivotY(playNeedle, 57);

        ViewHelper.setPivotX(playNeedleShadow, 98);
        ViewHelper.setPivotY(playNeedleShadow, 57);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(playNeedleShadow, "rotation", 20, 0).setDuration(800);
        animator2.start();

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(playNeedle, "rotation", 20, 0).setDuration(800);
        animator1.setStartDelay(80);
        animator1.start();
        return animator1;
    }

    private void playingAnimator() {
        ObjectAnimator animator2 = initPlayingAnimator();
        animator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimation = false;
                changeMusicState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @NonNull
    private ObjectAnimator initPlayingAnimator() {
        ViewHelper.setPivotX(playNeedle, 98);
        ViewHelper.setPivotY(playNeedle, 57);
        ObjectAnimator.ofFloat(playNeedle, "rotation", 0, 20).setDuration(800).start();

        ViewHelper.setPivotX(playNeedleShadow, 98);
        ViewHelper.setPivotY(playNeedleShadow, 57);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(playNeedleShadow, "rotation", 0, 20).setDuration(800);
        animator2.setStartDelay(80);
        animator2.start();
        return animator2;
    }

    private void changeMusicState() {
        Intent intentPlaySong = new Intent();
        intentPlaySong.setAction(MediaService.BROADCAST_ACTION_MUSIC_PLAY);
        localBroadcastManager.sendBroadcast(intentPlaySong);
    }

    @OnClick({R.id.playSong, R.id.nextSong, R.id.lastSong})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.playSong:
                if (isAnimation) {
                    return;
                }

                changePlayNeedleState();
                break;
            case R.id.nextSong:
                if (isAnimation) {
                    return;
                }

                if (MediaService.getSize() == 0) {
                    return;
                }

                if (MediaService.mediaPlayer != null && MediaService.mediaPlayer.isPlaying()) {
                    Intent intentNextSong = new Intent();
                    intentNextSong.setAction(MediaService.BROADCAST_ACTION_MUSIC_NEXT);
                    localBroadcastManager.sendBroadcast(intentNextSong);
                } else {
                    ObjectAnimator animator2 = initPlayingAnimator();
                    animator2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimation = false;
                            Intent intentNextSong = new Intent();
                            intentNextSong.setAction(MediaService.BROADCAST_ACTION_MUSIC_NEXT);
                            localBroadcastManager.sendBroadcast(intentNextSong);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }

                break;
            case R.id.lastSong:
                if (isAnimation) {
                    return;
                }

                if (MediaService.getSize() == 0) {
                    return;
                }

                if (MediaService.mediaPlayer != null && MediaService.mediaPlayer.isPlaying()) {
                    Intent intentLastSong = new Intent();
                    intentLastSong.setAction(MediaService.BROADCAST_ACTION_MUSIC_LAST);
                    localBroadcastManager.sendBroadcast(intentLastSong);
                } else {
                    ObjectAnimator animator2 = initPlayingAnimator();
                    animator2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimation = false;
                            Intent intentLastSong = new Intent();
                            intentLastSong.setAction(MediaService.BROADCAST_ACTION_MUSIC_LAST);
                            localBroadcastManager.sendBroadcast(intentLastSong);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
                break;
        }
    }

    @OnLongClick({R.id.nextSong, R.id.lastSong})
    public boolean onViewLongClick(View v) {
        switch (v.getId()) {
            case R.id.nextSong:
                if (binder != null && binder.isPlaying()) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
                }
                break;
            case R.id.lastSong:
                if (binder != null && binder.isPlaying()) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
                }
                break;
        }

        return true;// ----返回true屏蔽onClick
    }

    @OnTouch({R.id.nextSong, R.id.lastSong})
    public boolean onViewTouchClick(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.nextSong:
                if (binder != null && event.getAction() == MotionEvent.ACTION_UP && binder.isPlaying()) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
                }
                break;
            case R.id.lastSong:
                if (binder != null && event.getAction() == MotionEvent.ACTION_UP && binder.isPlaying()) {
                    binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
                }
                break;
        }

        return false;
    }
}
