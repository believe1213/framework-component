package com.minister.framework.boot.filter.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Filter工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-04-24 00:25
 */
public class FilterUtils {

    final static String GET = "GET";
    final static String POST = "POST";
    final static String JSON_TYPE = "application/json";
    final static String CONTENT_TYPE = "Content-Type";
    final static String REQUEST_ACCEPT_CONTENT_TYPE = "application/x-www-form-urlencoded,application/json";
    final static String SEPARATOR_STR = ",";

    public static final String DEFAULT_SKIP_PATTERN = "/api-docs.*|/autoconfig|/configprops|/dump|/health|/info|/metrics.*|/mappings|/trace" +
            "|/swagger.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|/favicon.ico|/hystrix.stream";
    private static Pattern skipPattern = Pattern.compile(DEFAULT_SKIP_PATTERN);

    private static UrlPathHelper urlPathHelper = new UrlPathHelper();

    /**
     * 默认跳过的URI路径地址
     */
    public static boolean skipPathUri(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return true;
        }
        if (skipPattern.matcher(uri).matches()) {
            return true;
        }
        if (uri.length() < 2) {
            return true;
        }
        return false;
    }

    /**
     * 获取 请求相对路径uri
     */
    public static String getRequestUri(HttpServletRequest request) {
        String uri = urlPathHelper.getRequestUri(request);
        if ("/error".equals(uri)) {
            uri = (String) request.getAttribute("javax.servlet.error.request_uri");
        }
        if (StringUtils.isEmpty(uri)) {
            return null;
        }
        uri = uri.replaceAll("\\/\\/", "\\/");
        return uri;
    }

    /**
     * 是否需要拦截参数，true=需要，false=不需要
     */
    public static boolean needRequestWrapper(HttpServletRequest request) {
        if (GET.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String rt = request.getHeader(CONTENT_TYPE);
        if (POST.equalsIgnoreCase(request.getMethod()) && StringUtils.isNotEmpty(rt) && rt.toLowerCase().contains(JSON_TYPE)) {
            return true;
        }
        return false;
    }

}
