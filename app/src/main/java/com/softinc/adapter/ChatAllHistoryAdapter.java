package com.softinc.adapter;

/**
 * Created by sophiemarceau_qu on 15/5/14.
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.StreamHandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;

import com.easemob.util.DateUtils;
import com.softinc.application.MyApplication;
import com.softinc.bean.User;
import com.softinc.config.Constant;
import com.softinc.db.UserDao;
import com.softinc.fragment.ContactsFragment;
import com.softinc.utils.CommonUtils;
import com.softinc.utils.SmileUtils;
import com.softinc.utils.UserUtils;
import com.softinc.view.Tag;
import com.softinc.youelink.R;
import com.softinc.youelink.UserActivity;

/**
 * 显示所有聊天记录adpater
 *
 */
public class ChatAllHistoryAdapter extends ArrayAdapter<EMConversation> {
private  static final  String TAG ="ChatAllhistory";
    private LayoutInflater inflater;
    private List<EMConversation> conversationList;
    private List<EMConversation> copyConversationList;
    private List<String> userlistFromServer;
    private List<String> nicknameList;
    private ConversationFilter conversationFilter;
    private boolean notiyfyByFilter;

    public ChatAllHistoryAdapter(Context context, int textViewResourceId, List<EMConversation> objects,List<String> objects1,List<String> objects2) {

        super(context, textViewResourceId, objects);
        this.conversationList = objects;
        this.userlistFromServer =objects1;
        this.nicknameList =objects2;
        copyConversationList = new ArrayList<EMConversation>();
        copyConversationList.addAll(objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_chat_history, parent, false);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.msgState = convertView.findViewById(R.id.msg_state);
            holder.list_item_layout = (RelativeLayout) convertView.findViewById(R.id.list_item_layout);
            convertView.setTag(holder);
        }
        if (position % 2 == 0) {
            holder.list_item_layout.setBackgroundResource(R.drawable.mm_listitem);
        } else {
            holder.list_item_layout.setBackgroundResource(R.drawable.mm_listitem_grey);
        }

        // 获取与此用户/群组的会话
        EMConversation conversation = getItem(position);
        // 获取用户username或者群组groupid
        String username = conversation.getUserName();

        List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
        EMContact contact = null;
        boolean isGroup = false;
        for (EMGroup group : groups) {
            if (group.getGroupId().equals(username)) {
                isGroup = true;
                contact = group;
                break;
            }
        }
        if (isGroup) {
            // 群聊消息，显示群聊头像
            holder.avatar.setImageResource(R.drawable.group_icon);
            holder.name.setText(contact.getNick() != null ? contact.getNick() : username);
        } else {
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(getContext(), UserActivity.class);
//                    intent.putExtra("user_id", );
//
//                    getContext().startActivity(intent);
                }
            });
            UserUtils.setUserAvatar(getContext(), username, holder.avatar);
            if (username.equals(Constant.GROUP_USERNAME)) {
                holder.name.setText("群聊");

            } else if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
                holder.name.setText("新的朋友");
            }
            for (int i = 0; i < userlistFromServer.size(); i++)  //外循环是循环的次数
            {
                for (int j = userlistFromServer.size() - 1 ; j > i; j--)  //内循环是 外循环一次比较的次数
                {



                    if (userlistFromServer.get(i)==userlistFromServer.get(j)){

                        userlistFromServer.remove(j);
                    }

                }
            }

//            Log.e(TAG,"username------>"+username);
//            Log.e(TAG,"userlistFromServer--a;lsdkfja;sldkfja;lskdjf;alsdkjfa;sldkjf---->"+userlistFromServer);
            if (userlistFromServer.contains(username)){
//                Log.e("chatAllhistrory","nicknameList--name---->"+nicknameList.get(userlistFromServer.indexOf(username)));
                holder.name.setText(nicknameList.get(userlistFromServer.indexOf(username)));

            }

        }

        if (conversation.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.unreadLabel.setVisibility(View.VISIBLE);
        } else {
            holder.unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = conversation.getLastMessage();
            holder.message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))),
                    BufferType.SPANNABLE);

            holder.time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
                holder.msgState.setVisibility(View.VISIBLE);
            } else {
                holder.msgState.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    private String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION: // 位置消息
                if (message.direct == EMMessage.Direct.RECEIVE) {
                    // 从sdk中提到了ui中，使用更简单不犯错的获取string的方法
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_recv");
                    digest = getStrng(context, R.string.location_recv);
                    digest = String.format(digest, message.getFrom());
                    return digest;
                } else {
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_prefix");
                    digest = getStrng(context, R.string.location_prefix);
                }
                break;
            case IMAGE: // 图片消息
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
                digest = getStrng(context, R.string.picture);
                break;
            case VOICE:// 语音消息
                digest = getStrng(context, R.string.voiceyinpin);
                break;
            case VIDEO: // 视频消息
                digest = getStrng(context, R.string.video);
                break;
            case TXT: // 文本消息
                if(!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL,false)){
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                }else{
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = getStrng(context, R.string.voice_call) + txtBody.getMessage();
                }
                break;
            case FILE: // 普通文件消息
                digest = getStrng(context, R.string.file);
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }

        return digest;
    }

    private static class ViewHolder {
        /** 和谁的聊天记录 */
        TextView name;
        /** 消息未读数 */
        TextView unreadLabel;
        /** 最后一条消息的内容 */
        TextView message;
        /** 最后一条消息的时间 */
        TextView time;
        /** 用户头像 */
        ImageView avatar;
        /** 最后一条消息的发送状态 */
        View msgState;
        /** 整个list中每一行总布局 */
        RelativeLayout list_item_layout;

    }

    String getStrng(Context context, int resId) {
        return context.getResources().getString(resId);
    }



    @Override
    public Filter getFilter() {
        if (conversationFilter == null) {
            conversationFilter = new ConversationFilter(conversationList);
        }
        return conversationFilter;
    }

    private class ConversationFilter extends Filter {
        List<EMConversation> mOriginalValues = null;

        public ConversationFilter(List<EMConversation> mList) {
            mOriginalValues = mList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<EMConversation>();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = copyConversationList;
                results.count = copyConversationList.size();
            } else {
                String prefixString = prefix.toString();
                final int count = mOriginalValues.size();
                final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();

                for (int i = 0; i < count; i++) {
                    final EMConversation value = mOriginalValues.get(i);
                    String username = value.getUserName();

                    EMGroup group = EMGroupManager.getInstance().getGroup(username);
                    String nickname ="";
                    if(group != null){
                        nickname = group.getGroupName();
                    }

                    if (userlistFromServer.contains(username)){
                        nickname = nicknameList.get(userlistFromServer.indexOf(username));

                    }


                    // First match against the whole ,non-splitted value
                    if (nickname.indexOf(prefixString)>-1) {
                        newValues.add(value);
                    } else{
                        final String[] words = nickname.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].indexOf(prefixString)>-1) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            conversationList.clear();
            conversationList.addAll((List<EMConversation>) results.values);
            if (results.count > 0) {
                notiyfyByFilter = true;
                notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "没有相关消息记录...", 1).show();
                notifyDataSetInvalidated();
            }

        }

    }

    @Override
    public void notifyDataSetChanged() {
        if (!CommonUtils.isNetWorkConnected(getContext())) {
            Toast.makeText(getContext(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        super.notifyDataSetChanged();
        if(!notiyfyByFilter){
            copyConversationList.clear();
            copyConversationList.addAll(conversationList);
            notiyfyByFilter = false;
        }
    }
}

