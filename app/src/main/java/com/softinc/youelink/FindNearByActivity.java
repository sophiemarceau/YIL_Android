package com.softinc.youelink;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.easemob.util.NetUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;




/**
 * Created by v-shux on 6/8/2015.
 */
public class FindNearByActivity extends Activity{

    private List<User> users = new ArrayList<User>();
    private int curPage = 1;//current page
    PullToRefreshListView nearbyList;
    private ProgressDialog pd;
    Title title;
    int isDataRefreshing = 0;

    public BDLocationListener myListener = new MyLocationListener();
    private LocationClient locationClient;
    int UPDATE_TIME = 10;

    double longitude;
    double latitude;

    UserAdapter uAdapter;

    Timer timer;
    private static final int TIME_OUT = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_nearby);
        initView();

        nearbyList.setMode(PullToRefreshBase.Mode.BOTH);

        // pull down to refresh
        nearbyList.getLoadingLayoutProxy(true, false).setPullLabel(getString(R.string.find_near_pulldownhint));
        nearbyList.getLoadingLayoutProxy(true, false).setRefreshingLabel(getString(R.string.find_near_pulldownrefreshhint));
        nearbyList.getLoadingLayoutProxy(true, false).setReleaseLabel(getString(R.string.find_near_pulldownreleasehint));
        // pull up to download
        nearbyList.getLoadingLayoutProxy(false, true).setPullLabel(getString(R.string.find_near_pulluphint));
        nearbyList.getLoadingLayoutProxy(false, true).setRefreshingLabel(getString(R.string.find_near_pulluprefreshhint));
        nearbyList.getLoadingLayoutProxy(false, true).setReleaseLabel(getString(R.string.find_near_pullupreleasehint));


        nearbyList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (nearbyList.isHeaderShown()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String label = format.format(new Date());

                    // get the last update time
                    nearbyList.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(getString(R.string.find_near_refreshdate) + label);

                    curPage = 1;
                    getLocation();
                } else {
                    curPage += 1;
                    setListData();
                }
                isDataRefreshing = 1;
            }
        });
        nearbyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    //user info
                    Intent intent = new Intent(FindNearByActivity.this, UserActivity.class);
                    intent.putExtra("user_id", users.get(position-1).getId());

                    startActivity(intent);
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        });

        getLocation();

        ListView actualListView = nearbyList.getRefreshableView();
        registerForContextMenu(actualListView);

        actualListView.setAdapter(uAdapter);
    }

    private void initView() {
        nearbyList = (PullToRefreshListView) this.findViewById(R.id.lv_nearbypeople);
        uAdapter = new UserAdapter();

        pd = new ProgressDialog(FindNearByActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.find_neaer_searchhint));
        pd.show();

        timer = new Timer(true);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                FindNearByActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    private void getLocation() {
        locationClient = new LocationClient(FindNearByActivity.this);// 设置定位条件
        locationClient.start();
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//是否打开GPS
        option.setCoorType("bd09ll");// 设置返回值的坐标类型。
        option.setProdName("YoueLink");// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);// 设置定时定位的时间间隔。单位毫秒
        locationClient.setLocOption(option);// 注册位置监听器
        locationClient.registerLocationListener(myListener);
        locationClient.requestLocation();

        //timer.schedule(task,60000);
    }


    TimerTask task = new TimerTask( ) {
        public void run ( ) {
            //locationManager.removeUpdates(locationListener);
            Message message = new Message( );
            message.what = TIME_OUT;
            myHandler.sendMessage(message);
        }
    };

    final Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TIME_OUT:
                    //打断线程
                    DismissPd();
                    locationClient.unRegisterLocationListener(myListener);
                    locationClient.stop();
                    Toast.makeText(FindNearByActivity.this, "获取位置信息超时。", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    };

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (location == null) {
                return;
            }
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            setListData();
            /*if (task != null) {
                task.cancel();
                task = null;
            }
            timer.cancel();*/
            locationClient.unRegisterLocationListener(myListener);
            locationClient.stop();
       }
    }



    private void setListData() {

        if (!NetUtils.hasNetwork(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable), 0).show();
            DismissPd();
            return;
        }

        //near
        RequestParams params = new RequestParams();
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("page", curPage);

        MyApplication.client.get(Constant.URL_NearByPeople, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (curPage == 1) {
                    FindNearByActivity.this.users.clear();
                }
                Log.d("ccc", "return data" + response);
                try {
                    JSONArray users = response.getJSONArray("Data");
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject data = (JSONObject) users.get(i);

                        User user = new User();
                        user.iconPath = Constant.URL_ImagePATH + data.getString("UserPic");
                        user.setNick(data.getString("NickName"));
                        user.id = data.getString("UID");
                        user.setDistance(data.getString("distance"));
                        user.setGender(data.getString("Gender"));

                        FindNearByActivity.this.users.add(user);
                    }
                    uAdapter.notifyDataSetChanged();
                    if (isDataRefreshing == 1) {
                        nearbyList.onRefreshComplete();
                        isDataRefreshing = 0;
                    }
                    DismissPd();
                } catch (JSONException e) {
                    e.printStackTrace();
                    DismissPd();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int test1 = statusCode;
                String test2 = responseString;
                Log.d("xxx", "fail", throwable);
                DismissPd();
            }
        });
    }

    private void DismissPd()
    {
        if (pd.isShowing())
        {
            pd.dismiss();
        }
    }


    /**
     * List����������
     */
    private class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            User user = users.get(position);

            if (convertView == null) {
                convertView = View.inflate(FindNearByActivity.this, R.layout.item_find_nearbypeople, null);

                holder = new ViewHolder();
                holder.user_icon = (ImageView) convertView.findViewById(R.id.find_near_usericon);
                holder.nick_name = (TextView) convertView.findViewById(R.id.find_near_nickname);
                holder.distence = (TextView) convertView.findViewById(R.id.find_near_distence);
                holder.user_gender = (ImageView) convertView.findViewById(R.id.find_near_gerder);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!TextUtils.isEmpty(user.iconPath) && !"nil".equals(user.iconPath)) {
                ImageLoader.getInstance().displayImage(user.iconPath, holder.user_icon);
            }
            holder.nick_name.setText(user.getNick());
            holder.distence.setText(getString(R.string.find_near_yue)+String.valueOf(Math.floor(Double.parseDouble(user.getDistance()) / 1000)).replace(".0","")+getString(R.string.find_near_mi));
            if (user.getGender().equals(Constant.GENDER_MAN.toString())) {
                holder.user_gender.setImageResource(R.drawable.near_male_icon);
            }
            else if (user.getGender().equals(Constant.GENDER_WOMEN.toString()))
            {
                holder.user_gender.setImageResource(R.drawable.near_female_icon);
            }

            return convertView;
        }
    }

    class ViewHolder {
        public ImageView user_icon;
        public TextView nick_name;
        public TextView distence;
        public ImageView user_gender;
    }
}
