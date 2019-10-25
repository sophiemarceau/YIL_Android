package com.softinc.youelink;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.softinc.view.Title;
import com.softinc.youelink.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SearchMeetingActivity extends Activity {

    private Title tv_name;

    private Button btnSearch;
    private EditText et_theme;
    private TextView txtDate;

    private RadioGroup rg_gender;
    private RadioButton rb_women;
    private RadioButton rb_man;
    private RadioButton rb_any_gender;

    private RadioGroup rg_level;
    private RadioButton rb_pu_tong;
    private RadioButton rb_yin_ka;
    private RadioButton rb_jin_ka;
    private RadioButton rb_hei_ka;

    private int gender = 0;
    private int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_meeting);

        initView();
    }

    private void initView() {
        tv_name = (Title) findViewById(R.id.tv_name);

        et_theme = (EditText) findViewById(R.id.et_theme);

        rb_pu_tong = (RadioButton) findViewById(R.id.rb_pu_tong);
        ////rb_jin_ka = (RadioButton) findViewById(R.id.rb_jin_ka);
        rb_hei_ka = (RadioButton) findViewById(R.id.rb_hei_ka);

        rb_women = (RadioButton) findViewById(R.id.rb_women);
        rb_man = (RadioButton) findViewById(R.id.rb_man);
        rb_any_gender = (RadioButton) findViewById(R.id.rb_any_gender);

        txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(SearchMeetingActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        txtDate.setText(format.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        rg_gender = (RadioGroup) findViewById(R.id.rg_gender);
        rg_level = (RadioGroup) findViewById(R.id.rg_member_level);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer gender_checkId = rg_gender.getCheckedRadioButtonId();
                if (gender_checkId == rb_any_gender.getId()) {
                    gender = 0;
                } else if (gender_checkId == rb_women.getId()) {
                    gender = 2;
                } else if (gender_checkId == rb_man.getId()) {
                    gender = 1;
                }

                Integer level_checkId = rg_level.getCheckedRadioButtonId();
                if (level_checkId == rb_pu_tong.getId()) {
                    level = 1;
                } else if (level_checkId == rb_hei_ka.getId()) {
                    level = 2;
                }

                String subject = et_theme.getText().toString();
                Intent intent = new Intent(SearchMeetingActivity.this, SearchMeetingResultActivity.class);
                intent.putExtra("subject", subject);
                intent.putExtra("gender", gender);
                intent.putExtra("date", (txtDate.getText() == "" ? "" : txtDate.getText()));
                intent.putExtra("level", level);

                SearchMeetingActivity.this.startActivity(intent);
            }
        });

        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                SearchMeetingActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }
}
