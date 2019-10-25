package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.SPUtils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends Activity {
    private String currentUsername;
    private String currentPassword;

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        new CountDownTimer(500, 500) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();

        initNavigate();
    }

    private void initNavigate() {
        try {
            String last_user_account = MyApplication.cacheManager.getAsString(Constant.CACHE_LAST_USER_ACCOUNT);
            String last_user_pwd = MyApplication.cacheManager.getAsString(Constant.CACHE_LAST_USER_PWD);
            if (!TextUtils.isEmpty(last_user_account) && !TextUtils.isEmpty(last_user_pwd)) {
                this.autoLogin(last_user_account, last_user_pwd);
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void autoLogin(String pn, String pw) {
        final String phoneNumber = pn;
        final String password = pw;

        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();

            initUserNoNet(phoneNumber);
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        } else {
            RequestParams params = new RequestParams();
            params.put("phoneNumber", phoneNumber);
            params.put("password", password);
            MyApplication.client.post(Constant.URL_LOGIN, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (ResponseUtils.isResultOK(response)) {
                        if (!existCacheUser(phoneNumber, password)) {
                            cacheUserAccount(phoneNumber, password);
                        }

                        try {
                            JSONObject dataObj = response.getJSONObject("Data");
                            String token = dataObj.getString("token").toString();
                            JSONObject userInfoObj = dataObj.getJSONObject("UserInfo");

                            currentPassword = userInfoObj.getString("hxPass");
                            currentUsername = userInfoObj.getString("hxUser");


                            MyApplication.uid = userInfoObj.getString("UID");

                            getCurrentUser(MyApplication.uid, phoneNumber);

                            //保存Token到SP
                            SPUtils.putString(SplashActivity.this, Constant.SP_TOKEN, token);

                            String nickName = userInfoObj.getString("NickName");

                            MyApplication.currentUserNick = nickName;
                            MyApplication.client.addHeader("Authentication", token);//添加登录令牌

                            EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {//回调
                                @Override
                                public void onSuccess() {
                                    // 登陆成功，保存用户名密码
                                    MyApplication.getInstance().setUserName(currentUsername);
                                    MyApplication.getInstance().setPassword(currentPassword);

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
                                                MyApplication.getInstance().logout(null);
                                                Toast.makeText(getApplicationContext(), R.string.login_failure_failed, 1).show();
                                            }
                                        });
                                        return;
                                    }

                                    // 进入主页
                                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                                    finish();
                                }

                                @Override
                                public void onProgress(int progress, String status) {
                                }

                                @Override
                                public void onError(final int code, final String message) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }


                            });

                        } catch (JSONException e) {
                            PromptUtils.showErrorDialog(SplashActivity.this, e.getMessage());
                            e.printStackTrace();
                        }

                    } else {
                        PromptUtils.showErrorDialog(SplashActivity.this, ResponseUtils.getInformation(response));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PromptUtils.showNoNetWork(SplashActivity.this);
                }
            });
        }
    }

    private void getCurrentUser(String uid, String pn) {
        final String phoneNumber = pn;

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

                    cacheUser(MyApplication.user, phoneNumber);
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

    private void cacheUser(User user, String account) {
        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_USER + account);
        if (obj == null) {
            MyApplication.cacheManager.put(Constant.CACHE_USER + account, gson.toJson(user));
        }
    }

    private void cacheUserAccount(String account, String pwd) {
        MyApplication.cacheManager.put(Constant.CACHE_USER_ACCOUNT + account, Constant.CACHE_USER_PWD + pwd);
        MyApplication.cacheManager.put(Constant.CACHE_LAST_USER_ACCOUNT, account);
        MyApplication.cacheManager.put(Constant.CACHE_LAST_USER_PWD, pwd);
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
