/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.softinc.youelink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.util.HanziToPinyin;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.adapter.ContactAdapter;
import com.softinc.application.MyApplication;
import com.softinc.view.Tag;
import com.softinc.widget.Sidebar;
import static com.softinc.fragment.ContactsFragment.usersfromServerList;
//import static com.softinc.fragment.ContactsFragment;

public class GroupPickContactsActivity extends BaseActivity {
    private  static final  String TAG ="pickContact";
    private ListView listView;
    /** 是否为一个新建的群组 */
    protected boolean isCreatingNewGroup;
    /** 是否为单选 */
    private boolean isSignleChecked;
    private PickContactAdapter contactAdapter;
    /** group中一开始就有的成员 */
    private List<String> exitingMembers;
    private List<User> alluserList;
    private String groupId;
    private String username;

    List<String> members_nicknameList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_pick_contacts);

        // String groupName = getIntent().getStringExtra("groupName");
         groupId = getIntent().getStringExtra("groupId");

        if(groupId.equals("创建群聊")){
//            Log.e(TAG,"groupId--------------------------------,"+groupId);
            groupId = null;
            username = getIntent().getStringExtra("nowchatname");
            exitingMembers = new ArrayList<String>();
            exitingMembers.add(username);
        }
        if (groupId == null) {// 创建群组
            isCreatingNewGroup = true;
        }else {
            // 获取此群组的成员列表
            EMGroup group = EMGroupManager.getInstance().getGroup(groupId);
            exitingMembers = group.getMembers();
//            Log.e(TAG,"exitingmembers--------->"+exitingMembers);
        }

        if(exitingMembers == null){

            exitingMembers = new ArrayList<String>();
        }

        // 获取好友列表
//        final List<User> alluserList = new ArrayList<User>();
//                        for (User temp:usersfromServerList){
//                    if (temp.getUsername().equals(username)){
//                        if (usersfromServerList.get(position).getNick()!=null){
//                            Log.e(TAG,"userfromserverlist------>"+usersfromServerList.get(position).getNick());
//                            holder.textView.setText(usersfromServerList.get(position).getNick());
//
//                        }
//                    }
//                }
        // 对list进行排序
//        exitingMembers.addAll(usersfromServerList);
//        Collections.sort(usersfromServerList, new Comparator<User>() {
//            @Override
//            public int compare(User lhs, User rhs) {
//                return (lhs.getUsername().compareTo(rhs.getUsername()));
//
//            }
//        });

        Collections.sort(usersfromServerList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                String pinyin1=  HanziToPinyin.getInstance().get(lhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                String pinyin2=  HanziToPinyin.getInstance().get(rhs.getNick().substring(0, 1)).get(0).target.substring(0, 1).toUpperCase();
                return pinyin1.compareTo(pinyin2);
            }
        });





        initGroupPcikView();


    }

    private void initGroupPcikView() {
        listView = (ListView) findViewById(R.id.list);
        contactAdapter = new PickContactAdapter(this, R.layout.row_contact_with_checkbox, usersfromServerList);
        listView.setAdapter(contactAdapter);
        ((Sidebar) findViewById(R.id.sidebar)).setListView(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                checkBox.toggle();

            }
        });
    }

    /**
     * 确认选择的members
     *
     * @param v
     */
    public void save(View v) {
        Intent intent = this.getIntent();


        intent.putExtra("groupNameStr", getToBeAddMembersUser());
        intent.putExtra("newmembers", getToBeAddMembers().toArray(new String[0]));
        this.setResult(RESULT_OK, intent);
        finish();


    }

    /**
     * 获取要被添加的成员
     *
     * @return
     */
    private List<String> getToBeAddMembers() {
        List<String> members = new ArrayList<String>();
        int length = contactAdapter.isCheckedArray.length;
        for (int i = 0; i < length; i++) {
            String username = contactAdapter.getItem(i).getUsername();
            if(isCreatingNewGroup ==true){
                if (contactAdapter.isCheckedArray[i]){
                    members.add(username);
                }
            }else{
                if (contactAdapter.isCheckedArray[i] && !exitingMembers.contains(username)) {
                    members.add(username);
                }


            }

        }

        return members;
    }


    private String getToBeAddMembersUser() {
        List<String> members =  new ArrayList<String>();
        int length = contactAdapter.isCheckedArray.length;
        for (int i = 0; i < length; i++) {
            String userStr = contactAdapter.getItem(i).getUsername();


            if(isCreatingNewGroup ==true){
                if (contactAdapter.isCheckedArray[i]){

                    for (User t:usersfromServerList){
                        if (t.getUsername().equals(userStr)){
                            members.add(t.getNick());
                        }
                    }

                }
            }else{
                if (contactAdapter.isCheckedArray[i] && !exitingMembers.contains(userStr)) {
                    for (User t:usersfromServerList){
                        if (t.getUsername().equals(userStr)){
                            members.add(t.getNick());
                        }
                    }

                }


            }

        }


        int size = members.size();
        String nicknameString ="";
        for (int i = 0; i <size; i++) {
                if (i == size - 1) {//拼接时，不包括最后一个&字符
                    nicknameString =nicknameString +members.get(i)+"...";

                } else {
                    nicknameString =nicknameString +members.get(i)+"、";
                }
        }
        return nicknameString;
    }

    /**
     * adapter
     */
    private class PickContactAdapter extends ContactAdapter {

        private boolean[] isCheckedArray;

        public PickContactAdapter(Context context, int resource, List<User> users) {
            super(context, resource, users);
            isCheckedArray = new boolean[users.size()];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
//			if (position > 0) {
            final String username = getItem(position).getUsername();

            // 选择框checkbox
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
//            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            final TextView name =(TextView) view.findViewById(R.id.name);
            for (int i = 0; i < usersfromServerList.size(); i++)  //外循环是循环的次数
            {
                for (int j = usersfromServerList.size() - 1 ; j > i; j--)  //内循环是 外循环一次比较的次数
                {



                    if (usersfromServerList.get(i)==usersfromServerList.get(j)){

                        usersfromServerList.remove(j);
                    }

                }
            }



            for (User temp:usersfromServerList){
                if (temp.getUsername().equals(username)){
                    name.setText(usersfromServerList.get(position).getNick());
                }
            }

            if(exitingMembers != null && exitingMembers.contains(username)){
                checkBox.setButtonDrawable(R.drawable.checkbox_bg_gray_selector);
            }else{
                checkBox.setButtonDrawable(R.drawable.checkbox_bg_selector);
            }
            if (checkBox != null) {
                // checkBox.setOnCheckedChangeListener(null);

                checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // 群组中原来的成员一直设为选中状态
                        if (exitingMembers.contains(username)) {
                            isChecked = true;
                            checkBox.setChecked(true);
                        }
                        isCheckedArray[position] = isChecked;
                        //如果是单选模式
                        if (isSignleChecked && isChecked) {
                            for (int i = 0; i < isCheckedArray.length; i++) {
                                if (i != position) {
                                    isCheckedArray[i] = false;
                                }
                            }
                            contactAdapter.notifyDataSetChanged();
                        }

                    }
                });
                // 群组中原来的成员一直设为选中状态
                if (exitingMembers.contains(username)) {
                    checkBox.setChecked(true);
                    isCheckedArray[position] = true;
                } else {
                    checkBox.setChecked(isCheckedArray[position]);
                }
            }
            return view;
        }
    }

    public void back(View view){
        finish();
    }

}
