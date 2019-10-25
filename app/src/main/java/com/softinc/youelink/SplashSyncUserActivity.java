package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.softinc.youelink.R;
public class SplashSyncUserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_sync_user);
    }

    public void syncContacts(View view) {
        startActivity(new Intent(this, ExchangeCardActivity.class));
        this.finish();
    }

}
