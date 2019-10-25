package com.softinc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softinc.youelink.R;

/**
 * Created by zhangbing on 15-1-26.
 */
public class Tag extends LinearLayout {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private Context context;
    private String text;
    private TagListener listener;

    // ===============================================================================
    // UI
    // ===============================================================================

    private Button bt_delete;
    private TextView tv_label;

    public Tag(Context context) {
        super(context);
        this.context = context;
        text = "傻boy";
        initView();
    }

    public Tag(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        text = attrs.getAttributeValue(NAMESPACE, "text");

        initView();
    }

    private void initView() {
        View.inflate(context, R.layout.view_tag, this);
        bt_delete = (Button) this.findViewById(R.id.bt_delete);
        tv_label = (TextView) this.findViewById(R.id.tv_label);

        tv_label.setText(text);

        bt_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }

    /**
     * 设置左侧标题名字
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
        tv_label.setText(text);
    }

    public String getText() {
        return text;
    }

    /**
     * 删除监听
     *
     * @param listener
     */
    public void setOnDeleteListener(TagListener listener) {
        this.listener = listener;
    }


    public interface TagListener {
        public void onClick(View v);
    }

}
