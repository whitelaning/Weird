package com.whitelaning.weird.fragment.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.framework.android.fragment.BaseFragment;
import com.framework.android.tool.FolderUtils;
import com.framework.android.tool.MediaScanner;
import com.framework.android.tool.TimeUtils;
import com.framework.android.tool.ToastUtils;
import com.framework.android.tool.logger.FileUtils;
import com.framework.android.view.ProgressLayout;
import com.github.mrengineer13.snackbar.SnackBar;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.other.circlerefresh.CircleRefreshLayout;
import com.other.swipe.util.Attributes;
import com.whitelaning.weird.R;
import com.whitelaning.weird.activity.video.MediaPlayerActivity;
import com.whitelaning.weird.adapter.video.VideoFragmentAdapter;
import com.whitelaning.weird.console.ErrorCode;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.model.EventChangeToolBar;
import com.whitelaning.weird.model.video.ModelVideoInfor;
import com.whitelaning.weird.modelFetch.video.VideoFragmentModelFetch;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class VideoFragment extends BaseFragment {
    public final static String TAG = "VideoFragment.TAG";
    @Bind(R.id.mListView)
    RecyclerView mRecyclerView;
    @Bind(R.id.mRefreshLayout)
    CircleRefreshLayout mRefreshLayout;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;

    private int playIndex = -1;//用于记录点击播放的视频是第几个
    private Context mContext;

    private VideoFragmentAdapter mAdapter;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private VideoFragmentModelFetch mModelFetch;
    private boolean isFolder = true;
    private String lastSelectFolderName;

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance(Bundle args) {
        VideoFragment fragment = new VideoFragment();
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
        initModelFetch();
    }

    private void initModelFetch() {
        mModelFetch = new VideoFragmentModelFetch(mContext, mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);
        initData();
        initView();
        initListener();
        initBroadcast();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mModelFetch.toFindVideoByLitePal();
    }

    private void initBroadcast() {
        registerLocalBroadcastReceiver();
    }

    private void initData() {
        mContext = getActivity();
    }

    private void initView() {
        mProgressLayout.showProgress();
    }

    /**
     * 注册本地广播接收者
     */
    private void registerLocalBroadcastReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MediaScanner.FINISHED_SCANNING);
        mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalBroadcastReceiver, mIntentFilter);
    }

    /**
     * 取消本地广播的注册
     */
    private void unRegisterLocalBroadcastReceiver() {
        if (mLocalBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocalBroadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterLocalBroadcastReceiver();
    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MediaScanner.FINISHED_SCANNING)) {
                mModelFetch.toFindVideoByLitePal();
            }
        }
    }

    private void initListener() {
        mRefreshLayout.setOnRefreshListener(new CircleRefreshLayout.OnCircleRefreshListener() {
            @Override
            public void completeRefresh() {
                ToastUtils.show("Refresh success");
            }

            @Override
            public void refreshing() {
                ToastUtils.show("Loading");
                mModelFetch.startMediaScanner();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<VideoFragment> mFragment;

        MyHandler(VideoFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoFragment fragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    if (fragment.mRefreshLayout.isRefreshing()) {
                        fragment.mRefreshLayout.finishRefreshing();
                    }
                    fragment.setAdapter();
                    break;
                case 1://显示information
                    fragment.showInformation(fragment.mAdapter.getVideoList().get(msg.arg1));
                    break;
                case 2://显示detelet
                    if (fragment.mDeleteSnackBarIsShow) {
                        ToastUtils.show("Please handle the last operation");
                    } else {
                        fragment.deleteVideoInforModel = fragment.mModelFetch.remove(msg.arg1);
                        fragment.deletePosition = msg.arg1;
                        fragment.showDelete();
                        fragment.mAdapter.notifyItemRemoved(msg.arg1);
                    }
                    break;
                case 3://播放视频
                    fragment.playIndex = msg.arg1;
                    MediaPlayerActivity.startActivityForResult(fragment.mContext,
                            1000,
                            fragment.mAdapter.getVideoList().get(msg.arg1).getPath(),
                            fragment.mAdapter.getVideoList().get(msg.arg1).getTitle());
                    break;
                case 4://第一次进入数据库中没有数据，启动扫描线程
                    fragment.mModelFetch.startMediaScanner();
                    break;
                case 5://扫描SD卡未发现有视频
                    fragment.mRefreshLayout.finishRefreshing();
                    fragment.mProgressLayout.showEmpty();
                    break;
                case 6://视频文件夹点击
                    String folderName = fragment.mAdapter.getVideoFolderList().get(msg.arg1).getFolderName();
                    ArrayList<ModelVideoInfor> item = fragment.mAdapter.getVideoFolderList().get(msg.arg1).getVideoList();
                    fragment.mAdapter.setVideoList(item);
                    fragment.mAdapter.setIsFolder(false);
                    fragment.isFolder = false;
                    fragment.mAdapter.notifyDataSetChanged();

                    fragment.lastSelectFolderName = folderName;

                    EventChangeToolBar object = new EventChangeToolBar(EventCode.EVENT_CHANGE_TOOLBAR_FROM_VIDEO_FRAGMENT);
                    object.setTitle(folderName);
                    object.setColor(R.color.colorBlue);
                    EventBus.getDefault().post(object);

                    break;
                case 7007://test code
                    break;
            }
        }
    }

    SnackBar.Builder mDeleteSnackBar;
    private boolean mDeleteSnackBarIsShow = false;
    private boolean isClickUndoDelete = false;

    private void showDelete() {
        if (mDeleteSnackBar == null) {
            mDeleteSnackBar = new SnackBar.Builder(getActivity())
                    .withOnClickListener(new SnackBar.OnMessageClickListener() {
                        @Override
                        public void onMessageClick(Parcelable token) {
                            isClickUndoDelete = true;
                            undoDelete();
                        }
                    })
                    .withMessage("Are you sure to delete this video?")
                    .withActionMessage("UNDO") // OR
                    .withTextColorId(R.color.white)
                    .withBackgroundColorId(R.color.colorRedDark)
                    .withVisibilityChangeListener(new SnackBar.OnVisibilityChangeListener() {
                        @Override
                        public void onShow(int stackSize) {
                            isClickUndoDelete = false;
                            mDeleteSnackBarIsShow = true;
                        }

                        @Override
                        public void onHide(int stackSize) {
                            mDeleteSnackBarIsShow = false;
                            if (isClickUndoDelete) {
                            } else {
                                FolderUtils.deleteFile(new File(deleteVideoInforModel.getPath()));
                                DataSupport.deleteAll(ModelVideoInfor.class, "path = ?", deleteVideoInforModel.getPath());
                            }
                        }
                    })
                    .withDuration((short) 5000);
        }

        if (!mDeleteSnackBarIsShow && mDeleteSnackBar != null) {
            mDeleteSnackBar.show();
        }
    }

    private int deletePosition = 0;
    private ModelVideoInfor deleteVideoInforModel;

    private void undoDelete() {
        mModelFetch.addVideo(deletePosition, deleteVideoInforModel);
        mAdapter.notifyItemInserted(deletePosition);
    }

    private void setAdapter() {
        mModelFetch.clearErrorPath();
        if (mModelFetch.allVideoFolderList.size() > 0) {
            if (mAdapter == null) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mAdapter = new VideoFragmentAdapter(mContext, mModelFetch.allVideoFolderList, mHandler);
                mAdapter.setMode(Attributes.Mode.Single);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }

            mProgressLayout.showContent();
        } else {
            mProgressLayout.showEmpty();
        }
    }

    private SweetSheet mSweetSheetInformation;
    private TextView mVideoInforPath;
    private TextView mVideoInforSize;
    private TextView mVideoInforResolution;
    private TextView mVideoInforDuration;

    private void showInformation(ModelVideoInfor videoInforModel) {
        if (mSweetSheetInformation == null) {
            mSweetSheetInformation = new SweetSheet(mProgressLayout);
            CustomDelegate customDelegate = new CustomDelegate(true,
                    CustomDelegate.AnimationType.DuangLayoutAnimation);
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_video_information, null, false);
            customDelegate.setCustomView(view);
            mSweetSheetInformation.setDelegate(customDelegate);

            mVideoInforPath = (TextView) view.findViewById(R.id.mVideoInforPath);
            mVideoInforSize = (TextView) view.findViewById(R.id.mVideoInforSize);
            mVideoInforResolution = (TextView) view.findViewById(R.id.mVideoInforResolution);
            mVideoInforDuration = (TextView) view.findViewById(R.id.mVideoInforDuration);
        }

        if (!mSweetSheetInformation.isShow()) {
            mVideoInforPath.setText(videoInforModel.getPath());
            mVideoInforSize.setText(FileUtils.convertFileSize(Long.parseLong(videoInforModel.getSize())));
            mVideoInforResolution.setText(String.format("%s X %s", videoInforModel.getWidth(), videoInforModel.getHeight()));
            mVideoInforDuration.setText(TimeUtils.secToTime(Long.parseLong(videoInforModel.getDuration())));

            mSweetSheetInformation.toggle();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == ErrorCode.ErrorPlayVedio) {
            try {
                String path = data.getStringExtra("path");
                int index = DataSupport.deleteAll(ModelVideoInfor.class, "path = ?", path);
                if (index >= 0) {
                    mModelFetch.allVideoList.remove(playIndex);
                    playIndex = -1;
                    setAdapter();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
}
