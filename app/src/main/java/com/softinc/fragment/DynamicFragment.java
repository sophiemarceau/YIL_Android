package com.softinc.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.utils.ACache;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.TimeHelper;
import com.softinc.youelink.HomeActivity;
import com.softinc.youelink.MeetingInfoActivity;
import com.softinc.youelink.NewMeeting1Activity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

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

import com.softinc.config.Constant;
import com.softinc.youelink.R;
import com.softinc.youelink.SearchMeetingActivity;
import com.softinc.youelink.UserActivity;

public class DynamicFragment extends Fragment {
    private List<Meeting> ls_meetings = new ArrayList<Meeting>();//活动

    private int curPage = 1;//当前数据的页数
    private int curTab = 1;//当前是那一页

    public static final String TAG = "DynamicFragment";

    private View view;
    private ImageButton iv_title_search;
    private ImageButton iv_title_add;
    private RadioGroup rg_tab;
    private PullToRefreshListView lv_meeting;

    private MeetingAdapter meetingAdapter;

    double latitude;
    double longitude;
    LocationManager locationManager;

    private boolean isDataRefreshing = false;

    private ProgressDialog pd;

    private Gson gson = new Gson();
    private String cacheName = Constant.CACHE_MEETINGS;

    public DynamicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        iv_title_search = (ImageButton) view.findViewById(R.id.ib_title_search);
        iv_title_add = (ImageButton) view.findViewById(R.id.ib_title_add);
        rg_tab = (RadioGroup) view.findViewById(R.id.rg_tab);

        lv_meeting = (PullToRefreshListView) view.findViewById(R.id.lv_meeting);
        lv_meeting.setMode(PullToRefreshBase.Mode.BOTH);
        meetingAdapter = new MeetingAdapter();
        lv_meeting.setAdapter(meetingAdapter);

        isDataRefreshing = false;

        pd = new ProgressDialog(getActivity());
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("正在加载...");
        pd.show();

