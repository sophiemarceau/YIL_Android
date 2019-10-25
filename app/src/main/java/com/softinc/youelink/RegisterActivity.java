package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.config.GlobalData;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.SPUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";

    private EditText et_phone_number;
    private Button bt_get_identify_code;
    private EditText et_identify_code;
    private EditText et_password;
    private ImageView iv_back_to_login;
    private Button bt_login;
    private TextView tv_user_protocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_phone_number = (EditText) this.findViewById(R.id.et_phone_number);
        bt_get_identify_code = (Button) this.findViewById(R.id.bt_get_identyfy_code);
        et_identify_code = (EditText) this.findViewById(R.id.et_identify_code);
        et_password = (EditText) this.findViewById(R.id.et_password);
        iv_back_to_login = (ImageView) this.findViewById(R.id.iv_back_to_login);
        bt_login = (Button) this.findViewById(R.id.bt_login);
        tv_user_protocol = (TextView) this.findViewById(R.id.tv_user_protocol);

        bt_get_identify_code.setOnClickListener(this);
        bt_login.setOnClickListener(this);
        iv_back_to_login.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == bt_get_identify_code) {
            //点击获取验证码
            String phoneNumber = et_phone_number.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                PromptUtils.showToast(this, "电话号码不能为空");
                return;
            }

            RequestParams requestParams = new RequestParams();
            requestParams.put("phoneNumber", phoneNumber);

            MyApplication.client.post(Constant.URL_BASE + "/Account/createVerifyCode", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    PromptUtils.showToast(RegisterActivity.this, "验证码已发送，注意查收！");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PromptUtils.showNoNetWork(RegisterActivity.this);
                }
            });
        } else if (v == bt_login) {
            //点击登录
            String identifyCode = et_identify_code.getText().toString();
            String password = et_password.getText().toString();
            final String phoneNumber = et_phone_number.getText().toString();

            if (TextUtils.isEmpty(identifyCode) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phoneNumber)) {
                PromptUtils.showToast(this, "手机号、验证码或密码不能为空");
                return;
            }

            if (password.length() < 6) {
                PromptUtils.showToast(this, "密码长度不能小于6位");
                return;
            }

            RequestParams params = new RequestParams();
            params.put("phoneNumber", phoneNumber);
            params.put("code", identifyCode);
            params.put("password", password);
            MyApplication.client.post(Constant.URL_REGISTER, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (ResponseUtils.isResultOK(response)) {
                        SPUtils.putString(RegisterActivity.this, Constant.SP_PHONE_NUMBER, phoneNumber);
                        //登录
                        try {
                            JSONObject dataObj = response.getJSONObject("Data");
                            String token = dataObj.getString("token").toString();
                            JSONObject userInfoObj = dataObj.getJSONObject("UserInfo");
                            String uid = userInfoObj.getString("UID");
                            GlobalData.currentUser.id = uid;

                            MyApplication.client.addHeader("Authentication", token);//添加登录令牌
                            startActivity(new Intent(RegisterActivity.this, CompleteUserInfo1Activity.class));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        PromptUtils.showToast(getApplicationContext(), ResponseUtils.getInformation(response));
                        return;
                    }
                }

            });
        } else if (v == iv_back_to_login) {//返回登录
            this.finish();
        }
    }
}
