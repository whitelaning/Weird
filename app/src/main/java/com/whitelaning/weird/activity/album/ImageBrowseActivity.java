package com.whitelaning.weird.activity.album;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.framework.android.activity.BaseActivity;
import com.whitelaning.weird.R;
import com.whitelaning.weird.adapter.album.ImagePagerAdapter;
import com.whitelaning.weird.view.HackyViewPager;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageBrowseActivity extends BaseActivity {

    @Bind(R.id.image_vp)
    HackyViewPager mViewPager;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * 图片列表
     */
    public static final String EXTRA_IMAGES = "extra_images";

    /**
     * 位置
     */
    public static final String EXTRA_INDEX = "extra_index";

    /**
     * 图片列表数据源
     */
    private ArrayList<String> mDatas = new ArrayList<>();

    /**
     * 进入到该界面时的索引
     */
    private int mPageIndex = 0;

    /**
     * 图片适配器
     */
    private ImagePagerAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browse);
        ButterKnife.bind(this);

        initToolbar("", getResources().getColor(R.color.black));

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_IMAGES)) {
            mDatas = intent.getStringArrayListExtra(EXTRA_IMAGES);
            mPageIndex = intent.getIntExtra(EXTRA_INDEX, 0);
            mImageAdapter = new ImagePagerAdapter(this, mDatas);
            mViewPager.setAdapter(mImageAdapter);
            mViewPager.setCurrentItem(mPageIndex);
        } else {
            finish();
        }
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
}
