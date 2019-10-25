package com.softinc.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.softinc.bean.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人访问
 * Created by zhangbing on 15-4-21.
 */
public class ContactDao {
    private static final String TAG = "ContactDao";

    public List<Contact> queryAllContacts(Context context) {

        //把所有的联系人
        List<Contact> contacts = new ArrayList<Contact>();

        // 得到一个内容解析器
        ContentResolver resolver = context.getContentResolver();
        // raw_contacts uri

        //下文URI格式: content:要访问的包名/表名
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri uriData = Uri.parse("content://com.android.contacts/data");

        Cursor cursor = resolver.query(uri, new String[]{"contact_id"},
                null, null, null);

        while (cursor.moveToNext()) {
            String contact_id = cursor.getString(0);

            if (contact_id != null) {
                //具体的某一个联系人
                Contact contact = new Contact();

                Cursor dataCursor = resolver.query(uriData, new String[]{
                                "data1", "mimetype"}, "contact_id=?",
                        new String[]{contact_id}, null);

                while (dataCursor.moveToNext()) {
                    String data1 = dataCursor.getString(0);
                    String mimetype = dataCursor.getString(1);
                    Log.d(TAG, "data1==" + data1 + "==mimetype==" + mimetype);

                    if ("vnd.android.cursor.item/name".equals(mimetype)) {
                        //联系人的姓名
//                        contact.setName(data1);
                    } else if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                        //联系人的电话号码
//                        contact.setPhoneNumber(data1);
                    }

                }
                contacts.add(contact);
                dataCursor.close();
            }

        }

        cursor.close();
        return contacts;
    }
}
