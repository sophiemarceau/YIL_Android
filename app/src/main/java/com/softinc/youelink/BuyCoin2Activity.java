package com.softinc.youelink;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pingplusplus.android.PaymentActivity;
import com.pingplusplus.android.PingppLog;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.utils.ResponseUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuyCoin2Activity extends BaseActivity {
    private static final String CHANNEL_ALIPAY = "alipay";
    /**
     * 开发者需要填一个服务端URL 该URL是用来请求支付需要的charge。务必确保，URL能返回json格式的charge对象。
     * 服务端生成charge 的方式可以参考ping++官方文档，地址 https://pingxx.com/guidance/server/import
     * <p/>
     * 【 http://218.244.151.190/demo/charge 】是 ping++ 为了方便开发者体验 sdk 而提供的一个临时 url 。
     * 改 url 仅能调用【模拟支付控件】，开发者需要改为自己服务端的 url 。
     */
    private static String YOUR_URL = Constant.URL_ORDER_CREATE_CHARGE;
    public static String URL = YOUR_URL + "?pId=";

    private static final int REQUEST_CODE_PAYMENT = 1;

    private Integer amount;
    private Integer pid;

    private ListView lv_pay;
    private TextView tv_count;
    private TextView tv_coin;
    private Button btn_buy;

    private List<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin2);

        this.amount = getIntent().getIntExtra("price", 0);
        this.pid = getIntent().getIntExtra("pid", 0);
        URL += this.pid;

        items.add(new Item(1, "支付宝"));

        this.initEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //支付页面返回处理
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
                showMsg(result, errorMsg, extraMsg);
            }
        }
    }

    private void initEvents() {
        lv_pay = generateFindViewById(R.id.lv_pay);
        lv_pay.setAdapter(new ItemAdapter(this, items));

        tv_count = generateFindViewById(R.id.tv_count);
        tv_coin = generateFindViewById(R.id.tv_coin);
        btn_buy = generateFindViewById(R.id.btn_buy);

        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PaymentTask().execute(new PaymentRequest(CHANNEL_ALIPAY, amount));
            }
        });
    }

    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        @Override
        protected void onPreExecute() {
            //按键点击之后的禁用，防止重复点击
            btn_buy.setOnClickListener(null);
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {

            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            try {
                //向Your Ping++ Server SDK请求数据
                data = postJson(URL, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            if (null == data) {
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
                return;
            }
            Log.d("charge", data);
            Intent intent = new Intent();
            String packageName = getPackageName();
            ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
            intent.setComponent(componentName);
            intent.putExtra(PaymentActivity.EXTRA_CHARGE, data);
            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
        }
    }

    class PaymentRequest {
        String channel;
        int amount;

        public PaymentRequest(String channel, int amount) {
            this.channel = channel;
            this.amount = amount;
        }
    }

    public void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null != msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null != msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BuyCoin2Activity.this);
        builder.setMessage(str);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    private static String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    private class ItemAdapter extends BaseAdapter {
        private List<Item> mList;
        private Context mContext;

        public ItemAdapter(Context context, List<Item> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
            convertView = _LayoutInflater.inflate(R.layout.activity_mine_detail_spinner_item, null);
            if (convertView != null) {
                TextView tv_item_value = (TextView) convertView.findViewById(R.id.tv_item_value);
                tv_item_value.setText(mList.get(position).getName());
            }
            return convertView;
        }
    }

    class Item {
        private Integer id;
        private String name;

        public Item(Integer id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
