package com.whitelaning.weird.adapter.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.whitelaning.weird.R;
import com.whitelaning.weird.view.AlbumImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Zack White on 2016/1/15.
 */
public class FolderImageListAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<String> mDataList;
    private onFolderImageOnClickListener mListener;
    private boolean mMultiselect = false;
    private Map<Integer, String> mImagesSelects = new HashMap<>();

    public FolderImageListAdapter(Context context, ArrayList<String> list) {
        mDataList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public String getItem(int position) {
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
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_folder_imager_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 加载图片
        Glide.with(mContext)
                .load(new File(getItem(position)))
                .placeholder(R.drawable.ic_photo_grey_20160110)
                .error(R.drawable.ic_photo_grey_20160110)
                .fallback(R.drawable.ic_empty_gray_20160110)
                .into(holder.mImageIv);

        holder.mImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(position);
                }
            }
        });

        holder.mImageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.onLongClick(position);
                    return true;
                }
                return false;
            }
        });

        if (isSelectImage(position)) {
            holder.mSelectBackground.setVisibility(View.VISIBLE);
        } else {
            holder.mSelectBackground.setVisibility(View.GONE);
        }

        return view;
    }

    public boolean isSelectImage(int position) {
        return mImagesSelects.containsKey(position);
    }

    public void clearSelectImage() {
        mImagesSelects.clear();
    }

    public String getSelectImagePath(int key) {
        Set<Integer> keSet = mImagesSelects.keySet();

        boolean hasKey = false;
        int defaultKey = 0;
        for (Integer item : keSet) {
            if (key == item) {
                hasKey = true;
                key = item;
                break;
            } else {
                hasKey = false;
                defaultKey = item;
            }
        }

        if (hasKey) {
            return mImagesSelects.get(key);
        } else {
            return mImagesSelects.get(defaultKey);
        }
    }


    public Map<Integer, String> getImagesSelectsList() {
        return mImagesSelects;
    }

    public interface onFolderImageOnClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnClickListener(onFolderImageOnClickListener mListener) {
        this.mListener = mListener;
    }

    public void setMultiselectModel(boolean multiselect) {
        mMultiselect = multiselect;
    }

    public void addSelectImage(int position, String imagePath) {
        mImagesSelects.put(position, imagePath);
    }

    public void removeSelectImageByPosition(int position) {
        mImagesSelects.remove(position);
    }

    public void removeImage(String paths) {

        mImagesSelects.clear();

        Iterator<String> iterator = mDataList.iterator();
        while (iterator.hasNext()) {
            String path = iterator.next();
            if (paths.contains(path)) {
                iterator.remove();   //注意这个地方
            }
        }
    }

    public int getImagesSelectNumber() {
        return mImagesSelects.size();
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'adapter_folder_imager_list_item.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.list_item_iv)
        AlbumImageView mImageIv;
        @Bind(R.id.mSelectBackground)
        RelativeLayout mSelectBackground;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
