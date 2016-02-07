package com.whitelaning.weird.adapter.album;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.bumptech.glide.Glide;
import com.whitelaning.weird.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * 查看大图的ViewPager适配器
 */
public class ImagePagerAdapter extends PagerAdapter {
    /**
     * 数据源
     */
    private List<String> mDatas = new ArrayList<>();

    /**
     * 显示参数
     */
    private Context mContext;

    public ImagePagerAdapter(Context mContext, ArrayList<String> dataList) {
        this.mContext = mContext;
        this.mDatas = dataList;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        photoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        Glide.with(mContext)
                .load(new File( getItem(position)))
                .placeholder(R.drawable.ic_photo_grey_20160110)
                .error(R.drawable.ic_photo_grey_20160110)
                .fallback(R.drawable.ic_empty_gray_20160110)
                .into(photoView);
        // Now just add PhotoView to ViewPager and return it
        container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        return photoView;
    }

    public String getItem(int position) {
        if (position < mDatas.size()) {
            return mDatas.get(position);
        } else {
            return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
