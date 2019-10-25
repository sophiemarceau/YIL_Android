package com.softinc.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 服务器返回数据解析工具
 * Created by zhangbing on 15-3-30.
 */
public class ResponseUtils {
    public static final String RESULT = "Result";
    public static final String INFORMATION = "Infomation";
    public static final String DATA = "Data";

    public static boolean isResultOK(JSONObject response) {
        try {
            String result = response.get(RESULT).toString();
            if (result.equals("true")) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String getInformation(JSONObject response) {
        try {
            return response.get(INFORMATION).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
