package com.minister.component.trace.context;

import cn.hutool.core.util.IdUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.minister.component.trace.entity.TraceEntity;
import com.minister.component.utils.context.HeadersContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * ThreadContext
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 12:12
 */
public class TraceContext {

    private static final TransmittableThreadLocal<TraceEntity> ENTITY = TransmittableThreadLocal.withInitial(TraceEntity::new);

    /**
     * 清空
     */
    public static void clean() {
        ENTITY.remove();
    }

    // ===== traceId =====

    /**
     * 添加 traceId
     *
     * @param traceId traceId
     */
    public static void setTraceId(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            return;
        }

        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity();
            ENTITY.set(traceEntity);
        }

        traceEntity.setTraceId(traceId);
    }

    /**
     * 获取 traceId
     */
    public static String getTraceId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            return null;
        }

        return traceEntity.getTraceId();
    }

    /**
     * 初始化 traceId
     */
    public static String initTraceId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity();
            ENTITY.set(traceEntity);
        }

        String traceId = IdUtil.fastSimpleUUID();

        traceEntity.setTraceId(traceId);
        HeadersContext.getHeaderEntity().setTraceId(traceId);

        return traceId;
    }

    /**
     * 判断 traceId 是否存在
     */
    public static boolean existsTraceId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            return false;
        }

        return StringUtils.isNotBlank(traceEntity.getTraceId());
    }

    // ===== threadId =====

    /**
     * 添加 threadId
     *
     * @param threadId threadId
     */
    public static void setThreadId(String threadId) {
        if (StringUtils.isBlank(threadId)) {
            return;
        }

        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity();
            ENTITY.set(traceEntity);
        }

        traceEntity.setThreadId(threadId);
    }

    /**
     * 获取 threadId
     */
    public static String getThreadId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            return null;
        }

        return traceEntity.getThreadId();
    }

    /**
     * 初始化 threadId
     */
    public static String initThreadId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            traceEntity = new TraceEntity();
            ENTITY.set(traceEntity);
        }

        String threadId = IdUtil.fastSimpleUUID();

        traceEntity.setThreadId(threadId);

        return threadId;
    }

    /**
     * 判断 threadId 是否存在
     */
    public static boolean existsThreadId() {
        TraceEntity traceEntity = ENTITY.get();
        if (Objects.isNull(traceEntity)) {
            return false;
        }

        return StringUtils.isNotBlank(traceEntity.getThreadId());
    }

}
