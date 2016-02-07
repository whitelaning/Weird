package com.whitelaning.weird.adapter.music;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.android.model.BaseViewHolder;
import com.whitelaning.weird.R;
import com.whitelaning.weird.model.music.ModelFolderInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicFolderFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<ModelFolderInfo> folderList;
    private List<ModelMusicInfo> songList;
    private Handler mHandler;
    private Context mContext;
    private boolean isFolder;
    private String path;
    private int nowPlayingSongId;

    public MusicFolderFragmentAdapter(Context mContext, ArrayList<ModelFolderInfo> folderList, Handler mHandler) {
        this.mContext = mContext;
        this.folderList = folderList;
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
            setFolderData((FolderViewHolder) holder, position);
        } else {
            setSongData((SingleViewHolder) holder, position);
        }
    }

    private void setFolderData(final FolderViewHolder holder, final int position) {
        final ModelFolderInfo sModelFolderInfo = folderList.get(position);
        holder.musicName.setText(sModelFolderInfo.getFolderName());

        String pathString = sModelFolderInfo.getFolderPath();
        if (pathString.contains("/0/")) {
            pathString = pathString.substring(pathString.indexOf("0/"), pathString.length());
        }

        holder.musicPath.setText(pathString);

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.arg1 = position;
                    mHandler.sendMessage(msg);
                }
            }
        });

        if (path != null && path.contains(sModelFolderInfo.getFolderPath())) {
            holder.isPlayView.setVisibility(View.VISIBLE);
        } else {
            holder.isPlayView.setVisibility(View.GONE);
        }
    }

    private void setSongData(final SingleViewHolder holder, final int position) {
        final ModelMusicInfo sMusicInfo = songList.get(position);

        holder.musicName.setText(sMusicInfo.getMusicName());
        StringBuilder stringBuilder = new StringBuilder();
        if (TextUtils.isEmpty(sMusicInfo.getAlbum())) {
            stringBuilder.append(sMusicInfo.getArtist());
        } else {
            stringBuilder.append(sMusicInfo.getArtist()).append(" - ").append(sMusicInfo.getAlbum());
        }
        holder.musicSinger.setText(stringBuilder.toString());

        holder.musicMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    msg.arg1 = position;
                    mHandler.sendMessage(msg);
                }
            }
        });


        holder.songRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null && sMusicInfo.getSongId() != nowPlayingSongId) {
                    Message msg = Message.obtain();
                    msg.what = 4;
                    msg.arg1 = position;
                    mHandler.sendMessage(msg);
                }
            }
        });

        if (sMusicInfo.getSongId() == nowPlayingSongId) {
            holder.isPlayView.setVisibility(View.VISIBLE);
        } else {
            holder.isPlayView.setVisibility(View.GONE);
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new FolderViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_folder_fragment_adapter_cell, parent, false));
        } else {
            return new SingleViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_single_fragment_adapter_cell, parent, false));
        }
    }

    @Override
    public int getItemCount() {
        if (isFolder) {
            return folderList.size();
        } else {
            return songList.size();
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNowPlayingSongId(int songId) {
        this.nowPlayingSongId = songId;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_music_single_fragment_adapter_cell.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class FolderViewHolder extends BaseViewHolder {

        @Bind(R.id.musicFolder)
        ImageView musicFolder;
        @Bind(R.id.isPlayView)
        ImageView isPlayView;
        @Bind(R.id.musicName)
        TextView musicName;
        @Bind(R.id.musicPath)
        TextView musicPath;
        @Bind(R.id.rootLayout)
        RelativeLayout rootLayout;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class SingleViewHolder extends BaseViewHolder {
        @Bind(R.id.musicMore)
        ImageView musicMore;
        @Bind(R.id.musicName)
        TextView musicName;
        @Bind(R.id.musicSinger)
        TextView musicSinger;
        @Bind(R.id.songRootLayout)
        RelativeLayout songRootLayout;
        @Bind(R.id.isPlayView)
        ImageView isPlayView;

        public SingleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setIsFolder(boolean b) {
        isFolder = b;
    }

    public List<ModelFolderInfo> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<ModelFolderInfo> folderList) {
        this.folderList = folderList;
    }

    public List<ModelMusicInfo> getSongList() {
        return songList;
    }

    public void setSongList(List<ModelMusicInfo> songList) {
        this.songList = songList;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
