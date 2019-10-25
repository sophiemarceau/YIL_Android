package com.softinc.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreference 读写工具类
 * Created by zhangbing on 15-3-30.
 */
public class SPUtils {

    private static final String SP_NAME = "SP_DEFAULT";
    private static final String DEFAULT_VALUE = "null";

    /**
     * 写入一个String键值对
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putString(Context context, String key, String value) {
        //获取SharedPreferences对象
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 读出一个String值,如果为空返回的是"null"
     *
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, DEFAULT_VALUE);
    }

}
