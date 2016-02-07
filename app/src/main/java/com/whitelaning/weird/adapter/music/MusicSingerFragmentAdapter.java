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
import com.framework.android.tool.logger.Logger;
import com.whitelaning.weird.R;
import com.whitelaning.weird.console.IConstants;
import com.whitelaning.weird.model.music.ModelArtistInfo;
import com.whitelaning.weird.model.music.ModelMusicInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicSingerFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<ModelArtistInfo> singerList;
    private List<ModelMusicInfo> songList;
    private Handler mHandler;
    private Context mContext;
    private boolean isFolder;
    private int nowPlayingSongId;
    private String artist;

    public MusicSingerFragmentAdapter(Context mContext, List<ModelArtistInfo> singerList, Handler mHandler) {
        this.mContext = mContext;
        this.singerList = singerList;
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
            setFolderData((SingerViewHolder) holder, position);
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
                    msg.what = 4;
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
                    msg.what = 5;
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

    private void setFolderData(SingerViewHolder holder, int position) {
        final ModelArtistInfo sModelArtistInfo = singerList.get(position);
        holder.musicName.setText(sModelArtistInfo.getArtistName());
        holder.musicNumber.setText(String.valueOf(sModelArtistInfo.getNumberOfTracks()));

        String artistName = sModelArtistInfo.getArtistName();

        if (artistName.contains("/")) {
            artistName = artistName.split("/")[0];
        } else if (artistName.contains("&")) {
            artistName = artistName.split("&")[0];
        }

        String artistPicPath = sModelArtistInfo.getArtistPicPath();
        if (StringUtils.isBlank(artistPicPath)) {
            downloadArtistPic(holder, sModelArtistInfo, artistName);
        } else {
            Glide.with(mContext)
                    .load(artistPicPath)
                    .override(120, 120)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.singer_list_default_image)
                    .error(R.drawable.singer_list_default_image)
                    .fallback(R.drawable.singer_list_default_image)
                    .into(holder.musicAlbum);
        }

        if (StringUtils.equals(artist, sModelArtistInfo.getArtistName())) {
            holder.isPlayView.setVisibility(View.VISIBLE);
        } else {
            holder.isPlayView.setVisibility(View.GONE);
        }

        setListener(holder, position);
    }


    private void setListener(SingerViewHolder sViewHolder, final int position) {
        sViewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
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
    }

    private void downloadArtistPic(final SingerViewHolder sViewHolder, final ModelArtistInfo sModelArtistInfo, String artistName) {
        OkHttpUtils
                .get()
                .url(IConstants.DONGTING_ARTIST_SEARCH)
                .addParams("q", artistName)
                .addParams("page", "1")
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
                                final String picUrl = item.optString("pic_url");

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ContentValues values1 = new ContentValues();
                                        values1.put("artistPicPath", picUrl);
                                        DataSupport.updateAll(ModelArtistInfo.class, values1, "artistName = ?", sModelArtistInfo.getArtistName());

                                        ContentValues values2 = new ContentValues();
                                        values2.put("artistPicPath", picUrl);
                                        int i = DataSupport.updateAll(ModelMusicInfo.class, values2, "artist = ?", sModelArtistInfo.getArtistName());
                                        Logger.i("i = " + i + "\n" + "sModelArtistInfo.getArtistName() = " + sModelArtistInfo.getArtistName());
                                    }
                                }).start();

                                Glide.with(mContext)
                                        .load(picUrl)
                                        .override(120, 120)
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.singer_list_default_image)
                                        .error(R.drawable.singer_list_default_image)
                                        .fallback(R.drawable.singer_list_default_image)
                                        .into(sViewHolder.musicAlbum);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new SingerViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_singer_fragment_adapter_cell, parent, false));
        } else {
            return new SingleViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.item_music_single_fragment_adapter_cell, parent, false));
        }
    }

    @Override
    public int getItemCount() {
        if (isFolder) {
            return singerList.size();
        } else {
            return songList.size();
        }
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_music_single_fragment_adapter_cell.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class SingerViewHolder extends BaseViewHolder {

        @Bind(R.id.musicAlbum)
        ImageView musicAlbum;
        @Bind(R.id.musicName)
        TextView musicName;
        @Bind(R.id.musicNumber)
        TextView musicNumber;
        @Bind(R.id.rootLayout)
        RelativeLayout rootLayout;
        @Bind(R.id.isPlayView)
        ImageView isPlayView;

        public SingerViewHolder(View itemView) {
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

    public List<ModelMusicInfo> getSongList() {
        return songList;
    }

    public void setSongList(List<ModelMusicInfo> songList) {
        this.songList = songList;
    }

    public List<ModelArtistInfo> getSingerList() {
        return singerList;
    }

    public void setSingerList(List<ModelArtistInfo> singerList) {
        this.singerList = singerList;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setNowPlayingSongId(int songId) {
        this.nowPlayingSongId = songId;
    }
}