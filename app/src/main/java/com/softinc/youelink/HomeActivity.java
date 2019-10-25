package com.softinc.youelink;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMNotifier;
import com.easemob.chat.GroupChangeListener;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.easemob.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.softinc.DemoHXSDKHelper;
import com.softinc.applib.controller.HXSDKHelper;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.InviteMessgeDao;
import com.softinc.db.UserDao;
import com.softinc.domain.InviteMessage;
import com.softinc.fragment.ContactsFragment;
import com.softinc.fragment.FindFragment;
import com.softinc.fragment.MineFragment;
import com.softinc.fragment.MsgFragment;
import com.softinc.fragment.DynamicFragment;

import com.softinc.utils.CommonUtils;
import com.softinc.youelink.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends FragmentActivity implements View.OnClickListener, EMEventListener {
    private static final String TAG = "HomeActivity";
    // ===============================================================================
    // UI
    // ===============================================================================
    private ViewPager pager;
    private ImageView iv_nav1;
    private ImageView iv_nav2;
    private ImageView iv_nav3;
    private ImageView iv_nav4;
    private ImageView iv_nav5;

    //5个Tab,全部使用Fragment填充
    private DynamicFragment dynamicFragment;//动态
    private ContactsFragment contactsFragment;//通讯录
    private FindFragment findFragment;//发现
    private MsgFragment msgFragment;//消息
    private MineFragment mineFragment;//我

    private ArrayList<Fragment> fragmentList;

    // 未读消息textview
    private TextView unreadLabel;
    // 未读通讯录textview
    private TextView unreadAddressLable;

    private TextView contact_unreadLabel;
    // 账号在别处登录
    public boolean isConflict = false;

    //账号被移除
    private boolean isCurrentAccountRemoved = false;

    private android.app.AlertDialog.Builder conflictBuilder;
    private android.app.AlertDialog.Builder accountRemovedBuilder;
    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;
    // ===============================================================================
    // 变量
    // ===============================================================================

    /**
     * 当前选中的位置  从0开始
     */
    private int currentTab;
    private ImageView indicators[];
    private int indicatorSelectedIcons[] = {
            R.drawable.nav1_select,
            R.drawable.nav2_select,
            R.drawable.nav5_select,
            R.drawable.nav3_select,
            R.drawable.nav4_select
    };
    private int indicatorUnSelectedIcons[] = {
            R.drawable.nav1,
            R.drawable.nav2,
            R.drawable.nav5,
            R.drawable.nav3,
            R.drawable.nav4
    };


    private MyConnectionListener connectionListener = null;
    private MyGroupChangeListener groupChangeListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED, false)) {
            // 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            MyApplication.getInstance().logout(null);
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)) {
            // 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }


        setContentView(R.layout.activity_home);

        // ===============================================================================
        // initView
        // ===============================================================================
        pager = (ViewPager) findViewById(R.id.pager);
        iv_nav1 = (ImageView) findViewById(R.id.iv_nav1);
        iv_nav2 = (ImageView) findViewById(R.id.iv_nav2);
        iv_nav3 = (ImageView) findViewById(R.id.iv_nav3);
        iv_nav4 = (ImageView) findViewById(R.id.iv_nav4);
        iv_nav5 = (ImageView) findViewById(R.id.iv_nav5);

        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);


        unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);


        inviteMessgeDao = new InviteMessgeDao(this);
        userDao = new UserDao(this);

        dynamicFragment = new DynamicFragment();
        contactsFragment = new ContactsFragment();
        msgFragment = new MsgFragment();
        findFragment = new FindFragment();
        mineFragment = new MineFragment();
        fragmentList = new ArrayList<Fragment>();

        fragmentList.add(dynamicFragment);
        fragmentList.add(contactsFragment);
        fragmentList.add(findFragment);
        fragmentList.add(msgFragment);
        fragmentList.add(mineFragment);

        iv_nav1.setOnClickListener(this);
        iv_nav2.setOnClickListener(this);
        iv_nav3.setOnClickListener(this);
        iv_nav4.setOnClickListener(this);
        iv_nav5.setOnClickListener(this);

        pager.setAdapter(new MPagerAdapter(getSupportFragmentManager(), fragmentList));
        pager.setCurrentItem(0);//设置当前显示标签页为第一页
//


        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                Log.e(TAG, "home----onPageSelected:" + position);
                currentTab = position;
                setIndicatorSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        pager.setOffscreenPageLimit(4);
        indicators = new ImageView[]{iv_nav1, iv_nav2, iv_nav3, iv_nav4, iv_nav5};
        setIndicatorSelected(pager.getCurrentItem());


        if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }


