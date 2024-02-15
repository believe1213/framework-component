package com.minister.component.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.Enumeration;
import java.util.Set;

/**
 * IpUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 19:36
 */
@Slf4j
public class IpUtil {

    private IpUtil() {
    }

    private static final String UNKNOWN = "unknown";

    private static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private static final String X_REAL_IP = "X-Real-IP";

    public static boolean isIpEqual(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String localIp = getRealLocalIp();
        log.debug("ip : {}, localIp : {}", ip, localIp);
        return ip.equals(localIp);
    }

    public static boolean isIpInCol(Set<String> ipSet) {
        if (CollectionUtils.isEmpty(ipSet)) {
            return false;
        }
        String localIp = getRealLocalIp();
        log.debug("ipSet : {}, localIp : {}", JacksonUtil.bean2Json(ipSet), localIp);
        return ipSet.contains(localIp);
    }

    public static String getRealLocalIp() {
        String ip = StringUtils.EMPTY;
        // 候选地址
        String candidateIp = StringUtils.EMPTY;
        try {
            Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
            // 遍历网络接口
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                // 排除回文地址和虚拟地址
                if (ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                // 获得与该网络接口绑定的 IP 地址，一般只有一个
                Enumeration<?> e2 = ni.getInetAddresses();

                while (e2.hasMoreElements()) {
                    InetAddress ia = (InetAddress) e2.nextElement();
                    if (ia.isLoopbackAddress() || ia.isAnyLocalAddress()) {
                        continue;
                    }
                    // 只获取ipV4地址
                    if (ia instanceof Inet6Address) {
                        continue;
                    }
                    // 如果是site-local地址
                    if (ia.isSiteLocalAddress()) {
                        ip = ia.getHostAddress();
                        break;
                    }
                    log.trace("not site-local");
                    if (StringUtils.isBlank(candidateIp)) {
                        candidateIp = ia.getHostAddress();
                        log.trace("candidateIp : {}", candidateIp);
                    }
                }
                if (StringUtils.isNotBlank(candidateIp)) {
                    log.trace("can not get site-local");
                    ip = candidateIp;
                }
            }
            // 如果没有发现 non-loopback 地址，只能用最次方案
            if (StringUtils.isBlank(ip)) {
                log.trace("can not get non-loopback");
                ip = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (SocketException | UnknownHostException e) {
            log.error("getLocalIP fail", e);
        }
        return ip;
    }

    /**
     * 获取用户实际IP地址
     *
     * @param request 当前请求对象
     * @return 实际IP地址
     */
    public static String getRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader(X_FORWARDED_FOR);
        log.trace("当前IP来源[X-Forwarded-For], 值[{}]", ip);
        if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return getRemoteIp(ip);
        }
        ip = request.getHeader(X_REAL_IP);
        log.trace("当前IP来源[X-Real-IP], 值[{}]", ip);
        if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return getRemoteIp(ip);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            log.trace("当前IP来源[Proxy-Client-IP], 值[{}]", ip);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            log.trace("当前IP来源[WL-Proxy-Client-IP], 值[{}]", ip);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            log.trace("当前IP来源[HTTP_CLIENT_IP], 值[{}]", ip);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            log.trace("当前IP来源[HTTP_X_FORWARDED_FOR], 值[{}]", ip);
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            log.trace("当前IP来源[getRemoteAddr], 值[{}]", ip);
        }
        return getRemoteIp(ip);
    }

    /**
     * 获取用户实际IP地址
     *
     * @param ip ip
     * @return 用户实际IP地址
     */
    private static String getRemoteIp(String ip) {
        if (IPV6_LOCAL.equals(ip)) {
            String ipv4FromLocal = getRealLocalIp();
            if (StringUtils.isNotEmpty(ipv4FromLocal)) {
                ip = ipv4FromLocal;
            }
        }
        return ip;
    }

}
