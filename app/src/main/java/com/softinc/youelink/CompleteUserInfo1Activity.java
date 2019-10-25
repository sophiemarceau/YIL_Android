package com.softinc.youelink;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.softinc.utils.ResponseUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.utils.PromptUtils;

public class CompleteUserInfo1Activity extends Activity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Calendar calendar;//日历
    private static final String TAG = "CompleteUserInfo1";
    private String nickName = null;//昵称
    private Integer gender = Constant.GENDER_MAN;//性别
    private String birthday = null;//生日
    private Integer section = null;//地区
    private Integer job = null;//职业

    private EditText et_nickname;
    private TextView tv_area;
    private TextView tv_job;
    private TextView tv_birthday;
    private RadioGroup rg_gender;
    private RadioButton rb_man;
    private RadioButton rb_women;
    private Button bt_next;
    private DatePickerDialog datePickerDialog;//日期选择对话框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_user_info_1);

        et_nickname = (EditText) this.findViewById(R.id.et_nickname);
        rg_gender = (RadioGroup) this.findViewById(R.id.rg_gender);
        rb_man = (RadioButton) this.findViewById(R.id.rb_man);
        rb_women = (RadioButton) this.findViewById(R.id.rb_women);
        tv_birthday = (TextView) this.findViewById(R.id.tv_birthday);
        bt_next = (Button) this.findViewById(R.id.bt_next);
        tv_area = (TextView) this.findViewById(R.id.tv_section);
        tv_job = (TextView) this.findViewById(R.id.tv_job);

        tv_birthday.setOnClickListener(this);
        tv_area.setOnClickListener(this);
        tv_job.setOnClickListener(this);
        bt_next.setOnClickListener(this);

    }

    /**
     * 下一步点击处理
     *
     * @param view
     */
    public void next(View view) {
        startActivity(new Intent(this, CompleteUserInfo2Activity.class));
        this.finish();
    }

    @Override
    public void onClick(View v) {
        //选择生日
        if (v == tv_birthday) {
            calendar = Calendar.getInstance();
            datePickerDialog = new DatePickerDialog(this, this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        //下一步
        else if (v == bt_next) {
            gender = rg_gender.getCheckedRadioButtonId() == rb_man.getId() ? Constant.GENDER_MAN : Constant.GENDER_WOMEN;
            nickName = et_nickname.getText().toString();

            if (TextUtils.isEmpty(nickName)) {
                PromptUtils.showToast(CompleteUserInfo1Activity.this, "请输入昵称");
                return;
            }

            RequestParams params = new RequestParams();
            params.put("gender", gender);
            params.put("nickName", nickName);
            if (!TextUtils.isEmpty(birthday)) params.put("brithday", birthday);
            if (section != null) params.put("areaId", section);
            if (job != null) params.put("jobId", job);

            MyApplication.client.post(Constant.URL_SET_USER_INFO, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "提交数据返回" + response);
                    if (ResponseUtils.isResultOK(response)) {
                        startActivity(new Intent(CompleteUserInfo1Activity.this, CompleteUserInfo2Activity.class));
                    } else {
                        PromptUtils.showErrorDialog(CompleteUserInfo1Activity.this, ResponseUtils.getInformation(response));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PromptUtils.showNoNetWork(CompleteUserInfo1Activity.this);
                }
            });
        }
        //选择地区
        else if (v == tv_area) {
            selectSection();
        }
        //选择职业
        else if (v == tv_job) {
            selectJob();
        }
    }

    private void selectJob() {
        final List<Integer> ids = new ArrayList<Integer>();
        final List<String> names = new ArrayList<String>();

        MyApplication.client.get(Constant.URL_JOB_LIST, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "得到职业列表:" + response);

                try {
                    JSONArray jobs = response.getJSONArray("Data");
                    for (int i = 0; i < jobs.length(); i++) {
                        JSONObject o = (JSONObject) jobs.get(i);
                        int id = o.getInt("ID");
                        String name = o.getString("Name");
                        ids.add(id);
                        names.add(name);
                    }

                    AlertDialog dialog = new AlertDialog.Builder(CompleteUserInfo1Activity.this)
                            .setTitle("请选则职业")
                            .setItems(names.toArray(new String[names.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = names.get(which);
                                    Log.d(TAG, "点即了" + name + which);
                                    tv_job.setText(name);
                                    job = which;
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.e(TAG, "错误代码" + statusCode, throwable);
            }
        });
    }

    /**
     * 选择地区
     */
    private void selectSection() {
        final List<Integer> ids = new ArrayList<Integer>();
        final List<String> names = new ArrayList<String>();

        MyApplication.client.get(Constant.URL_AREA_LIST, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "得到地区列表:" + response);
                try {
                    JSONArray areas = response.getJSONArray("Data");
                    for (int i = 0; i < areas.length(); i++) {
                        JSONObject o = (JSONObject) areas.get(i);
                        int id = o.getInt("ID");
                        String name = o.getString("Name");
                        ids.add(id);
                        names.add(name);
                    }

                    AlertDialog dialog = new AlertDialog.Builder(CompleteUserInfo1Activity.this)
                            .setTitle("请选择地区")
                            .setItems(names.toArray(new String[names.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = names.get(which);
                                    Log.d(TAG, "点即了" + name + which);
                                    tv_area.setText(name);
                                    section = which;
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.e(TAG, "错误代码" + statusCode, throwable);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Date time = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        birthday = format.format(time);
        Log.d(TAG, "选择的日期是" + birthday);
        tv_birthday.setText(birthday);
    }
}
