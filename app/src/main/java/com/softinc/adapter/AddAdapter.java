package com.softinc.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.config.GlobalData;
import com.softinc.domain.InviteMessage;
import com.softinc.fragment.ContactsFragment;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.SPUtils;
import com.softinc.utils.UserUtils;
import com.softinc.youelink.AlertDialog;
import com.softinc.youelink.HomeActivity;
import com.softinc.youelink.R;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

import  static com.softinc.fragment.ContactsFragment.contactList;
/**
 * Created by sophiemarceau_qu on 15/6/10.
 */
public class AddAdapter extends ArrayAdapter<User> {
    private int res;
    String s1;
    private Context context;
    List<User> userList;
    private static final String TAG = "AddAdapter";
    private ProgressDialog progressDialog;
    public AddAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.res = resource;
        this.userList = objects;
        this.context = context;
    }
    private static class ViewHolder {
        ImageView avator;
        TextView name;
        Button addBtn;
        // TextView time;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        final User user = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.row_add_newfriend, null);
            holder.avator = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.newfriendname);
            holder.addBtn = (Button) convertView.findViewById(R.id.user_state);


            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        for (User temp :contactList){
            if (temp.getUsername().equals(user.getUsername())){
                holder.addBtn.setText("已添加");
            }

        }

        if (user.getBuddyStatus().equals("1")){

            holder.addBtn.setText("等待验证");
            holder.addBtn.setEnabled(false);
        }

        holder.addBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 同意别人发的好友请求
                addFriendRequest(user,holder.addBtn);
            }
        });
        UserUtils.setUserAvatar(context, Constant.URL_ImagePATH + user.getAvatar(), holder.avator);
        holder.name.setText(user.getNick());
        return convertView;
    }


    private  void addFriendRequest( final User msg, final Button clickButton) {
//        Log.e("addadapter","username-----------------------"+msg.getUsername().toString());
//        Log.e("addadapter","MyApplication.getInstance()-----------------------"+MyApplication.getInstance().getUserName());
        if(MyApplication.getInstance().getUserName().equals(msg.getUsername().toString())){
            String str = context.getString(R.string.not_add_myself);

            Intent intent = new Intent(context, AlertDialog.class);
            intent.putExtra("msg", str);
            context.startActivity(intent);



            return;
        }

        if(MyApplication.getInstance().getContactList().containsKey(msg.getUsername().toString())){
            //提示已在好友列表中，无需添加
            if(EMContactManager.getInstance().getBlackListUsernames().contains(msg.getUsername().toString())){
                Intent intent = new Intent(context, AlertDialog.class);
                intent.putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可");
                context.startActivity(intent);
                return;
            }
            String strin = context.getString(R.string.This_user_is_already_your_friend);

            Intent intent = new Intent(context, AlertDialog.class);
            intent.putExtra("msg", strin);
            context.startActivity(intent);
            return;
        }
//        Log.e(TAG,"contactlist------------------>"+contactList);
        for (User user :contactList){
                if (user.getUsername().equals(msg.getUsername())){
                    String strin = context.getString(R.string.This_user_is_already_your_friend);

                    Intent intent = new Intent(context, AlertDialog.class);
                    intent.putExtra("msg", strin);
                    context.startActivity(intent);
                    return;
                }

        }



        progressDialog = new ProgressDialog(context);
        String stri = context.getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

//        new Thread(new Runnable() {
//            public void run() {
//
//                try {
//                    //demo写死了个reason，实际应该让用户手动填入
//                    String s = context.getResources().getString(R.string.Add_a_friend);
//                    EMContactManager.getInstance().addContact(msg.getHxUser(), s);
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        public void run() {
//                            progressDialog.dismiss();
//                            String s1 = context.getResources().getString(R.string.send_successful);
//                            Toast.makeText(context, s1, 1).show();
//                        }
//                    });
//                } catch (final Exception e) {
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        public void run() {
//                            progressDialog.dismiss();
//                            String s2 = context.getResources().getString(R.string.Request_add_buddy_failure);
//                            Toast.makeText(context, s2 + e.getMessage(), 1).show();
//                        }
//                    });
//                }
//            }
//        }).start();



        RequestParams params = new RequestParams();


        params.put("buddyId", msg.getId());
        params.put("buddyHxId", msg.getUsername());

//        Log.e(TAG,"params------>"+params);


        MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.e(TAG, "添加好友 返回值" + response);
                if (ResponseUtils.isResultOK(response)) {
                    try {
                        String dataObj = response.getString("Data");

                        s1 =  "已发送请求，等待对方验证";
                        clickButton.setText("等待验证");
                        clickButton.setEnabled(false);
                        String s = context.getResources().getString(R.string.Add_a_friend);
                        EMContactManager.getInstance().addContact(msg.getUsername(), s);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                            progressDialog.dismiss();

                            Toast.makeText(context, s1, 1).show();
                            }
                        });

                    } catch (Exception e) {
                        PromptUtils.showErrorDialog(context, e.getMessage());
                        e.printStackTrace();

                            String s2 = context.getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(context, s2 + e.getMessage(), 1).show();

                    }

                } else {
                    PromptUtils.showErrorDialog(context, ResponseUtils.getInformation(response));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                PromptUtils.showNoNetWork(context);
            }


        });
    }
}
