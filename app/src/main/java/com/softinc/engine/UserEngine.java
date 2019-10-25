package com.softinc.engine;

import android.content.Context;
import android.util.Log;

import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.HttpHelper;
import com.softinc.utils.PromptUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UserEngine {

    private static final String TAG = "UserEngine";

    /**
     * 获得用户信息
     *
     * @param context
     * @param id
     * @return
     */
    public static User userWithUID(final Context context, final String id) {
        final User user = new User();

        RequestParams params = new RequestParams();
        params.put("uid", id);

        MyApplication.client.get(Constant.URL_GET_USER_BY_ID, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject userObj = response.getJSONObject("Data");

                    user.iconPath = "http://123.57.217.223/youelink/upload/userpic/" + userObj.getString("UserPic");
                    user.setNick(userObj.getString("NickName"));
                    user.id = userObj.getString("UID");
                    user.setCreditPoint(userObj.getString("CreditPoint"));
                    user.setGender(userObj.getString("Gender"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                PromptUtils.showNoNetWork(context);
            }
        });
        return user;
    }

    /**
     * 从服务器获取所有好友
     *
     * @return
     */
    public static List<User> allFriendsFromServer() {
        final List<User> users = new ArrayList<User>();

        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "得到全部朋友" + response);

                try {
                    JSONArray userAry = response.getJSONArray("Data");
                    for (int i = 0; i < userAry.length(); i++) {
                        JSONObject userObj = (JSONObject) userAry.get(i);
                        User user = new User();
                        user.setIconPath(userObj.getString("UserPic"));
                        user.setNick(userObj.getString("NickName"));
                        user.setId(userObj.getString("UID"));
//                        user.setPhoneNumber(userObj.getString("PhoneNumber"));
                        user.setUsername(userObj.getString("hxUser"));
                        user.setAvatar(userObj.getString("UserPic"));

                        users.add(user);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        Log.e("list", "list--------user----->" + users);
        return users;
    }


    public static List<User> contactListFromServer(final Context context) {
        final List<User> users = new ArrayList<User>();

        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.e(TAG, "得到全部朋友" + response);

                try {
                    JSONArray userAry = response.getJSONArray("Data");
                    for (int i = 0; i < userAry.length(); i++) {
                        JSONObject userObj = (JSONObject) userAry.get(i);
                        User user = new User();
                        user.setIconPath(userObj.getString("UserPic"));
                        user.setNick(userObj.getString("NickName"));
                        user.setId(userObj.getString("UID"));
//                        user.setPhoneNumber(userObj.getString("PhoneNumber"));
                        user.setUsername(userObj.getString("hxUser"));
                        user.setAvatar(userObj.getString("UserPic"));

                        users.add(user);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        Log.e("list","list--------user----->"+users);
        return users;
    }
}
