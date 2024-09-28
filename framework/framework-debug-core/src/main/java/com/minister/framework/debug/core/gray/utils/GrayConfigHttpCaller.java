package com.minister.framework.debug.core.gray.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.minister.component.utils.JacksonUtil;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.debug.core.gray.entity.GrayTaskDispenseEntity;
import com.minister.framework.debug.core.utils.FmkConsoleEnvHostUtils;
import com.minister.framework.debug.core.utils.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取灰度规则HttpCaller
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
@Slf4j
public class GrayConfigHttpCaller {

    /**
     * 根据微服务名称返回当前微服务的灰度配置
     */
    public static GrayTaskDispenseEntity getRuleByServiceName(String serviceName, String env) throws Exception {
        String host = FmkConsoleEnvHostUtils.getEnvHost(env);
        if (StringUtils.isBlank(host)) {
            log.debug("can not find [{}] env gray config", env);
            return null;
        }
        GrayTaskDispenseEntity queryParam = new GrayTaskDispenseEntity();
        queryParam.setServiceName(serviceName);
        String resp = HttpCaller.post(host + "/grayTaskRule/getRuleByServiceName", JacksonUtil.bean2Json(queryParam));
        ResponseDto<GrayTaskDispenseEntity> responseData = JacksonUtil.json2Bean(resp, new TypeReference<ResponseDto<GrayTaskDispenseEntity>>() {
        });
        if (responseData != null && responseData.isSuccess()) {
            return responseData.getData();
        }
        return null;
    }

}
