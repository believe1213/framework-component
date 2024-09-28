package com.minister.framework.debug.core.local.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.minister.component.utils.JacksonUtil;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.debug.core.local.entity.DebugDispenseEntity;
import com.minister.framework.debug.core.utils.FmkConsoleEnvHostUtils;
import com.minister.framework.debug.core.utils.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 获取本地调试规则HttpCaller
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Slf4j
public class DebugConfigHttpCaller {

    /**
     * 根据微服务名称返回当前微服务的本地调试配置
     */
    public static DebugDispenseEntity getForwardInfo(String serviceName, String env) throws Exception {
        String host = FmkConsoleEnvHostUtils.getEnvHost(env);
        if (StringUtils.isBlank(host)) {
            log.debug("can not find [{}] env debug config", env);
            return null;
        }
        DebugDispenseEntity queryParam = new DebugDispenseEntity();
        queryParam.setServiceName(serviceName);
        String resp = HttpCaller.post(host + "/debugRule/getRuleByServiceName", JacksonUtil.bean2Json(queryParam));
        ResponseDto<DebugDispenseEntity> responseData = JacksonUtil.json2Bean(resp, new TypeReference<ResponseDto<DebugDispenseEntity>>() {
        });
        if (responseData != null && responseData.isSuccess()) {
            return responseData.getData();
        }
        return null;
    }

    /**
     * 获取NAT服务地址
     */
    public static Map<String, String> allNatAddress(String env) {
        String host = FmkConsoleEnvHostUtils.getEnvHost(env);
        if (StringUtils.isBlank(host)) {
            return null;
        }
        String resp = HttpCaller.get(host + "/nat/allNatAddress");
        ResponseDto<Map<String, String>> responseData = JacksonUtil.json2Bean(resp, new TypeReference<ResponseDto<Map<String, String>>>() {
        });
        if (responseData != null && responseData.isSuccess()) {
            return responseData.getData();
        }
        return null;
    }

}
