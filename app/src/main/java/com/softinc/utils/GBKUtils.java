package com.softinc.utils;

/**
 * 中文首字母提取,使用方法见源码.
 * Created by zhangbing on 15-1-27.
 */
public class GBKUtils {

    //使用方法
    //GBKUtils gbkUtils = new GBKUtils();
    //System.out.println(gbkUtils.string2AlphaFirst("我勒个去", "b"));

    public GBKUtils() {

    }

    //字母Z使用了两个标签，这里有２７个值
    //i, u, v都不做声母, 跟随前面的字母
    private char[] chartable = {
            '啊', '芭', '擦', '搭', '蛾', '发', '噶', '哈', '哈',
            '击', '喀', '垃', '妈', '拿', '哦', '啪', '期', '然',
            '撒', '塌', '塌', '塌', '挖', '昔', '压', '匝', '座'
    };

    private char[] alphatableb = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private char[] alphatables = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private int[] table = new int[27];  //初始化

    {
        for (int i = 0; i < 27; ++i) {
            table[i] = gbValue(chartable[i]);
        }
    }

    /**
     * 主函数,输入字符,得到他的声母,英文字母返回对应的大小写字母,其他非简体汉字返回 '0'.
     *
     * @param ch
     * @param type
     * @return
     */
    public char char2Alpha(char ch, String type) {
        if (ch >= 'a' && ch <= 'z')
            // return (char) (ch - 'a' + 'A');
            return ch;
        if (ch >= 'A' && ch <= 'Z')
            return ch;

        int gb = gbValue(ch);
        if (gb < table[0])
            return '0';

        int i;
        for (i = 0; i < 26; ++i) {
            if (match(i, gb))
                break;
        }

        if (i >= 26) {
            return '0';
        } else {
            if ("b".equals(type)) {//大写
                return alphatableb[i];
            } else {//小写
                return alphatables[i];
            }
        }
    }

    /**
     * 输入中英混合字符串,输出每一个字符的声母(英文字母直接输出)组成的字符串.
     *
     * @param SourceStr
     * @param type      传入b 大写 否则小写
     * @return
     */
    public String string2Alpha(String SourceStr, String type) {
        String Result = "";
        int StrLength = SourceStr.length();
        int i;
        try {
            for (i = 0; i < StrLength; i++) {
                Result += char2Alpha(SourceStr.charAt(i), type);
            }
        } catch (Exception e) {
            Result = "";
        }
        return Result;
    }

    /**
     * 输入中英混合字符串,输出第一个字符的声母(英文直接输出)
     *
     * @param SourceStr
     * @param type      传入b 大写 否则小写
     * @return
     */
    public String string2AlphaFirst(String SourceStr, String type) {
        String Result = "";
        try {
            Result += char2Alpha(SourceStr.charAt(0), type);
        } catch (Exception e) {
            Result = "";
        }
        return Result;
    }

    private boolean match(int i, int gb) {
        if (gb < table[i])
            return false;
        int j = i + 1;

        //字母Z使用了两个标签
        while (j < 26 && (table[j] == table[i]))
            ++j;
        if (j == 26)
            return gb <= table[j];
        else
            return gb < table[j];
    }

    //取出汉字的编码
    private int gbValue(char ch) {
        String str = new String();
        str += ch;
        try {
            byte[] bytes = str.getBytes("GBK");
            if (bytes.length < 2)
                return 0;
            return (bytes[0] << 8 & 0xff00) + (bytes[1] &
                    0xff);
        } catch (Exception e) {
            return 0;
        }
    }
}
