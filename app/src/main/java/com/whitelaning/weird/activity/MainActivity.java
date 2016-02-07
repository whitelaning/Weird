package com.whitelaning.weird.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.framework.android.activity.BaseActivity;
import com.framework.android.model.BaseEvent;
import com.whitelaning.weird.R;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.fragment.MainFragment;
import com.whitelaning.weird.fragment.album.AlbumFragment;
import com.whitelaning.weird.fragment.music.MusicFragment;
import com.whitelaning.weird.fragment.music.child.MusicAlbumFragment;
import com.whitelaning.weird.fragment.music.child.MusicFolderFragment;
import com.whitelaning.weird.fragment.music.child.MusicSingerFragment;
import com.whitelaning.weird.fragment.video.VideoFragment;
import com.whitelaning.weird.model.EventChangeToolBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.nav_view)
    NavigationView mNavView;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private int index; //当前侧滑栏处于的位置

    private MainFragment mMainFragment;
    private VideoFragment mVideoFragment;
    private AlbumFragment mAlbumFragment;
    private MusicFragment mMusicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initToolbar("Main", getResources().getColor(R.color.colorPrimary));
        initFragment(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void initData() {
        index = 0;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();

            mVideoFragment = VideoFragment.newInstance(null);
            mAlbumFragment = AlbumFragment.newInstance(null);
            mMainFragment = MainFragment.newInstance(null);
            mMusicFragment = MusicFragment.newInstance(null);

            fm.beginTransaction()
                    .add(R.id.id_fragment_container_main, mMainFragment, MainFragment.TAG)
                    .add(R.id.id_fragment_container_main, mVideoFragment, VideoFragment.TAG)
                    .add(R.id.id_fragment_container_main, mAlbumFragment, AlbumFragment.TAG)
                    .add(R.id.id_fragment_container_main, mMusicFragment, MusicFragment.TAG)
                    .show(mMainFragment)
                    .hide(mVideoFragment)
                    .hide(mAlbumFragment)
                    .hide(mMusicFragment)
                    .commit();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            mMainFragment = (MainFragment) fm.findFragmentByTag(MainFragment.TAG);
            mVideoFragment = (VideoFragment) fm.findFragmentByTag(VideoFragment.TAG);
            mAlbumFragment = (AlbumFragment) fm.findFragmentByTag(AlbumFragment.TAG);
            mMusicFragment = (MusicFragment) fm.findFragmentByTag(MusicFragment.TAG);

            if (mMainFragment == null) {
                mMainFragment = MainFragment.newInstance(null);
            }

            if (mVideoFragment == null) {
                mVideoFragment = VideoFragment.newInstance(null);
            }

            if (mAlbumFragment == null) {
                mAlbumFragment = AlbumFragment.newInstance(null);
            }

            if (mMusicFragment == null) {
                mMusicFragment = MusicFragment.newInstance(null);
            }

            fm.beginTransaction()
                    .show(mMainFragment)
                    .hide(mVideoFragment)
                    .hide(mAlbumFragment)
                    .hide(mMusicFragment)
                    .commit();
        }
    }

    private void initToolbar(String title, @ColorInt int color) {
        mToolbar.setTitle(title);
        mToolbar.setBackgroundColor(color);
        this.getWindow().setStatusBarColor(color);

        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!doFragmentBack()) {
                goToLauncher();
            }
        }
    }

    private void goToLauncher() {
        Intent startIntent = new Intent(Intent.ACTION_MAIN);
        startIntent.addCategory(Intent.CATEGORY_HOME);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
    }

    private boolean doFragmentBack() {
        switch (index) {
            case 0:
            case 1:
            case 2:
                VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentByTag(VideoFragment.TAG);
                if (!videoFragment.getIsFolder()) {
                    videoFragment.setIsFolder(true);
                    initToolbar("BlueVideo", getResources().getColor(R.color.colorBlue));
                    return true;
                }
            case 3:
                MusicFragment musicFragment = (MusicFragment) getSupportFragmentManager().findFragmentByTag(MusicFragment.TAG);
                switch (musicFragment.getIndex()) {
                    case 1:
                        MusicSingerFragment musicSingerFragment = (MusicSingerFragment) musicFragment.getFragment(1);
                        if (!musicSingerFragment.getIsFolder()) {
                            musicSingerFragment.setIsFolder(true);
                            initToolbar("RedMusic", getResources().getColor(R.color.colorRedDark));
                            return true;
                        }
                    case 2:
                        MusicAlbumFragment musicAlbumFragment = (MusicAlbumFragment) musicFragment.getFragment(2);
                        if (!musicAlbumFragment.getIsFolder()) {
                            musicAlbumFragment.setIsFolder(true);
                            initToolbar("RedMusic", getResources().getColor(R.color.colorRedDark));
                            return true;
                        }
                    case 3:
                        MusicFolderFragment musicFolderFragment = (MusicFolderFragment) musicFragment.getFragment(3);
                        if (!musicFolderFragment.getIsFolder()) {
                            musicFolderFragment.setIsFolder(true);
                            initToolbar("RedMusic", getResources().getColor(R.color.colorRedDark));
                            return true;
                        }
                }
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        showFragmentById(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragmentById(int id) {
        if (id == R.id.nav_main) {
            index = 0;
            getSupportFragmentManager().beginTransaction()
                    .show(mMainFragment)
                    .hide(mVideoFragment)
                    .hide(mAlbumFragment)
                    .hide(mMusicFragment)
                    .commit();
            initToolbar("Main", getResources().getColor(R.color.colorPrimary));
        } else if (id == R.id.nav_gallery) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mMainFragment)
                    .show(mAlbumFragment)
                    .hide(mVideoFragment)
                    .hide(mMusicFragment)
                    .commit();
            initToolbar("BlackAlbum", getResources().getColor(R.color.black));
            index = 1;
        } else if (id == R.id.nav_video) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mMainFragment)
                    .hide(mAlbumFragment)
                    .show(mVideoFragment)
                    .hide(mMusicFragment)
                    .commit();
            if (mVideoFragment.getIsFolder()) {
                initToolbar("BlueVideo", getResources().getColor(R.color.colorBlue));
            } else {
                initToolbarBack(mVideoFragment.getLastSelectFolderName(), getResources().getColor(R.color.colorBlue));
            }
            index = 2;
        } else if (id == R.id.nav_music) {
            getSupportFragmentManager().beginTransaction()
                    .hide(mMainFragment)
                    .hide(mAlbumFragment)
                    .hide(mVideoFragment)
                    .show(mMusicFragment)
                    .commit();
            initToolbar("RedMusic", getResources().getColor(R.color.colorRedDark));

            index = 3;
        } else if (id == R.id.nav_share) {
            index = 4;
        }
    }

    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent != null) {
            switch (baseEvent.getTAG()) {
                case EventCode.EVENT_CHANGE_TOOLBAR_FROM_MUSIC_FRAGMENT:
                    EventChangeToolBar item1 = (EventChangeToolBar) baseEvent;
                    if (item1.getType() == 0) {
                        showFragmentById(R.id.nav_music);
                    } else {
                        initToolbarBack(item1.getTitle(), getResources().getColor(item1.getColor()));
                    }
                    break;
                case EventCode.EVENT_CHANGE_TOOLBAR_FROM_VIDEO_FRAGMENT:
                    EventChangeToolBar item2 = (EventChangeToolBar) baseEvent;
                    initToolbarBack(item2.getTitle(), getResources().getColor(item2.getColor()));
                    break;
            }
        }
    }

    private void initToolbarBack(String title, @ColorInt int color) {
        mToolbar.setTitle(title);
        mToolbar.setBackgroundColor(color);
        this.getWindow().setStatusBarColor(color);
        setSupportActionBar(mToolbar);

        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFragmentBack();
            }
        });
    }
}
