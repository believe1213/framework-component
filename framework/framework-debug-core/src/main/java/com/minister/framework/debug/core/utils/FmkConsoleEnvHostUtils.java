package com.minister.framework.debug.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * framework console 环境地址工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Slf4j
public class FmkConsoleEnvHostUtils {

    public final static String devUrl = "http://erp-edge-dev.kye-erp.com:18766/api/fmk";
    public final static String stgUrl = "http://erp-edge-stg.kye-erp.com:18766/api/fmk";
    public final static String uatUrl = "http://erp-edge-uat.kyeapi.com:18766/api/fmk";
    public final static String prdUrl = "http://erp-basic-prd-gateway.kyeapi.com/api/fmk";

    /**
     * 根据不同环境获取各个环境下的FmkConsole服务配置地址
     */
    public static String getEnvHost(String env) {
        if (StringUtils.isBlank(env)) {
            log.warn("框架无法识别当前所在的服务环境（dev、stg、uat、prod）？，请在配置中心设置参数openapi.client.env: dev或openapi.client.env: stg" +
                    "或openapi.client.env: uat或openapi.client.env: prod");
            return null;
        }
        String host = null;
        env = env.toLowerCase();
        switch (env) {
            case "dev":
                host = devUrl;
                break;
            case "stg":
                host = stgUrl;
                break;
            case "uat":
                host = uatUrl;
                break;
            case "prod":
            case "prd":
                host = prdUrl;
                break;
        }
        if (StringUtils.isBlank(host)) {
            log.warn("您当前配置参数：openapi.client.env: {},框架无法识别当前所在的服务环境（dev、stg、uat、prod）？",env);
            return null;
        }
        return host;
    }
}
