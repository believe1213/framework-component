package com.minister.component.utils.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minister.component.utils.constants.HeadersKey;
import com.minister.component.utils.enums.NetIspEnum;
import com.minister.component.utils.enums.NetTypeEnum;
import com.minister.component.utils.enums.OsTypeEnum;
import lombok.Data;

/**
 * HeaderEntity
 *
 * @author QIUCHANGQING620
 * @date 2020-02-21 23:05
 */
@Data
public class HeaderEntity {

    /**
     * 客户端系统类型
     */
    @JsonProperty(HeadersKey.OS_TYPE)
    private OsTypeEnum osTypeEnum;

    /**
     * 客户端系统版本号
     */
    @JsonProperty(HeadersKey.OS_VERSION)
    private String osVersion;

    /**
     * 客户端设备号
     */
    @JsonProperty(HeadersKey.DEVICE_ID)
    private String deviceId;

    @JsonProperty(HeadersKey.DEVICE_TOKEN)
    private String deviceToken;

    @JsonProperty(HeadersKey.ROM_VERSION)
    private String romVersion;

    @JsonProperty(HeadersKey.SCREEN_DPI)
    private String screenDpi;

    /**
     * 网络类型
     */
    @JsonProperty(HeadersKey.NET_TYPE)
    private NetTypeEnum netTypeEnum;

    /**
     * 网络运营商
     */
    @JsonProperty(HeadersKey.NET_ISP)
    private NetIspEnum netIspEnum;

    /**
     * 客户端ip
     */
    @JsonProperty(HeadersKey.CLIENT_IP)
    private String clientIp;

    /**
     * app下载来源
     */
    @JsonProperty(HeadersKey.APP_PLATFORM)
    private String appPlatform;

    /**
     * app标识
     */
    @JsonProperty(HeadersKey.APP_ID)
    private String appId;

    /**
     * app名称
     */
    @JsonProperty(HeadersKey.APP_NAME)
    private String appName;

    /**
     * app版本号
     */
    @JsonProperty(HeadersKey.APP_VERSION)
    private String appVersion;

    /**
     * app路由
     */
    @JsonProperty(HeadersKey.ROUTE_CHANNEL)
    private String routeChannel;

    /**
     * 批次号
     */
    @JsonProperty(HeadersKey.BATCH_ID)
    private String batchId;

    /**
     * 场景
     */
    @JsonProperty(HeadersKey.SCENE)
    private String scene;

    /**
     * 渠道
     */
    @JsonProperty(HeadersKey.CHANNEL)
    private String channel;

    /**
     * 活动标识
     */
    @JsonProperty(HeadersKey.ACTIVITY_ID)
    private String activityId;

    /**
     * 活动渠道
     */
    @JsonProperty(HeadersKey.ACTIVITY_CHANNEL)
    private String activityChannel;

    /**
     * token
     */
    @JsonProperty(HeadersKey.TOKEN)
    private String token;

    /**
     * 签名
     */
    @JsonProperty(HeadersKey.SIGN)
    private String sign;

    /**
     * 时间戳
     */
    @JsonProperty(HeadersKey.TIMESTAMP)
    private String timestamp;

    /**
     * 随机值（计算签名）
     */
    @JsonProperty(HeadersKey.NONCE)
    private boolean isNonce;

    // ----- 非接口传入值 -----

    @JsonProperty(HeadersKey.REQUEST_IP)
    private String requestIp;

    /**
     * 用户标识
     */
    @JsonProperty(HeadersKey.USER_ID)
    private String userId;

    /**
     * 链路标识
     */
    @JsonProperty(HeadersKey.TRACE_ID)
    private String traceId;

}
