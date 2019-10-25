package com.softinc.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.softinc.DemoHXSDKHelper;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.ACache;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.SPUtils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 自定以Application,做App的初始化工作.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    public static Context applicationContext;
    private static MyApplication instance;
    // login user name
    public final String PREF_USERNAME = "username";

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";
    public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

    public static AsyncHttpClient client = null;
    public static PersistentCookieStore cookies = null;
    public static String uid = "";
    public static String phone = "";

    public static User user = new User();

    public static Integer cache_size = 0;

    public static File cache_file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/YYL");
    public static ACache cacheManager = ACache.get(cache_file);

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;

//        int pid = android.os.Process.myPid();
//        String processAppName = getAppName(pid);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回

//        if (processAppName == null || !processAppName.equalsIgnoreCase("com.softinc.youelink")) {
//            Log.e(TAG, "enter the service process!");
//            //"com.easemob.chatuidemo"为demo的包名，换到自己项目中要改成自己包名
//
//            // 则此application::onCreate 是被service 调用的，直接返回
//            return;
//        }


        //universal image loader 初始化
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);

//        //环信初始化
//        EMChat.getInstance().init(getApplicationContext());
//        EMChat.getInstance().setDebugMode(true);

        //初始化AsyncClient
        client = new AsyncHttpClient();
        //查看是否有token 有的话就带上
        String token = SPUtils.getString(getApplicationContext(), Constant.SP_TOKEN);
        if (token != null && !"null".equals(token)) {
            client.addHeader("Authentication", token);//添加登录令牌
        }
        cookies = new PersistentCookieStore(this);
        client.setCookieStore(cookies);

//        初始化百度地图SDK
//        在使用SDK各组件之前初始化context信息，传入ApplicationContext
//        注意该方法要再setContentView方法之前实现
//        SDKInitializer.initialize(getApplicationContext());

        applicationContext = this;
        instance = this;

        /**
         * this function will initialize the HuanXin SDK
         *
         * @return boolean true if caller can continue to call HuanXin related APIs after calling onInit, otherwise false.
         *
         * 环信初始化SDK帮助函数
         * 返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
         *
         * for example:
         * 例子：
         *
         * public class DemoHXSDKHelper extends HXSDKHelper
         *
         * HXHelper = new DemoHXSDKHelper();
         * if(HXHelper.onInit(context)){
         *     // do HuanXin related work
         * }
         */
        hxSDKHelper.onInit(applicationContext);

    }

    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        return hxSDKHelper.getContactList();
    }

    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        hxSDKHelper.setContactList(contactList);
    }

    /**
     * 获取当前登陆用户名
     *
     * @return
     */
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**
     * 获取密码
     *
     * @return
     */
    public String getPassword() {
        return hxSDKHelper.getPassword();
    }

    /**
     * 设置用户名
     *
     * @param username
     */
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**
     * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
     * 内部的自动登录需要的密码，已经加密存储了
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    /**
     * 退出登录,清空数据
     */
    public void logout(final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(emCallBack);
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}
