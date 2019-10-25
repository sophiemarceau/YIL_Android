package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.google.gson.Gson;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.ResponseUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.softinc.config.Constant;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.SPUtils;

public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText et_phone_number;
    private EditText et_password;
    private Button bt_login;
    private TextView tv_regist;
    private TextView tv_forget_password;
    private boolean progressShow;

    private String currentUsername;
    private String currentPassword;
    private ProgressDialog pd;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    @Override
    public void onClick(View v) {
        if (v == bt_login) {
            login();
        } else if (v == tv_regist) {
            //注册
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (v == tv_forget_password) {
            startActivity(new Intent(this, ForgetPasswordActivity.class));
        }
    }

    private void initView() {
        et_phone_number = (EditText) this.findViewById(R.id.et_phone_number);
        et_password = (EditText) this.findViewById(R.id.et_password);
        bt_login = (Button) this.findViewById(R.id.bt_login);
        tv_regist = (TextView) this.findViewById(R.id.tv_regist);
        tv_forget_password = (TextView) this.findViewById(R.id.tv_forget_password);

        bt_login.setOnClickListener(this);
        tv_regist.setOnClickListener(this);
        tv_forget_password.setOnClickListener(this);
    }

    private void login() {
        final String phoneNumber = et_phone_number.getText().toString();
        final String password = et_password.getText().toString();

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            PromptUtils.showToast(this, "用户名和密码不能为空");
            return;
        }

        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_USER + phoneNumber);
            if (obj == null) {
                return;
            }

            initUserNoNet(phoneNumber);
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();

        } else {
            RequestParams params = new RequestParams();
            params.put("phoneNumber", phoneNumber);
            params.put("password", password);
            MyApplication.client.post(Constant.URL_LOGIN, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (ResponseUtils.isResultOK(response)) {
                        PromptUtils.showToast(LoginActivity.this, "登录成功");

                        if (!existCacheUser(phoneNumber, password)) {
                            cacheUserAccount(phoneNumber, password);
                        }

                        cacheLastUserAccount(phoneNumber, password);

                        try {
                            JSONObject dataObj = response.getJSONObject("Data");
                            String token = dataObj.getString("token").toString();
                            JSONObject userInfoObj = dataObj.getJSONObject("UserInfo");

                            currentPassword = userInfoObj.getString("hxPass");
                            currentUsername = userInfoObj.getString("hxUser");

                            //保存当前用户Id
                            //GlobalData.currentUser.id = userInfoObj.getString("UID");

                            MyApplication.uid = userInfoObj.getString("UID");
                            MyApplication.phone = phoneNumber;

                            getCurrentUser(MyApplication.uid);

                            //保存Token到SP
                            SPUtils.putString(LoginActivity.this, Constant.SP_TOKEN, token);

                            String nickName = userInfoObj.getString("NickName");

//                        if (TextUtils.isEmpty(nickName)) {
//                            PromptUtils.showToast(getApplicationContext(), "您还没有设置昵称");
//                            startActivity(new Intent(LoginActivity.this, CompleteUserInfo1Activity.class));
//                        }
                            MyApplication.currentUserNick = nickName;
                            MyApplication.client.addHeader("Authentication", token);//添加登录令牌

//                        DemoApplication.currentUserNick = data.getStringExtra("edittext");

                            progressShow = true;
                            pd = new ProgressDialog(LoginActivity.this);
                            pd.setCanceledOnTouchOutside(false);
                            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    progressShow = false;
                                }
                            });
                            pd.setMessage(getString(R.string.Is_landing));
                            pd.show();

//                        final long start = System.currentTimeMillis();

                            EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {//回调
                                @Override
                                public void onSuccess() {
                                    if (!progressShow) {
                                        return;
                                    }
                                    // 登陆成功，保存用户名密码
                                    MyApplication.getInstance().setUserName(currentUsername);
                                    MyApplication.getInstance().setPassword(currentPassword);

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.setMessage(getString(R.string.list_is_for));
//                                        EMGroupManager.getInstance().loadAllGroups();
//                                        EMChatManager.getInstance().loadAllConversations();
//                                        Log.d("main", "登陆聊天服务器成功！");
                                        }
                                    });
                                    try {
                                        // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                                        // ** manually load all local groups and
                                        // conversations in case we are auto login
                                        EMGroupManager.getInstance().loadAllGroups();
                                        EMChatManager.getInstance().loadAllConversations();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        //取好友或者群聊失败，不让进入主页面
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                pd.dismiss();
                                                MyApplication.getInstance().logout(null);
                                                Toast.makeText(getApplicationContext(), R.string.login_failure_failed, 1).show();
                                            }
                                        });
                                        return;
                                    }
                                    if (!LoginActivity.this.isFinishing())
                                        pd.dismiss();
                                    // 进入主页
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }

                                @Override
                                public void onProgress(int progress, String status) {
                                }

                                @Override
                                public void onError(final int code, final String message) {
                                    if (!progressShow) {
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }


                            });

                        } catch (JSONException e) {
                            PromptUtils.showErrorDialog(LoginActivity.this, e.getMessage());
                            e.printStackTrace();
                        }

                    } else {
                        PromptUtils.showErrorDialog(LoginActivity.this, ResponseUtils.getInformation(response));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PromptUtils.showNoNetWork(LoginActivity.this);
                }
            });
        }
    }

    private void getCurrentUser(String uid) {
        RequestParams params = new RequestParams();
        params.put("uid", uid);

        MyApplication.client.get(Constant.URL_GET_USER_BY_ID, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject userObj = response.getJSONObject("Data");

                    MyApplication.user.iconPath = "http://123.57.217.223/youelink/upload/userpic/" + userObj.getString("UserPic");
                    MyApplication.user.setNick(userObj.getString("NickName"));
                    MyApplication.user.id = userObj.getString("UID");
                    MyApplication.user.setUserLevel(userObj.getString("UserLevel"));
                    MyApplication.user.CreditPoint = userObj.getString("CreditPoint");
                    MyApplication.user.setBrithday(userObj.getString("Brithday"));
                    MyApplication.user.setJobId(userObj.getString("JobID"));
                    MyApplication.user.setJobName(userObj.getString("JobName"));
                    MyApplication.user.setAreaId(userObj.getString("AreaID"));
                    MyApplication.user.setAreaName(userObj.getString("AreaName"));
                    MyApplication.user.setGender(userObj.getString("Gender"));
                    MyApplication.user.setBuddyRequest(userObj.getString("BuddyRequest"));
                    MyApplication.user.setMail(userObj.getString("EMail"));
                    MyApplication.user.setHobbies(userObj.getString("Hobbies"));
                    MyApplication.user.setPhoneNumber(et_phone_number.getText().toString());

                    cacheUser(MyApplication.user, et_phone_number.getText().toString());
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

    private Boolean existCacheUser(String phone, String pwd) {
        String p = MyApplication.cacheManager.getAsString(Constant.CACHE_USER_ACCOUNT + phone);
        if (p != null && !p.isEmpty() && p.equals(Constant.CACHE_USER_PWD + pwd)) {
            return true;
        }

        return false;
    }

    private void cacheUserAccount(String account, String pwd) {
        MyApplication.cacheManager.put(Constant.CACHE_USER_ACCOUNT + account, Constant.CACHE_USER_PWD + pwd);

    }

    private void cacheLastUserAccount(String account, String pwd) {
        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_LAST_USER_ACCOUNT);
        if (obj != null) {
            return;
        }
        MyApplication.cacheManager.put(Constant.CACHE_LAST_USER_ACCOUNT, account);
        MyApplication.cacheManager.put(Constant.CACHE_LAST_USER_PWD, pwd);
    }

    private void cacheUser(User user, String account) {
        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_USER + account);
        if (obj == null) {
            MyApplication.cacheManager.put(Constant.CACHE_USER + account, gson.toJson(user));
        }
    }

    private void initUserNoNet(String account) {
        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_USER + account);
        User user = gson.fromJson(obj, User.class);
        if (user != null) {
            MyApplication.uid = user.id;
            MyApplication.user = user;
        }
    }
}