        rg_tab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_hot:
                        changeTab(1);
                        break;
                    case R.id.rb_nearby:
                        changeTab(2);
                        break;
                    case R.id.rb_friends:
                        changeTab(3);
                        break;
                }
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
                    setListData(getTypeFromTabIndex(curTab));
                }

                isDataRefreshing = true;
            }
        });
        lv_meeting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    if (position - 1 < 0) {
                        return;
                    }

                    Meeting meeting = ls_meetings.get(position - 1);

                    if (!CommonUtils.isNetWorkConnected(getActivity())) {
                        String obj = MyApplication.cacheManager.getAsString(Constant.CACHE_MEETING + meeting.id);
                        if (obj == null) {
                            return;
                        }
                    }

                    Intent intent = new Intent(getActivity(), MeetingInfoActivity.class);
                    intent.putExtra("meeting_id", meeting.id);
                    intent.putExtra("meeting", meeting);
                    getActivity().startActivity(intent);

                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        });

        iv_title_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), NewMeeting1Activity.class));
            }
        });
        iv_title_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), SearchMeetingActivity.class));
            }
        });

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        getLocation();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.cacheName += MyApplication.uid;
        setListData(getTypeFromTabIndex(curTab));
    }

    private void setListData(int meetingType) {
        if (!CommonUtils.isNetWorkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();

            Type type = new TypeToken<List<Meeting>>() {
            }.getType();
            String obj = MyApplication.cacheManager.getAsString(this.cacheName);
            if (obj != null) {
                ls_meetings = gson.fromJson(obj, type);
                meetingAdapter.notifyDataSetChanged();
            }

            pd.dismiss();
            return;
        }

        //meetings.clear();
        //users.clear();
        //活动
        RequestParams params = new RequestParams();
        params.put("type", meetingType);
        params.put("page", curPage);

        if (meetingType == 2) {
            params.put("latitude", latitude);
            params.put("longitude", longitude);
        }

        MyApplication.client.get(Constant.URL_QUERY_MEETINGS, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (curPage == 1) {
                    ls_meetings.clear();
                }
                Log.d(TAG, "返回数据" + response);
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        JSONArray meetings = response.getJSONArray("Data");
                        int len = meetings.length();

                        for (int i = 0; i < len; i++) {
                            JSONObject data = (JSONObject) meetings.get(i);
                            Meeting m = new Meeting();
                            m.memberLevel = data.getString("UserLevel");
                            m.meetingAddress = data.getString("EventAddress");
                            m.id = data.getString("EID");
                            m.meetingTime = data.getString("EventDate");
                            m.title = data.getString("Title");
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
                            m.description = data.getString("Description");

                            m.uid = data.getString("UID");
                            m.hxUser = data.getString("hxUser");
                            m.userPic = Constant.URL_ImagePATH + data.getString("UserPic");
                            m.ownerGender = data.getString("OwnerGender");
                            m.nickName = data.getString("NickName");
                            m.creditPoint = data.getString("CreditPoint");
                            m.needAccept = data.getInt("NeedAccept");
                            m.memberLimit = data.getInt("MemberLimit");

                            ls_meetings.add(m);
                        }

                        meetingAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                    }

                    if (isDataRefreshing) {
                        lv_meeting.onRefreshComplete();
                        isDataRefreshing = false;
                    }

                    Object obj = MyApplication.cacheManager.getAsObject(cacheName);
                    if (obj != null) {
                        MyApplication.cacheManager.remove(cacheName);
                    } else {
                        MyApplication.cacheManager.put(cacheName, gson.toJson(ls_meetings));
                    }

                    pd.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    pd.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "失败", throwable);
            }
        });
    }

    private void changeTab(int targetTab) {
        pd.show();

        Log.d(TAG, "第" + targetTab + "页");

        if (targetTab == curTab) return;

        if (curTab == 2) {
            getLocation();
        }

        setListData(getTypeFromTabIndex(targetTab));

        curTab = targetTab;
    }

    private int getTypeFromTabIndex(int targetTab) {
        if (targetTab == 1)
            return Constant.TYPE_HOT;
        else if (targetTab == 2)
            return Constant.TYPE_NEAR;
        else if (targetTab == 3)
            return Constant.TYPE_FRIENDS;
        else {
            return 1;
        }
    }

    private void getLocation() {
        if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null || locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String provider = locationManager.getBestProvider(criteria, true);

            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            } else {
                locationManager.requestLocationUpdates(provider, 10, 1, locationListener);
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.find_near_locationfail), Toast.LENGTH_SHORT).show();
        }
    }

    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getActivity(), getString(R.string.find_near_locationfail), Toast.LENGTH_SHORT).show();
        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            setListData(getTypeFromTabIndex(curTab));

            locationManager.removeUpdates(locationListener);
        }
    };

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
                convertView = View.inflate(getActivity(), R.layout.item_meeting, null);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_name);
                holder.civ_icon = (CircleImageView) convertView.findViewById(R.id.civ_icon);
                holder.tv_xin_yong = (TextView) convertView.findViewById(R.id.tv_xin_yong);
                holder.tv_place = (TextView) convertView.findViewById(R.id.tv_place);
                holder.tv_time_and_money = (TextView) convertView.findViewById(R.id.tv_time_and_money);
                holder.tv_nickname = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.iv_gender = (ImageView) convertView.findViewById(R.id.iv_gender);
                holder.tv_watch_count = (TextView) convertView.findViewById(R.id.tv_watch_count);
                holder.tv_comment_count = (TextView) convertView.findViewById(R.id.tv_comment_count);
                holder.tv_level = (TextView) convertView.findViewById(R.id.tv_level);
                holder.tv_gender = (TextView) convertView.findViewById(R.id.tv_gender);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(meeting.title);

            if (!CommonUtils.isNetWorkConnected(getActivity())) {
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

            holder.civ_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CommonUtils.isNetWorkConnected(getActivity())) {
                        Intent intent = new Intent(getActivity(), UserActivity.class);
                        intent.putExtra("user_id", meeting.uid);

                        getActivity().startActivity(intent);
                    }
                }
            });

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

            if (!TextUtils.isEmpty(meeting.ownerGender) && !meeting.ownerGender.equals("1")) {
                holder.iv_gender.setBackgroundResource(R.drawable.female_icon);
            } else {
                holder.iv_gender.setBackgroundResource(R.drawable.male_icon);
            }

            if (!TextUtils.isEmpty(meeting.memberLevel) && !meeting.memberLevel.equals(null)) {
                if (meeting.memberLevel.equals("2")) {
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
        public TextView tv_level;
        public TextView tv_time_and_money;
        public TextView tv_nickname;
        public ImageView iv_gender;
        public TextView tv_watch_count;
        public TextView tv_comment_count;
        public TextView tv_gender;
    }
}
