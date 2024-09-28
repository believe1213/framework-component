package com.minister.framework.debug.core.stg.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.minister.component.utils.JacksonUtil;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.debug.core.stg.entity.MultiVersionEntity;
import com.minister.framework.debug.core.utils.FmkConsoleEnvHostUtils;
import com.minister.framework.debug.core.utils.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 多版本规则HttpCaller
 *
 * @author QIUCHANGQING620
 * @date 2024-05-02 00:00
 */
@Slf4j
public class MultiVersionConfigHttpCaller {

    /**
     * 根据微服务名称返回当前微服务的多版本测试配置
     */
    public static List<MultiVersionEntity> getMultiVersionInfo(String serviceName, String env) throws Exception {
        String host = FmkConsoleEnvHostUtils.getEnvHost(env);
        if (StringUtils.isBlank(host)) {
            log.debug("can not find [{}] env multi version config", env);
            return null;
        }
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("serviceName", serviceName);
        String resp = HttpCaller.post(host + "/multiVersion/getRuleByServiceName", JacksonUtil.bean2Json(queryParam));
        ResponseDto<List<MultiVersionEntity>> responseData = JacksonUtil.json2Bean(resp, new TypeReference<ResponseDto<List<MultiVersionEntity>>>() {
        });
        if (responseData != null && responseData.isSuccess()) {
            return responseData.getData();
        }
        return null;
    }

}
