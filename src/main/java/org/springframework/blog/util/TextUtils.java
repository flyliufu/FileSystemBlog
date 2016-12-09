package org.springframework.blog.util;

/**
 * 文本工具类
 * Created by liufu on 2016/12/9.
 */
public class TextUtils {


    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }
}
