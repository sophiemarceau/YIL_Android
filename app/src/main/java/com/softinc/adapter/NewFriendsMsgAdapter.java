package com.softinc.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.InviteMessgeDao;
import com.softinc.domain.InviteMessage;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.utils.UserUtils;
import com.softinc.view.Tag;
import com.softinc.youelink.NewFriendsMsgActivity;
import com.softinc.youelink.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by sophiemarceau_qu on 15/5/27.
 */
public class NewFriendsMsgAdapter extends ArrayAdapter<User> {
private static  final String TAG ="NewFriend";
    private Context context;
    private InviteMessgeDao messgeDao;
    WeakReference<Activity> weak;

    public NewFriendsMsgAdapter(Context context, int textViewResourceId, List<User> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        messgeDao = new InviteMessgeDao(context);
        this.weak = new WeakReference<Activity>((NewFriendsMsgActivity)context);
    }

    private static class ViewHolder {
        ImageView avator;
        TextView name;
        TextView reason;
        Button refuseBtn;
        Button agreeBtn;
        LinearLayout groupContainer;
        TextView groupname;
        // TextView time;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.row_invite_msg, null);
            holder.avator = (ImageView) convertView.findViewById(R.id.avatar);
//            holder.reason = (TextView) convertView.findViewById(R.id.message);
            holder.name = (TextView) convertView.findViewById(R.id.newfriendrequest_name);
            holder.refuseBtn = (Button) convertView.findViewById(R.id.user_state_refuse);
            holder.agreeBtn = (Button) convertView.findViewById(R.id.user_state_agree);
            holder.groupContainer = (LinearLayout) convertView.findViewById(R.id.ll_group);
            holder.groupname = (TextView) convertView.findViewById(R.id.tv_groupName);
            // holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final User user = getItem(position);
        if (user!=null){
            final NewFriendsMsgActivity activity = (NewFriendsMsgActivity) weak.get();
            holder.name.setText(user.getNick());
            holder.refuseBtn.setVisibility(View.VISIBLE);
            holder.refuseBtn.setEnabled(true);
//            holder.refuseBtn.setBackgroundResource(android.R.drawable.btn_default);
            holder.agreeBtn.setVisibility(View.VISIBLE);
            holder.agreeBtn.setEnabled(true);
//            holder.agreeBtn.setBackgroundResource(android.R.drawable.btn_default);
            UserUtils.setUserAvatar(context, Constant.URL_ImagePATH + user.getAvatar(), holder.avator);
            String str1 = context.getResources().getString(R.string.Has_agreed_to_your_friend_request);
            // 设置点击事件
                holder.refuseBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 同意别人发的好友请求
                        holder.refuseBtn.setEnabled(true);
                        handleInvitation(holder.refuseBtn, user, false,activity);
                    }
                });

            holder.agreeBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 同意别人发的好友请求
                    holder.agreeBtn.setEnabled(true);
                    handleInvitation(holder.agreeBtn, user, true,activity);
                }
            });
        }

