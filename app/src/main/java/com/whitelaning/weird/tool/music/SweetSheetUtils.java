package com.whitelaning.weird.tool.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.framework.android.tool.TimeUtils;
import com.framework.android.tool.logger.FileUtils;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.whitelaning.weird.R;
import com.whitelaning.weird.model.music.ModelMusicInfo;

import java.io.File;

/**
 * Created by Zack White on 2016/2/1.
 */
public class SweetSheetUtils {

    private TextView singer;
    private TextView album;
    private TextView fileName;
    private TextView duration;
    private TextView path;
    private TextView size;

    private SweetSheet mSweetSheetInformation;

    public void showMusicInformation(Context mContext, ModelMusicInfo musicInfo, RelativeLayout rootView) {
        if (mSweetSheetInformation == null) {
            mSweetSheetInformation = new SweetSheet(rootView);
            CustomDelegate customDelegate = new CustomDelegate(true,
                    CustomDelegate.AnimationType.DuangLayoutAnimation);
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_music_information, null, false);
            customDelegate.setCustomView(view);
            mSweetSheetInformation.setDelegate(customDelegate);

            singer = (TextView) view.findViewById(R.id.singer);
            album = (TextView) view.findViewById(R.id.album);
            fileName = (TextView) view.findViewById(R.id.fileName);
            duration = (TextView) view.findViewById(R.id.duration);
            path = (TextView) view.findViewById(R.id.path);
            size = (TextView) view.findViewById(R.id.size);
        }

        if (!mSweetSheetInformation.isShow()) {
            singer.setText(musicInfo.getArtist());
            album.setText(musicInfo.getAlbum());
            fileName.setText(musicInfo.getMusicName());
            duration.setText(TimeUtils.secToTime(musicInfo.getDuration()));
            path.setText(musicInfo.getData());
            size.setText(FileUtils.convertFileSize(new File(musicInfo.getData()).length()));
            mSweetSheetInformation.toggle();
        }
    }
}
