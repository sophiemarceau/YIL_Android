package com.softinc.youelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;

public class SearchPlaceActivity extends Activity {
    private static final String TAG = "SearchPlaceActivity";
    private List<PoiInfo> allPoi;
    private PoiSearch poiSearch;
    private MAdapter mAdapter;

    private EditText et_place;
    private ListView lv_poi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search_place);

        et_place = (EditText) this.findViewById(R.id.et_place);
        lv_poi = (ListView) this.findViewById(R.id.lv_poi);

        et_place.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s.toString())) return;
                Log.d(TAG, "文字改变" + s.toString());
                poiSearch.searchInCity(new PoiCitySearchOption().city("北京").keyword(s.toString()).pageNum(1));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        lv_poi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiInfo poiInfo = allPoi.get(position);
                Intent intent = new Intent(SearchPlaceActivity.this, NewMeeting1Activity.class);
                intent.putExtra("name", poiInfo.name);
                intent.putExtra("address", poiInfo.address);
                intent.putExtra("latitude", Double.toString(poiInfo.location.latitude));
                intent.putExtra("longitude", Double.toString(poiInfo.location.longitude));

                setResult(RESULT_OK, intent);
                SearchPlaceActivity.this.finish();
            }
        });

        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(poiListener);
    }

    /**
     * 地图返回POI监听
     */
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult result) {
            //获取POI检索结果
            allPoi = result.getAllPoi();
            if (allPoi != null && allPoi.size() > 0) {
                Log.d(TAG, "得到POI" + allPoi.size());
                setListDate();
            }
        }
        @Override
        public void onGetPoiDetailResult(PoiDetailResult result) {
            //获取Place详情页检索结果
            Integer i = 0;
        }

    };

    private void setListDate() {
        if (lv_poi.getAdapter() == null) {
            mAdapter = new MAdapter();
            lv_poi.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    class MAdapter extends BaseAdapter {

        /**
         * 设置ListView的总项数
         */
        @Override
        public int getCount() {
            return allPoi.size();
        }

        /**
         * 设置当前项的数据
         */
        @Override
        public Object getItem(int position) {
            return allPoi.get(position);
        }

        /**
         * 设置当前项的序号
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 绘制ListView每一项
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PoiInfo poiInfo = allPoi.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(SearchPlaceActivity.this, R.layout.item_place_info, null);
                holder = new ViewHolder();
                holder.tv_place_name = (TextView) convertView.findViewById(R.id.tv_place_name);
                holder.tv_place_address = (TextView) convertView.findViewById(R.id.tv_place_address);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tv_place_name.setText(poiInfo.name);
            holder.tv_place_address.setText(poiInfo.address);

            return convertView;
        }
    }

    class ViewHolder {
        public TextView tv_place_name;
        public TextView tv_place_address;
    }

}
