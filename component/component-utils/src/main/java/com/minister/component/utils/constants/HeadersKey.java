package com.minister.component.utils.constants;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 请求头key常量
 *
 * @author QIUCHANGQING620
 * @date 2024-02-11 23:22
 */
public class HeadersKey {

    // ----- 需处理的KEY start -----

    public static final String TRACE_ID = "x-trace-id";

    public static final String BATCH_ID = "x-batch-id";

    public static final String USER_ID = "x-user-id";

    // ----- 需处理的KEY end -----

    public static final String OS_TYPE = "x-os-type";

    public static final String OS_VERSION = "x-os-version";

    public static final String DEVICE_ID = "x-device-id";

    public static final String DEVICE_TOKEN = "x-device-token";

    public static final String ROM_VERSION = "x-rom-version";

    public static final String SCREEN_DPI = "x-screen-dpi";

    public static final String NET_TYPE = "x-net-type";

    public static final String NET_ISP = "x-net-isp";

    public static final String CLIENT_IP = "x-client-ip";

    public static final String APP_PLATFORM = "x-app-platform";

    public static final String APP_ID = "x-app-id";

    public static final String APP_NAME = "x-app-name";

    public static final String APP_VERSION = "x-app-version";

    public static final String ROUTE_CHANNEL = "x-route-channel";

    public static final String SCENE = "x-scene";

    public static final String CHANNEL = "x-channel";

    public static final String ACTIVITY_ID = "x-activity-id";

    public static final String ACTIVITY_CHANNEL = "x-activity-channel";

    public static final String TOKEN = "x-token";

    public static final String SIGN = "x-sign";

    public static final String TIMESTAMP = "x-timestamp";

    public static final String NONCE = "x-nonce";

    public static final String REQUEST_IP = "x-request-ip";

    public static final Set<String> ALL = Sets.newHashSet(TRACE_ID, BATCH_ID, USER_ID,
            OS_TYPE, OS_VERSION, DEVICE_ID, DEVICE_TOKEN, ROM_VERSION, SCREEN_DPI, NET_TYPE, NET_ISP, CLIENT_IP,
            APP_PLATFORM, APP_ID, APP_NAME, APP_VERSION, ROUTE_CHANNEL, SCENE, CHANNEL, ACTIVITY_ID, ACTIVITY_CHANNEL,
            TOKEN, SIGN, TIMESTAMP, NONCE, REQUEST_IP);

    public static boolean contains(String key) {
        return ALL.contains(key);
    }

}
