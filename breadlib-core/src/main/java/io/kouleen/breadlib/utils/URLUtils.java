package io.kouleen.breadlib.utils;


import io.kouleen.breadlib.exception.IORuntimeException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author zhangqing
 * @since 2023/2/16 14:01
 */
public class URLUtils {

    public static JarFile getJarFile(URL url) {
        try {
            JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
            return urlConnection.getJarFile();
        } catch (IOException ioException) {
            throw new IORuntimeException(ioException);
        }
    }
}
