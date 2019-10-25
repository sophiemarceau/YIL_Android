package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.engine.UserEngine;
import com.softinc.utils.TimeHelper;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchMeetingResultActivity extends Activity {
    private List<Meeting> meetings = new ArrayList<Meeting>();//活动

    private String subject;
    private Integer gender;
    private String date;
    private Integer level;
    private Integer payType;

    private Title title;
    private PullToRefreshListView lv_meeting;

    private Integer curPage = 1;

    private MeetingAdapter meetingAdapter;
    private Boolean isDataRefreshing = false;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_meeting_result);

        subject = getIntent().getStringExtra("subject");
        gender = (Integer) getIntent().getIntExtra("gender", 0);
        date = getIntent().getStringExtra("date");
        level = (Integer) getIntent().getIntExtra("level", 0);
        payType = (Integer) getIntent().getIntExtra("type", 1);

        meetings.clear();

        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在加载...");
        pd.show();

        initData();

        setListData();
    }

    private void initData() {

        title = (Title) findViewById(R.id.tv_name);
        lv_meeting = (PullToRefreshListView) findViewById(R.id.lv_meeting);
        lv_meeting.setMode(PullToRefreshBase.Mode.BOTH);
        meetingAdapter = new MeetingAdapter();
        lv_meeting.setAdapter(meetingAdapter);

        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                SearchMeetingResultActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {
                Intent intent = new Intent(SearchMeetingResultActivity.this, HomeActivity.class);
                SearchMeetingResultActivity.this.startActivity(intent);
            }
        });

        lv_meeting.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (lv_meeting.isHeaderShown()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String label = format.format(new Date());

                    // 显示最后更新的时间
                    lv_meeting.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(getString(R.string.find_near_refreshdate) + label);

                    curPage = 1;
                } else {
                    curPage += 1;
                    setListData();
                }
                isDataRefreshing = true;
            }
        });

        lv_meeting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position - 1 < 0) {
                    return;
                }

                Meeting meeting = meetings.get(position - 1);

                Intent intent = new Intent(SearchMeetingResultActivity.this, MeetingInfoActivity.class);
                intent.putExtra("meeting_id", meeting.id);
                intent.putExtra("meeting", meeting);

                SearchMeetingResultActivity.this.startActivity(intent);
            }
        });
    }

    private void setListData() {
        RequestParams params = new RequestParams();
        params.put("title", subject);
        params.put("Date", date);
        params.put("userLevel", level);
        params.put("gender", gender);
        params.put("type", "");
        params.put("page", curPage);
        params.put("address", "");

        MyApplication.client.post(Constant.URL_SEARCH_MEETINGS, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray meetings = response.getJSONArray("Data");
                        if (meetings.length() <= 0) {
                            pd.dismiss();
                            Toast.makeText(SearchMeetingResultActivity.this, "无符合条件的结果", Toast.LENGTH_LONG).show();
                            lv_meeting.onRefreshComplete();
                        } else {
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
                                m.commentCount = data.getString("CommentCount");
                                m.watchCount = data.getString("ViewCount");

                                m.hxUser = data.getString("hxUser");
                                m.userPic = Constant.URL_ImagePATH + data.getString("UserPic");
                                m.ownerGender = data.getString("OwnerGender");
                                m.nickName = data.getString("NickName");
                                m.creditPoint = data.getString("CreditPoint");
                                m.needAccept = data.getInt("NeedAccept");
                                m.memberLimit = data.getInt("MemberLimit");

                                SearchMeetingResultActivity.this.meetings.add(m);
                            }

                            meetingAdapter.notifyDataSetChanged();
                            if (curPage == 1 && isDataRefreshing == false) {
                                pd.dismiss();
                            } else {
                                lv_meeting.onRefreshComplete();
                            }
                        }
                    } else {
                        Toast.makeText(SearchMeetingResultActivity.this, response.getString("Message"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });
    }

    private class MeetingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return meetings.size();
        }

        @Override
        public Object getItem(int position) {
            return meetings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            final Meeting meeting = meetings.get(position);

            if (convertView == null) {
                convertView = View.inflate(SearchMeetingResultActivity.this, R.layout.item_meeting, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_name);
                holder.civ_icon = (CircleImageView) convertView.findViewById(R.id.civ_icon);
                holder.tv_xin_yong = (TextView) convertView.findViewById(R.id.tv_xin_yong);
                holder.tv_place = (TextView) convertView.findViewById(R.id.tv_place);
                holder.tv_time_and_money = (TextView) convertView.findViewById(R.id.tv_time_and_money);
                holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.tv_watch_count = (TextView) convertView.findViewById(R.id.tv_watch_count);
                holder.tv_comment_count = (TextView) convertView.findViewById(R.id.tv_comment_count);
                holder.tv_level = (TextView) convertView.findViewById(R.id.tv_level);
                holder.tv_gender = (TextView) convertView.findViewById(R.id.tv_gender);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(meeting.title);
            if (!TextUtils.isEmpty(meeting.userPic) && !"nil".equals(meeting.userPic)) {
                ImageLoader.getInstance().displayImage(meeting.userPic, holder.civ_icon);
            }

            if (!TextUtils.isEmpty(meeting.creditPoint) && !meeting.creditPoint.equals("null")) {
                holder.tv_xin_yong.setText(meeting.creditPoint);
            } else {
                holder.tv_xin_yong.setText("0");
            }

            holder.tv_place.setText(meeting.meetingAddress);

            if (!TextUtils.isEmpty(meeting.meetingTime) && !meeting.meetingTime.equals("null")) {
                holder.tv_time_and_money.setText(TimeHelper.translateIntToDate(Long.parseLong(meeting.meetingTime)));
            }

            holder.tv_nickname.setText(meeting.nickName + " ");

            if (!TextUtils.isEmpty(meeting.ownerGender) && !meeting.ownerGender.equals(null) && !meeting.ownerGender.equals("1")) {
                Drawable nav_up = getResources().getDrawable(R.drawable.female_icon);
                nav_up.setBounds(0, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                holder.tv_nickname.setCompoundDrawables(null, null, nav_up, null);
            }

            if (!TextUtils.isEmpty(meeting.memberLevel) && !meeting.memberLevel.equals(null)) {
                if (meeting.memberLevel.equals("2")) {
                    holder.tv_level.setText("铂金卡");
                } else if (meeting.memberLevel.equals("3")) {
                    holder.tv_level.setText("黑卡");
                }
            }
            if (!TextUtils.isEmpty(meeting.watchCount)) {
                holder.tv_watch_count.setText(meeting.watchCount);
            } else {
                holder.tv_watch_count.setText("0");
            }

            if (!TextUtils.isEmpty(meeting.commentCount)) {
                holder.tv_comment_count.setText(meeting.commentCount);
            } else {
                holder.tv_comment_count.setText("0");
            }

            String gender = "不限";
            switch (meeting.gender) {
                case "1":
                    gender = "男";
                    break;
                case "2":
                    gender = "女";
                    break;
                case "0":
                    gender = "不限";
                    break;
            }
            holder.tv_gender.setText(gender);

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
        public TextView tv_level;
        public TextView tv_watch_count;
        public TextView tv_comment_count;
        public TextView tv_gender;
    }
}
