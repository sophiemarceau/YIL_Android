package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by v-shux on 6/5/2015.
 */
public class FindScanResultActivity extends Activity implements View.OnClickListener{

    CircleImageView userIcon;
    TextView nickNmae;
    Button addToFriends;

    String u_nickNmae;
    String u_gender;
    String u_id;
    String u_icon;
    String u_hxid;
    String u_buddystatus;

    Title title;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_scanresult);
        initView();

        Bundle b=getIntent().getExtras();

        String[] userinfo=b.getString("result").split("\\|");
        u_nickNmae = userinfo[0];
        u_gender = userinfo[1];
        u_id = userinfo[2];
        u_icon = userinfo[3];
        u_hxid = userinfo[4];
        u_buddystatus = userinfo[5];

        nickNmae.setText(u_nickNmae);
        String iconPath = Constant.URL_ImagePATH  + u_icon;
        ImageLoader.getInstance().displayImage(iconPath, userIcon);

        if(u_buddystatus.equals("0"))
        {
            addToFriends.setText(R.string.find_scan_add);
            addToFriends.setBackgroundColor(getResources().getColor(R.color.buddystatus_non_added));
        }
        else if(u_buddystatus.equals("1"))
        {
            addToFriends.setText(R.string.find_scan_wait);
            addToFriends.setClickable(false);
            addToFriends.setBackgroundResource(R.color.white);
            addToFriends.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
        }
        else if(u_buddystatus.equals("2"))
        {
            addToFriends.setText(R.string.find_scan_haveadded);
            addToFriends.setClickable(false);
            addToFriends.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
        }
    }

    private void initView() {
        userIcon = (CircleImageView) this.findViewById(R.id.civ_icon);
        nickNmae = (TextView) this.findViewById(R.id.scanresult_nickname);
        addToFriends = (Button) this.findViewById(R.id.scanresult_add);

        addToFriends.setOnClickListener(this);

        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                FindScanResultActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == addToFriends) {
            RequestParams params = new RequestParams();
            params.put("buddyId", u_id);
            params.put("buddyHxId", u_hxid);

            MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d(TAG, "get user info" + response);
                    try {
                        String BuddyStatus = response.getString("Infomation").toString();
                        int status = response.getInt("Data");
                        if(status == 0)
                        {
                            addToFriends.setText(R.string.find_scan_haveadded);
                            addToFriends.setClickable(false);
                            addToFriends.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
                        }
                        else if(status == 1)
                        {
                            String s = getApplicationContext().getResources().getString(R.string.Add_a_friend);
                            EMContactManager.getInstance().addContact(u_hxid, s);
                            addToFriends.setText(R.string.find_scan_wait);
                            addToFriends.setClickable(false);
                            addToFriends.setBackgroundColor(getResources().getColor(R.color.buddystatus_added));
                        }

                        Toast.makeText(FindScanResultActivity.this, BuddyStatus, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(FindScanResultActivity.this, getString(R.string.find_scan_addfail), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
