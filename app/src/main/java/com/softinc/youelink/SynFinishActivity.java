package com.softinc.youelink;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.softinc.adapter.AddAdapter;
import com.softinc.adapter.NewFriendsMsgAdapter;
import com.softinc.application.MyApplication;
import com.softinc.bean.Contact;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.config.GlobalData;
import com.softinc.utils.PromptUtils;
import com.softinc.utils.ResponseUtils;
import com.softinc.view.Tag;
import com.softinc.view.Title;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sophiemarceau_qu on 15/6/10.
 */
public class SynFinishActivity extends BaseActivity {
    private  static  final String TAG ="SynFinish";
    Context mContext = null;
    Title title;
    /**获取库Phon表字段**/
    private static final String[] PHONES_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID };

    /**联系人显示名称**/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**电话号码**/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**头像ID**/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**联系人的ID**/
    private static final int PHONES_CONTACT_ID_INDEX = 3;


    /**联系人名称**/
    private ArrayList<Contact> mContactList = new ArrayList<Contact>();


    private ArrayList<String> mContactsName = new ArrayList<String>();

    /**联系人头像**/
    private ArrayList<String> mContactsNumber = new ArrayList<String>();

    /**联系人头像**/
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();

    ListView mListView = null;
    MyListAdapter myAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_sync_contact);
        title = (Title) this.findViewById(R.id.tv_name);
        title.setTitleClickListener(new Title.TitleListener() {
            @Override
            public void onLeftTitleClick(View v) {
                SynFinishActivity.this.finish();
            }

            @Override
            public void onRightTitleClick(View v) {

            }
        });
        /**得到手机通讯录联系人信息**/
        getPhoneContacts();
        requestData();
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view,
//                                    int position, long id) {
//                //调用系统方法拨打电话
//                Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri
//                        .parse("tel:" + mContactsNumber.get(position)));
//                startActivity(dialIntent);
//            }
//        });


    }

    /**得到手机通讯录联系人信息**/
    private void getPhoneContacts() {
        ContentResolver resolver = mContext.getContentResolver();

        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);


        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)){
                    continue;
                }


                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);


                //得到联系人ID
                Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

                //得到联系人头像ID
                Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

                //得到联系人头像Bitamp
                Bitmap contactPhoto = null;

                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if(photoid > 0 ) {
                    Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
                    contactPhoto = BitmapFactory.decodeStream(input);
                }else {
                    contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                }
                Contact contact = new Contact();
                contact.setName(contactName);
                phoneNumber = phoneNumber.replaceAll(" ", "");
                contact.setPhoneNumber(phoneNumber);
                contact.setAvator(contactPhoto);
                mContactList.add(contact);

            }

            phoneCursor.close();
        }






    }

    private void requestData() {
        String postPhone ="";
        for(int i =0;i<mContactList.size();i++){
            String phone =mContactList.get(i).getPhoneNumber().replace("+86","");
            String  newphone =phone.replace("-","");
            postPhone =postPhone+""+newphone;
            if (i<mContactList.size()-1){
                postPhone =postPhone+",";
            }
        }
//        Log.e(TAG,"postPhone---------------->"+postPhone);
        RequestParams params = new RequestParams();
        params.put("phoneNumbers", postPhone);
//        Log.e(TAG,"params---------------->"+params);
        MyApplication.client.post(Constant.URL_SyncContact, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.e(TAG,"response---------------->"+response);
                if (ResponseUtils.isResultOK(response)) {
                    try {
                        ArrayList<User> requestfromServercontactList = new ArrayList<User>();
                        JSONArray userAry = response.getJSONArray("Data");
                        List<User> listsearch =new ArrayList<User>();
//                        Log.e(TAG, "userAry------>" +userAry);
                        for (int i = 0; i < userAry.length(); i++) {
                            JSONObject userObj = (JSONObject) userAry.get(i);
                            User user = new User();

                            user.setPhoneNumber(userObj.getString("PhoneNumber"));
                            user.setHxUser(userObj.getString("hxUser"));
                            user.setBuddyStatus(userObj.getString("BuddyStatus"));
                            user.setId(userObj.getString("UID"));
//                            Log.e(TAG, "userAry------>" + userAry);
                            requestfromServercontactList.add(user);
                        }
//                        Log.e(TAG, "requestfromServercontactList------>" + requestfromServercontactList);
                        mListView =(ListView)findViewById(R.id.sync_list);
                        myAdapter = new MyListAdapter(SynFinishActivity.this,1,requestfromServercontactList);
                        mListView.setAdapter(myAdapter);




                    } catch (Exception e) {
                        PromptUtils.showErrorDialog(SynFinishActivity.this, e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    PromptUtils.showErrorDialog(SynFinishActivity.this, ResponseUtils.getInformation(response));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                PromptUtils.showNoNetWork(SynFinishActivity.this);
            }


        });

    }

    /**得到手机SIM卡联系人人信息**/
    private void getSIMContacts() {
        ContentResolver resolver = mContext.getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
                null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                // 得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                // 得到联系人名称
                String contactName = phoneCursor
                        .getString(PHONES_DISPLAY_NAME_INDEX);

                //Sim卡中没有联系人头像

                mContactsName.add(contactName);
                mContactsNumber.add(phoneNumber);
            }

            phoneCursor.close();
        }
    }

    static class MyListAdapter extends ArrayAdapter<User>{
        private int res;
        String s1;
        private Context mContext;
        List<User> userlistFromContact;
        private ProgressDialog progressDialog;
        public MyListAdapter(Context context,int res,List<User> obj) {
            super(context, res, obj);
            mContext = context;
            this.userlistFromContact =obj;
            this.res =res;
        }

        private static class ViewHolder {
            ImageView image;
            TextView name;
            TextView phonename ;
            Button requestBtn;
            // TextView time;
        }


        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final User user = getItem(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.row_sync_contact, null);

                holder.image = (ImageView) convertView.findViewById(R.id.avatar);
                holder.phonename= (TextView) convertView.findViewById(R.id.phone_number);
                holder.requestBtn = (Button) convertView.findViewById(R.id.requestBtn);

                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.requestBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 同意别人发的好友请求
                    finishContact(user, holder.requestBtn);
                }
            });
            String buddyStatus =user.getBuddyStatus();
            if (buddyStatus.equals("0")) {
                holder.requestBtn.setEnabled(true);
                holder.requestBtn.setText("加好友");
            }else if (buddyStatus.equals("1"))
            {
                holder.requestBtn.setEnabled(false);
                holder.requestBtn.setText("等待验证");
            }else if (buddyStatus .equals("2"))
            {
                holder.requestBtn.setEnabled(false);
                holder.requestBtn.setText("已添加");
            }

