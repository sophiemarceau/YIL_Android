package com.softinc.config;



import com.softinc.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局数据,缓存于内存中的数据.全App共享
 * Created by zhangbing on 15-4-1.
 *
 */
public class GlobalData {
    /**
     * 当前登录用户
     */
    public static User currentUser = new User();

    public static List<User> getAllFriends() {
        return allFriends;
    }

    public static void setAllFriends(List<User> allFriends) {
        GlobalData.allFriends = allFriends;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        GlobalData.currentUser = currentUser;
    }

    /**
     * 所有朋友列表

     */
    public static List<User> allFriends = new ArrayList<User>();
}
