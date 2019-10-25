package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by v-shux on 6/12/2015.
 */
public class FindAddBuddyResultActivity  extends Activity {

    private List<User> users = new ArrayList<User>();
    private int curPage = 1;//current page
    private PullToRefreshListView buddyList;
    private ProgressDialog pd;
    private boolean isDataRefreshing;
    private UserAdapter uAdapter;

    private String nickName;
    private String gender;
    private String age;
    private String areaId;
    private String jobId;

    private Title title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_searchbuddyresult);
        initView();
        Intent intent = getIntent();
        nickName = intent.getStringExtra("nickName");
        gender = intent.getStringExtra("gender");
        age = intent.getStringExtra("age");
        areaId = intent.getStringExtra("area");
        jobId = intent.getStringExtra("job");

        buddyList.setMode(PullToRefreshBase.Mode.BOTH);

        // pull down to refresh
        buddyList.getLoadingLayoutProxy(true, false).setPullLabel(getString(R.string.find_near_pulldownhint));
        buddyList.getLoadingLayoutProxy(true, false).setRefreshingLabel(getString(R.string.find_near_pulldownrefreshhint));
        buddyList.getLoadingLayoutProxy(true, false).setReleaseLabel(getString(R.string.find_near_pulldownreleasehint));
        // pull up to download
        buddyList.getLoadingLayoutProxy(false, true).setPullLabel(getString(R.string.find_near_pulluphint));
        buddyList.getLoadingLayoutProxy(false, true).setRefreshingLabel(getString(R.string.find_near_pulluprefreshhint));
        buddyList.getLoadingLayoutProxy(false, true).setReleaseLabel(getString(R.string.find_near_pullupreleasehint));


        buddyList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (buddyList.isHeaderShown()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String label = format.format(new Date());

                    // get the last update time
                    buddyList.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(getString(R.string.find_near_refreshdate) + label);

                    curPage = 1;
                    setListData();
                } else {
                    curPage += 1;
                    setListData();
                }
                isDataRefreshing = true;
            }
        });

        ListView actualListView = buddyList.getRefreshableView();
        registerForContextMenu(actualListView);
        actualListView.setAdapter(uAdapter);

        setListData();
    }

    private void initView() {
        buddyList = (PullToRefreshListView) this.findViewById(R.id.lv_buddyresult);
        uAdapter = new UserAdapter();
        pd = new ProgressDialog(FindAddBuddyResultActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.find_neaer_searchhint));
        pd.show();

        isDataRefreshing = false;

        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                FindAddBuddyResultActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    private void setListData() {
        if (!NetUtils.hasNetwork(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable), 0).show();
            DismissPd();
            return;
        }
        //near
        RequestParams params = new RequestParams();
        params.put("phoneNumber", "");
        params.put("nickName", nickName);
        params.put("Gender", gender);
        params.put("AreaID", areaId);
        params.put("JobID", jobId);
        params.put("age", age);
        params.put("page", curPage);

        MyApplication.client.get(Constant.URL_Search, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                if (curPage == 1) {
                    FindAddBuddyResultActivity.this.users.clear();
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
                        user.hxUser = data.getString("hxUser");
                        user.BuddyStatus = data.getString("BuddyStatus");

                        FindAddBuddyResultActivity.this.users.add(user);
                    }
                    uAdapter.notifyDataSetChanged();
                    if (isDataRefreshing == true) {
                        buddyList.onRefreshComplete();
                        isDataRefreshing = false;
                    }
                    DismissPd();

                } catch (JSONException e) {
                    e.printStackTrace();
                    DismissPd();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
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
         updateHandler handler = new updateHandler();

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
            final User user = users.get(position);

            if (convertView == null) {
                convertView = View.inflate(FindAddBuddyResultActivity.this, R.layout.item_findbuddyresult, null);

                holder = new ViewHolder();
                holder.user_icon = (CircleImageView) convertView.findViewById(R.id.find_buddy_icon);
                holder.nick_name = (TextView) convertView.findViewById(R.id.find_buddy_nickname);
                holder.buddy_add = (Button) convertView.findViewById(R.id.find_buddy_add);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!TextUtils.isEmpty(user.iconPath) && !"nil".equals(user.iconPath)) {
                ImageLoader.getInstance().displayImage(user.iconPath, holder.user_icon);
            }
            holder.nick_name.setText(user.getNick());

            if(user.getBuddyStatus().equals("0"))
            {
                holder.buddy_add.setText(R.string.find_scan_add);
                holder.buddy_add.setBackgroundColor(getResources().getColor(R.color.buddystatus_non_added));
            }
            else if(user.getBuddyStatus().equals("1"))
            {
                holder.buddy_add.setText(R.string.find_scan_wait);
                holder.buddy_add.setClickable(false);
                holder.buddy_add.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
            }
            else if(user.getBuddyStatus().equals("2"))
            {
                holder.buddy_add.setText(R.string.find_scan_haveadded);
                holder.buddy_add.setClickable(false);
                holder.buddy_add.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
            }

            holder.buddy_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( final View v) {
                    if(user.getId() == MyApplication.uid)
                    {
                        Toast.makeText(FindAddBuddyResultActivity.this, "不能加自己为好友", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (user.BuddyStatus.equals("0")) {
                        RequestParams params = new RequestParams();
                        params.put("buddyId", user.getId());
                        params.put("buddyHxId", user.getHxUser());

                        MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d(TAG, "get user info" + response);
                                try {

                                    int status = response.getInt("Data");
                                    if(status == 0) {
                                        user.setBuddyStatus("2");
                                    }
                                    else if(status == 1)
                                    {
                                        user.setBuddyStatus("1");
                                        String s = getApplicationContext().getResources().getString(R.string.Add_a_friend);
                                        EMContactManager.getInstance().addContact(user.getHxUser(), s);
                                    }

                                    String BuddyStatus = response.getString("Infomation").toString();
                                    Toast.makeText(FindAddBuddyResultActivity.this, BuddyStatus, Toast.LENGTH_SHORT).show();

                                    Message msg = new Message();
                                    Bundle b = new Bundle();// 存放数据
                                    msg.obj = v;
                                    msg.arg1 = status;
                                    msg.setData(b);
                                    handler.sendMessage(msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(FindAddBuddyResultActivity.this, getString(R.string.find_scan_addfail), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            return convertView;
        }
    }


    class updateHandler extends Handler {
        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(Message msg) {
            Button add = (Button)msg.obj;
            add.setClickable(false);
            if(msg.arg1 == 0) {
                add.setText(R.string.find_scan_haveadded);
                add.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
            }
            else if(msg.arg1 == 1) {
                add.setText(R.string.find_scan_wait);
                add.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
            }
        }
    }

    class ViewHolder {
        public CircleImageView user_icon;
        public TextView nick_name;
        public Button buddy_add;
    }
}
