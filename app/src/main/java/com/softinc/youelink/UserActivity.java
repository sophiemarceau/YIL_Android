package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting_User;
import com.softinc.config.Constant;
import com.softinc.utils.StringHelper;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends Activity {
    private Integer user_id;

    private LinearLayout llBack;

    private CircleImageView civ_icon;
    private ImageView ivSex;

    private TextView tvOwnCnt;
    private TextView tvJoinCnt;

    private TextView tvName;
    private TextView tvNickName;
    private TextView tvGender;
    private TextView tvBirth;
    private TextView tvAreaName;
    private TextView tv_phone;
    private TextView tv_mail;
    private TextView tv_interest;

    private LinearLayout ll_create;
    private LinearLayout ll_join;

    private Button btn_chat;
    private Button btn_add_friend;
    private Button btn_wait;

    private String hxUser_id = "";

    private Boolean get_friends_done = false, get_wait_friends_done = false;
    private Boolean is_friend = false, is_wait_friend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user_id = Integer.parseInt(getIntent().getStringExtra("user_id"));

        setContentView(R.layout.activity_user);

        this.initView();

        this.getUser();
    }

    private void initView() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserActivity.this.finish();
            }
        });

        ll_create = (LinearLayout) findViewById(R.id.ll_create);
        ll_join = (LinearLayout) findViewById(R.id.ll_join);
        ll_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MineCreateEvents.class);
                intent.putExtra("user_id", user_id);
                UserActivity.this.startActivity(intent);
            }
        });
        ll_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MineJoinEvents.class);
                intent.putExtra("user_id", user_id);
                UserActivity.this.startActivity(intent);
            }
        });

        civ_icon = (CircleImageView) findViewById(R.id.civ_icon);
        ivSex = (ImageView) findViewById(R.id.ivSex);

        tvOwnCnt = (TextView) findViewById(R.id.tvOwnCnt);
        tvJoinCnt = (TextView) findViewById(R.id.txtJoinCnt);
        tvName = (TextView) findViewById(R.id.txtName);
        tvNickName = (TextView) findViewById(R.id.txtNickName);
        tvGender = (TextView) findViewById(R.id.txtGender);
        tvBirth = (TextView) findViewById(R.id.txtBirth);
        tvAreaName = (TextView) findViewById(R.id.txtArea);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_mail = (TextView) findViewById(R.id.tv_mail);
        tv_interest = (TextView) findViewById(R.id.tv_interest);

        btn_chat = (Button) findViewById(R.id.btn_chat);
        btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
        btn_wait = (Button) findViewById(R.id.btn_wait);

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle myBundelForName = new Bundle();
                myBundelForName.putString("nickname", tvNickName.getText().toString());
                myBundelForName.putString("userId", user_id.toString());
                startActivity(new Intent(UserActivity.this, ChatActivity.class).putExtras(myBundelForName));
            }
        });

        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.toString().equals(MyApplication.uid)) {
                    Toast.makeText(UserActivity.this, "不能加自己为好友", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("buddyId", user_id);
                params.put("buddyHxId", hxUser_id);

                MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        try {
                            Boolean success = response.getBoolean("Result");
                            if (success) {
                                if (response.getInt("Data") == 1) {
                                    btn_add_friend.setVisibility(View.GONE);
                                    btn_wait.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), response.getString("Infomation"), Toast.LENGTH_SHORT).show();
                                } else {
                                    btn_add_friend.setVisibility(View.GONE);
                                    btn_chat.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "添加成功！", Toast.LENGTH_SHORT).show();

                                    String s = getApplicationContext().getResources().getString(R.string.Add_a_friend);
                                    EMContactManager.getInstance().addContact(hxUser_id, s);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void getUser() {
        RequestParams params = new RequestParams();
        params.put("uid", user_id);

        MyApplication.client.get(Constant.URL_GET_USER_BY_ID, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject userObj = response.getJSONObject("Data");

                    String iconPath = userObj.getString("UserPic");

                    if (!TextUtils.isEmpty(iconPath) && !"null".equals(iconPath)) {
                        ImageLoader.getInstance().displayImage("http://123.57.217.223/youelink/upload/userpic/" + iconPath, civ_icon);
                    }
                    tvOwnCnt.setText("发起的活动（" + userObj.getString("myEventCnt") + "）");
                    tvJoinCnt.setText("参与的活动（" + userObj.getString("myJoinCnt") + "）");
                    tvName.setText(userObj.getString("NickName"));
                    tvNickName.setText(userObj.getString("NickName"));

                    String gen = "女";
                    if (userObj.getString("Gender").equals("1")) {
                        ivSex.setBackgroundResource(R.drawable.male_icon);

                        gen = "男";
                    } else {
                        ivSex.setBackgroundResource(R.drawable.female_icon);
                    }
                    tvGender.setText(gen);

                    tvBirth.setText(userObj.getString("Brithday"));
                    tvAreaName.setText(userObj.getString("AreaName"));
                    ////tv_phone.setText(userObj.getString("AreaName"));

                    tv_mail.setText((!StringHelper.isEmptyOrNull(userObj.getString("EMail"))) ? userObj.getString("EMail") : "");
                    tv_interest.setText((!StringHelper.isEmptyOrNull(userObj.getString("Hobbies"))) ? userObj.getString("Hobbies") : "");

                    hxUser_id = userObj.getString("hxUser");

                    Integer status = userObj.getInt("BuddyStatus");
                    initChatBtn(status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initChatBtn(int status) {
        switch (status){
            case 0:
                btn_chat.setVisibility(View.GONE);
                btn_add_friend.setVisibility(View.VISIBLE);
                btn_wait.setVisibility(View.GONE);
                break;
            case 1:
                btn_chat.setVisibility(View.GONE);
                btn_add_friend.setVisibility(View.GONE);
                btn_wait.setVisibility(View.VISIBLE);
                break;
            case 2:
                btn_chat.setVisibility(View.VISIBLE);
                btn_add_friend.setVisibility(View.GONE);
                btn_wait.setVisibility(View.GONE);
                break;
        }
    }
}
