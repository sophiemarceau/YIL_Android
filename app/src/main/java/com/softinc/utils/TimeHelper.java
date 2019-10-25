package com.softinc.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {
    public static String translateIntToDate(long time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(time);
        return df.format(date);
    }

    public static Integer compareTwoTime(String t1, String t2) {
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        java.util.Calendar c1 = java.util.Calendar.getInstance();
        java.util.Calendar c2 = java.util.Calendar.getInstance();
        try {
            c1.setTime(df.parse(t1));
            c2.setTime(df.parse(t2));
        } catch (java.text.ParseException e) {
            System.err.println("格式不正确");
        }
        int result = c1.compareTo(c2);
        return result;
    }

    public static Integer validateOverTime(String meetingTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sDateFormat.format(new java.util.Date());
        return compareTwoTime(meetingTime,date);
    }
}
