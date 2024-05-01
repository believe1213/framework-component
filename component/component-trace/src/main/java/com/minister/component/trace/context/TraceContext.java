package com.minister.component.trace.context;

import cn.hutool.core.util.IdUtil;
import com.minister.component.trace.entity.TraceEntity;
import com.minister.component.utils.JacksonUtil;
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

    private static final ThreadLocal<TraceEntity> ENTITY = ThreadLocal.withInitial(TraceEntity::new);

    /**
     * 线程id
     */
    public static ThreadLocal<String> THREAD_ID = ThreadLocal.withInitial(String::new);

    /**
     * 清空
     */
    public static void clean() {
        ENTITY.remove();
    }

    /**
     * 清空
     */
    public static void cleanAll() {
        ENTITY.remove();
        THREAD_ID.remove();
    }

    // ===== threadId =====

    /**
     * 获取 threadId
     */
    public static String getThreadId() {
        return THREAD_ID.get();
    }

    /**
     * 初始化 threadId
     */
    public static String initThreadId() {
        String threadId = IdUtil.fastSimpleUUID();

        THREAD_ID.set(threadId);

        return threadId;
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
        HeadersContext.get().setTraceId(traceId);

        return traceId;
    }

    // ===== chainId =====

    /**
     * 添加 chainId
     *
     * @param chainId chainId
     */
    public static void putChainId(String chainId) {
        if (StringUtils.isBlank(chainId)) {
            return;
        }

        TraceEntity tracerEntity = ENTITY.get();
        if (Objects.isNull(tracerEntity)) {
            tracerEntity = new TraceEntity();
            ENTITY.set(tracerEntity);
        }

        tracerEntity.setChainId(chainId);
    }

    /**
     * 获取 chainId
     */
    public static String getChainId() {
        TraceEntity tracerEntity = ENTITY.get();
        if (Objects.isNull(tracerEntity)) {
            return null;
        }

        return tracerEntity.getChainId();
    }


    // ===== nodeId =====

    /**
     * 添加 nodeId
     *
     * @param nodeId nodeId
     */
    public static void putNodeId(String nodeId) {
        if (StringUtils.isBlank(nodeId)) {
            return;
        }

        TraceEntity tracerEntity = ENTITY.get();
        if (Objects.isNull(tracerEntity)) {
            tracerEntity = new TraceEntity();
            ENTITY.set(tracerEntity);
        }

        tracerEntity.setNodeId(nodeId);
    }

    /**
     * 获取 nodeId
     */
    public static String getNodeId() {
        TraceEntity tracerEntity = ENTITY.get();
        if (Objects.isNull(tracerEntity)) {
            return null;
        }

        return tracerEntity.getNodeId();
    }

    // ===== CustomTracerEntity =====

    public static TraceEntity copy() {
        TraceEntity tracerEntity = ENTITY.get();
        if (Objects.isNull(tracerEntity)) {
            return null;
        }
        return JacksonUtil.convertValue(tracerEntity, TraceEntity.class);
    }

    public static void set(TraceEntity tracerEntity) {
        ENTITY.set(tracerEntity);
    }

}
