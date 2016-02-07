package com.whitelaning.weird.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.framework.android.fragment.BaseFragment;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.whitelaning.weird.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Zack White on 1/28/2016.
 */
public class MainFragment extends BaseFragment {
    public final static String TAG = "MainFragment.TAG";

    @Bind(R.id.playNeedle)
    ImageView playNeedle;
    @Bind(R.id.playNeedleShadow)
    ImageView playNeedleShadow;
    @Bind(R.id.playSong)
    ImageView playSong;
    @Bind(R.id.nextSong)
    ImageView nextSong;
    @Bind(R.id.lastSong)
    ImageView lastSong;


    private Context mContext;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(Bundle args) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 获取Activity传递过来的数据
        }
        setRetainInstance(true);
        initModelFetch();
    }

    private void initModelFetch() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        initData();
        initView();
        initListener();

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initData() {
        mContext = getActivity();
    }

    private void initView() {
    }

    private boolean isPlaying = false;
    private boolean isAnimation = false;

    private void initListener() {

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        lastSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        playSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAnimation) {
                    return;
                }

                if (isPlaying) {
                    isPlaying = false;
                    isAnimation = true;
                    ViewHelper.setPivotX(playNeedle, 98);
                    ViewHelper.setPivotY(playNeedle, 57);

                    ViewHelper.setPivotX(playNeedleShadow, 98);
                    ViewHelper.setPivotY(playNeedleShadow, 57);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(playNeedleShadow, "rotation", 20, 0).setDuration(800);
                    animator2.start();

                    ObjectAnimator animator1 = ObjectAnimator.ofFloat(playNeedle, "rotation", 20, 0).setDuration(800);
                    animator1.setStartDelay(80);
                    animator1.start();
                    animator1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimation = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                } else {
                    isPlaying = true;
                    isAnimation = true;
                    ViewHelper.setPivotX(playNeedle, 98);
                    ViewHelper.setPivotY(playNeedle, 57);
                    ObjectAnimator.ofFloat(playNeedle, "rotation", 0, 20).setDuration(800).start();

                    ViewHelper.setPivotX(playNeedleShadow, 98);
                    ViewHelper.setPivotY(playNeedleShadow, 57);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(playNeedleShadow, "rotation", 0, 20).setDuration(800);
                    animator2.setStartDelay(80);
                    animator2.start();
                    animator2.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimation = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
