package com.softinc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.Toast;

import com.easemob.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.config.Constant;
import com.softinc.youelink.FindAddBuddyActivity;
import com.softinc.youelink.FindNearByActivity;
import com.softinc.youelink.FindScanResultActivity;
import com.softinc.youelink.MeetingInfoActivity;
import com.softinc.youelink.R;
import com.softinc.zxing.activity.CaptureActivity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class FindFragment extends Fragment {
    private View view;
    private LinearLayout find_scan_layout;
    private LinearLayout find_near_layout;
    private LinearLayout find_search_layout;

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_find, container, false);
        find_scan_layout = (LinearLayout) view.findViewById(R.id.find_scan_layout);
        find_near_layout = (LinearLayout) view.findViewById(R.id.find_near_layout);
        find_search_layout = (LinearLayout) view.findViewById(R.id.find_search_layout);

        find_scan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivityForResult(new Intent(getActivity(), CaptureActivity.class),0);
                getActivity().startActivity(new Intent(getActivity(), CaptureActivity.class));
            }
        });



        find_near_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FindNearByActivity.class);
                getActivity().startActivity(intent);
            }
        });

        find_search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //search
                Intent intent = new Intent(getActivity(), FindAddBuddyActivity.class);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            final String scanResult = bundle.getString("result");

            final String[] userInfo = scanResult.split("\\|");

            if(!scanResult.isEmpty() && userInfo.length ==5 )
            {
                if (!NetUtils.hasNetwork(getActivity().getApplicationContext())) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.network_unavailable), 0).show();
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("uid", userInfo[2]);

                MyApplication.client.get(Constant.URL_GET_USER_BY_ID, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d(TAG, "used to get user info" + response);
                        try {
                            JSONObject userObj = response.getJSONObject("Data");
                            String BuddyStatus = userObj.getString("BuddyStatus").toString();

                            String bundletext = scanResult + "|" + BuddyStatus;
                            Intent intent = new Intent(getActivity(), FindScanResultActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("result", bundletext);
                            intent.putExtras(bundle);
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(getActivity(), getString(R.string.find_scan_invailddata), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
            {
                Toast.makeText(getActivity(), getString(R.string.find_scan_invailddata), Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}
