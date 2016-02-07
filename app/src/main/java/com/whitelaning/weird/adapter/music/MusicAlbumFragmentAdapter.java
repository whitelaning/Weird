package com.whitelaning.weird.adapter.music;

import android.content.ContentValues;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.framework.android.model.BaseViewHolder;
import com.framework.android.tool.StringUtils;
import com.whitelaning.weird.R;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.music.ModelAlbumInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicAlbumFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<ModelAlbumInfo> albumList;
    private List<ModelMusicInfo> songList;
    private Handler mHandler;
    private Context mContext;
    private boolean isFolder;
    private int nowPlayingSongId;
    private int albumId;

    public MusicAlbumFragmentAdapter(Context mContext, ArrayList<ModelAlbumInfo> albumList, Handler mHandler) {
        this.mContext = mContext;
        this.albumList = albumList;
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
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        if (getItemViewType(position) == 0) {
            setFolderData((AlbumViewHolder) holder, position);
        } else {
            setSongData((SingleViewHolder) holder, position);
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

    private void setFolderData(AlbumViewHolder holder, int position) {
        final ModelAlbumInfo sAlbumInfo = albumList.get(position);
        holder.musicName.setText(sAlbumInfo.getAlbumName());

        int number = sAlbumInfo.getNumberOfSongs();
        StringBuilder musicNumber = new StringBuilder();
        if (number > 1) {
            musicNumber.append(number).append(" songs - ").append(sAlbumInfo.getArtist());
        } else {
            musicNumber.append(number).append(" song - ").append(sAlbumInfo.getArtist());
        }
        holder.musicNumber.setText(musicNumber.toString());

        String albumPicPath = sAlbumInfo.getAlbumPicPath();
        if (StringUtils.isBlank(albumPicPath)) {
            downloadArtistPic(holder, sAlbumInfo);
        } else {
            Glide.with(mContext)
                    .load(albumPicPath)
                    .override(150, 150)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_disk_play_program)
                    .error(R.drawable.placeholder_disk_play_program)
                    .fallback(R.drawable.placeholder_disk_play_program)
                    .into(holder.musicAlbum);
        }

        if (albumId == sAlbumInfo.getAlbumId()) {
            holder.isPlayView.setVisibility(View.VISIBLE);
        } else {
            holder.isPlayView.setVisibility(View.GONE);
        }

        setListener(holder, position);
    }

    private void downloadArtistPic(final AlbumViewHolder viewHolder, final ModelAlbumInfo sAlbumInfo) {
        OkHttpUtils
                .get()
                .url(IConstants.DONGTING_ALBUM_SEARCH)
                .addParams("q", sAlbumInfo.getAlbumName())
                .addParams("size", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            JSONArray dataList = responseJSON.optJSONArray("data");
                            if (dataList != null && dataList.length() > 0) {
                                JSONObject item = (JSONObject) dataList.get(0);
                                final String picUrl = item.optString("picUrl");

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ContentValues values = new ContentValues();
                                        values.put("albumPicPath", picUrl);
                                        DataSupport.updateAll(ModelAlbumInfo.class, values, "albumName = ?", sAlbumInfo.getAlbumName());
                                    }
                                }).start();

                                Glide.with(mContext)
                                        .load(picUrl)
                                        .override(150, 150)
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.placeholder_disk_play_program)
                                        .error(R.drawable.placeholder_disk_play_program)
                                        .fallback(R.drawable.placeholder_disk_play_program)
                                        .into(viewHolder.musicAlbum);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void setListener(AlbumViewHolder holder, final int position) {
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
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new AlbumViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_album_fragment_adapter_cell, parent, false));
        } else {
            return new SingleViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_single_fragment_adapter_cell, parent, false));
        }
    }

    @Override
    public int getItemCount() {
        if (isFolder) {
            return albumList.size();
        } else {
            return songList.size();
        }
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;

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
    static class AlbumViewHolder extends BaseViewHolder {

        @Bind(R.id.musicAlbumBg)
        ImageView musicAlbumBg;
        @Bind(R.id.musicAlbum)
        ImageView musicAlbum;
        @Bind(R.id.isPlayView)
        ImageView isPlayView;
        @Bind(R.id.musicName)
        TextView musicName;
        @Bind(R.id.musicNumber)
        TextView musicNumber;
        @Bind(R.id.rootLayout)
        RelativeLayout rootLayout;

        public AlbumViewHolder(View itemView) {
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

    public boolean isFolder() {
        return isFolder;
    }

    public List<ModelMusicInfo> getSongList() {
        return songList;
    }

    public void setSongList(List<ModelMusicInfo> songList) {
        this.songList = songList;
    }

    public List<ModelAlbumInfo> getAlbumList() {
        return albumList;
    }

    public void setAlbumList(List<ModelAlbumInfo> albumList) {
        this.albumList = albumList;
    }
}
