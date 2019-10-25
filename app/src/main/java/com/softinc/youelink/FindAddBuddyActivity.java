package com.softinc.youelink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by v-shux on 6/10/2015.
 */
public class FindAddBuddyActivity extends Activity{
    EditText tnickNmae;
    RadioGroup tgender;
    RadioButton rb_women;
    RadioButton rb_man;
    RadioButton rb_any_gender;

    EditText tage;
    TextView tarea;
    TextView tjob;
    Button queryBtn;

    Title title;

    String gender;
    String age;
    String nickName;
    String currentAreaId="";
    String currentJobId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_buddy);
        initView();
    }

    private void initView() {
        tnickNmae = (EditText) findViewById(R.id.find_search_nick);
        tgender = (RadioGroup) findViewById(R.id.find_search_gender);
        tage = (EditText) findViewById(R.id.find_search_age);
        tarea = (TextView) findViewById(R.id.find_search_area);
        tjob = (TextView) findViewById(R.id.find_search_job);
        queryBtn = (Button) findViewById(R.id.find_search_btnQuery);
        rb_women = (RadioButton) findViewById(R.id.rb_women);
        rb_man = (RadioButton) findViewById(R.id.rb_man);
        rb_any_gender = (RadioButton) findViewById(R.id.rb_any_gender);

        tarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FindAddBuddyActivity.this, AreaListActivity.class), 0);
            }
        });
        tjob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FindAddBuddyActivity.this, JobListActivity.class), 1);
            }
        });

        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age = tage.getText().toString();
                nickName= String.valueOf(tnickNmae.getText());
                Integer gender_checkId = tgender.getCheckedRadioButtonId();
                if (gender_checkId == rb_any_gender.getId()) {
                    gender = "";
                } else if (gender_checkId == rb_women.getId()) {
                    gender = "2";
                } else if (gender_checkId == rb_man.getId()) {
                    gender = "1";
                }

                RequestParams params = new RequestParams();
                params.put("phoneNumber", "");
                params.put("nickName", nickName);
                params.put("Gender", gender);
                params.put("AreaID", currentAreaId);
                params.put("JobID", currentJobId);
                params.put("age", age);
                params.put("page", 1);

                MyApplication.client.get(Constant.URL_Search, params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("ccc", "return data" + response);
                        try {
                            JSONArray tempusers = response.getJSONArray("Data");
                            if (tempusers.length() == 0) {
                                Toast toast = Toast.makeText(FindAddBuddyActivity.this, "无符合条件的结果", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return;
                            }

                            Intent intent = new Intent(FindAddBuddyActivity.this, FindAddBuddyResultActivity.class);
                            intent.putExtra("nickName", nickName);
                            intent.putExtra("gender", String.valueOf(gender));
                            intent.putExtra("age", age);
                            intent.putExtra("area", currentAreaId);
                            intent.putExtra("job", currentJobId);

                            FindAddBuddyActivity.this.startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    }
                });
            }
        });

        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                FindAddBuddyActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            switch(requestCode)
            {
                case 0:
                    tarea.setText(bundle.getString("Area"));
                    currentAreaId = bundle.getString("Id");
                    break;
                case 1:
                    tjob.setText(bundle.getString("Job"));
                    currentJobId = bundle.getString("Id");
                    break;
            }
        } else {
            Toast.makeText(FindAddBuddyActivity.this, getString(R.string.find_search_cityfail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        //必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}

