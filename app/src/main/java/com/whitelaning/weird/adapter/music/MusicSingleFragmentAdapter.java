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
import com.whitelaning.weird.model.music.ModelMusicInfo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Zack White on 1/29/2016.
 */
public class MusicSingleFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<ModelMusicInfo> singleList;
    private Handler mHandler;
    private Context mContext;
    private int nowPlayingSongId;

    public MusicSingleFragmentAdapter(Context mContext, List<ModelMusicInfo> singleList, Handler mHandler) {
        this.mContext = mContext;
        this.singleList = singleList;
        this.mHandler = mHandler;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {
        SingleViewHolder sViewHolder = (SingleViewHolder) holder;
        final ModelMusicInfo sMusicInfo = singleList.get(position);

        sViewHolder.musicName.setText(sMusicInfo.getMusicName());
        StringBuilder stringBuilder = new StringBuilder();
        if (TextUtils.isEmpty(sMusicInfo.getAlbum())) {
            stringBuilder.append(sMusicInfo.getArtist());
        } else {
            stringBuilder.append(sMusicInfo.getArtist()).append(" - ").append(sMusicInfo.getAlbum());
        }
        sViewHolder.musicSinger.setText(stringBuilder.toString());

        sViewHolder.musicMore.setOnClickListener(new View.OnClickListener() {
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

        sViewHolder.songRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler != null && sMusicInfo.getSongId() != nowPlayingSongId) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    msg.arg1 = position;
                    mHandler.sendMessage(msg);
                }
            }
        });

        if (sMusicInfo.getSongId() == nowPlayingSongId) {
            sViewHolder.isPlayView.setVisibility(View.VISIBLE);
        } else {
            sViewHolder.isPlayView.setVisibility(View.GONE);
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SingleViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.item_music_single_fragment_adapter_cell, parent, false));
    }

    @Override
    public int getItemCount() {
        return singleList.size();
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

    public List<ModelMusicInfo> getSingleList() {
        return singleList;
    }

    public void setSingleList(List<ModelMusicInfo> singleList) {
        this.singleList = singleList;
    }
}
