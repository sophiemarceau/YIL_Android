package com.softinc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.engine.UserEngine;
import com.softinc.utils.PromptUtils;
import com.softinc.youelink.MineCreateEvents;
import com.softinc.youelink.MineDetailActivity;
import com.softinc.youelink.MineGetCredit;
import com.softinc.youelink.MineJoinEvents;
import com.softinc.youelink.MineQR;
import com.softinc.youelink.MineSetting;
import com.softinc.youelink.R;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MineFragment extends Fragment {

    private View view;

    private ImageButton ib_setting;
    private ImageButton ib_more;

    private Button btnJoin;
    private Button btnCom;

    private LinearLayout ll_qr;
    private LinearLayout ll_create;
    private LinearLayout ll_join;
    private LinearLayout ll_checkin;

    private User user = new User();

    private CircleImageView civ_icon;

    private TextView txtName;
    private TextView txtLevel;

    private ImageView ivSex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, container, false);

        initView();

        initViewEvents();

        initData();

        return view;
    }

    private void initView() {
        ib_setting = (ImageButton) view.findViewById(R.id.ib_setting);
        ib_more = (ImageButton) view.findViewById(R.id.ib_more);

        btnJoin = (Button) view.findViewById(R.id.btnJoin);
        btnCom = (Button) view.findViewById(R.id.btnCom);

        ll_qr = (LinearLayout) view.findViewById(R.id.ll_qr);
        ll_create = (LinearLayout) view.findViewById(R.id.ll_create);
        ll_join = (LinearLayout) view.findViewById(R.id.ll_join);
        ll_checkin = (LinearLayout) view.findViewById(R.id.ll_checkin);

        civ_icon = (CircleImageView) view.findViewById(R.id.civ_icon);

        txtName = (TextView) view.findViewById(R.id.txtName);
        txtLevel = (TextView) view.findViewById(R.id.txtLevel);

        ivSex = (ImageView) view.findViewById(R.id.ivSex);
    }

    private void initViewEvents() {
        ib_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineSetting.class);
                getActivity().startActivity(intent);
            }
        });

        ib_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineDetailActivity.class);
                getActivity().startActivity(intent);
            }
        });

        btnCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineGetCredit.class);

                getActivity().startActivity(intent);
            }
        });

        ll_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineQR.class);

                getActivity().startActivity(intent);
            }
        });

        ll_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineCreateEvents.class);
                intent.putExtra("user_id", Integer.parseInt(MyApplication.uid));
                getActivity().startActivity(intent);
            }
        });

        ll_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MineJoinEvents.class);
                intent.putExtra("user_id", Integer.parseInt(MyApplication.uid));
                getActivity().startActivity(intent);
            }
        });

        ll_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initData() {
        user = MyApplication.user;
        if (user == null) {
            return;
        }

        if (!TextUtils.isEmpty(user.iconPath) && !"null".equals(user.iconPath)) {
            ImageLoader.getInstance().displayImage(user.iconPath, civ_icon);
        }

        txtName.setText(user.getNick());
        String level = "不限";
        if (user.getUserLevel() != null && user.getUserLevel().equals("2")) {
            level = "黑卡";
        }

        txtLevel.setText(level);
        if(!TextUtils.isEmpty(user.getCreditPoint())) {
            btnJoin.setText("我的信用值（" + user.getCreditPoint() + "）");
        }

        if (!TextUtils.isEmpty(user.getGender()) && user.getGender().equals("1")) {
            ivSex.setBackgroundResource(R.drawable.male_icon);
        } else {
            ivSex.setBackgroundResource(R.drawable.female_icon);
        }
    }
}
