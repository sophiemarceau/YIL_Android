package com.softinc.utils;

import android.content.Context;
import android.widget.ImageView;

import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.youelink.R;
import com.squareup.picasso.Picasso;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     *
     * @param username
     * @return
     */
    public static User getUserInfo(String username) {
        User user = MyApplication.getInstance().getContactList().get(username);
        if (user == null) {
            user = new User(username);
        }

        if (user != null) {
            //demo没有这些数据，临时填充
            user.setNick(username);
//            user.setAvatar("http://downloads.easemob.com/downloads/57.png");
        }
        return user;
    }

    /**
     * 设置用户头像
     *
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView) {
//        User user = getUserInfo(username);
        if (username != null) {
            Picasso.with(context).load(username).placeholder(R.drawable.default_avatar).into(imageView);
        } else {
            Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
        }
    }

}
