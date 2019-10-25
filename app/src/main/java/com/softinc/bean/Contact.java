package com.softinc.bean;

import android.graphics.Bitmap;

/**
 * 手机联系人
 */
public class Contact {
    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +

                '}';
    }

    public String name;//名字
    public String phoneNumber;//电话
    public Bitmap avator;

    public Bitmap getAvator() {
        return avator;
    }

    public void setAvator(Bitmap avator) {
        this.avator = avator;
    }

    public String sortLetters;//首字母拼音
    public boolean isFriend = false;//是否已经是朋友了






    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }
}
