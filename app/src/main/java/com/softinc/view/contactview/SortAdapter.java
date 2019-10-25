package com.softinc.view.contactview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.softinc.bean.Contact;
import com.softinc.youelink.R;

import java.util.List;

public class SortAdapter extends BaseAdapter implements SectionIndexer {

    private List<Contact> list = null;

    private Context mContext;

    public SortAdapter(Context mContext, List<Contact> list) {
        this.mContext = mContext;
        this.list = list;
    }

    public void updateListView(List<Contact> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        final Contact contact = list.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_sync_contact, null);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_catalogue = (TextView) convertView.findViewById(R.id.tv_catalog);
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.bt_addFriend = (Button) convertView.findViewById(R.id.bt_add_friend);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置字母序号的显示和隐藏
        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tv_catalogue.setVisibility(View.VISIBLE);
            viewHolder.tv_catalogue.setText(contact.sortLetters);
        } else {
            viewHolder.tv_catalogue.setVisibility(View.GONE);
        }

        viewHolder.tv_name.setText(contact.name);
        viewHolder.iv_icon.setImageResource(R.drawable.head4);
        //TODO 按钮和头像
        if (contact.isFriend) viewHolder.bt_addFriend.setVisibility(View.INVISIBLE);

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).sortLetters;
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return list.get(position).sortLetters.charAt(0);
    }

    final static class ViewHolder {
        TextView tv_catalogue;
        TextView tv_name;
        ImageView iv_icon;
        Button bt_addFriend;
    }

    /**
     * 提取英文的首字母，非英文字母用#代替。
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

}
