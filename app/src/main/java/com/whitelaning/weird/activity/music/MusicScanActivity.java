package com.whitelaning.weird.activity.music;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.android.activity.BaseActivity;
import com.framework.android.application.FrameworkApplication;
import com.framework.android.model.BaseEvent;
import com.framework.android.tool.ToastUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.whitelaning.weird.R;
import com.whitelaning.weird.animator.AnimatorPath;
import com.whitelaning.weird.animator.PathEvaluator;
import com.whitelaning.weird.animator.PathPoint;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.music.ModelAlbumInfo;
import com.whitelaning.weird.model.music.ModelArtistInfo;
import com.whitelaning.weird.model.music.ModelFolderInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.tool.music.MusicUtils;

import org.litepal.crud.DataSupport;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MusicScanActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;
    @Bind(R.id.scanIcon)
    ImageView scanIcon;
    @Bind(R.id.scan)
    TextView scan;

    private ObjectAnimator scanMusicAnimator;
    private AnimatorProxy mButtonProxy;
    private PathEvaluator mEvaluator = new PathEvaluator();
    private boolean isScaned = false;

    public static void startActivityForResult(Context mContext, int requestCode) {
        Intent intent = new Intent(mContext, MusicScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ((Activity) mContext).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_scan);
        ButterKnife.bind(this);
        initToolbar("Scan Media", getResources().getColor(R.color.colorRedDark));
        initScanMusicAnimator();
    }

    private void initScanMusicAnimator() {
        mButtonProxy = AnimatorProxy.wrap(scanIcon);
        LinearInterpolator lin = new LinearInterpolator();
        // Set up the path we're animating along
        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        path.lineTo(-50, 0);
        path.lineTo(-50, 100);
        path.lineTo(50, 100);
        path.lineTo(50, 0);
        path.lineTo(0, 0);

        // Set up the animation
        scanMusicAnimator = ObjectAnimator.ofObject(this, "buttonLoc",
                new PathEvaluator(), path.getPoints().toArray());
        scanMusicAnimator.setDuration(2000);
        scanMusicAnimator.setRepeatCount(-1);
        scanMusicAnimator.setRepeatMode(ValueAnimator.INFINITE);
        scanMusicAnimator.setInterpolator(lin);
    }

    private void initToolbar(String title, @ColorInt int color) {
        mToolbar.setTitle(title);
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

    @OnClick({R.id.scan})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                if (isScaned) {
                    setResult(1001);
                    finish();
                } else {
                    if (!scanMusicAnimator.isRunning()) {
                        isScaned = false;
                        scan.setText("Scanning...");
                        scanMusicAnimator.start();
                        scanMusicAction();
                    }
                }
                break;
        }
    }

    private void scanMusicAction() {
        new ScanTask().execute();
    }

    /**
     * 执行扫描任务
     *
     * @author Administrator
     */
    private class ScanTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                DataSupport.deleteAll(ModelMusicInfo.class);
                DataSupport.deleteAll(ModelArtistInfo.class);
                DataSupport.deleteAll(ModelAlbumInfo.class);
                DataSupport.deleteAll(ModelFolderInfo.class);

                MusicUtils.queryMusic(FrameworkApplication.getContext(), IConstants.START_FROM_LOCAL);
                MusicUtils.queryArtist(FrameworkApplication.getContext());
                MusicUtils.queryAlbums(FrameworkApplication.getContext());
                MusicUtils.queryFolder(FrameworkApplication.getContext());

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            scanMusicAnimator.cancel();

            isScaned = result;

            if (result) {
                ToastUtils.show("Scanning is finished");
                scan.setText("Scaned");

                Intent intent = new Intent();
                intent.setAction(IConstants.MUSIC_SCANNED_INFORMATION);
                LocalBroadcastManager.getInstance(MusicScanActivity.this).sendBroadcast(intent);

                BaseEvent object = new BaseEvent(EventCode.EVENT_MUSIC_SCANNED_INFORMATION);
                EventBus.getDefault().post(object);

            } else {
                ToastUtils.show("Something was error");
                scan.setText("Complete Scan");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (scanMusicAnimator.isRunning()) {// ----扫描中暂不可退出
                return true;
            } else {
                if (isScaned) {
                    setResult(1001);
                    finish();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * We need this setter to translate between the information the animator
     * produces (a new "PathPoint" describing the current animated location)
     * and the information that the button requires (an xy location). The
     * setter will be called by the ObjectAnimator given the 'buttonLoc'
     * property string.
     */
    public void setButtonLoc(PathPoint newLoc) {
        mButtonProxy.setTranslationX(newLoc.mX);
        mButtonProxy.setTranslationY(newLoc.mY);
    }
}
