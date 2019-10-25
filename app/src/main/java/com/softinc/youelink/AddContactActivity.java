package com.softinc.youelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.adapter.AddAdapter;
import com.softinc.adapter.NewFriendsMsgAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.config.GlobalData;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.SPUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sophiemarceau_qu on 15/5/27.
 */
public class AddContactActivity extends BaseActivity{
    private  static  final String TAG ="AddContactActivity";
    private EditText editText;
    private LinearLayout searchedUserLayout;
    private TextView nameText,mTextView;
    private Button searchBtn;
    private ImageView avatar;
    private InputMethodManager inputMethodManager;
    private String toAddUsername;
    private ProgressDialog progressDialog;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        mTextView = (TextView) findViewById(R.id.add_list_friends);

        editText = (EditText) findViewById(R.id.edit_note);
        String strAdd = getResources().getString(R.string.add_friend);
        mTextView.setText(strAdd);
        String strUserName = "手机号";
        editText.setHint(strUserName);
        searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
        nameText = (TextView) findViewById(R.id.name);
        searchBtn = (Button) findViewById(R.id.search);
        avatar = (ImageView) findViewById(R.id.avatar);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        listView =(ListView)findViewById(R.id.list);
    }


    /**
     * 查找contact
     * @param v
     */
    public void searchContact(View v) {
        final String name = editText.getText().toString();
        String saveText = searchBtn.getText().toString();
//        Log.e(TAG,"name------------>"+name);
//        Log.e(TAG,"saveText------------>"+saveText);
        if (getString(R.string.button_search).equals(saveText)) {
            toAddUsername = name;
            if(TextUtils.isEmpty(name)) {
                String st = "请输入手机号";
                startActivity(new Intent(this, AlertDialog.class).putExtra("msg", st));
                return;
            }
            if(name.length()>11){

                String st = "请输入手机号不能超过11位";
                startActivity(new Intent(this, AlertDialog.class).putExtra("msg", st));
                return;
            }

            // TODO 从服务器获取此contact,如果不存在提示不存在此用户

//          服务器存在此用户，显示此用户和添加按钮

            RequestParams params = new RequestParams();
            params.put("phoneNumber", name);
            params.put("nickName", "");
            params.put("Gender", "");
            params.put("AreaID", "");
            params.put("age", "");
            params.put("page","1");
            MyApplication.client.post(Constant.URL_Search, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    Log.e(TAG,"onSuccess----------------------------------------------->"+response);

                    if (ResponseUtils.isResultOK(response)) {
                        try {

                            JSONArray userAry = response.getJSONArray("Data");
                            if(userAry.length()==0){
                                String strin = "搜索不到该用户，没有结果";

                                Intent intent = new Intent(AddContactActivity.this, AlertDialog.class);
                                intent.putExtra("msg", strin);
                                AddContactActivity.this.startActivity(intent);
                                    return;
                            }
                            List<User> listsearch =new ArrayList<User>();
                            for (int i = 0; i < userAry.length(); i++) {
                                JSONObject userObj = (JSONObject) userAry.get(i);
                                User user = new User();

                                user.setId(userObj.getString("UID"));
                                user.setNick(userObj.getString("NickName"));
                                user.setUsername(userObj.getString("hxUser"));
                                user.setAvatar(userObj.getString("UserPic"));
                                user.setBuddyStatus(userObj.getString("BuddyStatus"));
                                listsearch.add(user);
                            }
                            searchedUserLayout.setVisibility(View.GONE);


                            AddAdapter adapter = new AddAdapter(AddContactActivity.this, 1, listsearch);
                            listView.setAdapter(adapter);






                        } catch (JSONException e) {
                            PromptUtils.showErrorDialog(AddContactActivity.this, e.getMessage());
                            e.printStackTrace();
                        }

                    } else {
                        PromptUtils.showErrorDialog(AddContactActivity.this, ResponseUtils.getInformation(response));
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    PromptUtils.showNoNetWork(AddContactActivity.this);
                }


            });
        }



    }

    /**
     *  添加contact
     * @param view
     */
    public void addContact(View view){
//        Log.e("searchContact","MyApplication.getInstance().getUserName()------>"+MyApplication.getInstance().getUserName());

//        Log.e("searchContact","nameText------>"+nameText.getText().toString());
        if(MyApplication.getInstance().getUserName().equals(nameText.getText().toString())){
            String str = getString(R.string.not_add_myself);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", str));
            return;
        }

        if(MyApplication.getInstance().getContactList().containsKey(nameText.getText().toString())){
            //提示已在好友列表中，无需添加
            if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
                startActivity(new Intent(this, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
                return;
            }
            String strin = getString(R.string.This_user_is_already_your_friend);
            startActivity(new Intent(this, AlertDialog.class).putExtra("msg", strin));
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo写死了个reason，实际应该让用户手动填入
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMContactManager.getInstance().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, 1).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), 1).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void back(View v) {
        finish();
    }
}

