package com.minister.component.utils.constants;

import java.util.concurrent.TimeUnit;

/**
 * TtlConstants
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 16:00
 */
public interface TtlConstants {

    long SEC_1_MINUTE = TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES);
    long SEC_2_MINUTES = TimeUnit.SECONDS.convert(2, TimeUnit.MINUTES);
    long SEC_3_MINUTES = TimeUnit.SECONDS.convert(3, TimeUnit.MINUTES);
    long SEC_5_MINUTES = TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES);
    long SEC_10_MINUTES = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES);
    long SEC_30_MINUTES = TimeUnit.SECONDS.convert(30, TimeUnit.MINUTES);
    long SEC_1_HOUR = TimeUnit.SECONDS.convert(1, TimeUnit.HOURS);
    long SEC_2_HOURS = TimeUnit.SECONDS.convert(2, TimeUnit.HOURS);
    long SEC_3_HOURS = TimeUnit.SECONDS.convert(3, TimeUnit.HOURS);
    long SEC_6_HOURS = TimeUnit.SECONDS.convert(6, TimeUnit.HOURS);
    long SEC_12_HOURS = TimeUnit.SECONDS.convert(12, TimeUnit.HOURS);
    long SEC_1_DAY = TimeUnit.SECONDS.convert(1, TimeUnit.DAYS);
    long SEC_2_DAYS = TimeUnit.SECONDS.convert(2, TimeUnit.DAYS);
    long SEC_3_DAYS = TimeUnit.SECONDS.convert(3, TimeUnit.DAYS);
    long SEC_7_DAYS = TimeUnit.SECONDS.convert(7, TimeUnit.DAYS);
    long SEC_15_DAYS = TimeUnit.SECONDS.convert(15, TimeUnit.DAYS);
    long SEC_30_DAYS = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS);

}
