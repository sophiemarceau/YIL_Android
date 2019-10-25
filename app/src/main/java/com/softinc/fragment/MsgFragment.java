package com.softinc.fragment;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.softinc.DemoHXSDKHelper;
import com.softinc.adapter.ChatAllHistoryAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.InviteMessgeDao;
import com.softinc.utils.CommonUtils;
import com.softinc.youelink.ChatActivity;
import com.softinc.youelink.HomeActivity;
import com.softinc.youelink.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.softinc.fragment.ContactsFragment.usersfromServerList;


public class MsgFragment extends Fragment {

    private final static String TAG = "MsgFragment";

    private InputMethodManager inputMethodManager;
    private ListView listView;
    private ChatAllHistoryAdapter adapter;
    private EditText query;
    private ImageButton clearSearch;
    public RelativeLayout errorItem;
    public TextView errorText;
    private boolean hidden;
    private List<EMConversation> conversationList = new ArrayList<EMConversation>();
    private List<User> userlistFromServer= new ArrayList<User>();

    private List<String> conversationSource= new ArrayList<String>();
    List<String> nicklist = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_history, container, false);
    }


    public String getNickName(List<User> list ,String conversationname) {
        for (User temp : list) {
            if (temp.getUsername().equals(conversationname)) {

                return  temp.getNick();

            }

        }
        return "";

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;

        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        listView = (ListView) getView().findViewById(R.id.list);
        errorItem = (RelativeLayout) getView().findViewById(R.id.rl_error_item);
        errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);
        conversationList.clear();
        conversationList.addAll(loadConversationsWithRecentChat());
//        Log.e(TAG,"好友列表－昵称－－111111111－>"+nicklist);
//        Log.e(TAG,"好友列表－－loadConversationsWithRecentChat－－>"+loadConversationsWithRecentChat());
        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
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

//                    Log.e(TAG,"好友列表－－－－>"+userlistFromServer.size());
//                    Log.e(TAG,"好友列表－conversationList－－－>"+conversationList.size());
                    if (userlistFromServer.size() > 0) {
                        DemoHXSDKHelper.getInstance().setGetcontactList(userlistFromServer);
                        if (conversationList.size()>0){
                            for (EMConversation conversation:conversationList) {
//                                Log.e(TAG,"conversation.getIsGroup()----------->"+conversation.getIsGroup());
                                if (!conversation.isGroup()) {
//                                    for (User temp : userlistFromServer) {
//                                        if (temp.getUsername().equals(conversation.getUserName())) {
////                                            Log.e(TAG,"temp.getIsGroup()----------->"+temp.getUsername());
//                                            nicklist.add(temp.getNick());
//                                            conversationSource.add(conversation.getUserName());
//                                        }
//
//                                    }

                                 String nickStr =   getNickName(usersfromServerList,conversation.getUserName());
                                    if (nickStr.equals("")){

                                        nicklist.add("系统消息");
                                    }else{
                                        nicklist.add(nickStr);
                                    }
                                    conversationSource.add(conversation.getUserName());
                                }else{

                                    EMGroup group = EMGroupManager.getInstance().getGroup(conversation.getUserName());
                                    nicklist.add(group.getGroupName());
                                    conversationSource.add(conversation.getUserName());
//                                    Log.e(TAG,group.getGroupName()+"------------group昵称-------－－－>"+conversation.getUserName());
                                }

                            }

                        }
                    }
//                    Log.e(TAG,"好友列表－昵称－－－>"+nicklist);
//                    Log.e(TAG,"好友列表－名字集合－－－>"+conversationSource);
                    setupListView();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 搜索框
        query = (EditText) getView().findViewById(R.id.query);
        String strSearch = getResources().getString(R.string.search);
        query.setHint(strSearch);
        // 搜索框中清除button
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

        clearSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });


        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");//建议把它写一个公共的变量，这里方便阅读就不写了。
        BroadcastReceiver mItemViewListClickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
