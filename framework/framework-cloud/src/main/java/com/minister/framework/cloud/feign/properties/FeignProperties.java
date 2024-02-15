package com.minister.framework.cloud.feign.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * feign 配置
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:24
 */
@Component
public class FeignProperties {

    /**
     * feign调用decode路由白名单
     */
    public static Set<String> decodeWhiteListUri;

    @Value("${framework-boot.feign.decode-white-list:http://id-center/}")
    public void setDecodeWhiteListUri(Set<String> decodeWhiteListUri) {
        FeignProperties.decodeWhiteListUri = decodeWhiteListUri;
    }

}
