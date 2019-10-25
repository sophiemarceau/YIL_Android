package com.softinc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softinc.youelink.R;

/**
 * Created by zhangbing on 15-1-23.
 */
public class SlidingButton extends LinearLayout implements View.OnClickListener {
    // ===============================================================================
    // 变量
    // ===============================================================================

    private static final String TAG = "SlidingButton";
    /**
     * 自定义控件命名空间
     */
    private static final String NAME_SPACE = "http://schemas.android.com/apk/res-auto";
    /**
     * 选中了左边?
     */
    private boolean checkLeft;
    private Context ctx;
    /**
     * 自定义控件  选中监听事件
     */
    private OnSelectListener listener;


    // ===============================================================================
    // 界面控件
    // ===============================================================================

    private TextView tv_left, tv_right;
    private ImageView iv_slidingLeft, iv_slidingRight;
    private RelativeLayout rl_sliding;


    public SlidingButton(Context context) {
        super(context);
    }

    public SlidingButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.ctx = context;

        String textLeft = attrs.getAttributeValue(NAME_SPACE, "text_left");
        String textRight = attrs.getAttributeValue(NAME_SPACE, "text_right");
        checkLeft = attrs.getAttributeValue(NAME_SPACE, "check_left").equals("true") ? true : false;

        View.inflate(context, R.layout.view_sliding_button, this);

        tv_left = (TextView) this.findViewById(R.id.tv_left);
        tv_right = (TextView) this.findViewById(R.id.tv_right);
        iv_slidingLeft = (ImageView) this.findViewById(R.id.iv_sliding_left);
        iv_slidingRight = (ImageView) this.findViewById(R.id.iv_sliding_right);
        rl_sliding = (RelativeLayout) findViewById(R.id.rl_sliding);

        tv_left.setText(textLeft);
        tv_right.setText(textRight);


        setBtPosition(checkLeft);

        rl_sliding.setOnClickListener(this);


    }

    /**
     * 改变按钮位置,通过设置2个ImageView的Visibility实现
     */
    private void setBtPosition(boolean isLeft) {
        Log.d(TAG, "改变按钮位置,是左边么?" + isLeft);

        if (isLeft) {
            iv_slidingLeft.setVisibility(VISIBLE);
            iv_slidingRight.setVisibility(INVISIBLE);
        } else {
            iv_slidingLeft.setVisibility(INVISIBLE);
            iv_slidingRight.setVisibility(VISIBLE);
        }
    }


    /**
     * 点击事件监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        //点击按钮 切换
        if (v == rl_sliding) {
            slideButton();
        }
    }

    /**
     * 播放滑动动画,实现切换按钮
     */
    private void slideButton() {
        rl_sliding.setClickable(false);
        Animation animation;

        if (checkLeft) {
            animation = AnimationUtils.loadAnimation(ctx, R.anim.bt_slide_right);
        } else {
            animation = AnimationUtils.loadAnimation(ctx, R.anim.bt_slide_left);
        }

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                checkLeft = !checkLeft;
                setBtPosition(checkLeft);
                if (listener != null) {
                    listener.onStateChanged(checkLeft);
                }
                rl_sliding.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (checkLeft) {
            iv_slidingLeft.startAnimation(animation);
        } else {
            iv_slidingRight.startAnimation(animation);
        }
    }


    /**
     * 选中回调
     */
    public interface OnSelectListener {
        public void onStateChanged(boolean isChecked);
    }


    public void setListener(OnSelectListener listener) {
        this.listener = listener;
    }
}
