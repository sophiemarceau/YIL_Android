package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.utils.ResponseUtils;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private Button bt_next;
    private Title title;
    private EditText et_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initView();
    }

    private void initView() {
        bt_next = generateFindViewById(R.id.bt_next);
        title = generateFindViewById(R.id.tv_name);
        this.et_phone = generateFindViewById(R.id.et_phone);

        bt_next.setOnClickListener(this);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                ForgetPasswordActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == bt_next) {

            RequestParams params = new RequestParams();
            params.put("phoneNumber", this.et_phone.getText().toString());

            MyApplication.client.post(Constant.URL_FORGET_PASSWORD, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (ResponseUtils.isResultOK(response)) {
                        Toast.makeText(ForgetPasswordActivity.this, "密码重置成功！注意查收短信！", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }
}
