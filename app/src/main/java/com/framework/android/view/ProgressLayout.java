package com.framework.android.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.whitelaning.weird.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Whitelaning on 2015/7/4.
 * Email: Whitelaning@gmail.com
 */
public class ProgressLayout extends RelativeLayout {
    private static final String TAG_PROGRESS = "ProgressLayout.TAG_PROGRESS";
    private static final String TAG_ERROR = "ProgressLayout.TAG_ERROR";
    private static final String TAG_EMPTY = "ProgressLayout.TAG_EMPTY";

    public enum State {
        CONTENT, PROGRESS, ERROR, EMPTY
    }
    private View mLoadingView;
    private TextView mErrorTextView;
    private View mEmptyView;

    private Context mContext;
    private State mState = State.CONTENT;

    private List<View> mContentViews = new ArrayList<>();

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public ProgressLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }


    private void init() {
        mLoadingView = LayoutInflater.from(mContext).inflate(R.layout.progress_pacman_loading, null);
        mLoadingView.setTag(TAG_PROGRESS);
        LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.addRule(CENTER_IN_PARENT);
        addView(mLoadingView, layoutParams1);
        //---------
        mErrorTextView = new TextView(getContext());
        mErrorTextView.setTag(TAG_ERROR);
        mErrorTextView.setGravity(Gravity.CENTER);
        mErrorTextView.setTextColor(mContext.getResources().getColor(R.color.text_color_default));
        LayoutParams layoutParams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(CENTER_IN_PARENT);
        addView(mErrorTextView, layoutParams2);
        //---------
        mEmptyView = LayoutInflater.from(mContext).inflate(R.layout.progress_empty, null);
        mEmptyView.setTag(TAG_ERROR);
        LayoutParams layoutParams3 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams3.addRule(CENTER_IN_PARENT);
        addView(mEmptyView, layoutParams3);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (child.getTag() == null ||
                !child.getTag().equals(TAG_PROGRESS) &&
                        !child.getTag().equals(TAG_ERROR)) {
            mContentViews.add(child);
        }
    }


    public void showProgress() {
        switchState(State.PROGRESS, null, Collections.<Integer>emptyList());
    }

    public void showErrorText(String error) {
        switchState(State.ERROR, error, Collections.<Integer>emptyList());
    }

    public void showContent() {
        switchState(State.CONTENT, null, Collections.<Integer>emptyList());
    }

    public void showEmpty() {
        switchState(State.EMPTY, null, Collections.<Integer>emptyList());
    }

    public void showProgress(List<Integer> skipIds) {
        switchState(State.PROGRESS, null, skipIds);
    }

    public void showErrorText() {
        switchState(State.ERROR, null, Collections.<Integer>emptyList());
    }

    public void showErrorText(List<Integer> skipIds) {
        switchState(State.ERROR, null, skipIds);
    }

    public void showErrorText(String error, List<Integer> skipIds) {
        switchState(State.ERROR, error, skipIds);
    }

    public void showContent(List<Integer> skipIds) {
        switchState(State.CONTENT, null, skipIds);
    }

    public void switchState(State state) {
        switchState(state, null, Collections.<Integer>emptyList());
    }

    public void switchState(State state, String errorText) {
        switchState(state, errorText, Collections.<Integer>emptyList());
    }

    public void switchState(State state, List<Integer> skipIds) {
        switchState(state, null, skipIds);
    }

    public void switchState(State state, String errorText, List<Integer> skipIds) {
        mState = state;

        switch (state) {
            case CONTENT:
                mLoadingView.setVisibility(GONE);
                mErrorTextView.setVisibility(View.GONE);
                mEmptyView.setVisibility(GONE);
                setContentVisibility(true, skipIds);
                break;
            case PROGRESS:
                mLoadingView.setVisibility(VISIBLE);
                mErrorTextView.setVisibility(View.GONE);
                mEmptyView.setVisibility(GONE);
                setContentVisibility(false, skipIds);
                break;
            case ERROR:
                if (TextUtils.isEmpty(errorText)) {
                    mErrorTextView.setText("Something is error");
                } else {
                    mErrorTextView.setText(errorText);
                }
                mLoadingView.setVisibility(GONE);
                mErrorTextView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(GONE);
                setContentVisibility(false, skipIds);
                break;
            case EMPTY:
                mLoadingView.setVisibility(GONE);
                mErrorTextView.setVisibility(View.GONE);
                mEmptyView.setVisibility(VISIBLE);
                setContentVisibility(false, skipIds);
        }
    }

    public State getState() {
        return mState;
    }

    public boolean isProgress() {
        return mState == State.PROGRESS;
    }

    public boolean isContent() {
        return mState == State.CONTENT;
    }

    public boolean isError() {
        return mState == State.ERROR;
    }

    private void setContentVisibility(boolean visible, List<Integer> skipIds) {
        for (View v : mContentViews) {
            if (!skipIds.contains(v.getId())) {
                v.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
}
