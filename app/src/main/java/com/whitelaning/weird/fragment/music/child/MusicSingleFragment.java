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
import com.whitelaning.weird.adapter.music.MusicSingleFragmentAdapter;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.model.music.EventNowPlayMusicInformation;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.whitelaning.weird.modelFetch.music.MusicSingleFragmentModelFetch;
import com.whitelaning.weird.service.music.MediaService;
import com.whitelaning.weird.tool.music.SweetSheetUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Zack White on 1/28/2016.
 */
public class MusicSingleFragment extends BaseFragment {

    public final static String TAG = "MusicSingleFragment.TAG";
    @Bind(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;

    private Context mContext;
    private MusicSingleFragmentModelFetch mModelFetch;
    private MusicSingleFragmentAdapter mAdapter;
    private SweetSheetUtils mSweetSheetUtils;

    public MusicSingleFragment() {
        // Required empty public constructor
    }

    public static MusicSingleFragment newInstance(Bundle args) {
        MusicSingleFragment fragment = new MusicSingleFragment();
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
        mModelFetch = new MusicSingleFragmentModelFetch(mContext, mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_single, container, false);
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
        WeakReference<MusicSingleFragment> mFragment;

        MyHandler(MusicSingleFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicSingleFragment fragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    fragment.mProgressLayout.showEmpty();
                    break;
                case 1:
                    fragment.setAdapter();
                    break;
                case 2:
                    fragment.mSweetSheetUtils.showMusicInformation(FrameworkApplication.getContext(), fragment.mModelFetch.list.get(msg.arg1), fragment.mProgressLayout);
                    break;
                case 3://play song
                    ModelMusicInfo item = fragment.mAdapter.getSingleList().get(msg.arg1);

                    Intent intent = new Intent(FrameworkApplication.getContext(), MediaService.class);

                    Bundle bundle = new Bundle();
                    bundle.putInt("position", msg.arg1);
                    bundle.putInt("type", 0);
                    bundle.putString("select", "single");

                    intent.putExtra("data", bundle);
                    FrameworkApplication.getContext().startService(intent);

                    break;
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
                            .margin(30, 0)
                            .build());
            mAdapter = new MusicSingleFragmentAdapter(mContext, mModelFetch.list, mHandler);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        mProgressLayout.showContent();
    }

    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent != null) {
            switch (baseEvent.getTAG()) {
                case EventCode.EVENT_MUSIC_PLAY_MUSIC_INFORMATION:
                    EventNowPlayMusicInformation item = (EventNowPlayMusicInformation) baseEvent;
                    mAdapter.setNowPlayingSongId(item.getSongId());
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
