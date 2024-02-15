package com.minister.component.utils;

import java.util.Map;

/**
 * 基于Method Key生成器
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 15:13
 */
@FunctionalInterface
public interface MethodKeyGenerator {

    /**
     * Generate a key for the given method parameters and method result.
     * this implements must be registered in spring container
     *
     * @param params the method parameters
     * @param result the method result
     * @return a generated key
     */
    String generate(Map<String, Object> params, Object result);

}
