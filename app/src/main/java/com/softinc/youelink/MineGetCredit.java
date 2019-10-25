package com.softinc.youelink;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.softinc.view.Title;

public class MineGetCredit extends Activity {
    private Title tv_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_how_get_credit);

        initView();
    }

    private void initView(){
        tv_name = (Title) findViewById(R.id.tv_name);
        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MineGetCredit.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }
}