//              Log.e("MSGMENT","onReceive");
//                conversationList.clear();
//                conversationList.addAll(loadConversationsWithRecentChat());
//                for ( EMConversation conversation:conversationList) {
//
//                    if (!conversation.getIsGroup()) {
//
//                    }else{
//
//                    }
//                }
                refreshmessageList();
                }
        };
        broadcastManager.registerReceiver(mItemViewListClickReceiver, intentFilter);

    }

    private void setupListView() {
        adapter = new ChatAllHistoryAdapter(getActivity(), 1, conversationList,conversationSource,nicklist );
        // 设置adapter
        listView.setAdapter(adapter);
        final String st2 = getResources().getString(R.string.Cant_chat_with_yourself);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = adapter.getItem(position);
                String username = conversation.getUserName();
                if (username.equals(MyApplication.getInstance().getUserName()))
                    Toast.makeText(getActivity(), st2, 0).show();
                else {
                    // 进入聊天页面
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    if (conversation.isGroup()) {
                        // it is group chat
                        intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                        intent.putExtra("groupId", username);
                    } else {
                        // it is single chat
//                        Log.e(TAG, "conversationSource---value------>" + conversationSource);

                            if (conversationSource.contains(username)){
                                Bundle myBundelForName = new Bundle();
//                                Log.e(TAG, "userID---value------>" + username);
                                myBundelForName.putString("userId", username);
                                myBundelForName.putString("nickname", (nicklist.get(conversationSource.indexOf(username))));
                                intent.putExtras(myBundelForName);
                            }
                    }
                    startActivity(intent);
                }
            }
        });
        // 注册上下文菜单
        registerForContextMenu(listView);

        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                hideSoftKeyboard();
                return false;
            }

        });
    }

    void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
        getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
        // }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        boolean deleteMessage = false;
        if (item.getItemId() == R.id.delete_message) {
            deleteMessage = true;
            handled = true;
        } else if (item.getItemId() == R.id.delete_conversation) {
            deleteMessage = false;
            handled = true;
        }
        EMConversation tobeDeleteCons = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
        // 删除此会话
        EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup(), deleteMessage);
        InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
        inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
        adapter.remove(tobeDeleteCons);
        adapter.notifyDataSetChanged();

        // 更新消息未读数
        ((HomeActivity) getActivity()).updateUnreadLabel();

        return handled ? true : super.onContextItemSelected(item);
    }

    /**
     * 刷新页面
     */
    public void refreshMsgFragment() {

        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        refreshmessageList();


    }

    /**
     * 获取所有会话
     *
     */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        // 过滤掉messages size为0的conversation
        /**
         * 如果在排序过程中有新消息收到，lastMsgTime会发生变化
         * 影响排序过程，Collection.sort会产生异常
         * 保证Conversation在Sort过程中最后一条消息的时间不变
         * 避免并发问题
         */
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        try {
            // Internal is TimSort algorithm, has bug
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     *
     */
    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first == con2.first) {
                    return 0;
                } else if (con2.first > con1.first) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refreshMsgFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden && ! ((HomeActivity)getActivity()).isConflict) {
            refreshMsgFragment();
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


    public void refreshmessageList() {
        userlistFromServer.clear();
        conversationList.clear();
        nicklist.clear();
        conversationSource.clear();
        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }

        conversationList.addAll(loadConversationsWithRecentChat());

//        Log.e(TAG,"refresh--------------refreshmessageList------------------->"+loadConversationsWithRecentChat());
        MyApplication.client.get(Constant.URL_ALL_FRIENDS, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {

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
                        if (conversationList.size()>0){
                            for (EMConversation conversation:conversationList) {
                                if (!conversation.isGroup()) {
                                    String nickStr =   getNickName(userlistFromServer,conversation.getUserName());
                                    if (nickStr.equals("")){

                                        nicklist.add("系统消息");
                                    }else{
                                        nicklist.add(nickStr);
                                    }
                                    conversationSource.add(conversation.getUserName());

                                }else{
                                    EMGroup group = EMGroupManager.getInstance().getGroup(conversation.getUserName());
                                    nicklist.add(group.getGroupName());
                                    conversationSource.add(conversation.getUserName());
//                                    nicklist.add("");
//                                    conversationSource.add("");
                                }

                            }

                        }
                    }


//                    Log.e(TAG,"refresh--------------conversationSource------------------->"+conversationSource);



                    if(adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
