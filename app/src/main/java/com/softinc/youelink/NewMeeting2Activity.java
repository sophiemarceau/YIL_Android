package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.config.Constant;
import com.softinc.utils.ResponseUtils;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewMeeting2Activity extends Activity implements View.OnClickListener {

    private Title title;
    private TextView tv_ranke;
    private EditText et_youb;
    private Button bt_charge;
    private TextView tv_youb_left;
    private Button bt_submit;

    private Meeting meeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting2);

        Intent intent = this.getIntent();
        meeting = (Meeting) intent.getSerializableExtra("meeting");

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserCoin();
    }

    private void initView() {
        title = (Title) this.findViewById(R.id.tv_name);
        tv_ranke = (TextView) this.findViewById(R.id.tv_ranke);
        et_youb = (EditText) this.findViewById(R.id.et_youb);
        bt_charge = (Button) this.findViewById(R.id.bt_charge);
        tv_youb_left = (TextView) this.findViewById(R.id.tv_youb_left);
        bt_submit = (Button) this.findViewById(R.id.bt_submit);

        bt_submit.setOnClickListener(this);
        bt_charge.setOnClickListener(this);

        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                NewMeeting2Activity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    private Boolean validateForm() {
        String coin = et_youb.getText().toString();
        Boolean result = true;
        if (coin.isEmpty()) {
            Toast.makeText(getApplicationContext(), "填写友币", Toast.LENGTH_LONG);
            result = false;
        } else {
            Integer c = Integer.parseInt(coin);
            if (c < 0) {
                Toast.makeText(getApplicationContext(), "不是5友币，需重新支付！", Toast.LENGTH_LONG).show();
                result = false;
            } else if (c > 100) {
                Toast.makeText(getApplicationContext(), "友币不能超过100，需重新支付！", Toast.LENGTH_LONG).show();
                result = false;
            }
        }

        return result;
    }

    @Override
    public void onClick(View v) {
        if (!validateForm()) {
            return;
        }

        if (v == bt_submit) {
            meeting.payYouB = et_youb.getText().toString();
            RequestParams params = new RequestParams();
            params.put("title", meeting.title);
            params.put("gender", meeting.gender);
            params.put("userLevel", meeting.memberLevel);
            params.put("dateTime", meeting.meetingTime);
            params.put("payType", meeting.payType == null ? 0 : meeting.payType);
            params.put("address", meeting.meetingAddress);
            params.put("latitude", meeting.latitude == null ? 0 : meeting.latitude);
            params.put("longitude", meeting.longitude == null ? 0 : meeting.longitude);
            params.put("coins", et_youb.getText().toString());
            params.put("des", meeting.description);
            params.put("needAccept", meeting.needAccept);
            params.put("memberLimit", meeting.memberLimit);

            MyApplication.client.post(Constant.URL_CREATE_MEETING, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        Boolean success = response.getBoolean("Result");
                        if (!success) {
                            String msg = response.getString("Infomation");
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "创建成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(NewMeeting2Activity.this, HomeActivity.class);
                            startActivity(intent);
                            NewMeeting2Activity.this.finish();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_SHORT).show();

                }
            });
        } else if (v == bt_charge) {
            Intent intent = new Intent(NewMeeting2Activity.this, BuyCoin1Activity.class);
            startActivity(intent);
            ////NewMeeting2Activity.this.finish();
        }
    }

    private void getUserCoin() {
        MyApplication.client.get(Constant.URL_GET_MY_COINS, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (ResponseUtils.isResultOK(response)) {
                    try {
                        Integer obj = response.getInt("Data");
                        tv_youb_left.setText(obj.toString());

                        if (obj > 0) {
                            getUserSort(obj);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUserSort(final int coin) {
        MyApplication.client.get(Constant.URL_EVENT_TOP_LIST, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (ResponseUtils.isResultOK(response)) {
                    try {
                        JSONArray objs = response.getJSONArray("Data");

                        for (int i = 0; i < objs.length(); i++) {
                            JSONObject data = (JSONObject) objs.get(i);
                            Integer coins = data.getInt("Coins");
                            if (coin >= coins) {
                                tv_ranke.setText(Integer.toString(i));
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
    }
}
