package com.minister.component.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TestMsg
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 17:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMsg {

    /**
     * 名称
     */
    private String name;

    /**
     * 时间，格式yyyy-MM-dd HH:mm:ss
     */
    private String time;

}
