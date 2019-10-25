package com.softinc.youelink;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.softinc.bean.Meeting;
import com.softinc.view.Title;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewMeeting1Activity extends Activity {
    public static final int CODE_SEARCH_PLACE = 1;
    private static final String TAG = "NewMeeting1Activity";

    private Title title;
    private EditText et_theme;
    private LinearLayout ll_container;
    private RadioGroup rg_gender;
    private RadioButton rb_women;
    private RadioButton rb_man;
    private RadioButton rb_any_gender;
    private RadioGroup rg_member_level;
    private RadioButton rb_hei_ka;
    private RadioButton rb_pu_tong;
    private TextView tv_date;
    private TextView tv_time;
    private EditText tv_place;
    private EditText tv_description;
    private CheckBox cb_pick;
    private EditText tv_count;

    private String date = "";
    private String time = "";

    private Meeting meeting = new Meeting();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting1);

        title = (Title) this.findViewById(R.id.tv_name);
        ll_container = (LinearLayout) this.findViewById(R.id.ll_container);
        et_theme = (EditText) this.findViewById(R.id.et_theme);
        rg_gender = (RadioGroup) this.findViewById(R.id.rg_gender);
        rb_women = (RadioButton) this.findViewById(R.id.rb_women);
        rb_man = (RadioButton) this.findViewById(R.id.rb_man);
        rb_any_gender = (RadioButton) this.findViewById(R.id.rb_any_gender);
        rg_member_level = (RadioGroup) this.findViewById(R.id.rg_member_level);
        rb_hei_ka = (RadioButton) this.findViewById(R.id.rb_hei_ka);
        rb_pu_tong = (RadioButton) this.findViewById(R.id.rb_pu_tong);
        tv_date = (TextView) this.findViewById(R.id.tv_date);
        tv_time = (TextView) this.findViewById(R.id.tv_time);
        tv_place = (EditText) this.findViewById(R.id.tv_place);
        tv_description = (EditText) this.findViewById(R.id.tv_description);

        cb_pick = (CheckBox) this.findViewById(R.id.cb_pick);
        tv_count = (EditText) this.findViewById(R.id.tv_count);

        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                NewMeeting1Activity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {
                meeting.title = et_theme.getText().toString();
                meeting.description = tv_description.getText().toString();
                meeting.meetingAddress = tv_place.getText().toString();
                meeting.needAccept = cb_pick.isChecked() ? 1 : 0;

                if (!tv_count.getText().toString().isEmpty()) {
                    meeting.memberLimit = Integer.parseInt(tv_count.getText().toString());
                }

                Integer gender_checkId = rg_gender.getCheckedRadioButtonId();
                if (gender_checkId == rb_any_gender.getId()) {
                    meeting.gender = "0";
                } else if (gender_checkId == rb_man.getId()) {
                    meeting.gender = "1";
                } else if (gender_checkId == rb_women.getId()) {
                    meeting.gender = "2";
                }

                Integer level_checkId = rg_member_level.getCheckedRadioButtonId();
                if (level_checkId == rb_pu_tong.getId()) {
                    meeting.memberLevel = "1";
                } else if (level_checkId == rb_hei_ka.getId()) {
                    meeting.memberLevel = "2";
                }

                StringBuilder error = new StringBuilder();
                if (meeting.title.isEmpty()) {
                    error.append("主题不能为空\r\n");
                }

                if (meeting.memberLimit == null || meeting.memberLimit < 0) {
                    error.append("参加人数不能小于0\r\n");
                }

                if (date.isEmpty()) {
                    error.append("日期不能为空\n");
                }

                if (time.isEmpty()) {
                    error.append("时间不能为空\n");
                }

                if (meeting.meetingAddress.isEmpty()) {
                    error.append("地点不能为空\n");
                }

                if (!error.toString().isEmpty()) {
                    Toast.makeText(NewMeeting1Activity.this.getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                } else {
                    meeting.meetingTime = date + " " + time;
                    Intent intent = new Intent(NewMeeting1Activity.this, NewMeeting2Activity.class);
                    intent.putExtra("meeting", meeting);
                    startActivity(intent);
                }
            }
        });

        ll_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ll_container.setFocusable(true);
                ll_container.setFocusableInTouchMode(true);
                ll_container.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_theme.getWindowToken(),0);
                return false;
            }
        });

        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(NewMeeting1Activity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                        date = format.format(calendar.getTime());

                        tv_date.setText(date);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(NewMeeting1Activity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        time = hourOfDay + ":" + (minute > 10 ? minute : "0" + minute);

                        tv_time.setText(time);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });
//        tv_place.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivityForResult(new Intent(NewMeeting1Activity.this, SearchPlaceActivity.class), CODE_SEARCH_PLACE);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == CODE_SEARCH_PLACE) {
            meeting.meetingAddress = data.getStringExtra("address");
            meeting.latitude = data.getStringExtra("latitude");
            meeting.longitude = data.getStringExtra("longitude");

            Log.d(TAG, "搜索地点返回" + meeting.toString());
            tv_place.setText(meeting.meetingAddress);
        }
    }
}
