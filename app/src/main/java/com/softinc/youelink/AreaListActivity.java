package com.softinc.youelink;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v-shux on 6/12/2015.
 */
public class AreaListActivity extends Activity {

    private ListView cityList;
    private ProgressDialog pd;

    private List<String> areas = new ArrayList<String>();
    private List<String> areaIds = new ArrayList<String>();

    ArrayAdapter<String> cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areaandjoblist);
        initView();

        cityList.setAdapter(cityAdapter);
        setListData();

        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    /*Intent intent = new Intent(getActivity(), MeetingInfoActivity.class);
                    intent.putExtra("meeting", meetings.get(position));
                    intent.putExtra("user", users.get(position));
                    getActivity().startActivity(intent);*/
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("Area", areas.get(position));
                    bundle.putString("Id", areaIds.get(position));
                    resultIntent.putExtras(bundle);
                    AreaListActivity.this.setResult(RESULT_OK, resultIntent);

                    AreaListActivity.this.finish();
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                }
            }
        });
    }

    private void initView() {
        cityList = (ListView)findViewById(R.id.lv_cityandjob);
        cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, areas);

        pd = new ProgressDialog(AreaListActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getString(R.string.find_neaer_searchhint));
        pd.show();
    }

    private void setListData()
    {
        MyApplication.client.get(Constant.URL_AREA_LIST, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.d(TAG, "areas:" + response);
                try {
                    JSONArray allareas = response.getJSONArray("Data");
                    for (int i = 0; i < allareas.length(); i++) {
                        JSONObject o = (JSONObject) allareas.get(i);
                        String id = o.getString("ID");
                        String name = o.getString("Name");
                        areaIds.add(id);
                        areas.add(name);
                    }
                    cityAdapter.notifyDataSetChanged();
                    pd.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //Log.e(TAG, "xxx" + statusCode, throwable);
            }
        });
    }
}
