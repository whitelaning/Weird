package com.framework.android.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 封装的ViewHolder
 */
public class WhiteViewHolder {
    private SparseArray<View> viewList; //View的集合


    private int position; //位置

    private View convertView;//导入的布局
    private Context context;//上下文
    private int layoutId;//布局ID

    /**
     * WhiteViewHolder 构造函数
     *
     * @param context  上下文
     * @param parent   ViewGroup
     * @param layoutId 布局ID
     * @param position index
     */
    private WhiteViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
        this.context = context;
        this.layoutId = layoutId;
        this.position = position;

        this.viewList = new SparseArray<View>();

        convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        convertView.setTag(this);
    }

    /**
     * 获取WhiteViewHolder的实力
     *
     * @param context     上下文
     * @param convertView 导入的布局View
     * @param parent      ViewGroup
     * @param layoutId    布局ID
     * @param position    index
     * @return WhiteViewHolder
     */
    public static WhiteViewHolder getInstance(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
        if (null == convertView) {
            return new WhiteViewHolder(context, parent, layoutId, position);
        } else {
            WhiteViewHolder whiteViewHolder = (WhiteViewHolder) convertView.getTag();
            whiteViewHolder.position = position;
            return whiteViewHolder;
        }
    }

    /**
     * 获取布局View
     *
     * @param ViewId view的ID
     * @param <T>
     * @return T , T extends View
     */
    public <T extends View> T getView(int ViewId) {
        View view = viewList.get(ViewId);
        if (null == view) {
            view = convertView.findViewById(ViewId);
            viewList.put(ViewId, view);
        }

        return (T) view;
    }

    /**
     * 返回convertView
     *
     * @return View
     */
    public View getConvertView() {
        return convertView;
    }

    /**
     * 设置TextView文本
     *
     * @param viewId TextView的ID
     * @param text   显示文本
     * @return
     */
    public WhiteViewHolder setText(int viewId, String text) {
        TextView textView = getView(viewId);
        textView.setText(text);
        return this;
    }

    public WhiteViewHolder setImageResource(int viewId, int imageId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(imageId);
        return this;
    }

    public WhiteViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView imageView = getView(viewId);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public WhiteViewHolder setImageViewBackgroundColor(int viewId, int color) {
        ImageView imageView = getView(viewId);
        imageView.setBackgroundColor(color);
        return this;
    }

    public WhiteViewHolder setImageViewBackgroundColor(int viewId, Drawable drawable) {
        ImageView imageView = getView(viewId);
        imageView.setBackground(drawable);
        return this;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setConvertView(View convertView) {
        this.convertView = convertView;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public SparseArray<View> getViewList() {
        return viewList;
    }

    public void setViewList(SparseArray<View> viewList) {
        this.viewList = viewList;
    }
}