//        String str2 = context.getResources().getString(R.string.agree);
//
//        String str3 = context.getResources().getString(R.string.Request_to_add_you_as_a_friend);
//        String str4 = context.getResources().getString(R.string.Apply_to_the_group_of);
//        String str5 = context.getResources().getString(R.string.Has_agreed_to);
//        String str6 = context.getResources().getString(R.string.Has_refused_to);
//        final User msg = getItem(position);
//        if (msg != null) {
//
//            holder.name.setText(msg.getNick());
//            // holder.time.setText(DateUtils.getTimestampString(new
//            // Date(msg.getTime())));
//            if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEAGREED) {
//                holder.status.setVisibility(View.INVISIBLE);
//                holder.reason.setText(str1);
//            } else if (msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED || msg.getStatus() == InviteMessage.InviteMesageStatus.BEAPPLYED) {
//                holder.status.setVisibility(View.VISIBLE);
//                holder.status.setEnabled(true);
//                holder.status.setBackgroundResource(android.R.drawable.btn_default);
//                holder.status.setText(str2);
//                if(msg.getStatus() == InviteMessage.InviteMesageStatus.BEINVITEED){
//                    if (msg.getReason() == null) {
//                        // 如果没写理由
//                        holder.reason.setText(str3);
//                    }
//                }else{ //入群申请
//                    if (TextUtils.isEmpty(msg.getReason())) {
//                        holder.reason.setText(str4 + msg.getGroupName());
//                    }
//                }
//                // 设置点击事件
//                holder.status.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // 同意别人发的好友请求
//                        acceptInvitation(holder.status, msg);
//                    }
//                });
//            } else if (msg.getStatus() == InviteMessage.InviteMesageStatus.AGREED) {
//                holder.status.setText(str5);
//                holder.status.setBackgroundDrawable(null);
//                holder.status.setEnabled(false);
//            } else if(msg.getStatus() == InviteMessage.InviteMesageStatus.REFUSED){
//                holder.status.setText(str6);
//                holder.status.setBackgroundDrawable(null);
//                holder.status.setEnabled(false);
//            }

            // 设置用户头像


        return convertView;
    }

    /**
     * 同意好友请求或者群申请
     *
     * @param button
     *
     */
    private void handleInvitation(final Button button, final User msg,Boolean flag, final NewFriendsMsgActivity activity) {
        button.setEnabled(false);
        final ProgressDialog pd = new ProgressDialog(context);
        String str1 = context.getResources().getString(R.string.Are_agree_with);
        final String str2 = context.getResources().getString(R.string.Has_agreed_to);
        final String str3 = context.getResources().getString(R.string.Agree_with_failure);
        pd.setMessage(str1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();


//        Log.e(TAG,"msg-------------->"+msg);
        if (flag) {

//            Log.e(TAG, "同意－－－－－－－－－－－－");
            RequestParams params =new RequestParams();
            params.put("BR_ID", msg.getBR_ID());
//            Log.e(TAG, "请求好友同意－－－－params－－－－－－－－"+params);
            MyApplication.client.post(Constant.URL_RequestAgreeFriend, params, new JsonHttpResponseHandler() {


                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    Log.e(TAG, "response---URL_RequestAgreeFriend---->" + response);
                    try {
                        if (ResponseUtils.isResultOK(response)) {
                            pd.dismiss();

                            Toast.makeText(context, "已同意好友的请求", 2).show();
                            activity.setListData();
                            EMChatManager.getInstance().acceptInvitation(msg.getUsername());
                        } else {
                            pd.dismiss();
                            PromptUtils.showErrorDialog(context, ResponseUtils.getInformation(response));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        pd.dismiss();
                        PromptUtils.showErrorDialog(context, e.getMessage());

                    }finally {
                        button.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.e(TAG, "onFailure-----agree----->" + responseString);
                    pd.dismiss();
                    PromptUtils.showNoNetWork(context);
                    button.setEnabled(true);
                }
            });

        } else {

            RequestParams params =new RequestParams();
//            Log.e(TAG, "disagree－－－－msg－－－－－－－－"+msg);
            params.put("BR_ID", msg.getBR_ID());
//            Log.e(TAG, "disagree－－－－params－－－－－－－－"+params);
            MyApplication.client.post(Constant.URL_RequestDisAgreeFriend, params, new JsonHttpResponseHandler() {


                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    Log.e(TAG, "response----URL_RequestDisAgreeFriend--->" + response);
                    try {
                        if (ResponseUtils.isResultOK(response)) {
//                            Log.e(TAG, "userAry---response---->" + response);
                            pd.dismiss();
                            Toast.makeText(context, "加好友的请求被拒绝", 2).show();
                            activity.setListData();
                            EMChatManager.getInstance().refuseInvitation(msg.getUsername());

                        } else {
                            pd.dismiss();
                            PromptUtils.showErrorDialog(context, ResponseUtils.getInformation(response));
                        }

                    } catch (Exception e) {
                        PromptUtils.showErrorDialog(context, e.getMessage());
                        e.printStackTrace();
                        pd.dismiss();
                    }finally {
                        button.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.e(TAG, "onFailure-----disagree----->" + responseString);
                    pd.dismiss();
                    PromptUtils.showNoNetWork(context);
                    button.setEnabled(true);

                }
            });
        }
    }



}

