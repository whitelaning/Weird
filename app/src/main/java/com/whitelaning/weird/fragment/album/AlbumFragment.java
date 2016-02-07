package com.whitelaning.weird.fragment.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.framework.android.fragment.BaseFragment;
import com.framework.android.model.BaseEvent;
import com.framework.android.view.ProgressLayout;
import com.whitelaning.weird.R;
import com.whitelaning.weird.activity.album.FolderImageListActivity;
import com.whitelaning.weird.adapter.album.ImageGroupAdapter;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.model.album.EventDeleteImage;
import com.whitelaning.weird.model.album.ModelImageGroup;
import com.whitelaning.weird.modelFetch.album.AlbumFragmentModelFetch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by Zack White on 2016/1/10.
 */
public class AlbumFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    public final static String TAG = "MusicAlbumFragment.TAG";
    @Bind(R.id.mGroupImagesGv)
    GridView mGroupImagesGv;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;
    @Bind(R.id.mCamera)
    ImageView mCamera;

    private Context mContext;
    private ImageGroupAdapter mGroupAdapter;
    private AlbumFragmentModelFetch mModelFetch;

    public AlbumFragment() {
        // Required empty public constructor
    }

    public static AlbumFragment newInstance(Bundle args) {
        AlbumFragment fragment = new AlbumFragment();
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
        EventBus.getDefault().register(this);
        initModelFetch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    private void initView() {
        mProgressLayout.showProgress();
    }

    private void initData() {
        mContext = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mModelFetch.toFindImage();
    }

    private void initModelFetch() {
        mModelFetch = new AlbumFragmentModelFetch(mContext, mHandler);
    }

    public void onEventMainThread(BaseEvent item) {
        if (item != null) {
            if (item.getTAG() == EventCode.EVENT_DELETE_IMAGE) {
                EventDeleteImage event = (EventDeleteImage) item;
                String paths = event.getDeletePathString();
                mGroupAdapter.removeImageByValue(paths);
                mGroupAdapter.notifyDataSetChanged();
            }
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<AlbumFragment> mFragment;

        MyHandler(AlbumFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AlbumFragment fragment = mFragment.get();
            switch (msg.what) {
                case 0:
                    // 加载失败，显示错误提示
                    fragment.mProgressLayout.showErrorText("Load is error");
                    break;
                case 1:
                    // 加载成功，显示数据
                    fragment.setImageAdapter((ArrayList<ModelImageGroup>) msg.obj);
                    break;
                case 7007://test code
                    break;
            }
        }
    }

    @OnClick({R.id.mCamera})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.mCamera:
                Intent intent = new Intent();
                intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivityForResult(intent, PICTURE_FROM_CAMERA);
                break;
        }
    }


    private static final int PICTURE_FROM_CAMERA = 1001;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICTURE_FROM_CAMERA:
                mModelFetch.toFindImage();
                break;
        }
    }

    /**
     * 构建GridView的适配器
     *
     * @param data
     */
    private void setImageAdapter(ArrayList<ModelImageGroup> data) {
        if (data == null || data.size() == 0) {
            mProgressLayout.showEmpty();
        } else {
            mProgressLayout.showContent();
        }

        mGroupAdapter = new ImageGroupAdapter(mContext, data, mGroupImagesGv);
        mGroupImagesGv.setAdapter(mGroupAdapter);
        mGroupImagesGv.setOnItemClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        ModelImageGroup imageGroup = mGroupAdapter.getItem(position);
        if (imageGroup == null) {
            return;
        }

        Intent mIntent = new Intent(mContext, FolderImageListActivity.class);
        mIntent.putExtra(FolderImageListActivity.EXTRA_TITLE, imageGroup.getDirName());
        mIntent.putStringArrayListExtra(FolderImageListActivity.EXTRA_IMAGES_DATAS, imageGroup.getImages());
        startActivityForResult(mIntent, 1000);
    }
}
