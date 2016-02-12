package com.whitelaning.weird.activity.music;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.android.activity.BaseActivity;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.whitelaning.weird.R;
import com.whitelaning.weird.animator.AnimatorPath;
import com.whitelaning.weird.animator.PathEvaluator;
import com.whitelaning.weird.animator.PathPoint;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
                scanMusicAnimator.start();
                break;
        }
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
