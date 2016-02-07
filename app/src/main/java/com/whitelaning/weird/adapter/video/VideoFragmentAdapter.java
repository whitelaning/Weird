package com.whitelaning.weird.adapter.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.android.model.BaseViewHolder;
import com.framework.android.tool.DiskLruCacheUtil;
import com.framework.android.tool.TimeUtils;
import com.framework.android.tool.logger.FileUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.other.io.DiskLruCache;
import com.other.swipe.SwipeLayout;
import com.other.swipe.adapters.RecyclerSwipeAdapter;
import com.whitelaning.weird.R;
import com.whitelaning.weird.model.video.ModelVideoFolderInfor;
import com.whitelaning.weird.model.video.ModelVideoInfor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Zack White on 2016/1/6.
 */
public class VideoFragmentAdapter extends RecyclerSwipeAdapter<BaseViewHolder> {

    private ArrayList<ModelVideoInfor> videoList = new ArrayList<>();
    private ArrayList<ModelVideoFolderInfor> videoFolderList = new ArrayList<>();
    private Handler mHandler;
    private Context mContext;
    private boolean isFolder = true;

    public VideoFragmentAdapter(Context mContext, ArrayList<ModelVideoFolderInfor> videoFolderList, Handler mHandler) {
        this.mContext = mContext;
        this.videoFolderList = videoFolderList;
        this.mHandler = mHandler;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFolder) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            setFolderData((ViewVideoFolderHolder) holder, position);
        } else {
            setVideoData((ViewVideoHolder) holder, position);
        }
    }

    private void setFolderData(ViewVideoFolderHolder videoFolderHolder, int position) {
        ModelVideoFolderInfor item = videoFolderList.get(position);
        videoFolderHolder.position = position;
        videoFolderHolder.title.setText(item.getFolderName());
        int number = item.getVideoList().size();
        if (number > 1) {
            videoFolderHolder.number.setText(String.format("%d Videos", number));
        } else if (number == 1) {
            videoFolderHolder.number.setText(String.format("%d Video", number));
        } else {
            videoFolderHolder.number.setText(String.format("%d Videos", number));
        }

        setVideoFolderListener(videoFolderHolder);
    }

    private void setVideoFolderListener(final ViewVideoFolderHolder videoFolderHolder) {
        videoFolderHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 6;
                    msg.arg1 = videoFolderHolder.position;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void setVideoData(ViewVideoHolder videoHolder, int position) {
        ModelVideoInfor item = videoList.get(position);

        videoHolder.position = position;

        String path = item.getPath();
        String title = item.getTitle();
        String displayName = item.getDisplayName();

        long duration = Long.parseLong(item.getDuration());
        String durationString = TimeUtils.secToTime(duration);

        long size = Long.parseLong(item.getSize());
        String sizeString = FileUtils.convertFileSize(size);

        //---------------------------------------------------------------------

        videoHolder.mTitle.setText(displayName);
        videoHolder.mTime.setText(String.format("%s | %s", durationString, sizeString));

        DiskLruCache.Snapshot snapShot = null;

        try {
            snapShot = DiskLruCacheUtil.getInstance().get(item.getImageDiskLruCacheKey());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (snapShot != null) {
            InputStream is = snapShot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            videoHolder.mThumbnail.setImageBitmap(bitmap);
        } else {
            videoHolder.mThumbnail.setImageResource(R.drawable.bg_video_default);
        }

        //---------------------------------------------------------------------

        setVideoListener(videoHolder);
    }

    private void setVideoListener(final ViewVideoHolder videoHolder) {
        videoHolder.mInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoHolder.mSwipeLayout.close();
                if (mHandler != null) {
                    Message msg1 = Message.obtain();
                    msg1.arg1 = videoHolder.position;
                    msg1.what = 1;
                    mHandler.sendMessage(msg1);
                }
            }
        });
        videoHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoHolder.isDelete = true;
                videoHolder.mSwipeLayout.close();
            }
        });

        videoHolder.mArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoHolder.mSwipeLayout.getOpenStatus() == SwipeLayout.Status.Close) {
                    videoHolder.mSwipeLayout.open();
                } else {
                    videoHolder.mSwipeLayout.close();
                }
            }
        });

        videoHolder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);// set show
        videoHolder.mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {

            int lastOffset = 0;

            @Override
            public void onStartOpen(SwipeLayout layout) {
            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {
                if (videoHolder.isDelete && mHandler != null) {
                    videoHolder.isDelete = false;
                    Message msg2 = Message.obtain();
                    msg2.arg1 = videoHolder.position;
                    msg2.what = 2;
                    mHandler.sendMessage(msg2);
                }
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                if (leftOffset * 180 / 240 < lastOffset) {
                    ObjectAnimator.ofFloat(videoHolder.mArrow, "rotation", lastOffset, leftOffset * 180 / 240).setDuration(300).start();
                    lastOffset = leftOffset * 180 / 240;
                } else if (leftOffset * 180 / 240 > lastOffset) {
                    ObjectAnimator.ofFloat(videoHolder.mArrow, "rotation", lastOffset, leftOffset * 180 / 240).setDuration(300).start();
                    lastOffset = leftOffset * 180 / 240;
                }
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        videoHolder.mSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    msg.arg1 = videoHolder.position;
                    mHandler.sendMessage(msg);
                }
            }
        });

        mItemManger.bind(videoHolder.itemView, videoHolder.position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ViewVideoFolderHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_main_activity_list_folder_cell, parent, false));
        } else {
            return new ViewVideoHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_main_activity_list_video_cell, parent, false));
        }
    }

    @Override
    public int getItemCount() {
        if (isFolder) {
            return videoFolderList.size();
        } else {
            return videoList.size();
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.mSwipeLayout;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public ArrayList<ModelVideoInfor> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<ModelVideoInfor> videoList) {
        this.videoList.clear();
        this.videoList.addAll(videoList);
    }

    public ArrayList<ModelVideoFolderInfor> getVideoFolderList() {
        return videoFolderList;
    }

    public void setVideoFolderList(ArrayList<ModelVideoFolderInfor> videoFolderList) {
        this.videoFolderList = videoFolderList;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_main_activity_list_cell.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewVideoHolder extends BaseViewHolder {
        @Bind(R.id.mThumbnail)
        ImageView mThumbnail;
        @Bind(R.id.mArrow)
        ImageView mArrow;
        @Bind(R.id.mTitle)
        TextView mTitle;
        @Bind(R.id.mTime)
        TextView mTime;
        @Bind(R.id.mSwipeLayout)
        SwipeLayout mSwipeLayout;
        @Bind(R.id.mInformation)
        TextView mInformation;
        @Bind(R.id.mDelete)
        TextView mDelete;
        @Bind(R.id.mSurface)
        LinearLayout mSurface;

        public int position;
        public boolean isDelete = false;

        ViewVideoHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_main_activity_list_cell.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewVideoFolderHolder extends BaseViewHolder {
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.number)
        TextView number;
        @Bind(R.id.rootLayout)
        RelativeLayout rootLayout;

        public int position;

        ViewVideoFolderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
