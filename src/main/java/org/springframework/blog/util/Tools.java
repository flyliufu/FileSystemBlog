package org.springframework.blog.util;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

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

    public static String readString4File(File file) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer();
        if (!file.isDirectory()) {
            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[512];
            int len;
            try {
                while ((len = fis.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
