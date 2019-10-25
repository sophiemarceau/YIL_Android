package com.softinc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softinc.youelink.R;

public class Title extends RelativeLayout implements View.OnClickListener {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private static final String TAG = "Title";
    private Context context;

    private int drawable_left;
    private String text_left;
    private String text_middle;
    private int drawable_right;
    private String text_right;
    private TitleListener listener;

    private ImageView iv_left;
    private TextView tv_left;
    private TextView tv_middle;
    private ImageView iv_right;
    private TextView tv_right;

    public Title(Context context) {
        super(context);
        this.context = context;
    }

    public Title(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        drawable_left = attrs.getAttributeResourceValue(NAMESPACE, "drawable_left", 0);
        text_left = attrs.getAttributeValue(NAMESPACE, "text_title_left");
        text_middle = attrs.getAttributeValue(NAMESPACE, "text_title_middle");
        drawable_right = attrs.getAttributeResourceValue(NAMESPACE, "drawable_right", 0);
        text_right = attrs.getAttributeValue(NAMESPACE, "text_title_right");

        initView();
    }

    private void initView() {
        View.inflate(context, R.layout.view_title, this);

        iv_left = (ImageView) findViewById(R.id.iv_title_left);
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_middle = (TextView) findViewById(R.id.tv_middle);
        iv_right = (ImageView) findViewById(R.id.iv_title_right);
        tv_right = (TextView) findViewById(R.id.tv_right);

        setDate();

        iv_left.setOnClickListener(this);
        tv_left.setOnClickListener(this);

        iv_right.setOnClickListener(this);
        tv_right.setOnClickListener(this);
    }

    /**
     * 控件绑定数据
     */
    private void setDate() {

        if (drawable_left != 0) {
            iv_left.setImageResource(drawable_left);
        } else {
            iv_left.setVisibility(GONE);
        }

        if (drawable_right != 0) {
            iv_right.setImageResource(drawable_right);
        } else {
            iv_right.setVisibility(GONE);
        }

        if (text_left != null) {
            tv_left.setText(text_left);
        } else {
            tv_left.setVisibility(INVISIBLE);
        }

        if (text_middle != null) {
            tv_middle.setText(text_middle);
        } else {
            tv_middle.setVisibility(INVISIBLE);
        }

        if (text_right != null) {
            tv_right.setText(text_right);
        } else {
            tv_right.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "点击了" + v);

        if (listener == null) return;

        if (v == tv_left || v == iv_left) {
            listener.onLeftTitleClick(v);
            Log.d(TAG, "左标题被点击了!!!!");
        } else if (v == tv_right || v == iv_right) {
            listener.onRightTitleClick(v);
            Log.d(TAG, "右标题被点击了!!!!");
        }
    }

    /**
     * 设置标题点击监听器
     *
     * @param listener
     */
    public void setTitleClickListener(TitleListener listener) {
        this.listener = listener;
    }

    public interface TitleListener {

        void onLeftTitleClick(View v);

        void onRightTitleClick(View v);
    }
}
