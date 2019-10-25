package com.softinc.youelink;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.easemob.chat.EMContactManager;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.softinc.application.MyApplication;
import com.softinc.bean.Comment;
import com.softinc.bean.Meeting;
import com.softinc.bean.Meeting_User;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.ACache;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.StringHelper;
import com.softinc.utils.TimeHelper;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MeetingInfoActivity extends Activity {

    private static final String TAG = "MeetingInfoActivity";

    private Title title;
    private TextView tv_title;
    private CircleImageView civ_icon;
    private TextView tv_xin_yong;
    private TextView tv_place;
    private TextView tv_time_and_money;
    private TextView tv_nickname;
    private Button btn_chat;
    private Button btnAddFriend;
    private Button btnWait;
    private TextView tv_description;
    private TextView tv_watch_count;
    private TextView tv_comment_count;
    private TextView tv_jb;

    private TextView tv_require;

    private Button btnJoin;
    private Button btnCom;
    private Button btnExit;

    private Button joinCount;
    private Button commentCount;

    private int meeting_id;
    private Meeting meeting = null;

    private ListView lv_comment;
    private ListView lv_users;

    private List<Comment> comment_list = new ArrayList<Comment>();
    private List<Meeting_User> user_list = new ArrayList<Meeting_User>();

    private Boolean is_joined = false;

    private List<User> lst_friends = new ArrayList<User>();
    private List<User> lst_request_friends = new ArrayList<User>();

    UserAdapter userAdapter = new UserAdapter();

    private boolean is_cur_user = false;

    private Gson gson = new Gson();
    private String cacheName = Constant.CACHE_MEETING;
    private String cacheMeetingComments = Constant.CACHE_MEETING_COMMENTS;
    private String cacheMeetingUsers = Constant.CACHE_MEETING_USERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_info);

        this.meeting_id = Integer.parseInt(getIntent().getStringExtra("meeting_id"));
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();

            this.meeting = gson.fromJson(MyApplication.cacheManager.getAsString(this.cacheName + this.meeting_id), Meeting.class);
        } else {
            this.meeting = (Meeting) getIntent().getSerializableExtra("meeting");

            this.cacheName += meeting.id;
            this.cacheMeetingComments += meeting.id;
            this.cacheMeetingUsers += meeting.id;
            this.cacheMeeting();
        }

        if (meeting.uid.equals(MyApplication.uid)) {
            this.is_cur_user = true;
        }

        initView();
        initViewEvents();

        getCommentsData();
        getUsersData();

        getEventInfo();

        if(MyApplication.uid.equals(this.meeting.uid)){
            btnJoin.setVisibility(View.GONE);
        }
    }

    private void getCommentsData() {
        if (!CommonUtils.isNetWorkConnected(this)) {
            this.initMeetingComments();

            lv_comment.invalidateViews();
            if (comment_list != null) {
                commentCount.setText("评论 " + comment_list.size());
            }
            return;
        }

        RequestParams params = new RequestParams();
        params.put("eid", meeting.id);
        params.put("page", 1);
        MyApplication.client.post(Constant.URL_COMMENT_OF_MEETING, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray comments = response.getJSONArray("Data");
                        for (int i = 0; i < comments.length(); i++) {

                            JSONObject obj = (JSONObject) comments.get(i);
                            Comment com = new Comment();
                            com.UID = obj.getInt("UID");
                            com.UserPic = obj.getString("UserPic");
                            com.NickName = obj.getString("NickName");
                            com.body = obj.getString("body");
                            com.CreateTime = obj.getString("CreateTime");
                            com.Gender = obj.getInt("Gender");
                            com.ET_ID = obj.getInt("ET_ID");
                            com.EID = obj.getInt("EID");

                            comment_list.add(com);
                        }

                        cacheMeetingComments(comment_list);

                        lv_comment.invalidateViews();
                        commentCount.setText("评论 " + comment_list.size());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    private void getUsersData() {
        if (!CommonUtils.isNetWorkConnected(this)) {
            this.initMeetingUsers();

            lv_users.invalidateViews();
            if (user_list != null) {
                joinCount.setText("报名 " + user_list.size());
            }
            return;
        }

        user_list.clear();
        RequestParams params = new RequestParams();
        params.put("eid", meeting.id);
        params.put("page", 1);
        MyApplication.client.get(Constant.URL_MEMBERS_OF_MEETING, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray users = response.getJSONArray("Data");
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject obj = (JSONObject) users.get(i);
                            Meeting_User user = new Meeting_User();
                            user.nickName = obj.getString("NickName");
                            user.userPic = obj.getString("UserPic");
                            user.creditPoint = obj.getInt("CreditPoint");
                            user.myEventCnt = obj.getInt("myEventCnt");
                            user.myJoinCnt = obj.getInt("myJoinCnt");
                            user.gender = obj.getInt("Gender");
                            user.userId = obj.getInt("UID");
                            user.isAccept = obj.getInt("IsAccept");
                            user.joinTime = obj.getString("JoinTime");

                            if (MyApplication.uid.equals(user.userId.toString())) {
                                is_joined = true;
                                btnJoin.setVisibility(View.GONE);
                                btnExit.setVisibility(View.VISIBLE);
                            }

                            user_list.add(user);
                        }

                        cacheMeetingUsers(user_list);

                        lv_users.invalidateViews();
                        joinCount.setText("报名 " + user_list.size());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initView() {
        title = (Title) this.findViewById(R.id.tv_name);
        tv_title = (TextView) this.findViewById(R.id.tv_title);
        civ_icon = (CircleImageView) this.findViewById(R.id.civ_icon);
        tv_xin_yong = (TextView) this.findViewById(R.id.tv_xin_yong);
        tv_place = (TextView) this.findViewById(R.id.tv_place);
        tv_time_and_money = (TextView) this.findViewById(R.id.tv_time_and_money);
        tv_nickname = (TextView) this.findViewById(R.id.tv_nickname);
        //tv_comments = (TextView) this.findViewById(R.id.tv_comments);
        btn_chat = (Button) this.findViewById(R.id.b_chat);
        ////bt_place = (Button) this.findViewById(R.id.bt_place);
        btnAddFriend = (Button) this.findViewById(R.id.btnAddFriend);
        btnWait = (Button) this.findViewById(R.id.btnWait);
        tv_description = (TextView) this.findViewById(R.id.tv_description);
        tv_watch_count = (TextView) this.findViewById(R.id.tv_watch_count);
        //tv_sign_up_count = (TextView) this.findViewById(R.id.tv_sign_up_count);
        tv_comment_count = (TextView) this.findViewById(R.id.tv_comment_count);
        lv_comment = (ListView) this.findViewById(R.id.lv_comment);
        btnJoin = (Button) this.findViewById(R.id.btnJoin);
        btnCom = (Button) this.findViewById(R.id.btnCom);
        btnExit = (Button) this.findViewById(R.id.btnExit);

        lv_comment = (ListView) findViewById(R.id.lv_comment);
        tv_jb = (TextView) this.findViewById(R.id.tv_jb);

        joinCount = (Button) findViewById(R.id.joinCount);
        joinCount.setText("报名  " + meeting.memberCount);
        commentCount = (Button) findViewById(R.id.commentCount);
        commentCount.setText("评论  " + meeting.commentCount);

        tv_require = (TextView) findViewById(R.id.tv_require);
        StringBuilder require = new StringBuilder();
        if (!TextUtils.isEmpty(meeting.memberLevel) && meeting.memberLevel.equals("2")) {
            require.append("黑卡  ");
        } else {
            require.append("不限  ");
        }

        require.append("限" + (meeting.memberLimit != null && meeting.memberLimit > 0 ? meeting.memberLimit : 0) + "人  ");

        if (!TextUtils.isEmpty(meeting.ownerGender)) {
            switch (meeting.ownerGender) {
                case "0":
                    require.append("性别不限  ");
                    break;
                case "1":
                    require.append("性别男  ");
                    break;
                case "2":
                    require.append("性别女  ");
                    break;
            }
        }

        require.append((meeting.needAccept != null && meeting.needAccept == 1) ? "报名需确认" : "报名不需确认");
        tv_require.setText(require.toString());
    }

    private void initChatBtn(Integer status) {
        switch (status){
            case 0:
//                btn_chat.setVisibility(View.GONE);
//                btnAddFriend.setVisibility(View.VISIBLE);
//                btnWait.setVisibility(View.GONE);
                break;
            case 1:
                btn_chat.setVisibility(View.GONE);
                btnAddFriend.setVisibility(View.GONE);
                btnWait.setVisibility(View.VISIBLE);
                break;
            case 2:
                btn_chat.setVisibility(View.VISIBLE);
                btnAddFriend.setVisibility(View.GONE);
                btnWait.setVisibility(View.GONE);
                break;
        }

        if(meeting.uid.equals(MyApplication.uid)){
            btn_chat.setVisibility(View.GONE);
            btnAddFriend.setVisibility(View.VISIBLE);
            btnWait.setVisibility(View.GONE);

            return;
        }
    }

    private void initViewEvents() {
        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle myBundelForName = new Bundle();
                myBundelForName.putString("nickname", meeting.nickName);
                myBundelForName.putString("userId", meeting.uid);
                startActivity(new Intent(MeetingInfoActivity.this, ChatActivity.class).putExtras(myBundelForName));
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (meeting.uid.equals(MyApplication.uid)) {
                    Toast.makeText(MeetingInfoActivity.this, "不能加自己为好友", Toast.LENGTH_SHORT).show();
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("buddyId", meeting.uid);
                params.put("buddyHxId", meeting.hxUser);

                MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        try {
                            Boolean success = response.getBoolean("Result");
                            if (success) {
                                if (response.getInt("Data") == 1) {
                                    btnAddFriend.setVisibility(View.GONE);
                                    btnWait.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), response.getString("Infomation"), Toast.LENGTH_SHORT).show();

                                } else {
                                    btnAddFriend.setVisibility(View.GONE);
                                    btn_chat.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "添加成功！", Toast.LENGTH_SHORT).show();

                                    String s = getApplicationContext().getResources().getString(R.string.Add_a_friend);
                                    EMContactManager.getInstance().addContact(meeting.hxUser, s);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        final CommentAdapter commentAdapter = new CommentAdapter();
        lv_comment.setAdapter(commentAdapter);

        lv_users = (ListView) findViewById(R.id.lv_users);

        lv_users.setAdapter(userAdapter);

        joinCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lv_users.setVisibility(View.VISIBLE);
                lv_comment.setVisibility(View.GONE);
                joinCount.setTextColor(0xFF333333);
                commentCount.setTextColor(0xFFCCCCCC);
            }
        });

        commentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lv_users.setVisibility(View.GONE);
                lv_comment.setVisibility(View.VISIBLE);
                joinCount.setTextColor(0xFFCCCCCC);
                commentCount.setTextColor(0xFF333333);
            }
        });

        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MeetingInfoActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });

        tv_title.setText(meeting.title);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), meeting.title, Toast.LENGTH_LONG).show();
            }
        });
        tv_place.setText(meeting.meetingAddress);

        tv_nickname.setText(meeting.nickName + "  ");
        tv_time_and_money.setText(TimeHelper.translateIntToDate(Long.parseLong(meeting.meetingTime)));
        tv_description.setText(meeting.description);

        if (!TextUtils.isEmpty(meeting.userPic)) {
            ImageLoader.getInstance().displayImage(meeting.userPic, civ_icon);
        }
        civ_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtils.isNetWorkConnected(MeetingInfoActivity.this)) {
                    return;
                }

                Intent intent = new Intent(MeetingInfoActivity.this, UserActivity.class);
                intent.putExtra("user_id", meeting.uid);

                MeetingInfoActivity.this.startActivity(intent);
            }
        });

        if (!TextUtils.isEmpty(meeting.creditPoint) && !meeting.creditPoint.equals("null")) {
            tv_xin_yong.setText(meeting.creditPoint);
        } else {

        }

        tv_watch_count.setText(meeting.watchCount);
        tv_comment_count.setText(meeting.commentCount);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateJoinConditions()) {
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("eid", meeting.id);
                params.put("act", 0);

                MyApplication.client.post(Constant.URL_CASE_JOIN, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Boolean success = response.getBoolean("Result");
                            if (!success) {
                                String msg = response.getString("Infomation");
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            } else {
                                btnJoin.setVisibility(View.GONE);
                                btnExit.setVisibility(View.VISIBLE);

                                userAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "报名成功，等待发起人确认！", Toast.LENGTH_SHORT).show();

                                user_list.clear();
                                getUsersData();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputServer = new EditText(MeetingInfoActivity.this);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MeetingInfoActivity.this);
                builder.setTitle("评论").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer).setNegativeButton("取消", null);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String comment = inputServer.getText().toString();

                        if (!comment.isEmpty()) {
                            RequestParams params = new RequestParams();
                            params.put("content", comment);
                            params.put("eid", meeting.id);

                            MyApplication.client.post(Constant.URL_CASE_ADD_COMMENT, params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    super.onSuccess(statusCode, headers, response);

                                    try {
                                        Boolean success = response.getBoolean("Result");
                                        if (!success) {
                                            String msg = response.getString("Infomation");
                                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            commentAdapter.notifyDataSetChanged();
                                            commentCount.setText("评论  " + meeting.commentCount + 1);
                                            Toast.makeText(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT).show();

                                            comment_list.clear();
                                            getCommentsData();
                                        }
                                    } catch (Exception ex) {
                                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    super.onFailure(statusCode, headers, responseString, throwable);
                                    Toast.makeText(getApplicationContext(), "服务异常", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入评论", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.show();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("eid", meeting.id);
                params.put("act", 1);

                MyApplication.client.post(Constant.URL_CASE_JOIN, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Boolean success = response.getBoolean("Result");
                            if (!success) {
                                String msg = response.getString("Infomation");
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            } else {
                                btnJoin.setVisibility(View.VISIBLE);
                                btnExit.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "你已取消报名！", Toast.LENGTH_SHORT).show();

                                user_list.clear();
                                getUsersData();
                            }
                        } catch (Exception ex) {
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        tv_jb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("eid", meeting.id);

                MyApplication.client.post(Constant.URL_CASE_REPORT_EVENT, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            Boolean success = response.getBoolean("Result");
                            if (success) {
                                Toast.makeText(getApplicationContext(), "举报成功！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), response.getString("Infomation"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.console().printf(responseString);
                        Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean validateJoinConditions() {
        StringBuilder strBuilder = new StringBuilder();
        if (meeting.gender.equals(MyApplication.user.getGender())) {
            strBuilder.append("性别不符\r\n");
        }

        if (!meeting.memberLevel.equals(MyApplication.user.getUserLevel())) {
            strBuilder.append("用户等级不符\r\n");
        }

        if (TimeHelper.validateOverTime(TimeHelper.translateIntToDate(Long.parseLong(meeting.meetingTime))) < 0) {
            strBuilder.append("活动已过期\r\n");
        }

        if (!strBuilder.toString().isEmpty()) {
            Toast.makeText(MeetingInfoActivity.this, strBuilder.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private class CommentAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return comment_list.size();
        }

        @Override
        public Object getItem(int position) {
            return comment_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            final Comment comment = comment_list.get(position);

            if (convertView == null) {
                convertView = View.inflate(MeetingInfoActivity.this, R.layout.item_meeting_join_comment, null);
                holder = new ViewHolder();
                holder.civ_user = (CircleImageView) convertView.findViewById(R.id.civ_user);
                holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
                holder.txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
                holder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!CommonUtils.isNetWorkConnected(MeetingInfoActivity.this)) {
                Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + comment.UID);
                if (bit != null) {
                    holder.civ_user.setImageBitmap(bit);
                }
            } else {
                if (!TextUtils.isEmpty(comment.UserPic) && !"nil".equals(comment.UserPic)) {
                    ImageLoader.getInstance().displayImage("http://123.57.217.223/youelink/upload/userpic/" + comment.UserPic, holder.civ_user, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + comment.UID);
                            if (bit == null) {
                                MyApplication.cacheManager.put(Constant.CACHE_USER_PORTRAIT + comment.UID, loadedImage);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }
            }

            holder.civ_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    return;
//                    if (!CommonUtils.isNetWorkConnected(MeetingInfoActivity.this)) {
//                        return;
//                    }
//
//                    Intent intent = new Intent(MeetingInfoActivity.this, UserActivity.class);
//                    intent.putExtra("user_id", comment.UID);
//
//                    startActivity(intent);
                }
            });

            holder.txtName.setText(comment.NickName);
            holder.txtInfo.setText(comment.body);
            if (!StringHelper.isEmptyOrNull(comment.CreateTime)) {
                holder.txtTime.setText(TimeHelper.translateIntToDate(Long.parseLong(comment.CreateTime.toString())));
            }

            return convertView;
        }
    }

    private class UserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return user_list.size();
        }

        @Override
        public Object getItem(int position) {
            return user_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            UserViewHolder holder = null;
            final Meeting_User user = user_list.get(position);

            if (convertView == null) {
                convertView = View.inflate(MeetingInfoActivity.this, R.layout.item_meeting_join_user, null);
                holder = new UserViewHolder();
                holder.civ_user = (CircleImageView) convertView.findViewById(R.id.civ_user);
                holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
                holder.txtCredit = (TextView) convertView.findViewById(R.id.txtCredit);
                holder.txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
                holder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);

                holder.btn_pass = (Button) convertView.findViewById(R.id.btn_pass);
                holder.btn_passed = (Button) convertView.findViewById(R.id.btn_passed);
                holder.tv_wait_pass = (TextView) convertView.findViewById(R.id.tv_wait_pass);
                holder.tv_passed = (TextView) convertView.findViewById(R.id.tv_pass);

                convertView.setTag(holder);
            } else {
                holder = (UserViewHolder) convertView.getTag();
            }

            if (!CommonUtils.isNetWorkConnected(MeetingInfoActivity.this)) {
                Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + user.userId);
                if (bit != null) {
                    holder.civ_user.setImageBitmap(bit);
                }
            } else {
                if (!TextUtils.isEmpty(user.userPic) && !"nil".equals(user.userPic)) {
                    ImageLoader.getInstance().displayImage("http://123.57.217.223/youelink/upload/userpic/" + user.userPic, holder.civ_user, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + user.userId);
                            if (bit == null) {
                                MyApplication.cacheManager.put(Constant.CACHE_USER_PORTRAIT + user.userId, loadedImage);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }
            }

            holder.civ_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    return;

//                    if (!CommonUtils.isNetWorkConnected(MeetingInfoActivity.this)) {
//                        return;
//                    }
//
//                    Intent intent = new Intent(MeetingInfoActivity.this, UserActivity.class);
//                    intent.putExtra("user_id", user.userId);
//
//                    startActivity(intent);
                }
            });

            holder.txtName.setText(user.nickName);

            holder.txtCredit.setText(user.creditPoint.toString());
            holder.txtCredit.setBackgroundResource(user.gender == 1 ? R.drawable.blue_trubg : R.drawable.pink_trubg);

            holder.txtInfo.setText("参与过" + user.myJoinCnt.toString() + "个活动，发起过" + user.myEventCnt.toString() + "个活动");

            if (user.joinTime != null && !user.joinTime.equals("null")) {
                ////holder.txtTime.setText(user.joinTime);
                holder.txtTime.setText(TimeHelper.translateIntToDate(Long.parseLong(user.joinTime.toString())));
            }

            if (is_cur_user) {
                holder.tv_passed.setVisibility(View.GONE);
                holder.tv_wait_pass.setVisibility(View.GONE);

                if (user.isAccept == 1) {
                    holder.btn_passed.setVisibility(View.VISIBLE);
                    holder.btn_pass.setVisibility(View.GONE);
                } else {
                    holder.btn_passed.setVisibility(View.GONE);
                    holder.btn_pass.setVisibility(View.VISIBLE);

                    holder.btn_pass.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RequestParams params = new RequestParams();
                            params.put("eid", meeting.id);
                            params.put("uid", user.userId);
                            MyApplication.client.post(Constant.URL_ACCEPT_JOIN_EVENT, params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    try {
                                        Boolean success = response.getBoolean("Result");
                                        if (success) {
                                            ////Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                                            getUsersData();
                                        } else {
                                            Toast.makeText(getApplicationContext(), response.getString("Infomation"), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    super.onFailure(statusCode, headers, responseString, throwable);
                                    Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
                }
            } else {
                holder.btn_passed.setVisibility(View.GONE);
                holder.btn_pass.setVisibility(View.GONE);

                if (user.isAccept == 1) {
                    holder.tv_passed.setVisibility(View.VISIBLE);
                    holder.tv_wait_pass.setVisibility(View.GONE);
                } else {
                    holder.tv_passed.setVisibility(View.GONE);
                    holder.tv_wait_pass.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }
    }

    class ViewHolder {
        public CircleImageView civ_user;
        public TextView txtName;
        public TextView txtCredit;
        public TextView txtInfo;
        public TextView txtTime;
    }

    class UserViewHolder {
        public CircleImageView civ_user;
        public TextView txtName;
        public TextView txtCredit;
        public TextView txtInfo;
        public TextView txtTime;

        private Button btn_pass;
        private Button btn_passed;
        private TextView tv_wait_pass;
        private TextView tv_passed;
    }

    private void cacheMeeting() {
        String result = MyApplication.cacheManager.getAsString(this.cacheName);
        if (result != null && !result.isEmpty()) {
            MyApplication.cacheManager.remove(this.cacheName);
        }
        MyApplication.cacheManager.put(this.cacheName, gson.toJson(this.meeting));
    }

    private void cacheMeetingUsers(List<Meeting_User> list) {
        String result = MyApplication.cacheManager.getAsString(this.cacheMeetingUsers);
        if (result != null && !result.isEmpty()) {
            MyApplication.cacheManager.remove(this.cacheMeetingUsers);
        }
        MyApplication.cacheManager.put(this.cacheMeetingUsers, gson.toJson(list));
    }

    private void initMeetingUsers() {
        Type type = new TypeToken<List<Meeting_User>>() {
        }.getType();
        this.user_list = gson.fromJson(MyApplication.cacheManager.getAsString(this.cacheMeetingUsers), type);
        if (this.user_list == null) {
            this.user_list = new ArrayList<Meeting_User>();
        }
    }

    private void cacheMeetingComments(List<Comment> list) {
        String result = MyApplication.cacheManager.getAsString(this.cacheMeetingComments);
        if (result != null && !result.isEmpty()) {
            MyApplication.cacheManager.remove(this.cacheMeetingComments);
        }
        MyApplication.cacheManager.put(this.cacheMeetingComments, gson.toJson(list));
    }

    private void initMeetingComments() {
        Type type = new TypeToken<List<Comment>>() {
        }.getType();
        this.comment_list = gson.fromJson(MyApplication.cacheManager.getAsString(this.cacheMeetingComments), type);
        if (this.comment_list == null) {
            this.comment_list = new ArrayList<Comment>();
        }
    }

    private void getEventInfo(){
        RequestParams params = new RequestParams();
        params.put("eid", meeting.id);

        MyApplication.client.get(Constant.URL_EVENT_INFO, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject meetingObj = response.getJSONObject("Data");

                    Integer status = meetingObj.getInt("BuddyStatus");
                    initChatBtn(status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }
}
