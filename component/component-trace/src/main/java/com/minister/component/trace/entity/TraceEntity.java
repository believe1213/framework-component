package com.minister.component.trace.entity;

import lombok.Data;

/**
 * 链路跟踪 Entity
 *
 * @author QIUCHANGQING620
 * @date 2021-10-25 09:21
 */
@Data
public class TraceEntity {

    /**
     * 链路id
     */
    private String traceId;

    /**
     * 线程跟踪id
     */
    private String threadId;

}
