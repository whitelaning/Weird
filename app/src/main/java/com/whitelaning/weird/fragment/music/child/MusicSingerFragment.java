package com.whitelaning.weird.fragment.music.child;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.framework.android.application.FrameworkApplication;
import com.framework.android.fragment.BaseFragment;
import com.framework.android.model.BaseEvent;
import com.framework.android.view.ProgressLayout;
import com.whitelaning.weird.R;
import com.whitelaning.weird.adapter.music.MusicSingerFragmentAdapter;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.EventChangeToolBar;
import com.whitelaning.weird.model.music.EventNowPlayMusicInformation;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.modelFetch.music.MusicSingerFragmentModelFetch;
import com.whitelaning.weird.service.music.MediaService;
import com.whitelaning.weird.tool.music.MusicUtils;
import com.whitelaning.weird.tool.music.SweetSheetUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Zack White on 1/28/2016.
 */
public class MusicSingerFragment extends BaseFragment {

    public final static String TAG = "MusicSingerFragment.TAG";
    @Bind(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;

    private Context mContext;
    private MusicSingerFragmentModelFetch mModelFetch;
    private MusicSingerFragmentAdapter mAdapter;
    private SweetSheetUtils mSweetSheetUtils;

    private boolean isFolder = true;
    private String lastSelectArtistName;

    public MusicSingerFragment() {
        // Required empty public constructor
    }

    public static MusicSingerFragment newInstance(Bundle args) {
        MusicSingerFragment fragment = new MusicSingerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 获取Activity传递过来的数据
        }
        EventBus.getDefault().register(this);
    }

    private void initModelFetch() {
        mModelFetch = new MusicSingerFragmentModelFetch(mContext, mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_singer, container, false);
        ButterKnife.bind(this, view);
        initData();
        initModelFetch();
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mModelFetch.getData();
    }

    private void initData() {
        mContext = getActivity();
        mSweetSheetUtils = new SweetSheetUtils();
    }

    private void initView() {
        mProgressLayout.showProgress();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<MusicSingerFragment> mFragment;

        MyHandler(MusicSingerFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicSingerFragment fragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    fragment.mProgressLayout.showEmpty();
                    break;
                case 1:
                    fragment.setAdapter();
                    break;
                case 3://点击歌手文件夹，进入歌手的歌单列表
                    String artistName = fragment.mModelFetch.list.get(msg.arg1).getArtistName();
                    List<ModelMusicInfo> musicList = MusicUtils.queryMusic(fragment.mContext, IConstants.START_FROM_ARTIST, artistName);

                    fragment.isFolder = false;
                    fragment.mAdapter.setSongList(musicList);
                    fragment.mAdapter.setIsFolder(false);
                    fragment.mAdapter.notifyDataSetChanged();

                    fragment.lastSelectArtistName = artistName;

                    EventChangeToolBar object = new EventChangeToolBar(EventCode.EVENT_CHANGE_TOOLBAR_FROM_MUSIC_FRAGMENT);
                    object.setTitle(artistName);
                    object.setColor(R.color.colorRedDark);
                    object.setType(1);
                    EventBus.getDefault().post(object);
                    break;
                case 4://显示歌曲信息
                    fragment.mSweetSheetUtils.showMusicInformation(FrameworkApplication.getContext(), fragment.mAdapter.getSongList().get(msg.arg1), fragment.mProgressLayout);
                    break;
                case 5://播放歌曲
                    //---启动服务,，播放音乐
                    ModelMusicInfo item = fragment.mAdapter.getSongList().get(msg.arg1);

                    Intent intent = new Intent(FrameworkApplication.getContext(), MediaService.class);

                    Bundle bundle = new Bundle();
                    bundle.putInt("position", msg.arg1);
                    bundle.putInt("type", 1);
                    bundle.putString("select", item.getArtist());

                    intent.putExtra("data", bundle);
                    FrameworkApplication.getContext().startService(intent);

                case 7007://test code
                    break;
            }
        }
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.addItemDecoration(
                    new HorizontalDividerItemDecoration.Builder(mContext)
                            .color(0xffd9dbdc)
                            .size(1)
                            .margin(170, 0)
                            .build());
            mAdapter = new MusicSingerFragmentAdapter(mContext, mModelFetch.list, mHandler);
            isFolder = true;
            mAdapter.setIsFolder(true);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mProgressLayout.showContent();
    }

    public boolean getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
        mAdapter.setIsFolder(true);
        mAdapter.notifyDataSetChanged();
    }

    public String getLastSelectArtistName() {
        return lastSelectArtistName;
    }

    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent != null) {
            switch (baseEvent.getTAG()) {
                case EventCode.EVENT_MUSIC_PLAY_MUSIC_INFORMATION:
                    EventNowPlayMusicInformation item = (EventNowPlayMusicInformation) baseEvent;
                    mAdapter.setNowPlayingSongId(item.getSongId());
                    mAdapter.setArtist(item.getArtist());
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
