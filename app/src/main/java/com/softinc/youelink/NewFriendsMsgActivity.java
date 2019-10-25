package com.softinc.youelink;

/**
 * Created by sophiemarceau_qu on 15/5/22.
 */
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.adapter.AddAdapter;
import com.softinc.adapter.NewFriendsMsgAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.InviteMessgeDao;
import com.softinc.domain.InviteMessage;
import com.softinc.engine.UserEngine;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 申请与通知
 *
 */
public class NewFriendsMsgActivity extends BaseActivity {
    public static final String TAG = "NewFriendsMsgActivity";
    private  List<User> requestnewuserList = new ArrayList<User>();
    private PullToRefreshListView listNewFriendRequestView;
    private int curPage = 1;//当前数据的页数
    private   NewFriendsMsgAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friends_msg);
        listNewFriendRequestView = (PullToRefreshListView) findViewById(R.id.newfriend_request_list);

        setListData();


    }

    public void back(View view) {
        finish();
    }




    /**
     * ListView配置数据
     *
     */
    public void setListData() {
        requestnewuserList.clear();
//        Log.e(TAG, "requestnewuserList---clear--->" + requestnewuserList);
        RequestParams params = new RequestParams();
        params.put("page",curPage);
//        Log.e(TAG, "curPage------>" + curPage);
        MyApplication.client.get(Constant.URL_RequestAgreelist, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.e(TAG, "返回数据" + response);
                if (ResponseUtils.isResultOK(response)) {
                    try {
                        JSONArray userAry = response.getJSONArray("Data");
//                        Log.e(TAG, "userAry------>" + userAry);
                        for (int i = 0; i < userAry.length(); i++) {
                            JSONObject userObj = (JSONObject) userAry.get(i);
                            User user = new User();

                            user.setId(userObj.getString("UID"));
                            user.setNick(userObj.getString("NickName"));
                            user.setUsername(userObj.getString("hxUser"));
                            user.setAvatar(userObj.getString("UserPic"));
                            user.setBR_ID(userObj.getString("BR_ID"));
                            requestnewuserList.add(user);
                        }
                        if (adapter!=null){

                            listNewFriendRequestView.onRefreshComplete();
                            adapter.notifyDataSetChanged();
                        }else{

                            adapter = new NewFriendsMsgAdapter(NewFriendsMsgActivity.this, 1, requestnewuserList);
                            adapter.notifyDataSetChanged();
                            listNewFriendRequestView.setAdapter(adapter);

                            listNewFriendRequestView.setMode(PullToRefreshBase.Mode.BOTH);
                            listNewFriendRequestView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                                @Override
                                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                                    if (requestnewuserList.size()>10){
                                        curPage += 1;
                                    }
                                    setListData();
                                }
                            });
                        }

                        //设置adapter

//                        MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME).setUnreadMsgCount(0);


                    } catch (JSONException e) {
                        PromptUtils.showErrorDialog(NewFriendsMsgActivity.this, e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    PromptUtils.showErrorDialog(NewFriendsMsgActivity.this, ResponseUtils.getInformation(response));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.e(TAG, responseString);
                PromptUtils.showNoNetWork(NewFriendsMsgActivity.this);
            }


        });
    }


}