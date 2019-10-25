package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.softinc.adapter.AddAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.Coin;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuyCoin1Activity extends BaseActivity {
    private List<Coin> coins = new ArrayList<Coin>();

    private Title title;
    private ListView lv_goods;
    private CoinAdapter coinAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin1);

        initData();
    }

    private void initData() {
        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                BuyCoin1Activity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });

        coinAdapter = new CoinAdapter();
        lv_goods = generateFindViewById(R.id.lv_goods);
        lv_goods.setAdapter(coinAdapter);
        lv_goods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Coin coin = coins.get(position);

                Intent intent = new Intent(BuyCoin1Activity.this, BuyCoin1Activity_1.class);
                intent.putExtra("pid", coin.getID());
                intent.putExtra("name", coin.getName());
                intent.putExtra("price", coin.getPrice());

                BuyCoin1Activity.this.startActivity(intent);
                BuyCoin1Activity.this.finish();
            }
        });

        getGoods();
    }

    private void getGoods() {
        MyApplication.client.get(Constant.URL_GetPurchaseList, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (ResponseUtils.isResultOK(response)) {

                    try {
                        JSONArray data_array = response.getJSONArray("Data");
                        for (int i = 0; i < data_array.length(); i++) {
                            Coin coin = new Coin();
                            JSONObject data = (JSONObject) data_array.get(i);
                            coin.setID(data.getInt("ID"));
                            coin.setName(data.getString("Name"));
                            coin.setPrice(data.getInt("Price"));

                            coins.add(coin);
                        }

                        coinAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {

                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(BuyCoin1Activity.this, responseString, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class CoinAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return coins.size();
        }

        @Override
        public Object getItem(int position) {
            return coins.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            Coin coin = coins.get(position);

            if (holder == null) {
                convertView = View.inflate(BuyCoin1Activity.this, R.layout.good_item, null);
                holder = new ViewHolder();
                holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
                holder.tv_coupon = (TextView) convertView.findViewById(R.id.tv_coupon);
                holder.tv_value = (TextView) convertView.findViewById(R.id.tv_value);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tv_count.setText(coin.getName());
            ////holder.tv_coupon.setText("10%优惠");
            holder.tv_value.setText("￥" + coin.getPrice());

            return convertView;
        }
    }

    class ViewHolder {
        public TextView tv_count;
        public TextView tv_coupon;
        public TextView tv_value;
    }
}
