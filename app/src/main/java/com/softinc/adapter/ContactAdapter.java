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
package com.softinc.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.utils.UserUtils;
import com.easemob.util.EMLog;
import com.softinc.youelink.R;

/**
 * 简单的好友Adapter实现
 *
 */
public class ContactAdapter extends ArrayAdapter<User>  implements SectionIndexer{
    private static final String TAG = "ContactAdapter";
    List<String> list;
    List<User> userList;
    List<User> copyUserList;
    private LayoutInflater layoutInflater;
    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private int res;
    private int newfriendCount ;
    private MyFilter myFilter;
    private boolean notiyfyByFilter;

    public ContactAdapter(Context context, int resource, List<User> objects ) {
        super(context, resource, objects);
        this.res = resource;
        this.newfriendCount =newfriendCount;

        this.userList = objects;
        copyUserList = new ArrayList<User>();
        copyUserList.addAll(objects);
        layoutInflater = LayoutInflater.from(context);
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView unreadMsgView;
        TextView nameTextview;
        TextView tvHeader;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(res, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.unreadMsgView = (TextView) convertView.findViewById(R.id.unread_msg_contact_number);
            holder.nameTextview = (TextView) convertView.findViewById(R.id.name);
            holder.tvHeader = (TextView) convertView.findViewById(R.id.header);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        User user = getItem(position);
//        Log.e("ContactAdapter", user.getUsername() + "----------------->"+user.getNick());

//        Log.e("ContactAdapter",  "userList----------------->"+userList);
//        if(user == null)
//            Log.e("ContactAdapter", position + "");
        //设置nick，demo里不涉及到完整user，用username代替nick显示
        String nickname = user.getNick();
        String username = user.getUsername();
        String header = user.getHeader();
        if (position == 0 || header != null && !header.equals(getItem(position - 1).getHeader())) {
            if ("".equals(header)) {
                holder.tvHeader.setVisibility(View.GONE);
            } else {
                holder.tvHeader.setVisibility(View.VISIBLE);
                holder.tvHeader.setText(header);
            }
        } else {
            holder.tvHeader.setVisibility(View.GONE);
        }

        //显示申请与通知item
        if(username.equals(Constant.NEW_FRIENDS_USERNAME)){
            holder.nameTextview.setText(user.getNick());
            User  user_NEW_FRIENDS_USERNAME = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
            holder.nameTextview.setText(user.getNick());
            holder.avatar.setImageResource(R.drawable.new_friends_icon);
            Log.e(TAG, "返回调------------------------------------显示申请与通知item------->" + this.newfriendCount +
                    "<------------------------------------------------显示申请与通知item");
            if(this.newfriendCount > 0){
                holder.unreadMsgView.setVisibility(View.VISIBLE);
//                holder.unreadMsgView.setText(user_NEW_FRIENDS_USERNAME.getUnreadMsgCount()+"");




                holder.unreadMsgView.setText(this.newfriendCount+"");
            }else{
                holder.unreadMsgView.setVisibility(View.INVISIBLE);
            }

        }else if(username.equals(Constant.GROUP_USERNAME)){
            //群聊item
            holder.nameTextview.setText(user.getNick());
            holder.avatar.setImageResource(R.drawable.groups_icon);
        }else{

          for (User temp: userList){
              if (username.equals(temp.getUsername())){
                  holder.nameTextview.setText(temp.getNick());
                  UserUtils.setUserAvatar(getContext(), Constant.URL_ImagePATH + temp.getAvatar(), holder.avatar);
              }
          }

            if(holder.unreadMsgView != null)
                holder.unreadMsgView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public User getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public int getPositionForSection(int section) {
        return positionOfSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }

    @Override
    public Object[] getSections() {
        positionOfSection = new SparseIntArray();
        sectionOfPosition = new SparseIntArray();
        int count = getCount();
        list = new ArrayList<String>();
        list.add(getContext().getString(R.string.search_header));
        positionOfSection.put(0, 0);
        sectionOfPosition.put(0, 0);
        for (int i = 1; i < count; i++) {

            String letter = getItem(i).getHeader();
            System.err.println("contactadapter getsection getHeader:" + letter + " name:" + getItem(i).getNick());
            int section = list.size() - 1;
            if (list.get(section) != null && !list.get(section).equals(letter)) {
                list.add(letter);
                section++;
                positionOfSection.put(section, i);
            }
            sectionOfPosition.put(i, section);
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public Filter getFilter() {
        if(myFilter==null){
//            Log.e("ContactAdapter", userList + "---------------->");
            myFilter = new MyFilter(userList);
        }
        return myFilter;
    }

    private class  MyFilter extends Filter{
        List<User> mOriginalList = null;

        public MyFilter(List<User> myList) {
            this.mOriginalList = myList;
        }

        @Override
        protected synchronized FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if(mOriginalList==null){
                mOriginalList = new ArrayList<User>();
            }
            EMLog.d(TAG, "contacts original size: " + mOriginalList.size());
            EMLog.d(TAG, "contacts copy size: " + copyUserList.size());

            if(prefix==null || prefix.length()==0){
                results.values = copyUserList;
                results.count = copyUserList.size();
            }else{
                String prefixString = prefix.toString();
//                Log.e("contactAdapter","prefixString-------->"+prefixString);
                final int count = mOriginalList.size();
                final ArrayList<User> newValues = new ArrayList<User>();
                for(int i=0;i<count;i++){
                    final User user = mOriginalList.get(i);
                    String username = user.getNick();
//                    Log.e("contactAdapter","username-------->"+username);
                    if(username.startsWith(prefixString)){
                        newValues.add(user);
                    }

                    else{
                        final String[] words = username.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(user);
                                break;
                            }
                        }
                    }
                }
                results.values=newValues;
                results.count=newValues.size();
            }
            EMLog.d(TAG, "contacts filter results size: " + results.count);
            return results;
        }

        @Override
        protected synchronized void publishResults(CharSequence constraint,
                                                   FilterResults results) {
            userList.clear();
            userList.addAll((List<User>)results.values);
            EMLog.d(TAG, "publish contacts filter results size: " + results.count);
            if (results.count > 0) {
                notiyfyByFilter = true;
                notifyDataSetChanged();
                notiyfyByFilter = false;
            } else {
                notifyDataSetInvalidated();
            }
        }
    }


    @Override
    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();
        if(!notiyfyByFilter){
            copyUserList.clear();
            copyUserList.addAll(userList);
        }
    }

    public void addNewFriendCount(int newfriendCount){
        this.newfriendCount =newfriendCount;
        notifyDataSetChanged();

    }


}
