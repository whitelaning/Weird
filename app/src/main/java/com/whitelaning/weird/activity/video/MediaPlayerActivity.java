package com.whitelaning.weird.activity.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whitelaning.weird.R;
import com.whitelaning.weird.console.ErrorCode;
import com.framework.android.activity.BaseActivity;
import com.framework.android.application.FrameworkApplication;
import com.framework.android.tool.ScreenUtils;
import com.framework.android.tool.StringUtils;
import com.framework.android.tool.TimeUtils;
import com.framework.android.tool.ToastUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MediaPlayerActivity extends BaseActivity {

    @Bind(R.id.mVideoView)
    VideoView mVideoView;
    @Bind(R.id.mOperationBg)
    ImageView mOperationBg;
    @Bind(R.id.mOperationFull)
    ImageView mOperationFull;
    @Bind(R.id.mOperationPercent)
    ImageView mOperationPercent;
    @Bind(R.id.mVolumeBrightnessLayout)
    FrameLayout mVolumeBrightnessLayout;
    @Bind(R.id.mFLProgressBg)
    ImageView mFLProgressBg;
    @Bind(R.id.mFLProgress)
    TextView mFLProgress;
    @Bind(R.id.mFLProgressLayoutRoot)
    FrameLayout mFLProgressLayoutRoot;
    @Bind(R.id.mVideoLoadingLayoutRoot)
    FrameLayout mVideoLoadingLayoutRoot;
    @Bind(R.id.mBackView)
    ImageView mBackView;
    @Bind(R.id.mMore)
    ImageView mMore;
    @Bind(R.id.mTitle)
    TextView mTitle;
    @Bind(R.id.mLockView)
    ImageView mLockView;
    @Bind(R.id.mPreviousView)
    ImageView mPreviousView;
    @Bind(R.id.mPlayView)
    ImageView mPlayView;
    @Bind(R.id.mNextView)
    ImageView mNextView;
    @Bind(R.id.mTypeView)
    ImageView mTypeView;
    @Bind(R.id.mMediaControllerRoot)
    RelativeLayout mMediaControllerRoot;
    @Bind(R.id.mLockView2)
    ImageView mLockView2;
    @Bind(R.id.mSeekBar)
    DiscreteSeekBar mSeekBar;
    @Bind(R.id.mVideoTime)
    TextView mVideoTime;
    @Bind(R.id.mCurrentTime)
    TextView mVideoCurrentTime;
    //--------------------------------------------------
    private String mVideoPath;//播放地址
    private String videoTitle;

    private GestureDetector mGestureDetector;//手势
    private AudioManager mAudioManager;//音频管理类

    private int mMaxVolume;//可以达到的最大声音
    private int mVolume = -1;//当前声音

    private float mBrightness = -1f;//当前年度
    private long mDuration;//视频的长度

    private int mVideoHeight;//视频的高度
    private int mVideoWidth;//视频的宽度

    private MediaController mMediaController;//视频控制器

    private boolean mIsLockScreen = false;//锁住屏幕标示
    private boolean mIsFinished = false;
    private boolean mIsStartTrackingTouch = false;
    private boolean needResume = true;//用于自动暂停，恢复播放
    private int mVideoType = 2;//适配缩放参数
    private int mProgressValue = 0;//当前进度条的进度，最高为100

    public static void startActivityForResult(Context mContext, int requestCode, String videoPath, String videoTitle) {
        Intent intent = new Intent(mContext, MediaPlayerActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        ((Activity) mContext).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVitamio();
        setContentView(R.layout.activity_media_player);
        ButterKnife.bind(this);
        initData();
        initVideoViewAndMediaController();
        initIntent();
        startPlay();

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
                    mVideoCurrentTime.setText(TimeUtils.secToTime(mProgressValue * mDuration / 100));
                    mSeekBar.setIndicatorFormatter(TimeUtils.secToTime(mProgressValue * mDuration / 100));
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                mIsStartTrackingTouch = true;
                mSeekBar.setIndicatorFormatter(TimeUtils.secToTime(mVideoView.getCurrentPosition()));
                mSeekBar.setProgress((int) (mVideoView.getCurrentPosition() * 100 / mDuration));
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                mIsStartTrackingTouch = false;
                if (mVideoView.isPlaying()) {
                    mVideoView.seekTo(mSeekBar.getProgress() * mDuration / 100);
                    disMediaController();
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private boolean initIntent() {

        Intent mIntent = getIntent();
        if (mIntent != null) {
            videoTitle = mIntent.getStringExtra("videoTitle");
            mVideoPath = mIntent.getStringExtra("videoPath");
            if (TextUtils.isEmpty(mVideoPath)) {
                mVideoPath = mIntent.getDataString();
                videoTitle = mVideoPath;
                if (TextUtils.isEmpty(mVideoPath)) {
                    ToastUtils.show("your media file URL/path is empty or error");
                    setResult(ErrorCode.ErrorVedioUrlPath);
                    finish();
                    return false;
                }
            }
        } else {
            ToastUtils.show("lost of intent");
            setResult(ErrorCode.ErrorVedioUrlPath);
            finish();
            return false;
        }

        mTitle.setText(videoTitle);

        return true;
    }

    private void initData() {
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initVideoViewAndMediaController() {

        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//高质量视频
        mVideoView.setVideoLayout(mVideoType, 0);//自适应屏幕,拉伸
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg) {
                /**
                 * 在视频预处理完成后调用。在视频预处理完成后被调用。
                 * 此时视频的宽度、高度、宽高比信息已经获取到.
                 * 此时可调用seekTo让视频从指定位置开始播放。
                 *
                 * @param mMediaPlayer
                 */

                mVideoHeight = arg.getVideoHeight();
                mVideoWidth = arg.getVideoWidth();
                mDuration = arg.getDuration();
                mVideoCurrentTime.setText(TimeUtils.secToTime(arg.getCurrentPosition()));
                mVideoTime.setText(TimeUtils.secToTime(mDuration));
                showMediaController();
                // optional need Vitamio 4.0
                arg.setPlaybackSpeed(1.0f);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mIsLockScreen = false;
                mIsFinished = true;
                mLockView2.setVisibility(View.GONE);
                mPlayView.setImageResource(R.drawable.ic_play_arrow_white_20160109);
                showMediaController();
            }
        });

        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        if (isPlaying()) {
                            stopPlayer();
                            needResume = true;
                        }
                        mVideoLoadingLayoutRoot.setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        // 缓存完成，继续播放
                        if (needResume) {
                            startPlayer();
                            needResume = false;
                        }
                        mVideoLoadingLayoutRoot.setVisibility(View.GONE);
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        // 显示下载速度
                        break;
                }

                return true;
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                /**
                 * 在异步操作调用过程中发生错误时调用。例如视频打开失败。
                 */
                ToastUtils.show("播放失败");
                initializeSomething();

                Intent intent = new Intent();
                intent.putExtra("path", mVideoPath);
                setResult(ErrorCode.ErrorPlayVedio, intent);
                finish();
                return true;
            }
        });


        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                /**
                 * 在网络视频流缓冲变化时调用。
                 */
                mVideoLoadingLayoutRoot.setVisibility(View.VISIBLE);
            }
        });

        mVideoView.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                /**
                 * 在seek操作完成后调用。
                 */
            }
        });

    }

    private void startPlay() {
        if (TextUtils.isEmpty(mVideoPath)) {
            ToastUtils.show("your media file URL/path is empty or error");
        } else {
            //设置地址，开始播放
            mVideoView.setVideoPath(mVideoPath);
            showMediaController();
        }
    }

    private boolean initVitamio() {
        if (Vitamio.isInitialized(FrameworkApplication.getContext())) {
            return true;
        } else {
            finish();
            return false;
        }
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (mIsLockScreen) {
                return true;
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (mIsLockScreen) {
                return;
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mMediaControllerRoot.getVisibility() == View.VISIBLE) {
                disMediaController();
            } else {
                showMediaController();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mIsLockScreen) {
                return true;
            }

            float mOldX = e1.getX();
            float mOldY = e1.getY();
            float x = e2.getRawX();
            float y = e2.getRawY();

            if (Math.abs(distanceY) / Math.abs(distanceX) > 4 && !isFastForward) {
                //上下滑动
                if (mOldX > ScreenUtils.getWidth() * 2 / 3) {
                    //右边滑动，调节声音
                    onVolumeSlide((mOldY - y) / ScreenUtils.getHeight());
                } else if (mOldX < ScreenUtils.getWidth() / 3) {
                    //左边滑动，调节亮度
                    onBrightnessSlide((mOldY - y) / ScreenUtils.getHeight());
                }
            } else if (Math.abs(distanceX) / Math.abs(distanceY) > 4 && !isUpDownScroll) {
                if (mVideoView.isPlaying()) {
                    //左右滑动
                    doFastForWard(x - mOldX);
                }
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mIsLockScreen) {
                return;
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mIsLockScreen) {
                return true;
            }

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mIsLockScreen) {
                return true;
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mIsLockScreen) {
                return true;
            }

            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                showMediaController();
            } else {
                mVideoView.start();
                mIsFinished = false;
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    private void showMediaController() {
        mHandler.removeMessages(1);
        Message msg = Message.obtain();
        msg.what = 2;
        mHandler.sendMessage(msg);
    }

    private void disMediaController() {
        mHandler.removeMessages(2);
        Message msg = Message.obtain();
        msg.what = 1;
        mHandler.sendMessageDelayed(msg, 4000);
    }

    private long currentProgress;

    private void doFastForWard(float mFastForward) {
        isFastForward = true;

        if (mVideoView.getCurrentPosition() + 100 * (long) (mFastForward) < 0) {
            currentProgress = 0;
        } else if (mVideoView.getCurrentPosition() + 100 * (long) (mFastForward) > mDuration) {
            currentProgress = mDuration;
        } else {
            currentProgress = mVideoView.getCurrentPosition() + 100 * (long) (mFastForward);
        }

        mFLProgress.setText(String.format("%s / %s", StringUtils.generateTime(currentProgress), StringUtils.generateTime(mDuration)));

        if (mFastForward > 0) {
            mFLProgressBg.setImageResource(R.mipmap.btn_fast_forward);
        } else {
            mFLProgressBg.setImageResource(R.mipmap.btn_back_forward);
        }

        mFLProgressLayoutRoot.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP: {
                endGesture();
                break;
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        if (isFastForward) {
            onSeekProgress(currentProgress);
        }

        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 800);
    }

    private void onSeekProgress(long dis) {
        String time = TimeUtils.secToTime(dis);
        mVideoCurrentTime.setText(time);
        mSeekBar.setIndicatorFormatter(time);
        mSeekBar.setProgress((int) (dis * 100 / mDuration));

        mVideoView.seekTo(dis);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        isUpDownScroll = true;
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0) {
                mVolume = 0;
            }

            // 显示音频调节图标
            mOperationBg.setImageResource(R.mipmap.video_volume_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = mOperationFull.getLayoutParams().width * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        isUpDownScroll = true;
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            }
            if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }

            // 显示亮度调节图标
            mOperationBg.setImageResource(R.mipmap.video_brightness_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        getWindow().setAttributes(lpa);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int) (mOperationFull.getLayoutParams().width * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<MediaPlayerActivity> mActivity;

        MyHandler(MediaPlayerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPlayerActivity activity = mActivity.get();

            switch (msg.what) {
                case 1://隐藏控制栏
                    hideControlBar(activity);
                    break;
                case 2://显示控制栏
                    showControlBar(activity);
                    break;
                case 3://更新时间
                    setTime(activity);
                    break;
                default:
                    activity.initializeSomething();
                    break;
            }
        }

        private void setTime(MediaPlayerActivity activity) {
            if (activity.mMediaControllerRoot.getVisibility() == View.VISIBLE) {
                String time = TimeUtils.secToTime(activity.mVideoView.getCurrentPosition());
                activity.mVideoCurrentTime.setText(time);
                activity.mSeekBar.setIndicatorFormatter(time);
                activity.mSeekBar.setProgress(activity.mSeekBar.getProgress() + 1);
                if (activity.mVideoView.isPlaying()) {
                    activity.refreshCurrentTime();
                }
            }
        }

        private void showControlBar(MediaPlayerActivity activity) {
            if (activity.mVideoView.isPlaying()) {
                activity.mVideoCurrentTime.setText(TimeUtils.secToTime(activity.mVideoView.getCurrentPosition()));
                activity.mSeekBar.setProgress((int) (activity.mVideoView.getCurrentPosition() * 100 / activity.mDuration));
                activity.refreshCurrentTime();
            }

            if (activity.mIsLockScreen) {
                activity.mLockView2.setVisibility(View.VISIBLE);
                activity.mMediaControllerRoot.setVisibility(View.GONE);
            } else {
                activity.mLockView2.setVisibility(View.GONE);
                activity.mMediaControllerRoot.setVisibility(View.VISIBLE);
            }

            if (!activity.mIsFinished) {
                activity.disMediaController();
            }
        }

        private void hideControlBar(MediaPlayerActivity activity) {
            if (!activity.mIsStartTrackingTouch) {
                activity.mMediaControllerRoot.setVisibility(View.GONE);
                activity.mLockView2.setVisibility(View.GONE);
                activity.mHandler.removeMessages(1);
                activity.mHandler.removeMessages(2);
                activity.mHandler.removeMessages(3);
            }
        }
    }

    private void refreshCurrentTime() {
        mHandler.removeMessages(3);
        Message msg = Message.obtain();
        msg.what = 3;
        mHandler.sendMessageDelayed(msg, 1000);
    }

    private boolean isFastForward = false;
    private boolean isUpDownScroll = false;

    private void initializeSomething() {
        isFastForward = false;
        isUpDownScroll = false;
        mVolumeBrightnessLayout.setVisibility(View.GONE);
        mFLProgressLayoutRoot.setVisibility(View.GONE);
    }

    private void stopPlayer() {
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    private void startPlayer() {
        if (mVideoView != null) {
            mVideoView.start();
            isFastForward = false;
        }
    }

    private boolean isPlaying() {
        return mVideoView != null && mVideoView.isPlaying();
    }

    @OnClick({R.id.mBackView, R.id.mMore, R.id.mLockView, R.id.mPreviousView,
            R.id.mNextView, R.id.mTypeView, R.id.mPlayView, R.id.mLockView2})
    public void onViewClick(View v) {

        switch (v.getId()) {
            case R.id.mBackView:
                finish();
                break;
            case R.id.mMore:
//                ToastUtils.show("更多");
                break;
            case R.id.mLockView:
                mIsLockScreen = true;
                break;
            case R.id.mLockView2:
                mIsLockScreen = false;
                break;
            case R.id.mPreviousView:
//                ToastUtils.show("上一首");
                break;
            case R.id.mNextView:
//                ToastUtils.show("下一首");
                break;
            case R.id.mTypeView:
                if (mVideoType + 1 > 4) {
                    mVideoType = 0;
                } else {
                    mVideoType = mVideoType + 1;
                }
                mVideoView.setVideoLayout(mVideoType, 0);
                mVideoView.requestFocus();
                break;
            case R.id.mPlayView:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    mPlayView.setImageResource(R.drawable.ic_play_arrow_white_20160109);
                    mSeekBar.setEnabled(false);
                } else {
                    mVideoView.start();
                    mIsFinished = false;
                    mPlayView.setImageResource(R.drawable.ic_pause_white_20150109);
                    mSeekBar.setEnabled(true);
                }
                break;
        }

        showMediaController();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsFinished = false;
    }

}