//
//        // setContactListener监听联系人的变化等
//        EMContactManager.getInstance().setContactListener(new MyContactListener());
//        // 注册一个监听连接状态的listener
//        EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
//        // 注册群聊相关的listener
//        EMGroupManager.getInstance().addGroupChangeListener(new MyGroupChangeListener());
//        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
//       EMChat.getInstance().setAppInited();


        init();
    }

    private void init() {
        // setContactListener监听联系人的变化等
        EMContactManager.getInstance().setContactListener(new MyContactListener());
        // 注册一个监听连接状态的listener

        connectionListener = new MyConnectionListener();
        EMChatManager.getInstance().addConnectionListener(connectionListener);

        groupChangeListener = new MyGroupChangeListener();
        // 注册群聊相关的listener
        EMGroupManager.getInstance().addGroupChangeListener(groupChangeListener);


        //内部测试方法，请忽略
//        registerInternalDebugReceiver();
    }

    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        MyApplication.getInstance().logout(null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!HomeActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (conflictBuilder == null)
                    conflictBuilder = new android.app.AlertDialog.Builder(HomeActivity.this);
                conflictBuilder.setTitle(st);
                conflictBuilder.setMessage(R.string.connect_conflict);
                conflictBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        conflictBuilder = null;
                        finish();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    }
                });
                conflictBuilder.setCancelable(false);
                conflictBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }

        }

    }


    @Override
    protected void onStop() {
        EMChatManager.getInstance().unregisterEventListener(this);
        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
        sdkHelper.popActivity(this);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }

    /**
     * 帐号被移除的dialog
     */
    private void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        MyApplication.getInstance().logout(null);
        String st5 = getResources().getString(R.string.Remove_the_notification);
        if (!HomeActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (accountRemovedBuilder == null)
                    accountRemovedBuilder = new android.app.AlertDialog.Builder(HomeActivity.this);
                accountRemovedBuilder.setTitle(st5);
                accountRemovedBuilder.setMessage(R.string.em_user_remove);
                accountRemovedBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        accountRemovedBuilder = null;
                        finish();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    }
                });
                accountRemovedBuilder.setCancelable(false);
                accountRemovedBuilder.create().show();
                isCurrentAccountRemoved = true;
            } catch (Exception e) {
                EMLog.e(TAG, "---------color userRemovedBuilder error" + e.getMessage());
            }

        }

    }

    /**
     * 设置底部选中
     *
     * @param position
     */
    private void setIndicatorSelected(int position) {
        Log.i(TAG, "当前选中:" + position);
        for (int i = 0; i < indicators.length; i++) {
            if (position == i) {
                //选中
                indicators[i].setImageResource(indicatorSelectedIcons[i]);
            } else {
                //没选中
                indicators[i].setImageResource(indicatorUnSelectedIcons[i]);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");
        switch (v.getId()) {
            case R.id.iv_nav1:
                pager.setCurrentItem(0, true);
                break;
            case R.id.iv_nav2:
                pager.setCurrentItem(1, true);
                break;

            case R.id.iv_nav3:

                pager.setCurrentItem(2, true);
                break;
            case R.id.iv_nav4:

                pager.setCurrentItem(3, true);
                break;
            case R.id.iv_nav5:

                pager.setCurrentItem(4, true);
                break;
        }
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: //普通消息
            {
                EMMessage message = (EMMessage) event.getData();

                //提示新消息
                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);

                refreshUI();
                break;
            }

            case EventOfflineMessage: {
                refreshUI();
                break;
            }
//            case EventConversationListChanged: {
////                Log.e(TAG,"EventConversationListChanged");
//                refreshUI();
//                break;
//            }
            default:
                break;
        }
    }

    private void refreshUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                // 刷新bottom bar消息未读数
                updateUnreadLabel();

                // 当前页面如果为聊天历史页面，刷新此页面
                if (msgFragment != null) {
                    msgFragment.refreshMsgFragment();
                }

            }
        });
    }


    /**
     * viewPager适配器
     */
    private class MPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public MPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {

            super(fm);
            this.list = list;

        }

        @Override
        public Fragment getItem(int position) {
            Log.i(TAG, "getItem");
            if (position == 0) {//动态

                return dynamicFragment;
            } else if (position == 1) {//通讯录

                return contactsFragment;
            } else if (position == 2) {//发现
                return findFragment;
            } else if (position == 3) {
                return msgFragment;
            } else if (position == 4) {
                return mineFragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    /**
     * 获取未读消息数
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        Log.e(TAG, "getUnreadMsgCountTotal");
        int unreadMsgCountTotal = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        return unreadMsgCountTotal;
    }


    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        Log.e(TAG, "updateUnreadLabel");
        int count = getUnreadMsgCountTotal();
//        Log.e(TAG, "updateUnreadLabel--------count-------->"+count);
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 刷新申请与通知消息数
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
//                Log.e(TAG, "updateUnreadAddressLable--刷新申请与通知消息数------count-------->"+count);
                if (count > 0) {
                    unreadAddressLable.setText(String.valueOf(count));
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                } else {
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    /**
     * 获取未读申请与通知消息
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;

        if (MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME) != null)
            unreadAddressCountTotal = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME).getUnreadMsgCount();
        return unreadAddressCountTotal;
    }

    /**
     * 检查当前用户是否被删除
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment f = fragmentList.get(pager.getCurrentItem());
        f.onActivityResult(requestCode, resultCode, data);
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        if (!isConflict && !isCurrentAccountRemoved) {
            updateUnreadLabel();
            updateUnreadAddressLable();
            EMChatManager.getInstance().activityResumed();
        }

        // unregister this event listener when this activity enters the background
        DemoHXSDKHelper sdkHelper = (DemoHXSDKHelper) DemoHXSDKHelper.getInstance();
        sdkHelper.pushActivity(this);

        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(this, new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage});
    }


    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;

    /***
     * 好友变化listener
     */
    private class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(List<String> usernameList) {

//            Log.e(TAG, "onContactAdded－－－－－－－－－－－－－－－－－－－－－－－－－－－－－" +
//                    "－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－usernameList－－－－－－－－－－－－>"+usernameList);
            // 保存增加的联系人
            Map<String, User> localUsers = MyApplication.getInstance().getContactList();
            Map<String, User> toAddUsers = new HashMap<String, User>();
            for (String username : usernameList) {
                User user = setUserHead(username);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    userDao.saveContact(user);
                }
                toAddUsers.put(username, user);
            }
            localUsers.putAll(toAddUsers);
            // 刷新ui
            if (currentTab == 1)
                contactsFragment.refresh();

        }

        @Override
        public void onContactDeleted(final List<String> usernameList) {
//            Log.e(TAG, "被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除被删除");
            // 被删除
            Map<String, User> localUsers = MyApplication.getInstance().getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                userDao.deleteContact(username);
                inviteMessgeDao.deleteMessage(username);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    // 如果正在与此用户的聊天页面
                    String st10 = getResources().getString(R.string.have_you_removed);
                    if (ChatActivity.activityInstance != null && usernameList.contains(ChatActivity.activityInstance.getToChatUsername())) {
                        Toast.makeText(HomeActivity.this, ChatActivity.activityInstance.getToChatUsername() + st10, 2).show();
                        ChatActivity.activityInstance.finish();
                    }
                    updateUnreadLabel();
                    // 刷新ui
                    contactsFragment.refresh();
                    msgFragment.refreshMsgFragment();
                }
            });

        }

        @Override
        public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            Log.e(TAG, "onContactInvited－－－－－－－－－onContactInvited －－－－－－－－－－－－－－－－－－－－－－－－－请求加你为好友－－－－－－－>");
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();

            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.e(TAG, username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMessage.InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            Log.d(TAG, username + "同意了你的好友请求");
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);

        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.d(username, username + "拒绝了你的好友请求");
        }

    }

    /**
     * 连接监听listener
     */
    private class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {
            boolean groupSynced = HXSDKHelper.getInstance().isGroupsSyncedWithServer();
            boolean contactSynced = HXSDKHelper.getInstance().isContactsSyncedWithServer();

            // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
            if (groupSynced && contactSynced) {
                new Thread() {
                    @Override
                    public void run() {
                        HXSDKHelper.getInstance().notifyForRecevingEvents();
                    }
                }.start();
            } else {
                if (!groupSynced) {
                    asyncFetchGroupsFromServer();
                }

                if (!contactSynced) {
                    asyncFetchContactsFromServer();
                }

//                if(!HXSDKHelper.getInstance().isBlackListSyncedWithServer()){
//                    asyncFetchBlackListFromServer();
//                }
            }
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (currentTab == 3) {
                        msgFragment.errorItem.setVisibility(View.GONE);

                    }
//
                }

            });
        }

        @Override
        public void onDisconnected(final int error) {
            final String st1 = getResources().getString(R.string.Less_than_chat_server_connection);
            final String st2 = getResources().getString(R.string.the_current_network);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        showAccountRemovedDialog();
                    } else if (error == EMError.CONNECTION_CONFLICT) {
                        // 显示帐号在其他设备登陆dialog
                        showConflictDialog();
                    } else {
                        if (currentTab == 3) {
                            msgFragment.errorItem.setVisibility(View.VISIBLE);
                            if (NetUtils.hasNetwork(HomeActivity.this))
                                msgFragment.errorText.setText(st1);
                            else
                                msgFragment.errorText.setText(st2);
                        }

                    }
                }

            });
        }
    }

    /**
     * MyGroupChangeListener
     */
    private class MyGroupChangeListener implements GroupChangeListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, final String inviter, String reason) {
            if (!CommonUtils.isNetWorkConnected(HomeActivity.this)) {
                Toast.makeText(HomeActivity.this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean hasGroup = false;
            for (EMGroup group : EMGroupManager.getInstance().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
                return;

            // 被邀请
            String st3 = getResources().getString(R.string.Invite_you_to_join_a_group_chat);
            final EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());


            MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        List<User> userlistFromServer = new ArrayList<User>();
                        userlistFromServer.clear();
                        JSONArray userAry = response.getJSONArray("Data");
                        for (int i = 0; i < userAry.length(); i++) {
                            JSONObject userObj = (JSONObject) userAry.get(i);
                            User user = new User();

                            user.setId(userObj.getString("UID"));
                            user.setNick(userObj.getString("NickName"));
                            user.setUsername(userObj.getString("hxUser"));
                            user.setAvatar(userObj.getString("UserPic"));

                            userlistFromServer.add(user);
                        }
                        if (userlistFromServer.size() > 0) {
                            for (User user : userlistFromServer) {

                                if (inviter.equals(user.getUsername())) {

                                    msg.addBody(new TextMessageBody(user.getNick() + "邀请你加入了群聊"));

                                    // 保存邀请消息
                                    EMChatManager.getInstance().saveMessage(msg);
                                    // 提醒新消息
                                    EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            updateUnreadLabel();
                                            // 刷新ui
                                            if (currentTab == 3)
                                                msgFragment.refreshMsgFragment();
                                            if (CommonUtils.getTopActivity(HomeActivity.this).equals(GroupsActivity.class.getName())) {
                                                GroupsActivity.instance.onResume();
                                            }
                                        }
                                    });
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }

        @Override
        public void onInvitationAccpted(String groupId, String inviter, String reason) {

        }

        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {

        }

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            // 提示用户被T了，demo省略此步骤
            // 刷新ui

//            Log.e(TAG,"onUserRemoved--------onUserRemovedonUserRemovedonUserRemovedonUserRemovedonUserRemovedonUserRemovedonUserRemoved---->");


            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        updateUnreadLabel();
                        if (currentTab == 3)
                            msgFragment.refreshMsgFragment();
                        if (CommonUtils.getTopActivity(HomeActivity.this).equals(GroupsActivity.class.getName())) {
                            GroupsActivity.instance.onResume();
                        }
                    } catch (Exception e) {
                        EMLog.e(TAG, "refresh exception " + e.getMessage());
                    }
                }
            });
        }

        @Override
        public void onGroupDestroy(String groupId, String groupName) {
            // 群被解散
            // 提示用户群被解散,demo省略
            // 刷新ui
            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    if (currentTab == 3)
                        msgFragment.refreshMsgFragment();
                    if (CommonUtils.getTopActivity(HomeActivity.this).equals(GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            });

        }

        @Override
        public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {
            // 用户申请加入群聊
            InviteMessage msg = new InviteMessage();
            msg.setFrom(applyer);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            Log.d(TAG, applyer + " 申请加入群聊：" + groupName);
            msg.setStatus(InviteMessage.InviteMesageStatus.BEAPPLYED);
            notifyNewIviteMessage(msg);
        }

        @Override
        public void onApplicationAccept(String groupId, String groupName, String accepter) {
            String st4 = getResources().getString(R.string.Agreed_to_your_group_chat_application);
            // 加群申请被同意
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new TextMessageBody(accepter + st4));
            // 保存同意消息
            EMChatManager.getInstance().saveMessage(msg);
            // 提醒新消息
            EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
                    if (currentTab == 0)
                        msgFragment.refreshMsgFragment();
                    if (CommonUtils.getTopActivity(HomeActivity.this).equals(GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            });
        }

        @Override
        public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
            // 加群申请被拒绝，demo未实现
        }

    }

    /**
     * 保存提示新消息
     *
     * @param msg
     */
    private void notifyNewIviteMessage(InviteMessage msg) {
        Log.e(TAG, "invite----notifyNewIviteMessage--->"+msg);
        saveInviteMsg(msg);
        // 提示有新消息
        EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

        // 刷新bottom bar消息未读数
        updateUnreadAddressLable();
        // 刷新好友页面ui
        if (currentTab == 1)
            contactsFragment.refresh();
    }

    /**
     * 保存邀请等msg
     *
     * @param msg
     */
    private void saveInviteMsg(InviteMessage msg) {
        // 保存msg
        inviteMessgeDao.saveMessage(msg);
        // 未读数加1
        User user = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);

        if (user.getUnreadMsgCount() == 0) {
            user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
        }

