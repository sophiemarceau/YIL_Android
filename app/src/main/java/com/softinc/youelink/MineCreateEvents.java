package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.engine.UserEngine;
import com.softinc.utils.ACache;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.TimeHelper;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MineCreateEvents extends Activity {

    private PullToRefreshListView lv_meetings;

    private int curPage = 1;
    private Boolean isDataRefreshing = false;

    private List<Meeting> ls_meetings = new ArrayList<Meeting>();

    private Title tv_name;

    private MeetingAdapter meetingAdapter;

    private Integer user_id;

    private Gson gson = new Gson();
    private String cacheName = Constant.CACHE_USER_CREATE_EVENTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user_id = getIntent().getIntExtra("user_id", 0);
        this.cacheName += user_id;

        setContentView(R.layout.activity_mine_create);

        initView();

        initViewEvents();

        setListData();
    }

    private void initView() {
        lv_meetings = (PullToRefreshListView) findViewById(R.id.lv_meeting);
        lv_meetings.setMode(PullToRefreshBase.Mode.BOTH);

        meetingAdapter = new MeetingAdapter();
        lv_meetings.setAdapter(meetingAdapter);

        tv_name = (Title) findViewById(R.id.tv_name);

//        pd = new ProgressDialog(MineCreateEvents.this);
//        pd.setCanceledOnTouchOutside(false);
//        pd.setMessage("正在加载...");
//        pd.show();
    }

    private void initViewEvents() {
        lv_meetings.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (lv_meetings.isHeaderShown()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String label = format.format(new Date());

                    // 显示最后更新的时间
                    lv_meetings.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(getString(R.string.find_near_refreshdate) + label);

                    curPage = 1;
                } else {
                    curPage += 1;
                    setListData();
                }
                isDataRefreshing = true;
            }
        });
        lv_meetings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    if (position - 1 < 0) {
                        return;
                    }

                    Meeting meeting = ls_meetings.get(position - 1);

                    if (!CommonUtils.isNetWorkConnected(MineCreateEvents.this)) {
                        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_MEETING + meeting.id);
                        if (obj == null) {
                            return;
                        }
                    }

                    Intent intent = new Intent(MineCreateEvents.this, MeetingInfoActivity.class);
                    intent.putExtra("meeting_id", meeting.id);
                    intent.putExtra("meeting", ls_meetings.get(position - 1));

                    MineCreateEvents.this.startActivity(intent);

                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        });

        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MineCreateEvents.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    private void setListData() {
        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();

            this.initCreateMeetings();

            meetingAdapter.notifyDataSetChanged();
            lv_meetings.onRefreshComplete();

            return;
        }

        RequestParams params = new RequestParams();
        params.put("type", 1);
        params.put("page", curPage);
        params.put("uid", user_id);

        MyApplication.client.get(Constant.URL_EVENT_LIST_BY_UID, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray meetings = response.getJSONArray("Data");
                        for (int i = 0; i < meetings.length(); i++) {
                            JSONObject data = (JSONObject) meetings.get(i);
                            Meeting m = new Meeting();
                            m.memberLevel = data.getString("UserLevel");
                            m.meetingAddress = data.getString("EventAddress");
                            m.id = data.getString("EID");
                            m.meetingTime = data.getString("EventDate");
                            m.title = data.getString("Title");
                            m.uid = data.getString("UID");
                            m.gender = data.getString("Gender");
                            m.createTime = data.getString("CreateTime");
                            m.onTop = data.getString("OnTop");
                            m.latitude = data.getString("Latitude");
                            m.longitude = data.getString("Longitude");
                            m.coinCount = data.getString("Coins");
                            m.memberCount = data.getString("MemberCount");
                            m.payType = data.getString("PayType");

                            m.hxUser = data.getString("hxUser");
                            m.userPic = Constant.URL_ImagePATH + data.getString("UserPic");
                            m.ownerGender = data.getString("OwnerGender");
                            m.nickName = data.getString("NickName");
                            m.creditPoint = data.getString("CreditPoint");

                            ls_meetings.add(m);
                        }

                        cacheCreateMeetings();

                        meetingAdapter.notifyDataSetChanged();
                        lv_meetings.onRefreshComplete();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("失败", responseString, throwable);
            }
        });
    }

    /**
     * List数据适配器
     */
    private class MeetingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ls_meetings.size();
        }

        @Override
        public Object getItem(int position) {
            return ls_meetings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            final Meeting meeting = ls_meetings.get(position);

            if (convertView == null) {
                convertView = View.inflate(MineCreateEvents.this, R.layout.item_meeting, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_name);
                holder.civ_icon = (CircleImageView) convertView.findViewById(R.id.civ_icon);
                holder.tv_xin_yong = (TextView) convertView.findViewById(R.id.tv_xin_yong);
                holder.tv_place = (TextView) convertView.findViewById(R.id.tv_place);
                holder.tv_time_and_money = (TextView) convertView.findViewById(R.id.tv_time_and_money);
                holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.iv_gender = (ImageView) convertView.findViewById(R.id.iv_gender);
                holder.tv_watch_count = (TextView) convertView.findViewById(R.id.tv_watch_count);
                //holder.tv_sign_up_count = (TextView) convertView.findViewById(R.id.tv_sign_up_count);
                holder.tv_comment_count = (TextView) convertView.findViewById(R.id.tv_comment_count);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(meeting.title);

            if (!CommonUtils.isNetWorkConnected(MineCreateEvents.this)) {
                Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + meeting.uid);
                if (bit != null) {
                    holder.civ_icon.setImageBitmap(bit);
                }
            } else {
                if (!TextUtils.isEmpty(meeting.userPic) && !"null".equals(meeting.userPic)) {
                    ImageLoader.getInstance().displayImage(meeting.userPic, holder.civ_icon, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            Bitmap bit = MyApplication.cacheManager.getAsBitmap(Constant.CACHE_USER_PORTRAIT + meeting.uid);
                            if (bit == null) {
                                MyApplication.cacheManager.put(Constant.CACHE_USER_PORTRAIT + meeting.uid, loadedImage);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }
            }

            if (!TextUtils.isEmpty(meeting.creditPoint) && !meeting.creditPoint.equals("null")) {
                holder.tv_xin_yong.setText(meeting.creditPoint);
            } else {
                holder.tv_xin_yong.setText("0");
            }
            holder.tv_place.setText(meeting.meetingAddress);
            holder.tv_time_and_money.setText(meeting.meetingTime + " " + meeting.payType);
            holder.tv_nickname.setText(meeting.nickName + " ");
            if (!TextUtils.isEmpty(meeting.ownerGender) && !meeting.ownerGender.equals(null) && !meeting.ownerGender.equals("1")) {
                holder.iv_gender.setBackgroundResource(R.drawable.female_icon);
            } else {
                holder.iv_gender.setBackgroundResource(R.drawable.male_icon);
            }
            holder.tv_watch_count.setText(meeting.watchCount);
            //holder.tv_sign_up_count.setText("200");
            holder.tv_comment_count.setText(meeting.commentCount);


            if (!TextUtils.isEmpty(meeting.meetingTime) && !meeting.meetingTime.equals("null")) {
                holder.tv_time_and_money.setText(TimeHelper.translateIntToDate(Long.parseLong(meeting.meetingTime)));
            }

            return convertView;
        }
    }

    class ViewHolder {
        public TextView title;
        public CircleImageView civ_icon;
        public TextView tv_xin_yong;
        public TextView tv_place;
        public TextView tv_time_and_money;
        public TextView tv_nickname;
        public ImageView iv_gender;
        public TextView tv_watch_count;
        public TextView tv_comment_count;
    }

    private void cacheCreateMeetings() {
        String obj = MyApplication.cacheManager.getAsString(this.cacheName);
        if (obj != null && !obj.isEmpty()) {
            MyApplication.cacheManager.remove(cacheName);
        }
        MyApplication.cacheManager.put(cacheName, gson.toJson(ls_meetings));
    }

    private void initCreateMeetings() {
        Type type = new TypeToken<List<Meeting>>() {
        }.getType();
        this.ls_meetings = gson.fromJson(MyApplication.cacheManager.getAsString(this.cacheName), type);
    }
}
