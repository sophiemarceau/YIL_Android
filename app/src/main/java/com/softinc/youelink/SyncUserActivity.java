package com.softinc.youelink;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.softinc.bean.User;
import com.softinc.config.GlobalData;
import com.softinc.dao.ContactDao;
//import com.softinc.domain.User;
import com.softinc.engine.UserEngine;
import com.softinc.utils.GBKUtils;
import com.softinc.view.Title;
import com.softinc.view.contactview.CharacterParser;
import com.softinc.view.contactview.PinyinComparator;
import com.softinc.view.contactview.SideBar;
import com.softinc.view.contactview.SortAdapter;
import com.softinc.bean.Contact;

import java.util.Collections;
import java.util.List;

public class SyncUserActivity extends Activity {
    private Title title;
    private ListView lv_contact;
    private SideBar sideBar;
    private TextView tv_dialog;
    private SortAdapter adapter;

    private List<Contact> contacts;
    private CharacterParser characterParser;//汉字转换成拼音的类
    private PinyinComparator pinyinComparator;//根据拼音来排列ListView里面的数据类
    private ContactDao contactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_user);

        initView();

        contacts = getContactsFromLocal();
        compareContactsWithCloud(contacts);
        Collections.sort(contacts, pinyinComparator);// 根据a-z进行排序源数据
        adapter = new SortAdapter(this, contacts);
        lv_contact.setAdapter(adapter);
    }

    /**
     * 把本地联系人数据和服务器好友数据做比较
     *
     * @param contacts
     */
    private void compareContactsWithCloud(List<Contact> contacts) {
        GlobalData.allFriends = UserEngine.allFriendsFromServer();
        //遍历联系人,和朋友列表比较,如果电话号码相同标识此人已经是朋友了.
        for (Contact contact : contacts) {
            for (User friend : GlobalData.allFriends) {
                if (contact.phoneNumber.equals(friend.phoneNumber)) contact.isFriend = true;
            }
        }
    }

    private void initView() {
        title = (Title) findViewById(R.id.tv_name);
        lv_contact = (ListView) findViewById(R.id.lv_contact);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        tv_dialog = (TextView) findViewById(R.id.tv_dialog);

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        sideBar.setTextView(tv_dialog);
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    lv_contact.setSelection(position);
                }

            }
        });

        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Toast.makeText(getApplication(), ((Contact) adapter.getItem(position)).name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 得到联系人数据,并生成排序用首字母
     *
     * @return
     */
    private List<Contact> getContactsFromLocal() {
        if (contactDao == null) contactDao = new ContactDao();
        List<Contact> contacts = contactDao.queryAllContacts(this);

        for (Contact contact : contacts) {

            //汉字转换成拼音
            String pinyin = characterParser.getSpelling(contact.name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                contact.sortLetters = sortString.toUpperCase();
            } else {
                contact.sortLetters = "#";
            }
        }

        return contacts;
    }

}
