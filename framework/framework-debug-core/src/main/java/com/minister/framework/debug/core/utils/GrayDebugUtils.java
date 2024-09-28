package com.minister.framework.debug.core.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.minister.component.utils.IpUtil;
import com.minister.component.utils.enums.EnvTypeEnum;
import com.minister.framework.debug.core.enums.DebugTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;

/**
 * 调试工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Slf4j
public class GrayDebugUtils {

    public static final String AUTO_REGISTER = "eureka.autoRegister";
    public static final String FRAMEWORK_GRAY = "framework_gray";

    public static final String CLUSTER_IP = "CLUSTER_IP";
    // 本地模式 ：LOCAL
    public static final String RUN_MODEL = "RUN.MODEL";
    private static final String LOCAL = "LOCAL";
    public static final String GROUP_NAME = "GROUP_NAME";

    /***
     * 是否为灰度实例 true= 灰度实例
     */
    public static boolean isGrayHost() {
        String runModel = System.getProperty(RUN_MODEL);
        if (runModel != null && runModel.equalsIgnoreCase(LOCAL)) {
            // 本地模式，即不做任何限制，和以前一模一样
            return false;
        }
        String autoRegister = System.getProperty(AUTO_REGISTER);
        if (autoRegister != null && !Boolean.parseBoolean(autoRegister)) {
            return true;
        }
        return false;
    }

    /**
     * 获取URL请求参数
     */
    public static String getParameterFromURL(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (StringUtils.isBlank(queryString)) {
            return queryString;
        }
        try {
            return URLDecoder.decode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String logStr = String.format("GET request decode [%s] fail", queryString);
            log.warn(logStr, e);
        }
        return null;
    }

    /**
     * 获取灰度调试的topic或queue的后缀名，主机名称或固定framework_gray
     */
    public static String getGrayDebugTopicAndQueueSuffix() {
        //windows返回主机
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return getLocalHostName();
        }
        //灰度返回固定 framework_gray
        return FRAMEWORK_GRAY;
    }

    /**
     * 是否 windows系统
     *
     * @return true 为 windows 系统
     */
    public static boolean isWindows() {
        if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前服务的注册IP
     *
     * @return 当前服务注册的IP
     */
    public static String getRegisterIp() {
        String ip = null;
        try {
            // 如果是容器，则获取clusterIp
            String clusterIp = System.getenv(CLUSTER_IP);
            if (StringUtils.isNotBlank(clusterIp)) {
                return clusterIp;
            }
            ConfigurableEnvironment configurableEnvironment = SpringUtil.getBean(ConfigurableEnvironment.class);
            if (configurableEnvironment != null) {
                PropertySource<?> springCloudClientHostInfo = configurableEnvironment.getPropertySources()
                        .get("springCloudClientHostInfo");
                ip = (String) springCloudClientHostInfo.getProperty("spring.cloud.client.ipAddress");
            }
        } catch (Exception e) {
            log.warn("通过spring.cloud.client.ipAddress获取ip失败: " + e.getMessage());
        }
        // 当无法从环境变量中获取IP时，从网卡信息（NetworkInterface）再获取一次
        if (StringUtils.isBlank(ip)) {
            ip = IpUtil.REAL_LOCK_IP;
        }
        return ip;
    }

    /**
     * 获取主机名
     */
    public static String getLocalHostName() {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤不需要的头信息判断
     */
    public static boolean isIncludedHeader(String headerName) {
        String name = headerName.toLowerCase();
        switch (name) {
            case "host":
            case "content-length":
            case "x-hostname":
            case "x-port":
            case "server":
            case "purpose":
            case "accept-encoding":
            case "transfer-encoding":
            case "x-application-context":
            case "x-sys-gray":
                return false;
            default:
                return true;
        }
    }

    /**
     * 获取调试类型
     */
    public static DebugTypeEnum getDebugType(String env) {
        EnvTypeEnum envType = EnvTypeEnum.getByName(env);
        switch (envType) {
            case DEV:
                return DebugTypeEnum.DEBUG;
            case STG:
                return DebugTypeEnum.MULTI_VERSION;
            case PROD:
            default:
                return DebugTypeEnum.GRAY;
        }
    }

}
