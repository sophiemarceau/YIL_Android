package com.softinc.youelink;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pingplusplus.android.PaymentActivity;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.utils.ResponseUtils;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONObject;

public class BuyCoin1Activity_1 extends BaseActivity {

    private static final String CHANNEL_ALIPAY = "alipay";
    /**
     * 开发者需要填一个服务端URL 该URL是用来请求支付需要的charge。务必确保，URL能返回json格式的charge对象。
     * 服务端生成charge 的方式可以参考ping++官方文档，地址 https://pingxx.com/guidance/server/import
     * <p/>
     * 【 http://218.244.151.190/demo/charge 】是 ping++ 为了方便开发者体验 sdk 而提供的一个临时 url 。
     * 改 url 仅能调用【模拟支付控件】，开发者需要改为自己服务端的 url 。
     */
    private static String YOUR_URL = Constant.URL_ORDER_CREATE_CHARGE;
    public static String URL;

    private static final int REQUEST_CODE_PAYMENT = 1;

    private Integer amount;
    private Integer pid;
    private String name;

    private Title title;
    private ListView lv_pay;
    private TextView tv_count;
    private TextView tv_coin;
    private Button btn_buy;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_buy_coin2);

        this.amount = getIntent().getIntExtra("price", 0);
        this.pid = getIntent().getIntExtra("pid", 0);
        this.name = getIntent().getStringExtra("name");

        StringBuilder result = new StringBuilder();
        result.append(YOUR_URL);
        result.append("?pId=");
        result.append(this.pid);
        URL = result.toString();

        this.initEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */

                if (result.equals("success")) {
                    BuyCoin1Activity_1.this.finish();
                }
            }
        }
    }

    private void initEvents() {
        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                BuyCoin1Activity_1.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });

        lv_pay = generateFindViewById(R.id.lv_pay);
        ////lv_pay.setAdapter(new ItemAdapter(this, items));

        tv_count = generateFindViewById(R.id.tv_dc);
        tv_count.setText(this.name.toString());

        tv_coin = generateFindViewById(R.id.tv_dm);
        tv_coin.setText(this.amount.toString());

        btn_buy = generateFindViewById(R.id.btn_buy);

        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createCharge(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case 100:
                                    String order = (String) msg.obj;

                                    Intent intent = new Intent();
                                    String packageName = BuyCoin1Activity_1.this.getPackageName();
                                    ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
                                    intent.setComponent(componentName);
                                    intent.putExtra(PaymentActivity.EXTRA_CHARGE, order);
                                    BuyCoin1Activity_1.this.startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                                    break;
                            }
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(BuyCoin1Activity_1.this, ex.getMessage(), Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void createCharge(final Handler handler) {
        MyApplication.client.post(URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                handler.obtainMessage(100, response.toString()).sendToTarget();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Toast.makeText(BuyCoin1Activity_1.this, responseString, Toast.LENGTH_LONG).show();
            }
        });
    }
}
