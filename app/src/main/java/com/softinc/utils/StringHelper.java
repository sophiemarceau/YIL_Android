package com.softinc.utils;

public class StringHelper {
    public static Boolean isEmptyOrNull(String source) {
        if (source != null && !source.isEmpty() && !source.equals("null")) {
            return false;
        }

        return true;
    }
}
