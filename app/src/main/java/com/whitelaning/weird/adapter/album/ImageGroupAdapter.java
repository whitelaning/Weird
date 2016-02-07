/**
 * ImageGroupAdapter.java
 * ImageChooser
 * <p/>
 * Created by likebamboo on 2014-4-22
 * Copyright (c) 1998-2014 http://likebamboo.github.io/ All rights reserved.
 */

package com.whitelaning.weird.adapter.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.whitelaning.weird.R;
import com.whitelaning.weird.model.album.ModelImageGroup;
import com.whitelaning.weird.view.AlbumImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 分组图片适配器
 */
public class ImageGroupAdapter extends BaseAdapter {
    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 图片列表
     */
    private List<ModelImageGroup> mDataList;

    /**
     * 容器
     */
    private View mContainer;

    public ImageGroupAdapter(Context context, List<ModelImageGroup> list, View container) {
        mDataList = list;
        mContext = context;
        mContainer = container;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public ModelImageGroup getItem(int position) {
        if (position < 0 || position > mDataList.size()) {
            return null;
        }
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.image_group_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ModelImageGroup item = getItem(position);
        if (item != null) {
            // 图片路径
            String path = item.getFirstImgPath();
            // 标题
            holder.mTitleTv.setText(item.getDirName());
            // 计数
            holder.mCountTv.setText(mContext.getString(R.string.image_count, item.getImageCount()));
            // 加载图片
            Glide.with(mContext)
                    .load(new File(path))
                    .placeholder(R.drawable.ic_photo_grey_20160110)
                    .error(R.drawable.ic_photo_grey_20160110)
                    .fallback(R.drawable.ic_empty_gray_20160110)
                    .into(holder.mImageIv);

        }
        return view;
    }

    public void removeImageByValue(String paths) {

        Iterator<ModelImageGroup> iteratorDataList = mDataList.iterator();
        while (iteratorDataList.hasNext()) {
            ModelImageGroup items = iteratorDataList.next();
            ArrayList<String> images = items.getImages();
            Iterator<String> iteratorImages = images.iterator();
            while (iteratorImages.hasNext()) {
                String path = iteratorImages.next();
                if (paths.contains(path)) {
                    iteratorImages.remove();
                }
            }

            if (images.size() <= 0) {
                iteratorDataList.remove();
            }
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'image_group_item.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    class ViewHolder {
        @Bind(R.id.group_item_image_iv)
        AlbumImageView mImageIv;
        @Bind(R.id.group_item_title_tv)
        TextView mTitleTv;
        @Bind(R.id.group_item_count_tv)
        TextView mCountTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
