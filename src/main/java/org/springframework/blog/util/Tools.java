package org.springframework.blog.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by liufu on 2016/12/12.
 */
public class Tools {

    public static String getString(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuffer sb = new StringBuffer();

        int len;
        char[] buffer = new char[256];
        while ((len = reader.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, len));
        }
        return sb.toString();
    }

}