//            text.setText(mContactList.get(position).getName());
             String phone = user.getPhoneNumber();
            holder.phonename.setText(phone);


            return convertView;
        }

        private void finishContact( final User msg, final Button requestButton) {
            requestButton.setEnabled(false);
            String username = MyApplication.uid;
//            Log.e(TAG, "GlobalData返回数据－－－－－－－" + username);
//            Log.e(TAG, "msg返回数据－－－－－－－" + msg.getId());
            if (username.equals(msg.getId())){
                String message =  "您不能添加自己为好友";
                Toast.makeText(mContext, message, 2).show();
                requestButton.setEnabled(true);
                return;
            }
            progressDialog = new ProgressDialog(mContext);
            String stri = mContext.getResources().getString(R.string.Is_sending_a_request);
            progressDialog.setMessage(stri);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            RequestParams params = new RequestParams();
            params.put("buddyId", msg.getId());
            params.put("buddyHxId",msg.getHxUser());
//            Log.e(TAG, "请求数据URL_ADD_BUDDY－－－－－－－－－－－>" + params);
            MyApplication.client.post(Constant.URL_ADD_BUDDY, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                    Log.e(TAG, "返回数据" + response);
                    if (ResponseUtils.isResultOK(response)) {
                        try {

                            s1 =  "请求添加好友成功，请等待好友应答";

                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();

                                    Toast.makeText(mContext, s1, 1).show();
                                    requestButton.setEnabled(false);
                                    requestButton.setText("等待验证");
                                }
                            });

                            String s = mContext.getResources().getString(R.string.Add_a_friend);
                            EMContactManager.getInstance().addContact(msg.getUsername(), s);
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();

                                    Toast.makeText(mContext, s1, 1).show();
                                }
                            });




//                            new Thread(new Runnable() {
//                                public void run() {
//
//                                    try {
//                                        //demo写死了个reason，实际应该让用户手动填入
//                                        String s = "加个好友呗";
//                                        EMContactManager.getInstance().addContact(toAddUsername, s);
//
//                                        ((Activity) mContext).runOnUiThread(new Runnable() {
//                                            public void run() {
//                                                progressDialog.dismiss();
//                                                String s1 =  mContext.getResources().getString(R.string.send_successful);
//                                                Toast.makeText(mContext, s1, 1).show();
//                                            }
//                                        });
//
//
//
//                                    } catch (final Exception e) {
//
//
//                                        ((Activity) mContext).runOnUiThread(new Runnable() {
//                                            public void run() {
//                                                progressDialog.dismiss();
//                                                String s2 = mContext.getResources().getString(R.string.Request_add_buddy_failure);
//                                                Toast.makeText(mContext, s2 + e.getMessage(), 1).show();
//                                            }
//                                        });
//
//                                    }
//                                }
//                            }).start();

                        } catch (Exception e) {
                            PromptUtils.showErrorDialog(mContext, e.getMessage());
                            e.printStackTrace();
                            String s2 = mContext.getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(mContext, s2 + e.getMessage(), 1).show();
                            requestButton.setEnabled(true);
                        }

                    } else {
                        PromptUtils.showErrorDialog(mContext, ResponseUtils.getInformation(response));
                        requestButton.setEnabled(true);
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    Log.e(TAG, responseString);
                    PromptUtils.showNoNetWork(mContext);
                    requestButton.setEnabled(true);
                }
            });

        }

    }

}