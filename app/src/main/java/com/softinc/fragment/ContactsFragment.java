package com.softinc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.adapter.ContactAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.InviteMessgeDao;
import com.softinc.db.UserDao;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.widget.Sidebar;
import com.softinc.youelink.AddContactActivity;
import com.softinc.youelink.ChatActivity;
import com.softinc.youelink.GroupsActivity;
import com.softinc.youelink.HomeActivity;
import com.softinc.youelink.NewFriendsMsgActivity;
import com.softinc.youelink.R;
import com.softinc.youelink.SynFinishActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 主页第二个Tab通讯录  联系人列表
 *
 * Created by zhangbing on 15-2-5.
 */
public class ContactsFragment extends Fragment {

    static final String TAG = "ContactsFragment";

    private ContactAdapter adapter;
    public static List<User> contactList;
    private ListView listView;
    private boolean hidden;
    private Sidebar sidebar;
    private  int newfriendCount;
    private InputMethodManager inputMethodManager;
    private List<String> blackList;
    public  Map<String, User> userlist = new HashMap<String, User>();
    public static  List<User> usersfromServerList = null;
    public static  List<User> fromserverlist =null;
    ImageButton clearSearch;
    EditText query;
    Button syncButton;

    public List<User> getUsersfromServerList() {
        return usersfromServerList;
    }

