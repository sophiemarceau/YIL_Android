package com.softinc.youelink;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.softinc.application.MyApplication;
import com.softinc.bean.Meeting;
import com.softinc.config.Constant;
import com.softinc.utils.ACache;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.QRHelper;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class MineQR extends Activity {
    private ImageView iv_qr;
    private Title tv_name;

    private String qr_code;

    private String cacheName = Constant.CACHE_USER_QR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mine_qr);

        this.cacheName += MyApplication.uid;

        initView();
    }

    private void initView() {
        iv_qr = (ImageView) findViewById(R.id.iv_qr);

        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();

            this.qr_code = MyApplication.cacheManager.getAsString(this.cacheName);
            initQR(qr_code);
            return;
        }

        MyApplication.client.get(Constant.URL_USER_QR, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("Result");
                    if (success) {
                        qr_code = response.getString("Data");

                        initQR(qr_code);

                        cacheUserQR(qr_code);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        tv_name = (Title) findViewById(R.id.tv_name);
        tv_name.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                MineQR.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
    }

    private void cacheUserQR(String qr_code) {
        String result = MyApplication.cacheManager.getAsString(this.cacheName);
        if (result != null && !result.isEmpty()) {
            MyApplication.cacheManager.remove(this.cacheName);
        }
        MyApplication.cacheManager.put(this.cacheName, qr_code);
    }

    private void initQR(String qr_code) {
        try {
            Bitmap qrCodeBitmap = QRHelper.createQRCode(qr_code, 350);
            iv_qr.setImageBitmap(qrCodeBitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
