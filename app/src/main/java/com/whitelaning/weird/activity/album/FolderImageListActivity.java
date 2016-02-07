package com.whitelaning.weird.activity.album;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.framework.android.activity.BaseActivity;
import com.framework.android.model.BaseEvent;
import com.framework.android.tool.FolderUtils;
import com.framework.android.tool.ToastUtils;
import com.framework.android.tool.logger.FileUtils;
import com.framework.android.view.ProgressLayout;
import com.whitelaning.weird.R;
import com.whitelaning.weird.adapter.album.FolderImageListAdapter;
import com.whitelaning.weird.console.EventCode;
import com.whitelaning.weird.model.album.EventDeleteImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class FolderImageListActivity extends BaseActivity {

    /**
     * title
     */
    public static final String EXTRA_TITLE = "extra_title";

    /**
     * 图片列表extra
     */
    public static final String EXTRA_IMAGES_DATAS = "extra_images";
    @Bind(R.id.images_gv)
    GridView mImagesGv;
    @Bind(R.id.mProgressLayout)
    ProgressLayout mProgressLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private ArrayList<String> mImages = new ArrayList<>();
    private FolderImageListAdapter mImageAdapter;
    private boolean mMultiselect = false;
    private MaterialDialog mDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_image_list);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (!TextUtils.isEmpty(title)) {
            initToolbar(title, getResources().getColor(R.color.black));
        }

        initView();

        if (getIntent().hasExtra(EXTRA_IMAGES_DATAS)) {
            mImages = getIntent().getStringArrayListExtra(EXTRA_IMAGES_DATAS);
            setAdapter();
            setListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setListener() {
        mImageAdapter.setOnClickListener(new FolderImageListAdapter.onFolderImageOnClickListener() {
            @Override
            public void onClick(int position) {
                if (mMultiselect) {
                    if (mImageAdapter.isSelectImage(position)) {
                        mImageAdapter.removeSelectImageByPosition(position);
                    } else {
                        mImageAdapter.addSelectImage(position, mImages.get(position));
                    }

                    if (mImageAdapter.getImagesSelectNumber() == 0) {
                        disMultiselectModel();
                    } else if (mImageAdapter.getImagesSelectNumber() >= 1) {
                        invalidateOptionsMenu();
                    }

                    mImageAdapter.notifyDataSetChanged();
                } else {
                    Intent i = new Intent(FolderImageListActivity.this, ImageBrowseActivity.class);
                    i.putExtra(ImageBrowseActivity.EXTRA_IMAGES, mImages);
                    i.putExtra(ImageBrowseActivity.EXTRA_INDEX, position);
                    startActivity(i);
                }
            }

            @Override
            public void onLongClick(int position) {
                if (!mMultiselect) {
                    showMultiselectModel();
                    mImageAdapter.setMultiselectModel(true);
                    mImageAdapter.addSelectImage(position, mImages.get(position));
                    mImageAdapter.notifyDataSetChanged();
                }
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMultiselect) {
                    disMultiselectModel();
                } else {
                    finish();
                }
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.actionDelete:
                        ToastUtils.show("删除");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Map<Integer, String> mImagesSelects = mImageAdapter.getImagesSelectsList();
                                for (String value : mImagesSelects.values()) {
                                    File deleteFile = new File(value);
                                    FolderUtils.deleteFile(deleteFile);
                                }
                                EventDeleteImage object = new EventDeleteImage(EventCode.EVENT_DELETE_IMAGE);

                                StringBuilder s = new StringBuilder();
                                for (String path : mImagesSelects.values()) {
                                    s.append("[").append(path).append("]").append(",");
                                }

                                object.setDeletePathString(s.toString());
                                EventBus.getDefault().post(object);
                            }
                        }).start();

                        break;
                    case R.id.actionInfor:
                        String filePath = mImageAdapter.getSelectImagePath(0);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(filePath, options); // 此时返回的bitmap为null

                        TextView tv = new TextView(FolderImageListActivity.this);
                        tv.setTextColor(0xFFbdbdbd);
                        tv.setText(String.format(
                                "Path : %s" +
                                        "\n\nMimetype : %s" +
                                        "\n\nSize : %s" +
                                        "\n\nResolution : %dx%d",
                                filePath,
                                options.outMimeType,
                                FileUtils.convertFileSize(new File(filePath).length()),
                                options.outWidth,
                                options.outHeight));
                        new MaterialDialog.Builder(FolderImageListActivity.this)
                                .title("Details")
                                .customView(tv, true)
                                .positiveText("Cancel")
                                .positiveColor(0xFFFFFFFF)
                                .titleColor(0xFFFFFFFF)
                                .dividerColor(0xFFFF0000)
                                .backgroundColor(0xaa000000)
                                .show();

                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_folder_image_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMultiselect) {
            menu.findItem(R.id.actionDelete).setVisible(true);

            if (mImageAdapter != null && mImageAdapter.getImagesSelectNumber() > 1) {
                menu.findItem(R.id.actionInfor).setVisible(false);
            } else {
                menu.findItem(R.id.actionInfor).setVisible(true);
            }
        } else {
            menu.findItem(R.id.actionDelete).setVisible(false);
            menu.findItem(R.id.actionInfor).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void initToolbar(String title, @ColorInt int color) {
        mToolbar.setTitle(title);
        mToolbar.setBackgroundColor(color);
        this.getWindow().setStatusBarColor(color);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
    }

    private void showMultiselectModel() {
        mMultiselect = true;
        invalidateOptionsMenu();
    }

    private void disMultiselectModel() {
        mMultiselect = false;
        invalidateOptionsMenu();
        mImageAdapter.setMultiselectModel(false);
        mImageAdapter.clearSelectImage();
        mImageAdapter.notifyDataSetChanged();
    }


    /**
     * 初始化界面元素
     */
    private void initView() {
        mProgressLayout.showProgress();
    }

    /**
     * 构建并初始化适配器
     */
    private void setAdapter() {
        mImageAdapter = new FolderImageListAdapter(this, mImages);
        mImagesGv.setAdapter(mImageAdapter);
        mProgressLayout.showContent();
    }

    public void onEventMainThread(BaseEvent item) {
        if (item != null) {
            if (item.getTAG() == EventCode.EVENT_DELETE_IMAGE) {
                EventDeleteImage event = (EventDeleteImage) item;
                String path = event.getDeletePathString();
                mImageAdapter.removeImage(path);
                mImageAdapter.notifyDataSetChanged();
                disMultiselectModel();

                if (0 >= mImages.size()) {
                    setResult(1001);
                    finish();
                }
            }
        }
    }
}