    public List<User> getContactlist() {
        return contactList;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //搜索框
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        syncButton =(Button)  view.findViewById(R.id.sync_button_contact);
        syncButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SynFinishActivity.class));
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
        if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        listView = (ListView) getView().findViewById(R.id.list);
        sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
        sidebar.setListView(listView);
        newfriendCount = 0;
        //黑名单列表
        blackList = EMContactManager.getInstance().getBlackListUsernames();
        contactList = new ArrayList<User>();
        usersfromServerList = new ArrayList<User>();
        // 获取设置contactlist

        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }


        getContactListFromServer();

        getnewFriendCountFromServer();
    }

    private void initlistView() {
        //搜索框
        query = (EditText) getView().findViewById(R.id.query);
        query.setHint(R.string.search);
        clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);

                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });
        // 设置adapter
        adapter = new ContactAdapter(getActivity(), R.layout.row_contact, contactList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String username = adapter.getItem(position).getUsername();

                if (Constant.NEW_FRIENDS_USERNAME.equals(username)) {
                    // 进入申请与通知页面
                    User user = userlist.get(Constant.NEW_FRIENDS_USERNAME);
                    user.setUnreadMsgCount(0);
                    User  user_NEW_FRIENDS_USERNAME = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
                    user_NEW_FRIENDS_USERNAME.setUnreadMsgCount(0);
                    startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
                } else if (Constant.GROUP_USERNAME.equals(username)) {
                    // 进入群聊列表页面
//                    Log.e(TAG,"username---------->"+username);
                    startActivity(new Intent(getActivity(), GroupsActivity.class));
                } else {
                    Bundle myBundelForName=new Bundle();
                    myBundelForName.putString("nickname",adapter.getItem(position).getNick());
                    myBundelForName.putString("userId", adapter.getItem(position).getUsername());
                    // demo中直接进入聊天页面，实际一般是进入用户详情页
                    startActivity(new Intent(getActivity(), ChatActivity.class).putExtras(myBundelForName));


                }
            }
        });
        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                    if (getActivity().getCurrentFocus() != null)
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        ImageView addContactView = (ImageView) getView().findViewById(R.id.iv_new_contact);
        // 进入添加好友页
        addContactView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });
        registerForContextMenu(listView);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // 长按前两个不弹menu
        if (((AdapterView.AdapterContextMenuInfo) menuInfo).position > 1) {
            getActivity().getMenuInflater().inflate(R.menu.context_contact_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_contact) {
            User tobeDeleteUser = adapter.getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
            // 删除此联系人
            deleteContact(tobeDeleteUser);
            // 删除相关的邀请消息
            InviteMessgeDao dao = new InviteMessgeDao(getActivity());
            dao.deleteMessage(tobeDeleteUser.getUsername());
            return true;
        }
//        else if(item.getItemId() == R.id.add_to_blacklist){
//            User user = adapter.getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
//            moveToBlacklist(user.getUsername());
//            return true;
//        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }

    /**
     * 删除联系人
     */
    public void deleteContact(final User tobeDeleteUser) {
        String st1 = getResources().getString(R.string.deleting);
        final String st2 = getResources().getString(R.string.Delete_failed);
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        try {


            RequestParams params = new RequestParams();
            params.put("buddyId", tobeDeleteUser.getId());
            params.put("buddyHxId",tobeDeleteUser.getUsername());
            MyApplication.client.post(Constant.URL_RemoveBuddy, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    Log.e(TAG, "删除好友返回数据" + response);
                    if (ResponseUtils.isResultOK(response)) {
                        try {
                            EMContactManager.getInstance().deleteContact(tobeDeleteUser.getUsername());
                            // 删除db和内存中此用户的数据
                            UserDao dao = new UserDao(getActivity());
                            dao.deleteContact(tobeDeleteUser.getUsername());
                            MyApplication.getInstance().getContactList().remove(tobeDeleteUser.getUsername());
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    adapter.remove(tobeDeleteUser);
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                    Intent intent =new Intent("android.intent.action.CART_BROADCAST");
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                                }
                            });
                        } catch (Exception e) {
                            PromptUtils.showErrorDialog(getActivity(), e.getMessage());
                            e.printStackTrace();
                            String s2 ="删除好友失败";
                            Toast.makeText(getActivity(), s2 + e.getMessage(), 1).show();
                        }

                    } else {
                        PromptUtils.showErrorDialog(getActivity(), ResponseUtils.getInformation(response));
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.e(TAG, responseString);
                    PromptUtils.showNoNetWork(getActivity());
                }
            });
        } catch (final Exception e) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    pd.dismiss();
                    Toast.makeText(getActivity(), st2 + e.getMessage(), 1).show();
                }
            });

        }

    }

    /**
     * 把user移入到黑名单
     */
    private void moveToBlacklist(final String username){
        final ProgressDialog pd = new ProgressDialog(getActivity());
        String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
        final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
        final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //加入到黑名单
                    EMContactManager.getInstance().addUserToBlackList(username, false);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2, 0).show();
                            refresh();
                        }
                    });
                } catch (EaseMobException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st3, 0).show();
                        }
                    });
                }
            }
        }).start();

    }

    // 刷新ui
    public void refresh() {
        try {
            // 可能会在子线程中调到这方法
            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                    Log.e(TAG,"返回调------------------------------------refreshrefresh------------------------------------------------------------");
                    if (!CommonUtils.isNetWorkConnected(getActivity())) {
                        Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reloadConactListFromServer();

                    getnewFriendCountFromServer();


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processContactsAndGroups() throws EaseMobException {
        // demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
//        List<String> usernames = EMContactManager.getInstance().getContactUserNames();
//        EMLog.d("roster", "contacts size: " + usernames.size());


            for (User user :usersfromServerList){
                    setUserHearder(user.getUsername(),user);
                    userlist.put(user.getUsername(), user);
            }

        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(R.string.Application_and_notify);
        newFriends.setNick(strChat);

        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        User groupUser = new User();
        String strGroup = getResources().getString(R.string.group_chat);
        groupUser.setUsername(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        userlist.put(Constant.GROUP_USERNAME, groupUser);
        MyApplication.getInstance().setContactList(userlist);
        // 存入内存
//        MyApplication.getInstance().setContactList(userlist);
//        // 存入db
//        UserDao dao = new UserDao(getActivity());
//        List<User> users = new ArrayList<User>(userlist.values());
//        dao.saveContactList(users);
//
//        //获取黑名单列表
//        List<String> blackList = EMContactManager.getInstance().getBlackListUsernamesFromServer();
//        //保存黑名单
//        EMContactManager.getInstance().saveBlackList(blackList);
//
//        // 获取群聊列表(群聊里只有groupid和groupname等简单信息，不包含members),sdk会把群组存入到内存和db中
//        EMGroupManager.getInstance().getGroupsFromServer();

        getContactList();

    }

    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     *
     * @param username
     * @param user
     */
    protected void setUserHearder(String username, User user) {
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
    }
    private void reloadConactListFromServer() {

        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                fromserverlist = new ArrayList<User>();
                try {
                    usersfromServerList.clear();
                    JSONArray userAry = response.getJSONArray("Data");
                    for (int i = 0; i < userAry.length(); i++) {
                        JSONObject userObj = (JSONObject) userAry.get(i);
                        User user = new User();

                        user.setId(userObj.getString("UID"));
                        user.setNick(userObj.getString("NickName"));
                        user.setUsername(userObj.getString("hxUser"));
                        user.setAvatar(userObj.getString("UserPic"));

                        usersfromServerList.add(user);
                        fromserverlist.add(user);
                    }

//                    Log.e(TAG,"返回刷新list－－－－－－size－－－－－－－－－－－－－"+usersfromServerList.size());
//                    Log.e(TAG,"返回刷新list－－fromserverlist－－－－－－－－size－－－－－－－－－"+fromserverlist.size());

                    Map<String, User> tempuserlist = new HashMap<String, User>();

                        for (User user :usersfromServerList){
                            setUserHearder(user.getUsername(),user);
                            tempuserlist.put(user.getUsername(), user);
                        }

                        // 添加user"申请与通知"
                        User newFriends = new User();
                        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
                        String strChat = getResources().getString(R.string.Application_and_notify);
                        newFriends.setNick(strChat);

                        tempuserlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
                        // 添加"群聊"
                        User groupUser = new User();
                        String strGroup = getResources().getString(R.string.group_chat);
                        groupUser.setUsername(Constant.GROUP_USERNAME);
                        groupUser.setNick(strGroup);
                        groupUser.setHeader("");
                        tempuserlist.put(Constant.GROUP_USERNAME, groupUser);

                        contactList.clear();
                        Iterator<Map.Entry<String, User>> iterator = tempuserlist.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, User> entry = iterator.next();
                            if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME)
                                    && !blackList.contains(entry.getKey()))
                                contactList.add(entry.getValue());

                        }

//                        Log.e(TAG,"返回刷新list－－fromserverlist－－－－－－－－contactList－－－－before－－－－－"+contactList);
                        // 排序

                        Collections.sort(contactList, new Comparator<User>() {
                            @Override
                            public int compare(User lhs, User rhs) {
                               String pinyin1=  HanziToPinyin.getInstance().get(lhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                                String pinyin2=  HanziToPinyin.getInstance().get(rhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                                return pinyin1.compareTo(pinyin2);
                            }
                        });

//                        Log.e(TAG,"返回刷新list－－fromserverlist－－－－－－－－contactList－－－－after－－－－－"+contactList);
                        // 加入"申请与通知"和"群聊"
                        if(tempuserlist.get(Constant.GROUP_USERNAME) != null)
                            contactList.add(0, tempuserlist.get(Constant.GROUP_USERNAME));
                        // 把"申请与通知"添加到首位
                        if(userlist.get(Constant.NEW_FRIENDS_USERNAME) != null)
                            contactList.add(0, tempuserlist.get(Constant.NEW_FRIENDS_USERNAME));

                        if (contactList.size()>0){
                            if (adapter!=null){
                                adapter.notifyDataSetChanged();
                            }
                        }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getContactListFromServer() {
        usersfromServerList.clear();
        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

                    JSONArray userAry = response.getJSONArray("Data");
//                    Log.e(TAG,"userAry----------->"+userAry.length());
                    for (int i = 0; i < userAry.length(); i++) {
                        JSONObject userObj = (JSONObject) userAry.get(i);
                        User user = new User();

                        user.setId(userObj.getString("UID"));
                        user.setNick(userObj.getString("NickName"));
                        user.setUsername(userObj.getString("hxUser"));
                        user.setAvatar(userObj.getString("UserPic"));

                        usersfromServerList.add(user);
                    }
//                    Log.e(TAG,"usersfromServerList----------->"+usersfromServerList);
                        processContactsAndGroups();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getnewFriendCountFromServer() {
        usersfromServerList.clear();
        MyApplication.client.get(Constant.URL_new_FRIENDSCount, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

                   int unreadCount =response.getInt("Data");

                   newfriendCount =unreadCount;

                    Log.e(TAG,"返回调------------------------------------unreadCount------->" +unreadCount+
                            "------------------------------------------------unreadCount");
                    if (adapter != null) {
                        adapter.addNewFriendCount(newfriendCount);
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }



    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    private void getContactList() {
        contactList.clear();
        //获取本地好友列表
//        Log.e("contactFragment", "users,map---------------->" + userlist);
        Iterator<Map.Entry<String, User>> iterator = userlist.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, User> entry = iterator.next();
            if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME)
                    && !blackList.contains(entry.getKey()))
                contactList.add(entry.getValue());

        }
        // 排序
        Collections.sort(contactList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                String pinyin1=  HanziToPinyin.getInstance().get(lhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                String pinyin2=  HanziToPinyin.getInstance().get(rhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                return pinyin1.compareTo(pinyin2);
            }
        });

        // 加入"申请与通知"和"群聊"
        if(userlist.get(Constant.GROUP_USERNAME) != null)
            contactList.add(0, userlist.get(Constant.GROUP_USERNAME));
        // 把"申请与通知"添加到首位
        if(userlist.get(Constant.NEW_FRIENDS_USERNAME) != null)
            contactList.add(0, userlist.get(Constant.NEW_FRIENDS_USERNAME));
//        Log.e("contactFragment","users,Collections.sort--------"+contactList.size()+"-------->"+contactList);

        initlistView();
    }

    void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(((HomeActivity)getActivity()).isConflict){
            outState.putBoolean("isConflict", true);
        }else if(((HomeActivity)getActivity()).getCurrentAccountRemoved()){
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }

    }
}
