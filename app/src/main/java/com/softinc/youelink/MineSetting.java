package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.utils.ACache;
import com.softinc.utils.PromptUtils;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MineSetting extends Activity {

    private LinearLayout ll_clear_cache;
    private CheckBox cb_push;
    private CheckBox cb_check;
    private Button btnExit;

    private Title tv_name;
    private TextView tv_about;
    private TextView tv_cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_setting);

        initView();
        initViewEvents();
    }

    private void initView() {
        btnExit = (Button) findViewById(R.id.btnExit);
        ll_clear_cache = (LinearLayout) findViewById(R.id.ll_clear_cache);
        tv_cache = (TextView) findViewById(R.id.txtCache);
        cb_push = (CheckBox) findViewById(R.id.cb_push);
        cb_check = (CheckBox) findViewById(R.id.cb_check);
        tv_name = (Title) findViewById(R.id.tv_name);
        tv_about = (TextView) findViewById(R.id.tv_about);

        if (MyApplication.user.getBuddyRequest().equals("1")) {
            cb_check.setChecked(true);
        } else {
            cb_check.setChecked(false);
        }

        calculateCacheSize();
    }

    private void initViewEvents() {
        if (MyApplication.user.getBuddyRequest() != null && MyApplication.user.getBuddyRequest().equals("1")) {
            cb_check.setChecked(true);
        } else {
            cb_check.setChecked(false);
        }

        ll_clear_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File[] files = MyApplication.cache_file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
                tv_cache.setText("0KB");
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EMChatManager.getInstance().logout(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_LAST_USER_ACCOUNT);
                        if (obj != null) {
                            MyApplication.cacheManager.remove(Constant.CACHE_LAST_USER_ACCOUNT);
                            MyApplication.cacheManager.remove(Constant.CACHE_LAST_USER_PWD);
                        }

                        startActivity(new Intent(MineSetting.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            }
        });

        cb_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    needConfirm(1);
                } else {
                    needConfirm(0);
                }
            }
        });

        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MineSetting.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });

        tv_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MineSetting.this, MineAboutActivity.class);
                MineSetting.this.startActivity(intent);
            }
        });
    }

    private void needConfirm(final Integer verify) {
        RequestParams params = new RequestParams();
        params.put("verify", verify);
        MyApplication.client.post(Constant.URL_USER_SETTING, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        MyApplication.user.setBuddyRequest(verify.toString());
                        Toast.makeText(MineSetting.this, "设置成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        ////Toast.makeText(MineSetting.this, response.getString(""), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MineSetting.this, responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void calculateCacheSize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = 0;
                File[] cachedFiles = MyApplication.cache_file.listFiles();
                if (cachedFiles != null) {
                    for (File cachedFile : cachedFiles) {
                        size += cachedFile.length();
                    }

                    tv_cache.setText(size + "KB");
                }
            }
        }).start();


        Long file_size = MyApplication.cache_file.length();
        tv_cache.setText((file_size) + "KB");
    }
}
