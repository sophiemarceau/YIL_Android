package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.softinc.youelink.R;

public class ExchangeCardActivity extends Activity {

    private ImageView iv_exchangeCard;

    // ===============================================================================
    // 变量
    // ===============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_card);

        initView();
    }

    private void initView() {
        iv_exchangeCard = (ImageView) findViewById(R.id.iv_exchange_card);

        iv_exchangeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExchangeCardActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

}
