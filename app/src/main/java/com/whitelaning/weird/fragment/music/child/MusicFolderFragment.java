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
import com.whitelaning.weird.adapter.music.MusicFolderFragmentAdapter;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.EventChangeToolBar;
import com.whitelaning.weird.model.music.EventNowPlayMusicInformation;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.modelFetch.music.MusicFolderFragmentModelFetch;
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
public class MusicFolderFragment extends BaseFragment {

    public final static String TAG = "MusicAlbumFragment.TAG";
    @Bind(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;

    private Context mContext;
    private MusicFolderFragmentModelFetch mModelFetch;
    private MusicFolderFragmentAdapter mAdapter;
    private SweetSheetUtils mSweetSheetUtils;

    private boolean isFolder;
    private String lastSelectFolderName;

    public MusicFolderFragment() {
        // Required empty public constructor
    }

    public static MusicFolderFragment newInstance(Bundle args) {
        MusicFolderFragment fragment = new MusicFolderFragment();
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
        mModelFetch = new MusicFolderFragmentModelFetch(mContext, mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_folder, container, false);
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
        WeakReference<MusicFolderFragment> mFragment;

        MyHandler(MusicFolderFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicFolderFragment fragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    fragment.mProgressLayout.showEmpty();
                    break;
                case 1:
                    fragment.setAdapter();
                    break;
                case 2://点击文件夹，进入文件夹的歌单列表
                    String folderPath = fragment.mModelFetch.list.get(msg.arg1).getFolderPath();
                    String folderName = fragment.mModelFetch.list.get(msg.arg1).getFolderName();

                    List<ModelMusicInfo> musicList = MusicUtils.queryMusic(fragment.mContext, IConstants.START_FROM_FOLDER, folderPath);
                    fragment.isFolder = false;
                    fragment.mAdapter.setSongList(musicList);
                    fragment.mAdapter.setIsFolder(false);
                    fragment.mAdapter.notifyDataSetChanged();

                    fragment.lastSelectFolderName = folderName;

                    EventChangeToolBar object = new EventChangeToolBar(EventCode.EVENT_CHANGE_TOOLBAR_FROM_MUSIC_FRAGMENT);
                    object.setTitle(folderName);
                    object.setColor(R.color.colorRedDark);
                    object.setType(1);
                    EventBus.getDefault().post(object);
                    break;
                case 3://显示歌曲信息
                    fragment.mSweetSheetUtils.showMusicInformation(FrameworkApplication.getContext(), fragment.mAdapter.getSongList().get(msg.arg1), fragment.mProgressLayout);
                    break;
                case 4:
                    //---启动服务,，播放音乐
                    ModelMusicInfo item = fragment.mAdapter.getSongList().get(msg.arg1);

                    Intent intent = new Intent(FrameworkApplication.getContext(), MediaService.class);

                    Bundle bundle = new Bundle();
                    bundle.putInt("position", msg.arg1);
                    bundle.putInt("type", 3);
                    bundle.putString("select", item.getFolder());

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
                            .margin(200, 0)
                            .build());
            mAdapter = new MusicFolderFragmentAdapter(mContext, mModelFetch.list, mHandler);
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

    public String getLastSelectFolderName() {
        return lastSelectFolderName;
    }

    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent != null) {
            switch (baseEvent.getTAG()) {
                case EventCode.EVENT_MUSIC_PLAY_MUSIC_INFORMATION:
                    EventNowPlayMusicInformation item = (EventNowPlayMusicInformation) baseEvent;
                    mAdapter.setNowPlayingSongId(item.getSongId());
                    mAdapter.setPath(item.getPath());
                    mAdapter.notifyDataSetChanged();
                    break;
                case EventCode.EVENT_MUSIC_SCANNED_INFORMATION:
                    mProgressLayout.showProgress();
                    mModelFetch.getData();
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