//        Log.e(TAG,"user-----user.getUnreadMsgCount()------->"+user.getUnreadMsgCount());


    }

    /**
     * set head
     *
     * @param username
     * @return
     */
    User setUserHead(String username) {
        User user = new User();
        user.setUsername(username);
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1).toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
        return user;
    }

    static void asyncFetchGroupsFromServer() {
        HXSDKHelper.getInstance().asyncFetchGroupsFromServer(new EMCallBack() {

            @Override
            public void onSuccess() {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(true);

                if (HXSDKHelper.getInstance().isContactsSyncedWithServer()) {
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }
            }

            @Override
            public void onError(int code, String message) {
                HXSDKHelper.getInstance().noitifyGroupSyncListeners(false);
            }

            @Override
            public void onProgress(int progress, String status) {

            }

        });
    }

    static void asyncFetchContactsFromServer() {
        HXSDKHelper.getInstance().asyncFetchContactsFromServer(new EMValueCallBack<List<String>>() {

            @Override
            public void onSuccess(List<String> usernames) {
                Context context = HXSDKHelper.getInstance().getAppContext();

                System.out.println("----------------" + usernames.toString());
                EMLog.d("roster", "contacts size: " + usernames.size());
                Map<String, User> userlist = new HashMap<String, User>();
                for (String username : usernames) {
                    User user = new User();
                    user.setUsername(username);
                    setUserHearder(username, user);
                    userlist.put(username, user);
                }
                // 添加user"申请与通知"
                User newFriends = new User();
                newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                String strChat = context.getString(R.string.Application_and_notify);
                newFriends.setNick(strChat);

                userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                // 添加"群聊"
                User groupUser = new User();
                String strGroup = context.getString(R.string.group_chat);
                groupUser.setUsername(Constant.GROUP_USERNAME);
                groupUser.setNick(strGroup);
                groupUser.setHeader("");
                userlist.put(Constant.GROUP_USERNAME, groupUser);

//                // 添加"聊天室"
//                User chatRoomItem = new User();
//                String strChatRoom = context.getString(R.string.chat_room);
//                chatRoomItem.setUsername(Constant.CHAT_ROOM);
//                chatRoomItem.setNick(strChatRoom);
//                chatRoomItem.setHeader("");
//                userlist.put(Constant.CHAT_ROOM, chatRoomItem);
//
//                // 添加"Robot"
//                User robotUser = new User();
//                String strRobot = context.getString(R.string.robot_chat);
//                robotUser.setUsername(Constant.CHAT_ROBOT);
//                robotUser.setNick(strRobot);
//                robotUser.setHeader("");
//                userlist.put(Constant.CHAT_ROBOT, robotUser);

                // 存入内存
                MyApplication.getInstance().setContactList(userlist);
                // 存入db
                UserDao dao = new UserDao(context);
                List<User> users = new ArrayList<User>(userlist.values());
                dao.saveContactList(users);

                HXSDKHelper.getInstance().notifyContactsSyncListener(true);

                if (HXSDKHelper.getInstance().isGroupsSyncedWithServer()) {
                    HXSDKHelper.getInstance().notifyForRecevingEvents();
                }

            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyContactsSyncListener(false);
            }

        });
    }

    static void asyncFetchBlackListFromServer() {
        HXSDKHelper.getInstance().asyncFetchBlackListFromServer(new EMValueCallBack<List<String>>() {

            @Override
            public void onSuccess(List<String> value) {
                EMContactManager.getInstance().saveBlackList(value);
                HXSDKHelper.getInstance().notifyBlackListSyncListener(true);
            }

            @Override
            public void onError(int error, String errorMsg) {
                HXSDKHelper.getInstance().notifyBlackListSyncListener(false);
            }

        });
    }

    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    private static void setUserHearder(String username, User user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            user.setHeader("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setHeader("#");
        } else {
            user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
                    .toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }
}
