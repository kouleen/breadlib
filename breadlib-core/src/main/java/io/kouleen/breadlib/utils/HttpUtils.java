package io.kouleen.breadlib.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangqing
 * @since 2023/2/8 18:56
 */
public abstract class HttpUtils {

    private static final Pattern CONTEXT_PATH_MATCH = Pattern.compile("(\\/)\\1+");

    /**
     * Build URL.
     *
     * @param isHttps    whether is https
     * @param serverAddr server ip/address
     * @param subPaths   api path
     * @return URL string
     */
    public static String buildUrl(boolean isHttps, String serverAddr, String... subPaths) {
        StringBuilder sb = new StringBuilder();
        if (isHttps) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(serverAddr);
        String pre = null;
        for (String subPath : subPaths) {
            if (!StringUtils.hasText(subPath)) {
                continue;
            }
            Matcher matcher = CONTEXT_PATH_MATCH.matcher(subPath);
            if (matcher.find()) {
                throw new IllegalArgumentException("Illegal url path expression : " + subPath);
            }
            if (pre == null || !pre.endsWith("/")) {
                if (subPath.startsWith("/")) {
                    sb.append(subPath);
                } else {
                    sb.append('/').append(subPath);
                }
            } else {
                if (subPath.startsWith("/")) {
                    sb.append(subPath.replaceFirst("\\/", ""));
                } else {
                    sb.append(subPath);
                }
            }
            pre = subPath;
        }
        return sb.toString();
    }

    /**
     * Encoding parameters to url string.
     *
     * @param params   parameters
     * @param encoding encoding charset
     * @return url string
     * @throws UnsupportedEncodingException if encoding string is illegal
     */
    public static String encodingParams(Map<String, String> params, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == params || params.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                continue;
            }

            sb.append(entry.getKey()).append('=');
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
            sb.append('&');
        }

        return sb.toString();
    }

    /**
     * Encoding KV list to url string.
     *
     * @param paramValues parameters
     * @param encoding    encoding charset
     * @return url string
     * @throws UnsupportedEncodingException if encoding string is illegal
     */
    public static String encodingParams(List<String> paramValues, String encoding) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
            sb.append(iter.next()).append('=');
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }
}
